package com.utracx.view.listener;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.AndroidViewModel;

import com.utracx.api.model.rest.device_data.DeviceData;
import com.utracx.api.request.interfaces.SerialNumberCallback;
import com.utracx.background.AddressTask.GeoAddressUpdateCallback;
import com.utracx.background.TripCalculationTask.TripCalculationTaskCallback;
import com.utracx.view.activity.MapActivity;
import com.utracx.view.activity.ReportsActivity;
import com.utracx.view.activity.TripDetailsActivity;
import com.utracx.viewmodel.MapActivityViewModel;
import com.utracx.viewmodel.ReportActivityViewModel;
import com.utracx.viewmodel.TripDetailsActivityViewModel;

import java.util.ArrayList;

import static com.utracx.api.request.calls.SerialNumberRequestCall.BUNDLE_KEY_RESPONSE_DATA;
import static com.utracx.api.request.calls.SerialNumberRequestCall.BUNDLE_KEY_SERIAL_NUMBER;
import static com.utracx.api.request.calls.VehicleDeviceDetailListRequestCall.BUNDLE_KEY_VEHICLE_DEVICE_DETAIL_LIST;

public class TripDetailsCallback implements SerialNumberCallback, TripCalculationTaskCallback,
        GeoAddressUpdateCallback {

    private static final String TAG = "TripDetailsCallback";
    private final AppCompatActivity activity;
    private final AndroidViewModel activityViewModel;

    public TripDetailsCallback(AppCompatActivity activity, AndroidViewModel activityViewModel) {
        this.activity = activity;
        this.activityViewModel = activityViewModel;
    }

    //Only for Trip Details Activity
    @Override
    public void onSerialNumberFetched(Bundle data) {
        if (data != null && data.containsKey(BUNDLE_KEY_SERIAL_NUMBER)) {
            String serialNumber = data.getString(BUNDLE_KEY_SERIAL_NUMBER);
            if (serialNumber != null && !serialNumber.isEmpty()
                    && activity instanceof TripDetailsActivity
                    && activityViewModel instanceof TripDetailsActivityViewModel) {
                ((TripDetailsActivityViewModel) activityViewModel).updateSerialNumber(((TripDetailsActivity) activity).getCurrentVehicleInfo().getId(), serialNumber);
            }
            if (serialNumber != null && !serialNumber.isEmpty()
                    && activity instanceof ReportsActivity
                    && activityViewModel instanceof ReportActivityViewModel) {
                ((ReportActivityViewModel) activityViewModel).updateSerialNumber(((ReportsActivity) activity).selectedVehicle.getId(), serialNumber);
            }
        } else {
            if (activity instanceof TripDetailsActivity) {
                ((TripDetailsActivity) activity).showFailureMessage("Unable to reach the Server. API fetch/response failed. (Code: EC7)");
            }
        }
    }

    //Only for Trip Details Activity
    @Override
    public void onSerialNumberFetchFailed(Bundle data) {
        if (data != null && data.containsKey(BUNDLE_KEY_RESPONSE_DATA)) {
            String responseString = data.getString(BUNDLE_KEY_RESPONSE_DATA, null);
            if (responseString != null) {
                Log.e(TAG, "onSerialNumberFetchFailed: API Response String - " + responseString);
            }
        }
        if (activity instanceof TripDetailsActivity) {
            ((TripDetailsActivity) activity).showFailureMessage("Unable to reach the Server. API request failed. (Code: EC6)");
        }
    }

    //Only for Trip Details Activity
    @Override
    public void onSerialNumberFetchError(@Nullable Bundle data) {
        if (activity instanceof TripDetailsActivity) {
            ((TripDetailsActivity) activity).showFailureMessage("Unable to reach the Server. API fetch/response failed. (Code: EC14)");
        }
    }

    @Override
    public void updateAddressInformation(@NonNull String resolvedAddress, double lat, double lng) {
        if (activityViewModel instanceof MapActivityViewModel) {
            ((MapActivityViewModel) activityViewModel).updateNewAddress(resolvedAddress, lat, lng);
        } else if (activityViewModel instanceof TripDetailsActivityViewModel) {
            ((TripDetailsActivityViewModel) activityViewModel).updateNewAddress(resolvedAddress, lat, lng);
        } else if (activityViewModel instanceof ReportActivityViewModel) {
            ((ReportActivityViewModel) activityViewModel).updateNewAddress(resolvedAddress, lat, lng);
        }
    }

    @Override
    public void onDeviceDataCleanupComplete(Bundle dataBundle) {
        if (dataBundle != null && dataBundle.containsKey(BUNDLE_KEY_VEHICLE_DEVICE_DETAIL_LIST)) {
            ArrayList<DeviceData> deviceDetails = dataBundle.getParcelableArrayList(BUNDLE_KEY_VEHICLE_DEVICE_DETAIL_LIST);
            if (deviceDetails != null && deviceDetails.size() > 0) {

                //update data to table via View-Model invocation
                if (activityViewModel instanceof MapActivityViewModel) {
                    ((MapActivityViewModel) activityViewModel).updateDeviceDetails(deviceDetails);
                } else if (activityViewModel instanceof TripDetailsActivityViewModel) {
                    ((TripDetailsActivityViewModel) activityViewModel).updateDeviceDetails(deviceDetails);
                } else if (activityViewModel instanceof ReportActivityViewModel) {
                    ((ReportActivityViewModel) activityViewModel).updateDeviceDetails(deviceDetails);
                }

                // Get the latest "valid" packet info, update to vehicle table and
                // to activity for UI refresh
                DeviceData latestDeviceData = null;
                for (int i = 0; i < deviceDetails.size(); i++) {
                    latestDeviceData = deviceDetails.get(i);
                    if (null != latestDeviceData.getD().getSourceDate()
                            && latestDeviceData.getD().getSourceDate() > 0
                            && null != latestDeviceData.getD()
                            && null != latestDeviceData.getD().getGnssFix()
                            && latestDeviceData.getD().getGnssFix() > 0) {
                        break;
                    }
                }

                if (latestDeviceData != null) {

                    if (activity instanceof MapActivity && activityViewModel instanceof MapActivityViewModel
                            && ((MapActivity) activity).getCurrentVehicleInfo().getLastUpdatedData() != null
                            && ((MapActivity) activity).getCurrentVehicleInfo().getLastUpdatedData().getSourceDate() != null
                            && latestDeviceData.getD().getSourceDate() > ((MapActivity) activity).getCurrentVehicleInfo().getLastUpdatedData().getSourceDate()) {

                        ((MapActivityViewModel) activityViewModel).updateLastUpdatedData(
                                ((MapActivity) activity).getCurrentVehicleInfo().getLastUpdatedData().getSerialNumber(),
                                latestDeviceData.getD(),
                                latestDeviceData.getD().getSourceDate()
                        );

                    } else if (activity instanceof TripDetailsActivity && activityViewModel instanceof TripDetailsActivityViewModel
                            && ((TripDetailsActivity) activity).getCurrentVehicleInfo().getLastUpdatedData() != null
                            && ((TripDetailsActivity) activity).getCurrentVehicleInfo().getLastUpdatedData().getSourceDate() != null
                            && latestDeviceData.getD().getSourceDate() > ((TripDetailsActivity) activity).getCurrentVehicleInfo().getLastUpdatedData().getSourceDate()) {

                        ((TripDetailsActivityViewModel) activityViewModel).updateLastUpdatedData(
                                ((TripDetailsActivity) activity).getCurrentVehicleInfo().getLastUpdatedData().getSerialNumber(),
                                latestDeviceData.getD(),
                                latestDeviceData.getD().getSourceDate()
                        );
                    }
                }
            }
        } else if (dataBundle == null) {
            //No Data use-case
            if (activity instanceof MapActivity) {
                ((MapActivity) activity).setNoData();
            } else if (activity instanceof TripDetailsActivity) {
                ((TripDetailsActivity) activity).setNoData();
            } else if (activity instanceof ReportsActivity) {
                ((ReportsActivity) activity).setNoData();
            }
        } else {
            if (activity instanceof MapActivity) {
                ((MapActivity) activity).showFailureMessage("Unable to reach the Server. API fetch/response failed. (Code: EC11)");
            } else if (activity instanceof TripDetailsActivity) {
                ((TripDetailsActivity) activity).showFailureMessage("Unable to reach the Server. API fetch/response failed. (Code: EC11)");
            } else if (activity instanceof ReportsActivity) {
                ((ReportsActivity) activity).showFailureMessage("Unable to reach the Server. API fetch/response failed. (Code: EC11)");
            }
        }
    }

    @Override
    public void onDeviceDataCleanupFailed(Bundle dataBundle) {
        if (dataBundle == null) {
            //No Data use-case
            if (activity instanceof MapActivity) {
                ((MapActivity) activity).setNoData();
            } else if (activity instanceof TripDetailsActivity) {
                ((TripDetailsActivity) activity).setNoData();
            } else if (activity instanceof ReportsActivity) {
                ((ReportsActivity) activity).setNoData();
            }
        } else {
            if (activity instanceof MapActivity) {
                ((MapActivity) activity).showFailureMessage("Unable to reach the Server. API fetch/response failed. (Code: EC12)");
            } else if (activity instanceof TripDetailsActivity) {
                ((TripDetailsActivity) activity).showFailureMessage("Unable to reach the Server. API fetch/response failed. (Code: EC12)");
            } else if (activity instanceof ReportsActivity) {
                ((ReportsActivity) activity).showFailureMessage("Unable to reach the Server. API fetch/response failed. (Code: EC11)");
            }
        }
    }
}
