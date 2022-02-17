package com.utracx.view.activity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.utracx.R;
import com.utracx.api.model.rest.vehicle_list.VehicleInfo;
import com.utracx.api.request.ApiUtils;
import com.utracx.api.request.calls.VehicleListRequestCall;
import com.utracx.api.request.interfaces.VehicleListCallback;
import com.utracx.database.datamodel.UserDataEntity;
import com.utracx.util.helper.DateTimeFromTimestamp;
import com.utracx.util.helper.SnackBarHelper;
import com.utracx.view.adapter.AlertVehicleAdapter;
import com.utracx.view.listener.DateButtonClickListener;
import com.utracx.view.listener.NextDateButtonClickListener;
import com.utracx.view.listener.PreviousDateButtonClickListener;
import com.utracx.viewmodel.AlertVehicleListActivityViewModel;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.function.LongToDoubleFunction;

import static android.media.CamcorderProfile.get;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static android.webkit.ConsoleMessage.MessageLevel.LOG;
import static com.utracx.api.request.calls.VehicleListRequestCall.BUNDLE_KEY_VEHICLE_LIST;
import static com.utracx.util.ConstantVariables.ONE_DAY_END_MILLISECOND;
import static com.utracx.util.ConstantVariables.STATE_DATA_LOADED;
import static com.utracx.util.ConstantVariables.STATE_LOADING_DATA;
import static com.utracx.util.ConstantVariables.STATE_NO_DATA;
import static com.utracx.util.helper.DateTimeHelper.getStartTimeOfDay;
import static com.utracx.util.helper.SharedPreferencesHelper.getUserEmail;
import static com.utracx.util.helper.StatusBarHelper.setupStatusBarWithToolbar;

public class AlertVehiclesListActivity extends AppCompatActivity implements VehicleListCallback {

    private static final String TAG = "AlertVehicleListActivi";
    private final VehicleListRequestCall vehicleListRequestCall = new VehicleListRequestCall(this);
    //Normal Views -- Updated on screen load
    private RecyclerView alertsRecyclerView;
    //Dynamic Views -- Updated on Database/REST query result
    private TextView textViewNoData;
    private TextView textViewLoading;
    private View pgsBar;
    private AlertVehicleListActivityViewModel activityViewModel;
    private AlertVehicleAdapter alertVehicleAdapter;
    private boolean firstAPICallCompleted = false;
    private LiveData<List<VehicleInfo>> currentVehicleInfo;
    long selectedTimestamp = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis();
    Calendar calendar;
    ImageView ivCalendar;
    DatePickerDialog dialog;
    int totalCount;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_vehicle_list);
        setupStatusBarWithToolbar(AlertVehiclesListActivity.this);

        SharedPreferences sh = getSharedPreferences("Myshared", MODE_PRIVATE);
        totalCount = sh.getInt("totalCount", 0);
        initViewModel();
        initViewElements();

        setupRecyclerView();

        setupPlaceHolderViews(STATE_LOADING_DATA);

        startObservingVehicleData();
        loadVehicleList();

    }

    private void initViewModel() {
        activityViewModel = new ViewModelProvider(this).get(AlertVehicleListActivityViewModel.class);
    }

    private void initViewElements() {
        pgsBar = findViewById(R.id.pBar);
        alertsRecyclerView = findViewById(R.id.recyclerViewAlertVehicles);
        textViewLoading = findViewById(R.id.tv_LoadingMessage);
        textViewNoData = findViewById(R.id.tv_NoDataMessage);
        ivCalendar = findViewById(R.id.iv_calender);


        Runnable REPEAT_ANIMATION = new Runnable() {
            @Override
            public void run() {
                ivCalendar.animate().rotationYBy(360).setDuration(5000).withEndAction(this::run).start();
            }
        };

        ivCalendar.animate().rotationYBy(360).setDuration(5000).withEndAction(REPEAT_ANIMATION).start();

        UserDataEntity userData = activityViewModel.fetchUserDataByEmail(getUserEmail(this));

        if(totalCount>1) {
            ivCalendar.setVisibility(GONE);
        }

        ivCalendar.setOnClickListener(v -> {
            calendarClickListener();
            Animation animFadeIn = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.calender_fade);
            ivCalendar.startAnimation(animFadeIn);

        });
    }

    private void calendarClickListener() {

        calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            selectedTimestamp = calendar.getTime().getTime();
            startObservingVehicleData();
        };
        new DatePickerDialog(AlertVehiclesListActivity.this, R.style.my_dialog_theme, dateSetListener, calendar
                .get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();

    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManagerService = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        alertsRecyclerView.setLayoutManager(layoutManagerService);

        alertVehicleAdapter = new AlertVehicleAdapter(this);

        alertsRecyclerView.setHasFixedSize(true);
        alertsRecyclerView.setAdapter(alertVehicleAdapter);
    }

    private void startObservingVehicleData() {

        currentVehicleInfo = activityViewModel.getVehiclesLiveListSortByAlertCount();

        currentVehicleInfo.observe(
                this,
                vehicleList -> {
                    if (vehicleList != null && !vehicleList.isEmpty() && alertVehicleAdapter != null){
                        alertVehicleAdapter.setVehicleList(vehicleList);
                        setupPlaceHolderViews(STATE_DATA_LOADED);
                    }
                    else {
                        // Updated data from the API
                        if (firstAPICallCompleted) {
                            setupPlaceHolderViews(STATE_NO_DATA);

                        } else {
                            loadVehicleList();
                        }
                    }
                }
        );


    }

    private void loadVehicleList() {

        Executors.newSingleThreadExecutor().execute(
                () -> {
                    UserDataEntity userData = activityViewModel.fetchUserDataByEmail(getUserEmail(this));
                    if (userData != null) {
                        ApiUtils.getInstance().getSOService()
                                .sendVehicleListData("vehicle", "0", "3500", userData.getUsername(), userData.getPassword())
                                .enqueue(vehicleListRequestCall);
                    }
                }
        );
    }

    private void setupPlaceHolderViews(int state) {
        switch (state) {
            case STATE_LOADING_DATA:
                textViewLoading.setVisibility(VISIBLE);
                textViewNoData.setVisibility(GONE);
                pgsBar.setVisibility(VISIBLE);
                alertsRecyclerView.setVisibility(GONE);
                break;
            case STATE_DATA_LOADED:
                textViewLoading.setVisibility(GONE);
                textViewNoData.setVisibility(GONE);
                alertsRecyclerView.setVisibility(VISIBLE);
                pgsBar.setVisibility(GONE);
                break;
            case STATE_NO_DATA:
                textViewLoading.setVisibility(GONE);
                // TODO changed here
                textViewNoData.setVisibility(VISIBLE);
                alertsRecyclerView.setVisibility(GONE);

                pgsBar.setVisibility(GONE);
                break;
        }
    }

    @Override
    public void onVehicleListFetched(Bundle dataBundle) {

        if (!firstAPICallCompleted) {
            firstAPICallCompleted = true;
        }

        if (dataBundle != null && dataBundle.containsKey(BUNDLE_KEY_VEHICLE_LIST)) {
            ArrayList<VehicleInfo> vehiclesArrayList = dataBundle.getParcelableArrayList(BUNDLE_KEY_VEHICLE_LIST);
            if (vehiclesArrayList != null && !vehiclesArrayList.isEmpty()) {
                 activityViewModel.updateNewVehiclesList(vehiclesArrayList);
                Log.d(TAG, "Fetched new Vehicle List");
                return;
            }
        }
        Log.e(TAG, "Failed to Fetch Vehicle List 2");
        setupPlaceHolderViews(STATE_NO_DATA);
    }

    @Override
    public void onVehicleListFetchFailed(Bundle data) {
        if (data != null && data.containsKey(VehicleListRequestCall.BUNDLE_KEY_RESPONSE_DATA)) {
            String responseString = data.getString(VehicleListRequestCall.BUNDLE_KEY_RESPONSE_DATA, null);
            if (responseString != null) {
                Log.e(TAG, "onVehicleListFetchFailed: API Response String - " + responseString);
            }
        }
        SnackBarHelper.showLongMessage(this, "Unable to reach the Server. (Error Code : EC9)");
        setupPlaceHolderViews(STATE_NO_DATA);
    }

    @Override
    public void onVehicleListFetchError(@Nullable Bundle data) {
        SnackBarHelper.showLongMessage(this, "Unable to reach the Server. (Error Code : EC10)");
        setupPlaceHolderViews(STATE_NO_DATA);
    }

    public void refreshAlerts(View view) {
        setupPlaceHolderViews(STATE_LOADING_DATA);
        firstAPICallCompleted = false;
        loadVehicleList();
    }

    public void onBackPressed(View view) {
        finish();
    }

}