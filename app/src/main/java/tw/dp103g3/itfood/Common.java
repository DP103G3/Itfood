package tw.dp103g3.itfood;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

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
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import tw.dp103g3.itfood.order.OrderWebSocketClient;

public class Common {
    private static final String TAG = "TAG_Common";
    private static final double EARTH_RADIUS = 6378.137;
    public static final String PREFERENCES_MEMBER = "member";
    public static final String PREFERENCES_CART = "cart";
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final int LOGIN_FALSE = 0;
    public static final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
    public static OrderWebSocketClient orderWebSocketClient;

    public static void connectServer(Context context, int memId) {
        URI uri = null;
        try {
            uri = new URI(Url.SOCKET_URI + memId);
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
        return pref.getInt("mem_id", 0);
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
            orderDetails.forEach((v, u) -> Log.d(TAG, String.format("%d, %d", v, u)));
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
}
