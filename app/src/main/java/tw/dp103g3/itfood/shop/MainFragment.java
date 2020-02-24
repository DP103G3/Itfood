package tw.dp103g3.itfood.shop;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import tw.dp103g3.itfood.main.Common;
import tw.dp103g3.itfood.R;
import tw.dp103g3.itfood.main.Url;
import tw.dp103g3.itfood.address.Address;
import tw.dp103g3.itfood.main.MainActivity;
import tw.dp103g3.itfood.main.SharedViewModel;
import tw.dp103g3.itfood.shopping_cart.LoginDialogFragment;
import tw.dp103g3.itfood.task.CommonTask;
import tw.dp103g3.itfood.task.ImageTask;

import static tw.dp103g3.itfood.main.Common.LOGIN_FALSE;
import static tw.dp103g3.itfood.main.Common.getAddresses;
import static tw.dp103g3.itfood.main.Common.showLoginDialog;
import static tw.dp103g3.itfood.main.MainActivity.getLocation;

public class MainFragment extends Fragment implements LoginDialogFragment.LoginDialogContract {
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
    private Button btAddress;
    private int memId;
    private Address selectedAddress;
    private Gson gson;
    private NavController navController;
    private View view;
    private SharedViewModel model;
    private Location location;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
        activity.checkLocationSettings();
        memId = Common.getMemId(activity);
        location = getLocation();
        model = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        model.selectAddress(null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Common.disconnectServer();
        this.view = view;
        ivCart = view.findViewById(R.id.ivCart);
        navController = Navigation.findNavController(view);
        Common.checkCart(activity, ivCart);
        ivCart.setOnClickListener(v -> navController.
                navigate(R.id.action_mainFragment_to_shoppingCartFragment));
        gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

        btAddress = view.findViewById(R.id.btAddress);
        btAddress.setOnClickListener(v -> {
            if (memId != LOGIN_FALSE) {
                navController.navigate(R.id.action_mainFragment_to_addressSelectFragment);
            } else {
                showLoginDialog(this);
            }
        });

        model.getSelectedAddress().observe(getViewLifecycleOwner(), address -> {
            if (address == null) {
                Log.d(TAG, "1");
                if (location == null) {
                    Common.showToast(activity, "無法取得現在位置");
                    if (memId == 0) {
                        selectedAddress = new Address(0, "", "無法取得", -1, -1);
                        Log.d(TAG, "3");
                    } else {
                        addresses = getAddresses(activity, memId);
                        selectedAddress = addresses.get(0);
                        model.selectAddress(selectedAddress);
                        Log.d(TAG, "4" + selectedAddress.getName());
                    }
                } else {
                    selectedAddress = new Address(location.getLatitude(), location.getLongitude());
                    model.selectAddress(selectedAddress);
                    Log.d(TAG, "5" + selectedAddress.getLatitude() + " , " + selectedAddress.getLongitude());
                }
            } else if (address.getId() != 0) {  //id為0的Address為現在位置, 設定條件防止遞迴 (只有在輸入非現在位置的地址才會更新）
                selectedAddress = address;
                Log.d(TAG, "2");
            } else {
                selectedAddress = address;
                Log.d(TAG, "6" + selectedAddress.getLatitude() + " , " + selectedAddress.getLongitude());
            }
            showShops();
        });

        if (shops == null) {
            shops = getShops(memId);
        }
        ivMap = view.findViewById(R.id.ivMap);
        ivMap.setOnClickListener(v -> navController.navigate(R.id.action_mainFragment_to_mapFragment));
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
                shops = getShops(memId);
            }
            scrollView.setVisibility(Common.networkConnected(activity) || !shops.isEmpty() ?
                    View.VISIBLE : View.GONE);
            swipeRefreshLayout.setRefreshing(true);
            showShops();
            swipeRefreshLayout.setRefreshing(false);
        });
        showShops();
    }

    private List<Shop> typeFilter(String type, List<Shop> shops) {
        return shops.stream().filter(v -> v.getTypes().contains(type)).collect(Collectors.toList());
    }

    private List<Shop> getShops(int id) {
        List<Shop> shops = new ArrayList<>();
        if (Common.networkConnected(activity)) {
            String url = Url.URL + "/ShopServlet";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getAllShow");
            jsonObject.addProperty("id", id);
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
        List<Shop> showShops = selectedAddress != null ? shops.stream().filter(v -> Common.Distance(v.getLatitude(), v.getLongitude(),
                selectedAddress.getLatitude(), selectedAddress.getLongitude()) < 5000)
                .collect(Collectors.toList()) : new ArrayList<>();
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

    @Override
    public void sendLoginResult(boolean isSuccessful) {
        if (isSuccessful) {
            NavOptions.Builder builder = new NavOptions.Builder();
            builder.setPopUpTo(R.id.mainFragment, false);
            NavOptions navOptions = builder.build();
            Navigation.findNavController(view).navigate(R.id.mainFragment, null, navOptions);
        }
    }

    @Override
    public void sendRegisterRequest() {
        NavOptions.Builder builder = new NavOptions.Builder();
        builder.setPopUpTo(R.id.mainFragment, false);
        NavOptions navOptions = builder.build();
        Navigation.findNavController(view).navigate(R.id.mainFragment, null, navOptions);
        Navigation.findNavController(view).navigate(R.id.action_mainFragment_to_registerFragment);
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
        if (selectedAddress != null) {
            btAddress.setText(selectedAddress.getName());
        }
        BottomNavigationView bottomNavigationView = activity.findViewById(R.id.bottomNavigation);
        if (bottomNavigationView.getVisibility() == View.GONE) {
            Common.showBottomNav(activity);
        }
    }
}
