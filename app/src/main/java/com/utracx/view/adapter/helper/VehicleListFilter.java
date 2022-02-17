package com.utracx.view.adapter.helper;

import android.util.Log;
import android.widget.Filter;

import androidx.annotation.NonNull;

import com.utracx.api.model.rest.vehicle_list.VehicleInfo;
import com.utracx.view.adapter.VehicleAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.utracx.util.ConstantVariables.THRESHOLD_TIME_FOR_NON_COMM_VEHICLE;
import static com.utracx.util.ConstantVariables.VEHICLE_MODE_ALL;
import static com.utracx.util.ConstantVariables.VEHICLE_MODE_NON_COMMUNICATING;
import static com.utracx.util.ConstantVariables.VEHICLE_MODE_ONLINE;

public class VehicleListFilter extends Filter {
    private static final String TAG = "VehicleListFilter";

    private final VehicleAdapter adapter;
    private final String vehicleMode;
    private boolean isSearching = false;

    private FilterResults filterResults;
    private List<VehicleInfo> vehicleInfoListFull;

    public VehicleListFilter(VehicleAdapter adapter, String vehicleMode) {
        this.adapter = adapter;
        this.vehicleMode = vehicleMode;
        this.vehicleInfoListFull = new ArrayList<>();
        this.filterResults = new FilterResults();
    }

    @Override
    protected FilterResults performFiltering(CharSequence charSequence) {
        List<VehicleInfo> filteredList = new ArrayList<>();

        if (charSequence == null || charSequence.length() == 0) {
            isSearching = false;
            filteredList.addAll(getFullList());
        } else {
            isSearching = true;
            String charString = null;
            try {
                charString = charSequence.toString().trim().toLowerCase();
            } catch (Exception e) {
                Log.e(TAG, "performFiltering: invalid  query text", e);
            }
            if (charString == null || charString.isEmpty()) {
                filteredList.addAll(getFullList());
            } else {

                for (VehicleInfo row : getFullList()) {

                    // Match condition. this might differ depending on requirement
                    // here we are looking for Registration Number, Vehicle Type and address
                    if (row != null
                            && (row.getVehicleRegistration().toLowerCase().contains(charString)
                            || (row.getVehicleType() != null && row.getVehicleType().toLowerCase().contains(charString))
                            || (row.getLastUpdatedData() != null
                            && row.getLastUpdatedData().getAddress() != null
                            && row.getLastUpdatedData().getAddress().toLowerCase().contains(charString))
                    )
                    ) {
                        filteredList.add(row);
                    }
                }

            }
        }

        filterResults.values = filteredList;
        filterResults.count = filteredList.size();
        return filterResults;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
        updateAdapter((List<VehicleInfo>) filterResults.values);
    }

    private void updateAdapter(List<VehicleInfo> vehicleInfoList) {
        adapter.updateFilterList(vehicleInfoList);
    }

    @NonNull
    private List<VehicleInfo> getFullList() {
        if (vehicleMode == null || vehicleMode.isEmpty()
                || vehicleMode.equalsIgnoreCase(VEHICLE_MODE_ALL)) {
            return vehicleInfoListFull;
        } else if (vehicleMode.equalsIgnoreCase(VEHICLE_MODE_NON_COMMUNICATING)) {
            return getNonCommunicatingDevices();
        } else if (vehicleMode.equalsIgnoreCase(VEHICLE_MODE_ONLINE)) {
            return getCommunicatingDevices();
        } else {
            return getVehiclesByMode();
        }
    }

    public void setFullList(List<VehicleInfo> newVehicleInfoList) {
        this.vehicleInfoListFull.clear();
        this.vehicleInfoListFull = new ArrayList<>(newVehicleInfoList);

        if (!isSearching) {
            updateAdapter(getFullList());
        }
    }

    @NotNull
    private List<VehicleInfo> getVehiclesByMode() {
        List<VehicleInfo> newFilteredList = new ArrayList<>();

        for (VehicleInfo eachVehicle : vehicleInfoListFull) {
            if (eachVehicle.getLastUpdatedData() != null
                    && eachVehicle.getLastUpdatedData().getVehicleMode() != null
                    && eachVehicle.getLastUpdatedData().getVehicleMode().equalsIgnoreCase(vehicleMode)
                    && eachVehicle.getLastUpdatedData().getSourceDate() != null
                    && (System.currentTimeMillis() - eachVehicle.getLastUpdatedData().getSourceDate()) < THRESHOLD_TIME_FOR_NON_COMM_VEHICLE
            ) {
                newFilteredList.add(eachVehicle);
            }
        }
        return newFilteredList;
    }

    @NotNull
    private List<VehicleInfo> getCommunicatingDevices() {
        List<VehicleInfo> newFilteredList = new ArrayList<>();
        for (VehicleInfo eachVehicle : vehicleInfoListFull) {
            if (eachVehicle.getLastUpdatedData() != null
                    && eachVehicle.getLastUpdatedData().getSourceDate() != null
                    && (System.currentTimeMillis() - eachVehicle.getLastUpdatedData().getSourceDate()) < THRESHOLD_TIME_FOR_NON_COMM_VEHICLE) {
                newFilteredList.add(eachVehicle);
            }
        }
        return newFilteredList;
    }

    @NotNull
    private List<VehicleInfo> getNonCommunicatingDevices() {
        List<VehicleInfo> newFilteredList = new ArrayList<>();
        for (VehicleInfo eachVehicle : vehicleInfoListFull) {
            if (eachVehicle.getLastUpdatedData() == null
                    || eachVehicle.getLastUpdatedData().getSourceDate() == null
                    || (System.currentTimeMillis() - eachVehicle.getLastUpdatedData().getSourceDate()) > THRESHOLD_TIME_FOR_NON_COMM_VEHICLE) {
                newFilteredList.add(eachVehicle);
            }
        }
        return newFilteredList;
    }

    boolean isSearching() {
        return isSearching;
    }
}
