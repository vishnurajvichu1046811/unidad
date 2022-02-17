package com.utracx.background;

import static com.utracx.util.ConstantVariables.MAX_SPEED_THRESHOLD_FOR_INVALID_ROUTE;
import static com.utracx.util.ConstantVariables.TOTAL_DISTANCE_THRESHOLD_FOR_INVALID_ROUTE;
import static com.utracx.util.ConstantVariables.VEHICLE_MODE_IDLE;
import static com.utracx.util.ConstantVariables.VEHICLE_MODE_MOVING;
import static com.utracx.util.ConstantVariables.VEHICLE_MODE_SLEEP;

import android.util.Log;

import androidx.annotation.NonNull;

import com.utracx.api.model.rest.device_data.DeviceData;
import com.utracx.util.helper.DistanceHelper;
import com.utracx.view.activity.MapActivity;
import com.utracx.view.adapter.data.TripSummary;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DistanceCalculationTask implements Runnable {
    private static final String TAG = "DistanceCalcTask";

    private final MapActivity mapActivity;
    private final DistanceCalculationCallBack callback;
    private final List<DeviceData> newDeviceDataList;
    private double totalDistance = 0.0;

    public DistanceCalculationTask(@NonNull MapActivity mapActivity,
                                   @NonNull List<DeviceData> newDeviceDataList,
                                   @NonNull DistanceCalculationCallBack callback) {
        this.mapActivity = mapActivity;
        this.callback = callback;
        this.newDeviceDataList = newDeviceDataList;
    }

    @Override
    public synchronized void run() {
        cleanupData();
    }

    private synchronized void cleanupData() {
        try {
            if (newDeviceDataList.size() > 0) {

                int clonedIndex = 0;
                DeviceData currentDeviceData = null;
                DeviceData previousLastDeviceData = null;
                List<TripSummary> tripSummaryList = new ArrayList<>();
                List<List<DeviceData>> tripSummaryDevicesSubList = new ArrayList<>();

                List<DeviceData> tempList = new ArrayList<>();

                for (int i = 0; i < newDeviceDataList.size(); i++) {

                    currentDeviceData = newDeviceDataList.get(i);

                    if (currentDeviceData != null
                            && currentDeviceData.getD() != null
                            && currentDeviceData.getD().getVehicleMode() != null
                            && !currentDeviceData.getD().getVehicleMode().isEmpty()
                            && currentDeviceData.getD().getLatitude() != null && currentDeviceData.getD().getLongitude() != null
                            && currentDeviceData.getD().getLatitude() != 0 && currentDeviceData.getD().getLongitude() != 0
                            && currentDeviceData.getD().getSourceDate() != null && currentDeviceData.getD().getSourceDate() > 0
                            && currentDeviceData.getD().getGnssFix() != null && currentDeviceData.getD().getGnssFix() == 1) {

                        //Treat H (Halt) packets same as M
                        if (currentDeviceData.getD().getVehicleMode().equalsIgnoreCase(VEHICLE_MODE_IDLE)) {
                            currentDeviceData.getD().setVehicleMode(VEHICLE_MODE_MOVING);
                        }

                        // Consider M and S packets only don't list other packets
                        if (currentDeviceData.getD().getVehicleMode().equalsIgnoreCase(VEHICLE_MODE_MOVING)
                                || currentDeviceData.getD().getVehicleMode().equalsIgnoreCase(VEHICLE_MODE_SLEEP)) {

                            if (tripSummaryList.isEmpty()
                                    || !currentDeviceData.getD().getVehicleMode().equalsIgnoreCase(tripSummaryList.get(clonedIndex).getVehicleMode())) {

                                // Sublist for previous summary item
                                // Will not run for first packet
                                if (tempList.size() > 0) {
                                    // add the last packet of this list as the continuation packet for next start
                                    // only for moving packet
                                    if (currentDeviceData.getD().getVehicleMode().equalsIgnoreCase(VEHICLE_MODE_MOVING)) {
                                        previousLastDeviceData = tempList.get(tempList.size() - 1);
                                    }

                                    // add the current packet as the last packet for previous node
                                    // Only for moving
                                    if (tempList.get(tempList.size() - 1).getD().getVehicleMode().equalsIgnoreCase(VEHICLE_MODE_MOVING)) {
                                        tempList.add(currentDeviceData);
                                    }

                                    tripSummaryDevicesSubList.add(tempList);
                                    tempList = new ArrayList<>();
                                }

                                tripSummaryList.add(new TripSummary());
                                // Add first packet from last set
                                if (previousLastDeviceData != null) {
                                    tempList.add(previousLastDeviceData);
                                    previousLastDeviceData = null;
                                }
                                tempList.add(currentDeviceData);

                                clonedIndex = tripSummaryList.size() - 1;

                                tripSummaryList.get(clonedIndex).setAverageSpeed(0.0);
                                tripSummaryList.get(clonedIndex).setStartTime(0);
                                tripSummaryList.get(clonedIndex).setEndTime(0);
                                tripSummaryList.get(clonedIndex).setDuration(0);
                                tripSummaryList.get(clonedIndex).setDistance(0.0);
                                tripSummaryList.get(clonedIndex).setVehicleMode(currentDeviceData.getD().getVehicleMode());
                                tripSummaryList.get(clonedIndex).setLastUpdatedData(currentDeviceData.getD());
                                tripSummaryList.get(clonedIndex).setLatitude(currentDeviceData.getD().getLatitude());
                                tripSummaryList.get(clonedIndex).setLongitude(currentDeviceData.getD().getLongitude());
                            } else {
                                tempList.add(currentDeviceData);
                            }
                        }
                    }
                }

                //for last row
                if (currentDeviceData != null) {
                    tempList.add(currentDeviceData);
                }
                tripSummaryDevicesSubList.add(tempList);

                if (!tripSummaryList.isEmpty() && !tripSummaryDevicesSubList.isEmpty()
                        && tripSummaryList.size() == tripSummaryDevicesSubList.size()) {
                    int size = tripSummaryDevicesSubList.size();
                    for (int i = 0; i < size; i++) {
                        tripSummaryList.get(i).setDuration(
                                findTotalDuration(tripSummaryDevicesSubList.get(i))
                        );

                        tripSummaryList.get(i).setStartTime(
                                findStartTime(tripSummaryDevicesSubList.get(i))
                        );

                        tripSummaryList.get(i).setEndTime(
                                findEndTime(tripSummaryDevicesSubList.get(i))
                        );

                        if (tripSummaryList.get(i).getVehicleMode().equalsIgnoreCase(VEHICLE_MODE_MOVING)) {


                            tripSummaryList.get(i).setDistance(
                                    findTotalDistance(tripSummaryDevicesSubList.get(i))
                            );

                            tripSummaryList.get(i).setAverageSpeed(
                                    findAverageSpeedManually(
                                            tripSummaryList.get(i).getDistance(),
                                            tripSummaryList.get(i).getDuration(),
                                            tripSummaryDevicesSubList.get(i)
                                    )
                            );
                        } else {
                            tripSummaryList.get(i).setAddress(
                                    findAddress(tripSummaryDevicesSubList.get(i))
                            );
                        }
                    }
                    sendDataToMapActivity(getCorrectedRoute(tripSummaryList, tripSummaryDevicesSubList));
                } else {
                    Log.e(TAG, "cleanupData: List size not matching");
                }


            }
            Log.d(TAG, "cleanupData: complete");
        } catch (Exception e) {
            Log.e(TAG, "onVehicleDeviceDetailsListFetched: ", e);
        }
    }

    private List<TripSummary> getCorrectedRoute(@NotNull List<TripSummary> tripSummaryList,
                                                @NotNull List<List<DeviceData>> tripSummaryDevicesSubList) {
        double totalDistance = 0.0;
        double maxSpeed = 0.0;
        int j;

        for (int i = 0; i < tripSummaryList.size(); i++) {
            totalDistance += tripSummaryList.get(i).getDistance();
            for (j = 0; tripSummaryDevicesSubList.get(i) != null && j < tripSummaryDevicesSubList.get(i).size(); j++) {
                if (tripSummaryDevicesSubList.get(i).get(j) != null
                        && tripSummaryDevicesSubList.get(i).get(j).getD() != null
                        && tripSummaryDevicesSubList.get(i).get(j).getD().getSpeed() != null
                        && maxSpeed < tripSummaryDevicesSubList.get(i).get(j).getD().getSpeed()) {
                    maxSpeed = tripSummaryDevicesSubList.get(i).get(j).getD().getSpeed();
                }
            }
        }

        if ((totalDistance <= TOTAL_DISTANCE_THRESHOLD_FOR_INVALID_ROUTE
                && maxSpeed <= MAX_SPEED_THRESHOLD_FOR_INVALID_ROUTE)) {
            // Kappakalam or Maze drawing - avoidance. Create just one node, stopped node
            TripSummary tripSummary = new TripSummary();

            tripSummary.setVehicleMode(VEHICLE_MODE_SLEEP);
            tripSummary.setLastUpdatedData(newDeviceDataList.get(0).getD());
            tripSummary.setLatitude(newDeviceDataList.get(0).getD().getLatitude());
            tripSummary.setLongitude(newDeviceDataList.get(0).getD().getLongitude());

            tripSummary.setDuration(findTotalDuration(newDeviceDataList));
            tripSummary.setStartTime(findStartTime(newDeviceDataList));
            tripSummary.setEndTime(findEndTime(newDeviceDataList));
            tripSummary.setAverageSpeed(0.0);
            tripSummary.setDistance(0.0);
            tripSummary.setAddress(findAddress(newDeviceDataList));

            List<TripSummary> newSingleTripSummaryList = new ArrayList<>();
            newSingleTripSummaryList.add(tripSummary);

            return newSingleTripSummaryList;
        }

        // If packets look good proceed with entire logic
        return tripSummaryList;
    }

    @NotNull
    private synchronized String findAddress(@NonNull List<DeviceData> deviceDataList) {
        if (deviceDataList.isEmpty()) {
            return "";
        }

        //Search any of the packets has a resolved address
        int listSize = deviceDataList.size();
        String address;
        for (int i = 0; i < listSize; i++) {
            address = deviceDataList.get(i).getD().getAddress();
            if (address != null && !address.isEmpty()) {
                return address;
            }
        }
        return "";
    }

    private synchronized long findStartTime(@NonNull List<DeviceData> deviceDataList) {
        if (deviceDataList.isEmpty() || deviceDataList.get(0).getD() == null) {
            return 0;
        }
        return deviceDataList.get(0).getD().getSourceDate();
    }

    private synchronized long findEndTime(@NonNull List<DeviceData> deviceDataList) {
        if (deviceDataList.isEmpty() || deviceDataList.get(0).getD() == null) {
            return 0;
        }
        return deviceDataList.get(deviceDataList.size() - 1).getD().getSourceDate();
    }

    private synchronized double findTotalDistance(@NonNull List<DeviceData> deviceDataList) {
        if (deviceDataList.isEmpty() || deviceDataList.size() < 2) {
            return 0;
        }
        return DistanceHelper.getDistance(deviceDataList);
    }

    private synchronized double findAverageSpeed(@NonNull List<DeviceData> deviceDataList) {
        int listSize = deviceDataList.size();
        if (deviceDataList.isEmpty()) {
            return 0.0;
        }
        if (listSize < 2) {
            return deviceDataList.get(0).getD().getSpeed();
        }

        double totalAverageSpeed = 0.0;
        double currentDeviceSpeed;
        for (int i = 0; i < listSize; i++) {
            currentDeviceSpeed = deviceDataList.get(i).getD().getSpeed();
            //noinspection StatementWithEmptyBody
            if (currentDeviceSpeed <= 0.0) {
                // ignored
            } else if (totalAverageSpeed <= 0.0) {
                totalAverageSpeed = currentDeviceSpeed;
            } else {
                totalAverageSpeed = (totalAverageSpeed + currentDeviceSpeed) / 2;
            }
        }
        return totalAverageSpeed;
    }

    private synchronized double findAverageSpeedManually(double distance, long timeInMilliSeconds, @NonNull List<DeviceData> deviceDataList) {
        if (distance < 1 || timeInMilliSeconds < 1) {
            return findAverageSpeed(deviceDataList);
        }
        return (distance) / (timeInMilliSeconds / 3.6e+6);
    }

    private synchronized long findTotalDuration(@NonNull List<DeviceData> deviceDataList) {
        if (deviceDataList.isEmpty() || deviceDataList.size() < 2
                || deviceDataList.get(0).getD() == null
                || deviceDataList.get(deviceDataList.size() - 1).getD() == null
                || deviceDataList.get(0).getD().getSourceDate() == null
                || deviceDataList.get(deviceDataList.size() - 1).getD().getSourceDate() == null) {
            return 0;
        }

        return Math.abs(
                deviceDataList.get(0).getD().getSourceDate()
                        - deviceDataList.get(deviceDataList.size() - 1).getD().getSourceDate()
        );
    }

    private synchronized void sendDataToMapActivity(@NonNull final List<TripSummary> filteredDeviceDataList) {
        if (!Thread.currentThread().isInterrupted()) {
            if (!filteredDeviceDataList.isEmpty()) {
                for (int i = 0; i < filteredDeviceDataList.size(); i++) {
                    totalDistance += filteredDeviceDataList.get(i).getDistance();
                }
            }
            mapActivity.runOnUiThread(() -> callback.updateDistance(newDeviceDataList, totalDistance));
        }
    }

    public interface DistanceCalculationCallBack {
        void updateDistance(@NonNull List<DeviceData> newDeviceDataList, double distance);
    }
}
