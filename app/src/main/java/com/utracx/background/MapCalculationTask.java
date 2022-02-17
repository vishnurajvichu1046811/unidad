package com.utracx.background;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.utracx.api.model.rest.device_data.DeviceData;
import com.utracx.api.model.rest.vehicle_list.LastUpdatedData;
import com.utracx.model.map.CustomMapPoint;
import com.utracx.model.map.InfoWindowViewModel;
import com.utracx.model.map.MapDataModel;
import com.utracx.view.activity.MapActivity;

import java.util.List;

import static com.utracx.util.ConstantVariables.THRESHOLD_TIME_FOR_P_AND_S_MARKER;
import static com.utracx.util.ConstantVariables.VEHICLE_MODE_IDLE;
import static com.utracx.util.ConstantVariables.VEHICLE_MODE_MOVING;
import static com.utracx.util.ConstantVariables.VEHICLE_MODE_SLEEP;

public final class MapCalculationTask implements Runnable {
    public static final String BUNDLE_KEY_MAP_ACTIVITY_VIEW_MODEL = "key_map_view_model";
    private static final String TAG = "MapCalculationTask";
    private final List<DeviceData> dataList;
    private final MapActivity activity;
    private final MapCalculationTaskCallback callback;
    private final double maxSpeed;

    private MapDataModel mapDataModel;

    public MapCalculationTask(@NonNull MapActivity activity, @NonNull List<DeviceData> dataList,
                              @NonNull MapCalculationTaskCallback callback, double maxSpeed) {
        this.activity = activity;
        this.dataList = dataList;
        this.maxSpeed = maxSpeed;
        this.callback = callback;
        this.mapDataModel = new MapDataModel();
    }

    @Override
    public void run() {
        try {
            if (dataList.size() > 0) {

                DeviceData currentDeviceData;
                DeviceData previousDeviceData = null;
                DeviceData firstStateChangeDeviceData = null;
                boolean isMarker;

                for (int i = 0; i < dataList.size(); i++) {

                    currentDeviceData = dataList.get(i);

                    if (currentDeviceData != null
                            && currentDeviceData.getD() != null
                            && currentDeviceData.getD().getVehicleMode() != null
                            && !currentDeviceData.getD().getVehicleMode().isEmpty()
                            && currentDeviceData.getD().getLatitude() != null && currentDeviceData.getD().getLongitude() != null
                            && currentDeviceData.getD().getLatitude() != 0 && currentDeviceData.getD().getLongitude() != 0
                            && currentDeviceData.getD().getSourceDate() != null && currentDeviceData.getD().getSourceDate() > 0
                            && currentDeviceData.getD().getGnssFix() != null && currentDeviceData.getD().getGnssFix() == 1
                            && checkIgnitionStatus(currentDeviceData.getD())
                    ) {

                        //Treat H (Halt) packets same as M
                        if (currentDeviceData.getD().getVehicleMode().equalsIgnoreCase(VEHICLE_MODE_IDLE)) {
                            currentDeviceData.getD().setVehicleMode(VEHICLE_MODE_MOVING);
                        }

                        // Consider M and S packets only don't list other packets
                        // For M packets consider only the packets with speed more than 0
                        if ((currentDeviceData.getD().getVehicleMode().equalsIgnoreCase(VEHICLE_MODE_MOVING) && currentDeviceData.getD().getSpeed() != null && currentDeviceData.getD().getSpeed() > 0)
                                || currentDeviceData.getD().getVehicleMode().equalsIgnoreCase(VEHICLE_MODE_SLEEP)) {

                            // If previous packet is sleep, skip all the next packets until state change
                            if (previousDeviceData == null || !(previousDeviceData.getD().getVehicleMode().equalsIgnoreCase(VEHICLE_MODE_SLEEP)
                                    && currentDeviceData.getD().getVehicleMode().equalsIgnoreCase(VEHICLE_MODE_SLEEP))) {

                                isMarker = isMarker(previousDeviceData, currentDeviceData);

                                mapDataModel.addNewMapPoint(
                                        new CustomMapPoint(
                                                new LatLng(currentDeviceData.getD().getLatitude(), currentDeviceData.getD().getLongitude()),
                                                getInfoWindowModel(currentDeviceData, isMarker || i == 0 || i == dataList.size() - 1), // should be marker or first or last point for showing info window
                                                isMarker,
                                                isMarker ? getMarkerType(firstStateChangeDeviceData, previousDeviceData, currentDeviceData) : null
                                        )
                                );
                            }

                            // First State change packet () will be assigned as state change packet -- First packet in a sub-series will be added here
                            if (firstStateChangeDeviceData == null || isFirstPacket(previousDeviceData, currentDeviceData)) {
                                firstStateChangeDeviceData = currentDeviceData;
                            }

                            // Current packet will be assigned as previous packet
                            previousDeviceData = currentDeviceData;
                        }
                    }
                }
            }
            Log.d(TAG, "cleanupData: complete");
        } catch (Exception e) {
            Log.e(TAG, "onVehicleDeviceDetailsListFetched: ", e);
        }

        sendDataToMapActivity();
    }

    private String getMarkerType(@Nullable DeviceData firstStateChangeDeviceData,
                                 @Nullable DeviceData previousDeviceData,
                                 @NonNull DeviceData currentDeviceData) {
        if (
            //Previous packet's vehicle-mode needs to be valid
                previousDeviceData != null && previousDeviceData.getD() != null
                        && previousDeviceData.getD().getVehicleMode() != null

                        //Current packet's vehicle-mode needs to be valid
                        && currentDeviceData.getD() != null && currentDeviceData.getD().getVehicleMode() != null

                        //Current and previous packet's vehicle-mode needs to be different
                        && !previousDeviceData.getD().getVehicleMode().equalsIgnoreCase(currentDeviceData.getD().getVehicleMode())

                        //Previous packet's vehicle-mode needs to be SLEEP or 'S'
                        && previousDeviceData.getD().getVehicleMode().equalsIgnoreCase(VEHICLE_MODE_SLEEP)
                        //Previous packet's vehicle-mode needs to be SLEEP or 'M'
                        && currentDeviceData.getD().getVehicleMode().equalsIgnoreCase(VEHICLE_MODE_MOVING)) {

            // First State change packet () When state changed from M -> S is captured
            // First State change packet () and current packet time difference is measured
            if (firstStateChangeDeviceData != null && firstStateChangeDeviceData.getD().getSourceDate() != null
                    && previousDeviceData.getD().getSourceDate() != null && previousDeviceData.getD().getSourceDate() > 0
                    && Math.abs(firstStateChangeDeviceData.getD().getSourceDate() - previousDeviceData.getD().getSourceDate()) > THRESHOLD_TIME_FOR_P_AND_S_MARKER) {
                return VEHICLE_MODE_IDLE;
            }
            return VEHICLE_MODE_SLEEP;
        }
        return null;
    }

    private InfoWindowViewModel getInfoWindowModel(@NonNull DeviceData currentDeviceData, boolean isMarker) {
            return new InfoWindowViewModel(
                    currentDeviceData.getD().getSourceDate(),
                    currentDeviceData.getD().getSpeed(),
                    currentDeviceData.getD().getAddress());
    }

    private boolean isMarker(@Nullable DeviceData previousDeviceData, @NonNull DeviceData currentDeviceData) {
        if (previousDeviceData != null && !previousDeviceData.getD().getVehicleMode().equalsIgnoreCase(currentDeviceData.getD().getVehicleMode())) {
            // state changed -- S-> M //or// M-> S
            // Marker to be added only for the S -> M state change. THe last Stopped packet.
            return previousDeviceData.getD().getVehicleMode().equalsIgnoreCase(VEHICLE_MODE_SLEEP)
                    && currentDeviceData.getD().getVehicleMode().equalsIgnoreCase(VEHICLE_MODE_MOVING);
        }
        return false;
    }

    private boolean isFirstPacket(@Nullable DeviceData previousDeviceData, @NonNull DeviceData currentDeviceData) {
        if (previousDeviceData != null && !previousDeviceData.getD().getVehicleMode().equalsIgnoreCase(currentDeviceData.getD().getVehicleMode())) {
            // state changed -- M-> S -- First packet
            return currentDeviceData.getD().getVehicleMode().equalsIgnoreCase(VEHICLE_MODE_SLEEP)
                    && previousDeviceData.getD().getVehicleMode().equalsIgnoreCase(VEHICLE_MODE_MOVING);
        }
        return false;
    }

    // To include a packet within the route,
    //      1. it should be ignition ON
    //      2. it should have max speed greater than 30 km/h for ignition OFF
    private boolean checkIgnitionStatus(@NonNull LastUpdatedData lastUpdatedData) {
        return lastUpdatedData.getIgnition() != null
                && (lastUpdatedData.getIgnition().toLowerCase().contains("on") || maxSpeed >= 30);
    }

    private void sendDataToMapActivity() {
        // If all the packets are invalid and removed, backup is to show the latest point as marker
        if (mapDataModel.getMapPoints().size() < 1 && dataList.size() > 0) {
            mapDataModel.addNewMapPoint(
                    new CustomMapPoint(
                            new LatLng(dataList.get(0).getD().getLatitude(), dataList.get(0).getD().getLongitude()),
                            getInfoWindowModel(dataList.get(0), true),
                            true,
                            "end"
                    )
            );
        }

        Bundle dataBundle = new Bundle();
        dataBundle.putParcelable(
                BUNDLE_KEY_MAP_ACTIVITY_VIEW_MODEL,
                mapDataModel
        );
        activity.runOnUiThread(
                () -> callback.onObtainDataFromServer(dataBundle)
        );
    }

    public interface MapCalculationTaskCallback {
        void onObtainDataFromServer(Bundle dataBundle);
    }
}
