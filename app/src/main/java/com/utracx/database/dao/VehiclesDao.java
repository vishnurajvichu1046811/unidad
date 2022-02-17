package com.utracx.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.utracx.api.model.rest.vehicle_list.VehicleInfo;

import java.util.List;

@Dao
public interface VehiclesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(List<VehicleInfo> newVehicleInfoList);

    @Query("SELECT * from vehicles_table ORDER BY vehicle_registration ASC")
    LiveData<List<VehicleInfo>> getAllLiveVehicles();

    @Query("SELECT * from vehicles_table ORDER BY alert_count DESC ")
    LiveData<List<VehicleInfo>> getAllLiveVehiclesSortbyAlertCount();

    @Query("SELECT * from vehicles_table " +
            "WHERE source_date IS NOT NULL" +
            " AND source_date>:startOfDay AND source_date<:endOfDay" +
            " AND serial_no IS NOT NULL" +
            " ORDER BY alert_count ASC")
    LiveData<List<VehicleInfo>> getAllLatestLiveVehicles(long startOfDay, long endOfDay);

    @Query("SELECT * from vehicles_table WHERE id=:id LIMIT 1")
    LiveData<VehicleInfo> getLiveVehicleByID(String id);

    @Query("UPDATE vehicles_table SET " +
            "ignition=:ignition," +
            "vehicle_mode=:vehicle_mode," +
            "latitude=:latitude," +
            "longitude=:longitude," +
            "gnss_fix=:gnss_fix," +
            "speed=:speed," +
            "source_date=:source_date," +
            "address=null" +
            " WHERE serial_no=:serialNumber")
    void updateLatestDataBySerialNumber(String serialNumber, String ignition,
                                        String vehicle_mode, double latitude,
                                        double longitude, int gnss_fix,
                                        double speed, long source_date);

    @Query("UPDATE vehicles_table SET " +
            "serial_no=:serial_no" +
            " WHERE id=:id")
    void updateSerialNo(String id, String serial_no);

    @Query("UPDATE vehicles_table SET address=:address" +
            " WHERE latitude=:latitude" +
            " AND longitude=:longitude" +
            " AND address IS NULL")
    void updateResolvedAddressByPoints(double latitude, double longitude, String address);

}
