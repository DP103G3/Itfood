package tw.dp103g3.itfood.address;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import tw.dp103g3.itfood.Common;
import tw.dp103g3.itfood.R;
import tw.dp103g3.itfood.Url;
import tw.dp103g3.itfood.task.CommonTask;

import static tw.dp103g3.itfood.Common.DATE_FORMAT;
import static tw.dp103g3.itfood.Common.PREFERENCES_MEMBER;


public class AddressFragment extends Fragment {
    @BindView(R.id.rvAddress)
    RecyclerView rvAddress;
    @BindView(R.id.toolbarLocation)
    Toolbar toolbarLocation;
    @BindView(R.id.fabAdd)
    FloatingActionButton fabAdd;
    private Activity activity;
    private SharedPreferences pref;
    private int mem_id;
    private List<Address> addresses;
    private String TAG = "TAG_AddressFragment";


    public AddressFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        pref = activity.getSharedPreferences(PREFERENCES_MEMBER, Context.MODE_PRIVATE);
        mem_id = pref.getInt("mem_id", 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_address, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        toolbarLocation.setNavigationOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        fabAdd.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_addressFragment_to_addAddressFragment));
    }

    private List<Address> getAddresses(int mem_id) {
        List<Address> addresses = new ArrayList<>();
        if (Common.networkConnected(activity)) {
            String url = Url.URL + "/AddressServlet";
            JsonObject jsonObject = new JsonObject();
            Gson gson = new GsonBuilder().setDateFormat(DATE_FORMAT).create();
            jsonObject.addProperty("action", "getAllShow");
            jsonObject.addProperty("mem_id", mem_id);
            String jsonOut = jsonObject.toString();
            try {
                String jsonIn = new CommonTask(url, jsonOut).execute().get();
                Type listType = new TypeToken<List<Address>>() {
                }.getType();
                addresses = gson.fromJson(jsonIn, listType);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Common.showToast(activity, R.string.textNoNetwork);
        }
        return addresses;
    }

    private void ShowAddress(List<Address> addresses) {
        AddressFragment.AddressAdapter addressAdapter = (AddressFragment.AddressAdapter) rvAddress.getAdapter();
        if (addressAdapter == null) {
            rvAddress.setAdapter(new AddressFragment.AddressAdapter(activity, addresses));
        } else {
            addressAdapter.setAddresses(addresses);
            addressAdapter.notifyDataSetChanged();
        }
    }

    class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.MyViewHolder> {
        private Context context;
        private List<Address> addresses;

        public AddressAdapter(Context context, List<Address> addresses) {
            this.addresses = addresses;
            this.context = context;
        }

        void setAddresses(List<Address> addresses) {
            this.addresses = addresses;
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.tvAddressName)
            TextView tvAddressName;
            @BindView(R.id.tvAddressDetail)
            TextView tvAddressDetail;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }

        @Override
        public int getItemCount() {
            return addresses.size();
        }

        @NonNull
        @Override
        public AddressFragment.AddressAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(context)
                    .inflate(R.layout.address_list_item_view_detail, parent, false);
            return new AddressFragment.AddressAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull AddressFragment.AddressAdapter.MyViewHolder holder, int position) {
                final Address address = addresses.get(position);
                Gson gson = Common.gson;
                String url = Url.URL + "/AddressServlet";
                holder.tvAddressName.setText(address.getName());
                holder.tvAddressDetail.setText(address.getInfo());
                holder.itemView.setOnClickListener(v -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    String[] options = new String[]{"刪除", "編輯"};
                    builder.setTitle("送餐地址 「" + address.getName() + "」" );
                    builder.setItems(options, (dialog, which) -> {
                        if (which == 0){
                            AlertDialog.Builder bd = new AlertDialog.Builder(activity);
                            bd.setTitle("刪除送餐地址" + "「" + address.getName() + "」");
                            bd.setMessage("你確定要刪除這筆送餐地址？");
                            bd.setPositiveButton("確定", (dialog12, which12) -> {
                                JsonObject jsonObject = new JsonObject();
                                address.setState(0);
                                jsonObject.addProperty("action", "update");
                                jsonObject.addProperty("address", gson.toJson(address, Address.class));
                                String jsonOut = jsonObject.toString();
                                int count = 0;
                                if (Common.networkConnected(activity)){
                                    try {
                                        count = Integer.parseInt(new CommonTask(url, jsonOut).execute().get());
                                    } catch (Exception e){
                                        Log.e(TAG, e.toString());
                                    }
                                } else {
                                    Common.showToast(activity, R.string.textNoNetwork);
                                }
                                if (count != 0){
                                    Common.showToast(activity, R.string.textDeleteSuccess);
                                    dialog12.dismiss();
                                } else {
                                    Common.showToast(activity, R.string.textDeleteFail);
                                }
                            });
                            bd.setNegativeButton("取消", (dialog1, which1) -> dialog1.cancel());
                            Dialog dialog1 = bd.create();
                            Common.setDialogUi(dialog1, activity);
                            dialog1.show();
                            dialog.dismiss();
                        } else {
                            TextInputLayout textInputLayoutAddressName;
                            TextInputLayout textInputLayoutAddressInfo;
                            View dialogView = View.inflate(activity, R.layout.dialog_edit_address, null);
                            textInputLayoutAddressInfo = dialogView.findViewById(R.id.textInputLayoutAddressInfo);
                            textInputLayoutAddressName = dialogView.findViewById(R.id.textInputLayoutAddressName);
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(activity);
                            builder1.setView(dialogView);

                            builder1.setPositiveButton("確定", (dialog13, which13) -> {
                                String addressName = textInputLayoutAddressName.getEditText().getText().toString().trim();
                                String addressInfo = textInputLayoutAddressInfo.getEditText().getText().toString().trim();
                                if (addressName.isEmpty()){
                                    textInputLayoutAddressInfo.setError(null);
                                    textInputLayoutAddressName.setError(null);
                                    textInputLayoutAddressName.setError("地址名稱不可為空");
                                } else if (addressInfo.isEmpty()){
                                    textInputLayoutAddressInfo.setError(null);
                                    textInputLayoutAddressName.setError(null);
                                    textInputLayoutAddressInfo.setError("請填寫詳細地址");
                                }  else if (geocode(addressInfo) == null){
                                    textInputLayoutAddressInfo.setError(null);
                                    textInputLayoutAddressName.setError(null);
                                    textInputLayoutAddressInfo.setError("該地址不存在，請檢查是否輸入錯誤");
                                } else {
                                    android.location.Address newAddress = geocode(addressInfo);
                                    textInputLayoutAddressInfo.setError(null);
                                    textInputLayoutAddressName.setError(null);
                                    int count = 0;
                                    JsonObject jsonObject = new JsonObject();
                                    address.setName(addressName);
                                    address.setInfo(addressInfo);
                                    address.setLatitude(newAddress.getLatitude());
                                    address.setLongitude(newAddress.getLongitude());
                                    address.setState(1);
                                    jsonObject.addProperty("action", "update");
                                    jsonObject.addProperty("address", gson.toJson(address));
                                    String jsonOut = jsonObject.toString();
                                    if (Common.networkConnected(activity)) {
                                        try {
                                            count = Integer.valueOf(new CommonTask(url, jsonOut).execute().get());
                                        } catch (Exception e){
                                            Log.e(TAG, e.toString());
                                        }
                                        if (count != 0){
                                            Common.showToast(activity, "編輯送餐地址成功");
                                            dialog13.dismiss();
                                        } else {
                                            Common.showToast(activity, "編輯地址失敗，請稍後再試");
                                        }
                                    }
                                }
                            });
                            builder1.setNegativeButton("取消", (dialog14, which14) -> dialog14.cancel());
                            Dialog dialog1 = builder1.create();
                            Common.setDialogUi(dialog1, activity);
                            dialog1.show();
                            textInputLayoutAddressName.getEditText().setText(address.getName());
                            textInputLayoutAddressInfo.getEditText().setText(address.getInfo());
                        }
                    });
                    Dialog dialog = builder.create();
                    Common.setDialogUi(dialog, activity);
                    dialog.show();
                });

            }
    }

    @Override
    public void onResume() {
        super.onResume();
        rvAddress.setLayoutManager(new LinearLayoutManager(activity));
        List<Address> addresses = getAddresses(mem_id);
        ShowAddress(addresses);
        Common.showBottomNav(activity);
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
}
