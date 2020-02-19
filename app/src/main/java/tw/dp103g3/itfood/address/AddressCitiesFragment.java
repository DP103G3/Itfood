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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import tw.dp103g3.itfood.R;
import tw.dp103g3.itfood.main.SharedViewModel;

public class AddressCitiesFragment extends Fragment {
    private Activity activity;
    private List<City> cities;
    private RecyclerView rvCities;
    private Toolbar toolbarCities;
    private SharedViewModel model;

    public AddressCitiesFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_address_cities, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvCities = view.findViewById(R.id.rvCities);
        toolbarCities = view.findViewById(R.id.toolbarCities);
        cities = getCities();
        rvCities.setLayoutManager(new LinearLayoutManager(activity));
        rvCities.setAdapter(new CityAdapter(activity, cities));
        model = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        toolbarCities.setNavigationOnClickListener(v -> Navigation.findNavController(v).popBackStack());
    }

    private class CityAdapter extends RecyclerView.Adapter<CityAdapter.MyViewHolder> {

        private Context context;
        private List<City> cities;

        CityAdapter(Context context, List<City> cities) {
            this.context = context;
            this.cities = cities;
        }

        private class MyViewHolder extends RecyclerView.ViewHolder {
            TextView tvContent;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                tvContent = itemView.findViewById(R.id.tvContent);
            }
        }

        @NonNull
        @Override
        public CityAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(context).inflate(R.layout.address_list_item_view, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull CityAdapter.MyViewHolder holder, int position) {
            final City city = cities.get(position);
            holder.tvContent.setText(city.getName());

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    model.selectCity(city);
                    Navigation.findNavController(v).popBackStack();
                }
            });

        }

        @Override
        public int getItemCount() {
            return cities.size();
        }
    }

    private List<City> getCities() {
        List<City> cities;
        Gson gson = new Gson();
        String addressJson = AddressJson.addressJson;
        Type listType = new TypeToken<List<City>>() {
        }.getType();
        cities = gson.fromJson(addressJson, listType);
        return cities;
    }

}
