package com.utracx.view.listener;

import android.os.Bundle;
import android.view.View;

import com.utracx.api.model.rest.vehicle_list.VehicleInfo;
import com.utracx.view.activity.MapActivity;
import com.utracx.view.activity.TripDetailsActivity;

import java.util.Calendar;
import java.util.Date;

import static com.utracx.api.request.ApiUtils.cancelAllOpenStreetMapsRequest;
import static com.utracx.api.request.ApiUtils.cancelAllSOSRequest;
import static com.utracx.util.ConstantVariables.KEY_DATE_SELECTED;
import static com.utracx.util.helper.DateTimeHelper.isSameDay;
import static com.utracx.util.helper.NavigationHelper.navigateToNewActivity;
import static com.utracx.view.activity.HomeActivity.BUNDLE_KEY_VEHICLE_DETAIL;

public final class MapButtonClickListener implements View.OnClickListener {
    private TripDetailsActivity tripDetailsActivity;

    public MapButtonClickListener(TripDetailsActivity tripDetailsActivity) {
        this.tripDetailsActivity = tripDetailsActivity;
    }

    @Override
    public void onClick(View mapButton) {
        cancelAllOpenStreetMapsRequest();
        cancelAllSOSRequest();

        Bundle bundle = new Bundle();

        VehicleInfo vehicleInfo = tripDetailsActivity.getCurrentVehicleInfo();
        if (vehicleInfo != null) {
            bundle.putParcelable(BUNDLE_KEY_VEHICLE_DETAIL, vehicleInfo);
        }

        Calendar calendar = tripDetailsActivity.getCurrentCalender();
        if (!isSameDay(new Date(), calendar.getTime())) {
            bundle.putLong(KEY_DATE_SELECTED, calendar.getTimeInMillis());
        }

        navigateToNewActivity(tripDetailsActivity, MapActivity.class, bundle);
    }
}
