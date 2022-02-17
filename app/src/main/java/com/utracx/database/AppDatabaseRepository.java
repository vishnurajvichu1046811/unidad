package com.utracx.database;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.utracx.api.model.rest.device_data.DeviceData;
import com.utracx.api.model.rest.vehicle_list.LastUpdatedData;
import com.utracx.api.model.rest.vehicle_list.VehicleInfo;
import com.utracx.database.datamodel.NominatimEntitiy;
import com.utracx.database.datamodel.UserDataEntity;

import java.util.List;

import static com.utracx.database.AppDatabase.databaseWriteExecutor;

public class AppDatabaseRepository {

    private static AppDatabaseRepository singleton;
    private final AppDatabase appDatabase;

    private AppDatabaseRepository(Context applicationContext) {
        this.appDatabase = AppDatabase.getDatabase(applicationContext);
    }

    public static synchronized AppDatabaseRepository getInstance(Context applicationContext) {
        if (singleton == null) {
            singleton = new AppDatabaseRepository(applicationContext);
        }
        return singleton;
    }

    public UserDataEntity getUserDataByEmail(String username) {
        return appDatabase.userDetailsDao().getUserDataByEmail(username);
    }

    public UserDataEntity getUserData() {
        return appDatabase.userDetailsDao().getLoggedInUserData();
    }

    public LiveData<UserDataEntity> getUserLiveData() {
        return appDatabase.userDetailsDao().getLoggedInUserLiveData();
    }

    public void deleteAllData() {
        databaseWriteExecutor.execute(
                () -> {
                    try {
                        appDatabase.clearAllTables();
                    } catch (Exception ignored) {
                        //ignored as crash might occur within the library
                    }
                }
        );
    }

    public void insertNewLoginData(UserDataEntity newLoginUser) {
        databaseWriteExecutor.execute(
                () -> {
                    try {
                        appDatabase.userDetailsDao().insert(newLoginUser);
                    } catch (Exception ignored) {
                        //ignored as crash might occur within the library
                    }
                }
        );
    }

    public void updateNewVehiclesList(List<VehicleInfo> newVehicleList) {
        databaseWriteExecutor.execute(
                () -> {
                    try {
                        appDatabase.vehiclesDao().insertOrUpdate(newVehicleList);
                    } catch (Exception ignored) {
                        Log.e("","");
                        //ignored as crash might occur within the library
                    }
                }
        );

    }

    public LiveData<VehicleInfo> getLiveVehicleData(@NonNull String vehicleID) {
        return appDatabase.vehiclesDao().getLiveVehicleByID(vehicleID);
    }

    public LiveData<List<VehicleInfo>> getVehiclesLiveList() {
        return appDatabase.vehiclesDao().getAllLiveVehicles();
    }

    public LiveData<List<VehicleInfo>> getVehiclesLiveListSortbyAlertCount() {
        return appDatabase.vehiclesDao().getAllLiveVehiclesSortbyAlertCount();
    }

    public LiveData<List<VehicleInfo>> getAllLatestLiveVehicles(long startOfDay, long endOfDay) {
        return appDatabase.vehiclesDao().getAllLatestLiveVehicles(startOfDay, endOfDay);
    }

    public void updateResolvedAddress(double latitude, double longitude, String resolvedAddress) {
        databaseWriteExecutor.execute(
                () -> {
                    try {
                        appDatabase.vehiclesDao().updateResolvedAddressByPoints(latitude, longitude, resolvedAddress);
                        appDatabase.deviceDataDao().updateResolvedAddressByPoints(latitude, longitude, resolvedAddress);
                        appDatabase.nominatimDao().insert(
                                new NominatimEntitiy(
                                        latitude,
                                        longitude,
                                        resolvedAddress
                                )
                        );
                    } catch (Exception ignored) {
                        //ignored as crash might occur within the library
                    }
                }
        );
    }

    public void updateDeviceDataList(List<DeviceData> deviceDetailList) {
        databaseWriteExecutor.execute(
                () -> {
                    try {
                        appDatabase.deviceDataDao().insertOrUpdate(deviceDetailList);
                    } catch (Exception ignored) {
                        //ignored as crash might occur within the library
                    }
                }
        );
    }

    public void updateSerialNumber(String vehicleID, String serialNumber) {
        databaseWriteExecutor.execute(
                () -> {
                    try {
                        appDatabase.vehiclesDao().updateSerialNo(vehicleID, serialNumber);
                    } catch (Exception ignored) {
                        //ignored as crash might occur within the library
                    }
                }
        );
    }

    public double getMaxSpeed(String serialNumber, long startOfDay, long endOfDay) {
        return appDatabase.deviceDataDao().getMaxSpeed(serialNumber, startOfDay, endOfDay);
    }

    public List<Double> getAvgSpeed(String serialNumber, long startOfDay, long endOfDay) {
        return appDatabase.deviceDataDao().getAvgSpeed(serialNumber, startOfDay, endOfDay);
    }

    public void updateLastUpdatedData(String serialNumber, LastUpdatedData lastUpdatedData, long lastUpdatedTime) {
        databaseWriteExecutor.execute(
                () -> {
                    try {
                        appDatabase.vehiclesDao().updateLatestDataBySerialNumber(
                                serialNumber,
                                lastUpdatedData.getIgnition(),
                                lastUpdatedData.getVehicleMode(),
                                lastUpdatedData.getLatitude(),
                                lastUpdatedData.getLongitude(),
                                lastUpdatedData.getGnssFix(),
                                lastUpdatedData.getSpeed(),
                                lastUpdatedTime
                        );
                    } catch (Exception ignored) {
                        //ignored as crash might occur within the library
                    }
                }
        );
    }

    public LiveData<List<DeviceData>> getAllLiveDeviceDetails(String serialNumber, long startOfDay, long endOfDay) {
        return appDatabase.deviceDataDao().getAllLiveDeviceDetails(serialNumber, startOfDay, endOfDay);
    }

    public long getLatestDeviceDataUpdateTimestamp(String serialNumber, long startOfDay, long endOfDay) {
        return appDatabase.deviceDataDao().getLatestDeviceDataUpdateTimestamp(serialNumber, startOfDay, endOfDay);
    }

    public void updateCountData(String userEmail, int movingCount, int idleCount, int sleepCount, int inactiveCount) {
        databaseWriteExecutor.execute(
                () -> {
                    try {
                        appDatabase.userDetailsDao().updateUserCountData(
                                userEmail,
                                movingCount,
                                idleCount,
                                sleepCount,
                                inactiveCount
                        );
                    } catch (Exception ignored) {
                        //ignored as crash might occur within the library
                    }
                }
        );
    }

    public LiveData<List<DeviceData>> getLiveAlertsForDevice(long startOfDay, long endOfDay, String serialNumber) {
        return appDatabase.deviceDataDao().getLiveAlertsForDevice(startOfDay, endOfDay, serialNumber);
    }

//pagination
    public LiveData<PagedList<DeviceData>> getLiveAlertsForDevicePagination(PagedList.Config config, long startOfDay, long endOfDay, String serialNumber){
        DataSource.Factory<Integer, DeviceData>factory = appDatabase.deviceDataDao().getLiveAlertsForDeviceWithPagination(startOfDay, endOfDay, serialNumber);
        return new LivePagedListBuilder(factory, config)
                .build();
    }

    public void updateFirebaseInstanceID(@NonNull String token) {
        databaseWriteExecutor.execute(
                () -> {
                    try {
                        appDatabase.userDetailsDao().updateFirebaseInstanceID(token);
                    } catch (Exception ignored) {
                        //ignored as crash might occur within the library
                    }
                }
        );
    }
}
