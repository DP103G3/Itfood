package tw.dp103g3.itfood.main;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.format.DateUtils;

import java.util.Calendar;

public class DateTimePickerDialog extends AlertDialog {
    private DateTimePicker dateTimePicker;
    private Calendar date = Calendar.getInstance();
    private OnDateTimeSetListener onDateTimeSetListener;

    public DateTimePickerDialog(Context context, long date) {
        super(context);
        dateTimePicker = new DateTimePicker(context);
        setView(dateTimePicker);
        dateTimePicker.setOnDateTimeChangedListener((view, year, month, day, hour, minute) -> {
            this.date.set(Calendar.YEAR, year);
            this.date.set(Calendar.MONTH, month);
            this.date.set(Calendar.DAY_OF_MONTH, day);
            this.date.set(Calendar.HOUR_OF_DAY, hour);
            this.date.set(Calendar.MINUTE, minute);
            this.date.set(Calendar.SECOND, 0);
            updateTitle(this.date.getTimeInMillis());
        });
        setButton(BUTTON_POSITIVE, "設置", this::onClick);
        setButton(BUTTON_NEGATIVE, "取消", (OnClickListener) null);
        this.date.setTimeInMillis(date);
        updateTitle(this.date.getTimeInMillis());
    }

    public interface OnDateTimeSetListener {
        void onDateTimeSet(AlertDialog dialog, Long date);
    }

    private void updateTitle(long date) {
        int flag = DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_DATE |
                DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_TIME;
        setTitle(DateUtils.formatDateTime(this.getContext(), date, flag));
    }

    public void setOnDateTimeSetListener(OnDateTimeSetListener callBack) {
        onDateTimeSetListener = callBack;
    }

    public void onClick(DialogInterface arg0, int arg1) {
        if (onDateTimeSetListener != null) {
            onDateTimeSetListener.onDateTimeSet(this, date.getTimeInMillis());
        }
    }
}
