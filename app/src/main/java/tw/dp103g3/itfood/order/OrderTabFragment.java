package tw.dp103g3.itfood.order;


import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import tw.dp103g3.itfood.R;
import tw.dp103g3.itfood.main.Common;
import tw.dp103g3.itfood.main.Url;
import tw.dp103g3.itfood.shop.Dish;
import tw.dp103g3.itfood.shop.Shop;
import tw.dp103g3.itfood.task.CommonTask;

import static android.view.View.GONE;
import static tw.dp103g3.itfood.main.Common.DATE_FORMAT;
import static tw.dp103g3.itfood.main.Common.orderWebSocketClient;
import static tw.dp103g3.itfood.main.Common.setDialogUi;

/**
 * A simple {@link Fragment} subclass.
 */
public class OrderTabFragment extends Fragment {
    private static final String TAG = "TAG_OrderTabFragment";
    private int counter;
    private static final int UNCONFIRMED = 0;
    private static final int MAKING = 1;
    private static final int PICKUP = 2;
    private static final int DELIVERING = 3;
    private static final int DONE = 4;
    private static final int CANCEL = 5;
    private static final int SELFPICK = 0;
    private static final int DELIVERY = 1;

    private RecyclerView rvOrder;
    private Activity activity;
    private ConstraintLayout layoutEmpty;
    private Set<Integer> order_states;
    private List<Order> sortedOrders;
    private CommonTask editOrderTask;
    private LocalBroadcastManager broadcastManager;
    private ImageView imageView8;

    OrderTabFragment(int counter) {
        this.counter = counter;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_order_tab, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        broadcastManager = LocalBroadcastManager.getInstance(activity);
        registerOrderReceiver();
        order_states = new HashSet<>();
        if (order_states.isEmpty()) {
            if (counter == 0) {
                order_states.add(0);
                order_states.add(1);
                order_states.add(2);
                order_states.add(3);
            } else if (counter == 1) {
                order_states.add(4);

            } else if (counter == 2) {
                order_states.add(5);
            }
        }
        imageView8 = view.findViewById(R.id.imageView8);
        PorterDuffColorFilter colorFilter = new PorterDuffColorFilter(getResources()
                .getColor(R.color.colorTextOnP, activity.getTheme()), PorterDuff.Mode.SRC_ATOP);
        imageView8.getDrawable().setColorFilter(colorFilter);
        rvOrder = view.findViewById(R.id.rvOrder);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);
        rvOrder.setLayoutManager(new LinearLayoutManager(activity));
        rvOrder.setPadding(0, 0, 0, Common.getNavigationBarHeight(activity));
    }

    @Override
    public void onResume() {
        super.onResume();

        sortedOrders = OrderFragment.getOrders().stream().filter(order -> order_states.stream()
                .anyMatch(v -> v == order.getOrder_state()))
                .sorted(Comparator.comparing((Order order) -> order.getOrder_time().getTime()).reversed())
                .collect(Collectors.toList());

        if (!sortedOrders.isEmpty()) {
            layoutEmpty.setVisibility(GONE);
            rvOrder.setVisibility(View.VISIBLE);
        } else {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvOrder.setVisibility(GONE);
        }
        ShowOrders(sortedOrders);
    }

    @Nullable
    @Override
    public Animator onCreateAnimator(int transit, boolean enter, int nextAnim) {
        if (nextAnim != 0x0) {
            Animator animator = AnimatorInflater.loadAnimator(getActivity(), nextAnim);
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    // We just need know animation ending when fragment entered and no need to know when exited
                    if (enter) {
                        // here add data to recyclerview adapter
                        ShowOrders(sortedOrders);
                    }
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });
            return animator;
        }
        return null;
    }

    private void ShowOrders(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            if (!Common.networkConnected(activity)) {
                Common.showToast(activity, R.string.textNoNetwork);
            }
        }
        OrderAdapter orderAdapter = (OrderAdapter) rvOrder.getAdapter();
        if (orderAdapter == null) {
            rvOrder.setAdapter(new OrderAdapter(activity, orders));
        } else {
            orderAdapter.setOrders(orders);
            orderAdapter.notifyDataSetChanged();
        }
    }

    private void registerOrderReceiver() {
        IntentFilter orderFilter = new IntentFilter("order");
        broadcastManager.registerReceiver(orderReceiver, orderFilter);
    }

    private BroadcastReceiver orderReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            Order order = Common.gson.fromJson(message, Order.class);
            Set<Order> orders = OrderFragment.getOrders();
            orders.remove(order);
            orders.add(order);
            OrderFragment.setOrders(orders);
            onResume();
            Log.d(TAG, message);
        }
    };

    private class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.MyViewHolder> {
        private Context context;
        private List<Order> orders;

        OrderAdapter(Context context, List<Order> orders) {
            this.context = context;
            this.orders = orders;
        }

        void setOrders(List<Order> orders) {
            this.orders = orders;
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            final Order order = orders.get(position);
            holder.rvOrderDetail.setVisibility(GONE);
            Shop shop = order.getShop();
            List<OrderDetail> orderDetails = order.getOrderDetails();
//            Log.d(TAG, "orderDetail1");
            Date order_time = order.getOrder_time();
            Date order_ideal = order.getOrder_ideal();
            Date order_delivery = order.getOrder_delivery();
            int order_ttprice = order.getOrder_ttprice();
            int order_state = order.getOrder_state();
            int order_type = order.getOrder_type();

            String order_state_text = "";
            String order_type_text = "";
            String order_ttprice_text;
            String order_time_text = "";

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());


            DecimalFormatSymbols symbols = new DecimalFormatSymbols();
            symbols.setDecimalSeparator(',');
            DecimalFormat decimalFormat = new DecimalFormat("$ ###,###,###,###", symbols);
            order_ttprice_text = decimalFormat.format(order_ttprice);

            holder.ibExpandable.setOnClickListener(v -> {
                boolean show = toggleLayout(!order.isExpanded(), v, holder.rvOrderDetail);
                order.setExpanded(show);
            });

            switch (order_state) {
                case UNCONFIRMED:
                    order_state_text = "已付款，等待接單";
                    order_time_text = "下單時間 : " + simpleDateFormat.format(order_time);
                    holder.btAction.setText("取消訂單");
                    holder.btAction.setEnabled(true);
                    break;
                case MAKING:
                    order_state_text = "製作中";
                    order_time_text = "下單時間 : " + simpleDateFormat.format(order_time);
                    holder.btAction.setText("顯示QR CODE");
                    holder.btAction.setEnabled(false);
                    break;
                case PICKUP:
                    order_state_text = order.getOrder_type() == 0 ? "製作完成，待取餐" : "等待外送員取餐";
                    order_time_text = "下單時間 : " + simpleDateFormat.format(order_time);
                    holder.btAction.setText("顯示QR CODE");
                    if (order.getOrder_type() == 0) {
                        holder.btAction.setEnabled(true);
                    } else {
                        holder.btAction.setEnabled(false);
                    }
                    break;
                case DELIVERING:
                    order_state_text = "運送中";
                    if (order.getOrder_ideal() != null) {
                        order_time_text = "預計送達時間 : " + simpleDateFormat.format(order_ideal);
                    }
                    holder.btAction.setText("顯示QR CODE");
                    holder.btAction.setEnabled(true);
                    break;
                case DONE:
                    order_state_text = "已取餐";
                    order_time_text = "訂單完成時間 : " + simpleDateFormat.format(order_delivery);
                    holder.btAction.setText("重新下單");
                    holder.btAction.setOnClickListener(v -> {
                        File orderDetail = new File(activity.getFilesDir(), "orderDetail");
                        Gson gson = Common.gson;
                        try (BufferedWriter out = new BufferedWriter(new FileWriter(orderDetail))) {
                            Map<Integer, Integer> orderDetailsMap = new HashMap<>();
                            for (int i = 0; i < orderDetails.size(); i++) {
                                orderDetailsMap.put(orderDetails.get(i).getDish().getId(), orderDetails.get(i).getOd_count());
                            }
                            JsonObject jsonObject = new JsonObject();
                            jsonObject.addProperty("shop", gson.toJson(shop));
                            Type type = new TypeToken<Map<Integer, Integer>>() {
                            }.getType();
                            jsonObject.addProperty("orderDetails", gson.toJson(orderDetailsMap, type));
                            out.write(jsonObject.toString());
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("shop", shop);
                            Navigation.findNavController(v).navigate(R.id.action_orderFragment_to_shoppingCartFragment, bundle);
                        } catch (IOException e) {
                            Log.e(TAG, e.toString());
                        }
                    });
                    break;
                case CANCEL:
                    order_state_text = "已取消訂單";
                    simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    order_time_text = "下單時間 : " + simpleDateFormat.format(order_time);
                    holder.btAction.setText("重新下單");
                    holder.btAction.setOnClickListener(v -> {
                        File orderDetail = new File(activity.getFilesDir(), "orderDetail");
                        Gson gson = Common.gson;
                        try (BufferedWriter out = new BufferedWriter(new FileWriter(orderDetail))) {
                            Map<Integer, Integer> orderDetailsMap = new HashMap<>();
                            for (int i = 0; i < orderDetails.size(); i++) {
                                orderDetailsMap.put(orderDetails.get(i).getDish().getId(), orderDetails.get(i).getOd_count());
                            }
                            JsonObject jsonObject = new JsonObject();
                            jsonObject.addProperty("shop", gson.toJson(shop));
                            Type type = new TypeToken<Map<Integer, Integer>>() {
                            }.getType();
                            jsonObject.addProperty("orderDetails", gson.toJson(orderDetailsMap, type));
                            out.write(jsonObject.toString());
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("shop", shop);
                            Navigation.findNavController(v).navigate(R.id.action_orderFragment_to_shoppingCartFragment, bundle);
                        } catch (IOException e) {
                            Log.e(TAG, e.toString());
                        }
                    });
                    break;
            }

            if (order_type == SELFPICK) {
                order_type_text = "自取";
            } else if (order_type == DELIVERY) {
                order_type_text = "外送";
            }

            holder.tvShopName.setText(shop.getName());
            holder.tvType.setText(order_type_text);
            holder.tvTotal.setText(order_ttprice_text);
            holder.tvTime.setText(order_time_text);
            holder.tvState.setText(order_state_text);

            holder.rvOrderDetail.setLayoutManager(new LinearLayoutManager(activity));
            holder.rvOrderDetail.setAdapter(new OrderDetailAdapter(activity, orderDetails));
            holder.setOrder(order);
        }

        @Override
        public int getItemCount() {
            return orders.size();
        }


        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(context).inflate(R.layout.order_item_view, parent, false);
            return new MyViewHolder(itemView);
        }

        private boolean toggleLayout(boolean isExpanded, View v, RecyclerView rv) {
            OrderAnimations.toggleArrow(v, isExpanded);
            if (isExpanded) {
                OrderAnimations.expand(rv);
            } else {
                OrderAnimations.collapse(rv);
            }
            return isExpanded;

        }

        private class MyViewHolder extends RecyclerView.ViewHolder {
            Order order;
            TextView tvShopName, tvType, tvTotal, tvState, tvTime;
            ImageButton ibExpandable;
            Button btAction;
            RecyclerView rvOrderDetail;

            MyViewHolder(@NonNull View itemView) {
                super(itemView);

                tvShopName = itemView.findViewById(R.id.tvShopName);
                tvState = itemView.findViewById(R.id.tvState);
                tvType = itemView.findViewById(R.id.tvType);
                tvTotal = itemView.findViewById(R.id.tvTotal);
                ibExpandable = itemView.findViewById(R.id.ibExpandable);
                tvTime = itemView.findViewById(R.id.tvTime);
                btAction = itemView.findViewById(R.id.btAction);
                rvOrderDetail = itemView.findViewById(R.id.rvOrderDetail);
                btAction.setOnClickListener(this::onBtActionClick);
            }

            void setOrder(Order order) {
                this.order = order;
            }

            void onBtActionClick(View view) {
                switch (order.getOrder_state()) {
                    case UNCONFIRMED:
                        Dialog dialogCancel = new AlertDialog.Builder(activity)
                                .setTitle(R.string.textAlertDialogTitleCancelOrder)
                                .setMessage(R.string.textAlertDialogMessageCancelOrder)
                                .setPositiveButton("確定", (dialog, which) -> {
                                    String url = Url.URL + "/OrderServlet";
                                    JsonObject jsonObject = new JsonObject();
                                    Gson gson = new GsonBuilder().setDateFormat(DATE_FORMAT).create();
                                    jsonObject.addProperty("action", "orderUpdate");
                                    order.setOrder_state(CANCEL);
                                    jsonObject.addProperty("order", gson.toJson(order));
                                    int count = 0;
                                    try {
                                        String result = new CommonTask(url, jsonObject.toString()).execute().get();
                                        count = Integer.valueOf(result);
                                    } catch (Exception e) {
                                        Log.e(TAG, e.toString());
                                    }
                                    if (count == 0) {
                                        Common.showToast(getActivity(), R.string.textCancelOrderFail);
                                    } else {
                                        Common.showToast(getActivity(), R.string.textCancelOrderSuccess);
                                        Set<Order> orders = OrderFragment.getOrders();
                                        orders.remove(order);
                                        orders.add(order);
                                        OrderFragment.setOrders(orders);
                                        OrderMessage orderMessageShop = new OrderMessage(order, "shop" + order.getShop().getId());
                                        String shopMessage = Common.gson.toJson(orderMessageShop);
                                        orderWebSocketClient.send(shopMessage);
                                        onResume();
                                    }
                                }).setNegativeButton("取消", (dialog, which) -> dialog.cancel())
                                .create();
                        setDialogUi(dialogCancel, activity);
                        dialogCancel.show();
                        break;
                    case PICKUP:
                    case DELIVERING:

                        Bundle bundle = new Bundle();
                        bundle.putSerializable("order", order);
                        Navigation.findNavController(view)
                                .navigate(R.id.action_orderFragment_to_QRCodeFragment, bundle);
                        break;
                    case DONE:
                    case CANCEL:
                        break;
                }
            }
        }
    }

    private class OrderDetailAdapter extends RecyclerView.Adapter<OrderDetailAdapter.MyViewHolder> {
        private Context context;
        List<OrderDetail> orderDetails;

        OrderDetailAdapter(Context context, List<OrderDetail> orderDetails) {
            this.context = context;
            this.orderDetails = orderDetails;
        }

        @Override
        public void onBindViewHolder(@NonNull OrderDetailAdapter.MyViewHolder holder, int position) {
            DecimalFormatSymbols symbols = new DecimalFormatSymbols();
            symbols.setDecimalSeparator(',');
            DecimalFormat decimalFormat = new DecimalFormat("$ ###,###,###,###", symbols);
            if (position != orderDetails.size()) {
                final OrderDetail orderDetail = orderDetails.get(position);
                Dish dish = orderDetail.getDish();
                int dishPrice = orderDetail.getOd_price();
                String tvDishCountText = "x" + orderDetail.getOd_count();
                holder.tvDishCount.setText(tvDishCountText);
                holder.tvDishPrice.setText(decimalFormat.format(dishPrice));

                if (dish.getInfo() == null || dish.getInfo().isEmpty()) {
                    holder.tvDishInfo.setText("");
                } else {
                    holder.tvDishInfo.setText(dish.getInfo());
                }
                holder.tvDishName.setText(dish.getName());
            } else {
                holder.tvDishName.setText("外送費");
                holder.tvDishPrice.setText(decimalFormat.format(70));
            }
        }


        @Override
        public int getItemCount() {
            return orderDetails.size() + 1;
        }

        @NonNull
        @Override
        public OrderDetailAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(context).inflate(R.layout.order_detail_item_view, parent, false);
            return new MyViewHolder(itemView);
        }

        private class MyViewHolder extends RecyclerView.ViewHolder {
            TextView tvDishName, tvDishInfo, tvDishPrice, tvDishCount;

            MyViewHolder(@NonNull View itemView) {
                super(itemView);
                tvDishName = itemView.findViewById(R.id.tvDishName);
                tvDishInfo = itemView.findViewById(R.id.tvDishInfo);
                tvDishPrice = itemView.findViewById(R.id.tvDishPrice);
                tvDishCount = itemView.findViewById(R.id.tvDishCount);
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        broadcastManager.unregisterReceiver(orderReceiver);
    }


}
