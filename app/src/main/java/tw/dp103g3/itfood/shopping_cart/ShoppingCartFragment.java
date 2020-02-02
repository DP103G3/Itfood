package tw.dp103g3.itfood.shopping_cart;


import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import tw.dp103g3.itfood.Common;
import tw.dp103g3.itfood.R;
import tw.dp103g3.itfood.Url;
import tw.dp103g3.itfood.address.Address;
import tw.dp103g3.itfood.main.MainActivity;
import tw.dp103g3.itfood.main.SharedViewModel;
import tw.dp103g3.itfood.shop.Dish;
import tw.dp103g3.itfood.shop.Shop;
import tw.dp103g3.itfood.task.CommonTask;

import static android.view.View.GONE;
import static tw.dp103g3.itfood.Common.DATE_FORMAT;
import static tw.dp103g3.itfood.Common.LOGIN_FALSE;
import static tw.dp103g3.itfood.Common.PREFERENCES_MEMBER;
import static tw.dp103g3.itfood.Common.getDayOfWeek;


public class ShoppingCartFragment extends Fragment {
    private static final String TAG = "TAG_ShoppingCartFragment";
    private Toolbar toolbarShoppingCart;
    private SharedPreferences memberPref;
    private Activity activity;
    private Button btLogin;
    private File orderDetail;
    private Shop shop;
    private Map<Integer, Integer> orderDetails;
    private Gson gson;
    private RecyclerView rvDish;
    private DishAdapter dishAdapter;
    private CommonTask getdishTask, getAddressTask;
    private List<Dish> dishes;
    private String shopName;
    private NavController navController;
    private static SparseIntArray totals;
    private View fragmentView;
    private TextView tvTotalBefore, tvTotalAfter, tvBottomTotal, tvAddress, tvOrderType, tvDeliveryTime;
    private int totalBefore, totalAfter, mem_id, orderType, selectedOrderType;
    private ScrollView scrollView;
    private BottomNavigationView bottomNavigationView;
    private Animator animator;
    private ConstraintLayout layoutBottom;
    private BottomNavigationView shoppingCartBottomView;
    private LinearLayout layoutDeliveryAddress, layoutOrderType, layoutDeliveryTime;
    private Address address;
    private Location lastLocation;
    private final int DELIVERY = 1;
    private final int SELFPICK = 0;
    private View divider;
    private SharedViewModel model;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        address = null;
        model = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        model.selectDeliveryTime(null);

        orderType = DELIVERY;
        totals = new SparseIntArray();
        memberPref = activity.getSharedPreferences(PREFERENCES_MEMBER, Context.MODE_PRIVATE);
        mem_id = memberPref.getInt("mem_id", LOGIN_FALSE);
        orderDetail = new File(activity.getFilesDir(), "orderDetail");

        if (mem_id != LOGIN_FALSE) {
            address = getAddresses(mem_id).isEmpty() ? null : getAddresses(mem_id).get(0);
        }

        try (BufferedReader in = new BufferedReader(new FileReader(orderDetail))) {
            gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
            String inStr = in.readLine();
            JsonObject jsonObject = gson.fromJson(inStr, JsonObject.class);
            String shopStr = jsonObject.get("shop").getAsString();
            shop = gson.fromJson(shopStr, Shop.class);
            shopName = shop.getName();
            String odStr = jsonObject.get("orderDetails").getAsString();
            Type type = new TypeToken<Map<Integer, Integer>>() {
            }.getType();
            orderDetails = gson.fromJson(odStr, type);
            orderDetails.forEach((v, u) -> Log.d(TAG, String.format("%d, %d", v, u)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        lastLocation = MainActivity.getLocation();

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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
        shoppingCartBottomView = view.findViewById(R.id.shoppingCartBottomView);
        layoutBottom = view.findViewById(R.id.layoutBottom);
        layoutDeliveryAddress = view.findViewById(R.id.layoutDeliveryAddress);
        tvBottomTotal = view.findViewById(R.id.tvBottomTotal);
        tvTotalAfter = view.findViewById(R.id.tvTotalAfter);
        tvAddress = view.findViewById(R.id.tvAddress);
        tvTotalBefore = view.findViewById(R.id.tvTotalBefore);
        tvDeliveryTime = view.findViewById(R.id.tvDeliveryTime);
        tvOrderType = view.findViewById(R.id.tvOrderType);
        navController = Navigation.findNavController(view);
        scrollView = view.findViewById(R.id.scrollView);
        layoutOrderType = view.findViewById(R.id.layoutOrderType);
        layoutDeliveryTime = view.findViewById(R.id.layoutDeliveryTime);
        divider = view.findViewById(R.id.divider);
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


        model.getSelectedAddress().observe(getViewLifecycleOwner(), address -> {
            this.address = address;
            tvAddress.setText(address.getInfo());
        });

        layoutDeliveryTime.setOnClickListener(v -> {
            DeliveryTimeSelectDialog dtsd = new DeliveryTimeSelectDialog(activity, model);
            dtsd.show();
        });

        layoutOrderType.setOnClickListener(v -> {
            AlertDialog.Builder alt_bld = new AlertDialog.Builder(activity);
            selectedOrderType = orderType;
            String[] orderTypes = {"自取", "外送"};
            alt_bld.setTitle("請選擇送餐方式");
            alt_bld.setSingleChoiceItems(orderTypes, orderType, (dialog, which) -> selectedOrderType = which);

            alt_bld.setNegativeButton("取消", (dialog, which) -> dialog.cancel());

            alt_bld.setPositiveButton("確認", (dialog, which) -> {
                orderType = selectedOrderType;
                updateDisplayOrderType();
            });
            AlertDialog alertDialog = alt_bld.create();
            alertDialog.show();
        });


        int height = (int) (shoppingCartBottomView.getHeight() * getResources().getDisplayMetrics().density);
        scrollView.setPadding(0, 0, 0, height);

        tvDeliveryTime.setText(R.string.tvDeliveryTime);

        model.getSelectedDeliveryTime().observe(getViewLifecycleOwner(), date -> {
            if (date != null) {
                Calendar calNow = Calendar.getInstance();
                Calendar cal = Calendar.getInstance();
                Calendar tomorrow = Calendar.getInstance();
                tomorrow.add(Calendar.DAY_OF_YEAR, 1);
                cal.setTime(date);
                String text;
                if (cal.get(Calendar.DAY_OF_WEEK) == calNow.get(Calendar.DAY_OF_WEEK)) {
                    text = "今天, "
                            + (cal.get(Calendar.HOUR_OF_DAY) < 10 ? "0" + cal.get(Calendar.HOUR_OF_DAY) : cal.get(Calendar.HOUR_OF_DAY))
                            + ":" +
                            (cal.get(Calendar.MINUTE) == 0 ? "00" : cal.get(Calendar.MINUTE));
                } else if (cal.get(Calendar.DAY_OF_WEEK) == tomorrow.get(Calendar.DAY_OF_WEEK)) {
                    text = "明天, "
                            + (cal.get(Calendar.HOUR_OF_DAY) < 10 ? "0" + cal.get(Calendar.HOUR_OF_DAY) : cal.get(Calendar.HOUR_OF_DAY))
                            + ":" +
                            (cal.get(Calendar.MINUTE) == 0 ? "00" : cal.get(Calendar.MINUTE));
                } else {
                    text = getDayOfWeek(cal) + ", " +
                            (cal.get(Calendar.HOUR_OF_DAY) < 10 ? "0" + cal.get(Calendar.HOUR_OF_DAY) : cal.get(Calendar.HOUR_OF_DAY))
                            + ":" +
                            (cal.get(Calendar.MINUTE) == 0 ? "00" : cal.get(Calendar.MINUTE));
                }
                tvDeliveryTime.setText(text);
            }
        });

        //設定tvAddress的預設文字，如果未登入會顯示最近一次定位的地址，登入則會顯示第一個個人地址
        if (address != null) {
            tvAddress.setText(address.getInfo());
        } else {
            double latitude = lastLocation.getLatitude();
            double longitude = lastLocation.getLongitude();
            android.location.Address localAddress = reverseGeocode(latitude, longitude);
            StringBuilder sb = new StringBuilder();
            if (localAddress != null) {
                for (int i = 0; i <= localAddress.getMaxAddressLineIndex(); i++) {
                    sb.append(localAddress.getAddressLine(i)).append("\n");
                }
            }
            tvAddress.setText(sb);
        }

        //確認登入狀態，以之決定btLogin是否顯示
        btLogin = view.findViewById(R.id.btLogin);
        if (mem_id != LOGIN_FALSE) {
            btLogin.setVisibility(GONE);
        }
        btLogin.setOnClickListener(v -> Navigation.findNavController(v)
                .navigate(R.id.action_shoppingCartFragment_to_loginFragment));

        if (mem_id != LOGIN_FALSE) {
            layoutDeliveryAddress.setOnClickListener(v -> {
                model.selectAddress(address);
                Navigation.findNavController(v).navigate(R.id.action_shoppingCartFragment_to_addressSelectFragment);
            });
        }

        toolbarShoppingCart = view.findViewById(R.id.toolbarShoppingCart);
        toolbarShoppingCart.setTitle(shopName);
        toolbarShoppingCart.setNavigationOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        dishes = new ArrayList<>();
        orderDetails.forEach((dish_id, dish_count) -> dishes.add(getDish(dish_id)));

        rvDish = view.findViewById(R.id.rvDish);

        rvDish.setLayoutManager(new LinearLayoutManager(activity));
        ShowDishes(dishes);

    }

    private List<Address> getAddresses(int mem_id) {
        List<Address> addresses = new ArrayList<>();
        if (Common.networkConnected(activity)) {
            String url = Url.URL + "/AddressServlet";
            JsonObject jsonObject = new JsonObject();
            Gson gson = new GsonBuilder().setDateFormat(DATE_FORMAT).create();
            jsonObject.addProperty("action", "getAllShow");
            jsonObject.addProperty("mem_id", mem_id);
            String jsonOut = jsonObject.toString();
            getAddressTask = new CommonTask(url, jsonOut);
            try {
                String jsonIn = getAddressTask.execute().get();
                Type listType = new TypeToken<List<Address>>() {
                }.getType();
                addresses = gson.fromJson(jsonIn, listType);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Common.showToast(activity, R.string.textNoNetwork);
        }
        return addresses;
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

    private android.location.Address reverseGeocode(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(activity);
        List<android.location.Address> addressList = null;
        try {
            addressList = geocoder.getFromLocation(latitude, longitude, 1);
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }

        if (addressList == null || addressList.isEmpty()) {
            return null;
        } else {
            return addressList.get(0);
        }
    }

    private class DishAdapter extends RecyclerView.Adapter<DishAdapter.MyViewHolder> {
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
                if (count <= 0) {
                    orderDetails.remove(dishId);

                    dishes.clear();

                    orderDetails.forEach((dish_id, dish_count) -> dishes.add(getDish(dish_id)));

                    DishAdapter dishAdapter = (DishAdapter) rvDish.getAdapter();
                    dishAdapter.setDishes(dishes);
                    dishAdapter.notifyDataSetChanged();
                }
                try (BufferedWriter out = new BufferedWriter(new FileWriter(orderDetail))) {
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("shop", gson.toJson(shop));
                    jsonObject.addProperty("orderDetails", gson.toJson(orderDetails));
                    out.write(jsonObject.toString());
                } catch (IOException e) {
                    Log.e(TAG, e.toString());
                }
                tvCount.setText(String.valueOf(count));
                tvDishPrice.setText(String.format(Locale.getDefault(), "$ %d", dish.getPrice() * count));
                if (orderDetails.isEmpty()) {
                    try (BufferedWriter out = new BufferedWriter(new FileWriter(orderDetail))) {
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("shop", gson.toJson(new Shop()));
                        jsonObject.addProperty("orderDetails", gson.toJson(orderDetails));
                        out.write(jsonObject.toString());
                    } catch (IOException e) {
                        Log.e(TAG, e.toString());
                    }
                    navController.popBackStack(R.id.mainFragment, false);
                }
                totals.append(dishId, dish.getPrice() * count);
                for (int i = 0; i < totals.size(); i++) {
                    totalBefore += totals.valueAt(i);
                }
                totalAfter = totalBefore + 30;
                tvTotalBefore.setText(String.format(Locale.getDefault(), "$ %d", totalBefore));
                tvTotalAfter.setText(String.format(Locale.getDefault(), "$ %d", totalAfter));
                tvBottomTotal.setText(String.format(Locale.getDefault(), "$ %d", totalAfter));
            }
        }

        @NonNull
        @Override
        public DishAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(context)
                    .inflate(R.layout.dish_cart_item_view, parent, false);
            return new DishAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull DishAdapter.MyViewHolder holder, int position) {
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
            totalBefore = 0;
            totalAfter = 0;
            for (int i = 0; i < totals.size(); i++) {
                totalBefore += totals.valueAt(i);
            }
            totalAfter = totalBefore + 30;
            tvTotalBefore.setText(String.format(Locale.getDefault(), "$ %d", totalBefore));
            tvTotalAfter.setText(String.format(Locale.getDefault(), "$ %d", totalAfter));
            tvBottomTotal.setText(String.format(Locale.getDefault(), "$ %d", totalAfter));
        }
    }

    private void updateDisplayOrderType() {
        if (orderType == DELIVERY) {
            tvOrderType.setText("外送");
            layoutDeliveryAddress.setVisibility(View.VISIBLE);
            divider.setVisibility(View.VISIBLE);
        } else if (orderType == SELFPICK) {
            tvOrderType.setText("自取");
            layoutDeliveryAddress.setVisibility(GONE);
            divider.setVisibility(GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Bundle bundle = getArguments();
        if (bundle != null) {
            address = (Address) bundle.getSerializable("address");
        }
    }

    @Override
    public void onStop() {
        super.onStop();
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
