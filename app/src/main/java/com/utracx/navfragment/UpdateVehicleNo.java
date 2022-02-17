package com.utracx.navfragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.utracx.R;
import com.utracx.api.model.rest.login.LoginData;
import com.utracx.api.model.rest.login.ProfileUpdateResponse;
import com.utracx.api.model.rest.vehicle_list.VehicleInfo;
import com.utracx.api.model.rest.vehicleregister_update.UpRegister;
import com.utracx.api.request.ApiUtils;
import com.utracx.api.request.calls.LoginRequestCall;
import com.utracx.api.request.calls.VehicleListRequestCall;
import com.utracx.api.request.interfaces.LoginCallback;
import com.utracx.api.request.interfaces.VehicleListCallback;
import com.utracx.api.request.interfaces.VehicleUpdateItemClick;
import com.utracx.database.AppDatabase;
import com.utracx.database.datamodel.UserDataEntity;
import com.utracx.util.helper.SharedPreferencesHelper;
import com.utracx.view.adapter.AlertAdapter;
import com.utracx.view.adapter.UpdateVehicleAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.utracx.api.request.calls.LoginRequestCall.BUNDLE_KEY_UPDATE_VEHICLE;
import static com.utracx.api.request.calls.LoginRequestCall.BUNDLE_KEY_USER_ADDRESS;
import static com.utracx.api.request.calls.LoginRequestCall.BUNDLE_KEY_USER_EMAIL_;
import static com.utracx.api.request.calls.LoginRequestCall.BUNDLE_KEY_USER_FULLNAME;
import static com.utracx.api.request.calls.LoginRequestCall.BUNDLE_KEY_USER_MOB;
import static com.utracx.api.request.calls.LoginRequestCall.BUNDLE_KEY_USER_PASSWORD;
import static com.utracx.api.request.calls.VehicleListRequestCall.BUNDLE_KEY_VEHICLE_LIST;
import static com.utracx.util.ConstantVariables.STATE_NO_DATA;
import static com.utracx.util.helper.SharedPreferencesHelper.getPassword;
import static com.utracx.util.helper.SharedPreferencesHelper.getUserEmail;

public class UpdateVehicleNo extends AppCompatActivity implements VehicleListCallback {

    private final VehicleListRequestCall vehicleListRequestCall = new VehicleListRequestCall(this);
    private RecyclerView rvVehicles;
    String uPass, userMail, currentReg;
    LoginData loginData;
    String currentRegNo = "";
    UserDataEntity data = null;
    ArrayList<VehicleInfo> vehicles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_vehicle_no);
        rvVehicles = findViewById(R.id.rv_upvehicle_no);


        findViewById(R.id.backView).setOnClickListener(backView -> finish());

        data = AppDatabase.getDatabase(this).userDetailsDao().getUserDataByEmail(
                SharedPreferencesHelper.getUserEmail(this)
        );

        userMail = getUserEmail(this);
        uPass = data.getPassword();

        loadVehicleList(uPass);
    }


    private void loadVehicleList(String pass) {

                        ApiUtils.getInstance().getSOService()
                                .sendVehicleListData("vehicle", "0", "3500", getUserEmail(this), pass)
                                .enqueue(vehicleListRequestCall);

    }


    @Override
    public void onVehicleListFetched(Bundle dataBundle) {

        if (dataBundle != null && dataBundle.containsKey(BUNDLE_KEY_VEHICLE_LIST)) {
            ArrayList<VehicleInfo> vehiclesArrayList = dataBundle.getParcelableArrayList(BUNDLE_KEY_VEHICLE_LIST);
            vehicles = vehiclesArrayList;
            setVehiclesList(vehiclesArrayList);
        }

    }

    private void setVehiclesList(ArrayList<VehicleInfo> vehiclesArrayList) {


        rvVehicles.setLayoutManager(new LinearLayoutManager(this));
        UpdateVehicleAdapter vehicleAdapter = new UpdateVehicleAdapter(this, vehiclesArrayList, new VehicleUpdateItemClick() {
            @Override
            public void onVehicleUpdateItemClicked(VehicleInfo vehicleInfo, int position) {


                currentRegNo = vehiclesArrayList.get(position).getVehicleRegistration();

                final View view = getLayoutInflater().inflate(R.layout.alert_vehicle_data, null);
                AlertDialog.Builder builder1 = new AlertDialog.Builder(UpdateVehicleNo.this, R.style.AlertDialog1);
                builder1.setMessage("Update vehicle details");

                final EditText etRegNo = (EditText) view.findViewById(R.id.et_vehicleno);
                etRegNo.setText(currentRegNo);

                builder1.setCancelable(true);

                builder1.setPositiveButton(
                        "Update",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                if(currentRegNo.equals(etRegNo.getText().toString().trim())){
                                    Toast.makeText(UpdateVehicleNo.this, "No change detected", Toast.LENGTH_SHORT).show();
                                }else if(etRegNo.getText().toString().trim().isEmpty()){
                                    Toast.makeText(UpdateVehicleNo.this, "Field cannot be empty", Toast.LENGTH_SHORT).show();
                                }else{
                                    //TODO update new reg No
                                    UpRegister upRegister = new UpRegister();
                                    upRegister.setVehicle_registration(etRegNo.getText().toString().trim());
                                    updateFromFleet(upRegister, currentRegNo);

                                    dialog.dismiss();
//                                    dialog.cancel();


                                }

                            }
                        });

                builder1.setNegativeButton(
                        "Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert11 = builder1.create();
                alert11.setView(view);
                alert11.show();

                currentReg = vehiclesArrayList.get(position).getVehicleRegistration();

                for(VehicleInfo vehicleInfo1 : vehiclesArrayList){
                    vehicleInfo1.isEditClicked = false;
                }
                vehiclesArrayList.get(position).isEditClicked = true;
                rvVehicles.getAdapter().notifyDataSetChanged();

            }

            @Override
            public void onUpdateClicked(VehicleInfo vehicleInfo, int position, String newregister) {

                    UpRegister upRegister = new UpRegister();
                    upRegister.setVehicle_registration(newregister);
                    updateFromFleet(upRegister, currentReg);

            }

            @Override
            public void onCancelClicked(VehicleInfo vehicleInfo, int position) {

            }
        });
        rvVehicles.setAdapter(vehicleAdapter);
    }

    private void updateFromVehicle(LoginData loginData,String currentReg) {

        ApiUtils.getInstance().getSOService().updateProfile(userMail, uPass, loginData).enqueue(new Callback<ProfileUpdateResponse>() {
            @Override
            public void onResponse(Call<ProfileUpdateResponse> call, Response<ProfileUpdateResponse> response) {
                Log.e("", "");
                loadVehicleList(uPass);
            }

            @Override
            public void onFailure(Call<ProfileUpdateResponse> call, Throwable t) {
                loadVehicleList(uPass);
            }
        });
    }
    private void updateFromFleet(UpRegister upRegister, String currentReg) {

        Log.i("old number",currentReg);
        Log.i("new number",upRegister.getVehicle_registration());

        try{

            ApiUtils.getInstance().getSOService().updateRegistration(currentReg, userMail, uPass, upRegister).enqueue(new Callback<ProfileUpdateResponse>() {
                @Override
                public void onResponse(Call<ProfileUpdateResponse> call, Response<ProfileUpdateResponse> response) {

                    LoginData loginData = new LoginData();
                    loginData.setVehicle_registration(upRegister.getVehicle_registration());

                    updateFromVehicle(loginData, currentReg);

                }

                @Override
                public void onFailure(Call<ProfileUpdateResponse> call, Throwable t) {

                    loadVehicleList(uPass);
                }
            });
        }catch(Exception e){

            Log.e("error",e.getMessage());
        }
    }

    @Override
    public void onVehicleListFetchFailed(Bundle data) {

        Log.e("","");

    }

    @Override
    public void onVehicleListFetchError(@Nullable Bundle data) {

        Log.e("","");
    }
}