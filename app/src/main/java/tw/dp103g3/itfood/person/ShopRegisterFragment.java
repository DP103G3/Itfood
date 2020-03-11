package tw.dp103g3.itfood.person;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Locale;

import tw.dp103g3.itfood.R;
import tw.dp103g3.itfood.main.Common;
import tw.dp103g3.itfood.main.Url;
import tw.dp103g3.itfood.shop.Shop;
import tw.dp103g3.itfood.task.AddressTask;
import tw.dp103g3.itfood.task.CommonTask;

public class ShopRegisterFragment extends Fragment {
    private static final String TAG = "TAG_RegisterFragment";
    private static final int REQ_TAKE_PICTURE = 0;
    private static final int REQ_PICK_PICTURE = 1;
    private static final int REQ_CROP_PICTURE = 2;
    private Uri contentUri;
    private Activity activity;
    private ImageButton ibBack;
    private ImageView ivShop;
    private byte[] image;
    private Button btTakePicture, btPickPicture, btRegister;
    private EditText etEmail, etPassword, etConfirm, etName, etPhone, etTax, etAddress, etInfo;
    private TextInputLayout tilInfo;
    private ProgressBar progressBar;
    private String textEmail, textPassword, textName, textPhone, textTax, textAddress;
    private boolean emailCheck, passwordCheck, confirmCheck, nameCheck, phoneCheck, taxCheck, addressCheck;
    private CommonTask registerTask;
    private AddressTask addressTask;
    private TextView tvShopDataInput;
    private android.location.Address address;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        emailCheck = false;
        passwordCheck = false;
        confirmCheck = false;
        nameCheck = false;
        phoneCheck = false;
        taxCheck = false;
        addressCheck = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register_shop, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        handledViews(view);
        activity.findViewById(R.id.bottomNavigation).setVisibility(View.GONE);
        ibBack.setOnClickListener(v ->
                Navigation.findNavController(v).popBackStack());
        btTakePicture.setOnClickListener(v -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File file = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            file = new File(file, "picture.jpg");
            contentUri = FileProvider.getUriForFile(
                    activity, activity.getPackageName() + ".provider", file);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);
            if (intent.resolveActivity(activity.getPackageManager()) != null) {
                checkPermission(intent);
            } else {
                Common.showToast(activity, R.string.textNoCameraAppFound);
            }
        });
        btPickPicture.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQ_PICK_PICTURE);
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
        etTax.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                taxCheck = false;
                textTax = s.toString().trim();
                if (textTax.isEmpty()) {
                    etTax.setError(getString(R.string.textNoEmpty));
                } else if (!textTax.matches("^[0-9]{8}$")) {
                    etTax.setError(getString(R.string.textTaxFormatError));
                } else {
                    char[] taxChar = textTax.toCharArray();
                    int tax7 = Integer.parseInt(String.valueOf(taxChar[6]));
                    int[] check = {1, 2, 1, 2, 1, 2, 4, 1};
                    int sum = 0;
                    for (int i = 0; i < taxChar.length; i++) {
                        int taxNum = Integer.parseInt(String.valueOf(taxChar[i]));
                        check[i] *= taxNum;
                        if (check[i] >= 10) {
                            check[i] = check[i] / 10 + check[i] % 10;
                        }
                        sum += check[i];
                    }
                    taxCheck = tax7 == 7 ? sum % 10 == 0 || (sum + 1) % 10 == 0 : sum % 10 == 0;
                    if (!taxCheck) {
                        etTax.setError(getString(R.string.textTaxFormatError));
                    }
                }
            }
        });
        etAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                addressCheck = false;
                textAddress = s.toString().trim();
                if (textAddress.isEmpty()) {
                    etAddress.setError(getString(R.string.textNoEmpty));
                }
            }
        });
        etInfo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() != 0) {
                    tilInfo.setHint(String.format(Locale.getDefault(), "%s %d / 200",
                            activity.getString(R.string.textInfo), s.length()));
                } else {
                    tilInfo.setHint(activity.getString(R.string.textInfoUnnecessary));
                }
            }
        });
        etEmail.setOnFocusChangeListener(this::focusChanged);
        etPassword.setOnFocusChangeListener(this::focusChanged);
        etConfirm.setOnFocusChangeListener(this::focusChanged);
        etName.setOnFocusChangeListener(this::focusChanged);
        etPhone.setOnFocusChangeListener(this::focusChanged);
        etTax.setOnFocusChangeListener(this::focusChanged);
        etAddress.setOnFocusChangeListener((v, hasFocus) -> {
            EditText e = (EditText) v;
            if (hasFocus) {
                progressBar.setVisibility(View.GONE);
            } else if (textAddress == null || textAddress.isEmpty()) {
                e.setError(getString(R.string.textNoEmpty));
            } else {
                addressTask = new AddressTask(activity, textAddress, ShopRegisterFragment.this);
                try {
                    progressBar.setVisibility(View.VISIBLE);
                    addressTask.execute();
                } catch (Exception ex) {
                    Log.e(TAG, e.toString());
                }
            }
        });
        btRegister.setOnClickListener(v -> {
            if (emailCheck && passwordCheck && confirmCheck && nameCheck && phoneCheck && addressCheck) {
                if (Common.networkConnected(activity)) {
                    String url = Url.URL + "/ShopServlet";
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("action", "insert");
                    String textInfo = etInfo.getText().toString();
                    Shop shop = new Shop(0, textEmail, textPassword, textName, textPhone, textTax, textAddress,
                            address.getLatitude(), address.getLongitude(), 1, (byte) 0,
                            textInfo, null, null, 0, 0);
                    jsonObject.addProperty("shop", Common.gson.toJson(shop));
                    if (image != null) {
                        jsonObject.addProperty("imageBase64",
                                Base64.encodeToString(image, Base64.DEFAULT));
                    }
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

        //設定按下文字後輸入預設店家資料
        tvShopDataInput.setOnClickListener(v -> {
            etEmail.setText(R.string.textShopEmailInput);
            etPassword.setText(R.string.textPasswordInput);
            etConfirm.setText(R.string.textConfirmInput);
            etName.setText(R.string.textShopNameInput);
            etPhone.setText(R.string.textPhoneInput);
            etTax.setText(R.string.textTaxInput);
            etAddress.setText(R.string.textAddressInput);
            etInfo.setText(R.string.textInfoInput);
        });
    }

    private void handledViews(View view) {
        ibBack = view.findViewById(R.id.ibBack);
        ivShop = view.findViewById(R.id.ivShop);
        btTakePicture = view.findViewById(R.id.btTakePicture);
        btPickPicture = view.findViewById(R.id.btPickPicture);
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        etConfirm = view.findViewById(R.id.etConfirm);
        etName = view.findViewById(R.id.etName);
        etPhone = view.findViewById(R.id.etPhone);
        etTax = view.findViewById(R.id.etTax);
        etAddress = view.findViewById(R.id.etAddress);
        progressBar = view.findViewById(R.id.progressBar);
        tilInfo = view.findViewById(R.id.tilInfo);
        etInfo = view.findViewById(R.id.etInfo);
        btRegister = view.findViewById(R.id.btRegister);
        tvShopDataInput = view.findViewById(R.id.tvShopDataInput);
    }

    private void checkPermission(Intent intent) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA)) {
                Common.showToast(activity, R.string.textOpenCameraPermission);
            } else {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, 100);
            }
        } else {
            startActivityForResult(intent, REQ_TAKE_PICTURE);
        }
    }

    public void setAddressCheck(boolean addressCheck) {
        this.addressCheck = addressCheck;
    }

    public void setAddress(Address address) {
        this.address = address;
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQ_TAKE_PICTURE:
                    crop(contentUri);
                    break;
                case REQ_PICK_PICTURE:
                    crop(data.getData());
                    break;
                case REQ_CROP_PICTURE:
                    Uri uri = data.getData();
                    Bitmap bitmap = null;
                    if (uri != null) {
                        try {
                            bitmap = BitmapFactory.decodeStream(
                                    activity.getContentResolver().openInputStream(uri));
                            ByteArrayOutputStream out = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                            image = out.toByteArray();
                        } catch (FileNotFoundException e) {
                            Log.e(TAG, e.toString());
                        }
                    }
                    if (bitmap != null) {
                        ivShop.setImageBitmap(bitmap);
                    } else {
                        ivShop.setImageResource(R.drawable.no_image);
                    }
                    break;
            }
        }
    }

    private void crop(Uri sourceImageUri) {
        File file = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        file = new File(file, "picture_cropped.jpg");
        Uri uri = Uri.fromFile(file);
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(sourceImageUri, "image/*");
        intent.putExtra("crop", true);
        intent.putExtra("aspectX", 0);
        intent.putExtra("aspectY", 0);
        intent.putExtra("outputX", 0);
        intent.putExtra("outputY", 0);
        intent.putExtra("scale", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        intent.putExtra("return-data", true);
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            startActivityForResult(intent, REQ_CROP_PICTURE);
        } else {
            Common.showToast(activity, R.string.textNoCropAppFound);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (registerTask != null) {
            registerTask.cancel(true);
            registerTask = null;
        }
        if (addressTask != null) {
            addressTask.cancel(true);
            addressTask = null;
        }
    }
}
