package tw.dp103g3.itfood;


import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

import java.util.Comparator;

import tw.dp103g3.itfood.order.Order;
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
}
