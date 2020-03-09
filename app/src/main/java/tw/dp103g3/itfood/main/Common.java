package tw.dp103g3.itfood.main;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tw.dp103g3.itfood.R;
import tw.dp103g3.itfood.address.Address;
import tw.dp103g3.itfood.order.OrderWebSocketClient;
import tw.dp103g3.itfood.shopping_cart.LoginDialogFragment;
import tw.dp103g3.itfood.task.CommonTask;

public class Common {
    private static final String TAG = "TAG_Common";
    private static final double EARTH_RADIUS = 6378.137;
    public static final String PREFERENCES_MEMBER = "member";
    public static final String PREFERENCES_ADDRESS = "address";
    public static final String PREFERENCES_CART = "cart";
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final int LOGIN_FALSE = 0;
    public static final String REGEX_EMAIL = "^\\w+((-\\w+)|(.\\w+))*@[A-Za-z0-9]+((\\.|\\-)[A-Za-z0-9]+)*\\.[A-Za-z]+$";
    public static final String REGEX_PHONE = "^09[0-9]{8}$";
    public static final String REGEX_IDENTITY_ID = "^[A-Za-z]{1}[0-9]{9}$";
    public static final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
    public static OrderWebSocketClient orderWebSocketClient;

    public static void connectServer(Context context, int memId) {
        URI uri = null;
        try {
            uri = new URI(Url.ORDER_SOCKET_URI + memId);
        } catch (URISyntaxException e) {
            Log.e(TAG, e.toString());
        }
        if (orderWebSocketClient == null) {
            orderWebSocketClient = new OrderWebSocketClient(uri, context);
            orderWebSocketClient.connect();
        }
    }

    public static void disconnectServer() {
        if (orderWebSocketClient != null) {
            orderWebSocketClient.close();
            orderWebSocketClient = null;
        }
    }

    public static int getMemId(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_MEMBER, Context.MODE_PRIVATE);
        return pref.getInt("mem_id", LOGIN_FALSE);
    }

    public static int getSelectedAddressId(Context context) {
        if (getMemId(context) != 0) {
            SharedPreferences pref = context.getSharedPreferences(PREFERENCES_ADDRESS, Context.MODE_PRIVATE);
            return pref.getInt("address_id", LOGIN_FALSE);
        } else {
            return 0;
        }
    }

    public static boolean networkConnected(Activity activity) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        }
        Network network = connectivityManager.getActiveNetwork();
        NetworkCapabilities actNetWork =
                connectivityManager.getNetworkCapabilities(network);
        if (actNetWork != null) {
            return actNetWork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    actNetWork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR);
        } else {
            return false;
        }
    }

    public static List<Address> getAddresses(Activity activity, int mem_id) {
        List<Address> addresses = new ArrayList<>();
        if (Common.networkConnected(activity)) {
            String url = Url.URL + "/AddressServlet";
            JsonObject jsonObject = new JsonObject();
            Gson gson = new GsonBuilder().setDateFormat(DATE_FORMAT).create();
            jsonObject.addProperty("action", "getAllShow");
            jsonObject.addProperty("mem_id", mem_id);
            String jsonOut = jsonObject.toString();
            try {
                String jsonIn = new CommonTask(url, jsonOut).execute().get();
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

//    public static List<Address> getAddresses(Activity activity, int memId) {
//        List<Address> addresses = new ArrayList<>();
//        if (Common.networkConnected(activity)) {
//            Address localAddress = new Address(0, activity.getString(R.string.textLocalPosition),
//                    null, -1, -1);
//            String url = Url.URL + "/AddressServlet";
//            JsonObject jsonObject = new JsonObject();
//            jsonObject.addProperty("action", "getAllShow");
//            jsonObject.addProperty("mem_id", memId);
//            String jsonOut = jsonObject.toString();
//            CommonTask getAllAddressTask = new CommonTask(url, jsonOut);
//            try {
//                String jsonIn = getAllAddressTask.execute().get();
//                Type listType = new TypeToken<List<Address>>() {
//                }.getType();
//                addresses = gson.fromJson(jsonIn, listType);
//            } catch (Exception e) {
//                e.printStackTrace();
//            } finally {
//                getAllAddressTask.cancel(true);
//            }
//            addresses = addresses != null ? addresses : new ArrayList<>();
//            File file = new File(activity.getFilesDir(), "localAddress");
//            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
//                localAddress = (Address) in.readObject();
//            } catch (IOException | ClassNotFoundException e) {
//                Log.e(TAG, e.toString());
//            }
//            addresses.add(0, localAddress);
//        } else {
//            Common.showToast(activity, R.string.textNoNetwork);
//        }
//        return addresses;
//    }

    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void showToast(Context context, int messageResId) {
        Toast.makeText(context, messageResId, Toast.LENGTH_SHORT).show();
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier(
                "status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static int getNavigationBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier(
                "navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static double Distance(double latitude1, double longitude1, double latitude2, double longitude2) {
        double radLatitude1 = rad(latitude1);
        double radLatitude2 = rad(latitude2);
        double a = radLatitude1 - radLatitude2;
        double b = rad(longitude1) - rad(longitude2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                + Math.cos(radLatitude1) * Math.cos(radLatitude2) * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000d) / 10000d;
        s = s * 1000;
        return s;
    }

    public static void checkCart(Activity activity, ImageView ivCart) {
        Map<Integer, Integer> orderDetails = new HashMap<>();
        File orderDetail = new File(activity.getFilesDir(), "orderDetail");
        try (BufferedReader in = new BufferedReader(new FileReader(orderDetail))) {
            String inStr = in.readLine();
            Type type = new TypeToken<Map<Integer, Integer>>() {
            }.getType();
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
            JsonObject jsonObject = gson.fromJson(inStr, JsonObject.class);
            String odStr = jsonObject.get("orderDetails").getAsString();
            orderDetails = gson.fromJson(odStr, type);
//            orderDetails.forEach((v, u) -> Log.d(TAG, String.format("%d, %d", v, u)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (orderDetails.isEmpty()) {
            ivCart.setVisibility(View.GONE);
        } else {
            ivCart.setVisibility(View.VISIBLE);
        }
    }

    public static double rad(double d) {
        return d * Math.PI / 180;
    }

    public static String getDayOfWeek(Calendar cal) {
        String day = "";
        switch (cal.get(Calendar.DAY_OF_WEEK)) {
            case 1:
                day = "星期天";
                break;
            case 2:
                day = "星期一";
                break;
            case 3:
                day = "星期二";
                break;
            case 4:
                day = "星期三";
                break;
            case 5:
                day = "星期四";
                break;
            case 6:
                day = "星期五";
                break;
            case 7:
                day = "星期六";
                break;
        }
        return day;
    }

    public static String formatCardNum(String cardNum) {
        char[] chars = cardNum.toCharArray();
        return new StringBuilder().append(chars[0]).append(chars[1]).append(chars[2])
                .append(chars[3]).append(" - ").append("****").append(" - ").append("****").append(" - ")
                .append(chars[12]).append(chars[13]).append(chars[14])
                .append(chars[15]).toString();
    }

    public static void showLoginDialog(Fragment fragment) {
        LoginDialogFragment loginDialogFragment = new LoginDialogFragment();
        loginDialogFragment.setTargetFragment(fragment, 0);
        loginDialogFragment.show(fragment.getParentFragmentManager(), "LoginDialogFragment");
    }

    public static void setDialogUi(Dialog dialog, Activity activity) {
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        dialog.getWindow().getDecorView().setSystemUiVisibility(activity.getWindow().getDecorView().getSystemUiVisibility());
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
                WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
                wm.updateViewLayout(dialog.getWindow().getDecorView(), dialog.getWindow().getAttributes());
            }
        });
    }

    public static void hideBottomNav(Activity activity) {
        BottomNavigationView bottomNavigationView = activity.findViewById(R.id.bottomNavigation);
        if (bottomNavigationView.getVisibility() == View.VISIBLE) {
            Animator animator = AnimatorInflater.loadAnimator(activity, R.animator.anim_bottom_navigation_slide_down);
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
    }

    public static void showBottomNav(Activity activity) {
        BottomNavigationView bottomNavigationView = activity.findViewById(R.id.bottomNavigation);
        if (bottomNavigationView.getVisibility() == View.GONE) {
            Animator animator = AnimatorInflater.loadAnimator(activity, R.animator.anim_bottom_navigation_slide_up);
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
}
