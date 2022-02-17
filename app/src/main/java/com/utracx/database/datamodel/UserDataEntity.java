package com.utracx.database.datamodel;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_data")
public class UserDataEntity {

    @PrimaryKey
    @NonNull
    private String username;

    @NonNull
    private String password;

    @ColumnInfo(name = "user_id")
    @NonNull
    private String userID;

    @ColumnInfo(name = "firebase_instance_id")
    private String firebaseInstanceID;

    @ColumnInfo(name = "timestamp")
    private long lastUpdatedTime;

    @ColumnInfo(name = "sleep_count")
    private Integer sleepCount;

    @ColumnInfo(name = "halt_count")
    private Integer haltCount;

    @ColumnInfo(name = "moving_count")
    private Integer movingCount;

    @ColumnInfo(name = "inactive_count")
    private Integer inactiveCount;

    @ColumnInfo(name = "is_registered")
    private boolean isRegistered;

    public UserDataEntity(@NonNull String username, @NonNull String password, @NonNull String userID) {
        this.username = username;
        this.password = password;
        this.userID = userID;

        this.lastUpdatedTime = System.currentTimeMillis();
        this.movingCount = 0;
        this.sleepCount = 0;
        this.haltCount = 0;
        this.inactiveCount = 0;
        this.isRegistered = false;
    }

    @NonNull
    public String getUsername() {
        return username;
    }

    public void setUsername(@NonNull String username) {
        this.username = username;
    }

    @NonNull
    public String getPassword() {
        return password;
    }

    public void setPassword(@NonNull String password) {
        this.password = password;
    }

    @NonNull
    public String getUserID() {
        return userID;
    }

    public void setUserID(@NonNull String userID) {
        this.userID = userID;
    }

    public long getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    public void setLastUpdatedTime(long lastUpdatedTime) {
        this.lastUpdatedTime = lastUpdatedTime;
    }

    public Integer getSleepCount() {
        return sleepCount;
    }

    public void setSleepCount(Integer sleepCount) {
        this.sleepCount = sleepCount;
    }

    public Integer getHaltCount() {
        return haltCount;
    }

    public void setHaltCount(Integer haltCount) {
        this.haltCount = haltCount;
    }

    public Integer getMovingCount() {
        return movingCount;
    }

    public void setMovingCount(Integer movingCount) {
        this.movingCount = movingCount;
    }

    public Integer getInactiveCount() {
        return inactiveCount;
    }

    public void setInactiveCount(Integer inactiveCount) {
        this.inactiveCount = inactiveCount;
    }

    public String getFirebaseInstanceID() {
        return firebaseInstanceID;
    }

    public void setFirebaseInstanceID(String firebaseInstanceID) {
        this.firebaseInstanceID = firebaseInstanceID;
    }

    public boolean isRegistered() {
        return isRegistered;
    }

    public void setRegistered(boolean registered) {
        isRegistered = registered;
    }
}
