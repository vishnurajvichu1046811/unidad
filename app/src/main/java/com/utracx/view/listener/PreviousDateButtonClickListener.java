package com.utracx.view.listener;

import android.app.Activity;
import android.view.View;

import com.utracx.view.activity.AlertListActivity;
import com.utracx.view.activity.TripDetailsActivity;

import java.util.Calendar;
import java.util.TimeZone;

import static com.utracx.api.request.ApiUtils.cancelAllOpenStreetMapsRequest;
import static com.utracx.api.request.ApiUtils.cancelAllSOSRequest;

public final class PreviousDateButtonClickListener implements View.OnClickListener {
    private final Activity activity;

    public PreviousDateButtonClickListener(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onClick(View mapButton) {
        Calendar calendar;
        if (activity instanceof TripDetailsActivity) {
            calendar = (Calendar) ((TripDetailsActivity) activity).getCurrentCalender().clone();
        } else {
            calendar = (Calendar) ((AlertListActivity) activity).getCurrentCalender().clone();
        }
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        if (calendar.before(Calendar.getInstance(TimeZone.getTimeZone("UTC")))) {
            cancelAllOpenStreetMapsRequest();
            cancelAllSOSRequest();
            if (activity instanceof TripDetailsActivity) {
                ((TripDetailsActivity) activity).setCurrentSelectedCalendar(calendar);
            } else {
                ((AlertListActivity) activity).setCurrentSelectedCalendar(calendar);
            }

        }
    }
}
