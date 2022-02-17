package com.utracx.view.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;


import com.google.android.material.navigation.NavigationView;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.utracx.R;
import com.utracx.api.model.rest.firebase.FirebaseRequestBody;
import com.utracx.api.model.rest.vehicle_list.VehicleInfo;
import com.utracx.api.request.ApiUtils;
import com.utracx.api.request.calls.FirebaseInstanceIdCall;
import com.utracx.api.request.calls.VehicleCountRequestCall;
import com.utracx.api.request.calls.VehicleListRequestCall;
import com.utracx.api.request.interfaces.VehicleCountCallback;
import com.utracx.api.request.interfaces.VehicleListCallback;
import com.utracx.database.datamodel.UserDataEntity;
import com.utracx.navfragment.Profile_F;
import com.utracx.util.helper.NavigationHelper;
import com.utracx.util.helper.SharedPreferencesHelper;
import com.utracx.util.helper.SnackBarHelper;
import com.utracx.viewmodel.DashBoardViewModel;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.Executors;

import static com.utracx.api.request.ApiUtils.cancelAllOpenStreetMapsRequest;
import static com.utracx.api.request.ApiUtils.cancelAllSOSRequest;
import static com.utracx.api.request.calls.VehicleCountRequestCall.BUNDLE_KEY_IDLE_COUNT;
import static com.utracx.api.request.calls.VehicleCountRequestCall.BUNDLE_KEY_INACTIVE_COUNT;
import static com.utracx.api.request.calls.VehicleCountRequestCall.BUNDLE_KEY_MOVING_COUNT;
import static com.utracx.api.request.calls.VehicleCountRequestCall.BUNDLE_KEY_RESPONSE_DATA;
import static com.utracx.api.request.calls.VehicleCountRequestCall.BUNDLE_KEY_SLEEP_COUNT;
import static com.utracx.api.request.calls.VehicleListRequestCall.BUNDLE_KEY_VEHICLE_LIST;
import static com.utracx.util.ConstantVariables.REPORTS_WEB_URL;
import static com.utracx.util.ConstantVariables.VEHICLE_MODE_ALL;
import static com.utracx.util.ConstantVariables.VEHICLE_MODE_MOVING;
import static com.utracx.util.ConstantVariables.VEHICLE_MODE_NON_COMMUNICATING;
import static com.utracx.util.ConstantVariables.VEHICLE_MODE_ONLINE;
import static com.utracx.util.ConstantVariables.VEHICLE_MODE_SLEEP;
import static com.utracx.util.ConstantVariables.VEHICLE_MOVE_TYPE;
import static com.utracx.util.helper.NavigationHelper.navigateToNewActivity;
import static com.utracx.util.helper.SharedPreferencesHelper.getUserEmail;

public class HomeActivity extends AppCompatActivity implements VehicleCountCallback, VehicleListCallback, View.OnClickListener {

    public static final String BUNDLE_KEY_VEHICLE_DETAIL = "key_vehicle_detail";
    private static final String TAG = "AppDashboard";
    public static final String IS_FROM_HOMEPAGE = "is_from_homepage";
    public static final String USER_VEHICLE_LIST = "user_vehicle_list";
    private TextView textViewMoving;
    private TextView textViewStopped;
    private TextView textViewTotalCount;
    private TextView textViewOffline;
    private TextView textViewOnline;
    TextView tvUserImg, tvUsername;
    private View progressBar;
    private int totalCount = 0;
    private DashBoardViewModel dashBoardViewModel;
    private boolean isLogoutFlag = false;
    ArrayList<VehicleInfo> vehiclesArrayList;

    ImageView ivMap;
    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    Boolean CheckDrawer = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        initViews();
        initOnClickListener();
        initViewModel();
        updateVersion();

    }

    private void initViews() {
        textViewMoving = findViewById(R.id.textViewMovingCount);
        textViewStopped = findViewById(R.id.textViewIdleCount);
        textViewOffline = findViewById(R.id.textViewOfflineCount);
        textViewOnline = findViewById(R.id.textViewOnlineCount);
        textViewTotalCount = findViewById(R.id.tv_TotalVehicle);
        progressBar = findViewById(R.id.progressBarHori);

        ivMap = findViewById(R.id.iv_map);
        if(getUserEmail(this).equals("admin")){
            ivMap.setVisibility(View.GONE);
        }

    }

    private void initOnClickListener() {

        findViewById(R.id.iv_profile).setOnClickListener(v -> gotoProfile());
        findViewById(R.id.imageView20).setOnClickListener(v -> fetchVehicleCount());
        findViewById(R.id.tile_layout).setOnClickListener(this);
        findViewById(R.id.image_btn_moving).setOnClickListener(this);
        findViewById(R.id.image_btn_stopped).setOnClickListener(this);
        findViewById(R.id.image_btn_non_comm).setOnClickListener(this);
        findViewById(R.id.image_btn_online).setOnClickListener(this);
        findViewById(R.id.image_btn_alert).setOnClickListener(this);
        findViewById(R.id.image_btn_report).setOnClickListener((view) -> gotoReportsPage1());

        ivMap.setOnClickListener(v -> {
            vehicleDetailsForHomeMap();
        });

    }

    public void gotoProfile(){

        Intent i = new Intent(this, Profile_F.class);
        startActivity(i);

    }

    private void gotoReportsPage1() {

        UserDataEntity userData = dashBoardViewModel.fetUserDataByEmail(getUserEmail(this));

        String enc_pass = null;

        try {

            if (userData!=null) {
                enc_pass = URLEncoder.encode(userData.getPassword(), "utf-8");
            }

            String reportsURL = String.format(
                    REPORTS_WEB_URL,
                    userData.getUsername(),
                    enc_pass
            );

            Uri uri = Uri.parse(reportsURL);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Toast.makeText(this, "Unable to fetch report", Toast.LENGTH_SHORT).show();
        }


    }


    private void initViewModel() {
        dashBoardViewModel = new ViewModelProvider(this).get(DashBoardViewModel.class);
        dashBoardViewModel.getDashboardData().observe(this, userDataViewModel -> {
                    if (userDataViewModel != null) {
                        setupCountViews(
                                userDataViewModel.getMovingCount(),
                                userDataViewModel.getHaltCount(),
                                userDataViewModel.getSleepCount(),
                                userDataViewModel.getInactiveCount()
                        );
                        hideProgressBar();

                        if (userDataViewModel.getFirebaseInstanceID() == null
                                || userDataViewModel.getFirebaseInstanceID().isEmpty()) {
                            generateNewFirebaseInstanceID(userDataViewModel);
                        }
                    }
                }
        );
    }

    private void generateNewFirebaseInstanceID(@NonNull UserDataEntity userData) {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful() || task.getResult() == null) {
                Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                return;
            }

            dashBoardViewModel.updateFirebaseInstanceID(task.getResult());

            FirebaseRequestBody firebaseRequestBody = new FirebaseRequestBody(
                    userData.getUsername(),
                    userData.getPassword(),
                    task.getResult());

            ApiUtils
                    .getInstance()
                    .getSOService()
                    .registerFirebaseForUser(firebaseRequestBody)
                    .enqueue(new FirebaseInstanceIdCall(firebaseRequestBody));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchVehicleCount();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void gotoDashboard(int id) {
        progressBar.setVisibility(View.GONE);

        String vehicleMode;

        if (id == R.id.image_btn_moving) {
            vehicleMode = VEHICLE_MODE_MOVING;
        } else if (id == R.id.image_btn_stopped) {
            vehicleMode = VEHICLE_MODE_SLEEP;
        }
        else if (id == R.id.image_btn_non_comm) {
            vehicleMode = VEHICLE_MODE_NON_COMMUNICATING;
        } else if (id == R.id.image_btn_online) {
            vehicleMode = VEHICLE_MODE_ONLINE;
        } else {
            vehicleMode = VEHICLE_MODE_ALL;
        }

        Bundle dataBundle = new Bundle();
        dataBundle.putString(VEHICLE_MOVE_TYPE, vehicleMode);

        navigateToNewActivity(HomeActivity.this, VehicleListActivity.class, dataBundle);
    }

    private void fetchVehicleCount() {
        progressBar.setVisibility(View.VISIBLE);

        Executors.newSingleThreadExecutor().execute(
                () -> {
                    UserDataEntity userData = dashBoardViewModel.fetUserDataByEmail(getUserEmail(this));
                    if (userData != null) {
                        ApiUtils.getInstance().getSOService()
                                .sendVehicleCountData(userData.getUsername(), userData.getPassword())
                                .enqueue(new VehicleCountRequestCall(this));
                    }
                }
        );
    }

    private void vehicleDetails() {
        Executors.newSingleThreadExecutor().execute(
                () -> {
                    UserDataEntity userData = dashBoardViewModel.fetUserDataByEmail(getUserEmail(this));
                    if (userData != null) {
                        ApiUtils.getInstance().getSOService()
                                .sendVehicleListData("vehicle", "0", "2", userData.getUsername(), userData.getPassword())
                                .enqueue(new VehicleListRequestCall(this));
                    }
                }
        );
    }

    private void vehicleDetailsForHomeMap() {
        Executors.newSingleThreadExecutor().execute(
                () -> {
                    UserDataEntity userData = dashBoardViewModel.fetUserDataByEmail(getUserEmail(this));
                    if (userData != null) {
                        ApiUtils.getInstance().getSOService()
                                .sendVehicleListData("vehicle", "0", "3500", userData.getUsername(), userData.getPassword())
                                .enqueue(new VehicleListRequestCall(new VehicleListCallback() {
                                    @Override
                                    public void onVehicleListFetched(Bundle dataBundle) {
                                        if (dataBundle != null && dataBundle.containsKey(BUNDLE_KEY_VEHICLE_LIST)) {
                                            ArrayList<VehicleInfo> vehiclesArrayList = dataBundle.getParcelableArrayList(BUNDLE_KEY_VEHICLE_LIST);
                                            if (vehiclesArrayList != null && !vehiclesArrayList.isEmpty()
                                                    && vehiclesArrayList.get(0) != null) {

                                                Bundle bundle = new Bundle();
                                                bundle.putBoolean(IS_FROM_HOMEPAGE, true);

                                                if (vehiclesArrayList != null && vehiclesArrayList.size() > 0)
                                                    bundle.putParcelableArrayList(USER_VEHICLE_LIST, vehiclesArrayList);

                                                // TODO Intent to map activity
                                                NavigationHelper.navigateToNewActivity(HomeActivity.this, MapActivity.class, bundle);
                                            }
                                        }
                                    }

                                    @Override
                                    public void onVehicleListFetchFailed(Bundle data) {

                                    }

                                    @Override
                                    public void onVehicleListFetchError(@Nullable Bundle data) {

                                    }
                                }));
                    }
                }
        );
    }

    @Override
    protected void onPause() {
        cancelAllOpenStreetMapsRequest();
        if (!isLogoutFlag) {
            cancelAllSOSRequest();
        }
        super.onPause();
    }

    private void logoutUserNow() {

        //flag to prevent the logout cancel of API calls
        isLogoutFlag = true;

        //call to the server
        unregisterFirebaseInstanceID();

        //Call to firebase SDK to delete the firebase token
        destroyFirebaseInstanceID();

        //Clear all the tables in the DB
        dashBoardViewModel.deleteAllData();

        //clear all values saved in the shared preferences
        SharedPreferencesHelper.clearPreferences(this);

        //Move to login activity
        NavigationHelper.clearNavigateToLoginActivity(this);
    }

    private void destroyFirebaseInstanceID() {
        Executors.newSingleThreadExecutor().execute(
                () -> {
                    try {
                        FirebaseInstanceId.getInstance().deleteInstanceId();
                    } catch (Exception e) {
                        Log.e(TAG, "logoutUserNow: unable to destroy firebase id", e);
                    }
                }
        );
    }

    private void unregisterFirebaseInstanceID() {
        Executors.newSingleThreadExecutor().execute(
                () -> {

                    UserDataEntity userData = dashBoardViewModel.fetUserDataByEmail(getUserEmail(this));
                    if (userData != null) {
                        FirebaseRequestBody firebaseRequestBody = new FirebaseRequestBody(
                                userData.getUsername(),
                                userData.getPassword(),
                                userData.getFirebaseInstanceID()
                        );

                        ApiUtils
                                .getInstance()
                                .getSOService()
                                .unregisterFirebaseForUser(firebaseRequestBody)
                                .enqueue(new FirebaseInstanceIdCall(firebaseRequestBody));
                    }
                }
        );
    }

    private void hideProgressBar() {
        runOnUiThread(() -> {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                }
        );
    }

    @Override
    public void onVehicleCountFetched(Bundle data) {
        if (data != null && data.containsKey(BUNDLE_KEY_MOVING_COUNT)
                && data.containsKey(BUNDLE_KEY_IDLE_COUNT)
                && data.containsKey(BUNDLE_KEY_SLEEP_COUNT)
                && data.containsKey(BUNDLE_KEY_INACTIVE_COUNT)) {

            dashBoardViewModel.updateCountData(
                    getUserEmail(this),
                    data.getInt(BUNDLE_KEY_MOVING_COUNT),
                    data.getInt(BUNDLE_KEY_IDLE_COUNT),
                    data.getInt(BUNDLE_KEY_SLEEP_COUNT),
                    data.getInt(BUNDLE_KEY_INACTIVE_COUNT)
            );

            Log.d(TAG, "Fetched Vehicle count successfully");
        } else {
            Log.e(TAG, "Failed to fetch vehicle count");
            logoutUserNow();
        }
    }

    private void setupCountViews(int movingCount, int haltCount, int sleepCount, int inactiveCount) {
        totalCount = movingCount + haltCount + sleepCount + inactiveCount;

        textViewMoving.setText(String.valueOf(movingCount));
        textViewStopped.setText(String.valueOf(sleepCount));
        textViewOnline.setText(String.valueOf(totalCount - inactiveCount));
        textViewOffline.setText(String.valueOf(inactiveCount));
        textViewTotalCount.setText(String.valueOf(totalCount));

        SharedPreferences sharedPreferences = getSharedPreferences("Myshared", MODE_PRIVATE);
        SharedPreferences.Editor tcount = sharedPreferences.edit();
        tcount.putInt("totalCount",totalCount);
        tcount.apply();

    }

    @Override
    public void onVehicleCountFetchFailed(Bundle data) {
        if (data != null && data.containsKey(BUNDLE_KEY_RESPONSE_DATA)) {
            String responseString = data.getString(BUNDLE_KEY_RESPONSE_DATA, null);
            if (responseString != null) {
                Log.e(TAG, "onVehicleCountFailed: API Response String - " + responseString);
            }
        }
        SnackBarHelper.showLongMessage(HomeActivity.this, "Unable to reach the Server. (Error Code : EC4)");
        hideProgressBar();
        logoutUserNow();
    }

    @Override
    public void onVehicleCountFetchError(@Nullable Bundle data) {
        SnackBarHelper.showLongMessage(HomeActivity.this, "Unable to reach the Server. (Error Code : EC3)");
        hideProgressBar();
        logoutUserNow();
    }

    @Override
    public void onVehicleListFetched(Bundle dataBundle) {
        if (dataBundle != null && dataBundle.containsKey(BUNDLE_KEY_VEHICLE_LIST)) {
            ArrayList<VehicleInfo> vehiclesArrayList = dataBundle.getParcelableArrayList(BUNDLE_KEY_VEHICLE_LIST);
            if (vehiclesArrayList != null && !vehiclesArrayList.isEmpty()
                    && vehiclesArrayList.get(0) != null) {

                 this.vehiclesArrayList = vehiclesArrayList;
                dashBoardViewModel.updateNewVehiclesList(vehiclesArrayList);

                Bundle newDataBundle = new Bundle();
                newDataBundle.putParcelable(BUNDLE_KEY_VEHICLE_DETAIL, vehiclesArrayList.get(0));
                navigateToNewActivity(this, MapActivity.class, newDataBundle);
            }
        } else {
            Log.e(TAG, "Failed to Fetch Vehicle List 2");
            gotoDashboard(-1);
        }
        hideProgressBar();
    }


    @Override
    public void onVehicleListFetchFailed(Bundle data) {
        if (data != null && data.containsKey(VehicleListRequestCall.BUNDLE_KEY_RESPONSE_DATA)) {
            String responseString = data.getString(VehicleListRequestCall.BUNDLE_KEY_RESPONSE_DATA, null);
            if (responseString != null) {
                Log.e(TAG, "onVehicleListFetchFailed: API Response String - " + responseString);
            }
        }
        SnackBarHelper.showLongMessage(HomeActivity.this, "Unable to reach the Server. (Error Code : EC1)");
        hideProgressBar();
        logoutUserNow();
    }

    @Override
    public void onVehicleListFetchError(@Nullable Bundle data) {
        SnackBarHelper.showLongMessage(HomeActivity.this, "Unable to reach the Server. (Error Code : EC2)");
        hideProgressBar();
        logoutUserNow();
    }

    @Override
    public void onClick(View v) {
        int clickedViewId = v.getId();
        if (clickedViewId == R.id.image_btn_alert) {
            navigateToNewActivity(HomeActivity.this, AlertVehiclesListActivity.class, null);
        } else {
            if (totalCount == 1) {
                progressBar.setVisibility(View.VISIBLE);
                vehicleDetails();
            } else {
                gotoDashboard(clickedViewId);
            }
        }
    }

    private void gotoReportsPage() {

        navigateToNewActivity(this, ReportsActivity.class, null);
    }



    private void updateVersion() {
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            ((TextView) findViewById(R.id.about_text_view)).setText(
                    String.format(
                            Locale.ENGLISH,
                            "Ver. %s", pInfo.versionName
                    )
            );
        } catch (Exception e) {
            Log.e(TAG, "updateVersion: unable to fetch the version information", e);
        }
    }
}
