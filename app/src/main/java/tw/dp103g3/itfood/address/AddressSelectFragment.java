package tw.dp103g3.itfood.address;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import tw.dp103g3.itfood.Common;
import tw.dp103g3.itfood.R;
import tw.dp103g3.itfood.Url;
import tw.dp103g3.itfood.main.SharedViewModel;
import tw.dp103g3.itfood.task.CommonTask;

import static tw.dp103g3.itfood.Common.DATE_FORMAT;
import static tw.dp103g3.itfood.Common.PREFERENCES_MEMBER;


public class AddressSelectFragment extends Fragment {
    private String TAG = "TAG_AddressSelectFragment";
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private CommonTask getAddressTask;
    private Activity activity;
    private List<Address> addresses;
    private SharedPreferences pref;
    private int mem_id;
    private Address selectedAddress;
    private CardView cardViewCheck;
    private View view;
    private SharedViewModel model;


    public AddressSelectFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        pref = activity.getSharedPreferences(PREFERENCES_MEMBER, Context.MODE_PRIVATE);
        mem_id = pref.getInt("mem_id", 0);

        model = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_address_select, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;

        model.getSelectedAddress().observe(getViewLifecycleOwner(), address -> {
            selectedAddress = address;
        });

        toolbar = view.findViewById(R.id.toolbarAddressSelect);
        cardViewCheck = view.findViewById(R.id.cardViewCheck);
        recyclerView = view.findViewById(R.id.rvAddressRadioButton);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));

        toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).popBackStack());
        cardViewCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                model.selectAddress(selectedAddress);
                Navigation.findNavController(v).popBackStack();
            }
        });

        addresses = getAddresses(mem_id);

        ShowAddress(addresses);

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
            getAddressTask = new CommonTask(url, jsonOut);
            try {
                String jsonIn = getAddressTask.execute().get();
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
        if (addresses == null || addresses.isEmpty()) {
            if (Common.networkConnected(activity)) {
                Common.showToast(activity, "no address found");
                Navigation.findNavController(view).popBackStack();
            }
        }
        AddressAdapter addressAdapter = (AddressAdapter) recyclerView.getAdapter();
        if (addressAdapter == null) {
            recyclerView.setAdapter(new AddressAdapter(activity, addresses));
        } else {
            addressAdapter.setAddresses(addresses);
            addressAdapter.notifyDataSetChanged();
        }
    }

    private class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.MyViewHolder> {
        private Context context;
        private List<Address> addresses;

        public AddressAdapter(Context context, List<Address> addresses) {
            this.addresses = addresses;
            this.context = context;
        }

        void setAddresses(List<Address> addresses) {
            this.addresses = addresses;
        }

        private class MyViewHolder extends RecyclerView.ViewHolder {
            RadioButton radioButton;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                radioButton = itemView.findViewById(R.id.radioButton);
            }
        }

        @Override
        public int getItemCount() {
            return addresses.size() + 1;
        }

        @NonNull
        @Override
        public AddressAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(context)
                    .inflate(R.layout.select_radio_button_item_view, parent, false);
            return new AddressAdapter.MyViewHolder(itemView);
        }

        private RadioButton lastChecked = null;

        @Override
        public void onBindViewHolder(@NonNull AddressAdapter.MyViewHolder holder, int position) {
            if (position == addresses.size()) {
                holder.radioButton.setText(R.string.addAddress);
                holder.radioButton.setButtonDrawable(R.drawable.add);
                holder.radioButton.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_addressSelectFragment_to_addAddressFragment));
            } else {
                final Address address = addresses.get(position);
                holder.radioButton.setText(address.getInfo());
                holder.radioButton.setTag(position);

                if (address.getId() == selectedAddress.getId()) {
                    holder.radioButton.setChecked(true);
                    lastChecked = holder.radioButton;
                    selectedAddress = addresses.get(position);
                } else {
                    holder.radioButton.setChecked(false);
                }

                holder.radioButton.setOnClickListener(v -> {
                    RadioButton rb = (RadioButton) v;
                    if (rb.isChecked()) {
                        if (lastChecked != null && lastChecked != rb) {
                            lastChecked.setChecked(false);
                        }
                        lastChecked = rb;
                    }
                    selectedAddress = address;
                });
            }
        }


    }
}
