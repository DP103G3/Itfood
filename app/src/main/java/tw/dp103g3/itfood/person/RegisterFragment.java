package tw.dp103g3.itfood.person;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.gson.JsonObject;

import tw.dp103g3.itfood.main.Common;
import tw.dp103g3.itfood.R;
import tw.dp103g3.itfood.main.Url;
import tw.dp103g3.itfood.member.Member;
import tw.dp103g3.itfood.task.CommonTask;

public class RegisterFragment extends Fragment {
    private static final String TAG = "TAG_RegisterFragment";
    private Activity activity;
    private EditText etEmail, etPassword, etConfirm, etName, etPhone;
    private Button btRegister;
    private ImageButton ibBack;
    private TextView tvMemberDataInput;
    private String textEmail, textPassword, textName, textPhone;
    private boolean emailCheck, passwordCheck, confrimCheck, nameCheck, phoneCheck;
    private CommonTask registerTask;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        emailCheck = false;
        passwordCheck = false;
        confrimCheck = false;
        nameCheck = false;
        phoneCheck = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        handledViews(view);
        ibBack = view.findViewById(R.id.ibBack);
        ibBack.setOnClickListener(v -> {
            Navigation.findNavController(view).popBackStack();
        });
        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                emailCheck = false;
                textEmail = s.toString().trim();
                if (textEmail.matches("^.*[A-Z]+(.+[A-Z]+)*.*$")) {
                    textEmail = textEmail.toLowerCase();
                    etEmail.setText(textEmail);
                    etEmail.setSelection(textEmail.length());
                }
                if (textEmail.isEmpty()) {
                    etEmail.setError(getString(R.string.textNoEmpty));
                } else if (!textEmail.matches(Common.REGEX_EMAIL)) {
                    etEmail.setError(getString(R.string.textEmailFormatError));
                } else {
                    emailCheck = true;
                }
            }
        });
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                passwordCheck = false;
                textPassword = s.toString().trim();
                if (textPassword.isEmpty()) {
                    etPassword.setError(getString(R.string.textNoEmpty));
                } else {
                    passwordCheck = true;
                }
            }
        });
        etConfirm.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                confrimCheck = false;
                String textConfirm = s.toString().trim();
                if (textConfirm.isEmpty()) {
                    etConfirm.setError(getString(R.string.textNoEmpty));
                } else if (!textConfirm.equals(textPassword)) {
                    etConfirm.setError(getString(R.string.textConfirmError));
                } else {
                    confrimCheck = true;
                }
            }
        });
        etName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                nameCheck = false;
                textName = s.toString().trim();
                if (textName.isEmpty()) {
                    etName.setError(getString(R.string.textNoEmpty));
                } else {
                    nameCheck = true;
                }
            }
        });
        etPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                phoneCheck = false;
                textPhone = s.toString().trim();
                if (textPhone.isEmpty()) {
                    etPhone.setError(getString(R.string.textNoEmpty));
                } else if (!textPhone.matches(Common.REGEX_PHONE)) {
                    etPhone.setError(getString(R.string.textPhoneFormatError));
                } else {
                    phoneCheck = true;
                }
            }
        });
        etEmail.setOnFocusChangeListener(this::focusChanged);
        etPassword.setOnFocusChangeListener(this::focusChanged);
        etConfirm.setOnFocusChangeListener(this::focusChanged);
        etName.setOnFocusChangeListener(this::focusChanged);
        etPhone.setOnFocusChangeListener(this::focusChanged);
        btRegister.setOnClickListener(v -> {
            if (emailCheck && passwordCheck && confrimCheck && nameCheck && phoneCheck) {
                if (Common.networkConnected(activity)) {
                    String url = Url.URL + "/MemberServlet";
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("action", "insert");
                    Member member = new Member(0, textName, textPassword, textEmail, textPhone,
                            null, 1);
                    jsonObject.addProperty("member", Common.gson.toJson(member));
                    registerTask = new CommonTask(url, jsonObject.toString());
                    int count = 0;
                    try {
                        String result = registerTask.execute().get();
                        count = Integer.parseInt(result);
                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }
                    if (count == -1) {
                        Common.showToast(activity, R.string.textEmailUsed);
                    } else if (count == 0) {
                        Common.showToast(activity, R.string.textRegisterFail);
                    } else {
                        SharedPreferences pref = activity.getSharedPreferences(
                                Common.PREFERENCES_MEMBER, Context.MODE_PRIVATE);
                        pref.edit().putInt("mem_id", count)
                                .putString("mem_password", textPassword).apply();
                        Common.showToast(activity, R.string.textRegisterSuccess);
                        Navigation.findNavController(v).popBackStack(R.id.mainFragment, false);
                    }
                } else {
                    Common.showToast(activity, R.string.textNoNetwork);
                }
            } else {
                Common.showToast(activity, R.string.textCheckEditText);
            }
        });
        //設定按下文字後輸入預設會員資料
        tvMemberDataInput.setOnClickListener(v -> {
            etEmail.setText(R.string.textEmailInput);
            etPassword.setText(R.string.textPasswordInput);
            etConfirm.setText(R.string.textConfirmInput);
            etName.setText(R.string.textNameInput);
            etPhone.setText(R.string.textPhoneInput);
        });
    }

    private void handledViews(View view) {
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        etConfirm = view.findViewById(R.id.etConfirm);
        etName = view.findViewById(R.id.etName);
        etPhone = view.findViewById(R.id.etPhone);
        btRegister = view.findViewById(R.id.btRegister);
        tvMemberDataInput = view.findViewById(R.id.tvMemberDataInput);
    }

    private void focusChanged(View v, boolean hasFocus) {
        EditText e = (EditText) v;
        if (!hasFocus && e.getText().length() == 0) {
            if (e.getError() == null) {
                e.setError(getString(R.string.textNoEmpty));
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (registerTask != null) {
            registerTask.cancel(true);
            registerTask = null;
        }
    }
}
