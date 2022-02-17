package com.utracx.util.helper;

import com.utracx.api.model.rest.device_data.DeviceData;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DistanceHelper {

    public static Double getDistance(@NotNull List<DeviceData> list) {
        Double totalDistance = 0.0;
        if (list.size() < 1)
            return totalDistance;
        for (int i = 1; i < list.size(); i++) {
            long endTime = list.get(i).getD().getSourceDate();
            long startTime = list.get(i - 1).getD().getSourceDate();

            long timediff = Math.abs((endTime - startTime) / 1000L);
            totalDistance += distance(list.get(i - 1).getD().getLatitude(),
                    list.get(i).getD().getLatitude(), list.get(i - 1).getD().getLongitude(),
                    list.get(i).getD().getLongitude(),
                    list.get(i - 1).getD().getSpeed(), list.get(i).getD().getSpeed(), (timediff));
        }
        return totalDistance;
    }

    private static Double distance(Double lat1, Double lat2, Double lon1, Double lon2, Double currGpsSpeed, Double prevGpsSpeed, Long elapsedDurationBetweenPackets) {
        if (lat1 != null && lat2 != null && lon1 != null && lon2 != null) {
            Double distanceFromCoords = calculateDistanceFromCoordinates(lat1, lat2, lon1, lon2);
            double distanceFromSpeed = elapsedDurationBetweenPackets < 600 ? calculateDistanceFromSpeed(currGpsSpeed, prevGpsSpeed, elapsedDurationBetweenPackets) : 0;

            return (distanceFromCoords > distanceFromSpeed ? distanceFromCoords : distanceFromSpeed);
        } else {
            return 0.0;
        }
    }

    @NotNull
    public static Double calculateDistanceFromCoordinates(double lat1, double lat2, double lon1, double lon2) {
        if (((lat1 == lat2) && (lon1 == lon2)) || ((lat1) == 0.0 || (lat2) == 0.0 || (lon1) == 0.0 || (lon2) == 0.0)) {
            return 0.0;
        }
        int R = 6378; // Radius of the Earth in miles
        double rlat1 = lat1 * (Math.PI / 180); // Convert degrees to radians
        double rlat2 = lat2 * (Math.PI / 180); // Convert degrees to radians
        double difflat = rlat2 - rlat1; // Radian difference (latitudes)
        double difflon = (lon2 - lon1) * (Math.PI / 180); // Radian difference (longitudes)

        return 2 * R * Math.asin(Math.sqrt(Math.sin(difflat / 2) * Math.sin(difflat / 2) + Math.cos(rlat1) * Math.cos(rlat2) * Math.sin(difflon / 2) * Math.sin(difflon / 2)));
    }

    private static Double calculateDistanceFromSpeed(Double currGpsSpeed, Double prevGpsSpeed, Long elapsedDurationBetweenPackets) {
        return (((currGpsSpeed + prevGpsSpeed) / 2) * (elapsedDurationBetweenPackets) / (60 * 60));
    }
}
