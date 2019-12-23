package tw.dp103g3.itfood.favorite;

//TODO TOOLBAR加入MENUOPTION、TITLE
//TODO itemView add onClickListener navigate to Shop Detail.

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
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
import java.util.Locale;

import tw.dp103g3.itfood.Common;
import tw.dp103g3.itfood.R;
import tw.dp103g3.itfood.Url;
import tw.dp103g3.itfood.member.Member;
import tw.dp103g3.itfood.shop.Shop;
import tw.dp103g3.itfood.task.CommonTask;
import tw.dp103g3.itfood.task.ImageTask;


public class FavoriteFragment extends Fragment {
    private final static String TAG = "TAG_FavoriteFragment";
    private AppCompatActivity activity;
    private Toolbar toolbar;
    private CommonTask getFavoritesTask;
    private CommonTask getShopTask;
    private CommonTask favoriteDeleteTask;
    private Member member;
    private int memId;
    private RecyclerView rvFavorite;
    private List<Favorite> favorites;
    private List<Shop> shops;
    private ImageTask shopImageTask;
    private ConstraintLayout layoutFavoriteNoItem;
    private TextView tvFavoriteNoItem;
    private ImageView ivFavoriteNoItem, ivBack;
    private Button btBackToMain;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (AppCompatActivity)getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorite, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        final NavController navController = Navigation.findNavController(view);
        toolbar = view.findViewById(R.id.toolbarFavorite);
        toolbar.setPadding(0, Common.getStatusBarHeight(activity), 0, 0);
        activity.setSupportActionBar(toolbar);
        rvFavorite = view.findViewById(R.id.rvFavorite);
        rvFavorite.setLayoutManager(new LinearLayoutManager(activity));

        try{
            handleViews();
        } catch (NullPointerException e){
            System.out.println(TAG + e.toString());
            navController.popBackStack();
        }
        btBackToMain.setOnClickListener(v -> navController.popBackStack());

        layoutFavoriteNoItem.setVisibility(View.GONE);

        ivBack.setOnClickListener(v -> {
            Navigation.findNavController(v).popBackStack();
        });

        Bundle bundle = getArguments();
        if (bundle == null || bundle.getSerializable("member") == null) {
            Common.showToast(activity, "error");
            navController.popBackStack();
            return;
        }

        member = (Member) bundle.getSerializable("member");
        try{
            memId = member.getMemId();
        } catch (Exception e){
            System.out.println(TAG + e.toString());
            Common.showToast(activity, "error");
            navController.popBackStack();
            return;
        }
        favorites = getFavorites(memId);
        if (favorites == null || favorites.isEmpty()){
            rvFavorite.setVisibility(View.GONE);
            layoutFavoriteNoItem.setVisibility(View.VISIBLE);
        }
        shops = new ArrayList<>();
        for(int i = 0; i < favorites.size(); i ++){
            shops.add(getShopById(favorites.get(i).getShopId()));
        }

        showShops();
    }



    private List<Favorite> getFavorites(int memId) {
        List<Favorite> favorites = null;
        if (Common.networkConnected(activity)) {
            String url = Url.URL + "/FavoriteServlet";
            JsonObject jsonObject = new JsonObject();
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
            jsonObject.addProperty("action", "findByMemberId");
            jsonObject.addProperty("memId", memId);
            String jsonOut = jsonObject.toString();
            getFavoritesTask = new CommonTask(url, jsonOut);
            try {
                String jsonIn = getFavoritesTask.execute().get();
                Type listType = new TypeToken<List<Favorite>>() {
                }.getType();
                favorites = gson.fromJson(jsonIn, listType);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Common.showToast(activity, R.string.textNoNetwork);
        }
        return favorites;
    }

    private Shop getShopById(int shopId){
        Shop shop = null;
        if (Common.networkConnected(activity)) {
            String url = Url.URL + "/ShopServlet";
            JsonObject jsonObject = new JsonObject();
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
            jsonObject.addProperty("action", "getShopById");
            jsonObject.addProperty("id", shopId);
            String jsonOut = jsonObject.toString();
            getShopTask = new CommonTask(url, jsonOut);
            try {
                String jsonIn = getShopTask.execute().get();
                shop = gson.fromJson(jsonIn, Shop.class);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Common.showToast(activity, R.string.textNoNetwork);
        }
        return shop;
    }

    private void setAdapter(RecyclerView recyclerView, List<Shop> shops, int itemViewResId) {
        if (shops == null || shops.isEmpty()) {
            Common.showToast(activity, R.string.textNoShopsFound);
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
        setAdapter(rvFavorite, shops, R.layout.large_shop_item_view);
    }


    private class ShopAdapter extends RecyclerView.Adapter<FavoriteFragment.ShopAdapter.MyViewHolder> {
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
        public FavoriteFragment.ShopAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(context)
                    .inflate(itemViewResId, parent, false);
            return new FavoriteFragment.ShopAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull FavoriteFragment.ShopAdapter.MyViewHolder holder, int position) {
            final Shop shop = shops.get(position);
            String url = Url.URL + "/ShopServlet";
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
            holder.itemView.setOnLongClickListener(v -> {
                PopupMenu popupMenu = new PopupMenu(activity, v , Gravity.END);
                popupMenu.inflate(R.menu.favorite_menu);
                popupMenu.setOnMenuItemClickListener(item -> {
                    if(item.getItemId() == R.id.delete){
                        if (Common.networkConnected(activity)){
                            String url1 = Url.URL + "/FavoriteServlet";
                            Gson gson = new Gson();
                            JsonObject jsonObject = new JsonObject();
                            Favorite favorite = new Favorite(memId, shop.getId());
                            String jsonOut = gson.toJson(favorite);
                            jsonObject.addProperty("action", "favoriteDelete");
                            jsonObject.addProperty("favorite", jsonOut);
                            int count = 0;
                            try{
                                favoriteDeleteTask = new CommonTask(url1, jsonObject.toString());
                                String result = favoriteDeleteTask.execute().get();
                                count = Integer.valueOf(result);
                            } catch (Exception e){
                                Log.e(TAG, e.toString());
                            }
                            if(count == 0){
                                Common.showToast(activity, "delete fail");
                            } else{
                                shops.remove(shop);
                                ShopAdapter.this.notifyDataSetChanged();
                                FavoriteFragment.this.shops.remove(shop);
                                //Common.showToast(activity, "delete successfully");
                            }
                        } else{
                            Common.showToast(activity, R.string.textNoNetwork);
                        }
                    }
                    return true;
                });
                popupMenu.show();
                return true;
            });
        }
    }

    private void handleViews(){
        layoutFavoriteNoItem = this.getView().findViewById(R.id.layoutFavoriteNoItem);
        ivBack = this.getView().findViewById(R.id.ivBack);
        ivFavoriteNoItem = this.getView().findViewById(R.id.ivFavoriteNoItem);
        tvFavoriteNoItem = this.getView().findViewById(R.id.tvFavoriteNoItem);
        btBackToMain = this.getView().findViewById(R.id.btBackToMain);
        ivFavoriteNoItem.setImageResource(R.drawable.coffee);

    }
}
