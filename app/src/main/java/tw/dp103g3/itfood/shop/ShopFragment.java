package tw.dp103g3.itfood.shop;


import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import tw.dp103g3.itfood.main.Common;
import tw.dp103g3.itfood.R;
import tw.dp103g3.itfood.main.Url;
import tw.dp103g3.itfood.main.DateTimePickerDialog;
import tw.dp103g3.itfood.task.CommonTask;
import tw.dp103g3.itfood.task.ImageTask;

import static tw.dp103g3.itfood.main.Common.PREFERENCES_CART;

public class ShopFragment extends Fragment {
    private final static String TAG = "TAG_ShopFragment";
    private SimpleDateFormat simpleDateFormat;
    private AppCompatActivity activity;
    private AppBarLayout appBarLayout;
    private ImageView ivBack, ivShop, ivCart, ivComment;
    private ImageTask shopImageTask, dishImageTask;
    private CommonTask getdishTask;
    private Shop shop;
    private List<Dish> dishes;
    private Map<Integer, Integer> orderDetails;
    private Toolbar tbTitle;
    private Button btTime;
    private TextView tvName, tvRate;
    private RecyclerView rvDish;
    private Gson gson;
    private File orderDetail;
    private SharedPreferences pref;
    private NavController navController;
    private BottomNavigationView bottomNavigationView;
    private Animator animator;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (AppCompatActivity) getActivity();
        pref = activity.getSharedPreferences(PREFERENCES_CART, Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_shop, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        navController = Navigation.findNavController(view);
        bottomNavigationView = activity.findViewById(R.id.bottomNavigation);
        animator = AnimatorInflater.loadAnimator(activity, R.animator.anim_bottom_navigation_slide_down);
        animator.setTarget(bottomNavigationView);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                bottomNavigationView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.start();
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd EEEE HH:mm", Locale.getDefault());
        tbTitle = view.findViewById(R.id.tbTitle);
        orderDetail = new File(activity.getFilesDir(), "orderDetail");
        try (BufferedReader in = new BufferedReader(new FileReader(orderDetail))) {
            String inStr = in.readLine();
            gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
            JsonObject jsonObject = gson.fromJson(inStr, JsonObject.class);
            String orderDetailsStr = jsonObject.get("orderDetails").getAsString();
            Type type = new TypeToken<Map<Integer, Integer>>(){}.getType();
            orderDetails = gson.fromJson(orderDetailsStr, type) != null ?
                    gson.fromJson(orderDetailsStr, type) : new HashMap<>();
        } catch (IOException e) {
            e.printStackTrace();
        }
        btTime = view.findViewById(R.id.btTime);
        Calendar showTime = Calendar.getInstance();
        showTime.set(Calendar.MINUTE, showTime.get(Calendar.MINUTE) - showTime.get(Calendar.MINUTE) % 15);
        showTime.add(Calendar.MINUTE, 45);
        btTime.setText(simpleDateFormat.format(showTime.getTime()));
        btTime.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MINUTE, 30);
            DateTimePickerDialog dialog = new DateTimePickerDialog(activity, cal.getTimeInMillis());
            dialog.setOnDateTimeSetListener((alertDialog, date) ->
                    btTime.setText(simpleDateFormat.format(date)));
            dialog.show();
        });
        ivBack = view.findViewById(R.id.ivBack);
        ivBack.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());
        ivCart = view.findViewById(R.id.ivCart);
        ivCart.setOnClickListener(v -> {
            navController.navigate(R.id.action_shopFragment_to_shoppingCartFragment);
        });
        Bundle bundle = getArguments();
        shop = (Shop) bundle.getSerializable("shop");
        ivComment = view.findViewById(R.id.ivComment);

        ivComment.setOnClickListener(v -> {
            navController.navigate(R.id.action_shopFragment_to_shopCommentFragment, bundle);
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
            int changeOffset = 220;
            if (offset > - changeOffset) {
                tbTitle.setPadding(0, 0, 0, 0);
                color = Color.argb((changeOffset + offset) * 255 / 220, 255, 243, 210);
                tvName.setTextColor(color);
                btTime.setTextColor(color);
                tvRate.setTextColor(color);
                PorterDuffColorFilter colorFilter = new PorterDuffColorFilter(getResources().getColor(R.color.colorWhite, activity.getTheme()), PorterDuff.Mode.SRC_ATOP);
                ivBack.getDrawable().setColorFilter(colorFilter);
                ivCart.getDrawable().setColorFilter(colorFilter);
                ivComment.getDrawable().setColorFilter(colorFilter);
            } else {
                tbTitle.setPadding(0, 0, tbTitle.getHeight(), 0);
                color = Color.argb((- changeOffset - offset) * 255 / (appBarLayout.getTotalScrollRange() - changeOffset), 91, 63, 54);
                tvName.setTextColor(Color.argb(0, 0, 0, 0));
                btTime.setTextColor(color);
                tvRate.setTextColor(Color.argb(0, 0, 0, 0));
                PorterDuffColorFilter colorFilter = new PorterDuffColorFilter(getResources().getColor(R.color.colorTextOnP, activity.getTheme()), PorterDuff.Mode.SRC_ATOP);
                ivBack.getDrawable().setColorFilter(colorFilter);
                ivCart.getDrawable().setColorFilter(colorFilter);
                ivComment.getDrawable().setColorFilter(colorFilter);
            }
        });
        double rate = (double) shop.getTtscore() / shop.getTtrate();
        String rateStr = shop.getTtrate() == 0 ? getResources().getString(R.string.textNoRate) :
                String.format(Locale.getDefault(), "%.1f (%d)", rate, shop.getTtrate());
        tvRate.setText(rateStr);

        rvDish = view.findViewById(R.id.rvDish);
//        rvDish.setPadding(0, rvDish.getPaddingTop(), 0, Common.getNavigationBarHeight(activity) + 20);
        rvDish.setLayoutManager(new LinearLayoutManager(activity));
        dishes = getDishes();
        ShowDishes(dishes);

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
            int dishId, count;
            ImageView ivDish;
            TextView tvDishName, tvInfo, tvPrice;
            ImageButton ibAdd, ibRemove;
            EditText etCount;
            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                ivDish = itemView.findViewById(R.id.ivDish);
                tvDishName = itemView.findViewById(R.id.tvDishName);
                tvInfo = itemView.findViewById(R.id.tvInfo);
                tvPrice = itemView.findViewById(R.id.tvPrice);
                ibAdd = itemView.findViewById(R.id.ibAdd);
                ibRemove = itemView.findViewById(R.id.ibRemove);
                etCount = itemView.findViewById(R.id.etCount);
            }
            void onEditCountClick(View view) {
                try (BufferedReader in = new BufferedReader(new FileReader(orderDetail))) {
                    StringBuilder sb = new StringBuilder();
                    String line = "";
                    while ((line = in.readLine()) != null) {
                        sb.append(line);
                    }
                    JsonObject jsonObject = gson.fromJson(sb.toString(), JsonObject.class);
                    String shopStr = jsonObject.get("shop").getAsString();
                    Shop odShop = gson.fromJson(shopStr, Shop.class);
                    if (odShop.getId() != 0 && !shop.equals(odShop)) {
                        Common.showToast(activity, "clear cart");
                        orderDetails = new HashMap<>();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (view.getId() == R.id.ibAdd) {
                    count++;
                } else {
                    count = count <= 0 ? 0 : count - 1;
                }
//                Log.d(TAG, String.valueOf(dishId));
                orderDetails.put(dishId, count);
                if (count <= 0){
                    orderDetails.remove(dishId);
                }
                try (BufferedWriter out = new BufferedWriter(new FileWriter(orderDetail))) {
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("shop", gson.toJson(shop));
                    jsonObject.addProperty("orderDetails", gson.toJson(orderDetails));
                    out.write(jsonObject.toString());
                } catch (IOException e) {
                    Log.e(TAG, e.toString());
                }
                Common.checkCart(activity, ivCart);
                etCount.setText(String.valueOf(count));
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
            holder.dishId = dish.getId();
            String url = Url.URL + "/DishServlet";
            String dishName = dish.getName();
            String info = dish.getInfo();
            int price = dish.getPrice();
            holder.count = orderDetails.get(dish.getId()) != null ? orderDetails.get(dish.getId()) : 0;
            holder.tvDishName.setText(dishName);
            holder.tvInfo.setText(info);
            holder.tvPrice.setText(String.format(Locale.getDefault(), "$ %d", price));
            holder.ivDish.setVisibility(View.GONE);
            dishImageTask = new ImageTask(url, dish.getId(), imageSize, holder.ivDish);
            dishImageTask.execute();
            holder.ibAdd.setOnClickListener(holder::onEditCountClick);
            holder.ibAdd.setOnClickListener(holder::onEditCountClick);
            holder.ibRemove.setOnClickListener(holder::onEditCountClick);
            holder.etCount.setText(String.valueOf(holder.count));
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
        PorterDuffColorFilter colorFilter = new PorterDuffColorFilter(getResources().getColor(R.color.colorTextOnP, activity.getTheme()), PorterDuff.Mode.SRC_ATOP);
        ivBack.getDrawable().setColorFilter(colorFilter);
        ivCart.getDrawable().setColorFilter(colorFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        animator = AnimatorInflater.loadAnimator(activity, R.animator.anim_bottom_navigation_slide_up);
        animator.setTarget(bottomNavigationView);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                bottomNavigationView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.start();
    }
}
