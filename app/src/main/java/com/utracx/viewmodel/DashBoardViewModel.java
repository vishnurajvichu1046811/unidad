package com.utracx.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.utracx.api.model.rest.vehicle_list.VehicleInfo;
import com.utracx.database.AppDatabaseRepository;
import com.utracx.database.datamodel.UserDataEntity;

import java.util.List;

public class DashBoardViewModel extends AndroidViewModel {

    private LiveData<UserDataEntity> dashboardData;
    private AppDatabaseRepository appDatabaseRepository;

    public DashBoardViewModel(@NonNull Application application) {
        super(application);
        appDatabaseRepository = AppDatabaseRepository.getInstance(application.getApplicationContext());
        dashboardData = appDatabaseRepository.getUserLiveData();
    }

    public void updateNewVehiclesList(List<VehicleInfo> newVehicleList) {
        appDatabaseRepository.updateNewVehiclesList(newVehicleList);
    }

    public LiveData<UserDataEntity> getDashboardData() {
        return dashboardData;
    }

    public UserDataEntity fetUserDataByEmail(String username) {
        return appDatabaseRepository.getUserDataByEmail(username);
    }

    public void deleteAllData() {
        appDatabaseRepository.deleteAllData();
    }

    public void insertUserData(UserDataEntity newLoginUser) {
        appDatabaseRepository.insertNewLoginData(newLoginUser);
    }

    public void updateCountData(String userEmail, int movingCount, int idleCount,
                                int sleepCount, int inactiveCount) {
        appDatabaseRepository.updateCountData(userEmail, movingCount, idleCount, sleepCount, inactiveCount);
    }

    public void updateFirebaseInstanceID(@NonNull String token) {
        appDatabaseRepository.updateFirebaseInstanceID(token);
    }
}
