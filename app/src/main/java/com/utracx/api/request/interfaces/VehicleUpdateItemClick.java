package com.utracx.api.request.interfaces;

import com.utracx.api.model.rest.vehicle_list.VehicleInfo;

import java.util.ArrayList;

public interface VehicleUpdateItemClick {

    void onVehicleUpdateItemClicked(VehicleInfo vehicleInfo, int position);
    void onUpdateClicked(VehicleInfo vehicleInfo, int position, String newregister);
    void onCancelClicked(VehicleInfo vehicleInfo, int position);


}
