package com.utracx.view.adapter;

import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.utracx.R;
import com.utracx.api.model.rest.vehicle_list.LastUpdatedData;
import com.utracx.background.AddressTask;
import com.utracx.util.helper.MapHelper;
import com.utracx.view.listener.TripDetailsCallback;
import com.utracx.viewmodel.MarkerInfoViewModel;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

public class MakerInfoWindow implements GoogleMap.InfoWindowAdapter, AddressTask.AddressCallback {
    private final View myContentsView;
    private TripDetailsCallback tripDetailsCallback;
    private MarkerInfoViewModel markerInfoViewModel;
    private TextView tvAddress;
    private ImageView locationIcon;
    private Marker marker;
    private final AppCompatActivity activity;
    private boolean isGeoCoder;

    public MakerInfoWindow(AppCompatActivity activity, boolean isGeoCoder) {
        this.activity = activity;
        initViewModel();
        this.isGeoCoder = isGeoCoder;
        myContentsView = activity.getLayoutInflater().inflate(R.layout.custom_marker_info, null);
        setupCallBack();
    }

    @Override
    public View getInfoWindow(@NotNull Marker marker) {
        boolean isLocationAvailable = Boolean.FALSE;
        if (this.marker == null || !this.marker.getId().equals(marker.getId())) {
            this.marker = marker;
        } else if (this.marker.getTag() != null) {
            isLocationAvailable = Boolean.TRUE;
        }

        TextView tvTime = ((TextView) myContentsView.findViewById(R.id.time));
        TextView tvSpeed = ((TextView) myContentsView.findViewById(R.id.speed));
        tvAddress = ((TextView) myContentsView.findViewById(R.id.address));
        locationIcon = (ImageView) myContentsView.findViewById(R.id.loc_icon);

        tvAddress.setMovementMethod(new ScrollingMovementMethod());
        if (marker.getSnippet() != null) {
            try {
                JSONObject jsonObject = new JSONObject(marker.getSnippet());

                if (jsonObject.has("time")) {
                    tvTime.setText(String.format("%s",
                            MapHelper.getEpochTime(jsonObject.getLong("time"),
                                    "hh:mm aa")));
                }

                if (jsonObject.has("address")) {
                    tvAddress.setText(jsonObject.getString("address"));
                } else if (isLocationAvailable && marker.getTag() instanceof String) {
                    tvAddress.setText(marker.getTag().toString());
                } else if (jsonObject.has("latitude")) {
                    tvAddress.setText(activity.getString(R.string.location_fetching));
                    setHourGlassStatus(Boolean.TRUE);
                    LastUpdatedData lastUpdatedData = new LastUpdatedData();
                    lastUpdatedData.setGnssFix(2);
                    lastUpdatedData.setLatitude(jsonObject.getDouble("latitude"));
                    lastUpdatedData.setLongitude(jsonObject.getDouble("longitude"));
                    new AddressTask(isGeoCoder, lastUpdatedData, activity.getBaseContext(), tripDetailsCallback, this).run();
                }

                if (jsonObject.has("speed")) {
                    tvSpeed.setText(String.format("%s Km/hr", jsonObject.getDouble("speed")));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return myContentsView;
    }

    @Override
    public View getInfoContents(@NotNull Marker marker) {
        return myContentsView;
    }

    private void initViewModel() {
        markerInfoViewModel = new ViewModelProvider(activity).get(MarkerInfoViewModel.class);
    }

    private void setupCallBack() {
        tripDetailsCallback = new TripDetailsCallback(activity, markerInfoViewModel);
    }

    @Override
    public void onLocationResolvedSuccess(String resolvedAddress, double lat, double lng) {
        if (activity != null) {
            activity.runOnUiThread(() -> {
                if (resolvedAddress != null) {
                    tvAddress.setText(resolvedAddress);
                    tvAddress.invalidate();
                }
                setHourGlassStatus(Boolean.FALSE);
                if (marker != null && marker.isInfoWindowShown()) {
                    marker.setTag(resolvedAddress);
                    marker.hideInfoWindow();
                    marker.showInfoWindow();
                }
            });
        }
    }

    @Override
    public void onLocationResolvedFailed(double lat, double lng) {
        if (activity != null) {
            activity.runOnUiThread(() -> {
                if (tvAddress != null) {
                    tvAddress.setText(activity.getString(R.string.location_not_available));
                    tvAddress.invalidate();
                }
                setHourGlassStatus(Boolean.FALSE);
                if (marker != null && marker.isInfoWindowShown()) {
                    marker.setTag(activity.getString(R.string.location_not_available));
                    marker.hideInfoWindow();
                    marker.showInfoWindow();
                }
            });
        }
    }

    private void setHourGlassStatus(boolean status) {
        if (status) {
            locationIcon.setVisibility(View.VISIBLE);
        } else {
            locationIcon.setVisibility(View.GONE);
        }
        myContentsView.invalidate();
    }
}
