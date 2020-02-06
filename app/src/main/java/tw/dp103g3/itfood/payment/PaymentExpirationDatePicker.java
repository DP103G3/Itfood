package tw.dp103g3.itfood.payment;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.NumberPicker;

import java.util.Calendar;

import tw.dp103g3.itfood.R;
import tw.dp103g3.itfood.main.SharedViewModel;

public class PaymentExpirationDatePicker extends AlertDialog {
    private NumberPicker npYear, npMonth;
    private Button btCancel, btConfirm;
    private Context context;
    private SharedViewModel model;

    protected PaymentExpirationDatePicker(Context context, SharedViewModel model) {
        super(context);
        this.context = context;
        this.model = model;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment_expiration_date_picker);

        btCancel = findViewById(R.id.btCancel);
        btConfirm = findViewById(R.id.btConfirm);
        npMonth = findViewById(R.id.npMonth);
        npYear = findViewById(R.id.npYear);

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;

        npYear.setMinValue(year);
        npYear.setMaxValue(year + 20);
        npYear.setValue(year);
        npYear.setWrapSelectorWheel(false);

        npMonth.setMinValue(1);
        npMonth.setMaxValue(12);
        npMonth.setValue(month);
        npMonth.setWrapSelectorWheel(false);

        btCancel.setOnClickListener(v -> {
            cancel();
        });

        btConfirm.setOnClickListener(v -> {
            String yearInput = String.valueOf(npYear.getValue()).substring(2);
            String monthInput;
            if (npMonth.getValue() < 10) {
                monthInput = "0" + npMonth.getValue();
            } else {
                monthInput = String.valueOf(npMonth.getValue());
            }
            String date = monthInput + "/" + yearInput;
            model.setExpirationDate(date);
            dismiss();
        });


    }
}
