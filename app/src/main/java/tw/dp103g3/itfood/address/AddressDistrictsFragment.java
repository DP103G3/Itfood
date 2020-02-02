package tw.dp103g3.itfood.address;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import tw.dp103g3.itfood.R;
import tw.dp103g3.itfood.main.SharedViewModel;


/**
 * A simple {@link Fragment} subclass.
 */
public class AddressDistrictsFragment extends Fragment {
    private Activity activity;
    private RecyclerView rvDistricts;
    private Toolbar toolbarDistricts;
    private List<District> districtList;
    private SharedViewModel model;
    private String TAG = "TAG_AddressDistrictFragment";

    public AddressDistrictsFragment() {
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_address_districts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setViews(view);

        toolbarDistricts.setNavigationOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        model = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        model.getSelectedCity().observe(getViewLifecycleOwner(), city -> {
            districtList = city.getDistricts();
            rvDistricts.setLayoutManager(new LinearLayoutManager(activity));
            rvDistricts.setAdapter(new DistrictAdapter(activity, districtList));
        });


    }

    private class DistrictAdapter extends RecyclerView.Adapter<DistrictAdapter.MyViewHolder> {
        private Context context;
        private List<District> districts;

        DistrictAdapter(Context context, List<District> districts) {
            this.context = context;
            this.districts = districts;
        }

        private class MyViewHolder extends RecyclerView.ViewHolder {

            TextView tvContent;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                tvContent = itemView.findViewById(R.id.tvContent);
            }
        }

        @Override
        public int getItemCount() {
            return districts.size();
        }

        @NonNull
        @Override
        public DistrictAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(context).inflate(R.layout.address_list_item_view, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull DistrictAdapter.MyViewHolder holder, int position) {
            final District district = districts.get(position);
            holder.tvContent.setText(district.getName());

            holder.itemView.setOnClickListener(v -> {
                model.selectDistrict(district);
                Navigation.findNavController(v).popBackStack();
            });

        }

    }

    private void setViews(View view) {
        rvDistricts = view.findViewById(R.id.rvDistricts);
        toolbarDistricts = view.findViewById(R.id.toolbarDistricts);
    }
}
