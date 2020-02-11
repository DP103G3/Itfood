package tw.dp103g3.itfood.main;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.NumberPicker;

import java.util.Calendar;

import tw.dp103g3.itfood.R;

public class DateTimePicker extends FrameLayout {
    private NumberPicker dateSpinner;
    private NumberPicker hourSpinner;
    private NumberPicker minuteSpinner;
    private Calendar date, oDate;
    private int hour, minute, oHour, oMinute;
    private int dateMaxVal = 4;
    private String[] dateDisplayValues = new String[dateMaxVal + 1];
    private OnDateTimeChangedListener onDateTimeChangedListener;

    public DateTimePicker(Context context, Calendar date) {
        super(context);
        oDate = Calendar.getInstance();
        this.date = date;
        hour = date.get(Calendar.HOUR_OF_DAY);
        minute = date.get(Calendar.MINUTE);

        inflate(context, R.layout.date_time_piker, this);

        dateSpinner = this.findViewById(R.id.npDate);
        dateSpinner.setMinValue(0);
        dateSpinner.setMaxValue(dateMaxVal);
        initDate(dateMaxVal);
        dateSpinner.setWrapSelectorWheel(false);
        dateSpinner.setOnValueChangedListener(onDateChangedLintener);

        hourSpinner = this.findViewById(R.id.npHour);
        initHour();
        hourSpinner.setWrapSelectorWheel(false);
        hourSpinner.dispatchSetSelected(false);
        hourSpinner.setOnValueChangedListener(onHourChangedLintener);

        minuteSpinner = this.findViewById(R.id.npMinute);
        initMin();
        minuteSpinner.setWrapSelectorWheel(false);
        minuteSpinner.dispatchSetSelected(false);
        minuteSpinner.setOnValueChangedListener(onMinuteChangedLintener);
        oHour = hourSpinner.getValue();
        oMinute = minuteSpinner.getValue();
        onDateTimeChanged();
    }

    private NumberPicker.OnValueChangeListener onDateChangedLintener = (picker, oldVal, newVal) -> {
        oDate = Calendar.getInstance();
        oDate.add(Calendar.DAY_OF_MONTH, dateSpinner.getValue());
        initHour();
        initMin();
        onDateTimeChanged();
    };

    private NumberPicker.OnValueChangeListener onHourChangedLintener = (picker, oldVal, newVal) -> {
        oHour = hourSpinner.getValue();
        initMin();
        onDateTimeChanged();
    };

    private NumberPicker.OnValueChangeListener onMinuteChangedLintener = (picker, oldVal, newVal) -> {
        oMinute = minuteSpinner.getValue();
        onDateTimeChanged();
    };

    private void initHour() {
        int hourMinVal = dateSpinner.getValue() == 0 ? ((minute / 15 + 1) == 4 ? hour + 1 : hour) : 8;
        hourSpinner.setMaxValue(22);
        hourSpinner.setMinValue(hourMinVal);
        hourSpinner.setValue(hourMinVal);
        oHour = hourSpinner.getValue();
    }

    private void initMin() {
        minuteSpinner.setMaxValue(3);
        int minuteMinVal = (hourSpinner.getValue() == hour) && (dateSpinner.getValue() == 0) ? (minute / 15 + 1) : 0;
        minuteSpinner.setMinValue(minuteMinVal);
        Log.d("TAG", String.valueOf(minuteMinVal));
        minuteSpinner.setFormatter(value -> String.valueOf(value * 15));
        minuteSpinner.setValue(minuteMinVal);
        oMinute = minuteSpinner.getValue();
    }

    private void initDate(int dateMaxVal) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date.getTimeInMillis());
        cal.add(Calendar.DAY_OF_YEAR, -1);
        dateSpinner.setDisplayedValues(null);
        for (int i = 0; i <= dateMaxVal; i++) {
            cal.add(Calendar.DAY_OF_YEAR, 1);
            dateDisplayValues[i] = (String) DateFormat.format("MM.dd EEEE", cal);
        }
        dateSpinner.setDisplayedValues(dateDisplayValues);
        dateSpinner.setValue(0);
        dateSpinner.invalidate();
    }

    public interface OnDateTimeChangedListener {
        void onDateTimeChanged(DateTimePicker view, int year, int month, int day, int hour, int minute);
    }

    public void setOnDateTimeChangedListener(OnDateTimeChangedListener callBack) {
        onDateTimeChangedListener = callBack;
    }

    public void onDateTimeChanged() {
        if (onDateTimeChangedListener != null) {
            onDateTimeChangedListener.onDateTimeChanged(this, oDate.get(Calendar.YEAR),
                    oDate.get(Calendar.MONTH), oDate.get(Calendar.DAY_OF_MONTH), oHour, oMinute * 15);
        }
    }

}
