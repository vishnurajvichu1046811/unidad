package com.utracx.view.adapter.helper;

import android.widget.Filter;

import com.utracx.api.model.rest.vehicle_list.VehicleInfo;
import com.utracx.view.adapter.AlertAdapter;
import com.utracx.view.adapter.AlertVehicleAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class AlertListFilter extends Filter {
    private static final String TAG = "AlertListFilter";

    private final AlertVehicleAdapter adapter;

    private FilterResults filterResults;
    private List<VehicleInfo> vehicleInfoListFull;
    private HashMap<String, AlertAdapter> alertsHashMap;

    public AlertListFilter(AlertVehicleAdapter adapter) {
        this.adapter = adapter;
        this.vehicleInfoListFull = new ArrayList<>();
        this.alertsHashMap = new HashMap<>();
        this.filterResults = new FilterResults();
    }

    @Override
    protected synchronized FilterResults performFiltering(CharSequence charSequence) {
        List<VehicleInfo> filteredList = new ArrayList<>();
        VehicleInfo row;

        List<VehicleInfo> fullVehicleListCopy = new CopyOnWriteArrayList<>(Collections.unmodifiableList(this.vehicleInfoListFull));
        if (alertsHashMap.size() > 0) {
            for (int i = 0; i < fullVehicleListCopy.size(); i++) {
                row = fullVehicleListCopy.get(0);
                // Match condition. this might differ depending on requirement
                // here we are looking for Registration Number, Vehicle Type and address
                if (row != null && (row.getLastUpdatedData().getSerialNumber() == null || row.getLastUpdatedData().getSerialNumber().isEmpty()
                        || alertsHashMap.get(row.getLastUpdatedData().getSerialNumber()) != null)) {
                    filteredList.add(row);
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

    }

    //Updated from the Activity
    public void setVehicleList(List<VehicleInfo> newVehicleInfoList) {
        this.vehicleInfoListFull.clear();
        this.vehicleInfoListFull = new ArrayList<>(newVehicleInfoList);
    }

    //Updated from the Adapter
    public void setAdapterList(HashMap<String, AlertAdapter> newAlertsHashMap) {
        this.alertsHashMap.clear();
        this.alertsHashMap = new HashMap<>(newAlertsHashMap);
    }
}
