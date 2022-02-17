package com.utracx.view.activity;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.utracx.api.request.calls.VehicleDeviceDetailListRequestCall.BUNDLE_KEY_VEHICLE_DEVICE_DETAIL_LIST;
import static com.utracx.util.ConstantVariables.ONE_DAY_END_MILLISECOND;
import static com.utracx.util.ConstantVariables.REMOTE_MESSAGE_BUNDLE_KEY;
import static com.utracx.util.ConstantVariables.STATE_DATA_LOADED;
import static com.utracx.util.ConstantVariables.STATE_LOADING_DATA;
import static com.utracx.util.ConstantVariables.STATE_NO_DATA;
import static com.utracx.util.helper.DateTimeHelper.getStartTimeOfDay;
import static com.utracx.util.helper.NavigationHelper.KEY_NAVIGATION_DATA_BUNDLE;
import static com.utracx.util.helper.ResourceHelper.getVehicleIconResource;
import static com.utracx.util.helper.SharedPreferencesHelper.getUserEmail;
import static com.utracx.util.helper.StatusBarHelper.setupStatusBarWithToolbar;
import static com.utracx.view.activity.HomeActivity.BUNDLE_KEY_VEHICLE_DETAIL;

import android.annotation.SuppressLint;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.messaging.RemoteMessage;
import com.utracx.R;
import com.utracx.api.model.rest.device_data.DeviceData;
import com.utracx.api.model.rest.vehicle_list.LastUpdatedData;
import com.utracx.api.model.rest.vehicle_list.VehicleInfo;
import com.utracx.api.request.ApiUtils;
import com.utracx.api.request.calls.VehicleDeviceDetailListRequestCall;
import com.utracx.api.request.interfaces.VehicleDeviceDetailListCallback;
import com.utracx.background.AddressTask;
import com.utracx.database.datamodel.UserDataEntity;
import com.utracx.view.adapter.AlertAdapter;
import com.utracx.view.listener.DateButtonClickListener;
import com.utracx.view.listener.NextDateButtonClickListener;
import com.utracx.view.listener.PreviousDateButtonClickListener;
import com.utracx.viewmodel.AlertListActivityViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.Executors;

public class AlertListActivity extends AppCompatActivity implements VehicleDeviceDetailListCallback, AddressTask.GeoAddressUpdateCallback {

    private static final String TAG = "AlertListActivity";
    private final VehicleDeviceDetailListRequestCall alertsListRequestCall = new VehicleDeviceDetailListRequestCall(this);
    //Updated on Date selection
    private TextView textViewSelectedDate;
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd - MMM - yyyy", Locale.getDefault());
    private Calendar selectedCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

    //Dynamic Views -- Updated on Database/REST query result
    private RecyclerView alertsRecyclerView;
    private TextView textViewNoData;
    private TextView textViewLoading;
    private View pgsBar;
    private TextView textViewVehicleRegNo;
    private TextView textViewAlerts01;
    private TextView textViewAlerts02;
    private TextView textViewAlerts03;
    private TextView textViewAlerts04;
    private TextView textViewAlerts05;
    private TextView textViewAlertTotal;
    private View refreshButton;
    private LinearLayout alertTypeLayout;
    private AlertListActivityViewModel activityViewModel;
    //private LiveData<PagedList<DeviceData>> currentDateDeviceDetail;
    private LiveData<List<DeviceData>> currentDateDeviceDetail;
    private AlertAdapter alertsAdapter;
    private VehicleInfo vehicleInfo;
    private ImageView imageViewVehicleType;
    private boolean fetchAPICompleted = false;

    private boolean loading = true;
    int pastVisiblesItems, visibleItemCount, totalItemCount;
    private TextView tvLoadMore;
    int count;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_list);
        setupStatusBarWithToolbar(AlertListActivity.this);

        initViewModel();
        initViewElements();
        setupTimeTextView();
        loadIntentData();
        setupRecyclerView();

        loadVehicleData();
        startObservingAlertsData();
        fetchDeviceData();

    }

    private void loadVehicleData() {
        if (vehicleInfo != null) {
            //registration number
            if (vehicleInfo.getVehicleRegistration() != null && !vehicleInfo.getVehicleRegistration().isEmpty()) {
                textViewVehicleRegNo.setText(vehicleInfo.getVehicleRegistration());
            }

            if (vehicleInfo.getVehicleType() != null && !vehicleInfo.getVehicleType().isEmpty()) {
                imageViewVehicleType.setImageDrawable(
                        getVehicleIconResource(this, vehicleInfo.getVehicleType())
                );
            }

            //Counts
            setupCountTextView(textViewAlerts01, vehicleInfo.getMainPowerRemovalAlertCount(), 1000);

            setupCountTextView(textViewAlerts02, vehicleInfo.getWireCutAlertCount(), 1000);

            setupCountTextView(textViewAlerts03, vehicleInfo.getEmergencyAlertCount(), 1000);

            setupCountTextView(textViewAlerts04, vehicleInfo.getOverspeedAlertCount(), 1000);
            setupCountTextView(textViewAlerts05, vehicleInfo.getOthersAlertCount(), 1000);

            setupCountTextView(
                    textViewAlertTotal,
                    vehicleInfo.getMainPowerRemovalAlertCount()
                            + vehicleInfo.getWireCutAlertCount()
                            + vehicleInfo.getEmergencyAlertCount()
                            + vehicleInfo.getOverspeedAlertCount()
                            + vehicleInfo.getOthersAlertCount(),
                    100
            );
        }
    }

    @SuppressLint("SetTextI18n")
    private void setupCountTextView(TextView textView, int count, int threshold) {

        if (count > 0) {
            textView.setVisibility(VISIBLE);
            if (count < threshold) {
                textView.setText(String.valueOf(count));
            } else {
                textView.setText(threshold == 1000 ? "999+" : "99+");
            }
        } else {
            textView.setVisibility(GONE);
            textView.setText("");
        }

        this.count = count;

            if (count < 20 || count == alertsRecyclerView.getLayoutManager().getItemCount())
                tvLoadMore.setVisibility(GONE);
            else tvLoadMore.setVisibility(VISIBLE);
        }

    private void initViewModel() {
        activityViewModel = new ViewModelProvider(this).get(AlertListActivityViewModel.class);
    }

    private void setupTimeTextView() {
        textViewSelectedDate.setText(simpleDateFormat.format(getCurrentCalender().getTime()));
    }

    private void initViewElements() {
        refreshButton = findViewById(R.id.imageView20);
        alertTypeLayout = findViewById(R.id.alert_type_view);
        textViewVehicleRegNo = findViewById(R.id.textViewVehicleRegNo);
        imageViewVehicleType = findViewById(R.id.imageViewVehicleType);
        textViewAlerts01 = findViewById(R.id.textViewAlerts01);
        textViewAlerts02 = findViewById(R.id.textViewAlerts02);
        textViewAlerts03 = findViewById(R.id.textViewAlerts03);
        textViewAlerts04 = findViewById(R.id.textViewAlerts04);
        textViewAlerts05 = findViewById(R.id.textOther);
        textViewAlertTotal = findViewById(R.id.textViewAlertTotal);
        textViewSelectedDate = findViewById(R.id.textViewItemDetailsDate);
        tvLoadMore = findViewById(R.id.tv_load_more);


        pgsBar = findViewById(R.id.pBar);
        alertsRecyclerView = findViewById(R.id.recyclerViewAlertVehicles);
        textViewLoading = findViewById(R.id.tv_LoadingMessage);
        textViewNoData = findViewById(R.id.tv_NoDataMessage);
        textViewSelectedDate.setOnClickListener(new DateButtonClickListener(this));


        findViewById(R.id.imageView12).setOnClickListener(new NextDateButtonClickListener(this));
        findViewById(R.id.imageView11).setOnClickListener(new PreviousDateButtonClickListener(this));
        tvLoadMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvLoadMore.setVisibility(GONE);
                new Handler().postDelayed(() -> {
                    pgsBar.setVisibility(GONE);
                    alertsAdapter.setTotalVisibleCount(alertsRecyclerView.getLayoutManager().getItemCount());
                    alertsAdapter.notifyDataSetChanged();
                }, 1000);
                pgsBar.setVisibility(VISIBLE);

            }
        });

    }


    private void setupRecyclerView() {
        LinearLayoutManager layoutManagerService = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        alertsRecyclerView.setLayoutManager(layoutManagerService);

        if (vehicleInfo == null) {
            alertsAdapter = new AlertAdapter();
            alertsAdapter.AlertAdapterValues(this, this, Boolean.FALSE);
        } else {
            alertsAdapter = new AlertAdapter();
            alertsAdapter.AlertAdapterValues(this, this, vehicleInfo.getUseGoogleApi());
        }

        alertsRecyclerView.setHasFixedSize(true);
        DividerItemDecoration itemDecoration = new DividerItemDecoration(alertsRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
        itemDecoration.setDrawable(new ColorDrawable(ContextCompat.getColor(AlertListActivity.this, R.color.colorDescription)));


        alertsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) { //check for scroll down
                    visibleItemCount = layoutManagerService.getChildCount();
                    totalItemCount = layoutManagerService.getItemCount();
                    pastVisiblesItems = layoutManagerService.findFirstVisibleItemPosition();

                    if (loading) {
                        if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                            loading = false;
                            Log.v("...", "Last Item Wow !");
                            // Do pagination.. i.e. fetch new data

                            new Handler().postDelayed(() -> {
                                pgsBar.setVisibility(GONE);
                                if (count < 20 || count == alertsRecyclerView.getLayoutManager().getItemCount())
                                    tvLoadMore.setVisibility(GONE);
                                else tvLoadMore.setVisibility(VISIBLE);
                            },1000);

                            loading = true;
                        }
                    }
                }
            }
        });


        alertsRecyclerView.setAdapter(alertsAdapter);

    }

    private void loadIntentData() {
        Bundle dataBundle = getIntent().getExtras();
        if (dataBundle != null) {
            if (dataBundle.containsKey(KEY_NAVIGATION_DATA_BUNDLE)
                    && dataBundle.getBundle(KEY_NAVIGATION_DATA_BUNDLE) != null) {
                Bundle innerBundle = dataBundle.getBundle(KEY_NAVIGATION_DATA_BUNDLE);

                if (innerBundle != null && innerBundle.containsKey(BUNDLE_KEY_VEHICLE_DETAIL)) {
                    VehicleInfo vehicleDetail = innerBundle.getParcelable(BUNDLE_KEY_VEHICLE_DETAIL);
                    if (vehicleDetail != null) {
                        this.vehicleInfo = vehicleDetail;
                    }
                }
            } else if (dataBundle.containsKey(REMOTE_MESSAGE_BUNDLE_KEY)) {
                processNotificationData(dataBundle.getParcelable(REMOTE_MESSAGE_BUNDLE_KEY));
            }
        }
    }

    private void processNotificationData(RemoteMessage remoteMessage) {
        if (null != remoteMessage) {

            this.vehicleInfo = new VehicleInfo();
            this.vehicleInfo.setLastUpdatedData(new LastUpdatedData());

            if (remoteMessage.getData().containsKey("vehicle_type")
                    && remoteMessage.getData().get("vehicle_type") != null) {
                try {
                    this.vehicleInfo.setVehicleType(
                            Objects.requireNonNull(remoteMessage.getData().get("vehicle_type"))
                    );
                } catch (NumberFormatException e) {
                    Log.e(TAG, "loadIntentData: ", e);
                }
            }

            if (remoteMessage.getNotification() != null
                    && remoteMessage.getNotification().getTitle() != null) {
                this.vehicleInfo.setVehicleRegistration(
                        remoteMessage.getNotification().getTitle()
                );
            }

            if (remoteMessage.getData().containsKey("serial_no")) {
                this.vehicleInfo
                        .getLastUpdatedData()
                        .setSerialNumber(remoteMessage.getData().get("serial_no"));
            }

            if (remoteMessage.getData().containsKey("source_date")
                    && remoteMessage.getData().get("source_date") != null) {
                try {
                    this.vehicleInfo.getLastUpdatedData().setSourceDate(
                            Long.parseLong(Objects.requireNonNull(remoteMessage.getData().get("source_date")))
                    );
                } catch (NumberFormatException e) {
                    Log.e(TAG, "loadIntentData: ", e);
                }
            }

            if (remoteMessage.getData().containsKey("emergency_alert_count")
                    && remoteMessage.getData().get("emergency_alert_count") != null) {
                try {
                    this.vehicleInfo.setEmergencyAlertCount(
                            Integer.parseInt(Objects.requireNonNull(remoteMessage.getData().get("emergency_alert_count")))
                    );
                } catch (NumberFormatException e) {
                    Log.e(TAG, "loadIntentData: ", e);
                }
            }

            if (remoteMessage.getData().containsKey("overspeed_alert_count")
                    && remoteMessage.getData().get("overspeed_alert_count") != null) {
                try {
                    this.vehicleInfo.setOverspeedAlertCount(
                            Integer.parseInt(Objects.requireNonNull(remoteMessage.getData().get("overspeed_alert_count")))
                    );
                } catch (NumberFormatException e) {
                    Log.e(TAG, "loadIntentData: ", e);
                }
            }

            if (remoteMessage.getData().containsKey("wire_cut_alert_count")
                    && remoteMessage.getData().get("wire_cut_alert_count") != null) {
                try {
                    this.vehicleInfo.setWireCutAlertCount(
                            Integer.parseInt(Objects.requireNonNull(remoteMessage.getData().get("wire_cut_alert_count")))
                    );
                } catch (NumberFormatException e) {
                    Log.e(TAG, "loadIntentData: ", e);
                }
            }

            if (remoteMessage.getData().containsKey("main_power_removal_alert_count")
                    && remoteMessage.getData().get("main_power_removal_alert_count") != null) {
                try {
                    this.vehicleInfo.setWireCutAlertCount(
                            Integer.parseInt(Objects.requireNonNull(remoteMessage.getData().get("main_power_removal_alert_count")))
                    );
                } catch (NumberFormatException e) {
                    Log.e(TAG, "loadIntentData: ", e);
                }
            }
        } else {
            finish();
        }
    }

    public Calendar getCurrentCalender() {
        return selectedCalendar;
    }

    public void setCurrentSelectedCalendar(Calendar selectedCalendar) {
        fetchAPICompleted = Boolean.FALSE;
        this.selectedCalendar = selectedCalendar;
        setupTimeTextView();
        stopObservingDeviceDataList();
        startObservingAlertsData();
    }

    private void stopObservingDeviceDataList() {
        if (currentDateDeviceDetail != null) {
            currentDateDeviceDetail.removeObservers(this);
        }
    }

    private void startObservingAlertsData() {

        totalItemCount = alertsRecyclerView.getLayoutManager().getItemCount();

        if (activityViewModel != null && vehicleInfo != null && vehicleInfo.getLastUpdatedData() != null
                && vehicleInfo.getLastUpdatedData().getSerialNumber() != null
                && !vehicleInfo.getLastUpdatedData().getSerialNumber().isEmpty()) {
            long startOfDay = getStartTimeOfDay(selectedCalendar.getTimeInMillis());
            currentDateDeviceDetail = activityViewModel.getLiveAlertsForDevice(
                    startOfDay,
                    startOfDay + ONE_DAY_END_MILLISECOND,
                    vehicleInfo.getLastUpdatedData().getSerialNumber()
            );


            currentDateDeviceDetail.observe(
                    this,
                    alertList -> {
                        if (alertList != null && !alertList.isEmpty()) {
                            setupPlaceHolderViews(STATE_DATA_LOADED);
                            alertsAdapter.updateAlertsList(alertList);
                            calculateAlertCount(alertList);
                        }
                        else {
                            // Updated data from the API
                            if (fetchAPICompleted) {
                                setupPlaceHolderViews(STATE_NO_DATA);
                            } else {
                                setupPlaceHolderViews(STATE_LOADING_DATA);
                                fetchDeviceData();
                            }
                        }
                    }
            );

        }
    }

    private void calculateAlertCount(List<DeviceData> alertList) {
        Executors.newSingleThreadExecutor().execute(
                () -> {
                    DeviceData temp;
                    int overSpeed = 0;
                    int mainPower = 0;
                    int wireCut = 0;
                    int emergencyButton = 0;
                    int others = 0;
                    for (int i = 0; i < alertList.size(); i++) {
                        temp = alertList.get(i);
                        if (temp.getD().getPacketType().toLowerCase().contains("overspeed")) {
                            overSpeed++;
                        } else if (temp.getD().getPacketType().toLowerCase().contains("wire")) {
                            wireCut++;
                        } else if (temp.getD().getPacketType().toLowerCase().contains("main")) {
                            mainPower++;
                        } else if (temp.getD().getPacketType().toLowerCase().contains("emergency")) {
                            emergencyButton++;
                        } else {
                            others++;
                        }
                    }

                    vehicleInfo.setMainPowerRemovalAlertCount(mainPower);
                    vehicleInfo.setWireCutAlertCount(wireCut);
                    vehicleInfo.setEmergencyAlertCount(emergencyButton);
                    vehicleInfo.setOverspeedAlertCount(overSpeed);
                    vehicleInfo.setOthersCount(others);
                    runOnUiThread(this::loadVehicleData);
                }
        );
    }

    private void fetchDeviceData() {
        Executors.newSingleThreadExecutor().execute(
                () -> {
                    String startTime = getStartTimeOfDay(selectedCalendar.getTimeInMillis()) + "";
                    String endTime = getStartTimeOfDay(selectedCalendar.getTimeInMillis()) + ONE_DAY_END_MILLISECOND + "";

                    if (!startTime.isEmpty() && !endTime.isEmpty()) {

                        UserDataEntity userData = activityViewModel.fetchUserDataByEmail(getUserEmail(this));
                        if (userData != null && vehicleInfo != null
                                && vehicleInfo.getLastUpdatedData() != null
                                && vehicleInfo.getLastUpdatedData().getSerialNumber() != null
                                && !vehicleInfo.getLastUpdatedData().getSerialNumber().isEmpty()) {
                            ApiUtils.getInstance().getSOService()
                                    .getAlertsForDevice(
                                            startTime,
                                            endTime,
                                            userData.getUsername(),
                                            userData.getPassword(),
                                            vehicleInfo.getLastUpdatedData().getSerialNumber()
                                    )
                                    .enqueue(alertsListRequestCall);
                        }


                    }
                }
        );
    }

    private void setupPlaceHolderViews(int state) {
        switch (state) {
            case STATE_LOADING_DATA:
                pgsBar.setVisibility(VISIBLE);
                textViewLoading.setVisibility(VISIBLE);
                textViewNoData.setVisibility(GONE);
                alertsRecyclerView.setVisibility(GONE);
                alertTypeLayout.setVisibility(GONE);
                textViewAlertTotal.setVisibility(GONE);
                break;
            case STATE_DATA_LOADED:
                textViewLoading.setVisibility(GONE);
                textViewNoData.setVisibility(GONE);
                pgsBar.setVisibility(GONE);
                alertsRecyclerView.setVisibility(VISIBLE);
                alertTypeLayout.setVisibility(VISIBLE);
                textViewAlertTotal.setVisibility(VISIBLE);
                break;
            case STATE_NO_DATA:
                textViewNoData.setVisibility(VISIBLE);
                textViewLoading.setVisibility(GONE);
                pgsBar.setVisibility(GONE);
                alertsRecyclerView.setVisibility(GONE);
                alertTypeLayout.setVisibility(GONE);
                textViewAlertTotal.setVisibility(GONE);
                break;
            default:
                Log.e(TAG, "setupPlaceHolderViews: Invalid state");
                break;
        }
    }

    @Override
    public void onVehicleDeviceDetailsListFetched(Bundle dataBundle) {
        if (!fetchAPICompleted) {
            fetchAPICompleted = true;
        }
        refreshButton.clearAnimation();

        if (dataBundle != null && dataBundle.containsKey(BUNDLE_KEY_VEHICLE_DEVICE_DETAIL_LIST)) {
            List<DeviceData> deviceDetails = dataBundle.getParcelableArrayList(BUNDLE_KEY_VEHICLE_DEVICE_DETAIL_LIST);
            if (deviceDetails != null && !deviceDetails.isEmpty()) {
                Log.d(TAG, "onVehicleDeviceDetailsListFetched: new Device data list obtained ");
                activityViewModel.updateAlerts(deviceDetails);
                return;
            }
        }

        setupPlaceHolderViews(STATE_NO_DATA);
    }

    @Override
    public void onVehicleDeviceDetailsListFetchFailed(Bundle data) {
        setupPlaceHolderViews(STATE_NO_DATA);
        refreshButton.clearAnimation();
    }

    @Override
    public void onVehicleDeviceDetailsListFetchError(@Nullable Bundle data) {
        setupPlaceHolderViews(STATE_NO_DATA);
    }

    public void refreshAlerts(View view) {
        setupPlaceHolderViews(STATE_LOADING_DATA);
        fetchAPICompleted = false;
        fetchDeviceData();
    }

    public void onBackPressed(View view) {
        finish();
    }

    @Override
    public void updateAddressInformation(@NonNull String resolvedAddress, double lat, double lng) {
        if (activityViewModel != null) {
            activityViewModel.updateNewAddress(resolvedAddress, lat, lng);
        }
    }
}