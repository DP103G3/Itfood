package tw.dp103g3.itfood;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import tw.dp103g3.itfood.shop.Dish;
import tw.dp103g3.itfood.task.CommonTask;

import static android.view.View.GONE;
import static tw.dp103g3.itfood.Common.PREFERENCES_CART;
import static tw.dp103g3.itfood.Common.PREFERENCES_MEMBER;


/**
 * A simple {@link Fragment} subclass.
 */
public class ShoppingCartFragment extends Fragment {
    private static final String TAG = "TAG_ShoppingCartFragment";
    private Toolbar toolbarShoppingCart;
    private SharedPreferences cartPref, memberPref;
    private Activity activity;
    private Button btLogin;
    private File orderDetail;
    private int mem_id, shop_id;
    private Map<Integer, Integer> orderDetails;
    private Gson gson;
    private RecyclerView rvDish;
    private DishAdapter dishAdapter;
    private CommonTask getdishTask;
    private List<Dish> dishes;
    private String shopName;
    private NavController navController;
    private static SparseIntArray totals;
    private View fragmentView;
    private TextView tvTotalBefore, tvTotalAfter;
    private int totalBefore, totalAfter;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        totals = new SparseIntArray();
        cartPref = activity.getSharedPreferences(PREFERENCES_CART, Context.MODE_PRIVATE);
        memberPref = activity.getSharedPreferences(PREFERENCES_MEMBER, Context.MODE_PRIVATE);
        shopName = cartPref.getString("shop_name", "");
        mem_id = memberPref.getInt("mem_id",0);
        Log.d(TAG, "mem_id :" + mem_id);
        orderDetail = new File(activity.getFilesDir(), "orderDetail");

        try (BufferedReader in = new BufferedReader(new FileReader(orderDetail))) {
            gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
            String inStr = in.readLine();
            JsonObject jsonObject = gson.fromJson(inStr, JsonObject.class);
            shop_id = jsonObject.get("shopId").getAsInt();
            String odStr = jsonObject.get("orderDetails").getAsString();
            Type type = new TypeToken<Map<Integer, Integer>>(){}.getType();
            orderDetails = gson.fromJson(odStr, type);
            orderDetails.forEach((v,u) -> Log.d(TAG, String.format("%d, %d", v, u)));
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_shopping_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fragmentView = view;
        tvTotalAfter = view.findViewById(R.id.tvTotalAfter);
        tvTotalBefore = view.findViewById(R.id.tvTotalBefore);
        navController = Navigation.findNavController(view);


        btLogin = view.findViewById(R.id.btLogin);
        if (mem_id != 0){
            btLogin.setVisibility(GONE);
        }
        btLogin.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_shoppingCartFragment_to_loginFragment));

        toolbarShoppingCart = view.findViewById(R.id.toolbarShoppingCart);
        toolbarShoppingCart.setTitle(shopName);
        toolbarShoppingCart.setNavigationOnClickListener(v -> Navigation.findNavController(v).popBackStack());
        dishes = new ArrayList<>();
        orderDetails.forEach((dish_id, dish_count) -> dishes.add(getDish(dish_id)));

        rvDish = view.findViewById(R.id.rvDish);

        rvDish.setLayoutManager(new LinearLayoutManager(activity));
        ShowDishes(dishes);


    }

    private Dish getDish(int dish_id) {
        Dish dish = null;
        if (Common.networkConnected(activity)) {
            String url = Url.URL + "/DishServlet";
            JsonObject jsonObject = new JsonObject();
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
            jsonObject.addProperty("action", "getDishById");
            jsonObject.addProperty("id", dish_id);
            String jsonOut = jsonObject.toString();
            getdishTask = new CommonTask(url, jsonOut);
            try {
                String jsonIn = getdishTask.execute().get();
                dish = gson.fromJson(jsonIn, Dish.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Common.showToast(activity, R.string.textNoNetwork);
        }
        return dish;
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

    private class DishAdapter extends RecyclerView.Adapter<ShoppingCartFragment.DishAdapter.MyViewHolder> {
        private Context context;
        private List<Dish> dishes;

        public DishAdapter(Context context, List<Dish> dishes) {
            this.context = context;
            this.dishes = dishes;
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
            TextView tvDishName, tvDishPrice, tvCount;
            ImageButton ibAdd, ibRemove;
            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                tvDishName = itemView.findViewById(R.id.tvDishName);
                tvDishPrice = itemView.findViewById(R.id.tvDishPrice);
                ibAdd = itemView.findViewById(R.id.btAdd);
                ibRemove = itemView.findViewById(R.id.btRemove);
                tvCount = itemView.findViewById(R.id.tvCount);
            }
            void onEditCountClick(View view) {
                Dish dish = getDish(dishId);
                totalBefore = 0;
                totalAfter = 0;
                if (view.getId() == R.id.btAdd) {
                    count++;
                } else {
                    count--;
                }
                Log.d(TAG, String.valueOf(dishId));
                orderDetails.put(dishId, count);
                if (count <= 0){
                    orderDetails.remove(dishId);

                    dishes.clear();

                    orderDetails.forEach((dish_id, dish_count) -> dishes.add(getDish(dish_id)));

                    DishAdapter dishAdapter = (DishAdapter) rvDish.getAdapter();
                    dishAdapter.setDishes(dishes);
                    dishAdapter.notifyDataSetChanged();
                }
                try (BufferedWriter out = new BufferedWriter(new FileWriter(orderDetail));) {
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("shopId", shop_id);
                    jsonObject.addProperty("orderDetails", gson.toJson(orderDetails));
                    out.write(jsonObject.toString());
                } catch (IOException e) {
                    Log.e(TAG, e.toString());
                }
                tvCount.setText(String.valueOf(count));
                tvDishPrice.setText(String.format(Locale.getDefault(), "$ %d", dish.getPrice() * count));
                if(orderDetails.isEmpty()){
                    navController.popBackStack();
                }
                totals.append(dishId, dish.getPrice() * count);
                for (int i = 0; i < totals.size(); i++ ){
                    totalBefore += totals.valueAt(i);
                }
                totalAfter = totalBefore + 30;
                tvTotalBefore.setText(String.format(Locale.getDefault(), "$ %d", totalBefore));
                tvTotalAfter.setText(String.format(Locale.getDefault(), "$ %d", totalAfter));



            }
        }

        @NonNull
        @Override
        public ShoppingCartFragment.DishAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(context)
                    .inflate(R.layout.dish_cart_item_view, parent, false);
            return new ShoppingCartFragment.DishAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull ShoppingCartFragment.DishAdapter.MyViewHolder holder, int position) {
            final Dish dish = dishes.get(position);
            holder.dishId = dish.getId();
            String dishName = dish.getName();


            holder.count = orderDetails.get(dish.getId()) != null ? orderDetails.get(dish.getId()) : 0;

            int price = dish.getPrice();
            int total = price * holder.count;

            holder.tvDishName.setText(dishName);
            holder.tvDishPrice.setText(String.format(Locale.getDefault(), "$ %d", total));
            holder.ibAdd.setOnClickListener(holder::onEditCountClick);
            holder.ibRemove.setOnClickListener(holder::onEditCountClick);
            holder.tvCount.setText(String.valueOf(holder.count));

            totals.append(dish.getId(), total);

        }

        @Override
        public void onViewAttachedToWindow(@NonNull MyViewHolder holder) {
            super.onViewAttachedToWindow(holder);
            Log.d(TAG, "TOTALS " + totals.toString());
            totalBefore = 0;
            totalAfter = 0;
            for (int i = 0; i < totals.size(); i++ ){
                totalBefore += totals.valueAt(i);
            }
            totalAfter = totalBefore + 30;
            tvTotalBefore.setText(String.format(Locale.getDefault(), "$ %d", totalBefore));
            tvTotalAfter.setText(String.format(Locale.getDefault(), "$ %d", totalAfter));
        }
    }


}
