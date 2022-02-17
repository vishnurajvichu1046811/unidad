package com.utracx.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.utracx.database.datamodel.NominatimEntitiy;

@Dao
public interface NominatimDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(NominatimEntitiy nominatimEntitiy);

    @Query("SELECT * from nominatim_osm WHERE lat=:latitude AND lon=:longitude LIMIT 1")
    NominatimEntitiy getNominatimData(double latitude, double longitude);
}
