package com.utracx.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.utracx.api.model.rest.device_data.DeviceData;
import com.utracx.api.model.rest.vehicle_list.LastUpdatedData;
import com.utracx.api.model.rest.vehicle_list.VehicleInfo;
import com.utracx.database.AppDatabaseRepository;
import com.utracx.database.datamodel.UserDataEntity;

import java.util.List;

public class MapActivityViewModel extends AndroidViewModel {

    private AppDatabaseRepository repository;

    public MapActivityViewModel(@NonNull Application application) {
        super(application);
        repository = AppDatabaseRepository.getInstance(application.getApplicationContext());
    }

    public UserDataEntity fetUserDataByEmail(String username) {
        return repository.getUserDataByEmail(username);
    }

    public LiveData<List<DeviceData>> getLiveDeviceDataList(String serialNumber, long startOfDay, long endOfDay) {
        return repository.getAllLiveDeviceDetails(serialNumber, startOfDay, endOfDay);
    }

    public long getLatestDeviceDataUpdateTimestamp(String serialNumber, long startOfDay, long endOfDay) {
        return repository.getLatestDeviceDataUpdateTimestamp(serialNumber, startOfDay, endOfDay);
    }

    public void updateNewAddress(String resolvedAddress, double lat, double lng) {

        repository.updateResolvedAddress(
                lat,
                lng,
                resolvedAddress
        );
    }

    public void updateDeviceDetails(List<DeviceData> deviceDetailList) {
        repository.updateDeviceDataList(deviceDetailList);
    }

    public void updateLastUpdatedData(String serialNumber, LastUpdatedData lastUpdatedData, long lastUpdatedTime) {
        repository.updateLastUpdatedData(serialNumber, lastUpdatedData, lastUpdatedTime);
    }

    public LiveData<VehicleInfo> getLiveVehicleInfo(String vehicleId) {
        return repository.getLiveVehicleData(vehicleId);
    }

    public double getMaxSpeed(String serialNumber, long startOfDay, long endOfDay) {
        return repository.getMaxSpeed(serialNumber, startOfDay, endOfDay);
    }
}
