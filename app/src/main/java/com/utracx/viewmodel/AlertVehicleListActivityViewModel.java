package com.utracx.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.utracx.api.model.rest.vehicle_list.VehicleInfo;
import com.utracx.database.AppDatabaseRepository;
import com.utracx.database.datamodel.UserDataEntity;

import java.util.List;

public class AlertVehicleListActivityViewModel extends AndroidViewModel {

    private AppDatabaseRepository repository;

    public AlertVehicleListActivityViewModel(@NonNull Application application) {
        super(application);
        repository = AppDatabaseRepository.getInstance(application.getApplicationContext());
    }

    public UserDataEntity fetchUserDataByEmail(String username) {
        return repository.getUserDataByEmail(username);
    }

    public LiveData<List<VehicleInfo>> getAllLatestLiveVehicles(long startOfDay, long endOfDay) {
        return repository.getAllLatestLiveVehicles(startOfDay, endOfDay);
    }

    public void updateNewVehiclesList(List<VehicleInfo> newVehiclesList) {
        repository.updateNewVehiclesList(newVehiclesList);
    }

    public LiveData<List<VehicleInfo>> getVehiclesLiveListSortByAlertCount() {
        return repository.getVehiclesLiveListSortbyAlertCount();
    }

}
