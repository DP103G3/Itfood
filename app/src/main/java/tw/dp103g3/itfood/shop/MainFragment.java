package tw.dp103g3.itfood.shop;


import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import tw.dp103g3.itfood.Common;
import tw.dp103g3.itfood.R;
import tw.dp103g3.itfood.task.CommonTask;
import tw.dp103g3.itfood.task.ImageTask;

public class MainFragment extends Fragment {
    private final static String TAG = "TAG_MainFragment";
    private AppCompatActivity activity;
    private RecyclerView rvNewShop, rvAllShop, rvChineseShop;
    private Toolbar toolbar;
    private List<Shop> shops;
    private CommonTask getAllShopTask;
    private ImageTask shopImageTask;
    private SwipeRefreshLayout swipeRefreshLayout;

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
        toolbar = view.findViewById(R.id.toolbar);
        activity.setSupportActionBar(toolbar);
        shops = getShops();
        rvNewShop = view.findViewById(R.id.rvNewShop);
        rvNewShop.setLayoutManager(new GridLayoutManager(
                activity, 1, RecyclerView.HORIZONTAL, false));
        rvChineseShop = view.findViewById(R.id.rvChineseShop);
        rvChineseShop.setLayoutManager(new GridLayoutManager(
                activity, 1, RecyclerView.HORIZONTAL, false));
        rvAllShop = view.findViewById(R.id.rvAllShop);
        rvAllShop.setLayoutManager(new LinearLayoutManager(activity));

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
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
        List<Shop> shops = null;
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
            Common.ShowToast(activity, R.string.textNoNetwork);
        }
        return shops;
    }

    private void setAdapter(RecyclerView recyclerView, List<Shop> shops, int itemViewResId) {
        if (shops == null || shops.isEmpty()) {
            Common.ShowToast(activity, R.string.textNoShopsFound);
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
                .filter(v -> System.currentTimeMillis() - v.getJointime().getTime() <= 2592000000l)
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

        public ShopAdapter(Context context, List<Shop> shops, int itemViewResId) {
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
            public MyViewHolder(View itemView) {
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
                typeSb.append(line + " ");
            }
            String type = typeSb.toString().trim().replaceAll(" ", "，");
            int id = shop.getId();
            double rate = (double) shop.getTtscore() / shop.getTtrate();
            shopImageTask = new ImageTask(url, id, imageSize, holder.ivShop);
            shopImageTask.execute();
            holder.tvName.setText(shop.getName());
            holder.tvType.setText(type);
            holder.tvRate.setText(String.format(Locale.getDefault(), "%.1f", rate));
        }
    }
}
