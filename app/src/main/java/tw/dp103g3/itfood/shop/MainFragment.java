package tw.dp103g3.itfood.shop;


import android.animation.Animator;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
import java.util.stream.Collectors;

import tw.dp103g3.itfood.Common;
import tw.dp103g3.itfood.R;
import tw.dp103g3.itfood.Url;
import tw.dp103g3.itfood.address.Address;
import tw.dp103g3.itfood.main.MainActivity;
import tw.dp103g3.itfood.task.CommonTask;
import tw.dp103g3.itfood.task.ImageTask;

public class MainFragment extends Fragment {
    private final static String TAG = "TAG_MainFragment";
    private MainActivity activity;
    private ImageView ivCart, ivMap;
    private RecyclerView rvNewShop, rvAllShop, rvChineseShop, rvAmericanShop;
    private List<Shop> shops;
    private List<Address> addresses;
    private CommonTask getAllShopTask, getAllAddressTask;
    private ImageTask shopImageTask;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ScrollView scrollView;
    private Spinner spAddress;
    private int memId;
    private Address selectedAddress;
    private Gson gson;
    private NavController navController;
    private View view;
    private SharedPreferences preferences;
    private BottomNavigationView bottomNavigationView;
    private Animator animator;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
        activity.checkLocationSettings();
        preferences = activity.getSharedPreferences(Common.PREFERENCES_MEMBER,
                Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Common.disconnectServer();
        bottomNavigationView = activity.findViewById(R.id.bottomNavigation);
        memId = Common.getMemId(activity);
        this.view = view;
        ivCart = view.findViewById(R.id.ivCart);
        navController = Navigation.findNavController(view);
        Common.checkCart(activity, ivCart);
        ivCart.setOnClickListener(v -> navController.
                navigate(R.id.action_mainFragment_to_shoppingCartFragment));
        gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        Log.d(TAG, "1");
        Address localAddress = new Address(0, getString(R.string.textLocalPosition),
                null, -1, -1);
        addresses = getAddresses(memId) != null ?
                getAddresses(memId) : new ArrayList<>();
        File file = new File(activity.getFilesDir(), "localAddress");
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            localAddress = (Address) in.readObject();
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
                showShops();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        if (shops == null) {
            shops = getShops();
        }
        ivMap = view.findViewById(R.id.ivMap);
        ivMap.setOnClickListener(v -> {
            navController.navigate(R.id.action_mainFragment_to_mapFragment);
        });
        scrollView = view.findViewById(R.id.scrollView);
        scrollView.setVisibility(Common.networkConnected(activity) || !shops.isEmpty() ?
                View.VISIBLE : View.GONE);
        rvNewShop = view.findViewById(R.id.rvNewShop);
        rvNewShop.setLayoutManager(new GridLayoutManager(
                activity, 1, RecyclerView.HORIZONTAL, false));
        rvChineseShop = view.findViewById(R.id.rvChineseShop);
        rvChineseShop.setLayoutManager(new GridLayoutManager(
                activity, 1, RecyclerView.HORIZONTAL, false));
        rvAmericanShop = view.findViewById(R.id.rvAmericanShop);
        rvAmericanShop.setLayoutManager(new GridLayoutManager(
                activity, 1, RecyclerView.HORIZONTAL, false));
        rvAllShop = view.findViewById(R.id.rvAllShop);
        rvAllShop.setPadding(0, 0, 0, Common.getNavigationBarHeight(activity));
        rvAllShop.setLayoutManager(new LinearLayoutManager(activity));

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (Common.networkConnected(activity)) {
                shops = getShops();
            }
            scrollView.setVisibility(Common.networkConnected(activity) || !shops.isEmpty() ?
                    View.VISIBLE : View.GONE);
            swipeRefreshLayout.setRefreshing(true);
            showShops();
            swipeRefreshLayout.setRefreshing(false);
        });
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
                Type listType = new TypeToken<List<Address>>() {
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

    private List<Shop> typeFilter(String type, List<Shop> shops) {
        return shops.stream().filter(v -> v.getTypes().contains(type)).collect(Collectors.toList());
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

    private void setAdapter(RecyclerView recyclerView, List<Shop> shops, int itemViewResId) {
        ShopAdapter shopAdapter = (ShopAdapter) recyclerView.getAdapter();
        if (shopAdapter == null) {
            recyclerView.setAdapter(new ShopAdapter(activity, shops, itemViewResId));
        } else {
            shopAdapter.setShops(shops);
            shopAdapter.notifyDataSetChanged();
        }
    }

    private void showShops() {
        if (shops == null || shops.isEmpty()) {
            if (Common.networkConnected(activity)) {
                Common.showToast(activity, R.string.textNoShopsFound);
            }
            shops = new ArrayList<>();
        }
        List<Shop> showShops = shops.stream().filter(v -> Common.Distance(v.getLatitude(), v.getLongitude(),
                selectedAddress.getLatitude(), selectedAddress.getLongitude()) < 5000)
                .collect(Collectors.toList());
        List<Shop> newShop = showShops.stream()
                .filter(v -> System.currentTimeMillis() - v.getJointime().getTime() <= 2592000000L)
                .collect(Collectors.toList());
        setAdapter(rvNewShop, newShop, R.layout.small_shop_item_view);
        setAdapter(rvChineseShop, typeFilter("中式", showShops), R.layout.small_shop_item_view);
        setAdapter(rvAmericanShop, typeFilter("美式料理", showShops), R.layout.small_shop_item_view);
        Comparator<Shop> cmp = Comparator.comparing(v ->
                Common.Distance(v.getLatitude(), v.getLongitude(),
                        selectedAddress.getLatitude(), selectedAddress.getLongitude()));
        List<Shop> sortedShops = showShops.stream().sorted(cmp)
                .collect(Collectors.toList());
        setAdapter(rvAllShop, sortedShops, R.layout.large_shop_item_view);
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
            imageSize = getResources().getDisplayMetrics().widthPixels / 2;
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
        public ShopAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(context)
                    .inflate(itemViewResId, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull ShopAdapter.MyViewHolder holder, int position) {
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
            holder.ivShop.setImageResource(R.drawable.no_image);
            shopImageTask = new ImageTask(url, id, imageSize, holder.ivShop);
            shopImageTask.execute();
            holder.tvName.setText(shop.getName());
            holder.tvType.setText(type);
            holder.tvRate.setText(String.format(Locale.getDefault(),
                    "%.1f(%d)", rate, shop.getTtrate()));
            holder.itemView.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putSerializable("shop", shop);
                navController.navigate(R.id.action_mainFragment_to_shopFragment, bundle);
            });
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (getAllAddressTask != null) {
            getAllAddressTask.cancel(true);
            getAllAddressTask = null;
        }
        if (getAllShopTask != null) {
            getAllShopTask.cancel(true);
            getAllShopTask = null;
        }
        if (shopImageTask != null) {
            shopImageTask.cancel(true);
            shopImageTask = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Common.checkCart(activity, ivCart);
    }
}
