package tw.dp103g3.itfood.person;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tw.dp103g3.itfood.Common;
import tw.dp103g3.itfood.R;
import tw.dp103g3.itfood.Url;
import tw.dp103g3.itfood.member.Member;
import tw.dp103g3.itfood.task.CommonTask;


public class PersonalInfoFragment extends Fragment {
    private Activity activity;
    private ListAdapter memberAdapter, guestAdapter;
    private int memId;
    private int[] memberAction, guestAction;
    private ListView listView;
    private List<Map<String, Object>> memberList, guestList;
    private SharedPreferences preferences;
    private ImageView ivCart;
    private View view;
    private static final String TAG = "TAG_MemberUpdateFragment";
    private Member memberInfo;
    private CommonTask memberGetAllTask;
    private CommonTask memberDeleteTask;
    private TextView tvMemberName ,tvMemberPhone ,tvMemberEmail ,tvMemberAddress ,tvMemberCard;
    private ImageView ivMemberName ,ivMemberPhone ,ivMemberEmail ,ivMemberAddress ,ivMemberCard, ivMemberPassword;
    int memberId;
    private Gson gson;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        preferences =
                activity.getSharedPreferences(Common.PREFERENCES_MEMBER, Context.MODE_PRIVATE);
        memberId = Common.getMemId(activity);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_personal_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvMemberName = view.findViewById(R.id.tvMemberName);
        tvMemberPhone = view.findViewById(R.id.tvMemberPhone);
        //tvMemberEmail = view.findViewById(R.id.tvMemberEmail);
        //tvMemberAddress = view.findViewById(R.id.tvMemberAddress);
        //tvMemberCard = view.findViewById(R.id.tvMemberCard);
        ivMemberName = view.findViewById(R.id.ivMemberName);
        ivMemberPhone = view.findViewById(R.id.ivMemberPhone);
        //ivMemberEmail = view.findViewById(R.id.ivMemberEmail);
        //ivMemberAddress = view.findViewById(R.id.ivMemberAddress);
        //ivMemberCard = view.findViewById(R.id.ivMemberCard);
        ivMemberPassword = view.findViewById(R.id.ivMemberPassword);


        showMember();
    }


    @SuppressLint("LongLogTag")
    private void showMember() {

        int mem_id = memberId;
        memberInfo = null;
        if (Common.networkConnected(activity)) {
            String url = Url.URL + "/MemberServlet";
            JsonObject jsonObject = new JsonObject();
            gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
            jsonObject.addProperty("action", "getAccount");
            jsonObject.addProperty("mem_id" , mem_id);
            String jsonOut = jsonObject.toString();
            CommonTask getMemberTask = new CommonTask(url, jsonOut);
            try {
                String jsonIn = getMemberTask.execute().get();
                memberInfo = gson.fromJson(jsonIn, Member.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Common.showToast(activity, R.string.textNoNetwork);
        }

        /*memberAddress = null;
        if (Common.networkConnected(activity)) {
            String url = Url.URL + "/AddressServlet";
            JsonObject jsonObject = new JsonObject();
            Gson gsonAddress = new GsonBuilder().create();
            jsonObject.addProperty("action", "getAll");
            jsonObject.addProperty("mem_id" , mem_id);
            String jsonOut = jsonObject.toString();
            CommonTask getAddressTask = new CommonTask(url, jsonOut);
            try {
                String jsonIn = getAddressTask.execute().get();
                memberAddress = gsonAddress.fromJson(jsonIn, Address.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Common.ShowToast(activity, R.string.textNoNetwork);
        }*/

        tvMemberName.setText(memberInfo.getMemName());
        tvMemberPhone.setText(memberInfo.getMemPhone());
        //tvMemberEmail.setText(memberInfo.getMemEmail());
        /*if (memberAddress == null){

        }else {
            tvMemberAddress.setText(memberAddress.getInfo());
        }*/

        //tvMemberCard.setText(memberInfo.getMemEmail());
        ivMemberName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("member", memberInfo);
                Navigation.findNavController(view)
                        .navigate(R.id.action_personalInfoFragment_to_nameUpdateFragment, bundle);

            }
        });
        ivMemberPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("member", memberInfo);
                Navigation.findNavController(view)
                        .navigate(R.id.action_personalInfoFragment_to_phoneUpdateFragment, bundle);

            }
        });
        /*ivMemberEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("member", memberInfo);
                Navigation.findNavController(view)
                        .navigate(R.id.action_memberUpdateFragment_to_memberEmailUpdateFragment, bundle);

            }
        });
        ivMemberAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {


            }
        });
        ivMemberCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {


            }
        });*/
        ivMemberPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("member", memberInfo);
                Navigation.findNavController(view)
                        .navigate(R.id.action_personalInfoFragment_to_passwordUpdateFragment, bundle);

            }
        });

    }


}
