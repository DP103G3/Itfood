package tw.dp103g3.itfood.payment;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import tw.dp103g3.itfood.Common;
import tw.dp103g3.itfood.R;
import tw.dp103g3.itfood.Url;
import tw.dp103g3.itfood.main.SharedViewModel;
import tw.dp103g3.itfood.task.CommonTask;

import static tw.dp103g3.itfood.Common.DATE_FORMAT;
import static tw.dp103g3.itfood.Common.PREFERENCES_MEMBER;


public class AddPaymentFragment extends Fragment {
    private String TAG = "TAG_AddPaymentFragment";
    private Activity activity;
    private SharedPreferences pref;
    private int mem_id;
    private CommonTask insertPaymentTask;
    private TextInputLayout etName, etCardNum, etExpirationDate, etPhone, etSafeCode;
    private String nameInput, cardNumInput, expirationDateInput, phoneInput, safeCodeInput;
    private Toolbar toolbarAddPayment;
    private CardView cardViewCheck;
    private SharedViewModel model;
    private Payment payment;
    private Drawable warning;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        pref = activity.getSharedPreferences(PREFERENCES_MEMBER, Context.MODE_PRIVATE);
        mem_id = pref.getInt("mem_id", 0);
        model = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        model.setExpirationDate(null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_payment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        toolbarAddPayment = view.findViewById(R.id.toolbarAddPayment);
        cardViewCheck = view.findViewById(R.id.cardViewCheck);
        etName = view.findViewById(R.id.etName);
        etExpirationDate = view.findViewById(R.id.etExpirationDate);
        etSafeCode = view.findViewById(R.id.etSafeCode);
        etPhone = view.findViewById(R.id.etPhone);
        etCardNum = view.findViewById(R.id.etCardNum);

        toolbarAddPayment.setNavigationOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        model.getSelectedExpirationDate().observe(getViewLifecycleOwner(), date -> {
            if (date != null) {
                etExpirationDate.getEditText().setText(date);
            }
        });

        etExpirationDate.getEditText().setOnClickListener(v -> {
            PaymentExpirationDatePicker picker = new PaymentExpirationDatePicker(activity, model);
            picker.show();
        });

        cardViewCheck.setOnClickListener(v -> {
            if (!validateSafeCode() | !validatePhone() | !validateName() | !validateExpirationDate() |
                    !validateCardNumber()) {
                return;
            } else {
                Payment payment = new Payment(0, null, mem_id, cardNumInput, expirationDateInput,
                        nameInput, safeCodeInput, phoneInput, 1);
                model.selectPayment(payment);
                String url = Url.URL + "/PaymentServlet";
                Gson gson = new GsonBuilder().setDateFormat(DATE_FORMAT).create();
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("action", "insert");
                jsonObject.addProperty("payment", gson.toJson(payment));
                int count = 0;

                try {
                    insertPaymentTask = new CommonTask(url, jsonObject.toString());
                    String result = insertPaymentTask.execute().get();
                    count = Integer.valueOf(result);
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }

                if (count == 0) {
                    Common.showToast(activity, R.string.addPaymentFail);
                } else {
                    Common.showToast(activity, R.string.addPaymentSuccess);
                    model.selectPayment(payment);
                    Navigation.findNavController(v).popBackStack();
                }
            }
        });

    }

    private boolean validateCardNumber() {
        cardNumInput = etCardNum.getEditText().getText().toString().trim();

        if (cardNumInput.isEmpty()) {
            etCardNum.setError("此欄位不可為空！");
            return false;
        } else if (cardNumInput.length() < 16) {
            etCardNum.setError("請輸入十六位數字");
            return false;
        } else if (!(cardNumInput.startsWith("5") || cardNumInput.startsWith("4"))) {
            etCardNum.setError("輸入的信用卡號碼無效");
            return false;
        } else {
            etCardNum.setError(null);
            return true;
        }
    }

    private boolean validatePhone() {
        phoneInput = etPhone.getEditText().getText().toString().trim();
        if (phoneInput.isEmpty()) {
            etPhone.setError("此欄位不可為空！");
            return false;
        } else if (phoneInput.length() < 10) {
            etPhone.setError("請輸入十位數字");
            return false;
        } else if (!phoneInput.startsWith("09")) {
            etPhone.setError("請輸入\"09\"的號碼");
            return false;
        } else {
            etPhone.setError(null);
            return true;
        }
    }

    private boolean validateSafeCode() {
        safeCodeInput = etSafeCode.getEditText().getText().toString().trim();
        if (safeCodeInput.isEmpty()) {
            etSafeCode.setError("此欄位不可為空！");
            return false;
        } else if (safeCodeInput.length() < 3) {
            etSafeCode.setError("安全碼為三位數字");
            return false;
        } else {
            etSafeCode.setError(null);
            return true;
        }
    }

    private boolean validateName() {
        nameInput = etName.getEditText().getText().toString().trim();
        if (nameInput.isEmpty()) {
            etName.setError("此欄位不可為空！");
            return false;
        } else if (nameInput.length() > 6) {
            etName.setError("姓名不可超過六個字");
            return false;
        } else {
            etName.setError(null);
            return true;
        }
    }

    private boolean validateExpirationDate() {
        expirationDateInput = etExpirationDate.getEditText().getText().toString().trim();
        if (expirationDateInput.isEmpty()) {
            etExpirationDate.setError("請選擇信用卡到期日");
            return false;
        } else {
            etExpirationDate.setError(null);
            return true;
        }
    }


}
