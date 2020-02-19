package tw.dp103g3.itfood.shopping_cart;


import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
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
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import tw.dp103g3.itfood.Common;
import tw.dp103g3.itfood.R;
import tw.dp103g3.itfood.Url;
import tw.dp103g3.itfood.address.Address;
import tw.dp103g3.itfood.main.SharedViewModel;
import tw.dp103g3.itfood.member.Member;
import tw.dp103g3.itfood.order.Order;
import tw.dp103g3.itfood.order.OrderDetail;
import tw.dp103g3.itfood.payment.Payment;
import tw.dp103g3.itfood.shop.Dish;
import tw.dp103g3.itfood.shop.Shop;
import tw.dp103g3.itfood.task.CommonTask;

import static android.view.View.GONE;
import static tw.dp103g3.itfood.Common.DATE_FORMAT;
import static tw.dp103g3.itfood.Common.LOGIN_FALSE;
import static tw.dp103g3.itfood.Common.PREFERENCES_MEMBER;
import static tw.dp103g3.itfood.Common.formatCardNum;
import static tw.dp103g3.itfood.Common.getDayOfWeek;
import static tw.dp103g3.itfood.Common.setDialogUi;
import static tw.dp103g3.itfood.Common.showLoginDialog;


public class ShoppingCartFragment extends Fragment implements LoginDialogFragment.LoginDialogContract {
    private static final String TAG = "TAG_ShoppingCartFragment";
    private FragmentActivity activity;
    private File orderDetail;
    private Shop shop;
    private Map<Integer, Integer> orderDetails;
    private Gson gson;
    private String shopName;
    private NavController navController;
    private static SparseIntArray totals;
    private int totalBefore;
    private int totalAfter;
    private int mem_id;
    private int orderType;
    private int selectedOrderType;
    private BottomNavigationView bottomNavigationView;
    private Animator animator;
    private Address address;
    private Payment payment;
    private final int DELIVERY = 1;
    private final int SELFPICK = 0;
    private SharedViewModel model;
    private Date date;
    private Member member;
    private Cart cart;
    private List<Integer> dishIds;
    private SharedPreferences memberPref;
    @BindView(R.id.shoppingCartBottomView)
    BottomNavigationView shoppingCartBottomView;
    @BindView(R.id.layoutCheckOut)
    CardView layoutCheckOut;
    @BindView(R.id.layoutOrderType)
    LinearLayout layoutOrderType;
    @BindView(R.id.layoutDeliveryTime)
    LinearLayout layoutDeliveryTime;
    @BindView(R.id.layoutPayment)
    LinearLayout layoutPayment;
    @BindView(R.id.scrollView)
    ScrollView scrollView;
    @BindView(R.id.rvDish)
    RecyclerView rvDish;
    @BindView(R.id.divider)
    View divider;
    @BindView(R.id.layoutDeliveryAddress)
    LinearLayout layoutDeliveryAddress;
    @BindView(R.id.tvTotalBefore)
    TextView tvTotalBefore;
    @BindView(R.id.tvTotalAfter)
    TextView tvTotalAfter;
    @BindView(R.id.tvBottomTotal)
    TextView tvBottomTotal;
    @BindView(R.id.tvAddress)
    TextView tvAddress;
    @BindView(R.id.tvOrderType)
    TextView tvOrderType;
    @BindView(R.id.tvDeliveryTime)
    TextView tvDeliveryTime;
    @BindView(R.id.tvPaymentMethod)
    TextView tvPaymentMethod;
    @BindView(R.id.btLogin)
    Button btLogin;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        payment = null;
        date = null;
        model = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        model.selectDeliveryTime(null);
        model.selectPayment(null);
        orderType = DELIVERY;
        totals = new SparseIntArray();
        memberPref = activity.getSharedPreferences(PREFERENCES_MEMBER, Context.MODE_PRIVATE);
        mem_id = Common.getMemId(activity);

        orderDetail = new File(activity.getFilesDir(), "orderDetail");

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


        dishIds = new ArrayList<>();
        orderDetails.forEach((id, count) -> dishIds.add(id));
        cart = getCart(dishIds, mem_id);

        if (mem_id != LOGIN_FALSE) {
            payment = cart.getPayments().isEmpty() ? null : cart.getPayments().get(0);
            model.selectPayment(payment);
            member = cart.getMember();
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
        ButterKnife.bind(this, view);
        handleViews();
        navController = Navigation.findNavController(view);

        model.getSelectedAddress().observe(getViewLifecycleOwner(), address -> {
            this.address = address;
            if (address != null) {
                String addressString = address.getName() + " " + address.getInfo();
                tvAddress.setText(addressString);
            } else {
                tvAddress.setText("");
            }
        });


        model.getSelectedPayment().observe(getViewLifecycleOwner(), payment -> {
            this.payment = payment;
            if (payment != null) {
                tvPaymentMethod.setText(formatCardNum(payment.getPay_cardnum()));
            } else {
                tvPaymentMethod.setText("付現");
            }
        });

        layoutPayment.setOnClickListener(layoutPaymentListener());

        layoutDeliveryTime.setOnClickListener(v -> {
            DeliveryTimeSelectDialog dtsd = new DeliveryTimeSelectDialog(activity, model);
            Common.setDialogUi(dtsd, activity);
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
            Common.setDialogUi(alertDialog, activity);
            alertDialog.show();
        });


        int height = (int) (shoppingCartBottomView.getHeight() * getResources().getDisplayMetrics().density);
        scrollView.setPadding(0, 0, 0, height);

        tvDeliveryTime.setText(R.string.tvDeliveryTime);


        model.getSelectedDeliveryTime().observe(getViewLifecycleOwner(), date -> {
            if (date != null) {
                this.date = date;
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

        //設定tvAddress的預設文字
        if (address != null) {
            tvAddress.setText(address.getInfo());
        } else {
            tvAddress.setText("");
        }

        //確認登入狀態，以之決定btLogin是否顯示
        btLogin = view.findViewById(R.id.btLogin);
        if (mem_id != LOGIN_FALSE) {
            btLogin.setVisibility(GONE);
        }
        btLogin.setOnClickListener(v -> showLoginDialog(this));

        layoutDeliveryAddress.setOnClickListener(layoutDeliveryAddressListener());

        Toolbar toolbarShoppingCart = view.findViewById(R.id.toolbarShoppingCart);
        toolbarShoppingCart.setTitle(shopName);
        toolbarShoppingCart.setNavigationOnClickListener(v -> navController.popBackStack());

        List<Dish> dishes;
        dishes = cart.getDishes();

        rvDish = view.findViewById(R.id.rvDish);

        rvDish.setLayoutManager(new LinearLayoutManager(activity));
        ShowDishes(dishes);

        //結帳按鈕
        layoutCheckOut.setOnClickListener(layoutCheckListener());
    }

    @Override
    public void onResume() {
        super.onResume();
        /* 確認餐車內的店家可以送達選取的地址 */
        if (Common.Distance(address.getLatitude(), address.getLongitude()
                , shop.getLatitude(), shop.getLongitude()) > 5000) {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("送餐地址無法送達");
            builder.setMessage("餐車內的店家無法送達你所選取的地址，你要移除餐車嗎？");
            builder.setNegativeButton("返回", (dialog, which) -> {
                navController.popBackStack();
                dialog.cancel();
            });
            builder.setPositiveButton("好", (dialog, which) -> {
                clearCart();
                navController.popBackStack();
                dialog.dismiss();
            });
            Dialog dialog = builder.create();
            setDialogUi(dialog, activity);
            dialog.show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Common.showBottomNav(activity);
    }

    private int sendOrder(Order order) {
        Gson gson = new GsonBuilder().setDateFormat(DATE_FORMAT).create();
        List<JsonObject> jsonObjects = new ArrayList<>();
        int count = 0;
        String url = Url.URL + "/OrderServlet";
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("action", "orderInsert");
        jsonObject.addProperty("order", gson.toJson(order));
        orderDetails.forEach((dish_id, dish_count) -> {
            Dish dish = getDish(dish_id);
            JsonObject object = new JsonObject();
            object.addProperty("dish_id", dish_id);
            object.addProperty("od_count", dish_count);
            object.addProperty("od_price", (dish_count * dish.getPrice()));
            object.addProperty("od_message", "");
            jsonObjects.add(object);
        });
        jsonObject.addProperty("orderDetailsJson", gson.toJson(jsonObjects));
        String jsonOut = jsonObject.toString();
        try {
            String jsonIn = new CommonTask(url, jsonOut).execute().get();
            count = Integer.valueOf(jsonIn);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return count;
    }

    private Dish getDish(int dish_id) {
        Dish dish = null;
        if (Common.networkConnected(activity)) {
            String url = Url.URL + "/DishServlet";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getDishById");
            jsonObject.addProperty("id", dish_id);
            String jsonOut = jsonObject.toString();
            CommonTask getDishTask = new CommonTask(url, jsonOut);
            try {
                String jsonIn = getDishTask.execute().get();
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

    @Override
    public void sendLoginResult(boolean isSuccessful) {
        if (isSuccessful) {
            mem_id = memberPref.getInt("mem_id", 0);
            cart = getCart(dishIds, mem_id);
            address = cart.getAddresses().isEmpty() ? null : cart.getAddresses().get(0);
            payment = cart.getPayments().isEmpty() ? null : cart.getPayments().get(0);
            model.selectPayment(payment);
            model.selectAddress(address);
            layoutCheckOut.setOnClickListener(layoutCheckListener());
            layoutDeliveryAddress.setOnClickListener(layoutDeliveryAddressListener());
            layoutPayment.setOnClickListener(layoutPaymentListener());
            btLogin.setVisibility(GONE);
        }
    }

    private class DishAdapter extends RecyclerView.Adapter<DishAdapter.MyViewHolder> {
        private final Context context;
        private List<Dish> dishes;

        DishAdapter(Context context, List<Dish> dishes) {
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
            Dish dish;
            final TextView tvDishName;
            final TextView tvDishPrice;
            final TextView tvCount;
            final ImageButton ibAdd;
            final ImageButton ibRemove;

            MyViewHolder(@NonNull View itemView) {
                super(itemView);
                tvDishName = itemView.findViewById(R.id.tvDishName);
                tvDishPrice = itemView.findViewById(R.id.tvDishPrice);
                ibAdd = itemView.findViewById(R.id.btAdd);
                ibRemove = itemView.findViewById(R.id.btRemove);
                tvCount = itemView.findViewById(R.id.tvCount);
            }

            void onEditCountClick(View view) {
                dishId = dish.getId();
                totalBefore = 0;
                totalAfter = 0;
                if (view.getId() == R.id.btAdd) {
                    count++;
                } else {
                    count--;
                }
                orderDetails.put(dishId, count);
                if (count <= 0) {
                    orderDetails.remove(dishId);

                    dishes.clear();

                    List<Integer> dishIds = new ArrayList<>();
                    orderDetails.forEach((dish_id, dish_count) -> dishIds.add(dish_id));
                    Cart cart = getCart(dishIds, mem_id);
                    dishes = cart.getDishes();

                    DishAdapter dishAdapter = (DishAdapter) rvDish.getAdapter();
                    assert dishAdapter != null;
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
            holder.dish = dish;
            String dishName = dish.getName();

            holder.count = (orderDetails.get(dish.getId()) != null) ? orderDetails.get(dish.getId()) : 0;

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




    private Cart getCart(List<Integer> dishIds, int mem_id) {
        Cart cart = null;
        if (Common.networkConnected(activity)) {
            String dishIdsJson = gson.toJson(dishIds);
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getCart");
            jsonObject.addProperty("mem_id", mem_id);
            jsonObject.addProperty("dishIds", dishIdsJson);
            String url = Url.URL + "/OrderServlet";
            Gson gson = new GsonBuilder().setDateFormat(DATE_FORMAT).create();
            String jsonOut = jsonObject.toString();
            CommonTask getCartTask = new CommonTask(url, jsonOut);
            try {
                String jsonIn = getCartTask.execute().get();
                cart = gson.fromJson(jsonIn, Cart.class);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Common.showToast(activity, R.string.textNoNetwork);
            navController.popBackStack();
        }
        return cart;
    }

    private void handleViews() {
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
    }

    private void navigateToAddressSelect() {
        model.selectAddress(address);
        Bundle bundle = new Bundle();
        bundle.putInt("FROM", R.id.shoppingCartFragment);
        bundle.putSerializable("shop", shop);
        navController.navigate(R.id.action_shoppingCartFragment_to_addressSelectFragment, bundle);
    }

    private void navigateToPaymentSelect() {
        model.selectPayment(payment);
        navController.navigate(R.id.action_shoppingCartFragment_to_paymentSelectFragment);
    }


    private View.OnClickListener layoutDeliveryAddressListener() {
        return v -> {
            if (mem_id != LOGIN_FALSE) {
                navigateToAddressSelect();
            } else {
                showLoginDialog(this);
            }
        };
    }

    private View.OnClickListener layoutPaymentListener() {
        return v -> {
            if (mem_id != LOGIN_FALSE) {
                navigateToPaymentSelect();
            } else {
                showLoginDialog(this);
            }
        };
    }

    private View.OnClickListener layoutCheckListener() {
        return v -> {
            if (mem_id != LOGIN_FALSE) {
                Gson gson = new GsonBuilder().setDateFormat(DATE_FORMAT).create();
                int adrs_id;
                Date orderIdeal;
                List<OrderDetail> orderDetails = new ArrayList<>();
                Calendar now = Calendar.getInstance();

                if (date == null) {
                    now.add(Calendar.MINUTE, 30);
                    orderIdeal = now.getTime();
                    now.add(Calendar.MINUTE, -30);
                } else {
                    orderIdeal = date;
                }

                if (orderType == 0) {
                    adrs_id = 0;
                } else {
                    if (address != null) {
                        adrs_id = address.getId();
                    } else {
                        Common.showToast(activity, "送餐資料不可空白，請選取或新增地址");
                        return;
                    }
                }
                Order order;
                int pay_id = payment == null ? 0 : payment.getPay_id();

                if (payment != null) {
                    order = new Order(shop, mem_id, 0, pay_id, 0, orderIdeal,
                            now.getTime(), null, adrs_id, member.getMemName(), member.getMemPhone(),
                            totalAfter, 0, 0, orderType);
                } else {
                    order = new Order(shop, mem_id, 0, pay_id, 0, orderIdeal,
                            now.getTime(), null, adrs_id, member.getMemName(), member.getMemPhone(),
                            totalAfter, 0, 0, orderType);
                }

                int orderCount = sendOrder(order);
                if (orderCount != 0) {
                    try (BufferedWriter out = new BufferedWriter(new FileWriter(orderDetail))) {
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("shop", gson.toJson(new Shop()));
                        orderDetails.clear();
                        jsonObject.addProperty("orderDetails", gson.toJson(orderDetails));
                        out.write(jsonObject.toString());
                        navController.popBackStack();
                        Common.showToast(activity, "訂單下訂成功");
                    } catch (IOException e) {
                        Log.e(TAG, e.toString());
                        Common.showToast(activity, "訂單下訂失敗");
                    }
                } else {
                    Common.showToast(activity, "訂單下訂失敗");
                }
            } else {
                showLoginDialog(this);
            }
        };
    }

    private void clearCart() {
        Map<Integer, Integer> orderDetails = new HashMap<>();
        try (BufferedWriter out = new BufferedWriter(new FileWriter(orderDetail))) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("shop", gson.toJson(shop));
            jsonObject.addProperty("orderDetails", gson.toJson(orderDetails));
            out.write(jsonObject.toString());
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }
    }
}
