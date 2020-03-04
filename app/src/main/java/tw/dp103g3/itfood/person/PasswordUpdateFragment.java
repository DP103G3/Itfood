package tw.dp103g3.itfood.person;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
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
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import tw.dp103g3.itfood.R;
import tw.dp103g3.itfood.Url;
import tw.dp103g3.itfood.main.Common;
import tw.dp103g3.itfood.member.Member;
import tw.dp103g3.itfood.shop.Shop;
import tw.dp103g3.itfood.task.CommonTask;

import static androidx.constraintlayout.widget.Constraints.TAG;


public class PasswordUpdateFragment extends Fragment {
    private Activity activity;
    private SharedPreferences preferences;
    private NavController navController;
    private View view;
    private EditText etObsoletePassword, etPassword, etCheckPassword;
    private TextView tvObsoletePasswordWarning, tvPasswordWarning, tvCheckPasswordWarning;
    private Button btSend;
    private CommonTask getOrderTask, editOrderTask, loginTask;
    private Member member;
    private Toolbar toolbarChangPassword;
    int memberId, counts;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        preferences =
                activity.getSharedPreferences(Common.PREFERENCES_MEMBER, Context.MODE_PRIVATE);
        memberId = Common.getMemId(activity);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_password_update, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        Common.connectServer(activity, Common.getMemId(activity));
        etObsoletePassword = view.findViewById(R.id.etObsoletePassword);
        etPassword = view.findViewById(R.id.etPassword);
        etCheckPassword = view.findViewById(R.id.etCheckPassword);
        tvObsoletePasswordWarning = view.findViewById(R.id.tvObsoletePasswordWarning);
        tvPasswordWarning = view.findViewById(R.id.tvPasswordWarning);
        tvCheckPasswordWarning = view.findViewById(R.id.tvCheckPasswordWarning);
        btSend = view.findViewById(R.id.btSend);
        toolbarChangPassword = view.findViewById(R.id.toolbarChangPassword);

        toolbarChangPassword.setNavigationOnClickListener(v -> Navigation.findNavController(v).popBackStack());
        navController = Navigation.findNavController(view);


        final NavController navController = Navigation.findNavController(view);
        Bundle bundle = getArguments();
        if (bundle == null || bundle.getSerializable("member") == null) {
            Common.showToast(activity, R.string.textNoMembersFound);
            navController.popBackStack();
            return;
        }
        member = (Member) bundle.getSerializable("member");


        btSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                counts = 0;
                SharedPreferences pref = activity.getSharedPreferences(Common.PREFERENCES_MEMBER, Context.MODE_PRIVATE);
                String password = member.getMemPassword();
                String obsoletePassword = etObsoletePassword.getText().toString().trim();
                String newPassword = etPassword.getText().toString().trim();
                String checkPassword = etCheckPassword.getText().toString().trim();
                if (password != null){
                    //檢查新密碼
                    if (newPassword.length() <= 0){
                        tvPasswordWarning.setText("請輸入新密碼！");
                        counts++;
                    }else {
                        tvPasswordWarning.setText("");
                    }

                    //檢查確認密碼
                    if (checkPassword.length() <= 0){
                        tvCheckPasswordWarning.setText("請再輸入新密碼！");
                        counts++;
                    }else if (checkPassword.equals(newPassword)) {
                        tvCheckPasswordWarning.setText("");
                    }else {
                        tvCheckPasswordWarning.setText("密碼不相符！請確認後再試一次。");
                        counts++;
                    }

                    //檢查舊密碼
                    if (obsoletePassword.length() <= 0){
                        tvObsoletePasswordWarning.setText("請輸入舊密碼！");
                        counts++;
                    }else if (password.equals(obsoletePassword)) {
                        tvObsoletePasswordWarning.setText("");
                    }else {
                        tvObsoletePasswordWarning.setText("密碼錯誤！請確認後再試一次。");
                        counts++;
                    }

                    if(counts != 0){
                        counts = 0;
                        return;
                    }else {

                        if (Common.networkConnected(activity)) {

                            int id = memberId;
                            String url = Url.URL + "/MemberServlet";
                            if (Common.networkConnected(activity)) {
                                JsonObject jsonObject = new JsonObject();
                                Gson gson = new GsonBuilder().create();
                                jsonObject.addProperty("action", "getAccount");
                                jsonObject.addProperty("id", id);
                                String jsonOut = jsonObject.toString();
                                CommonTask getShopTask = new CommonTask(url, jsonOut);
                                try {
                                    String jsonIn = getShopTask.execute().get();
                                    member = gson.fromJson(jsonIn, Member.class);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Common.showToast(activity, R.string.textNoNetwork);
                            }

                            member.updatePassword(id, newPassword);
                            JsonObject jsonObject = new JsonObject();
                            jsonObject.addProperty("action", "updatePassword");
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
                                /* 回前一個Fragment */
                                navController.popBackStack();
                            }
                        } else {
                            Common.showToast(activity, R.string.textNoNetwork);
                        }
                    }



                }else {
                    navController.popBackStack(R.id.action_passwordUpdateFragment_to_personFragment, true);
                    Common.showToast(activity, "請登入後再試。");
                }


            }
        });



    }

}
