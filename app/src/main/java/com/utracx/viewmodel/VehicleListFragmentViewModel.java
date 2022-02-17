package com.utracx.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.utracx.api.model.rest.vehicle_list.VehicleInfo;
import com.utracx.database.AppDatabaseRepository;
import com.utracx.database.datamodel.UserDataEntity;

import java.util.List;

public class VehicleListFragmentViewModel extends AndroidViewModel {
    private AppDatabaseRepository repository;

    public VehicleListFragmentViewModel(@NonNull Application application) {
        super(application);
        repository = AppDatabaseRepository.getInstance(application.getApplicationContext());
    }

    public void updateNewVehiclesList(List<VehicleInfo> newVehiclesList) {
        repository.updateNewVehiclesList(newVehiclesList);
    }

    public LiveData<List<VehicleInfo>> getLiveVehiclesList() {
        return repository.getVehiclesLiveList();
    }

    public UserDataEntity fetchUserDataByEmail(String username) {
        return repository.getUserDataByEmail(username);
    }

    public void updateNewAddress(String resolvedAddress, double lat, double lng) {

        repository.updateResolvedAddress(
                lat,
                lng,
                resolvedAddress
        );
    }
}
