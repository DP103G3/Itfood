package tw.dp103g3.itfood.order;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import tw.dp103g3.itfood.main.Common;
import tw.dp103g3.itfood.R;
import tw.dp103g3.itfood.main.Url;
import tw.dp103g3.itfood.shopping_cart.LoginDialogFragment;
import tw.dp103g3.itfood.task.CommonTask;

import static tw.dp103g3.itfood.main.Common.DATE_FORMAT;
import static tw.dp103g3.itfood.main.Common.PREFERENCES_MEMBER;
import static tw.dp103g3.itfood.main.Common.showLoginDialog;

public class OrderFragment extends Fragment implements LoginDialogFragment.LoginDialogContract {
    private final static String TAG = "TAG_OrderFragment";
    private FragmentActivity activity;
    private final static int NOT_LOGGED_IN = 0;
    private final static int NO_ITEM = 1;
    private final static int NORMAL = 2;
    private static int mem_id;
    private int status;
    private NavController navController;
    private static Set<Order> orders;
    private ViewPager2 viewPager2;
    private TabLayout tabLayout;
    private SharedPreferences pref;

    static void setOrders(Set<Order> orders) {
        OrderFragment.orders = orders;
    }

    static Set<Order> getOrders() {
        return orders;
    }

    static int getMem_id() {
        return mem_id;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        pref = activity.getSharedPreferences(PREFERENCES_MEMBER, Context.MODE_PRIVATE);
        mem_id = pref.getInt("mem_id", 0);
        orders = getOrders(mem_id);
        if (mem_id == 0) {
            status = NOT_LOGGED_IN;
        } else {
            if (orders == null || orders.isEmpty()) {
                status = NO_ITEM;
            } else {
                status = NORMAL;
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (status == NOT_LOGGED_IN) {
            return inflater.inflate(R.layout.fragment_order_not_logged_in, container, false);
        } else if (status == NO_ITEM) {
            return inflater.inflate(R.layout.fragment_order_no_item, container, false);
        } else if (status == NORMAL) {
            return inflater.inflate(R.layout.fragment_order, container, false);
        } else {
            return inflater.inflate(R.layout.fragment_order_not_logged_in, container, false);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Common.connectServer(activity, Common.getMemId(activity));
//        broadcastManager = LocalBroadcastManager.getInstance(activity);
//        registerOrderReceiver();
        navController = Navigation.findNavController(view);
        switch (status) {
            case NOT_LOGGED_IN: {
                Button btLogin = view.findViewById(R.id.btLogin);
                btLogin.setOnClickListener(v -> {
                    showLoginDialog(this);
                });
                break;
            }
            case NO_ITEM: {
                Button btBackToMain = view.findViewById(R.id.btBackToMain);
                btBackToMain.setOnClickListener(v ->
                        navController.popBackStack(R.id.mainFragment, false));
                break;
            }
            case NORMAL: {
                tabLayout = view.findViewById(R.id.tabLayOut);
                viewPager2 = view.findViewById(R.id.viewPager);
                if (viewPager2.getAdapter() == null) {
                    viewPager2.setAdapter(new ViewPagerAdapter(activity));
                }
                viewPager2.setOffscreenPageLimit(1);
                new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> {
                    switch (position) {
                        case 0: {
                            tab.setText(R.string.textUnfinished);
                            break;
                        }
                        case 1: {
                            tab.setText(R.string.textDone);
                            break;
                        }
                        case 2: {
                            tab.setText(R.string.textCanceled);
                            break;
                        }
                    }
                }).attach();
            }
        }
    }

    @Override
    public void sendLoginResult(boolean isSuccessful) {
        if (isSuccessful) {
            navController.popBackStack(R.id.mainFragment, false);
        }
    }

    @Override
    public void sendRegisterRequest() {
        navController.navigate(R.id.registerFragment);
    }

    public class ViewPagerAdapter extends FragmentStateAdapter {
        static final int TABS_ITEM_SIZE = 3;

        ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return new OrderTabFragment(position);
        }

        @Override
        public int getItemCount() {
            return TABS_ITEM_SIZE;
        }
    }

    private Set<Order> getOrders(int mem_id) {
        Set<Order> orders = new HashSet<>();
        if (Common.networkConnected(activity)) {
            String url = Url.URL + "/OrderServlet";
            Gson gson = new GsonBuilder().setDateFormat(DATE_FORMAT).create();
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "findByCase");
            jsonObject.addProperty("type", "member");
            jsonObject.addProperty("id", mem_id);
            String jsonOut = jsonObject.toString();
            CommonTask getOrderTask = new CommonTask(url, jsonOut);
            try {
                String jsonIn = getOrderTask.execute().get();
                Type listType = new TypeToken<Set<Order>>() {
                }.getType();
                orders = gson.fromJson(jsonIn, listType);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Common.showToast(activity, R.string.textNoNetwork);
        }
        return orders;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (viewPager2 != null) {
            viewPager2.setAdapter(null);
        }
    }
}


