package com.utracx.view.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.romainpiel.shimmer.Shimmer;
import com.romainpiel.shimmer.ShimmerTextView;
import com.utracx.R;
import com.utracx.api.model.rest.device_data.DeviceData;
import com.utracx.api.model.rest.vehicle_list.VehicleInfo;
import com.utracx.api.request.ApiUtils;
import com.utracx.api.request.calls.SerialNumberRequestCall;
import com.utracx.api.request.calls.VehicleDeviceDetailListRequestCall;
import com.utracx.background.DeviceDataCalculationTask;
import com.utracx.background.AddressTask;
import com.utracx.background.TripCalculationTask;
import com.utracx.database.datamodel.UserDataEntity;
import com.utracx.util.helper.MapHelper;
import com.utracx.view.adapter.DeviceDataAdapter;
import com.utracx.view.listener.DateButtonClickListener;
import com.utracx.view.listener.MapButtonClickListener;
import com.utracx.view.listener.NextDateButtonClickListener;
import com.utracx.view.listener.PreviousDateButtonClickListener;
import com.utracx.view.listener.TripDetailsCallback;
import com.utracx.viewmodel.TripDetailsActivityViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.utracx.api.request.ApiUtils.cancelAllOpenStreetMapsRequest;
import static com.utracx.api.request.ApiUtils.cancelAllSOSRequest;
import static com.utracx.util.ConstantVariables.KEY_DATE_SELECTED;
import static com.utracx.util.ConstantVariables.ONE_DAY_END_MILLISECOND;
import static com.utracx.util.ConstantVariables.STATE_DATA_LOADED;
import static com.utracx.util.ConstantVariables.STATE_LOADING_DATA;
import static com.utracx.util.ConstantVariables.STATE_NO_DATA;
import static com.utracx.util.ConstantVariables.THRESHOLD_TIME_FOR_NON_COMM_VEHICLE;
import static com.utracx.util.helper.DateTimeHelper.getEndTimeOfDay;
import static com.utracx.util.helper.DateTimeHelper.getStartTimeOfDay;
import static com.utracx.util.helper.DateTimeHelper.isSameDay;
import static com.utracx.util.helper.MathHelper.round;
import static com.utracx.util.helper.NavigationHelper.KEY_NAVIGATION_DATA_BUNDLE;
import static com.utracx.util.helper.ResourceHelper.getGSMStrengthIconResource;
import static com.utracx.util.helper.ResourceHelper.getVehicleIconResource;
import static com.utracx.util.helper.ResourceHelper.getVehicleModeBackground;
import static com.utracx.util.helper.SharedPreferencesHelper.getUserEmail;
import static com.utracx.util.helper.StatusBarHelper.setupStatusBarWithToolbar;
import static com.utracx.view.activity.HomeActivity.BUNDLE_KEY_VEHICLE_DETAIL;
import static java.lang.String.valueOf;

public class TripDetailsActivity extends BaseRefreshActivity
        implements BaseRefreshActivity.HandlerTimerCallback {

    private static final String TAG = "TripDetailsActivity";
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd - MMM - yyyy", Locale.getDefault());
    //Updated on Date selection
    private Calendar selectedCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    //Normal Views -- Updated on screen load
    private TextView textViewVehicleRegNo;
    private RecyclerView recyclerViewDeviceDetails;
    //Dynamic Views -- Updated on each packet
    private ImageView imageViewVehicleType;
    private TextView txtIgnition;
    private TextView txtStatus;
    private ImageView imageViewGSM;
    private TextView textViewTotalDistance;
    private TextView textViewMaxSpeed;
    private TextView textViewCurrentSpeed;
    private TextView textViewAvgSpeed;
    private ShimmerTextView textViewLatestAddress;
    private TextView textViewVehicleDate;
    private View vehicleImageContainerLayout;
    //Dynamic Views -- Updated on user Action
    private TextView textViewSelectedDate;
    //Dynamic Views -- Updated on Database/REST query result
    private TextView textViewNoData;
    private TextView textViewLoading;
    private TextView txtIMEI;
    private View btnMapActivity;
    private View pgsBar;
    private LiveData<List<DeviceData>> currentDateDeviceDetail;
    private VehicleInfo vehicleInfo;
    private TripDetailsActivityViewModel activityViewModel;
    private DeviceDataAdapter tripDetailAdapter;
    private TripDetailsCallback tripDetailsCallback;
    private VehicleDeviceDetailListRequestCall deviceDetailsRequestCall;
    private ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
    private boolean firstAPICallCompleted = false;
    private Shimmer shimmer;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details);
        setupStatusBarWithToolbar(TripDetailsActivity.this);

        initViewModel();
        setupCallBack();
        initViewElements();

        setupPlaceHolderViews(STATE_LOADING_DATA);
        loadIntentData();
        setupTimeTextView();
        loadVehicleDetails();
        setupRecyclerView();
        startObservingVehicleInformation();
        startObservingDeviceDataList();
    }

    @Override
    protected void onResume() {
        toggleAutoRefresh();
        super.onResume();
    }

    @Override
    protected void onStop() {
        cancelAllOpenStreetMapsRequest();
        cancelAllSOSRequest();
        super.onStop();
    }

    @Override
    public void onUpdate() {
        if (vehicleInfo != null && vehicleInfo.getLastUpdatedData() != null && (vehicleInfo.getLastUpdatedData().getSerialNumber() == null || vehicleInfo.getLastUpdatedData().getSerialNumber().isEmpty())) {
            fetchVehicleSerialNumber();
        } else {
            fetchDeviceData();
        }
    }

    private void initViewModel() {
        activityViewModel = new ViewModelProvider(this).get(TripDetailsActivityViewModel.class);
    }

    private void setupCallBack() {
        tripDetailsCallback = new TripDetailsCallback(this, activityViewModel);
        deviceDetailsRequestCall = new VehicleDeviceDetailListRequestCall(new TripCalculationTask(tripDetailsCallback));
    }

    private void initViewElements() {
        pgsBar = findViewById(R.id.pBar);
        vehicleImageContainerLayout = findViewById(R.id.vehicleImageContainerLayout);
        textViewSelectedDate = findViewById(R.id.textViewItemDetailsDate);
        textViewTotalDistance = findViewById(R.id.textView12);
        textViewVehicleRegNo = findViewById(R.id.textViewVehicleRegNo);
        recyclerViewDeviceDetails = findViewById(R.id.recyclerViewTripDetails);
        imageViewVehicleType = findViewById(R.id.imageViewVehicleType);
        txtIgnition = findViewById(R.id.txtIgnition);
        txtStatus = findViewById(R.id.txtStatus);
        imageViewGSM = findViewById(R.id.imageViewSignal);
        textViewLoading = findViewById(R.id.tv_LoadingMessage);
        textViewNoData = findViewById(R.id.tv_NoDataMessage);
        textViewCurrentSpeed = findViewById(R.id.txtCurrentSpeed);
        txtIMEI = findViewById(R.id.txtIMEI);
        textViewMaxSpeed = findViewById(R.id.textView7);
        textViewAvgSpeed = findViewById(R.id.textView3);
        textViewVehicleDate = findViewById(R.id.txtVehicleDate);
        textViewLatestAddress = findViewById(R.id.textViewVehicleAddress);
        shimmer = new Shimmer();
        btnMapActivity = findViewById(R.id.btn_MapActivity);
        btnMapActivity.setOnClickListener(new MapButtonClickListener(this));

        findViewById(R.id.imageView12).setOnClickListener(new NextDateButtonClickListener(this));
        findViewById(R.id.imageView11).setOnClickListener(new PreviousDateButtonClickListener(this));
        textViewSelectedDate.setOnClickListener(new DateButtonClickListener(this));
    }

    private void setupTimeTextView() {
        textViewSelectedDate.setText(simpleDateFormat.format(getCurrentCalender().getTime()));
    }

    private void loadIntentData() {
        Bundle dataBundle = getIntent().getExtras();
        if (dataBundle != null && dataBundle.containsKey(KEY_NAVIGATION_DATA_BUNDLE)
                && dataBundle.getBundle(KEY_NAVIGATION_DATA_BUNDLE) != null) {
            Bundle innerBundle = dataBundle.getBundle(KEY_NAVIGATION_DATA_BUNDLE);

            if (innerBundle != null && innerBundle.containsKey(BUNDLE_KEY_VEHICLE_DETAIL)) {
                VehicleInfo vehicleDetail = innerBundle.getParcelable(BUNDLE_KEY_VEHICLE_DETAIL);
                if (vehicleDetail != null) {
                    this.vehicleInfo = vehicleDetail;
                    setIMEI();
                }
            }

            if (innerBundle != null && innerBundle.containsKey(KEY_DATE_SELECTED)) {
                long selectedDate = innerBundle.getLong(KEY_DATE_SELECTED, -1L);
                if (selectedDate > 0) {
                    getCurrentCalender().setTimeInMillis(selectedDate);
                    getCurrentCalender().setTimeZone(TimeZone.getTimeZone("UTC"));
                    setupTimeTextView();
                }
            }
        }
    }

    private void loadVehicleDetails() {

        String vehicleType = vehicleInfo.getVehicleType();
        imageViewVehicleType.setImageDrawable(getVehicleIconResource(this, vehicleType));

        textViewVehicleRegNo.setText(
                vehicleInfo
                        .getVehicleRegistration()
                        .replace(".", "")
                        .replace(" ", "")
                        .toUpperCase()
        );
        setVehicleAddress();
        setGPSState();
        setGSMState();
        updateCurrentVehicleMode();
        setIgnitionState();
        updateCurrentSpeed();
        updateDistanceLabel();
    }

    private void setIMEI() {
        if (vehicleInfo.getLastUpdatedData() != null
                && vehicleInfo.getLastUpdatedData().getImeiNo() != null) {
            txtIMEI.setText(vehicleInfo.getLastUpdatedData().getImeiNo());
        } else {
            txtIMEI.setText(getString(R.string.card_imei_no_format));
        }
    }

    private void updateDistanceLabel() {
        if (!vehicleInfo.isRouteRequired()) {
            ((TextView) findViewById(R.id.distanceLabel)).setText(R.string.gps_distance);
        }
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManagerService = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerViewDeviceDetails.setLayoutManager(layoutManagerService);

        tripDetailAdapter = new DeviceDataAdapter(this, tripDetailsCallback, vehicleInfo.getUseGoogleApi());
        recyclerViewDeviceDetails.setAdapter(tripDetailAdapter);

        recyclerViewDeviceDetails.setHasFixedSize(true);
        /*recyclerViewDeviceDetails.addOnScrollListener(
                new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(@NotNull RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        if (dy > 0 && btnMapActivity.getVisibility() == View.VISIBLE) {
                            btnMapActivity.setVisibility(GONE);
                        } else if (dy < 0 && btnMapActivity.getVisibility() != View.VISIBLE) {
                            btnMapActivity.setVisibility(VISIBLE);
                        }
                    }
                }
        );*/
    }

    private void startObservingVehicleInformation() {
        if (activityViewModel != null && vehicleInfo != null && !vehicleInfo.getId().isEmpty()) {
            activityViewModel.getLiveVehicleInfo(vehicleInfo.getId()).observe(this, updatedVehicleInfo -> {
                if (updatedVehicleInfo != null) {
                    //Previously the serial number of the vehicle was null and
                    // the API call fetched the details and updated the Database
                    //Case - Loading a vehicle for the first time
                    this.vehicleInfo = updatedVehicleInfo;
                    updateVehicleInformation();
                }
            });
        }
    }

    private void startObservingDeviceDataList() {
        if (activityViewModel != null && vehicleInfo != null && vehicleInfo.getLastUpdatedData() != null && vehicleInfo.getLastUpdatedData().getSerialNumber() != null
                && !vehicleInfo.getLastUpdatedData().getSerialNumber().isEmpty()) {
            stopObservingDeviceDataList();
            long startOfDay = getStartTimeOfDay(getCurrentCalender().getTimeInMillis());
            currentDateDeviceDetail = activityViewModel.getLiveDeviceDataList(vehicleInfo.getLastUpdatedData().getSerialNumber(), startOfDay, startOfDay + ONE_DAY_END_MILLISECOND);
            currentDateDeviceDetail
                    .observe(
                            this,
                            deviceDataList -> {
                                if (deviceDataList != null && !deviceDataList.isEmpty()) {

                                    stopCalculationThreads();

                                    executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
                                    executor.execute(
                                            new DeviceDataCalculationTask(this, deviceDataList, tripDetailAdapter)
                                    );

                                    updateVehicleInformation();
                                }

                                // when database has no entries for an old date fire the API,
                                // for today the API call will automatically fire with each refresh
                                if ((deviceDataList == null || deviceDataList.isEmpty())
                                        && !isSameDay(Calendar.getInstance().getTime(), getCurrentCalender().getTime())) {
                                    onUpdate();
                                }

                                // Updated data from the API
                                if (firstAPICallCompleted) {
                                    setupPlaceHolderViews(STATE_NO_DATA);
                                }

                            }
                    );
        }
    }

    private void updateVehicleInformation() {
        // DB updated with new data from API, update the other fields
        updateMaxSpeed();
        updateAvgSpeed();
        setVehicleAddress();
        setGPSState();
        setGSMState();
        setIgnitionState();
        updateCurrentSpeed();
        updateCurrentVehicleMode();
        updateCurrentVehicleDate();
    }

    private void stopCalculationThreads() {
        //If another thread is live, discard
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
            Log.d(TAG, "startObservingDeviceDataList: existing threads purged");
        }
    }

    private void stopObservingDeviceDataList() {
        if (currentDateDeviceDetail != null) {
            currentDateDeviceDetail.removeObservers(this);
        }
    }

    private void toggleAutoRefresh() {
        super.updateCallbackChild(isSameDay(Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime(), getCurrentCalender().getTime()) ? this : null);
        super.setAutoRefresh(isSameDay(Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime(), getCurrentCalender().getTime()));
    }

    private void setVehicleAddress() {
        if (vehicleInfo.getLastUpdatedData() != null) {
            String address = vehicleInfo.getLastUpdatedData().getAddress();
            if (address == null || address.isEmpty()) {
                textViewLatestAddress.setText(getString(R.string.location_fetching));
                if (!shimmer.isAnimating())
                    shimmer.start(textViewLatestAddress);
                new AddressTask(vehicleInfo.getUseGoogleApi(), vehicleInfo.getLastUpdatedData(), this, tripDetailsCallback, null).run();
                Log.d(TAG, "setVehicleAddress: address is null");
            } else {
                if (shimmer.isAnimating()) {
                    shimmer.cancel();
                }
                textViewLatestAddress.setText(formatAddress(address));
            }
        }
    }

    private String formatAddress(String address) {

        if (address.contains("+")) {
            address = address.substring(address.indexOf(",")+1);
        }

        return address;

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

    private void updateCurrentVehicleMode() {
        if (isSameDay(getCurrentCalender().getTime(), Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime())) {
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
        } else {
            textViewCurrentSpeed.setText(
                    String.format(getString(R.string.card_speed), 0.0));
        }
    }

    private void updateAvgSpeed() {
        if (tripDetailAdapter != null && tripDetailAdapter.getTotalDistance() > 0) {
            Executors.newSingleThreadExecutor().execute(
                    () -> {
                        long startOfDay = getStartTimeOfDay(getCurrentCalender().getTimeInMillis());
                        List<Double> speedList = activityViewModel.getAvgSpeed(vehicleInfo.getLastUpdatedData().getSerialNumber(), startOfDay, startOfDay + ONE_DAY_END_MILLISECOND);
                        if (!speedList.isEmpty()) {
                            double avgSpeed = getAverageSpeed(speedList);
                            runOnUiThread(
                                    () -> textViewAvgSpeed.setText(
                                            getString(R.string.km_per_hr_placeholder, round(avgSpeed, 1))
                                    )
                            );
                        }

                    }
            );
        } else {
            textViewAvgSpeed.setText(getString(R.string.km_per_hr_placeholder, 0.0));
        }
    }

    private double getAverageSpeed(@NonNull List<Double> speedList) {
        double sum = 0.0;
        int i = 0;
        for (; i < speedList.size(); i++) {
            sum += speedList.get(i);
        }
        if (i > 0) {
            return sum / i;
        }
        return sum;
    }

    private void updateMaxSpeed() {
        if (tripDetailAdapter != null && tripDetailAdapter.getTotalDistance() > 0) {
            Executors.newSingleThreadExecutor().execute(
                    () -> {
                        long startOfDay = getStartTimeOfDay(getCurrentCalender().getTimeInMillis());
                        double maxSpeed = activityViewModel.getMaxSpeed(vehicleInfo.getLastUpdatedData().getSerialNumber(), startOfDay, startOfDay + ONE_DAY_END_MILLISECOND);
                        runOnUiThread(() -> textViewMaxSpeed.setText(
                                getString(R.string.km_per_hr_placeholder, round(maxSpeed, 1)))
                        );
                    }
            );
        } else {
            textViewMaxSpeed.setText(getString(R.string.km_per_hr_placeholder, 0.0));
        }
    }

    private void setTotalDistance(@NonNull Double distance) {
        if (textViewTotalDistance != null) {
            textViewTotalDistance.setText(
                    getString(R.string.km_placeholder,
                            valueOf(round(distance, 2)))
            );
        }
    }

    private void resetVehicleData() {
        txtIgnition.setText("");
        txtStatus.setText("");
        imageViewGSM.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_gsm_0, getTheme()));
        textViewLatestAddress.setText(getString(R.string.location_fetching));
        if (!shimmer.isAnimating()) {
            shimmer.start(textViewLatestAddress);
        }
        textViewTotalDistance.setText(
                getString(R.string.km_placeholder,
                        valueOf(round(0.00, 2)))
        );
        textViewMaxSpeed.setText(getString(R.string.km_per_hr_placeholder, 0.0));
        textViewAvgSpeed.setText(getString(R.string.km_per_hr_placeholder, 0.0));
        textViewCurrentSpeed.setText("  ");

    }

    private void fetchVehicleSerialNumber() {
        if (vehicleInfo.getLastUpdatedData().getSerialNumber() == null || vehicleInfo.getLastUpdatedData().getSerialNumber().isEmpty()) {
            Executors.newSingleThreadExecutor().execute(
                    () -> {
                        UserDataEntity userData = activityViewModel.fetUserDataByEmail(getUserEmail(this));
                        if (userData != null) {
                            ApiUtils.getInstance().getSOService()
                                    .getSerialNumber(vehicleInfo.getVehicleRegistration(), userData.getUsername(), userData.getPassword())
                                    .enqueue(new SerialNumberRequestCall(vehicleInfo.getVehicleRegistration(), tripDetailsCallback));
                        }
                    }
            );
        }
    }

    private void fetchDeviceData() {
        Executors.newSingleThreadExecutor().execute(
                () -> {
                    String startTime = getStartTimeOfDay(getCurrentCalender().getTimeInMillis()) + "";
                    String endTime = getEndTimeOfDay(getCurrentCalender().getTimeInMillis()) + "";

                    // Check if DB has packet for the selected date
                    long lastUpdatedTime = getSourceDateFromLastObtainedPacket(getStartTimeOfDay(getCurrentCalender().getTimeInMillis()));
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
        if (vehicleInfo != null && vehicleInfo.getLastUpdatedData() != null && vehicleInfo.getLastUpdatedData().getSerialNumber() != null
                && !vehicleInfo.getLastUpdatedData().getSerialNumber().isEmpty()) {
            return activityViewModel.getLatestDeviceDataUpdateTimestamp(vehicleInfo.getLastUpdatedData().getSerialNumber(), startOfDay, startOfDay + ONE_DAY_END_MILLISECOND);
        }
        return 0L;
    }

    private void clearTripList() {
        tripDetailAdapter.updateDeviceDataList(new ArrayList<>());
    }

    // Begin Public access methods
    public void setupPlaceHolderViews(int state) {
        switch (state) {
            case STATE_LOADING_DATA:
                textViewLoading.setVisibility(VISIBLE);
                textViewNoData.setVisibility(GONE);
                btnMapActivity.setVisibility(GONE);
                pgsBar.setVisibility(VISIBLE);
                recyclerViewDeviceDetails.setVisibility(GONE);
                break;
            case STATE_DATA_LOADED:
                textViewLoading.setVisibility(GONE);
                textViewNoData.setVisibility(GONE);
                btnMapActivity.setVisibility(GONE);
                recyclerViewDeviceDetails.setVisibility(VISIBLE);
                pgsBar.setVisibility(GONE);
                break;
            case STATE_NO_DATA:
                textViewLoading.setVisibility(GONE);
                textViewNoData.setVisibility(VISIBLE);
                recyclerViewDeviceDetails.setVisibility(GONE);
                btnMapActivity.setVisibility(GONE);
                pgsBar.setVisibility(GONE);
                break;

            default:
                Log.e(TAG, "setupPlaceHolderViews: Invalid view state");
                break;
        }
    }

    public VehicleInfo getCurrentVehicleInfo() {
        return vehicleInfo;
    }

    public Calendar getCurrentCalender() {
        return selectedCalendar;
    }

    public void setCurrentSelectedCalendar(Calendar selectedCalendar) {
        stopObservingDeviceDataList();
        stopCalculationThreads();
        clearTripList();

        this.selectedCalendar = selectedCalendar;
        setupTimeTextView();
        setupPlaceHolderViews(STATE_LOADING_DATA);
        toggleAutoRefresh();
        startObservingDeviceDataList();
        resetVehicleData();
    }

    public void showFailureMessage(String message) {
        Log.d(TAG, "showFailureMessage: message - " + message);
        setupPlaceHolderViews(STATE_NO_DATA);
    }

    public void setNoData() {
        // Updated data from the API
        firstAPICallCompleted = true;
        setupPlaceHolderViews(STATE_NO_DATA);
    }

    public void updateLatestData(@NonNull DeviceData deviceData, double distance) {
        vehicleInfo.setLastUpdatedData(deviceData.getD());
        updateVehicleInformation();
        setTotalDistance(distance);
    }
}