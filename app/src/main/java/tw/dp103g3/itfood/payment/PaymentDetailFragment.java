package tw.dp103g3.itfood.payment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import tw.dp103g3.itfood.Common;
import tw.dp103g3.itfood.R;
import tw.dp103g3.itfood.Url;
import tw.dp103g3.itfood.task.CommonTask;


public class PaymentDetailFragment extends Fragment {
    @BindView(R.id.tvCardNum)
    TextView tvCardNum;
    @BindView(R.id.tvCardType)
    TextView tvCardType;
    @BindView(R.id.toolbarPaymentDetail)
    Toolbar toolbarDetail;
    private Activity activity;
    private Payment payment;
    private CommonTask deletePaymentTask;
    private String TAG = "TAG_PaymentDetailFragment";

    public PaymentDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        Bundle bundle = getArguments();
        payment = (Payment) bundle.getSerializable("payment");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_payment_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        String cardType;
        String cardNum = payment.getPay_cardnum();
        if (cardNum.startsWith("4")) {
            cardType = "VISA";
        } else if (cardNum.startsWith("5")) {
            cardType = "MASTERCARD";
        } else {
            cardType = "UNKNOWN";
        }
        tvCardType.setText(cardType);
        tvCardNum.setText(cardNum);
        toolbarDetail.setNavigationOnClickListener(v -> Navigation.findNavController(v).popBackStack());
        toolbarDetail.inflateMenu(R.menu.payment_detail_fragment_menu);
        toolbarDetail.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.delete) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
                alertDialogBuilder.setTitle("刪除付款資訊");
                alertDialogBuilder.setMessage("你確定要刪除這筆付款資訊？");
                alertDialogBuilder.setNegativeButton("取消", (dialog, which) -> {
                    dialog.cancel();
                });
                alertDialogBuilder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String url = Url.URL + "/PaymentServlet";
                        JsonObject jsonObject = new JsonObject();
                        Gson gson = Common.gson;
                        payment.setPay_state(0);
                        jsonObject.addProperty("action", "update");
                        jsonObject.addProperty("payment", gson.toJson(payment));
                        String jsonOut = jsonObject.toString();
                        int count = 0;
                        try {
                            deletePaymentTask = new CommonTask(url, jsonOut);
                            count = Integer.valueOf(deletePaymentTask.execute().get());
                        } catch (Exception e) {
                            Log.e(TAG, e.toString());
                        }

                        if (count != 0) {
                            Common.showToast(activity, "刪除成功");
                            Navigation.findNavController(view).popBackStack();
                        } else {
                            Common.showToast(activity, "刪除失敗，請稍後再試。");
                        }
                    }
                });
                AlertDialog alertDialog = alertDialogBuilder.create();
                Common.setDialogUi(alertDialog, activity);
                alertDialog.show();
            }
            return true;
        });


    }


}
