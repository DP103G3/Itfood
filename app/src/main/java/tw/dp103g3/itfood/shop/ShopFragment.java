package tw.dp103g3.itfood.shop;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
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
import tw.dp103g3.itfood.task.CommonTask;
import tw.dp103g3.itfood.task.ImageTask;

public class ShopFragment extends Fragment {
    private final static String TAG = "TAG_ShopFragment";
    private AppCompatActivity activity;
    private AppBarLayout appBarLayout;
    private ImageView ivBack, ivShop, ivComment;
    private ImageTask shopImageTask, dishImageTask;
    private CommonTask getdishTask;
    private Shop shop;
    private List<Dish> dishes;
    private TextView tvName, tvTime, tvRate;
    private RecyclerView rvDish;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (AppCompatActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_shop, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        initToolbar(R.id.toolbar, view);
        initToolbar(R.id.tbTitle, view);
        tvTime = view.findViewById(R.id.tvTime);
        ivBack = view.findViewById(R.id.ivBack);
        ivBack.setOnClickListener(v -> {
            Navigation.findNavController(v).popBackStack();
        });

        Bundle bundle = getArguments();
        shop = (Shop) bundle.getSerializable("shop");
        ivComment = view.findViewById(R.id.ivComment);

        ivComment.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_shopFragment_to_shopCommentFragment, bundle);
        });

        ivShop = view.findViewById(R.id.ivShop);
        String url = Url.URL + "/ShopServlet";
        int imageSize = getResources().getDisplayMetrics().widthPixels;
        shopImageTask = new ImageTask(url, shop.getId(), imageSize);
        try {
            Bitmap bitmap = shopImageTask.execute().get();
            ivShop.setImageBitmap(bitmap);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        tvName = view.findViewById(R.id.tvName);
        tvRate = view.findViewById(R.id.tvRate);
        tvName.setText(shop.getName());
        appBarLayout = view.findViewById(R.id.appBarLayout);
        appBarLayout.addOnOffsetChangedListener((appBarLayout, offset) -> {
            int color;
            int changeOffset = 150;
            if (offset > - changeOffset) {
                color = Color.argb((int) ((200 + offset) * 1.28) - 1, 255, 243, 210);
                tvName.setTextColor(color);
                tvTime.setTextColor(color);
                tvRate.setTextColor(color);
                PorterDuffColorFilter colorFilter = new PorterDuffColorFilter(getResources().getColor(R.color.colorWhite, activity.getTheme()), PorterDuff.Mode.SRC_ATOP);
                ivBack.getDrawable().setColorFilter(colorFilter);
            } else {
                color = Color.argb((- changeOffset - offset) * 255 / (appBarLayout.getTotalScrollRange() - changeOffset), 91, 63, 54);
                tvName.setTextColor(Color.argb(0, 0, 0, 0));
                tvTime.setTextColor(color);
                tvRate.setTextColor(Color.argb(0, 0, 0, 0));
                PorterDuffColorFilter colorFilter = new PorterDuffColorFilter(getResources().getColor(R.color.colorTextOnP, activity.getTheme()), PorterDuff.Mode.SRC_ATOP);
                ivBack.getDrawable().setColorFilter(colorFilter);
            }
        });
        double rate = (double) shop.getTtscore() / shop.getTtrate();
        tvRate.setText(String.format(Locale.getDefault(),
                "%.1f(%d)", rate, shop.getTtrate()));

        rvDish = view.findViewById(R.id.rvDish);
        rvDish.setPadding(0, rvDish.getPaddingTop(), 0, Common.getNavigationBarHeight(activity) * 2 + Common.getStatusBarHeight(activity) + 20);
        rvDish.setLayoutManager(new LinearLayoutManager(activity));
        dishes = getDishes();
        ShowDishes(dishes);

    }

    private void initToolbar(int resId, View view) {
        Toolbar toolbar = view.findViewById(resId);
        ViewGroup.LayoutParams params = toolbar.getLayoutParams();
        params.height += Common.getStatusBarHeight(activity);
        toolbar.setLayoutParams(params);
        toolbar.setPadding(0, Common.getStatusBarHeight(activity), 0, 0);
    }

    private List<Dish> getDishes() {
        List<Dish> dishes = new ArrayList<>();
        if (Common.networkConnected(activity)) {
            String url = Url.URL + "/DishServlet";
            JsonObject jsonObject = new JsonObject();
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
            jsonObject.addProperty("action", "getAllShow");
            jsonObject.addProperty("shop_id", shop.getId());
            String jsonOut = jsonObject.toString();
            getdishTask = new CommonTask(url, jsonOut);
            try {
                String jsonIn = getdishTask.execute().get();
                Type listType = new TypeToken<List<Dish>>(){
                }.getType();
                dishes = gson.fromJson(jsonIn, listType);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Common.showToast(activity, R.string.textNoNetwork);
        }
        return dishes;
    }

    private void ShowDishes(List<Dish> dishes) {
        if (dishes == null || dishes.isEmpty()) {
            if (Common.networkConnected(activity)) {
                Common.showToast(activity, R.string.textNoDishesFound);
            }
        }
        DishAdapter dishAdapter = (DishAdapter) rvDish.getAdapter();
        if (dishAdapter == null) {
            rvDish.setAdapter(new DishAdapter(activity, dishes));
        } else {
            dishAdapter.setDishes(dishes);
            dishAdapter.notifyDataSetChanged();
        }
    }

    private class DishAdapter extends RecyclerView.Adapter<DishAdapter.MyViewHolder> {
        private Context context;
        private List<Dish> dishes;
        private int imageSize;

        public DishAdapter(Context context, List<Dish> dishes) {
            this.context = context;
            this.dishes = dishes;
            imageSize = getResources().getDisplayMetrics().widthPixels / 4;
        }

        void setDishes(List<Dish> dishes) {
            this.dishes = dishes;
        }

        @Override
        public int getItemCount() {
            return dishes.size();
        }

        private class MyViewHolder extends RecyclerView.ViewHolder {
            ImageView ivDish;
            TextView tvDishName, tvInfo, tvPrice;
            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                ivDish = itemView.findViewById(R.id.ivDish);
                tvDishName = itemView.findViewById(R.id.tvDishName);
                tvInfo = itemView.findViewById(R.id.tvInfo);
                tvPrice = itemView.findViewById(R.id.tvPrice);
            }
        }

        @NonNull
        @Override
        public DishAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(context)
                    .inflate(R.layout.dish_item_view, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull DishAdapter.MyViewHolder holder, int position) {
            final Dish dish = dishes.get(position);
            String url = Url.URL + "/DishServlet";
            String dishName = dish.getName();
            String info = dish.getInfo();
            int price = dish.getPrice();
            holder.tvDishName.setText(dishName);
            holder.tvInfo.setText(info);
            holder.tvPrice.setText("$" + String.valueOf(price));
            dishImageTask = new ImageTask(url, dish.getId(), imageSize, holder.ivDish);
            dishImageTask.execute();
        }
    }
}
