package com.utracx.animator;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.StringDef;
import androidx.annotation.WorkerThread;
import androidx.appcompat.widget.AppCompatSeekBar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CustomCap;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.utracx.R;
import com.utracx.model.map.CustomMapPoint;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;

import static com.utracx.util.ConstantVariables.MAX_SPEED_THRESHOLD_FOR_INVALID_ROUTE;
import static com.utracx.util.ConstantVariables.THRESHOLD_DISTANCE_FOR_MARKER_ADD;
import static com.utracx.util.ConstantVariables.TOTAL_DISTANCE_THRESHOLD_FOR_INVALID_ROUTE;
import static com.utracx.util.ConstantVariables.VEHICLE_MODE_SLEEP;
import static com.utracx.util.helper.MathHelper.round;
import static com.utracx.util.helper.ResourceHelper.getBitmapDescriptor;
import static java.lang.String.valueOf;

public class MapAnimator extends AnimatorListenerAdapter implements ValueAnimator.AnimatorUpdateListener {
    private static final String TAG = "MapAnimator";

    private final GoogleMap googleMap;
    private final AccelerateDecelerateInterpolator interpolator = new AccelerateDecelerateInterpolator();
    private final String vehicleType;
    private final PolylineOptions optionsBackground;
    private final PolylineOptions optionsForeground;
    private final Activity activity;
    private final TextView textViewCurrentSpeed;
    private final boolean isRouteRequired;
    //private final TextView speedLabel;
    private ValueAnimator percentageCompletion;
    private Polyline backgroundPolyline;
    private Polyline foregroundPolyline;
    private final List<Marker> markerList;
    public List<CustomMapPoint> routePoints = new ArrayList<>();
    private double totalDistance;
    private Marker vehicleMarker;
    private final AppCompatSeekBar timeLineSeekBar;
    private final float zoomLevel = 15.0f;
    private final String vehicleMode;
    private final long sourceDate;
    private final ToggleButton routeAnimationToggle;
    private boolean isRouteAnimationRequired = false;

    public MapAnimator(@NonNull GoogleMap googleMap, @NonNull String vehicleType,
                       @NonNull Activity activity, boolean isRouteRequired, String vehicleMode,
                       AppCompatSeekBar timeLineSeekBar,
                       long sourceDate, TextView textViewCurrentSpeed,
                       ToggleButton routeAnimationToggle) {
        this.googleMap = googleMap;
        this.vehicleType = vehicleType;
        this.activity = activity;
        this.isRouteRequired = isRouteRequired;
        this.markerList = new ArrayList<>();
        this.vehicleMode = vehicleMode;
        this.optionsBackground = getBackgroundOptions();
        this.optionsForeground = getForegroundOptions();
        this.timeLineSeekBar = timeLineSeekBar;
        this.sourceDate = sourceDate;
        this.textViewCurrentSpeed = textViewCurrentSpeed;
        this.routeAnimationToggle = routeAnimationToggle;
       // this.speedLabel = speedLabel;
    }

    @NotNull
    private PolylineOptions getBackgroundOptions() {
        return new PolylineOptions()
                .color(Color.parseColor(isRouteRequired ? "#FF7D4412" : "#007D4412"))
                .zIndex(2.0f)
                .width(20);
    }

    @NotNull
    private PolylineOptions getForegroundOptions() {
        return new PolylineOptions()
                .color(Color.parseColor(isRouteRequired ? "#FFE87817" : "#00E87817"))
                .zIndex(3.0f)
                .endCap(new CustomCap(getBitmapDescriptor("end", vehicleType), 13))
                .jointType(JointType.ROUND)
                .width(10);
    }

    private void initialiseAnimation() {
        if (this.percentageCompletion != null) {
            this.percentageCompletion.pause();
            this.percentageCompletion.cancel();
            this.percentageCompletion.end();
            this.percentageCompletion.setDuration(100);
            this.percentageCompletion.removeAllUpdateListeners();
            this.percentageCompletion.removeAllListeners();
        }

        this.percentageCompletion = ValueAnimator.ofInt(0, routePoints.size());
        this.percentageCompletion.setInterpolator(interpolator);
        this.percentageCompletion.addUpdateListener(this);
        this.percentageCompletion.addListener(this);
        if (isRouteAnimationRequired) {
            this.percentageCompletion.setDuration(calculateAnimationDuration());
        } else {
            this.percentageCompletion.setDuration(5);
        }
    }

    private long calculateAnimationDuration() {
        long duration;
        int routeSize = routePoints.size();
        if (routeSize < 100) {
            duration = 300L;
        } else if (routeSize < 250) {
            duration = 280L;
        } else if (routeSize < 500) {
            duration = 260L;
        } else if (routeSize < 750) {
            duration = 240L;
        } else if (routeSize < 1000) {
            duration = 220L;
        } else if (routeSize < 1500) {
            duration = 200L;
        } else {
            duration = 200L;
        }
        long totalDuration = 1000 + routeSize * (Double.valueOf(duration * Double.parseDouble(mSpeedMultiplier)).longValue());
        Log.i(TAG, "calculateAnimationDuration: Total duration: " + totalDuration);
        return totalDuration;
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        showCurrentVehiclePosition();
        super.onAnimationEnd(animation);
    }

    int percentageValue = 0;

    @WorkerThread
    private void animateDrawing(@NonNull List<LatLng> currentlyDrawnPoints, int percentageValue) {
        int sublistIndexStart = 0;
        int sublistIndexEnd = percentageValue;
        if (isRouteAnimationRequired) {
            if (percentageValue >= this.percentageValue) {
                this.percentageValue = percentageValue;
            } else {
                return;
            }
        } else {
            if (!currentlyDrawnPoints.isEmpty()) {
                sublistIndexStart = currentlyDrawnPoints.size() - 1;
            }
            if (sublistIndexEnd < sublistIndexStart) {
                sublistIndexEnd = sublistIndexStart + 2;
            }
        }


        activity.runOnUiThread(
                () -> {
                    if (percentageCompletion.isRunning()) {
                        if (timeLineSeekBar.getProgress() < 100 && isRouteAnimationRequired) {
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(buildLatLngBBound(percentageValue).build().getCenter(), zoomLevel));
                            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                                    .target(googleMap.getCameraPosition().target)
                                    .zoom(zoomLevel)
                                    .bearing(30)
                                    .build()));
                            if (percentageValue < routePoints.size() && routePoints.get(percentageValue) != null) {
                                textViewCurrentSpeed.setText(valueOf(round(routePoints.get(percentageValue).getSpeed(), 1)));
                                //activity.findViewById(R.id.textViewKmLabel).setVisibility(View.VISIBLE);
                            }

                        } else {
                            percentageCompletion.end();
                        }
                    }
                });

        if (isRouteAnimationRequired) {
            activity.runOnUiThread(
                    () -> {
                        if (percentageValue < routePoints.size()) {
                            CustomMapPoint temp = routePoints.get(percentageValue);
                            currentlyDrawnPoints.add(temp.getMapPoint());

                            if (temp.isMarkerPoint() && totalDistance > THRESHOLD_DISTANCE_FOR_MARKER_ADD
                                    && !temp.getMarkerType().equalsIgnoreCase(VEHICLE_MODE_SLEEP)) {
                                addNewMarker(temp.getMapPoint(), getBitmapDescriptor(temp.getMarkerType(), vehicleType));
                            }

                            backgroundPolyline.setPoints(currentlyDrawnPoints);
                            foregroundPolyline.setPoints(currentlyDrawnPoints);
                            if (percentageValue >= 100) {
                                //setupVehicleMarker(false);
                            }
                        }
                    }
            );
        } else {
            Log.i(TAG, "animateDrawing: percentage - " + percentageValue);
            for (int i = sublistIndexStart; i < sublistIndexEnd && i < this.routePoints.size(); i++) {
                CustomMapPoint temp = routePoints.get(i);
                currentlyDrawnPoints.add(temp.getMapPoint());

                if (temp.isMarkerPoint() && totalDistance > THRESHOLD_DISTANCE_FOR_MARKER_ADD
                        && !temp.getMarkerType().equalsIgnoreCase(VEHICLE_MODE_SLEEP)) {
                    activity.runOnUiThread(
                            () -> addNewMarker(temp.getMapPoint(), getBitmapDescriptor(temp.getMarkerType(), vehicleType))
                    );
                }
                Log.i(TAG, "**current_points**: " + currentlyDrawnPoints.size() + "-routePoints: " + routePoints.size() + "-sublistIndexStart: " + sublistIndexStart + "-sublistIndexEnd: " + sublistIndexEnd);
            }

            activity.runOnUiThread(
                    () -> {
                        backgroundPolyline.setPoints(currentlyDrawnPoints);
                        foregroundPolyline.setPoints(currentlyDrawnPoints);
                    }
            );
        }
    }
    /*@WorkerThread
    private void animateDrawing(@NonNull List<LatLng> currentlyDrawnPoints, int percentageValue, List<CustomMapPoint> routePoints) {

        int sublistIndexStart = 0;
        if (!currentlyDrawnPoints.isEmpty()) {
            sublistIndexStart = currentlyDrawnPoints.size() - 1;
        }

        int sublistIndexEnd = percentageValue;
        if (sublistIndexEnd < sublistIndexStart) {
            sublistIndexEnd = sublistIndexStart + 2;
        }

        if (routePoints != null) {
            for (int i = sublistIndexStart; i < sublistIndexEnd && i < routePoints.size(); i++) {
                CustomMapPoint temp = routePoints.get(i);
                if (temp != null) {
                    currentlyDrawnPoints.add(temp.getMapPoint());
                    if (temp.isMarkerPoint() && totalDistance > THRESHOLD_DISTANCE_FOR_MARKER_ADD
                            && !temp.getMarkerType().equalsIgnoreCase(VEHICLE_MODE_SLEEP)) {
                        activity.runOnUiThread(
                                () ->
                                        addNewMarker(temp.getMapPoint(), getBitmapDescriptor(temp.getMarkerType(), vehicleType,vehicleMode, sourceDate))
                        );
                    } else {
                        if (routePoints != null && i <= routePoints.size() - 2)
                            activity.runOnUiThread(
                                    () ->
                                            addNewMarker(temp.getMapPoint(), getBitmapDescriptor("blank", null,vehicleMode, sourceDate))
                            );
                    }
                }

                if (routePoints == null)
                    break;
            }
        }

        activity.runOnUiThread(
                () -> {
                    if (backgroundPolyline != null && foregroundPolyline != null) {
                        backgroundPolyline.setPoints(currentlyDrawnPoints);
                        foregroundPolyline.setPoints(currentlyDrawnPoints);
                    }
                }
        );
    }
*/

    private void addNewMarker(LatLng position, BitmapDescriptor bitmapDescriptor) {
        if (isRouteRequired) {
            markerList.add(
                    googleMap.addMarker(
                            new MarkerOptions().draggable(false).icon(bitmapDescriptor).position(position)
                                    .zIndex(2.0f)

                    )
            );
        }
    }

    private void setupVehicleMarker(boolean isInvisible) {

        int vehicleMarkerIndex = this.routePoints.size() - 1;

        // if already vehicle marker is there - remove it
        if (isInvisible) {
            if (vehicleMarker != null) {
                vehicleMarker.remove();
            }
        } else {
            removeAllMarkersOnMap();
        }

        // else either null or empty -- No vehicle marker
        vehicleMarker = googleMap.addMarker(
                new MarkerOptions()
                        .draggable(false)
                        .icon(getBitmapDescriptor(isInvisible ? "blank" : "end", vehicleType))
                        .position(this.routePoints.get(vehicleMarkerIndex).getMapPoint())
                        .zIndex(2.0f)
        );

        vehicleMarker.setTag(this.routePoints.get(vehicleMarkerIndex).getMapPoint());
    }

    private void removeAllMarkersOnMap() {
        if (markerList == null || markerList.isEmpty()) {
            return;
        }
        for (int i = 0; i < markerList.size(); i++) {
            markerList.get(i).remove();
        }
        markerList.clear();
    }

    @NonNull
    private LatLngBounds.Builder buildLatLngBBound(int position) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        if (position < 0) {
            for (int i = 0; i < routePoints.size(); i++) {
                try {
                    builder.include(routePoints.get(i).getMapPoint());
                } catch (Exception e) {
                    Log.e(TAG, "buildLatLngBBound: error unable to set bounds", e);
                }
            }
        }
        if (position < 5) {
            for (int i = 0; i <= position; i++) {
                try {
                    builder.include(routePoints.get(i).getMapPoint());
                } catch (Exception e) {
                    Log.e(TAG, "buildLatLngBBound: error unable to set bounds", e);
                }
            }
        } else {
            for (int i = position - 5; i < position; i++) {
                try {
                    builder.include(routePoints.get(i).getMapPoint());
                } catch (Exception e) {
                    Log.e(TAG, "buildLatLngBBound: error unable to set bounds", e);
                }
            }
        }
        return builder;
    }

    @Override
    @MainThread
    public synchronized void onAnimationUpdate(@NotNull ValueAnimator animation) {
        if (foregroundPolyline != null) {
            final List<LatLng> points = foregroundPolyline.getPoints();

            if (timeLineSeekBar.getProgress() < 100 && isRouteAnimationRequired)
                timeLineSeekBar.setProgress(((int) animation.getCurrentPlayTime() * 100) / (int) animation.getDuration());
            else
                timeLineSeekBar.setProgress(100);

            final int percentageValue = (int) animation.getAnimatedValue();
            if (points != null && percentageCompletion != null && percentageCompletion.isRunning()) {
                Executors.newCachedThreadPool().execute(
                        () -> animateDrawing(points, percentageValue)
                );
            }
        }
    }
    public synchronized void showCurrentVehiclePosition() {
        try {
            if (routePoints != null && !routePoints.isEmpty()) {
                if (isRouteAnimationRequired) {
                    googleMap.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                    routePoints.get(routePoints.size() - 1).getMapPoint(), zoomLevel
                            )
                    );
                } else {
                    LatLngBounds.Builder builder = buildLatLngBBound(-1);
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 70));
                    isRouteAnimationRequired = false;
                }
                if (routePoints.get(routePoints.size() - 1) != null) {
                    textViewCurrentSpeed.setText(valueOf(round(routePoints.get(routePoints.size() - 1).getSpeed(), 1)));
                    //activity.findViewById(R.id.textViewKmLabel).setVisibility(View.VISIBLE);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "showCurrentVehiclePosition: ", e);
        }
    }

    /*public synchronized void showCurrentVehiclePosition() {
        try {
            if (routePoints != null && !routePoints.isEmpty()) {
                if (isRouteAnimationRequired) {
                    googleMap.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                    routePoints.get(routePoints.size() - 1).getMapPoint(), zoomLevel
                            )
                    );
                } else {
                    LatLngBounds.Builder builder = buildLatLngBBound(-1);
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 70));
                    isRouteAnimationRequired = false;
                }
                if (routePoints.get(routePoints.size() - 1) != null) {
                    textViewCurrentSpeed.setText(valueOf(round(routePoints.get(routePoints.size() - 1).getSpeed(), 1)));
                    //activity.findViewById(R.id.textViewKmLabel).setVisibility(View.VISIBLE);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "showCurrentVehiclePosition: ", e);
        }
    }*/

    public synchronized void replay() {
        if (routePoints.isEmpty()) {
            return;
        }

        if (routePoints.size() == 1) {
            activity.runOnUiThread(this::showCurrentVehiclePosition);
            return;
        }

        LatLng startPosition = this.routePoints.get(0).getMapPoint();
        activity.runOnUiThread(
                () -> {
                    // Remove existing markers from Map
                    removeAllMarkersOnMap();

                    if (backgroundPolyline != null) {
                        backgroundPolyline.remove();
                    }
                    backgroundPolyline = googleMap.addPolyline(optionsBackground.add(startPosition));

                    if (foregroundPolyline != null) {
                        foregroundPolyline.remove();
                    }
                    foregroundPolyline = googleMap.addPolyline(optionsForeground.add(startPosition));

                    if (totalDistance > THRESHOLD_DISTANCE_FOR_MARKER_ADD) {
                        addNewMarker(
                                startPosition,
                                getBitmapDescriptor("start", vehicleType)
                        );
                    }

                    initialiseAnimation();
                    routeAnimationToggle.setEnabled(true);
                    percentageCompletion.start();
                }
        );
    }

    public void setRouteAnimationRequired(boolean isRouteAnimationRequired) {
        this.isRouteAnimationRequired = isRouteAnimationRequired;
    }

    public synchronized void animateRoute(List<CustomMapPoint> routePoints, double totalDistance,
                                          double maxSpeed,boolean x, boolean y) {

        if ((routePoints == null || routePoints.isEmpty() || this.routePoints.size() >= routePoints.size())) {
            //cannot animate when obtained points are null or empty. Go Back
            // Also when existing points and new points are the same, no animation possible
            return;
        }

        this.totalDistance = totalDistance;

        // fixing the minimum drawing distance
        // IF (max speed <= 10 km/h AND distance travelled <= 15 km) OR
        // IF only one marker point in the list
        // THEN Do not draw route map.
        if (totalDistance == 0 || routePoints.size() == 1 ||
                (this.totalDistance <= TOTAL_DISTANCE_THRESHOLD_FOR_INVALID_ROUTE
                        && maxSpeed <= MAX_SPEED_THRESHOLD_FOR_INVALID_ROUTE)) {

            //At-least one element
            this.routePoints = routePoints;
            activity.runOnUiThread(
                    () -> {
                        setupVehicleMarker(false);
                        showCurrentVehiclePosition();
                    }
            );
            timeLineSeekBar.setProgress(100);
            return;
        }

        Collections.reverse(routePoints);

        if (!this.routePoints.isEmpty()) {
            // Logic for if - already some points are plotted and animation was done
            // route is already existing, also the route has a minimum of 2 points.
            // and the new route list has more points than the current list
            this.routePoints.addAll(
                    routePoints.subList(this.routePoints.size() - 1, routePoints.size() - 1)
            );
            activity.runOnUiThread(
                    () -> {
                        if (percentageCompletion != null && !percentageCompletion.isRunning()) {
                            initialiseAnimation();
                            routeAnimationToggle.setEnabled(true);
                            percentageCompletion.start();
                        }
                    }
            );
        } else {
            //create a copy of the data for local manipulation
            this.routePoints = new ArrayList<>(routePoints);
            replay();
        }
    }

    public void clearMap() {
        activity.runOnUiThread(
                () -> {
                    // Remove existing markers from Map
                    removeAllMarkersOnMap();
                    googleMap.clear();
                    if (backgroundPolyline != null) {
                        backgroundPolyline.remove();
                    }

                    if (foregroundPolyline != null) {
                        foregroundPolyline.remove();
                    }
                }
        );
    }

    public void stopAnimation() {
        if (percentageCompletion != null && percentageCompletion.isRunning()) {
            this.percentageCompletion.pause();
            this.percentageCompletion.cancel();
            this.percentageCompletion.end();
        }
    }

    public void pauseRouteDrawing() {
        if (percentageCompletion != null && percentageCompletion.isRunning()) {
            this.percentageCompletion.pause();
        }
    }

    public void resumeRouteDrawing() {
        if (percentageCompletion != null && percentageCompletion.isPaused()) {
            this.percentageCompletion.resume();
        }
    }

    public void changeAnimationSpeed() {
        setSpeedPlay();
        if (percentageCompletion != null && percentageCompletion.isRunning()) {
            //Set the duration;
            percentageCompletion.pause();
            percentageCompletion.setDuration(calculateAnimationDuration());
            long value = (percentageCompletion.getDuration() * timeLineSeekBar.getProgress()) / 100;
            percentageCompletion.setCurrentPlayTime(value);
            percentageCompletion.resume();
        }
    }

    public static final String FAST = ".3";
    public static final String FASTER = ".15";
    public static final String SLOW = "1.5";
    public static final String NORMAL = "1.0";

    @StringDef({FAST, FASTER, SLOW, NORMAL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SpeedMultiplier {
    }

    @SpeedMultiplier
    public String mSpeedMultiplier = NORMAL;

    private void setSpeedPlay() {
        if (mSpeedMultiplier.equals(NORMAL)) {
            mSpeedMultiplier = FAST;
            //speedLabel.setText("Fast");
        } else if (mSpeedMultiplier.equals(FAST)) {
            mSpeedMultiplier = FASTER;
            //speedLabel.setText("Faster");
        } else if (mSpeedMultiplier.equals(FASTER)) {
            mSpeedMultiplier = SLOW;
            //speedLabel.setText("Slow");
        } else {
            mSpeedMultiplier = NORMAL;
            //speedLabel.setText("");
        }
        Log.i(TAG, "**current_points**: Speed Change:" + mSpeedMultiplier);
    }
}