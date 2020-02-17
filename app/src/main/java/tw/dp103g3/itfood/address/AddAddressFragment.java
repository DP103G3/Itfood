package tw.dp103g3.itfood.address;


import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.List;

import tw.dp103g3.itfood.Common;
import tw.dp103g3.itfood.R;
import tw.dp103g3.itfood.Url;
import tw.dp103g3.itfood.main.SharedViewModel;
import tw.dp103g3.itfood.task.CommonTask;

import static tw.dp103g3.itfood.Common.DATE_FORMAT;


public class AddAddressFragment extends Fragment {
    private EditText etCity, etAdminDistrict, etAddressName, etAddressDetail;
    private CardView cardViewConfirm;
    private Activity activity;
    private SharedViewModel model;
    private Toolbar toolbarAddAddress;
    private City city;
    private District district;
    private SharedPreferences pref;
    private int mem_id;
    private CommonTask getAddressTask;
    private String TAG = "TAG_AddAddressFragment";
    private BottomNavigationView bottomNavigationView;
    private Animator animator;


    public AddAddressFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        pref = activity.getSharedPreferences(Common.PREFERENCES_MEMBER, Context.MODE_PRIVATE);
        mem_id = pref.getInt("mem_id", Common.LOGIN_FALSE);

        model = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        model.selectCity(null);
        model.selectDistrict(null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_address, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        city = null;
        district = null;

        setViews(view);

        toolbarAddAddress.setNavigationOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        Toast toast = Toast.makeText(activity, "請選擇城市", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        etAdminDistrict.setOnClickListener(v -> toast.show());

        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                View view = activity.getCurrentFocus();
                if (view != null) {
                    InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                return true;
            }
        });

        model.getSelectedCity().observe(getViewLifecycleOwner(), city -> {
            this.city = city;
            if (city != null) {
                etCity.setText(city.getName());
                etAdminDistrict.setOnClickListener(null);
                etAdminDistrict.setOnClickListener(v -> {
                    Navigation.findNavController(v).navigate(R.id.action_addAddressFragment_to_addressDistrictsFragment);
                    View view1 = activity.getCurrentFocus();
                    if (view1 != null) {
                        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                });
            }
        });

        model.getSelectedDistrict().observe(getViewLifecycleOwner(), district -> {
            this.district = district;
            if (district != null) {
                etAdminDistrict.setText(district.getName());
            }
        });

        etCity.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_addAddressFragment_to_addressCitiesFragment);
            model.selectDistrict(null);
            etAdminDistrict.setText("");
            View view2 = activity.getCurrentFocus();
            if (view2 != null) {
                InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        });

        cardViewConfirm.setOnClickListener(v -> {
            String addressDetail = etAddressDetail.getText().toString();
            String addressName = etAddressName.getText().toString();
            if (district == null || city == null || addressDetail.isEmpty() || addressName.isEmpty()) {
                Toast.makeText(activity, "請確實填寫每一項目", Toast.LENGTH_SHORT).show();
            } else {
                String locationName;
                StringBuilder sb = new StringBuilder();
                sb.append(city.getName()).append(district.getName()).append(addressDetail);
                locationName = sb.toString();
                android.location.Address newAddress = geocode(locationName);
                if (newAddress == null) {
                    Toast.makeText(activity, "查無此地址", Toast.LENGTH_SHORT).show();
                } else {
                    String url = Url.URL + "/AddressServlet";
                    Address address = new Address(0, mem_id, addressName, locationName, 1, newAddress.getLatitude(), newAddress.getLongitude());
                    JsonObject jsonObject = new JsonObject();
                    Gson gson = new GsonBuilder().setDateFormat(DATE_FORMAT).create();
                    jsonObject.addProperty("action", "insert");
                    jsonObject.addProperty("address", gson.toJson(address));
                    String jsonOut = jsonObject.toString();

                    int count = 0;
                    try {
                        getAddressTask = new CommonTask(url, jsonOut);
                        String result = getAddressTask.execute().get();
                        count = Integer.valueOf(result);
                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }

                    if (count == 0) {
                        Toast.makeText(activity, "新增失敗，請稍後再試", Toast.LENGTH_SHORT).show();
                    } else {
                        Common.showToast(activity, "新增地址成功");
                        Navigation.findNavController(v).popBackStack();
                    }

                }

            }
        });

    }

    private void setViews(View view) {
        etCity = view.findViewById(R.id.etCity);
        etAdminDistrict = view.findViewById(R.id.etAdminDistrict);
        etAddressName = view.findViewById(R.id.etAddressName);
        etAddressDetail = view.findViewById(R.id.etAddressDetail);
        cardViewConfirm = view.findViewById(R.id.cardViewConfirm);
        toolbarAddAddress = view.findViewById(R.id.toolbarAddAddress);
    }

    private android.location.Address geocode(String locationName) {
        Geocoder geocoder = new Geocoder(activity);
        List<android.location.Address> addressList = null;
        try {
            addressList = geocoder.getFromLocationName(locationName, 1);
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }

        if (addressList == null || addressList.isEmpty()) {
            return null;
        } else {
            return addressList.get(0);
        }
    }

    private void handleViews() {
        bottomNavigationView = activity.findViewById(R.id.bottomNavigation);
        if (bottomNavigationView.getVisibility() == View.VISIBLE) {
            animator = AnimatorInflater.loadAnimator(activity, R.animator.anim_bottom_navigation_slide_down);
            animator.setTarget(bottomNavigationView);
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    bottomNavigationView.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            animator.start();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        handleViews();
    }
}
