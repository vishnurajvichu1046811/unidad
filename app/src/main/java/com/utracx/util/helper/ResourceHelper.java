package com.utracx.util.helper;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.utracx.R;
import com.utracx.util.ConstantVariables;

import org.jetbrains.annotations.NotNull;

import static com.utracx.util.ConstantVariables.THRESHOLD_TIME_FOR_NON_COMM_VEHICLE;
import static com.utracx.util.ConstantVariables.VEHICLE_MODE_IDLE;
import static com.utracx.util.ConstantVariables.VEHICLE_MODE_MOVING;
import static com.utracx.util.ConstantVariables.VEHICLE_MODE_SLEEP;

public final class ResourceHelper {

    private ResourceHelper() {
    }

    public static Drawable getVehicleIconResource(@NonNull Context context, @Nullable String vehicleType) {
        if (vehicleType != null) {
            vehicleType = vehicleType.toLowerCase().trim();
            if (vehicleType.equalsIgnoreCase("CAR")) {
                return ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_vehicle_car, context.getTheme());
            } else if (vehicleType.equalsIgnoreCase("BUS")) {
                return ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_vehicle_bus, context.getTheme());
            } else if (vehicleType.contains("mini")) {
                return ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_vehicle_mini_lorry, context.getTheme());
            } else if (vehicleType.equalsIgnoreCase("SCHOOL BUS")) {
                return ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_vehicle_school_bus, context.getTheme());
            } else if (vehicleType.contains("truck") || vehicleType.contains("lorry")) {
                return ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_vehicle_truck, context.getTheme());
            }
        }
        return ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_vehicle_car, context.getTheme());
    }

    public static Drawable getGSMStrengthIconResource(@NonNull Context context, int signalStrength) {

        if (signalStrength >= 38) {
            return ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_gsm_100, context.getTheme());
        } else if (signalStrength >= 22) {
            return ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_gsm_50, context.getTheme());
        } else if (signalStrength >= 10) {
            return ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_gsm_25, context.getTheme());
        } else if (signalStrength >= 3) {
            return ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_gsm_10, context.getTheme());
        }
        return ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_gsm_0, context.getTheme());
    }

    public static Drawable getVehicleModeBackground(@NonNull Context context, @NonNull String vehicleMode,
                                                    long lastUpdatedTimestamp) {

        if (lastUpdatedTimestamp != 0 && (System.currentTimeMillis() - lastUpdatedTimestamp) >= THRESHOLD_TIME_FOR_NON_COMM_VEHICLE) {
            return ResourcesCompat.getDrawable(context.getResources(), R.drawable.bg_red_full_rounded, context.getTheme());
        } else if (vehicleMode.equalsIgnoreCase(VEHICLE_MODE_MOVING)) {
            return ResourcesCompat.getDrawable(context.getResources(), R.drawable.bg_blue_full_rounded, context.getTheme());
        } else if (vehicleMode.equalsIgnoreCase(VEHICLE_MODE_IDLE)) {
            return ResourcesCompat.getDrawable(context.getResources(), R.drawable.bg_green_full_rounded, context.getTheme());
        } else if (vehicleMode.equalsIgnoreCase(VEHICLE_MODE_SLEEP)) {
            return ResourcesCompat.getDrawable(context.getResources(), R.drawable.bg_orange_full_rounded, context.getTheme());
        }
        return ResourcesCompat.getDrawable(context.getResources(), R.drawable.bg_red_full_rounded, context.getTheme());
    }

/*    @NotNull
    public static BitmapDescriptor getBitmapDescriptor(String markerType, String vehicleType, String vehicleMode, long lastUpdatedTimestamp) {
        if (markerType != null && markerType.equalsIgnoreCase(ConstantVariables.VEHICLE_MODE_IDLE)) {
            return BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_park);
        } else if (markerType != null && markerType.equalsIgnoreCase(ConstantVariables.VEHICLE_MODE_SLEEP)) {
            return BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_sleep);
        } else if (markerType != null && markerType.equalsIgnoreCase(ConstantVariables.VEHICLE_MODE_MOVING)) {
            return BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_sleep);
        } else if (markerType != null && markerType.equalsIgnoreCase("end")) {
            return BitmapDescriptorFactory.fromResource(getVehicleMarker(vehicleType, vehicleMode, lastUpdatedTimestamp));
        } else if (markerType != null && markerType.equalsIgnoreCase("start")) {
            return BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_start);
        } else if (markerType != null && markerType.equalsIgnoreCase("blank")) {
            return BitmapDescriptorFactory.fromResource(R.drawable.ic_transparent);
        } else {
            return BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_sleep);
        }
    }*/
@NotNull
public static BitmapDescriptor getBitmapDescriptor(String markerType, String vehicleType) {
    if (markerType != null && markerType.equalsIgnoreCase(ConstantVariables.VEHICLE_MODE_IDLE)) {
        return BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_park);
    } else if (markerType != null && markerType.equalsIgnoreCase(ConstantVariables.VEHICLE_MODE_SLEEP)) {
        return BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_sleep);
    } else if (markerType != null && markerType.equalsIgnoreCase(ConstantVariables.VEHICLE_MODE_MOVING)) {
        return BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_sleep);
    } else if (markerType != null && markerType.equalsIgnoreCase("end")) {
        return BitmapDescriptorFactory.fromResource(getVehicleTypeMarker(vehicleType));
    } else if (markerType != null && markerType.equalsIgnoreCase("start")) {
        return BitmapDescriptorFactory.fromResource(R.drawable.ic_map_start);
    } else if (markerType != null && markerType.equalsIgnoreCase("blank")) {
        return BitmapDescriptorFactory.fromResource(R.drawable.ic_map_loc_popup);
    } else {
        return BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_sleep);
    }
}

    public static int getVehicleMarker(String vehicleType, String mode, long lastUpdatedTimestamp) {

        if (TextUtils.isEmpty(vehicleType) || TextUtils.isEmpty(vehicleType.trim())) {
            return R.drawable.ic_marker_car_runnning;
        }

        vehicleType = vehicleType.toLowerCase().trim();
        if (lastUpdatedTimestamp != 0 && (System.currentTimeMillis() - lastUpdatedTimestamp) >= THRESHOLD_TIME_FOR_NON_COMM_VEHICLE) {
            return getInactiveVehicleMarker(vehicleType);
        } else if (mode.equals(VEHICLE_MODE_MOVING)) {
            return getRunningVehicleMarker(vehicleType);
        } else if (mode.equals(VEHICLE_MODE_SLEEP)) {
            return getParkedVehicleMarker(vehicleType);
        } else if (mode.equals(VEHICLE_MODE_IDLE)) {
            return getHaltVehicleMarker(vehicleType);
        } else {
            return R.drawable.ic_marker_car_runnning;
        }
    }

    private static int getRunningVehicleMarker(String vehicleType) {
        if (vehicleType.contains("school")) {
            return R.drawable.ic_marker_school_bus_running;
        } else if (vehicleType.contains("mini")) {
            return R.drawable.ic_marker_minitruck_running;
        } else if (vehicleType.contains("truck") || vehicleType.contains("lorry")) {
            return R.drawable.ic_marker_lorry_running;
        } else if (vehicleType.contains("bus")) {
            return R.drawable.ic_marker_bus_running;
        } else {
            return R.drawable.ic_marker_car_runnning;
        }
    }

    private static int getHaltVehicleMarker(String vehicleType) {
        if (vehicleType.contains("school")) {
            return R.drawable.ic_marker_school_bus_halt;
        } else if (vehicleType.contains("mini")) {
            return R.drawable.ic_marker_minitruck_halt;
        } else if (vehicleType.contains("truck") || vehicleType.contains("lorry")) {
            return R.drawable.ic_marker_lorry_halt;
        } else if (vehicleType.contains("bus")) {
            return R.drawable.ic_marker_bus_halt;
        } else {
            return R.drawable.ic_marker_car_halt;
        }
    }

    private static int getParkedVehicleMarker(String vehicleType) {
        if (vehicleType.contains("school")) {
            return R.drawable.ic_marker_school_bus_parked;
        } else if (vehicleType.contains("mini")) {
            return R.drawable.ic_marker_minitruck_parked;
        } else if (vehicleType.contains("truck") || vehicleType.contains("lorry")) {
            return R.drawable.ic_marker_lorry_parked;
        } else if (vehicleType.contains("bus")) {
            return R.drawable.ic_marker_bus_parked;
        } else {
            return R.drawable.ic_marker_car_parked;
        }
    }

    private static int getInactiveVehicleMarker(String vehicleType) {
        if (vehicleType.contains("school")) {
            return R.drawable.ic_marker_school_bus_inactive;
        } else if (vehicleType.contains("mini")) {
            return R.drawable.ic_marker_minitruck_inactive;
        } else if (vehicleType.contains("truck") || vehicleType.contains("lorry")) {
            return R.drawable.ic_marker_lorry_inactive;
        } else if (vehicleType.contains("bus")) {
            return R.drawable.ic_marker_bus_inactive;
        } else {
            return R.drawable.ic_marker_car_inactive;
        }
    }


    private static int getVehicleTypeMarker(String vehicleType) {

        if (TextUtils.isEmpty(vehicleType) || TextUtils.isEmpty(vehicleType.trim())) {
            return R.drawable.ic_marker_car;
        }

        vehicleType = vehicleType.toLowerCase().trim();

        if (vehicleType.contains("school")) {
            return R.drawable.ic_marker_school_bus;
        } else if (vehicleType.contains("mini")) {
            return R.drawable.ic_marker_mini_truck;
        } else if (vehicleType.contains("truck") || vehicleType.contains("lorry")) {
            return R.drawable.ic_marker_lorry;
        } else if (vehicleType.contains("bus")) {
            return R.drawable.ic_marker_bus;
        } else {
            return R.drawable.ic_marker_car;
        }
    }

    public static Drawable getAlertDrawable(@NonNull Context context, @NonNull String packetType) {
        if (packetType.contains("power")) {
            return ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_main_power_alert, context.getTheme());
        } else if (packetType.contains("wire")) {
            return ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_wire_cut_alert, context.getTheme());
        } else if (packetType.contains("emergency")) {
            return ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_warning, context.getTheme());
        } else if (packetType.contains("speed")) {
            return ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_max_speed_white, context.getTheme());
        } else {
            return ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_warning, context.getTheme());
        }
    }

    public static int getAlertBackground(@NonNull Context context, @NonNull String packetType) {

        if (packetType.contains("power")) {
            return R.drawable.bg_alert_01;
        } else if (packetType.contains("wire")) {
            return R.drawable.bg_alert_02;
        } else if (packetType.contains("emergency")) {
            return R.drawable.bg_alert_03;
        } else if (packetType.contains("speed")) {
            return R.drawable.bg_alert_04;
        } else {
            return R.drawable.bg_alert_03;
        }
    }
}
