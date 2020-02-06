package tw.dp103g3.itfood.order;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

import java.util.Comparator;
import java.util.Set;

import tw.dp103g3.itfood.Common;
import tw.dp103g3.itfood.R;
import tw.dp103g3.itfood.order.Order;
import tw.dp103g3.itfood.order.OrderFragment;
import tw.dp103g3.itfood.qrcode.Contents;
import tw.dp103g3.itfood.qrcode.QRCodeEncoder;


/**
 * A simple {@link Fragment} subclass.
 */
public class QRCodeFragment extends Fragment {
    private final static String TAG = "TAG_QRCodeFragment";
    private Activity activity;
    private ImageView ivQRCode;
    private Order order;
    private LocalBroadcastManager broadcastManager;
    private NavController navController;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_qrcode, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        broadcastManager = LocalBroadcastManager.getInstance(activity);
        registerOrderReceiver();
        navController = Navigation.findNavController(view);
        Bundle bundle = getArguments();
        if (bundle != null) {
            order = (Order) bundle.getSerializable("order");
        }
        ivQRCode = view.findViewById(R.id.ivQRCode);
        String qrCodeText = Common.gson.toJson(order);
        int dimension = getResources().getDisplayMetrics().widthPixels * 2 / 3;
        QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(qrCodeText, null,
                Contents.Type.TEXT, BarcodeFormat.QR_CODE.toString(), dimension);
        try {
            Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
            ivQRCode.setImageBitmap(bitmap);
        } catch (WriterException e) {
            Log.e(TAG, e.toString());
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
            navController.popBackStack();
            Log.d(TAG, message);
        }
    };

    @Override
    public void onStop() {
        super.onStop();
        broadcastManager.unregisterReceiver(orderReceiver);
    }
}
