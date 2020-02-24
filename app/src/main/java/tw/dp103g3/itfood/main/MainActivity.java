package tw.dp103g3.itfood.main;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import tw.dp103g3.itfood.R;
import tw.dp103g3.itfood.address.Address;
import tw.dp103g3.itfood.member.Member;
import tw.dp103g3.itfood.shop.Shop;
import tw.dp103g3.itfood.task.CommonTask;

import static tw.dp103g3.itfood.main.Common.DATE_FORMAT;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "TAG_MainActivity";
    private final static int REQ_CHECK_SETTINGS = 101;
    private final static int PER_ACCESS_LOCATION = 201;
    private BottomNavigationView bottomNavigationView;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private static Location lastLocation;
    private FusedLocationProviderClient fusedLocationClient;
    private View decorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        decorView = getWindow().getDecorView();
        setContentView(R.layout.activity_main);
        Activity activity = this;
        SharedPreferences memberPref = activity.getSharedPreferences(Common.PREFERENCES_MEMBER, Context.MODE_PRIVATE);
        int mem_id = memberPref.getInt("mem_id", 0);
        String mem_password = memberPref.getString("mem_password", null);
        if (mem_id != 0 & Common.networkConnected(activity)) {
            Member member = getMember(mem_id);
            if (mem_password == null || !mem_password.equals(member.getMemPassword())) {
                memberPref.edit().putInt("mem_id", 0).apply();
                memberPref.edit().remove("mem_password").apply();
            }
        } else {
            Common.showToast(this, R.string.textNoNetwork);
        }
        SharedViewModel model = new ViewModelProvider(this).get(SharedViewModel.class);

        bottomNavigationView = findViewById(R.id.bottomNavigation);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);
        File localAddress = new File(this.getFilesDir(), "localAddress");
        File orderDetail = new File(this.getFilesDir(), "orderDetail");
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
        if (!localAddress.exists()) {
            try (ObjectOutputStream out =
                         new ObjectOutputStream(new FileOutputStream(localAddress))) {
                out.writeObject(new Address(0, getString(R.string.textLocalPosition),
                        null, -1, -1));
            } catch (IOException e) {
                Log.e(TAG, e.toString());
            }
        }
        if (!orderDetail.exists()) {
            try (BufferedWriter out = new BufferedWriter(new FileWriter(orderDetail))) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("shop", gson.toJson(new Shop()));
                jsonObject.addProperty("orderDetails", gson.toJson(new HashMap<Integer, Integer>()));
                out.write(jsonObject.toString());
            } catch (IOException e) {
                Log.e(TAG, e.toString());
            }
        }
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000)
                .setSmallestDisplacement(500);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                lastLocation = locationResult.getLastLocation();
            }
        };
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

    }


    public void checkLocationSettings() {
        LocationSettingsRequest.Builder builder =
                new LocationSettingsRequest.Builder()
                        .addLocationRequest(locationRequest);
        Task<LocationSettingsResponse> task =
                LocationServices.getSettingsClient(this)
                        .checkLocationSettings(builder.build());
        task.addOnSuccessListener(this, locationSettingsResponse -> {
            if (ActivityCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED) {
                setLocation();
            }
        });
        task.addOnFailureListener(e -> {
            if (e instanceof ResolvableApiException) {
                Log.e(TAG, e.getMessage());
                ResolvableApiException resolvable = (ResolvableApiException) e;
                try {
                    resolvable.startResolutionForResult(this, REQ_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException ex) {
                    Log.e(TAG, ex.getMessage());
                }
            }
        });
    }

    private void setLocation() {
        if (fusedLocationClient == null) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationClient.getLastLocation().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    lastLocation = task.getResult();
                    Address localAddress = new Address(0, getString(R.string.textLocalPosition), null,
                            lastLocation == null ? -1 : lastLocation.getLatitude(),
                            lastLocation == null ? -1 : lastLocation.getLongitude());
                    SharedViewModel model = new ViewModelProvider(this).get(SharedViewModel.class);
                    model.selectAddress(localAddress);
                    File file = new File(this.getFilesDir(), "localAddress");
                    try (ObjectOutputStream out =
                                 new ObjectOutputStream(new FileOutputStream(file))) {
                        out.writeObject(localAddress);
                    } catch (IOException e) {
                        Log.e(TAG, e.toString());
                    }
                }
            });
            fusedLocationClient.requestLocationUpdates(
                    locationRequest, locationCallback, null);
        }
    }

    public static Location getLocation() {
        return lastLocation;
    }

    @Override
    protected void onStart() {
        super.onStart();
        askAccessLocationPermission();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    private void askAccessLocationPermission() {
        String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION
        };
        int result = ContextCompat.checkSelfPermission(this, permissions[0]);
        if (result == PackageManager.PERMISSION_DENIED) {
            requestPermissions(permissions, PER_ACCESS_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PER_ACCESS_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_DENIED) {
            Common.showToast(this, R.string.textLocationAccessNotGranted);
        }
    }

    private Member getMember(int mem_id) {
        Member member = new Member();
        String url = Url.URL + "/MemberServlet";
        JsonObject jsonObject = new JsonObject();
        Gson gson = new GsonBuilder().setDateFormat(DATE_FORMAT).create();
        jsonObject.addProperty("action", "findById");
        jsonObject.addProperty("mem_id", mem_id);
        String jsonOut = jsonObject.toString();
        if (Common.networkConnected(this)) {
            try {
                String jsonIn = new CommonTask(url, jsonOut).execute().get();
                member = gson.fromJson(jsonIn, Member.class);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Common.showToast(this, R.string.textNoNetwork);
        }
        return member;
    }
}
