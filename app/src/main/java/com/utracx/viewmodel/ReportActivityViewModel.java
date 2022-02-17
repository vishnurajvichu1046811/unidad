package com.utracx.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.utracx.api.model.rest.device_data.DeviceData;
import com.utracx.api.model.rest.vehicle_list.VehicleInfo;
import com.utracx.database.AppDatabaseRepository;
import com.utracx.database.datamodel.UserDataEntity;

import java.util.List;

public class ReportActivityViewModel extends AndroidViewModel {

    private AppDatabaseRepository repository;

    public ReportActivityViewModel(@NonNull Application application) {
        super(application);
        repository = AppDatabaseRepository.getInstance(application.getApplicationContext());
    }

    public LiveData<VehicleInfo> getLiveVehicleInfo(@NonNull String vehicleID) {
        return repository.getLiveVehicleData(vehicleID);
    }

    public UserDataEntity fetUserDataByEmail(String username) {
        return repository.getUserDataByEmail(username);
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

    public void updateSerialNumber(String vehicleID, String serialNumber) {
        repository.updateSerialNumber(vehicleID, serialNumber);
    }

    public double getMaxSpeed(String serialNumber, long startOfDay, long endOfDay) {
        return repository.getMaxSpeed(serialNumber, startOfDay, endOfDay);
    }

    public List<Double> getAvgSpeed(String serialNumber, long startOfDay, long endOfDay) {
        return repository.getAvgSpeed(serialNumber, startOfDay, endOfDay);
    }

    public LiveData<List<DeviceData>> getLiveDeviceDataList(String serialNumber, long startOfDay, long endOfDay) {
        return repository.getAllLiveDeviceDetails(serialNumber, startOfDay, endOfDay);
    }
}
