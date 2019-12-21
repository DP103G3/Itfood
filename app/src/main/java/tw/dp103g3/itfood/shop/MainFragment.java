package tw.dp103g3.itfood.shop;


import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import tw.dp103g3.itfood.Common;
import tw.dp103g3.itfood.R;
import tw.dp103g3.itfood.task.CommonTask;
import tw.dp103g3.itfood.task.ImageTask;

public class MainFragment extends Fragment {
    private final static String TAG = "TAG_MainFragment";
    private DrawerLayout drawerLayout;
    private AppCompatActivity activity;
    private RecyclerView rvNewShop, rvAllShop, rvChineseShop;
    private Toolbar toolbar;
    private List<Shop> shops;
    private CommonTask getAllShopTask;
    private ImageTask shopImageTask;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ScrollView scrollView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (AppCompatActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        drawerLayout = view.findViewById(R.id.drawerLayout);
        ActionBarDrawerToggle actionBarDrawerToggle =
                new ActionBarDrawerToggle(activity, drawerLayout, R.string.textOpen, R.string.textClose);
        actionBarDrawerToggle.syncState();
        NavigationView navigationView = view.findViewById(R.id.navigationView);
        toolbar = view.findViewById(R.id.toolbar);
        toolbar.setPadding(0, Common.getStatusBarHeight(activity), 0, 0);
        activity.setSupportActionBar(toolbar);
        NavController navController = Navigation.findNavController(view);
        NavigationUI.setupWithNavController(navigationView, navController);
        NavigationUI.setupWithNavController(toolbar, navController, drawerLayout);


        if (shops == null) {
            shops = getShops();
        }
        scrollView = view.findViewById(R.id.scrollView);
        scrollView.setVisibility(Common.networkConnected(activity) || !shops.isEmpty() ?
                View.VISIBLE : View.GONE);
        rvNewShop = view.findViewById(R.id.rvNewShop);
        rvNewShop.setLayoutManager(new GridLayoutManager(
                activity, 1, RecyclerView.HORIZONTAL, false));
        rvChineseShop = view.findViewById(R.id.rvChineseShop);
        rvChineseShop.setLayoutManager(new GridLayoutManager(
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

    private List<Shop> typeFilter(String type) {
        return shops.stream().filter(v -> v.getTypes().contains(type)).collect(Collectors.toList());
    }

    private List<Shop> getShops() {
        List<Shop> shops = new ArrayList<>();
        if (Common.networkConnected(activity)) {
            String url = Common.URL + "/ShopServlet";
            JsonObject jsonObject = new JsonObject();
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
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
        if (shops == null || shops.isEmpty()) {
            if (Common.networkConnected(activity)) {
                Common.showToast(activity, R.string.textNoShopsFound);
            }
        }
        ShopAdapter shopAdapter = (ShopAdapter) recyclerView.getAdapter();
        if (shopAdapter == null) {
            recyclerView.setAdapter(new ShopAdapter(activity, shops, itemViewResId));
        } else {
            shopAdapter.setShops(shops);
            shopAdapter.notifyDataSetChanged();
        }

    }

    private void showShops() {
        List<Shop> newShop = shops.stream()
                .filter(v -> System.currentTimeMillis() - v.getJointime().getTime() <= 2592000000L)
                .collect(Collectors.toList());
        setAdapter(rvNewShop, newShop, R.layout.small_shop_item_view);
        setAdapter(rvChineseShop, typeFilter("中式"), R.layout.small_shop_item_view);
        setAdapter(rvAllShop, shops, R.layout.large_shop_item_view);
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
        public ShopAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(context)
                    .inflate(itemViewResId, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull ShopAdapter.MyViewHolder holder, int position) {
            final Shop shop = shops.get(position);
            String url = Common.URL + "/ShopServlet";
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
                Bundle bundle = new Bundle();
                bundle.putSerializable("shop", shop);
                Navigation.findNavController(v)
                        .navigate(R.id.action_mainFragment_to_shopFragment, bundle);
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
