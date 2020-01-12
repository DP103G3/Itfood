package tw.dp103g3.itfood.order;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import tw.dp103g3.itfood.Common;
import tw.dp103g3.itfood.R;
import tw.dp103g3.itfood.Url;
import tw.dp103g3.itfood.member.Member;
import tw.dp103g3.itfood.task.CommonTask;

import static tw.dp103g3.itfood.Common.DATE_FORMAT;
import static tw.dp103g3.itfood.Common.PREFERENCES_MEMBER;

public class OrderFragment extends Fragment {
    private final static String TAG = "TAG_OrderFragment";
    private Activity activity;
    private Member member;
    private final static int NOT_LOGGED_IN = 0;
    private final static int NO_ITEM = 1;
    private final static int NORMAL = 2;
    private int mem_id, status;
    private NavController navController;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        activity = getActivity();
        SharedPreferences pref = activity.getSharedPreferences(PREFERENCES_MEMBER, Context.MODE_PRIVATE);
        mem_id = pref.getInt("mem_id", 0);

        if (mem_id == 0) {
            status = NOT_LOGGED_IN;
        } else {
            member = getMember(mem_id);
            if (getOrders(mem_id) == null || getOrders(mem_id).isEmpty()) {
                status = NO_ITEM;
            } else {
                status = NORMAL;
            }

        }

        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
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
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        switch (status) {
            case NOT_LOGGED_IN: {
                Button btLogin = view.findViewById(R.id.btLogin);
                btLogin.setOnClickListener(v -> {
                    navController.navigate(R.id.action_orderFragment_to_loginFragment);
                });
                break;
            }
            case NO_ITEM: {
                Button btBackToMain = view.findViewById(R.id.btBackToMain);
                btBackToMain.setOnClickListener(v -> {
                    navController.navigate(R.id.action_orderFragment_to_mainFragment);
                });
                break;
            }
            case NORMAL: {
                member = getMember(mem_id);
                TabLayout tabLayout = view.findViewById(R.id.tabLayOut);
                ViewPager2 viewPager2 = view.findViewById(R.id.viewPager);

                viewPager2.setAdapter(createCardAdapter());

                new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> {

                    switch (position) {
                        case 0: {
                            tab.setText(R.string.unconfirmed);
                            break;
                        }
                        case 1: {
                            tab.setText(R.string.making);
                            break;
                        }
                        case 2: {
                            tab.setText(R.string.unpickup);
                            break;
                        }
                        case 3: {
                            tab.setText(R.string.delivering);
                            break;
                        }
                        case 4: {
                            tab.setText(R.string.done);
                            break;
                        }
                        case 5: {
                            tab.setText(R.string.canceled);
                            break;
                        }
                    }
                }).attach();
                viewPager2.setOffscreenPageLimit(1);

            }
        }
    }

    private ViewPagerAdapter createCardAdapter() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(super.getActivity());
        return adapter;
    }


    private Member getMember(int mem_id) {
        Member member = null;
        if (Common.networkConnected(activity)) {
            String url = Url.URL + "/MemberServlet";
            JsonObject jsonObject = new JsonObject();
            Gson gson = new GsonBuilder().setDateFormat(Common.DATE_FORMAT).create();
            jsonObject.addProperty("action", "findById");
            jsonObject.addProperty("mem_id", mem_id);
            String jsonOut = jsonObject.toString();
            CommonTask getMemberTask = new CommonTask(url, jsonOut);
            try {
                String jsonIn = getMemberTask.execute().get();
                member = gson.fromJson(jsonIn, Member.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Common.showToast(activity, R.string.textNoNetwork);
        }
        return member;
    }

    public class ViewPagerAdapter extends FragmentStateAdapter {
        public static final int TABS_ITEM_SIZE = 6;

        public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return OrderTabFragment.newInstance(position);
        }

        @Override
        public int getItemCount() {
            return TABS_ITEM_SIZE;
        }
    }

    private List<Order> getOrders(int mem_id) {
        List<Order> orders = new ArrayList<>();
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
                Type listType = new TypeToken<List<Order>>() {
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

}


