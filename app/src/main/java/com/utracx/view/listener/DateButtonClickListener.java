package com.utracx.view.listener;

import android.app.Activity;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.utracx.R;
import com.utracx.view.activity.AlertListActivity;
import com.utracx.view.activity.AlertVehiclesListActivity;
import com.utracx.view.activity.MapActivity;
import com.utracx.view.activity.ReportsActivity;
import com.utracx.view.activity.TripDetailsActivity;

import java.util.Calendar;
import java.util.TimeZone;

import static com.utracx.api.request.ApiUtils.cancelAllOpenStreetMapsRequest;
import static com.utracx.api.request.ApiUtils.cancelAllSOSRequest;
import static com.utracx.util.helper.DateTimeHelper.isSameDay;

public final class DateButtonClickListener implements View.OnClickListener,
        MaterialPickerOnPositiveButtonClickListener<Long> {
    private static final String TAG = "DateButtonClickListener";
    private final Activity activity;

    public DateButtonClickListener(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onClick(View mapButton) {
        Calendar clonedCalendar = null;
        if (activity instanceof TripDetailsActivity) {
            clonedCalendar = (Calendar) ((TripDetailsActivity) activity).getCurrentCalender().clone();
        } else if (activity instanceof MapActivity) {
            clonedCalendar = (Calendar) ((MapActivity) activity).getCurrentCalender().clone();
        } else if (activity instanceof ReportsActivity) {
            clonedCalendar = (Calendar) ((ReportsActivity) activity).getCurrentCalender().clone();
        } else {
            clonedCalendar = (Calendar) ((AlertListActivity) activity).getCurrentCalender().clone();
        }

        clonedCalendar.setTimeZone(TimeZone.getDefault());
        MaterialDatePicker<Long> builder = MaterialDatePicker.Builder
                .datePicker()
                .setCalendarConstraints(getCalendarConstraints())
                .setSelection(clonedCalendar.getTimeInMillis())
                .build();

        builder.addOnPositiveButtonClickListener(this);
        builder.addOnNegativeButtonClickListener(
                cancelButton -> Log.e(TAG, "onClick: Date picker Canceled")
        );
        builder.addOnCancelListener(
                cancelButton -> Log.e(TAG, "onClick: Date picker Canceled")
        );
        builder.setCancelable(true);
        builder.setShowsDialog(true);
        if (activity instanceof TripDetailsActivity) {
            builder.showNow(((TripDetailsActivity) activity).getSupportFragmentManager(), "DATE_PICKER");
        } else if (activity instanceof MapActivity) {
            builder.showNow(((MapActivity) activity).getSupportFragmentManager(), "DATE_PICKER");
        } else if (activity instanceof ReportsActivity) {
            builder.showNow(((ReportsActivity) activity).getSupportFragmentManager(), "DATE_PICKER");
        } else {
            builder.showNow(((AlertListActivity) activity).getSupportFragmentManager(), "DATE_PICKER");
        }
    }

    private CalendarConstraints getCalendarConstraints() {

        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();

        Calendar startCalender = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        startCalender.add(Calendar.DAY_OF_MONTH, -30);
        constraintsBuilder.setStart(startCalender.getTimeInMillis());

        Calendar endCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        constraintsBuilder.setEnd(endCalendar.getTimeInMillis());

        return constraintsBuilder.build();
    }

    private void showFutureAlertDialog() {
        new AlertDialog.Builder(activity, R.style.AlertDialog)
                .setTitle("Invalid Date")
                .setMessage("No vehicle data available for the selected date !")
                .setPositiveButton("OK", (null))
                .create()
                .show();
    }

    @Override
    public void onPositiveButtonClick(Long selection) {
        Calendar newCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        newCal.setTimeInMillis(selection);

        Calendar todayCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

        if (newCal.after(todayCalendar) && !isSameDay(newCal.getTime(), todayCalendar.getTime())) {
            showFutureAlertDialog();
            return;
        }
        cancelAllOpenStreetMapsRequest();
        cancelAllSOSRequest();
        if (activity instanceof TripDetailsActivity) {
            ((TripDetailsActivity) activity).setCurrentSelectedCalendar(newCal);
        } else if (activity instanceof MapActivity) {
            ((MapActivity) activity).setCurrentSelectedCalendar(false, newCal);
        } else if (activity instanceof ReportsActivity) {
            ((ReportsActivity) activity).setCurrentSelectedCalendar(newCal);
        }else {
            ((AlertListActivity) activity).setCurrentSelectedCalendar(newCal);
        }

    }
}
