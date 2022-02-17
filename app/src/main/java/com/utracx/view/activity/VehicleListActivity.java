package com.utracx.view.activity;


import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.utracx.R;
import com.utracx.api.model.rest.vehicle_list.VehicleInfo;
import com.utracx.api.request.ApiUtils;
import com.utracx.api.request.calls.VehicleListRequestCall;
import com.utracx.api.request.interfaces.VehicleListCallback;
import com.utracx.background.AddressTask;
import com.utracx.database.datamodel.UserDataEntity;
import com.utracx.util.helper.NavigationHelper;
import com.utracx.util.helper.SnackBarHelper;
import com.utracx.util.helper.StatusBarHelper;
import com.utracx.view.adapter.VehicleAdapter;
import com.utracx.view.adapter.helper.VehicleAdapterDataObserver;
import com.utracx.view.adapter.helper.VehicleListFilter;
import com.utracx.viewmodel.DashBoardViewModel;
import com.utracx.viewmodel.VehicleListFragmentViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Executors;

import static com.utracx.api.request.ApiUtils.cancelAllOpenStreetMapsRequest;
import static com.utracx.api.request.calls.VehicleListRequestCall.BUNDLE_KEY_RESPONSE_DATA;
import static com.utracx.api.request.calls.VehicleListRequestCall.BUNDLE_KEY_VEHICLE_LIST;
import static com.utracx.util.ConstantVariables.KEY_DATE_SELECTED;
import static com.utracx.util.ConstantVariables.VEHICLE_MODE_ALL;
import static com.utracx.util.ConstantVariables.VEHICLE_MOVE_TYPE;
import static com.utracx.util.helper.DateTimeHelper.isSameDay;
import static com.utracx.util.helper.NavigationHelper.KEY_NAVIGATION_DATA_BUNDLE;
import static com.utracx.util.helper.SharedPreferencesHelper.getUserEmail;
import static com.utracx.view.activity.HomeActivity.BUNDLE_KEY_VEHICLE_DETAIL;
import static com.utracx.view.activity.HomeActivity.USER_VEHICLE_LIST;

public class VehicleListActivity extends AppCompatActivity implements SearchView.OnQueryTextListener,
        VehicleListCallback, AddressTask.GeoAddressUpdateCallback, SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "VehicleList";
    private VehicleAdapter vehicleAdapter;
    private String currentVehicleType;
    private View imageView2;

    private VehicleListFragmentViewModel fragmentViewModel;
    private final VehicleListRequestCall vehicleListRequestCall = new VehicleListRequestCall(this);
    private SwipeRefreshLayout swipeContainer;

    private boolean firstAPICallCompleted = false;

    public static final String IS_FROM_HOMEPAGE = "is_from_homepage";
    private DashBoardViewModel dashBoardViewModel;

    MaterialButtonToggleGroup materialButtonToggleGroup;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_list);
        StatusBarHelper.setupStatusBarWithToolbar(this);

        initViews();
        initIntentData();
        initViewModel();
        setupRecyclerView();
        setupSearchView();
        setupSwipeToRefresh();
        startObservingVehicleList();

    }



    private void initViews() {
        imageView2 = findViewById(R.id.imageView2);
        swipeContainer = findViewById(R.id.swipeContainer);
        findViewById(R.id.backView).setOnClickListener(backView -> finish());

        dashBoardViewModel = new ViewModelProvider(this).get(DashBoardViewModel.class);

    }

    private void initIntentData() {
        Bundle dataBundle = null;
        if (getIntent() != null && getIntent().getExtras() != null) {
            dataBundle = getIntent().getExtras();
        }

        if (dataBundle != null && dataBundle.containsKey(KEY_NAVIGATION_DATA_BUNDLE)) {

            Bundle innerBundle = dataBundle.getBundle(KEY_NAVIGATION_DATA_BUNDLE);

            if (innerBundle != null && innerBundle.containsKey(VEHICLE_MOVE_TYPE)) {
                currentVehicleType = innerBundle.getString(VEHICLE_MOVE_TYPE, VEHICLE_MODE_ALL);
            } else {
                currentVehicleType = VEHICLE_MODE_ALL;
            }
        } else {
            currentVehicleType = VEHICLE_MODE_ALL;
        }
    }

    private void initViewModel() {
        fragmentViewModel = new ViewModelProvider(this).get(VehicleListFragmentViewModel.class);
    }

    private void setupSwipeToRefresh() {
        swipeContainer.setColorSchemeResources(
                android.R.color.holo_orange_light,

                android.R.color.holo_green_dark,

                android.R.color.holo_purple,

                android.R.color.holo_red_light
        );
        swipeContainer.setOnRefreshListener(this);
        swipeContainer.setRefreshing(true);
    }

    private void setupRecyclerView() {
        RecyclerView recyclerViewTrip = findViewById(R.id.recyclerViewTrip);
        recyclerViewTrip.setHasFixedSize(true);
        LinearLayoutManager layoutManagerService = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerViewTrip.setLayoutManager(layoutManagerService);

        vehicleAdapter = new VehicleAdapter(this, this, currentVehicleType);
        recyclerViewTrip.setAdapter(vehicleAdapter);

        vehicleAdapter.registerAdapterDataObserver(new VehicleAdapterDataObserver(vehicleAdapter, currentVehicleType, findViewById(R.id.empty_text_view)));
    }

    private void setupSearchView() {
        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(this);
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setOnQueryTextFocusChangeListener((view, isFocused) -> {
                    if (isFocused) {
                        imageView2.setVisibility(View.INVISIBLE);
                    }
                }
        );
        searchView.setOnCloseListener(() -> {
            imageView2.setVisibility(View.VISIBLE);
            return false;
        });
    }

    private void startObservingVehicleList() {
        fragmentViewModel.getLiveVehiclesList().observe(
                this,
                vehicleList -> {

                    if (vehicleList != null && !vehicleList.isEmpty() && vehicleAdapter != null) {
                        ((VehicleListFilter) vehicleAdapter.getFilter()).setFullList(vehicleList);
                        if (firstAPICallCompleted) {
                            hideProgressBar();
                        }
                    } else {
                        // Updated data from the API
                        if (firstAPICallCompleted) {
                            if (vehicleAdapter != null) {
                                ((VehicleListFilter) vehicleAdapter.getFilter()).setFullList(new ArrayList<>());
                            }
                            hideProgressBar();
                        } else {
                            loadVehicleList();
                        }
                    }
                }
        );
    }

    @Override
    public void onResume() {
        super.onResume();
        reloadRecyclerAdapter();
        loadVehicleList();
    }

    private void reloadRecyclerAdapter() {
        if (null != vehicleAdapter) {
            vehicleAdapter.notifyDataSetChanged();
        }
    }

    private void loadVehicleList() {
        swipeContainer.setRefreshing(true);
        Executors.newSingleThreadExecutor().execute(
                () -> {
                    UserDataEntity userData = fragmentViewModel.fetchUserDataByEmail(getUserEmail(this));
                    if (userData != null) {
                        ApiUtils.getInstance().getSOService()
                                .sendVehicleListData("vehicle", "0", "3500", userData.getUsername(), userData.getPassword())
                                .enqueue(vehicleListRequestCall);
                    }
                }
        );
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        processSearch(query);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        processSearch(query);
        return true;
    }

    private void processSearch(String query) {
        vehicleAdapter.getFilter().filter(query);
    }

    @Override
    public void onVehicleListFetched(Bundle dataBundle) {

        if (!firstAPICallCompleted) {
            firstAPICallCompleted = true;
        }

        if (dataBundle != null && dataBundle.containsKey(BUNDLE_KEY_VEHICLE_LIST)) {
            ArrayList<VehicleInfo> vehiclesArrayList = dataBundle.getParcelableArrayList(BUNDLE_KEY_VEHICLE_LIST);
            if (vehiclesArrayList != null && !vehiclesArrayList.isEmpty()) {

                fragmentViewModel.updateNewVehiclesList(vehiclesArrayList);
                Log.d(TAG, "Fetched new Vehicle List");
                return;
            }
        }

        Log.e(TAG, "Failed to Fetch Vehicle List 2");
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
        SnackBarHelper.showLongMessage(this, "Unable to reach the Server. (Error Code : EC9)");
        hideProgressBar();
    }

    @Override
    public void onVehicleListFetchError(@Nullable Bundle data) {
        SnackBarHelper.showLongMessage(this, "Unable to reach the Server. (Error Code : EC10)");
        hideProgressBar();
    }

    private void hideProgressBar() {
        runOnUiThread(
                () -> {
                    if (swipeContainer != null) {
                        swipeContainer.setRefreshing(false);
                    }
                }
        );
    }

    @Override
    public void updateAddressInformation(@NonNull String resolvedAddress, double lat, double lng) {
        if (fragmentViewModel != null) {
            fragmentViewModel.updateNewAddress(resolvedAddress, lat, lng);
        }
    }

    @Override
    public void onRefresh() {
        if (!swipeContainer.isRefreshing()) {
            loadVehicleList();
        }
    }

    public void showList(View v){


    }

    public void vehicleDetailsForVehicleListMap() {

        Executors.newSingleThreadExecutor().execute(
                () -> {
                    UserDataEntity userData = dashBoardViewModel.fetUserDataByEmail(getUserEmail(this));

                    if (userData != null) {
                        ApiUtils.getInstance().getSOService()
                                .sendVehicleListData("vehicle", "0", "3500", userData.getUsername(), userData.getPassword())
                                .enqueue(new VehicleListRequestCall(new VehicleListCallback() {
                                    @Override
                                    public void onVehicleListFetched(Bundle dataBundle) {
                                        if (dataBundle != null && dataBundle.containsKey(BUNDLE_KEY_RESPONSE_DATA)) {
                                            ArrayList<VehicleInfo> vehiclesArrayList = dataBundle.getParcelableArrayList(BUNDLE_KEY_RESPONSE_DATA);
                                            if (vehiclesArrayList != null && !vehiclesArrayList.isEmpty()
                                                    && vehiclesArrayList.get(0) != null) {

                                                Bundle bundle = new Bundle();
                                                //bundle.putParcelable(BUNDLE_KEY_VEHICLE_DETAIL, li);
                                                bundle.putBoolean(IS_FROM_HOMEPAGE, true);

                                                if (vehiclesArrayList != null && vehiclesArrayList.size() > 0)
                                                    bundle.putParcelableArrayList(USER_VEHICLE_LIST, vehiclesArrayList);

                                                // TODO Intent to map activity
                                                NavigationHelper.navigateToNewActivity(VehicleListActivity.this, MapActivity.class, bundle);
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
}