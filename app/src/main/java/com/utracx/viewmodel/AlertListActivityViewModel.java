package com.utracx.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;

import com.utracx.api.model.rest.device_data.DeviceData;
import com.utracx.database.AppDatabaseRepository;
import com.utracx.database.datamodel.UserDataEntity;

import java.util.List;

public class AlertListActivityViewModel extends AndroidViewModel {

    private AppDatabaseRepository repository;

    public AlertListActivityViewModel(@NonNull Application application) {
        super(application);
        repository = AppDatabaseRepository.getInstance(application.getApplicationContext());
    }

    public UserDataEntity fetchUserDataByEmail(String username) {
        return repository.getUserDataByEmail(username);
    }

    public LiveData<List<DeviceData>> getLiveAlertsForDevice(long startOfDay, long endOfDay, String serialNumber) {
        PagedList.Config config = new PagedList.Config.Builder()
                .setPageSize(20)
                .setInitialLoadSizeHint(10)
                .setPrefetchDistance(20)
                .setEnablePlaceholders(false)
                .build();
        return repository.getLiveAlertsForDevice(startOfDay, endOfDay, serialNumber);
    }

    public LiveData<PagedList<DeviceData>> getLiveAlertsForDevicePagination(int totalCount,long startOfDay, long endOfDay, String serialNumber) {

        PagedList.Config config = new PagedList.Config.Builder()
                .setPageSize(20)
                .setInitialLoadSizeHint(totalCount)
                .setPrefetchDistance(20)
                .setEnablePlaceholders(false)
                .build();
        return repository.getLiveAlertsForDevicePagination(config,startOfDay, endOfDay, serialNumber);
    }

    public void updateAlerts(List<DeviceData> alertList) {
        repository.updateDeviceDataList(alertList);
    }

    public void updateNewAddress(String resolvedAddress, double lat, double lng) {

        repository.updateResolvedAddress(
                lat,
                lng,
                resolvedAddress
        );
    }
}
