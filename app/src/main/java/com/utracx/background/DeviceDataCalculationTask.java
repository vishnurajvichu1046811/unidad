package com.utracx.background;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import com.utracx.api.model.rest.device_data.DeviceData;
import com.utracx.util.helper.DistanceHelper;
import com.utracx.view.activity.ReportsActivity;
import com.utracx.view.activity.TripDetailsActivity;
import com.utracx.view.adapter.DeviceDataAdapter;
import com.utracx.view.adapter.data.TripSummary;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.utracx.util.ConstantVariables.MAX_SPEED_THRESHOLD_FOR_INVALID_ROUTE;
import static com.utracx.util.ConstantVariables.STATE_DATA_LOADED;
import static com.utracx.util.ConstantVariables.STATE_NO_DATA;
import static com.utracx.util.ConstantVariables.TOTAL_DISTANCE_THRESHOLD_FOR_INVALID_ROUTE;
import static com.utracx.util.ConstantVariables.VEHICLE_MODE_IDLE;
import static com.utracx.util.ConstantVariables.VEHICLE_MODE_MOVING;
import static com.utracx.util.ConstantVariables.VEHICLE_MODE_SLEEP;

public class DeviceDataCalculationTask implements Runnable {
    private static final String TAG = "DeviceDataCalcTask";
    private Activity activity;
    private DeviceDataAdapter tripDetailAdapter;
    private List<DeviceData> newDeviceDataList;
    private Double totalDistance = 0.0;

    public DeviceDataCalculationTask(Activity activity,
                                     List<DeviceData> newDeviceDataList,
                                     DeviceDataAdapter tripDetailAdapter) {
        this.activity = activity;
        this.tripDetailAdapter = tripDetailAdapter;
        this.newDeviceDataList = newDeviceDataList;
    }

    @Override
    public synchronized void run() {
        cleanupData();
    }

    private synchronized void cleanupData() {
        try {
            if (newDeviceDataList != null && newDeviceDataList.size() > 0) {
                totalDistance = 0.0;
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
                            double distance = findTotalDistance(tripSummaryDevicesSubList.get(i));
                            totalDistance += distance;
                            tripSummaryList.get(i).setDistance(
                                    distance
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

                        if (i == size - 1)
                            tripSummaryList.get(i).setInitialData(tripSummaryDevicesSubList.get(size - 1).get(tripSummaryDevicesSubList.get(size - 1).size() - 1).getD());
                    }

                    onTripSummaryDataReady(getCorrectedRoute(tripSummaryList, tripSummaryDevicesSubList));

                    try {
                        // Top most element contains the latest packet for the selected date
                        sendLatestDataToActivity(tripSummaryDevicesSubList.get(0).get(0));
                    } catch (Exception e) {
                        Log.e(TAG, "cleanupData: failed to send data to activity", e);
                    }
                } else {
                    Log.e(TAG, "cleanupData: List size not matching");
                }
            }
            Log.d(TAG, "cleanupData: complete");
        } catch (Exception e) {
            Log.e(TAG, "onVehicleDeviceDetailsListFetched: ", e);
        }
    }

    private void sendLatestDataToActivity(@NonNull DeviceData deviceData) {
        if (!Thread.currentThread().isInterrupted()) {
            activity.runOnUiThread(
                    () -> {
                        if (activity instanceof TripDetailsActivity)
                            ((TripDetailsActivity) activity).updateLatestData(deviceData, totalDistance);
                    }
            );
        }
    }

    private List<TripSummary> getCorrectedRoute(@NotNull List<TripSummary> tripSummaryList,
                                                @NotNull List<List<DeviceData>> tripSummaryDevicesSubList) {
        double maxSpeed = 0.0;
        int j;

        for (int i = 0; i < tripSummaryList.size(); i++) {
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

    /*
     *below method of calculating speed of vechile is actual implementation
     */
    private synchronized double findAverageSpeedManually(double distance, long timeInMilliSeconds, @NonNull List<DeviceData> deviceDataList) {
        if (distance < 1 || timeInMilliSeconds < 1) {
            return findAverageSpeed(deviceDataList);
        }
        return (distance) / (timeInMilliSeconds / 3.6e+6);
    }

    /*private synchronized double findAverageSpeedManually(double distance, long timeInMilliSeconds, @NonNull List<DeviceData> deviceDataList) {
        if (distance < 1 || timeInMilliSeconds < 1) {
            return findAverageSpeed(deviceDataList);
        }

        double speed = 0;
        for (int i = 0; i < deviceDataList.size(); i++) {
            speed = speed + deviceDataList.get(i).getD().getSpeed();
        }

        if (speed != 0) {
            speed = speed / deviceDataList.size();
        }
        return speed;
    }*/

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

    private synchronized void onTripSummaryDataReady(@NonNull final List<TripSummary> filteredDeviceDataList) {
        if (!Thread.currentThread().isInterrupted()) {
            activity.runOnUiThread(
                    () -> {
                        if (activity instanceof TripDetailsActivity) {
                            tripDetailAdapter.updateDeviceDataList(filteredDeviceDataList);
                            ((TripDetailsActivity) activity).setupPlaceHolderViews(filteredDeviceDataList.size() > 0 ? STATE_DATA_LOADED : STATE_NO_DATA);
                        } else {
                            ((ReportsActivity) activity).reportDataReady(filteredDeviceDataList, totalDistance);
                        }
                    }
            );
        }
    }
}
