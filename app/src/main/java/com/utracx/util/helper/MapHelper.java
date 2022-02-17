package com.utracx.util.helper;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.utracx.api.model.osm.nominatim.NominatimData;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static com.utracx.util.ConstantVariables.THRESHOLD_DISTANCE_FOR_ADDRESS;

public final class MapHelper {

    private MapHelper() {
    }

    public static String getAddressString(@NonNull NominatimData nominatimData, @NotNull LatLng actualLocation) {
        return getAddressString(nominatimData, actualLocation.latitude, actualLocation.longitude);
    }

    public static String getAddressString(@NonNull NominatimData nominatimData, double lat, double lng) {

        if (nominatimData.getLat() == null && nominatimData.getLon() == null) {
            return "";
        }
        double distance = DistanceHelper.calculateDistanceFromCoordinates(
                Double.parseDouble(nominatimData.getLat()),
                lat,
                Double.parseDouble(nominatimData.getLon()),
                lng
        );
        if (distance > THRESHOLD_DISTANCE_FOR_ADDRESS) {
            //Distance is far, so use the coordinates and find distance
            return getNearByAddress(nominatimData, distance);
        } else {
            //Resolved address is very near
            return getActualAddress(nominatimData);
        }
    }

    @NotNull
    private static String getNearByAddress(@NonNull NominatimData nominatimData, double distance) {

        String shortAddress = getShortAddress(nominatimData);
        if (shortAddress != null && !shortAddress.isEmpty()) {
            distance = MathHelper.round(distance, 2);
            return String.format(
                    "%s km away from %s",
                    distance,
                    shortAddress
            );
        }

        return " - - ";
    }

    @Nullable
    private static String getShortAddress(@NonNull NominatimData nominatimData) {
        if (nominatimData.getAddress() != null) {
            String city = nominatimData.getAddress().getCounty();
            String subLocality = nominatimData.getAddress().getStateDistrict();
            String state = nominatimData.getAddress().getState();
            if (city != null && state != null) {
                return (city + ", " + state).trim();
            } else if (subLocality != null && state != null) {
                return (subLocality + ", " + state).trim();
            } else if (state != null) {
                return state.trim();
            }
        }
        return null;
    }

    @NotNull
    private static String getActualAddress(@NotNull NominatimData nominatimData) {
        if (nominatimData.getDisplayName() != null) {
            return nominatimData.getDisplayName().trim();
        }

        if (nominatimData.getAddress() != null) {
            String city = nominatimData.getAddress().getCounty();
            String subLocality = nominatimData.getAddress().getStateDistrict();
            String state = nominatimData.getAddress().getState();
            if (city != null && state != null) {
                return (city + ", " + state).trim();
            } else if (subLocality != null && state != null) {
                return (subLocality + ", " + state).trim();
            } else if (state != null) {
                return state.trim();
            }
        }

        return " - - ";
    }

    @NotNull
    public static String getEpochTime(@NonNull Long epochTimestamp, @NonNull String dateFormat) {
        Date date = new Date(epochTimestamp);
        DateFormat format = new SimpleDateFormat(dateFormat, Locale.ENGLISH);
        format.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
        format.format(date);
        format.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));//your zone
        return format.format(date);
    }
}