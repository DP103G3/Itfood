package tw.dp103g3.itfood.main;

import android.content.Context;
import android.text.format.DateFormat;
import android.widget.FrameLayout;
import android.widget.NumberPicker;

import java.util.Calendar;

import tw.dp103g3.itfood.R;

public class DateTimePicker extends FrameLayout {
    private NumberPicker dateSpinner;
    private NumberPicker hourSpinner;
    private NumberPicker minuteSpinner;
    private Calendar date;
    private int hour, minute;
    private String[] dateDisplayValues = new String[7];
    private OnDateTimeChangedListener onDateTimeChangedListener;

    public DateTimePicker(Context context) {
        super(context);
        date = Calendar.getInstance();
        hour = date.get(Calendar.HOUR_OF_DAY);
        minute = date.get(Calendar.MINUTE);

        inflate(context, R.layout.date_time_piker, this);

        dateSpinner = this.findViewById(R.id.npDate);
        dateSpinner.setMinValue(0);
        dateSpinner.setMaxValue(6);
        updateDateControl();
        dateSpinner.setOnValueChangedListener(onDateChangedLintener);

        hourSpinner = this.findViewById(R.id.npHour);
        hourSpinner.setMaxValue(22);
        hourSpinner.setMinValue(8);
        hourSpinner.setWrapSelectorWheel(false);
        hourSpinner.setValue(hour);
        hourSpinner.dispatchSetSelected(false);
        hourSpinner.setOnValueChangedListener(onHourChangedLintener);

        minuteSpinner = this.findViewById(R.id.npMinute);
        minuteSpinner.setMaxValue(3);
        minuteSpinner.setMinValue(0);
        minuteSpinner.setFormatter(value -> String.valueOf(value * 15));
//        minuteSpinner.setValue(minute / 15 + 1);
        minuteSpinner.dispatchSetSelected(false);
        minuteSpinner.setOnValueChangedListener(onMinuteChangedLintener);
    }

    private NumberPicker.OnValueChangeListener onDateChangedLintener = (picker, oldVal, newVal) -> {
        date.add(Calendar.DAY_OF_MONTH, newVal - oldVal);
        updateDateControl();
        onDateTimeChanged();
    };

    private NumberPicker.OnValueChangeListener onHourChangedLintener = (picker, oldVal, newVal) -> {
        hour = hourSpinner.getValue();
        onDateTimeChanged();
    };

    private NumberPicker.OnValueChangeListener onMinuteChangedLintener = (picker, oldVal, newVal) -> {
        minute = minuteSpinner.getValue() * 15;
        onDateTimeChanged();
    };

    private void updateDateControl() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date.getTimeInMillis());
        cal.add(Calendar.DAY_OF_YEAR, -7 / 2 - 1);
        dateSpinner.setDisplayedValues(null);
        for (int i = 0; i < 7; i++) {
            cal.add(Calendar.DAY_OF_YEAR, 1);
            dateDisplayValues[i] = (String) DateFormat.format("MM.dd EEEE", cal);
        }
        dateSpinner.setDisplayedValues(dateDisplayValues);
        dateSpinner.setValue(7 / 2);
        dateSpinner.invalidate();
    }

    public interface OnDateTimeChangedListener {
        void onDateTimeChanged(DateTimePicker view, int year, int month, int day, int hour, int minute);
    }

    public void setOnDateTimeChangedListener(OnDateTimeChangedListener callBack) {
        onDateTimeChangedListener = callBack;
    }

    private void onDateTimeChanged() {
        if (onDateTimeChangedListener != null) {
            onDateTimeChangedListener.onDateTimeChanged(this, date.get(Calendar.YEAR),
                    date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH), hour, minute);
        }
    }

}
