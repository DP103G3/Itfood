package tw.dp103g3.itfood.shopping_cart;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import tw.dp103g3.itfood.Common;
import tw.dp103g3.itfood.R;
import tw.dp103g3.itfood.Url;
import tw.dp103g3.itfood.member.Member;
import tw.dp103g3.itfood.task.CommonTask;

import static tw.dp103g3.itfood.Common.DATE_FORMAT;
import static tw.dp103g3.itfood.Common.PREFERENCES_MEMBER;

public class LoginDialogFragment extends DialogFragment {
    private String TAG = "TAG_LoginDialogFragment";
    private TextInputLayout textInputLayoutPassword;
    private TextInputLayout textInputLayoutUsername;
    private Button btCancel;
    private Button btLogin;
    private Button btSignUp;
    private View view;
    private CommonTask getMemberTask;
    private Member member;
    private Activity activity;
    private SharedPreferences pref;
    Boolean isSuccessful;
    LoginDialogContract mHost;

    public interface LoginDialogContract {
        void sendLoginResult(boolean isSuccessful);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mHost = (LoginDialogContract) getTargetFragment();
        } catch (ClassCastException e) {
            Log.e(TAG, e.toString());
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        activity = getActivity();
        pref = activity.getSharedPreferences(PREFERENCES_MEMBER, Context.MODE_PRIVATE);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.login_alert_dialog, null);
        builder.setView(view);

        Dialog dialog = builder.create();

        btLogin = view.findViewById(R.id.btLogin);
        btCancel = view.findViewById(R.id.btCancel);
        btSignUp = view.findViewById(R.id.btSignUp);
        textInputLayoutUsername = view.findViewById(R.id.textInputLayoutUsername);
        textInputLayoutPassword = view.findViewById(R.id.textInputLayoutPassword);

        btLogin.setOnClickListener(v -> {
            if (!validateUsername() || !validatePassword()) {
                isSuccessful = false;
                mHost.sendLoginResult(isSuccessful);
                return;
            } else {
                pref.edit().putInt("mem_id", member.getMemId()).apply();
                pref.edit().putString("mem_password", member.getMemPassword()).apply();
                Common.showToast(activity, "登入成功");
                isSuccessful = true;
                mHost.sendLoginResult(isSuccessful);
                dismiss();
            }
        });


        btCancel.setOnClickListener(v -> {
            dialog.cancel();
            isSuccessful = false;
        });

        //TODO 加入註冊頁面
//        btSignUp.setOnClickListener(v -> {
//
//        });

        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private boolean validateUsername() {
        String username = textInputLayoutUsername.getEditText().getText().toString().trim();
        member = getMember(username);
        textInputLayoutPassword.setError(null);

        if (username.isEmpty()) {
            textInputLayoutUsername.setError("請輸入帳號 (電子信箱)");
            return false;
        } else if (!username.contains("@")) {
            textInputLayoutUsername.setError("帳號為電子信箱格式");
            return false;
        } else if (member == null || member.getMemId() == 0) {
            textInputLayoutUsername.setError("此帳戶不存在，請確認輸入是否正確");
            return false;
        } else {
            textInputLayoutUsername.setError(null);
            return true;
        }

    }

    private boolean validatePassword() {
        String password = textInputLayoutPassword.getEditText().getText().toString().trim();
        if (password.isEmpty()) {
            textInputLayoutPassword.setError("請輸入密碼");
            return false;
        } else if (!password.equals(member.getMemPassword())) {
            textInputLayoutPassword.setError("密碼錯誤，請檢查是否正確");
            return false;
        } else if (member.getMemState() == 0) {
            textInputLayoutPassword.setError("此帳號已刪除或是被停權");
            return false;
        } else {
            textInputLayoutPassword.setError(null);
            return true;
        }
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
    }

    private Member getMember(String email) {
        String url = Url.URL + "/MemberServlet";
        JsonObject jsonObject = new JsonObject();
        Gson gson = new GsonBuilder().setDateFormat(DATE_FORMAT).create();
        jsonObject.addProperty("action", "findByEmail");
        jsonObject.addProperty("mem_email", email);
        String jsonOut = jsonObject.toString();

        if (Common.networkConnected(activity)) {
            try {
                getMemberTask = new CommonTask(url, jsonOut);
                String jsonIn = getMemberTask.execute().get();
                member = gson.fromJson(jsonIn, Member.class);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Common.showToast(activity, R.string.textNoNetwork);
        }

        return member;
    }
}
