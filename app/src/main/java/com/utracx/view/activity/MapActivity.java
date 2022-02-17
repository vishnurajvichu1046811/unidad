package com.utracx.view.activity;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.appcompat.widget.AppCompatToggleButton;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.transition.AutoTransition;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.mcsoft.timerangepickerdialog.RangeTimePickerDialog;
import com.romainpiel.shimmer.Shimmer;
import com.romainpiel.shimmer.ShimmerTextView;
import com.utracx.R;
import com.utracx.animator.MapAnimator;
import com.utracx.api.model.rest.device_data.DeviceData;
import com.utracx.api.model.rest.vehicle_list.VehicleInfo;
import com.utracx.api.request.ApiUtils;
import com.utracx.api.request.calls.VehicleDeviceDetailListRequestCall;
import com.utracx.background.AddressTask;
import com.utracx.background.MapCalculationTask;
import com.utracx.background.TripCalculationTask;
import com.utracx.database.datamodel.UserDataEntity;
import com.utracx.model.map.CustomMapPoint;
import com.utracx.model.map.MapDataModel;
import com.utracx.util.helper.DateTimeHelper;
import com.utracx.util.helper.DistanceHelper;
import com.utracx.util.helper.MapHelper;
import com.utracx.util.helper.NavigationHelper;
import com.utracx.util.helper.ResourceHelper;
import com.utracx.util.helper.SnackBarHelper;
import com.utracx.util.helper.StatusBarHelper;
import com.utracx.view.adapter.MakerInfoWindow;
import com.utracx.view.listener.DateButtonClickListener;
import com.utracx.view.listener.TripDetailsCallback;
import com.utracx.viewmodel.MapActivityViewModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.utracx.api.request.ApiUtils.cancelAllOpenStreetMapsRequest;
import static com.utracx.api.request.ApiUtils.cancelAllSOSRequest;
import static com.utracx.background.MapCalculationTask.BUNDLE_KEY_MAP_ACTIVITY_VIEW_MODEL;
import static com.utracx.util.ConstantVariables.KEY_DATE_SELECTED;
import static com.utracx.util.ConstantVariables.ONE_DAY_END_MILLISECOND;
import static com.utracx.util.ConstantVariables.THRESHOLD_TIME_FOR_NON_COMM_VEHICLE;
import static com.utracx.util.helper.DateTimeHelper.getEndTimeOfDay;
import static com.utracx.util.helper.DateTimeHelper.getStartTimeOfDay;
import static com.utracx.util.helper.DateTimeHelper.isSameDay;
import static com.utracx.util.helper.MathHelper.round;
import static com.utracx.util.helper.NavigationHelper.KEY_NAVIGATION_DATA_BUNDLE;
import static com.utracx.util.helper.ResourceHelper.getGSMStrengthIconResource;
import static com.utracx.util.helper.ResourceHelper.getVehicleModeBackground;
import static com.utracx.util.helper.SharedPreferencesHelper.getUserEmail;
import static com.utracx.view.activity.HomeActivity.BUNDLE_KEY_VEHICLE_DETAIL;

public class MapActivity extends BaseRefreshActivity implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener, BaseRefreshActivity.HandlerTimerCallback,
        AddressTask.GeoAddressUpdateCallback, GoogleMap.OnCameraIdleListener,
        MapCalculationTask.MapCalculationTaskCallback,
        GoogleMap.OnMapLoadedCallback,
        RangeTimePickerDialog.ISelectedTime, CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "MapActivity";
    private static final String TIME_RANGE_PICKER_TAG = "timerangepicker";
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
    // Map calculation
    private final ExecutorService calculationExecutorService = Executors.newSingleThreadExecutor();
    boolean isFullscreen = false;
    //Google Maps related items
    private GoogleMap googleMap;
    private float zoomLevel = 15.0f;
    //Location fetch - Get Vehicle's GPS LetLng
    private VehicleDeviceDetailListRequestCall deviceDetailsRequestCall;
    //Data from Trip Details
    private VehicleInfo vehicleInfo;
    private Calendar startTimeCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    private Calendar endTimeCalendar;
    //UI elements for showing vehicle details
    private ImageView imageViewVehicleType;
    private TextView txtStatus;
    private ImageView imageViewGSM;
    private TextView txtIgnition;
    private View vehicleImageContainerLayout;
    private TextView textViewRegNo, tvRegTop, tvImeiTop, tvDistance;
    private TextView textViewVehicleDate;
    private TextView textViewCurrentSpeed, tvCurrentSpeed;
    private TextView txtIMEI;
    private MapActivityViewModel activityViewModel;
    private View contentView;
    private Future<?> mapCalculationThread;
    private TextInputEditText dateInputEditText;
    private TextInputEditText timeInputEditText;
    private LiveData<List<DeviceData>> currentDateDeviceDetail;
    //Animation
    private MapAnimator mapAnimator;
    private ViewGroup parentView;
    private View vehicleInfoLayout;
    private TripDetailsCallback tripDetailsCallback;
    private Double distance = 0.0d;
    private final ExecutorService animExecutorService = Executors.newSingleThreadExecutor();
    private boolean shouldShowCurrentLocation = Boolean.FALSE;
    private List<CustomMapPoint> customMapPoints;
    private ShimmerTextView textViewAddress;
    private Shimmer shimmer;

    LinearLayout llPlaybackDtime, llPlayBottom;
    LinearLayout llPlayzoom;
    ImageView ivUnidadimg, ivHome;
    private FloatingActionButton mFab;
    RelativeLayout topseekbar;
    //AppCompatToggleButton playbutton;
    RelativeLayout rlBootamSeekbar, rlTopLayout;
    private AppCompatSeekBar timeLineSeekBar;
    Button btnTripDetails, btnRoutePlayback;
    private ToggleButton routeAnimationToggle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map1);
        StatusBarHelper.setupStatusBarWithToolbar(this);

        findViewById(R.id.backView).setOnClickListener(backView -> finish());

        rlTopLayout = findViewById(R.id.top_layout);
        topseekbar = findViewById(R.id.top_layout_seekbar);
        rlBootamSeekbar = findViewById(R.id.rl_bottomseek);
        //playbutton = findViewById(R.id.routeToggleButton);
        llPlaybackDtime = findViewById(R.id.ll_playback_dtime);
        llPlayBottom = findViewById(R.id.ll_playBottom);
        llPlayBottom.setVisibility(View.VISIBLE);
        topseekbar.setVisibility(View.GONE);
        llPlaybackDtime.setVisibility(View.GONE);
        routeAnimationToggle = findViewById(R.id.routeToggleButton);
        routeAnimationToggle.setOnCheckedChangeListener(this);
        timeLineSeekBar = findViewById(R.id.timeline);
        ImageButton speedChange = findViewById(R.id.speedChange);
        TextView speedLabel = findViewById(R.id.speedLabel);
        speedChange.setOnClickListener(v -> {
            mapAnimator.changeAnimationSpeed();
        });

        timeLineSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                if (timeLineSeekBar.getProgress() == 100) {
                    //routeAnimationToggle.setEnabled(false);
                    routeAnimationToggle.setChecked(true);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        initViewsFromLayout();
        setupDateTextView();
        initViewModel();
        intiCallbacks();
        startGoogleMapLoading();
        if(isFromHome()) {

            getUserVehicleList();
            hideViews();
        }
        else {
            loadIntentData();
            setupVehicleDataInViews();
            updateIMEI();
            startObservingVehicleInfo();
        }
    }

    @Override
    protected void onResume() {
        toggleAutoRefresh();
        super.onResume();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;


        setupMapStyle();
        String speed = MapAnimator.NORMAL;
        if (mapAnimator != null) {
            speed = mapAnimator.mSpeedMultiplier;
        }
        this.mapAnimator = new MapAnimator(
                googleMap,
                vehicleInfo.getVehicleType(),
                this,
                vehicleInfo.isRouteRequired(),
                vehicleInfo.getLastUpdatedData().getVehicleMode(),
                timeLineSeekBar, vehicleInfo.getLastUpdatedData().getSourceDate(),
                textViewCurrentSpeed,
                routeAnimationToggle
        );
        mapAnimator.mSpeedMultiplier = speed;
        mapAnimator.setRouteAnimationRequired(false);

        this.googleMap.setPadding(0, 0, 0, 380);
        this.googleMap.setOnMapLoadedCallback(this);
        this.googleMap.setOnMarkerClickListener(this);
        this.googleMap.setInfoWindowAdapter(new MakerInfoWindow(this, vehicleInfo.getUseGoogleApi()));
    }

    private void setupMapStyle() {
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json)
            );

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }
    }

    @Override
    public void onMapLoaded() {
        startObservingDeviceDataList();
    }

    @Override
    public void onUpdate() {
        fetchDeviceData();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (!marker.isInfoWindowShown() &&
                (marker.getSnippet() != null)) {
            try {
                new JSONObject(marker.getSnippet());
                googleMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                marker.showInfoWindow();
            } catch (JSONException jsonException) {
                Log.i(TAG, "onMarkerClick: " + jsonException.getMessage());
            }

        } else {
            marker.hideInfoWindow();
        }
        return true;
    }

    @Override
    public void onCameraIdle() {
        if (googleMap != null) {
            googleMap.getCameraPosition();
            CameraPosition position = googleMap.getCameraPosition();
            if (position.zoom != zoomLevel) {
                this.zoomLevel = position.zoom;
            }
        }
    }

    @Override
    public void onObtainDataFromServer(Bundle dataBundle) {
        if(!isFromHome()) {
            if (dataBundle != null && dataBundle.containsKey(BUNDLE_KEY_MAP_ACTIVITY_VIEW_MODEL)) {
                MapDataModel mapDataModel = dataBundle.getParcelable(BUNDLE_KEY_MAP_ACTIVITY_VIEW_MODEL);
                if (mapDataModel != null && mapDataModel.getMapPoints() != null
                        && !mapDataModel.getMapPoints().isEmpty()) {
                    if (customMapPoints != null) {
                        customMapPoints.clear();
                        customMapPoints.addAll(mapDataModel.getMapPoints());
                    } else {
                        customMapPoints = Collections.synchronizedList(mapDataModel.getMapPoints());
                    }

                    startAnimationThread(customMapPoints, isFromHome(),0);
                }


            }
        }else{

            if(getIntent() != null && getIntent().hasExtra(NavigationHelper.KEY_NAVIGATION_DATA_BUNDLE)) {

                Bundle data = getIntent().getBundleExtra(NavigationHelper.KEY_NAVIGATION_DATA_BUNDLE);
                if(data != null && data.containsKey(HomeActivity.USER_VEHICLE_LIST)) {
                    ArrayList<VehicleInfo> vehiclesArrayList = data.getParcelableArrayList(HomeActivity.USER_VEHICLE_LIST);


                    if (customMapPoints != null) {
                        customMapPoints.clear();}
                    else customMapPoints = new ArrayList<>();

                    if(vehiclesArrayList != null && vehiclesArrayList.size() >0)
                        for(VehicleInfo vehicleInfo : vehiclesArrayList){

                            if(vehicleInfo != null && vehicleInfo.getLastUpdatedData() != null) {
                                LatLng latLng = new LatLng(vehicleInfo.getLastUpdatedData().getLatitude(), vehicleInfo.getLastUpdatedData().getLongitude());
                                CustomMapPoint customMapPoint = new CustomMapPoint(latLng, null, true, "");
                                customMapPoints.add(customMapPoint);
                            }
                        }
                }

            }
            startAnimationThread(customMapPoints, isFromHome(),0);
        }
    }

    @Override
    protected void onStop() {
        cancelAllOpenStreetMapsRequest();
        cancelAllSOSRequest();
        super.onStop();
    }

    private void toggleAutoRefresh() {
        super.updateCallbackChild(isSameDay(Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime(), startTimeCalendar.getTime()) ? this : null);
        super.setAutoRefresh(isSameDay(Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime(), startTimeCalendar.getTime()));
    }

    private void fetchDeviceData() {
        Executors.newSingleThreadExecutor().execute(
                () -> {
                    String startTime = getStartTimeOfDay(startTimeCalendar.getTimeInMillis()) + "";
                    String endTime = getEndTimeOfDay(startTimeCalendar.getTimeInMillis()) + "";

                    // Check if DB has packet for the selected date
                    long lastUpdatedTime = getSourceDateFromLastObtainedPacket(getStartTimeOfDay(startTimeCalendar.getTimeInMillis()));
                    if (lastUpdatedTime > 0) {
                        startTime = lastUpdatedTime + "";
                    }

                    if (!startTime.isEmpty() && !endTime.isEmpty()) {

                        UserDataEntity userData = activityViewModel.fetUserDataByEmail(getUserEmail(this));
                        if (userData != null && vehicleInfo != null && vehicleInfo.getLastUpdatedData() != null && vehicleInfo.getLastUpdatedData().getSerialNumber() != null
                                && !vehicleInfo.getLastUpdatedData().getSerialNumber().isEmpty()) {
                            ApiUtils.getInstance().getSOService()
                                    .getOptimisedVehicleDeviceDataV2(
                                            vehicleInfo.getLastUpdatedData().getSerialNumber(),
                                            userData.getUsername(),
                                            userData.getPassword(),
                                            startTime,
                                            endTime
                                    )
                                    .enqueue(deviceDetailsRequestCall);
                        }
                    }
                }
        );
    }

    private long getSourceDateFromLastObtainedPacket(long startOfDay) {
        if (vehicleInfo.getLastUpdatedData()
                != null && vehicleInfo.getLastUpdatedData().getSerialNumber() != null)
            return activityViewModel.getLatestDeviceDataUpdateTimestamp(vehicleInfo.getLastUpdatedData().getSerialNumber(), startOfDay, startOfDay + ONE_DAY_END_MILLISECOND);
        return 0;
    }

    private void updatePacketData() {

        updateCurrentVehicleDate();
        updateCurrentVehicleMode();
        setupAddress();
        setGSMState();
        setGPSState();
        setIgnitionState();
        updateCurrentSpeed();
        updateIMEI();
    }

    private void updateIMEI() {
        if (vehicleInfo != null && vehicleInfo.getLastUpdatedData() != null && vehicleInfo.getLastUpdatedData().getImeiNo() != null) {
            txtIMEI.setText(vehicleInfo.getLastUpdatedData().getImeiNo());
            tvImeiTop.setText(vehicleInfo.getLastUpdatedData().getImeiNo());
        } else {
            txtIMEI.setText("");
            tvImeiTop.setText("");
        }
    }

    private void updateDistance(@NonNull List<DeviceData> mapPoints) {
        runOnUiThread(() -> {
            setProgressBarViewState(Boolean.FALSE);
            Executors.newSingleThreadExecutor().execute(
                    () -> {
                        List<DeviceData> dummyList = new ArrayList<>(mapPoints);
                        Collections.reverse(dummyList);
                        distance = DistanceHelper.getDistance(mapPoints);
                        Collections.reverse(mapPoints);
//                        tvDistance.setText(String.format("%.2f",distance) + " Km");
                    }
            );
        }
        );

        tvDistance.setText(String.format("%.2f",distance) + " Km");
    }

    private void setGPSState() {

        if ( vehicleInfo.getLastUpdatedData() != null && vehicleInfo.getLastUpdatedData().getGnssFix() != null
                && (System.currentTimeMillis() - vehicleInfo.getLastUpdatedData().getSourceDate()) < THRESHOLD_TIME_FOR_NON_COMM_VEHICLE) {
            txtStatus.setText(
                    getString(R.string.card_connect_label));
        } else {
            txtStatus.setText(
                    getString(R.string.card_disconnect_label));
        }
    }

    private void setGSMState() {
        if (vehicleInfo.getLastUpdatedData() != null && vehicleInfo.getLastUpdatedData().getGsmSignalStrength() != null) {
            imageViewGSM.setImageDrawable(
                    getGSMStrengthIconResource(this, vehicleInfo.getLastUpdatedData().getGsmSignalStrength())
            );
        } else {
            imageViewGSM.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_gsm_0, getTheme()));
        }
    }

    private void setIgnitionState() {
        if (vehicleInfo.getLastUpdatedData() != null
                && vehicleInfo.getLastUpdatedData().getIgnition() != null
                && vehicleInfo.getLastUpdatedData().getIgnition().toLowerCase().contains("on")) {
            txtIgnition.setText(
                    getString(R.string.card_ignition_on_label));
        } else {
            txtIgnition.setText(
                    getString(R.string.card_ignition_off_label));
        }
    }

    private void updateCurrentSpeed() {
        if (vehicleInfo.getLastUpdatedData() != null
                && vehicleInfo.getLastUpdatedData().getSpeed() != null
                && vehicleInfo.getLastUpdatedData().getSpeed() > 0.0) {
            textViewCurrentSpeed.setText(
                    String.format(getString(R.string.card_speed), round(vehicleInfo.getLastUpdatedData().getSpeed(), 1)));

            tvCurrentSpeed.setText(textViewCurrentSpeed.getText().toString());
        } else {
            textViewCurrentSpeed.setText(String.format(getString(R.string.card_speed), 0.0));
            tvCurrentSpeed.setText(String.format(getString(R.string.card_speed), 0.0));
        }
    }

    private void setupAddress() {
        if (vehicleInfo == null || vehicleInfo.getLastUpdatedData() == null) {
            return;
        }
        String address = null;
        try {
            address = vehicleInfo.getLastUpdatedData().getAddress();
        } catch (Exception e) {
            Log.e(TAG, "setupAddress: Unable to get address", e);
        }
        if (address == null || address.isEmpty()) {
            if (tripDetailsCallback == null) {
                tripDetailsCallback = new TripDetailsCallback(this, activityViewModel);
            }
            new AddressTask(vehicleInfo.getUseGoogleApi() ,vehicleInfo.getLastUpdatedData(), this, tripDetailsCallback, null).run();
            return;
        }

        setLabelForTextView(textViewAddress, formatAddress(vehicleInfo.getLastUpdatedData().getAddress()));
        textViewAddress.setSelected(true);
    }

    private void updateCurrentVehicleMode() {
        if (vehicleInfo.getLastUpdatedData() != null
                && vehicleInfo.getLastUpdatedData().getVehicleMode() != null
                && vehicleInfo.getLastUpdatedData().getSourceDate() != null) {
            vehicleImageContainerLayout.setBackground(
                    getVehicleModeBackground(
                            this,
                            vehicleInfo.getLastUpdatedData().getVehicleMode(),
                            vehicleInfo.getLastUpdatedData().getSourceDate()
                    )
            );
        } else {
            vehicleImageContainerLayout.setBackground(
                    getVehicleModeBackground(
                            this,
                            "",
                            0
                    )
            );
        }
    }

    private void updateCurrentVehicleDate() {
        if (vehicleInfo.getLastUpdatedData() != null
                && vehicleInfo.getLastUpdatedData().getSourceDate() != null) {
            String date = MapHelper.getEpochTime(
                    vehicleInfo.getLastUpdatedData().getSourceDate(),
                    "dd-MMM-yyyy"
            );
            String time = MapHelper.getEpochTime(
                    vehicleInfo.getLastUpdatedData().getSourceDate(),
                    "hh:mm aa"
            );
            textViewVehicleDate.setText(
                    String.format(getString(R.string.card_date), date, time)
            );
        } else {
            textViewVehicleDate.setText(getString(R.string.card_date_no_format));
        }
    }

    private void setupDateTextView() {
        dateInputEditText.setText(simpleDateFormat.format(getStartTimeCalendar().getTime()));
        shouldShowCurrentLocation = isSameDay(Calendar.getInstance().getTime(), getStartTimeCalendar().getTime());
    }

    private void startObservingVehicleInfo() {
        if (activityViewModel != null && vehicleInfo != null
                && !vehicleInfo.getId().isEmpty()
                && vehicleInfo.getLastUpdatedData() != null
                && vehicleInfo.getLastUpdatedData().getSerialNumber() != null
                && !vehicleInfo.getLastUpdatedData().getSerialNumber().isEmpty()) {

            activityViewModel.getLiveVehicleInfo(vehicleInfo.getId())
                    .observe(
                            this,
                            updatedVehicleInfo -> {
                                if (null != vehicleInfo && null != vehicleInfo.getLastUpdatedData()) {
                                    this.vehicleInfo = updatedVehicleInfo;

                                    if (this.vehicleInfo.getLastUpdatedData().getSourceDate() != null
                                            && this.vehicleInfo.getLastUpdatedData().getSourceDate() > 0) {
                                        updatePacketData();
                                    }
                                }
                            }
                    );
        }
    }

    private void startObservingDeviceDataList() {
        if (activityViewModel != null && vehicleInfo != null
                && vehicleInfo.getLastUpdatedData() != null
                && vehicleInfo.getLastUpdatedData().getSerialNumber() != null
                && !vehicleInfo.getLastUpdatedData().getSerialNumber().isEmpty()) {

            stopObservingDeviceDataList();
            long statTimeMilliSec;
            long endTimeMilliSec;
            if (Objects.requireNonNull(timeInputEditText.getText()).toString().trim().equals("")) {
                statTimeMilliSec = getStartTimeOfDay(getStartTimeCalendar().getTimeInMillis());
                endTimeMilliSec = getStartTimeOfDay(getStartTimeCalendar().getTimeInMillis()) + ONE_DAY_END_MILLISECOND;
            } else {
                long difference_In_Time
                        = getStartTimeCalendar().getTimeInMillis() - getEndTimeCalendar().getTimeInMillis();

                long difference_In_Minutes
                        = (difference_In_Time
                        / (1000 * 60))
                        % 60;
                statTimeMilliSec = getStartTimeCalendar().getTimeInMillis();
                if (difference_In_Minutes <= 1) {
                    endTimeMilliSec = getStartTimeOfDay(getStartTimeCalendar().getTimeInMillis()) + ONE_DAY_END_MILLISECOND;
                } else {
                    endTimeMilliSec = getEndTimeCalendar().getTimeInMillis();
                }
            }
            currentDateDeviceDetail = activityViewModel.getLiveDeviceDataList(vehicleInfo.getLastUpdatedData().getSerialNumber(), statTimeMilliSec, endTimeMilliSec);
            currentDateDeviceDetail.observe(
                    this,
                    deviceDataList -> {
                        // when database has no entries for an old date fire the API,
                        // for today the API call will automatically fire with each refresh
                        if (!isSameDay(new Date(), startTimeCalendar.getTime())
                                && (deviceDataList == null || deviceDataList.isEmpty())) {
                            onUpdate();
                            setProgressBarViewState(Boolean.FALSE);
                            return;
                        }


                        if (deviceDataList != null ) {// && !deviceDataList.isEmpty()
                            String imei = vehicleInfo.getLastUpdatedData().getImeiNo();
                            if(!deviceDataList.isEmpty())
                                vehicleInfo.setLastUpdatedData(deviceDataList.get(0).getD());
                            vehicleInfo.getLastUpdatedData().setImeiNo(imei);
                            updateDistance(deviceDataList);
                            updatePacketData();
                            fireMapCalculationTask(statTimeMilliSec, endTimeMilliSec, deviceDataList);
                        } else {

                            setProgressBarViewState(Boolean.FALSE);

                        }
                    }
            );
        }
    }

    private Calendar getStartTimeCalendar() {
        return startTimeCalendar;
    }

    public Calendar getEndTimeCalendar() {
        if (endTimeCalendar == null) {
            endTimeCalendar = Calendar.getInstance();
            endTimeCalendar.setTimeInMillis(getStartTimeCalendar().getTimeInMillis());
        }
        return endTimeCalendar;
    }

    private void fireMapCalculationTask(long startOfDay, long endTimeMilliSec, List<DeviceData> deviceDataList) {
        Executors.newSingleThreadExecutor().execute(
                () -> {
                    double maxSpeed = activityViewModel.getMaxSpeed(vehicleInfo.getLastUpdatedData().getSerialNumber(), startOfDay, endTimeMilliSec);
                    if (mapCalculationThread != null && !mapCalculationThread.isDone() && !mapCalculationThread.isCancelled()) {
                        mapCalculationThread.cancel(true);
                        mapCalculationThread = null;
                    }
                    mapCalculationThread = calculationExecutorService.submit(
                            new MapCalculationTask(
                                    MapActivity.this,
                                    deviceDataList,
                                    MapActivity.this,
                                    maxSpeed
                            )
                    );
                }
        );
    }

    private String formatAddress(String address) {

        if (address.contains("+")) {
            address = address.substring(address.indexOf(",")+1);
        }

        return address;

    }

/*    private void startAnimationThread(List<CustomMapPoint> mapPoints, boolean fromHome, int streekPos) {
        animExecutorService.submit(
                () -> {
                    if (mapPoints!=null && mapPoints.size()>0) {
                        setProgressBarViewState(Boolean.FALSE);
                        long startOfDay = getStartTimeOfDay(startTimeCalendar.getTimeInMillis());

                        double maxSpeed = activityViewModel.getMaxSpeed(vehicleInfo.getLastUpdatedData().getSerialNumber(), startOfDay, startOfDay + ONE_DAY_END_MILLISECOND);

                        if (this.mapAnimator != null) {
                            mapAnimator.animateRoute(mapPoints, distance, maxSpeed, shouldShowCurrentLocation,fromHome);
                        }
                    }
                }
        );
    }*/
private void startAnimationThread(List<CustomMapPoint> mapPoints,boolean x, int y) {
    animExecutorService.submit(
            () -> {
                long statTimeMilliSec;
                long endTimeMilliSec;
                if (timeInputEditText.getText().toString().trim().equals("")) {
                    statTimeMilliSec = getStartTimeOfDay(getStartTimeCalendar().getTimeInMillis());
                    endTimeMilliSec = getStartTimeOfDay(getStartTimeCalendar().getTimeInMillis()) + ONE_DAY_END_MILLISECOND;
                } else {
                    long difference_In_Time
                            = getStartTimeCalendar().getTimeInMillis() - getEndTimeCalendar().getTimeInMillis();

                    long difference_In_Minutes
                            = (difference_In_Time
                            / (1000 * 60))
                            % 60;
                    statTimeMilliSec = getStartTimeCalendar().getTimeInMillis();
                    if (difference_In_Minutes <= 1) {
                        endTimeMilliSec = getStartTimeOfDay(getStartTimeCalendar().getTimeInMillis()) + ONE_DAY_END_MILLISECOND;
                    } else {
                        endTimeMilliSec = getEndTimeCalendar().getTimeInMillis();
                    }
                }

                double maxSpeed = activityViewModel.getMaxSpeed(vehicleInfo.getLastUpdatedData().getSerialNumber(), statTimeMilliSec, endTimeMilliSec);

                if (this.mapAnimator != null) {
                    mapAnimator.animateRoute(mapPoints, distance, maxSpeed,false,false);
                }
            }
    );
}


    private void initViewModel() {
        activityViewModel = new ViewModelProvider(this).get(MapActivityViewModel.class);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void initViewsFromLayout() {
        contentView = getWindow().getDecorView();
        parentView = findViewById(R.id.coordinatorLayout);
        txtIMEI = findViewById(R.id.txtIMEI);
        vehicleInfoLayout = findViewById(R.id.vehicleInfoLayout);
        timeInputEditText = findViewById(R.id.time_input);
        dateInputEditText = findViewById(R.id.date_input);

        btnTripDetails = findViewById(R.id.button2);
        btnRoutePlayback = findViewById(R.id.button3);

        vehicleImageContainerLayout = findViewById(R.id.vehicleImageContainerLayout);
        imageViewVehicleType = findViewById(R.id.imageViewVehicleType);
        textViewAddress = findViewById(R.id.textViewVehicleAddress);
        textViewRegNo = findViewById(R.id.textViewVehicleRegNo);
        tvRegTop = findViewById(R.id.tv_VehicleRegNo);
        tvImeiTop = findViewById(R.id.txtIMEI1);
        tvDistance = findViewById(R.id.tv_distance);
        textViewVehicleDate = findViewById(R.id.txtVehicleDate);
        txtStatus = findViewById(R.id.txtStatus);
        imageViewGSM = findViewById(R.id.imageViewSignal);
        txtIgnition = findViewById(R.id.txtIgnition);

        textViewCurrentSpeed = findViewById(R.id.txtCurrentSpeed);
        tvCurrentSpeed = findViewById(R.id.tv_CurrentSpeed);

        shimmer = new Shimmer();
        timeInputEditText.setFocusableInTouchMode(false);
        timeInputEditText.setLongClickable(false);
        timeInputEditText.setOnClickListener(view -> showTimeRange());
        timeInputEditText.setCursorVisible(false);
        dateInputEditText.setFocusableInTouchMode(false);
        dateInputEditText.setLongClickable(false);
        dateInputEditText.setOnClickListener(new DateButtonClickListener(this));
        dateInputEditText.setCursorVisible(false);
        mFab = findViewById(R.id.fab);
        llPlayzoom = findViewById(R.id.ll_playzoom);
        ivUnidadimg = findViewById(R.id.iv_unidadimg);
        ivUnidadimg.setVisibility(View.VISIBLE);

        btnTripDetails.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
        btnRoutePlayback.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));

//        ivHome = findViewById(R.id.iv_Home);
//        ivHome.setOnClickListener(backView -> finish());
//        ivHome.setVisibility(View.GONE);
        timeLineSeekBar = findViewById(R.id.timeline);

        ImageButton speedChange = findViewById(R.id.speedChange);

        speedChange.setOnClickListener(v -> {
            mapAnimator.changeAnimationSpeed();
        });

        timeLineSeekBar.setMax(100);
        timeLineSeekBar.setEnabled(true);
        timeLineSeekBar.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {

                return false;
            }
        });

        ImageButton replayButton = findViewById(R.id.replay_route);

        replayButton.setOnClickListener(v -> {

            if(timeLineSeekBar.getProgress() >0){
                timeLineSeekBar.setProgress(0);
            }
            if (mapAnimator != null && customMapPoints != null && customMapPoints.size() > 0) {
                setProgressBarViewState(Boolean.TRUE);
                shouldShowCurrentLocation = Boolean.FALSE;
                mapAnimator.stopAnimation();
                //clearMap();
                startAnimationThread(customMapPoints, isFromHome(),0);
            }

        });

        timeLineSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                if (timeLineSeekBar.getProgress() == 100) {
                    routeAnimationToggle.setChecked(true);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

//                Toast.makeText(MapActivity.this, "position"+seekBar.getProgress(), Toast.LENGTH_SHORT).show();
                Log.e("","");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

//                int seekbarPos = seekBar.getProgress();
//                startAnimationThread(customMapPoints, isFromHome(),seekbarPos);

//                Toast.makeText(MapActivity.this, "position"+seekBar.getProgress(), Toast.LENGTH_SHORT).show();
                Log.e("","");
            }
        });

      /*  playbutton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                if (isChecked) {
                    mapAnimator.pauseRouteDrawing();
                } else {
                    if (timeLineSeekBar.getProgress() == 100) {
                        replayTimeLine();
                    } else {
                        mapAnimator.resumeRouteDrawing();
                    }
                }

            }
        });*/

    }
    private void replayTimeLine() {
        if (mapAnimator != null) {
            List<CustomMapPoint> mapPoints = mapAnimator.routePoints;
            Collections.reverse(mapPoints);
            mapAnimator.stopAnimation();
            clearMap();
            mapAnimator.setRouteAnimationRequired(true);
            startAnimationThread(mapPoints,false,0);
        }
    }

    private void intiCallbacks() {
        tripDetailsCallback = new TripDetailsCallback(this, activityViewModel);
        deviceDetailsRequestCall = new VehicleDeviceDetailListRequestCall(new TripCalculationTask(tripDetailsCallback));
    }

    private void loadIntentData() {
        Bundle dataBundle = getIntent().getExtras();
        if (dataBundle != null && dataBundle.containsKey(KEY_NAVIGATION_DATA_BUNDLE)) {
            Bundle innerBundle = dataBundle.getBundle(KEY_NAVIGATION_DATA_BUNDLE);

            if (innerBundle != null) {
                if (innerBundle.containsKey(BUNDLE_KEY_VEHICLE_DETAIL)) {
                    vehicleInfo = innerBundle.getParcelable(BUNDLE_KEY_VEHICLE_DETAIL);
                }

                if (innerBundle.containsKey(KEY_DATE_SELECTED)) {
                    long selectedDate = innerBundle.getLong(KEY_DATE_SELECTED, -1L);
                    if (selectedDate > 0) {
                        getStartTimeCalendar().setTimeInMillis(selectedDate);
                        getStartTimeCalendar().setTimeZone(TimeZone.getTimeZone("UTC"));
                        setupDateTextView();
                    }
                }
            }
        }
    }

    private void startGoogleMapLoading() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_frag);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void setupVehicleDataInViews() {
        setLabelForTextView(
                textViewRegNo,
                vehicleInfo
                        .getVehicleRegistration()
                        .replace(".", "")
                        .replace(" ", "")
                        .toUpperCase()
        );
        tvRegTop.setText(textViewRegNo.getText().toString());
        setLabelForTextView(textViewAddress, "");
        imageViewVehicleType.setImageDrawable(ResourceHelper.getVehicleIconResource(this, vehicleInfo.getVehicleType()));
        updateDistanceLabel();
    }
    private void updateDistanceLabel() {
        if (!vehicleInfo.isRouteRequired()) {
           // ((TextView) findViewById(R.id.distanceLabel)).setText(R.string.gps_distance);
        }
    }

    private void setLabelForTextView(TextView textView, String label) {
        if (label != null && !label.isEmpty()) {
            textView.setText(label);
        }
        handleTextViewAnimation(textView, label);
    }

    private void handleTextViewAnimation(TextView textViewAnimation, String label) {
        if (textViewAnimation.getId() == textViewAddress.getId()) {
            if (label!=null && label.trim().length()<1 && !shimmer.isAnimating()) {
                textViewAddress.setText(textViewAddress.getContext().getString(R.string.location_fetching));
                shimmer.start(textViewAddress);
            } else if (shimmer.isAnimating()){
                shimmer.cancel();
            }
        }
    }

    private void hideSystemUI() {
        toggleView(true);
        contentView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    private void showSystemUI() {
        toggleView(false);
        contentView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        & ~View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        & ~View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    private void toggleView(boolean show) {

        Transition transition = new AutoTransition();
        transition.setDuration(300);
        transition.addTarget(R.id.vehicleInfoLayout);

        TransitionManager.beginDelayedTransition(parentView, transition);
        vehicleInfoLayout.setVisibility(show ? View.GONE : View.VISIBLE);
        findViewById(R.id.toolbar).setVisibility(show ? View.GONE : View.VISIBLE);
    }

    public void showFailureMessage(String message) {
        setProgressBarViewState(Boolean.FALSE);
        SnackBarHelper.showLongMessage(MapActivity.this, message);
    }

    public VehicleInfo getCurrentVehicleInfo() {
        return vehicleInfo;
    }

    /*
     * Button Click methods
     */

    public void toggleFullScreen(View view) {
        if (isFullscreen) {
            showSystemUI();
            ((FloatingActionButton) view).setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_fullscreen));
            this.googleMap.setPadding(0, 0, 0, 380);
        } else {
            hideSystemUI();
            ((FloatingActionButton) view).setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_fullscreen_exit));
            this.googleMap.setPadding(0, 0, 0, 0);
        }
        isFullscreen = !isFullscreen;

      /*  if (mapAnimator != null) {
            mapAnimator.setupBounds();
        }*/
    }

    public void showCurrentVehiclePosition(View view) {
        if (mapAnimator != null && mapAnimator.routePoints != null && mapAnimator.routePoints.size() > 0) {
            /*mapAnimator.showCurrentVehiclePosition(mapAnimator.routePoints.get(mapAnimator.routePoints.size()-1));*/
            mapAnimator.showCurrentVehiclePosition();
        }
    }

    public void setNoData() {
        // No need to show an error snack bar message
        if (mapAnimator != null)
            mapAnimator.stopAnimation();
        //clearMap();
        setProgressBarViewState(Boolean.FALSE);
    }

    @Override
    public void updateAddressInformation(@NonNull String resolvedAddress, double lat, double lng) {
        setProgressBarViewState(Boolean.FALSE);
        if (activityViewModel != null) {
            activityViewModel.updateNewAddress(resolvedAddress, lat, lng);
        }
    }

    private void clearMap() {
        if (mapAnimator != null) {
            timeLineSeekBar.setProgress(0);
            routeAnimationToggle.setChecked(false);
            mapAnimator.clearMap();
            String speed = MapAnimator.NORMAL;
            if (mapAnimator != null) {
                speed = mapAnimator.mSpeedMultiplier;
            }
            mapAnimator = null;
            mapAnimator = new MapAnimator(
                    googleMap,
                    vehicleInfo.getVehicleType(),
                    this,
                    vehicleInfo.isRouteRequired(),
                    vehicleInfo.getLastUpdatedData().getVehicleMode(),
                    timeLineSeekBar,
                    vehicleInfo.getLastUpdatedData().getSourceDate(),
                    textViewCurrentSpeed,
                    routeAnimationToggle

            );
            mapAnimator.mSpeedMultiplier = speed;
        }
    }

    public void navigateTripDetails(View view) {
        cancelAllOpenStreetMapsRequest();
        Bundle bundle = new Bundle();
        Calendar calendar = getCurrentCalender();
        if (!isSameDay(new Date(), calendar.getTime())) {
            bundle.putLong(KEY_DATE_SELECTED, calendar.getTimeInMillis());
        }
        bundle.putParcelable(BUNDLE_KEY_VEHICLE_DETAIL, vehicleInfo);
        NavigationHelper.navigateToNewActivity(this, TripDetailsActivity.class, bundle);
    }

    public void playBack(View view) {

        hideplaybuttons();

        if (mapAnimator != null && customMapPoints != null && customMapPoints.size() > 0) {
            setProgressBarViewState(Boolean.TRUE);
            shouldShowCurrentLocation = Boolean.FALSE;
            mapAnimator.stopAnimation();
            //clearMap();
            startAnimationThread(customMapPoints, isFromHome(),0);
        }
    }

    public Calendar getCurrentCalender() {
        return startTimeCalendar;
    }

    private void showTimeRange() {
        RangeTimePickerDialog dialog = new RangeTimePickerDialog();
        dialog.setTextBtnNegative("Cancel");
        dialog.setTextBtnPositive("Ok");
        dialog.newInstance(R.color.colorPrimary, R.color.colorWhite, R.color.colorWhite, R.color.colorPrimaryDark, false);
        dialog.setRadiusDialog(10);
        dialog.show(getFragmentManager(), TIME_RANGE_PICKER_TAG);
    }

    private void stopObservingDeviceDataList() {
        if (currentDateDeviceDetail != null) {
            currentDateDeviceDetail.removeObservers(this);
            currentDateDeviceDetail = null;
        }
    }

    public void setCurrentSelectedCalendar(boolean isDateChanged, Calendar selectedCalendar) {
        startTimeCalendar = selectedCalendar;
        if (!isDateChanged) {
            timeInputEditText.setText("");
            endTimeCalendar = null;
            setupDateTextView();
        }
        setProgressBarViewState(Boolean.TRUE);
        mapAnimator.stopAnimation();

        toggleAutoRefresh();
        clearMap();
        mapAnimator.setRouteAnimationRequired(false);
        setupAddress();
        startObservingDeviceDataList();
    }

    private void setProgressBarViewState(boolean isVisible) {
        runOnUiThread(
                () -> {
                    ProgressBar progressBar = findViewById(R.id.mapLoadingProgressBar);
                    progressBar.setVisibility(isVisible ? View.VISIBLE : View.GONE);
                }
        );
    }

    @Override
    public void onSelectedTime(int hourStart, int minuteStart, int hourEnd, int minuteEnd) {
        getStartTimeCalendar().setTimeInMillis(DateTimeHelper.getUpdatedDate(getStartTimeCalendar().getTimeInMillis(), hourStart, minuteStart));
        getEndTimeCalendar().setTimeInMillis(DateTimeHelper.getUpdatedDate(getStartTimeCalendar().getTimeInMillis(), hourEnd, minuteEnd));
        timeInputEditText.setText(DateTimeHelper.getDateRange(getStartTimeCalendar().getTimeInMillis(), getEndTimeCalendar().getTimeInMillis()));
        shouldShowCurrentLocation = Boolean.FALSE;
        setCurrentSelectedCalendar(true, getStartTimeCalendar());
    }


    private void getUserVehicleList() {

        if(getIntent() != null && getIntent().hasExtra(NavigationHelper.KEY_NAVIGATION_DATA_BUNDLE)) {

            Bundle data = getIntent().getBundleExtra(NavigationHelper.KEY_NAVIGATION_DATA_BUNDLE);
            if(data != null && data.containsKey(HomeActivity.USER_VEHICLE_LIST)) {
                ArrayList<VehicleInfo> vehiclesArrayList = data.getParcelableArrayList(HomeActivity.USER_VEHICLE_LIST);
                if(vehiclesArrayList != null && vehiclesArrayList.size() >0)
                    vehicleInfo = vehiclesArrayList.get(0);
            }

        }
    }

    private boolean isFromHome() {

        if(getIntent() != null && getIntent().hasExtra(NavigationHelper.KEY_NAVIGATION_DATA_BUNDLE)){

            Bundle data = getIntent().getBundleExtra(NavigationHelper.KEY_NAVIGATION_DATA_BUNDLE);

            if(data != null && data.containsKey(VehicleListActivity.IS_FROM_HOMEPAGE) && data.getBoolean(VehicleListActivity.IS_FROM_HOMEPAGE,false))
                return true;
            else return false;


        } else
            return false;
    }

    private void hideViews(){
        vehicleInfoLayout.setVisibility(View.GONE);
        llPlayzoom.setVisibility(View.GONE);
        mFab.setVisibility(View.GONE);
        ivUnidadimg.setVisibility(View.VISIBLE);
        ivHome.setVisibility(View.VISIBLE);
    }

    private void hideplaybuttons(){
        llPlayzoom.setVisibility(View.GONE);
        topseekbar.setVisibility(View.VISIBLE);
        ivUnidadimg.setVisibility(View.VISIBLE);
        rlBootamSeekbar.setVisibility(View.GONE);
        llPlayBottom.setVisibility(View.GONE);
        llPlaybackDtime.setVisibility(View.VISIBLE);
        rlTopLayout.setVisibility(View.GONE);
        startAnimationThread(customMapPoints, isFromHome(),0);
    }


    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        if (isChecked) {
            mapAnimator.pauseRouteDrawing();
        } else {
            if (timeLineSeekBar.getProgress() == 100) {
                replayTimeLine();
            } else {
                mapAnimator.resumeRouteDrawing();
            }
        }
    }
}
