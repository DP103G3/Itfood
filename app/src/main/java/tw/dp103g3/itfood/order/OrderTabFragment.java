package tw.dp103g3.itfood.order;


import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import tw.dp103g3.itfood.Common;
import tw.dp103g3.itfood.R;
import tw.dp103g3.itfood.Url;
import tw.dp103g3.itfood.shop.Dish;
import tw.dp103g3.itfood.shop.Shop;
import tw.dp103g3.itfood.task.CommonTask;

import static android.view.View.GONE;
import static tw.dp103g3.itfood.Common.DATE_FORMAT;
import static tw.dp103g3.itfood.Common.PREFERENCES_MEMBER;

/**
 * A simple {@link Fragment} subclass.
 */
public class OrderTabFragment extends Fragment {
    private static final String TAG = "TAG_OrderTabFragment";

    private static final String ARG_COUNT = "param1";
    private int counter;
    public static final int UNCONFIRMED = 0;
    public static final int MAKING = 1;
    public static final int PICKUP = 2;
    public static final int DELIVERING = 3;
    public static final int DONE = 4;
    public static final int CANCEL = 5;
    public static final int SELFPICK = 0;
    public static final int DELIVERY = 1;

    private RecyclerView rvOrder;
    private int mem_id;
    private Activity activity;
    private CommonTask getOrderTask, getShopTask, getOrderDetailTask, getDishTask;
    private List<Order> orders;
    private ConstraintLayout layoutEmpty;
    private SharedPreferences pref;
    private ProgressBar progressBar;
    private ArrayList<Integer> order_states;
    private ArrayList<Order> sortedOrders;



    public OrderTabFragment() {
        // Required empty public constructor
    }

    public static OrderTabFragment newInstance(int counter){
        OrderTabFragment orderTabFragment = new OrderTabFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COUNT, counter);
        orderTabFragment.setArguments(args);
        return orderTabFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null){
            counter = getArguments().getInt(ARG_COUNT);
        }
        activity = getActivity();
        order_states = new ArrayList<>();

        if(order_states.isEmpty()) {
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_order_tab, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        progressBar = view.findViewById(R.id.progressBar);
        rvOrder = view.findViewById(R.id.rvOrder);
        counter = getArguments().getInt(ARG_COUNT);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);

        pref = activity.getSharedPreferences(PREFERENCES_MEMBER, Context.MODE_PRIVATE);
        mem_id = pref.getInt("mem_id", 0);

        orders = new ArrayList<>();

        for (Integer state : order_states) {
            orders.addAll(getOrders(mem_id, state));
        }

        if(orders.isEmpty()){
            layoutEmpty.setVisibility(View.VISIBLE);
            progressBar.setVisibility(GONE);
        }

        sortedOrders = orders.stream().sorted
                (Comparator.comparing((Order order) -> order.getOrder_time().getTime()).reversed())
                .collect(Collectors.toCollection(ArrayList::new));

        rvOrder.setLayoutManager(new LinearLayoutManager(activity));
        rvOrder.setPadding(0, 0, 0, Common.getNavigationBarHeight(activity));

    }

    @Override
    public void onResume() {
        Log.d(TAG, "resume" + counter);
        rvOrder.setLayoutManager(new LinearLayoutManager(activity));
        progressBar.setVisibility(View.VISIBLE);
        rvOrder.setVisibility(GONE);
        orders = new ArrayList<>();
//        ArrayList<Integer> order_states = new ArrayList<>();
//
//        if (counter == 0){
//            order_states.clear();
//            order_states.add(0);
//            order_states.add(1);
//            order_states.add(2);
//            order_states.add(3);
//        } else if (counter == 1){
//            order_states.clear();
//            order_states.add(4);
//
//        } else if (counter == 2){
//            order_states.clear();
//            order_states.add(5);
//        }

        for (Integer state : order_states) {
            orders.addAll(getOrders(mem_id, state));
        }
        sortedOrders = orders.stream().sorted
                (Comparator.comparing((Order order) -> order.getOrder_time().getTime()).reversed())
                .collect(Collectors.toCollection(ArrayList::new));

        if(sortedOrders.isEmpty()){
            layoutEmpty.setVisibility(View.VISIBLE);
            progressBar.setVisibility(GONE);
        }
        ShowOrders(sortedOrders);
        if (!sortedOrders.isEmpty()){
            layoutEmpty.setVisibility(GONE);
        } else {
            layoutEmpty.setVisibility(View.VISIBLE);
        }
        super.onResume();
    }

    @Nullable
    @Override
    public Animator onCreateAnimator(int transit, boolean enter, int nextAnim) {
        if (nextAnim != 0x0) {
            Animator animator = AnimatorInflater.loadAnimator(getActivity(), nextAnim);
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {}
                @Override
                public void onAnimationEnd(Animator animation) {
                    // We just need know animation ending when fragment entered and no need to know when exited
                    if (enter) {
                        // here add data to recyclerview adapter
                        ShowOrders(sortedOrders);
                    }
                }
                @Override
                public void onAnimationCancel(Animator animation) {}
                @Override
                public void onAnimationRepeat(Animator animation) {}
            });
            return animator;
        }
        return null;
    }

    private List<Order> getOrders(int mem_id, int state){
        List <Order> orders = new ArrayList<>();
        if(Common.networkConnected(activity)){
            String url = Url.URL + "/OrderServlet";
            Gson gson = new GsonBuilder().setDateFormat(DATE_FORMAT).create();
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "findByCase");
            jsonObject.addProperty("type", "member");
            jsonObject.addProperty("id" , mem_id);
            jsonObject.addProperty("state", state);
            String jsonOut = jsonObject.toString();
            getOrderTask = new CommonTask(url, jsonOut);
            try {
                String jsonIn = getOrderTask.execute().get();
                Type listType = new TypeToken<List<Order>>(){}.getType();
                orders = gson.fromJson(jsonIn, listType);
            } catch (Exception e){
                Log.e(TAG, e.toString());
            }
        } else{
            Common.showToast(activity, R.string.textNoNetwork);
        }
        return orders;
    }

//    private List<OrderDetail> getOrderDetails(int order_id){
//        List <OrderDetail> orderDetails = new ArrayList<>();
//        if(Common.networkConnected(activity)){
//            String url = Url.URL + "/OrderDetailServlet";
//            Gson gson = new GsonBuilder().setDateFormat(DATE_FORMAT).create();
//            JsonObject jsonObject = new JsonObject();
//            jsonObject.addProperty("action", "findByOrderId");
//            jsonObject.addProperty("order_id",order_id );
//            String jsonOut = jsonObject.toString();
//            getOrderDetailTask = new CommonTask(url, jsonOut);
//            try {
//                String jsonIn = getOrderDetailTask.execute().get();
//                Type listType = new TypeToken<List<OrderDetail>>(){}.getType();
//                orderDetails = gson.fromJson(jsonIn, listType);
//            } catch (Exception e){
//                Log.e(TAG, e.toString());
//            }
//        } else{
//            Common.showToast(activity, R.string.textNoNetwork);
//        }
//        return orderDetails;
//    }

    private Dish getDish (int dish_id){
        Dish dish = null;
        if (Common.networkConnected(activity)){
            String url = Url.URL + "/DishServlet";
            Gson gson = new GsonBuilder().setDateFormat(DATE_FORMAT).create();
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getDishById");
            jsonObject.addProperty("id", dish_id);
            String jsonOut = jsonObject.toString();
            getDishTask = new CommonTask(url, jsonOut);
            try{
                String jsonIn = getDishTask.execute().get();
                dish = gson.fromJson(jsonIn, Dish.class);
            } catch (Exception e){
                Log.e(TAG, e.toString());
            }
        } else {
            Common.showToast(activity, R.string.textNoNetwork);
        }
        return dish;
    }

//    private Shop getShop (int shop_id){
//        Shop shop = null;
//        if (Common.networkConnected(activity)){
//            String url = Url.URL + "/ShopServlet";
//            Gson gson = new GsonBuilder().setDateFormat(DATE_FORMAT).create();
//            JsonObject jsonObject = new JsonObject();
//            jsonObject.addProperty("action", "getShopById");
//            jsonObject.addProperty("id", shop_id);
//            String jsonOut = jsonObject.toString();
//            getShopTask = new CommonTask(url, jsonOut);
//            try {
//                String jsonIn = getShopTask.execute().get();
//                shop = gson.fromJson(jsonIn, Shop.class);
//            } catch (Exception e){
//                Log.e(TAG, e.toString());
//            }
//        } else {
//            Common.showToast(activity, R.string.textNoNetwork);
//        }
//        return shop;
//    }

    private void ShowOrders(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            if (Common.networkConnected(activity)) {
            } else {
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



    private class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.MyViewHolder>{
        private Context context;
        private List<Order> orders;

        OrderAdapter(Context context, List<Order> orders){
            this.context = context;
            this.orders = orders;
        }

        void setOrders(List<Order> orders) {
            this.orders = orders;
        }


        private class MyViewHolder extends RecyclerView.ViewHolder{
            TextView tvShopName, tvType, tvTotal, tvState, tvTime;
            Button btAction;
            RecyclerView rvOrderDetail;

            MyViewHolder(@NonNull View itemView) {
                super(itemView);
                tvShopName = itemView.findViewById(R.id.tvShopName);
                tvState = itemView.findViewById(R.id.tvState);
                tvType = itemView.findViewById(R.id.tvType);
                tvTotal = itemView.findViewById(R.id.tvTotal);
                tvTime = itemView.findViewById(R.id.tvTime);
                btAction = itemView.findViewById(R.id.btAction);
                rvOrderDetail = itemView.findViewById(R.id.rvOrderDetail);
            }
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

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            final Order order = orders.get(position);
            Shop shop = order.getShop();
            List<OrderDetail> orderDetails = order.getOrderDetails();
            Log.d(TAG, "orderdetail1");
            Date order_time = order.getOrder_time();
            Date order_ideal = order.getOrder_ideal();
            Date order_delivery = order.getOrder_delivery();
            int order_ttprice = order.getOrder_ttprice();
            int order_state = order.getOrder_state();
            int order_type = order.getOrder_type();

            String order_state_text = "";
            String order_type_text = "";
            String order_ttprice_text = "";
            String order_time_text = "";

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());


            DecimalFormatSymbols symbols = new DecimalFormatSymbols();
            symbols.setDecimalSeparator(',');
            DecimalFormat decimalFormat = new DecimalFormat("$ ###,###,###,###", symbols);
            order_ttprice_text = decimalFormat.format(order_ttprice);


            switch (order_state){
                case UNCONFIRMED :{
                    order_state_text = "已付款，等待接單";
                    order_time_text = "下單時間 : " + simpleDateFormat.format(order_time);
                    holder.btAction.setText("取消訂單");
                    holder.btAction.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new AlertDialog.Builder(activity)
                                    .setTitle(R.string.alertDialogTitleCancelOrder)
                                    .setPositiveButton("我確定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
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
                                            } catch (Exception e){
                                                Log.e(TAG, e.toString());
                                            }
                                            if (count == 0){
                                                Common.showToast(getActivity(), R.string.cancelOrderFail);
                                            } else{
                                                Common.showToast(getActivity(), R.string.cancelOrderSuccess);
                                                orders.clear();
                                                orders = getOrders(mem_id, counter);
                                                setOrders(orders);
                                                rvOrder.getAdapter().notifyDataSetChanged();
                                            }
                                        }
                                    }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            }).setMessage(R.string.alertDialogMessageCancelOrder)
                                    .show();
                        }
                    });
                    break;
                }
                case MAKING :{
                    order_state_text = "製作中";
                    order_time_text = "下單時間 : " + simpleDateFormat.format(order_time);
                    holder.btAction.setText("取消訂單");
                    holder.btAction.setEnabled(false);
                    break;
                }
                case PICKUP :{
                    order_state_text = "製作完成，待取餐";
                    order_time_text = "下單時間 : " + simpleDateFormat.format(order_time);
                    holder.btAction.setText("顯示QR CODE");
                    break;
                }
                case DELIVERING :{
                    order_state_text = "運送中";
                    if (order.getOrder_ideal() != null){
                        order_time_text = "預計送達時間 : " + simpleDateFormat.format(order_ideal);
                    }
                    holder.btAction.setText("顯示QR CODE");
                    break;
                }
                case DONE :{
                    order_state_text = "已取餐";
                    order_time_text = "訂單完成時間 : " + simpleDateFormat.format(order_delivery);
                    holder.btAction.setText("重新下單");
                    break;
                }
                case CANCEL :{
                    order_state_text = "已取消訂單";
                    simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault() );
                    order_time_text = "下單時間 : " + simpleDateFormat.format(order_time);
                    holder.btAction.setText("檢舉訂單");

                    break;
                }
            }


            if (order_type == SELFPICK){
                order_type_text = "自取";
            } else if (order_type == DELIVERY){
                order_type_text = "外送";
            }

            holder.tvShopName.setText(shop.getName());
            holder.tvType.setText(order_type_text);
            holder.tvTotal.setText(order_ttprice_text);
            holder.tvTime.setText(order_time_text);
            holder.tvState.setText(order_state_text);

            holder.rvOrderDetail.setLayoutManager(new LinearLayoutManager(activity));
            holder.rvOrderDetail.setAdapter(new OrderDetailAdapter(activity, orderDetails));
            progressBar.setVisibility(GONE);
        }


    }

    private class OrderDetailAdapter extends RecyclerView.Adapter<OrderDetailAdapter.MyViewHolder>{
        private Context context;
        List<OrderDetail> orderDetails;

        OrderDetailAdapter(Context context, List<OrderDetail> orderDetails){
            this.context = context;
            this.orderDetails = orderDetails;
        }

        void setOrderDetails(List<OrderDetail> orderDetails){
            this.orderDetails = orderDetails;
        }

        private class MyViewHolder extends RecyclerView.ViewHolder{
            TextView tvDishName, tvDishInfo, tvDishPrice, tvDishCount;

            MyViewHolder(@NonNull View itemView) {
                super(itemView);
                tvDishName = itemView.findViewById(R.id.tvDishName);
                tvDishInfo = itemView.findViewById(R.id.tvDishInfo);
                tvDishPrice = itemView.findViewById(R.id.tvDishPrice);
                tvDishCount = itemView.findViewById(R.id.tvDishCount);
            }
        }


        @Override
        public int getItemCount() {
            return orderDetails.size();
        }

        @NonNull
        @Override
        public OrderDetailAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(context).inflate(R.layout.order_detail_item_view, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull OrderDetailAdapter.MyViewHolder holder, int position) {
            final OrderDetail orderDetail = orderDetails.get(position);
            Dish dish = orderDetail.getDish();
            int dishPrice = orderDetail.getOd_price()*orderDetail.getOd_count();

            DecimalFormatSymbols symbols = new DecimalFormatSymbols();
            symbols.setDecimalSeparator(',');
            DecimalFormat decimalFormat = new DecimalFormat("$ ###,###,###,###", symbols);

            String tvDishCountText = "x" + orderDetail.getOd_count();
            holder.tvDishCount.setText(tvDishCountText);
            holder.tvDishPrice.setText(decimalFormat.format(dishPrice));

            if (dish.getInfo() == null || dish.getInfo().isEmpty()){
                holder.tvDishInfo.setText("");
            } else {
                holder.tvDishInfo.setText(dish.getInfo());
            }

            holder.tvDishName.setText(dish.getName());


        }


    }
}

//TODO 在OrderItemView 裡頭btAction 應需求加入on click 功能
