package tw.dp103g3.itfood.person;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.gson.JsonObject;

import org.w3c.dom.Text;

import tw.dp103g3.itfood.R;
import tw.dp103g3.itfood.main.Common;
import tw.dp103g3.itfood.main.Url;
import tw.dp103g3.itfood.task.CommonTask;

public class DelRegisterFragment extends Fragment {
    private static final String TAG = "TAG_RegisterFragment";
    private Activity activity;
    private ImageButton ibBack;
    private EditText etEmail, etPassword, etConfirm, etName, etPhone, etIdentityId;
    private Button btRegister;
    private TextView tvDeliveryDataInput;
    private String textEmail, textPassword, textName, textPhone, textIdentityId;
    private boolean emailCheck, passwordCheck, confirmCheck, nameCheck, phoneCheck, identityIdCheck;
    private CommonTask registerTask;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        emailCheck = false;
        passwordCheck = false;
        confirmCheck = false;
        nameCheck = false;
        phoneCheck = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register_del, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        handledViews(view);
        ibBack.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());

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
                confirmCheck = false;
                String textConfirm = s.toString().trim();
                if (textConfirm.isEmpty()) {
                    etConfirm.setError(getString(R.string.textNoEmpty));
                } else if (!textConfirm.equals(textPassword)) {
                    etConfirm.setError(getString(R.string.textConfirmError));
                } else {
                    confirmCheck = true;
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
        etIdentityId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                identityIdCheck = false;
                textIdentityId = s.toString().trim().toUpperCase();
                if (textIdentityId.isEmpty()) {
                    etIdentityId.setError(getString(R.string.textNoEmpty));
                } else if (!textIdentityId.matches(Common.REGEX_IDENTITY_ID)) {
                    etIdentityId.setError(getString(R.string.textIdentityIdFormatError));
                } else {
                    char[] identityIdChar = textIdentityId.toCharArray();
                    char letterChar = identityIdChar[0];
                    int letterInt;
                    switch (letterChar) {
                        case 'A': letterInt = 10; break;
                        case 'B': letterInt = 11; break;
                        case 'C': letterInt = 12; break;
                        case 'D': letterInt = 13; break;
                        case 'E': letterInt = 14; break;
                        case 'F': letterInt = 15; break;
                        case 'G': letterInt = 16; break;
                        case 'H': letterInt = 17; break;
                        case 'I': letterInt = 34; break;
                        case 'J': letterInt = 18; break;
                        case 'K': letterInt = 19; break;
                        case 'L': letterInt = 20; break;
                        case 'M': letterInt = 21; break;
                        case 'N': letterInt = 22; break;
                        case 'O': letterInt = 35; break;
                        case 'P': letterInt = 23; break;
                        case 'Q': letterInt = 24; break;
                        case 'R': letterInt = 25; break;
                        case 'S': letterInt = 26; break;
                        case 'T': letterInt = 27; break;
                        case 'U': letterInt = 28; break;
                        case 'V': letterInt = 29; break;
                        case 'W': letterInt = 32; break;
                        case 'X': letterInt = 30; break;
                        case 'Y': letterInt = 31; break;
                        default: letterInt = 33; break;
                    }
                    int[] identityIdInt = new int[11];
                    for (int i = 0; i < identityIdInt.length; i++) {
                        if (i == 0) {
                            identityIdInt[i] = letterInt / 10;
                        } else if (i == 1) {
                            identityIdInt[i] = letterInt % 10;
                        } else {
                            identityIdInt[i] = Integer.parseInt(String.valueOf(identityIdChar[i - 1]));
                        }
                    }
                    int[] check = {1, 9, 8, 7, 6, 5, 4, 3, 2 ,1, 1};
                    int sum = 0;
                    for (int i = 0; i < check.length; i++) {
                        sum += check[i] * identityIdInt[i];
                    }
                    identityIdCheck = sum % 10 == 0;
                    if (!identityIdCheck) {
                        etIdentityId.setError(getString(R.string.textIdentityIdFormatError));
                    }
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
        etIdentityId.setOnFocusChangeListener(this::focusChanged);
        btRegister.setOnClickListener(v -> {
            if (emailCheck && passwordCheck && confirmCheck && nameCheck && phoneCheck && identityIdCheck) {
                if (Common.networkConnected(activity)) {
                    String url = Url.URL + "/DeliveryServlet";
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("action", "insert");
                    Delivery delivery = new Delivery(textName, textPassword, textEmail, textPhone,
                            textIdentityId, 0, 1);
                    jsonObject.addProperty("delivery", Common.gson.toJson(delivery));
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
                    } else if (count == -2) {
                        Common.showToast(activity, R.string.textIdentityIdUsed);
                    } else {

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
        //設定按下文字後輸入預設外送員資料
         tvDeliveryDataInput.setOnClickListener(v -> {
            etEmail.setText(R.string.textDeliveryEmailInput);
            etPassword.setText(R.string.textPasswordInput);
            etConfirm.setText(R.string.textConfirmInput);
            etName.setText(R.string.textDeliveryNameInput);
            etPhone.setText(R.string.textPhoneInput);
            etIdentityId.setText(R.string.textIdentityIdInput);

        });

    }

    private void handledViews(View view) {
        ibBack = view.findViewById(R.id.ibBack);
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        etConfirm = view.findViewById(R.id.etConfirm);
        etName = view.findViewById(R.id.etName);
        etIdentityId = view.findViewById(R.id.etIdentityId);
        etPhone = view.findViewById(R.id.etPhone);
        btRegister = view.findViewById(R.id.btRegister);
        tvDeliveryDataInput = view.findViewById(R.id.tvDeliveryDataInput);

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
