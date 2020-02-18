package tw.dp103g3.itfood.address;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
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

import java.util.List;

import tw.dp103g3.itfood.Common;
import tw.dp103g3.itfood.R;
import tw.dp103g3.itfood.main.SharedViewModel;
import tw.dp103g3.itfood.task.CommonTask;


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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        mem_id = Common.getMemId(activity);
        pref = activity.getSharedPreferences(Common.PREFERENCES_ADDRESS, Context.MODE_PRIVATE);
        model = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_address_select, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        this.view = view;

        model.getSelectedAddress().observe(getViewLifecycleOwner(), address -> selectedAddress = address);

        toolbar = view.findViewById(R.id.toolbarAddressSelect);
        cardViewCheck = view.findViewById(R.id.cardViewCheck);
        recyclerView = view.findViewById(R.id.rvAddressRadioButton);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));

        toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).popBackStack());
        cardViewCheck.setOnClickListener(v -> {
            model.selectAddress(selectedAddress);
            Navigation.findNavController(v).popBackStack();
        });

        addresses = Common.getAddresses(activity, mem_id);

        ShowAddress(addresses);

    }

    private void ShowAddress(List<Address> addresses) {
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
            if (addresses.isEmpty()) {
                return 1;
            } else {
                return addresses.size() + 1;
            }
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
            Drawable checkedIcon = getResources().getDrawable(R.drawable.round_check_circle, activity.getTheme());
            ColorStateList tint = getResources().getColorStateList(R.color.radio_button_custom_button, activity.getTheme());
            checkedIcon.setTintList(tint);
            if (position == addresses.size()) {
                holder.radioButton.setText(R.string.textAddAddress);
                Drawable add = getResources().getDrawable(R.drawable.add, activity.getTheme());
                holder.radioButton.setCompoundDrawablesWithIntrinsicBounds(add, null, null, null);
                holder.radioButton.setOnClickListener(v -> Navigation.findNavController(v)
                        .navigate(R.id.action_addressSelectFragment_to_addAddressFragment));
            } else {
                final Address address = addresses.get(position);
                holder.radioButton.setCompoundDrawablesWithIntrinsicBounds(checkedIcon, null, null, null);
                String addressStr = address.getName() +
                        (address.getInfo() == null ? "" : "   " + address.getInfo());
                holder.radioButton.setText(addressStr);
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
                    model.selectAddress(selectedAddress);
                });
            }
        }
    }
}
