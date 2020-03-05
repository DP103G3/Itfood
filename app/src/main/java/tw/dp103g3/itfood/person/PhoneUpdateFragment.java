package tw.dp103g3.itfood.person;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import tw.dp103g3.itfood.main.Common;
import tw.dp103g3.itfood.R;
import tw.dp103g3.itfood.main.Url;
import tw.dp103g3.itfood.member.Member;
import tw.dp103g3.itfood.task.CommonTask;


public class PhoneUpdateFragment extends Fragment {
    private static final String TAG = "TAG_MemberPhoneUpdateFragment";
    private Activity activity;
    private Member member;
    private EditText etMemberPhone;
    private Button btOk;
    private TextView tvWarning;
    private Toolbar toolbarPhoneUpdate;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_phone_update, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        etMemberPhone = view.findViewById(R.id.etMemberPhone);
        btOk = view.findViewById(R.id.btOk);
        tvWarning = view.findViewById(R.id.tvWarning);
        toolbarPhoneUpdate = view.findViewById(R.id.toolbarPhoneUpdate);

        toolbarPhoneUpdate.setNavigationOnClickListener(v -> Navigation.findNavController(v).popBackStack());


        final NavController navController = Navigation.findNavController(view);
        Bundle bundle = getArguments();
        if (bundle == null || bundle.getSerializable("member") == null) {
            Common.showToast(activity, R.string.textNoMembersFound);
            navController.popBackStack();
            return;
        }
        member = (Member) bundle.getSerializable("member");

        btOk.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onClick(final View view) {
                int id = member.getMemId();

                String phone = etMemberPhone.getText().toString();
                if (phone.length() <= 0) {
                    Common.showToast(activity, R.string.textPhoneIsInvalid);
                    tvWarning.setText("請輸入電話號碼！");
                    return;
                }else if(phone.length() < 10){
                    tvWarning.setText("號碼數不足,請再確認！");
                    Common.showToast(activity, R.string.textPhoneError);
                    return;
                }

                if (Common.networkConnected(activity)) {
                    String url = Url.URL + "/MemberServlet";
                    member.UpdatePhone(id, phone);
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("action", "update");
                    jsonObject.addProperty("member", new Gson().toJson(member));
                    int count = 0;
                    try {
                        String result = new CommonTask(url, jsonObject.toString()).execute().get();
                        count = Integer.valueOf(result);
                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }
                    if (count == 0) {
                        Common.showToast(activity, R.string.textUpdateFail);
                    } else {
                        Common.showToast(activity, R.string.textUpdateSuccess);
                        navController.popBackStack();
                    }
                } else {
                    Common.showToast(activity, R.string.textNoNetwork);
                }

            }

        });


        showMember();
    }

    private void showMember(){
        etMemberPhone.setText(member.getMemPhone());
    }


}
