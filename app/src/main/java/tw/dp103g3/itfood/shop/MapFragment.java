package tw.dp103g3.itfood.shop;


import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import tw.dp103g3.itfood.Common;
import tw.dp103g3.itfood.R;
import tw.dp103g3.itfood.Url;
import tw.dp103g3.itfood.address.Address;
import tw.dp103g3.itfood.member.Member;
import tw.dp103g3.itfood.task.CommonTask;
import tw.dp103g3.itfood.task.ImageTask;


/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment {
    private static final String TAG = "TAG_MapFragment";
    private Activity activity;
    private GoogleMap map;
    private Address selectedAddress;
    private Gson gson;
    private NavController navController;
    private ImageView ivCart;
    private int memId;
    private List<Address> addresses;
    private CommonTask getAllShopTask, getAllAddressTask;
    private ImageTask shopImageTask;
    private Spinner spAddress;
    private List<Shop> shops;
    private RecyclerView rvShop;
    private List<Marker> markers;
    private BottomNavigationView bottomNavigationView;
    private Animator animator;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        navController = Navigation.findNavController(view);
        bottomNavigationView = activity.findViewById(R.id.bottomNavigation);
        memId = Common.getMemId(activity);
        gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        Address localAddress = null;
        addresses = getAddresses(memId);
        File file = new File(activity.getFilesDir(), "localAddress");
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            localAddress = (Address) in.readObject();
            Log.d(TAG, String.valueOf(localAddress.getLatitude()));
        } catch (IOException | ClassNotFoundException e) {
            Log.e(TAG, e.toString());
        }
        addresses.add(0, localAddress);
        List<String> addressNames = new ArrayList<>();
        for (int i = 0; i < addresses.size(); i++) {
            addressNames.add(addresses.get(i).getName());
        }
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(activity,
                android.R.layout.simple_spinner_dropdown_item, addressNames);
        spAddress = view.findViewById(R.id.spAddress);
        spAddress.setAdapter(arrayAdapter);
        spAddress.setSelection(0, true);
        selectedAddress = addresses.get(0);
        spAddress.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedAddress = addresses.get(position);
                moveMap(selectedAddress.getLatLng());
                showShops();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        ivCart = view.findViewById(R.id.ivCart);
        ivCart.setOnClickListener(v -> {
            navController.navigate(R.id.action_mapFragment_to_shoppingCartFragment);
        });
        shops = getShops();
        markers = new ArrayList<>();
        MapView mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.onStart();
        mapView.getMapAsync(googleMap -> {
            map = googleMap;
            moveMap(selectedAddress.getLatLng());
            shops.forEach(v -> {
                Marker marker = map.addMarker(new MarkerOptions()
                        .position(v.getLatLng()).title(v.getName())
                        .snippet(v.getAddress()).alpha(0.5f));
                marker.setTag(v.getId());
                markers.add(marker);
            });
            map.setOnMarkerClickListener(v -> {
                v.setAlpha(1);
                v.showInfoWindow();
                return true;
            });
            map.setOnInfoWindowCloseListener(v -> v.setAlpha(0.5f));
            map.setOnInfoWindowClickListener(v -> moveMap(v.getPosition()));
            map.setOnInfoWindowLongClickListener(v -> {
                Optional<Shop> shopOptional = shops.stream()
                        .filter(shop -> shop.getId() == (Integer) v.getTag()).findAny();
                if (shopOptional.isPresent()) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("shop", shopOptional.get());
                    navController.navigate(R.id.action_mapFragment_to_shopFragment, bundle);
                }
            });
        });
        rvShop = view.findViewById(R.id.rvShop);
        rvShop.setLayoutManager(new GridLayoutManager(activity, 1,
                RecyclerView.HORIZONTAL, false));
        showShops();
    }

    private List<Address> getAddresses(int mem_id) {
        List<Address> adresses = new ArrayList<>();
        if (Common.networkConnected(activity)) {
            String url = Url.URL + "/AddressServlet";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getAllShow");
            jsonObject.addProperty("mem_id", mem_id);
            String jsonOut = jsonObject.toString();
            getAllAddressTask = new CommonTask(url, jsonOut);
            try {
                String jsonIn = getAllAddressTask.execute().get();
                Type listType = new TypeToken<List<Address>>(){
                }.getType();
                adresses = gson.fromJson(jsonIn, listType);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Common.showToast(activity, R.string.textNoNetwork);
        }
        return adresses;
    }

    private void moveMap(LatLng latLng) {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)
                .zoom(17)
                .build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        map.animateCamera(cameraUpdate);
    }

    private void setAdapter(RecyclerView recyclerView, List<Shop> shops, int itemViewResId) {
        ShopAdapter shopAdapter = (ShopAdapter) recyclerView.getAdapter();
        if (shopAdapter == null) {
            recyclerView.setAdapter(new ShopAdapter(activity, shops, itemViewResId));
        } else {
            shopAdapter.setShops(shops);
            shopAdapter.notifyDataSetChanged();
        }
    }

    private List<Shop> getShops() {
        List<Shop> shops = new ArrayList<>();
        if (Common.networkConnected(activity)) {
            String url = Url.URL + "/ShopServlet";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getAllShow");
            String jsonOut = jsonObject.toString();
            getAllShopTask = new CommonTask(url, jsonOut);
            try {
                String jsonIn = getAllShopTask.execute().get();
                Type listType = new TypeToken<List<Shop>>() {
                }.getType();
                shops = gson.fromJson(jsonIn, listType);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Common.showToast(activity, R.string.textNoNetwork);
        }
        return shops;
    }

    private void showShops() {
        if (shops == null || shops.isEmpty()) {
            if (Common.networkConnected(activity)) {
                Common.showToast(activity, R.string.textNoShopsFound);
            }
            shops = new ArrayList<>();
        }
        Comparator<Shop> cmp = Comparator.comparing(v ->
                Common.Distance(v.getLatitude(), v.getLongitude(),
                        selectedAddress.getLatitude(), selectedAddress.getLongitude()));
        List<Shop> sortedShops = shops.stream().sorted(cmp)
                .collect(Collectors.toList());
        setAdapter(rvShop, sortedShops, R.layout.map_shop_item_view);
    }

    private class ShopAdapter extends RecyclerView.Adapter<ShopAdapter.MyViewHolder> {
        private Context context;
        private List<Shop> shops;
        private int imageSize;
        private int itemViewResId;

        private ShopAdapter(Context context, List<Shop> shops, int itemViewResId) {
            this.context = context;
            this.shops = shops;
            this.itemViewResId = itemViewResId;
            imageSize = getResources().getDisplayMetrics().widthPixels;
        }

        void setShops(List<Shop> shops) {
            this.shops = shops;
        }

        @Override
        public int getItemCount() {
            return shops.size();
        }

        private class MyViewHolder extends RecyclerView.ViewHolder {
            ImageView ivShop;
            TextView tvName, tvType, tvRate;

            private MyViewHolder(View itemView) {
                super(itemView);
                ivShop = itemView.findViewById(R.id.ivShop);
                tvName = itemView.findViewById(R.id.tvName);
                tvType = itemView.findViewById(R.id.tvType);
                tvRate = itemView.findViewById(R.id.tvRate);
            }
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(context)
                    .inflate(itemViewResId, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            final Shop shop = shops.get(position);
            String url = Url.URL + "/ShopServlet";
            List<String> types = shop.getTypes();
            StringBuilder typeSb = new StringBuilder();
            for (String line : types) {
                typeSb.append(line).append(" ");
            }
            String type = typeSb.toString().trim().replaceAll(" ", "，");
            int id = shop.getId();
            double rate = (double) shop.getTtscore() / shop.getTtrate();
            shopImageTask = new ImageTask(url, id, imageSize, holder.ivShop);
            shopImageTask.execute();
            holder.tvName.setText(shop.getName());
            holder.tvType.setText(type);
            holder.tvRate.setText(String.format(Locale.getDefault(),
                    "%.1f(%d)", rate, shop.getTtrate()));
            holder.itemView.setOnClickListener(v -> {
                moveMap(shop.getLatLng());
            });
            holder.itemView.setOnLongClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putSerializable("shop", shop);
                navController.navigate(R.id.action_mapFragment_to_shopFragment, bundle);
                return true;
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Common.checkCart(activity, ivCart);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (getAllAddressTask != null) {
            getAllAddressTask.cancel(true); getAllAddressTask = null;
        }
        if (getAllShopTask != null) {
            getAllShopTask.cancel(true); getAllShopTask = null;
        }
        if (shopImageTask != null) {
            shopImageTask.cancel(true); shopImageTask = null;
        }
    }
}
