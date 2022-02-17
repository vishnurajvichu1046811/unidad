package com.utracx.view.listener;

import android.app.Activity;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import com.utracx.R;
import com.utracx.view.activity.AlertListActivity;
import com.utracx.view.activity.AlertVehiclesListActivity;
import com.utracx.view.activity.TripDetailsActivity;

import java.util.Calendar;
import java.util.TimeZone;

import static com.utracx.api.request.ApiUtils.cancelAllOpenStreetMapsRequest;
import static com.utracx.api.request.ApiUtils.cancelAllSOSRequest;
import static com.utracx.util.helper.DateTimeHelper.isSameDay;

public final class NextDateButtonClickListener implements View.OnClickListener {
    private final Activity activity;

    public NextDateButtonClickListener(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onClick(View mapButton) {
        Calendar newCal;
        if (activity instanceof  TripDetailsActivity) {
            newCal =  (Calendar) ((TripDetailsActivity) activity).getCurrentCalender().clone();
        } else {
            newCal =  (Calendar) ((AlertListActivity) activity).getCurrentCalender().clone();
        }

        newCal.add(Calendar.DAY_OF_MONTH, 1);

        Calendar todayCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

        if (newCal.after(todayCalendar) && !isSameDay(newCal.getTime(), todayCalendar.getTime())) {
            showFutureAlertDialog();
            return;
        }
        cancelAllOpenStreetMapsRequest();
        cancelAllSOSRequest();
        if (activity instanceof  TripDetailsActivity) {
            ((TripDetailsActivity) activity).setCurrentSelectedCalendar(newCal);
        }else {
            ((AlertListActivity) activity).setCurrentSelectedCalendar(newCal);
        }
    }

    private void showFutureAlertDialog() {
        new AlertDialog.Builder(activity, R.style.AlertDialog)
                .setTitle("Invalid Date")
                .setMessage("No vehicle data available for the selected date !")
                .setPositiveButton("OK", (null))
                .create()
                .show();
    }
}
