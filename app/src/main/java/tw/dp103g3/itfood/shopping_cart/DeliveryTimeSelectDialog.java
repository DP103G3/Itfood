package tw.dp103g3.itfood.shopping_cart;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.NonNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import tw.dp103g3.itfood.R;
import tw.dp103g3.itfood.main.SharedViewModel;

import static tw.dp103g3.itfood.Common.DATE_FORMAT;
import static tw.dp103g3.itfood.Common.getDayOfWeek;

public class DeliveryTimeSelectDialog extends AlertDialog {
    private Spinner spTime;
    private int year;
    private int month;
    private int day;
    private Context context;
    private SharedViewModel model;


    public DeliveryTimeSelectDialog(@NonNull Context context, SharedViewModel model) {
        super(context);
        this.context = context;
        this.model = model;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.delivery_time_select_dialog);
        Button btnCancel = findViewById(R.id.btnCancel);
        Button btnConfirm = findViewById(R.id.btnConfirm);

        Calendar cal = Calendar.getInstance();

        year = cal.get(Calendar.YEAR);
        month = cal.get(Calendar.MONTH) + 1;
        day = cal.get(Calendar.DAY_OF_MONTH);
        int hour1 = cal.get(Calendar.HOUR_OF_DAY);
        int minute1 = cal.get(Calendar.MINUTE);

        //將一天的時間切為十五分鐘一個單位
        int timeUnit = (hour1 * 4) + (minute1 / 15) + (((minute1 % 15) != 0) ? 1 : 0);

        Spinner spDate = findViewById(R.id.spDate);
        spTime = findViewById(R.id.spTime);

        List<String> dates = new ArrayList<>();
        List<String> times = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            cal = Calendar.getInstance();
            if (i == 0) {
                dates.add(String.format(Locale.getDefault(), "今天, %d月 %d日", cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH)));
            } else if (i == 1) {
                cal.add(Calendar.DAY_OF_YEAR, 1);
                dates.add(String.format(Locale.getDefault(), "明天, %s", (cal.get(Calendar.MONTH) + 1) + "月 " + (cal.get(Calendar.DAY_OF_MONTH)) + "日"));
            } else {
                cal.add(Calendar.DAY_OF_YEAR, 2);
                dates.add(String.format(Locale.getDefault(), "%s", getDayOfWeek(cal) + ", " + (cal.get(Calendar.MONTH) + 1) + "月 " + (cal.get(Calendar.DAY_OF_MONTH) + "日")));
            }
        }

        ArrayAdapter<String> datesAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, dates);
        datesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spDate.setAdapter(datesAdapter);
        spDate.setSelection(0, true);

        for (int x = timeUnit + 2; x <= 88; x++) {
            String hour = (x / 4) < 10 ? "0" + (x / 4) : String.valueOf(x / 4);
            String minute = ((x % 4 * 15 == 0 ? "00" : String.valueOf(x % 4 * 15)));
            times.add(String.format(Locale.getDefault(), "%s:%s", hour, minute));
        }
        ArrayAdapter<String> timesAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, times);
        timesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTime.setAdapter(timesAdapter);
        spTime.setSelection(0, true);

        Spinner.OnItemSelectedListener dateListener = new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Calendar cal = Calendar.getInstance();
                if (position == 0) {
                    times.clear();
                    for (int x = timeUnit + 2; x <= 88; x++) {
                        String hour = (x / 4) < 10 ? "0" + (x / 4) : String.valueOf(x / 4);
                        String minute = ((x % 4 * 15 == 0 ? "00" : String.valueOf(x % 4 * 15)));
                        times.add(String.format(Locale.getDefault(), "%s:%s", hour, minute));
                    }
                    ArrayAdapter<String> timesAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, times);
                    timesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spTime.setAdapter(timesAdapter);
                    spTime.setSelection(0, true);
                    month = cal.get(Calendar.MONTH + 1);
                    day = cal.get(Calendar.DAY_OF_MONTH);
                } else {
                    times.clear();
                    for (int x = 32; x <= 88; x++) {
                        String hour = (x / 4) < 10 ? "0" + (x / 4) : String.valueOf(x / 4);
                        String minute = ((x % 4 * 15 == 0 ? "00" : String.valueOf(x % 4 * 15)));
                        times.add(String.format(Locale.getDefault(), "%s:%s", hour, minute));
                    }
                    ArrayAdapter<String> timesAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, times);
                    timesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spTime.setAdapter(timesAdapter);
                    spTime.setSelection(0, true);

                    if (position == 1) {
                        cal.add(Calendar.DAY_OF_YEAR, 1);
                        month = cal.get(Calendar.MONTH) + 1;
                        day = cal.get(Calendar.DAY_OF_MONTH);
                    } else if (position == 2) {
                        cal.add(Calendar.DAY_OF_YEAR, 2);
                        month = cal.get(Calendar.MONTH) + 1;
                        day = cal.get(Calendar.DAY_OF_MONTH);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };

        spDate.setOnItemSelectedListener(dateListener);

        btnConfirm.setOnClickListener(v -> {
            SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
            String dateString = String.format(Locale.getDefault(), "%d-%d-%d %s:00", year, month, day, spTime.getSelectedItem());
            try {
                Date date = dateFormat.parse(dateString);
                model.selectDeliveryTime(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            dismiss();
        });

        btnCancel.setOnClickListener(v -> cancel());

    }


}
