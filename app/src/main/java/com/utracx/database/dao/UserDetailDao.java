package com.utracx.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.utracx.database.datamodel.UserDataEntity;

@Dao
public interface UserDetailDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserDataEntity userDataEntity);

    @Query("SELECT * from user_data LIMIT 1")
    LiveData<UserDataEntity> getLoggedInUserLiveData();

    @Query("SELECT * from user_data LIMIT 1")
    UserDataEntity getLoggedInUserData();

    @Query("SELECT * from user_data WHERE username=:username")
    UserDataEntity getUserDataByEmail(String username);

    @Query("UPDATE user_data SET " +
            "moving_count=:movingCount," +
            "halt_count=:idleCount," +
            "sleep_count=:sleepCount," +
            "inactive_count=:inactiveCount " +
            "WHERE username=:userEmail")
    void updateUserCountData(String userEmail, int movingCount, int idleCount, int sleepCount,
                             int inactiveCount);

    @Query("UPDATE user_data SET firebase_instance_id=:token")
    void updateFirebaseInstanceID(String token);
}
