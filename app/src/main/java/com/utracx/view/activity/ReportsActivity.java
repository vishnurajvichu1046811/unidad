package com.utracx.view.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.utracx.R;
import com.utracx.api.model.rest.device_data.DeviceData;
import com.utracx.api.model.rest.search.Datum;
import com.utracx.api.request.ApiUtils;
import com.utracx.api.request.calls.VehicleDeviceDetailListRequestCall;
import com.utracx.api.request.calls.VehicleSearchRequestCall;
import com.utracx.api.request.interfaces.VehicleResponseCallback;
import com.utracx.background.DeviceDataCalculationTask;
import com.utracx.background.AddressTask;
import com.utracx.background.TripCalculationTask;
import com.utracx.database.datamodel.UserDataEntity;
import com.utracx.model.PDFUtility;
import com.utracx.util.helper.KeyBoardHelper;
import com.utracx.util.helper.MapHelper;
import com.utracx.util.helper.TextInputAutoCompleteTextView;
import com.utracx.view.adapter.AutoSuggestVehicleAdapter;
import com.utracx.view.adapter.data.TripSummary;
import com.utracx.view.listener.DateButtonClickListener;
import com.utracx.view.listener.TripDetailsCallback;
import com.utracx.viewmodel.ReportActivityViewModel;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.utracx.api.request.ApiUtils.cancelAllOpenStreetMapsRequest;
import static com.utracx.api.request.calls.VehicleSearchRequestCall.BUNDLE_KEY_RESPONSE_DATA;
import static com.utracx.util.ConstantVariables.ONE_DAY_END_MILLISECOND;
import static com.utracx.util.ConstantVariables.VEHICLE_MODE_MOVING;
import static com.utracx.util.helper.DateTimeHelper.getEndTimeOfDay;
import static com.utracx.util.helper.DateTimeHelper.getStartTimeOfDay;
import static com.utracx.util.helper.MathHelper.round;
import static com.utracx.util.helper.NavigationHelper.navigateToNewActivity;
import static com.utracx.util.helper.SharedPreferencesHelper.getUserEmail;
import static com.utracx.view.activity.ReportViewer.REPORT_DOCUMENT_PATH;
import static java.lang.String.valueOf;

public class ReportsActivity extends BaseRefreshActivity
        implements VehicleResponseCallback, PDFUtility.OnDocumentClose, AddressTask.GeoAddressUpdateCallback, AddressTask.AddressCallback {
    private static final String TAG = "ReportsActivity";
    private ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
    private LiveData<List<DeviceData>> currentDateDeviceDetail;
    private TripDetailsCallback tripDetailsCallback;
    private VehicleDeviceDetailListRequestCall deviceDetailsRequestCall;
    private ReportActivityViewModel activityViewModel;
    private Calendar selectedCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    private TextInputAutoCompleteTextView txtEdtVehicleNumber;
    private final List<String> vehicleSearchString = new ArrayList<>();
    private AutoSuggestVehicleAdapter autoSuggestVehicleAdapter;
    private TextInputEditText txtStartDate;
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd - MMM - yyyy", Locale.getDefault());
    private List<Datum> vehicleList;
    public Datum selectedVehicle;
    private boolean isReportReady = Boolean.TRUE;
    private List<TripSummary> tripSummaries;
    private boolean isReportPreviewActivityLaunched = Boolean.FALSE;
    private ProgressDialog progressAlert;
    private Double totalDistance = 0.0;
    private static final int STORAGE_PERMISSION_REQUEST = 112;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);
        setAutoRefresh(false);
        initViewModel();
        initView();
        getVehicleInformation();
        showProgressDialog(null, "Loading.. Please wait");
        setupDateTextView();
        setupCallBack();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initView() {
        txtEdtVehicleNumber = findViewById(R.id.vehicle_number);
        txtStartDate = findViewById(R.id.start_date_input);
        txtStartDate.setFocusableInTouchMode(false);
        txtStartDate.setLongClickable(false);
        txtStartDate.setOnClickListener(new DateButtonClickListener(this));
        txtStartDate.setCursorVisible(false);
        findViewById(R.id.backView).setOnClickListener(backView -> finish());
        txtEdtVehicleNumber.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (autoSuggestVehicleAdapter != null && s.length() > 3) {
                    autoSuggestVehicleAdapter.getFilter().filter(s);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        txtEdtVehicleNumber.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;

            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (txtEdtVehicleNumber.getRight() -
                        txtEdtVehicleNumber.getCompoundDrawables()
                                [DRAWABLE_RIGHT].getBounds().width())) {
                    KeyBoardHelper.hideKeyboard(ReportsActivity.this,
                            txtEdtVehicleNumber.getRootView());
                    return true;
                }
            }
            return false;
        });
        findViewById(R.id.btnDetailedReport).setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= 23) {
                String[] PERMISSIONS = {android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
                if (!hasPermissions(this, PERMISSIONS)) {
                    ActivityCompat.requestPermissions(ReportsActivity.this, PERMISSIONS, STORAGE_PERMISSION_REQUEST);
                } else {
                    onFilePermissionEnabled();
                }
            } else {
                onFilePermissionEnabled();
            }

        });
    }

    private void onFilePermissionEnabled() {
        if (txtEdtVehicleNumber.getText().toString().trim().length() <= 0) {
            Toast.makeText(this, "Please enter vehicle registration number.", Toast.LENGTH_LONG).show();
            return;
        }

        isReportPreviewActivityLaunched = Boolean.FALSE;
        selectedVehicle = null;
        tripSummaries = null;
        totalDistance = 0.0;
        String vehicleNumber = txtEdtVehicleNumber.getText().toString().trim();
        Datum vehicleData = null;
        for (Datum datum : vehicleList) {
            if (datum.getText().equals(vehicleNumber))
                vehicleData = datum;
        }

        if (vehicleData == null)
            return;
        isReportReady = Boolean.FALSE;
        selectedVehicle = vehicleData;
        startObservingDeviceDataList();
        showProgressDialog("Report", "Generating report. Please wait");
    }

    private void fetchDeviceData(String serialNumber) {
        Executors.newSingleThreadExecutor().execute(
                () -> {
                    String startTime = getStartTimeOfDay(getCurrentCalender().getTimeInMillis()) + "";
                    String endTime = getEndTimeOfDay(getCurrentCalender().getTimeInMillis()) + "";

                    if (!startTime.isEmpty() && !endTime.isEmpty()) {

                        UserDataEntity userData = activityViewModel.fetUserDataByEmail(getUserEmail(this));
                        ApiUtils.getInstance().getSOService()
                                .getOptimisedVehicleDeviceDataV2(
                                        serialNumber,
                                        userData.getUsername(),
                                        userData.getPassword(),
                                        startTime,
                                        endTime
                                )
                                .enqueue(deviceDetailsRequestCall);
                    }
                }
        );
    }

    private List<String[]> generateReportData(List<TripSummary> tripSummaryList) {
        List<String[]> temp = new ArrayList<>();
        for (int i = 0; i < tripSummaryList.size(); i++) {
            TripSummary tripSummary = tripSummaryList.get(i);
            String startTime = "";
            String startPoint = "";
            String endTime = "";
            String endPoint = "";
            String duration = "";
            String avgSpeed = "";
            String distance = "";
            boolean isAllValueAdded = false;
            if (tripSummary.getInitialData() != null) {
                startTime = MapHelper.getEpochTime(tripSummary.getInitialData().getSourceDate(), "hh:mm:ss aa");
                startPoint = tripSummary.getInitialData().getAddress();
                endTime = MapHelper.getEpochTime(tripSummary.getStartTime(), "hh:mm:ss aa");
                endPoint = tripSummary.getAddress();
                duration = getTimeText(tripSummary.getDuration()).toString();
                avgSpeed = TextUtils.concat(round(tripSummary.getAverageSpeed(), 1) + " km/hr").toString();
                distance = round(tripSummary.getDistance(), 2) + " Km";
                isAllValueAdded = Boolean.TRUE;
            }

            if (!isAllValueAdded) {

                if (tripSummary.getVehicleMode().equalsIgnoreCase(VEHICLE_MODE_MOVING)) {
                    endTime = MapHelper.getEpochTime(tripSummary.getStartTime(), "hh:mm:ss aa");
                    endPoint = tripSummary.getAddress();
                    duration = getTimeText(tripSummary.getDuration()).toString();
                    distance = round(tripSummary.getDistance(), 2) + " Km";
                    avgSpeed = TextUtils.concat(round(tripSummary.getAverageSpeed(), 1) + " km/hr").toString();

                    i++;

                    tripSummary = tripSummaryList.get(i);
                    if (tripSummary.getInitialData() != null) {
                        startTime = MapHelper.getEpochTime(tripSummary.getInitialData().getSourceDate(), "hh:mm:ss aa");
                        startPoint = tripSummary.getInitialData().getAddress();
                    } else {
                        startTime = MapHelper.getEpochTime(tripSummary.getStartTime(), "hh:mm:ss aa");
                        startPoint = tripSummary.getAddress();
                    }

                } else {
                    startTime = MapHelper.getEpochTime(tripSummary.getStartTime(), "hh:mm:ss aa");
                    startPoint = tripSummary.getAddress();


                    i++;

                    tripSummary = tripSummaryList.get(i);

                    if (tripSummary.getInitialData() != null) {
                        startTime = MapHelper.getEpochTime(tripSummary.getInitialData().getSourceDate(), "hh:mm:ss aa");
                        startPoint = tripSummary.getInitialData().getAddress();
                    }
                    avgSpeed = TextUtils.concat(round(tripSummary.getAverageSpeed(), 1) + " km/hr").toString();
                    endTime = MapHelper.getEpochTime(tripSummary.getStartTime(), "hh:mm:ss aa");
                    endPoint = tripSummary.getAddress();
                    duration = getTimeText(tripSummary.getDuration()).toString();
                    distance = round(tripSummary.getDistance(), 2) + " Km";
                }
            }
            String[] row = new String[]{startTime, startPoint, endTime, endPoint, duration, avgSpeed, distance};
            temp.add(row);
        }

        return temp;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onFilePermissionEnabled();
            } else {
                Toast.makeText(this, getString(R.string.storage_permission_deny), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void initViewModel() {
        activityViewModel = new ViewModelProvider(this).get(ReportActivityViewModel.class);
    }

    private void setupCallBack() {
        tripDetailsCallback = new TripDetailsCallback(this, activityViewModel);
        deviceDetailsRequestCall = new VehicleDeviceDetailListRequestCall(new TripCalculationTask(tripDetailsCallback));
    }

    private void getVehicleInformation() {
        Executors.newSingleThreadExecutor().execute(
                () -> {
                    UserDataEntity userData = activityViewModel.fetUserDataByEmail(getUserEmail(this));
                    ApiUtils.getInstance().getSOService()
                            .getVehicleKeyWord(userData.getUsername(), userData.getPassword(), "")
                            .enqueue(new VehicleSearchRequestCall(this));
                });
    }

    private void startObservingDeviceDataList() {
        long startOfDay = getStartTimeOfDay(getCurrentCalender().getTimeInMillis());
        currentDateDeviceDetail = activityViewModel.getLiveDeviceDataList(selectedVehicle.getId(), startOfDay, startOfDay + ONE_DAY_END_MILLISECOND);
        currentDateDeviceDetail
                .observe(
                        this,
                        deviceDataList -> {
                            if (deviceDataList != null && !deviceDataList.isEmpty() && !isReportReady) {
                                stopCalculationThreads();

                                executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
                                executor.execute(
                                        new DeviceDataCalculationTask(this, deviceDataList, null)
                                );
                            }
                            fetchDeviceData(selectedVehicle.getId());
                        }
                );
    }

    public Calendar getCurrentCalender() {
        return selectedCalendar;
    }

    @Override
    protected void onDestroy() {
        cancelAllOpenStreetMapsRequest();
        super.onDestroy();
    }

    @Override
    public void onVehicleResponseReceived(Bundle data) {
        dismissProgressDialog();
        if (data != null && data.containsKey(BUNDLE_KEY_RESPONSE_DATA)) {
            vehicleSearchString.clear();
            Log.i(TAG, "onVehicleResponseReceived: ");
            List<Datum> result = data.getParcelableArrayList(BUNDLE_KEY_RESPONSE_DATA);
            vehicleList = result;
            for (Datum datum : result) {
                vehicleSearchString.add(datum.getText());
            }

            if (autoSuggestVehicleAdapter == null) {
                autoSuggestVehicleAdapter = new AutoSuggestVehicleAdapter(this,
                        android.R.layout.simple_dropdown_item_1line, android.R.id.text1, vehicleSearchString);
                txtEdtVehicleNumber.setAdapter(autoSuggestVehicleAdapter);
            } else {
                autoSuggestVehicleAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onVehicleResponseFailed(Bundle data) {
        dismissProgressDialog();
        Toast.makeText(this, getString(R.string.reports_failure), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPDFDocumentClose(File file) {
        dismissProgressDialog();
        if (!isReportPreviewActivityLaunched) {
            Bundle bundle = new Bundle();
            bundle.putString(REPORT_DOCUMENT_PATH, file.getAbsolutePath());
            navigateToNewActivity(this, ReportViewer.class, bundle);
            isReportPreviewActivityLaunched = Boolean.TRUE;
        }

    }

    private void stopObservingDeviceDataList() {
        if (currentDateDeviceDetail != null) {
            currentDateDeviceDetail.removeObservers(this);
        }
    }

    public void setCurrentSelectedCalendar(Calendar selectedCalendar) {
        cancelAllOpenStreetMapsRequest();
        this.selectedCalendar = selectedCalendar;
        setupDateTextView();

        if (selectedVehicle != null) {
            stopObservingDeviceDataList();
            startObservingDeviceDataList();
        }
    }

    private Calendar getStartTimeCalendar() {
        return selectedCalendar;
    }

    private void setupDateTextView() {
        txtStartDate.setText(simpleDateFormat.format(getStartTimeCalendar().getTime()));
    }

    private void stopCalculationThreads() {
        //If another thread is live, discard
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
            Log.d(TAG, "startObservingDeviceDataList: existing threads purged");
        }
    }

    public void generateReport() {
        if (tripSummaries == null)
            return;

        File pdfFolder = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), "report");
        if (!pdfFolder.exists()) {
            pdfFolder.mkdir();
        }
        Date date = new Date();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(date);

        File myFile = new File(pdfFolder + "/" + timeStamp + ".pdf");
        try {
            String distance = getString(R.string.km_placeholder,
                    valueOf(round(totalDistance, 2)));
            PDFUtility.createPdf(this, ReportsActivity.this, txtEdtVehicleNumber.getText().toString(), Objects.requireNonNull(txtStartDate.getText()).toString(), distance, generateReportData(tripSummaries), myFile.getPath(), true);
        } catch (Exception e) {
            runOnUiThread(() -> {
                dismissProgressDialog();
                e.printStackTrace();
                Log.e(TAG, "Error Creating Pdf");
                Toast.makeText(this, "Error Creating Pdf", Toast.LENGTH_SHORT).show();
            });
        }
    }

    public void reportDataReady(List<TripSummary> tripSummaryList, Double totalDistance) {
        isReportReady = Boolean.TRUE;

        if (tripSummaryList != null && tripSummaryList.size() == 1 && tripSummaryList.get(0).getInitialData() == null) {
            setNoData(getString(R.string.no_data_available));
        } else if (tripSummaryList != null && tripSummaryList.size() < 1) {
            setNoData(getString(R.string.no_data_available));
        } else {
            this.tripSummaries = tripSummaryList;
            this.totalDistance = totalDistance;
            getAddress();
        }
    }

    public void setNoData(String message) {
        isReportReady = Boolean.TRUE;
        dismissProgressDialog();
        runOnUiThread(() -> Toast.makeText(ReportsActivity.this, message, Toast.LENGTH_LONG).show());
    }

    public void setNoData() {
        dismissProgressDialog();
        isReportReady = Boolean.TRUE;
        runOnUiThread(() -> Toast.makeText(ReportsActivity.this, "Information not available to generate report", Toast.LENGTH_LONG).show());
    }

    public void showFailureMessage(String message) {
        dismissProgressDialog();
        isReportReady = Boolean.TRUE;
        runOnUiThread(() -> Toast.makeText(ReportsActivity.this, message, Toast.LENGTH_LONG).show());
    }

    private CharSequence getTimeText(long durationInMilliSeconds) {

        // Values
        long hours = TimeUnit.MILLISECONDS.toHours(durationInMilliSeconds);

        long minutes = TimeUnit.MILLISECONDS.toMinutes(durationInMilliSeconds) - (hours * 60);

        long seconds = TimeUnit.MILLISECONDS.toSeconds(durationInMilliSeconds) - (hours * 60 * 60) - (minutes * 60);

        if (hours > 0) {
            return TextUtils.concat(
                    valueOf(hours), "h",
                    " ", valueOf(minutes), "m",
                    " ", valueOf(seconds), "s"
            );
        }

        if (minutes > 0) {
            return TextUtils.concat(
                    " ", valueOf(minutes), "m",
                    " ", valueOf(seconds), "s"
            );
        }

        // Only seconds
        return TextUtils.concat(
                valueOf(seconds), " ", "s"
        );
    }

    private void getAddress() {
        if (tripSummaries == null)
            return;

        boolean isAnyAddressResolveRequired = Boolean.FALSE;

        for (int i = 0; i < tripSummaries.size(); i++) {
            TripSummary tripSummary = tripSummaries.get(i);
            String address = null;
            try {
                address = tripSummary.getAddress();
            } catch (Exception e) {
                dismissProgressDialog();
                Log.e(TAG, "setupAddress: Unable to get address", e);
            }

            String initialAddress = null;
            try {
                if (tripSummary.getInitialData() != null && tripSummary.getInitialData().getAddress() == null)
                    initialAddress = tripSummary.getInitialData().getAddress();
            } catch (Exception e) {
                dismissProgressDialog();
                Log.e(TAG, "setupAddress: Unable to get address", e);
            }
            if ((initialAddress == null || initialAddress.isEmpty()) && tripSummary.getInitialData() != null) {
                if (tripDetailsCallback == null) {
                    tripDetailsCallback = new TripDetailsCallback(this, activityViewModel);
                }
                isAnyAddressResolveRequired = Boolean.TRUE;
                new AddressTask(false, tripSummary.getInitialData(), this, tripDetailsCallback, this).run();
            }

            if (address == null || address.isEmpty()) {
                if (tripDetailsCallback == null) {
                    tripDetailsCallback = new TripDetailsCallback(this, activityViewModel);
                }
                isAnyAddressResolveRequired = Boolean.TRUE;
                new AddressTask(false, tripSummary.getLastUpdatedData(), this, tripDetailsCallback, this).run();
                return;
            }
        }

        if (!isAnyAddressResolveRequired) {
            dismissProgressDialog();
            generateReport();
        }
    }

    @Override
    public void updateAddressInformation(@NonNull @NotNull String resolvedAddress, double lat, double lng) {
        Log.i(TAG, "updateAddressInformation: ");
    }

    @Override
    public void onLocationResolvedSuccess(String resolvedAddress, double lat, double lng) {
        if (tripSummaries != null) {
            boolean isAllLocationResolved = Boolean.TRUE;
            for (int i = 0; i < tripSummaries.size(); i++) {
                TripSummary tripSummary = tripSummaries.get(i);
                boolean isChanged = Boolean.FALSE;
                if (tripSummary.getLatitude() == lat && tripSummary.getLongitude() == lng) {
                    tripSummary.setAddress(resolvedAddress);
                    isChanged = Boolean.TRUE;
                }
                if (tripSummary.getInitialData() != null &&
                        tripSummary.getInitialData().getLatitude() == lat &&
                        tripSummary.getInitialData().getLongitude() == lng) {
                    tripSummary.getInitialData().setAddress(resolvedAddress);
                    isChanged = Boolean.TRUE;
                }

                if ((tripSummary.getInitialData() != null && tripSummary.getInitialData().getAddress() == null) ||
                        tripSummary.getAddress() == null)
                    isAllLocationResolved = Boolean.FALSE;
                if (isChanged)
                    tripSummaries.set(i, tripSummary);
            }

            if (isAllLocationResolved && !isReportPreviewActivityLaunched) {
                generateReport();
            }

            if (!isAllLocationResolved) {
                getAddress();
            }
        }
    }

    @Override
    public void onLocationResolvedFailed(double lat, double lng) {
        dismissProgressDialog();
        if (tripSummaries != null) {
            boolean isAllLocationResolved = Boolean.TRUE;
            for (int i = 0; i < tripSummaries.size(); i++) {
                TripSummary tripSummary = tripSummaries.get(i);
                boolean isChanged = Boolean.FALSE;
                if (tripSummary.getLatitude() == lat && tripSummary.getLongitude() == lng) {
                    tripSummary.setAddress("NIL");
                    isChanged = Boolean.TRUE;
                }
                if (tripSummary.getInitialData() != null &&
                        tripSummary.getInitialData().getLatitude() == lat &&
                        tripSummary.getInitialData().getLongitude() == lng) {
                    tripSummary.getInitialData().setAddress("NIL");
                    isChanged = Boolean.TRUE;
                }

                if (tripSummary.getAddress() == null || tripSummary.getInitialData().getAddress() == null)
                    isAllLocationResolved = Boolean.FALSE;
                if (isChanged)
                    tripSummaries.set(i, tripSummary);
            }

            if (isAllLocationResolved && !isReportPreviewActivityLaunched) {
                generateReport();
            }

            if (!isAllLocationResolved) {
                getAddress();
            }
        }
    }

    private void dismissProgressDialog() {
        if (progressAlert != null && progressAlert.isShowing())
            progressAlert.dismiss();
    }

    private void showProgressDialog(String title, String message) {
        progressAlert = ProgressDialog.show(this, title,
                message, true);
    }

    private static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
}
