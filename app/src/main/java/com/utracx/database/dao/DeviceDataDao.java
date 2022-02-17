package com.utracx.database.dao;

import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;
import androidx.paging.PagedList;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.utracx.api.model.rest.device_data.DeviceData;

import java.util.List;

@Dao
public interface DeviceDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(List<DeviceData> newDeviceDataList);

    @Query("SELECT * from device_updates" +
            " WHERE serial_no=:serialNo" +
            " AND source_date IS NOT NULL" +
            " AND packet_type NOT NULL" +
            " AND longitude IS NOT NULL" +
            " AND latitude IS NOT NULL" +
            " AND longitude>1 AND latitude>1" +
            " AND source_date>:startTime AND source_date<:endTime" +
            " ORDER BY source_date DESC")
    LiveData<List<DeviceData>> getAllLiveDeviceDetails(String serialNo, long startTime, long endTime);

    @Query("SELECT source_date from device_updates WHERE serial_no=:serialNumber" +
            " AND source_date IS NOT NULL AND source_date>:startOfDay AND source_date<:endOfDay" +
            " ORDER BY source_date DESC LIMIT 1")
    long getLatestDeviceDataUpdateTimestamp(String serialNumber, long startOfDay, long endOfDay);

    @Query("SELECT speed from device_updates WHERE serial_no=:serialNumber" +
            " AND source_date IS NOT NULL" +
            " AND source_date > :startOfDay AND  source_date < :endOfDay" +
            " ORDER BY speed DESC LIMIT 1")
    double getMaxSpeed(String serialNumber, long startOfDay, long endOfDay);

    @Query("SELECT speed from device_updates WHERE speed IS NOT NULL AND speed>0" +
            " AND vehicle_mode='M' AND serial_no=:serialNumber" +
            " AND source_date IS NOT NULL" +
            " AND source_date > :startOfDay AND source_date < :endOfDay")
    List<Double> getAvgSpeed(String serialNumber, long startOfDay, long endOfDay);

    @Query("UPDATE device_updates" +
            " SET address=:address" +
            " WHERE latitude=:latitude" +
            " AND longitude=:longitude")
    void updateResolvedAddressByPoints(double latitude, double longitude, String address);

    @Query("SELECT * from device_updates WHERE serial_no=:serialNumber" +
            " AND source_date IS NOT NULL" +
            " AND source_date>:startOfDay" +
            " AND source_date<:endOfDay" +
            " AND packet_type NOT NULL" +
            " AND UPPER(packet_type) NOT LIKE '%DUPLICATE%'" +
            " AND (UPPER(packet_type) LIKE '%OVERSPEED CRITICAL ALERT%' OR (UPPER(packet_type) LIKE '%EMERGENCY%' AND UPPER(packet_type) NOT LIKE '%OFF%')" +
            "OR UPPER(packet_type) LIKE '%HARSH BREAKING ALERT PACKET%'" +
            "OR UPPER(packet_type) LIKE '%HARSH ACCELERATION ALERT PACKET%'" +
            "OR UPPER(packet_type) LIKE '%RASH TURNING ALERT PACKET%'" +
            "OR UPPER(packet_type) LIKE '%GNSS BOX OPENED ALERT PACKET%'" +
            "OR UPPER(packet_type) LIKE '%GEOFENCE ENTRY ALERT PACKET%'" +
            "OR UPPER(packet_type) LIKE '%GEOFENCE EXIT ALERT PACKET%'" +
            "OR UPPER(packet_type) LIKE '%VEHICLE BATTERY RECONNECT/ CONNECT BACK TO MAIN BATTERY ALERT PACKET%'" +
            "OR UPPER(packet_type) LIKE '%INTERNAL BATTERY LOW ALERT PACKET%'" +
            "OR UPPER(packet_type) LIKE '%INTERNAL BATTERY REMOVED ALERT PACKET%'" +
            "OR UPPER(packet_type) LIKE '%WIRE CUT CRITICAL ALERT%'" +
            "OR UPPER(packet_type) LIKE '%MAIN POWER REMOVED CRITICAL ALERT%'" +
            "OR UPPER(packet_type) LIKE '%TILT CRITICAL ALERT PACKET%'" +
            "OR UPPER(packet_type) LIKE '%IMPACT CRITICAL ALERT PACKET%'" +
            "OR UPPER(packet_type) LIKE '%OVERSPEED + GEOFENCE ENTRY CRITICAL ALERT PACKET%'" +
            "OR UPPER(packet_type) LIKE '%OVERSPEED + GEOFENCE EXIT CRITICAL ALERT PACKET%')" +
            " ORDER BY source_date DESC")
    LiveData<List<DeviceData>> getLiveAlertsForDevice(long startOfDay, long endOfDay, String serialNumber);


    @Query("SELECT * from device_updates  WHERE serial_no=:serialNumber"  +
            " AND source_date IS NOT NULL" +
            " AND source_date>:startOfDay" +
            " AND source_date<:endOfDay" +
            " AND packet_type NOT NULL" +
            " AND UPPER(packet_type) NOT LIKE '%DUPLICATE%'" +
            " AND (UPPER(packet_type) LIKE '%OVERSPEED CRITICAL ALERT%' OR (UPPER(packet_type) LIKE '%EMERGENCY%' AND UPPER(packet_type) NOT LIKE '%OFF%')" +
            "OR UPPER(packet_type) LIKE '%HARSH BREAKING ALERT PACKET%'" +
            "OR UPPER(packet_type) LIKE '%HARSH ACCELERATION ALERT PACKET%'" +
            "OR UPPER(packet_type) LIKE '%RASH TURNING ALERT PACKET%'" +
            "OR UPPER(packet_type) LIKE '%GNSS BOX OPENED ALERT PACKET%'" +
            "OR UPPER(packet_type) LIKE '%GEOFENCE ENTRY ALERT PACKET%'" +
            "OR UPPER(packet_type) LIKE '%GEOFENCE EXIT ALERT PACKET%'" +
            "OR UPPER(packet_type) LIKE '%VEHICLE BATTERY RECONNECT/ CONNECT BACK TO MAIN BATTERY ALERT PACKET%'" +
            "OR UPPER(packet_type) LIKE '%INTERNAL BATTERY LOW ALERT PACKET%'" +
            "OR UPPER(packet_type) LIKE '%INTERNAL BATTERY REMOVED ALERT PACKET%'" +
            "OR UPPER(packet_type) LIKE '%WIRE CUT CRITICAL ALERT%'" +
            "OR UPPER(packet_type) LIKE '%MAIN POWER REMOVED CRITICAL ALERT%'" +
            "OR UPPER(packet_type) LIKE '%TILT CRITICAL ALERT PACKET%'" +
            "OR UPPER(packet_type) LIKE '%IMPACT CRITICAL ALERT PACKET%'" +
            "OR UPPER(packet_type) LIKE '%OVERSPEED + GEOFENCE ENTRY CRITICAL ALERT PACKET%'" +
            "OR UPPER(packet_type) LIKE '%OVERSPEED + GEOFENCE EXIT CRITICAL ALERT PACKET%')" +
            " ORDER BY source_date DESC")
    LiveData<List<DeviceData>> getLiveAlertsForDevicePagination(long startOfDay, long endOfDay, String serialNumber);

    //pagination
    @Query("SELECT * from device_updates WHERE serial_no=:serialNumber" +
            " AND source_date IS NOT NULL" +
            " AND source_date>:startOfDay" +
            " AND source_date<:endOfDay" +
            " AND packet_type NOT NULL" +
            " AND UPPER(packet_type) NOT LIKE '%DUPLICATE%'" +
            " AND (UPPER(packet_type) LIKE '%OVERSPEED CRITICAL ALERT%' OR (UPPER(packet_type) LIKE '%EMERGENCY%' AND UPPER(packet_type) NOT LIKE '%OFF%')" +
            "OR UPPER(packet_type) LIKE '%HARSH BREAKING ALERT PACKET%'" +
            "OR UPPER(packet_type) LIKE '%HARSH ACCELERATION ALERT PACKET%'" +
            "OR UPPER(packet_type) LIKE '%RASH TURNING ALERT PACKET%'" +
            "OR UPPER(packet_type) LIKE '%GNSS BOX OPENED ALERT PACKET%'" +
            "OR UPPER(packet_type) LIKE '%GEOFENCE ENTRY ALERT PACKET%'" +
            "OR UPPER(packet_type) LIKE '%GEOFENCE EXIT ALERT PACKET%'" +
            "OR UPPER(packet_type) LIKE '%VEHICLE BATTERY RECONNECT/ CONNECT BACK TO MAIN BATTERY ALERT PACKET%'" +
            "OR UPPER(packet_type) LIKE '%INTERNAL BATTERY LOW ALERT PACKET%'" +
            "OR UPPER(packet_type) LIKE '%INTERNAL BATTERY REMOVED ALERT PACKET%'" +
            "OR UPPER(packet_type) LIKE '%WIRE CUT CRITICAL ALERT%'" +
            "OR UPPER(packet_type) LIKE '%MAIN POWER REMOVED CRITICAL ALERT%'" +
            "OR UPPER(packet_type) LIKE '%TILT CRITICAL ALERT PACKET%'" +
            "OR UPPER(packet_type) LIKE '%IMPACT CRITICAL ALERT PACKET%'" +
            "OR UPPER(packet_type) LIKE '%OVERSPEED + GEOFENCE ENTRY CRITICAL ALERT PACKET%'" +
            "OR UPPER(packet_type) LIKE '%OVERSPEED + GEOFENCE EXIT CRITICAL ALERT PACKET%')" +
            " ORDER BY source_date DESC")
    DataSource.Factory<Integer, DeviceData> getLiveAlertsForDeviceWithPagination(long startOfDay, long endOfDay, String serialNumber);


}
