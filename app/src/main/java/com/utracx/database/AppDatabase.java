package com.utracx.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.utracx.api.model.rest.device_data.DeviceData;
import com.utracx.api.model.rest.vehicle_list.AlertHistoryData;
import com.utracx.api.model.rest.vehicle_list.VehicleInfo;
import com.utracx.database.dao.DeviceDataDao;
import com.utracx.database.dao.NominatimDao;
import com.utracx.database.dao.UserDetailDao;
import com.utracx.database.dao.VehiclesDao;
import com.utracx.database.datamodel.NominatimEntitiy;
import com.utracx.database.datamodel.UserDataEntity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

///7/12
@Database(entities = {UserDataEntity.class, NominatimEntitiy.class, VehicleInfo.class, DeviceData.class}, version = 19, exportSchema = false)
//@Database(entities = {UserDataEntity.class, NominatimEntitiy.class, VehicleInfo.class, DeviceData.class, AlertHistoryData.class}, version = 19, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public static final ExecutorService databaseWriteExecutor = Executors.newSingleThreadExecutor();
    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context,
                            AppDatabase.class,
                            "utrack.db"
//                            "utrack1.db"
                    )
                            .fallbackToDestructiveMigrationOnDowngrade()
                            .fallbackToDestructiveMigration()
                            .allowMainThreadQueries()
                            .addMigrations(MIGRATION_17_25)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    static final Migration MIGRATION_17_25 = new Migration(17, 25) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE vehicles_table ADD COLUMN 'alert_count' INTEGER");
        }
    };

    static final Migration MIGRATION_18_25 = new Migration(17, 25) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE vehicles_table ADD COLUMN 'use_google_api' BOOLEAN");
        }
    };

    public abstract UserDetailDao userDetailsDao();

    public abstract VehiclesDao vehiclesDao();

    public abstract DeviceDataDao deviceDataDao();

    public abstract NominatimDao nominatimDao();
}