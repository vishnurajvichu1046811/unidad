package com.utracx.api.request.contracts;


import com.utracx.api.model.rest.about.AboutResponseData;
import com.utracx.api.model.rest.device_data.VehicleDeviceResponseData;
import com.utracx.api.model.rest.firebase.FirebaseRequestBody;
import com.utracx.api.model.rest.firebase.FirebaseResponseData;
import com.utracx.api.model.rest.login.LoginData;
import com.utracx.api.model.rest.login.LoginResponseData;
import com.utracx.api.model.rest.login.ProfileUpdateResponse;
import com.utracx.api.model.rest.search.SearchResponse;
import com.utracx.api.model.rest.serial_number.SerialNumberDataResponse;
import com.utracx.api.model.rest.vehicle_count.VehicleCountResponseData;
import com.utracx.api.model.rest.vehicle_list.VehicleInfo;
import com.utracx.api.model.rest.vehicle_list.VehicleListResponseData;
import com.utracx.api.model.rest.vehicleregister_update.UpRegister;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface APIService {

    @POST("user/login?version=v3")
    Call<LoginResponseData> sendLoginData(
            @Query("username") String username,
            @Query("password") String password
    );


    @POST("registrationtoken")
    Call<FirebaseResponseData> registerFirebaseForUser(@Body FirebaseRequestBody requestBody);

    @HTTP(method = "DELETE", path = "registrationtoken", hasBody = true)
    Call<FirebaseResponseData> unregisterFirebaseForUser(@Body FirebaseRequestBody requestBody);


    @GET("vehicles/count")
    Call<VehicleCountResponseData> sendVehicleCountData(
            @Query("username") String username,
            @Query("password") String password
    );

    @GET("users?version=v3")
    Call<VehicleListResponseData> sendVehicleListData(
            @Query("role") String role,
            @Query("start") String start,
            @Query("length") String length,
            @Query("username") String username,
            @Query("password") String password
    );

    @GET("devices/{reg_no}")
    Call<SerialNumberDataResponse> getSerialNumber(
            @Path("reg_no") String registrationNumber,
            @Query("username") String username,
            @Query("password") String password
    );

    @GET("data?version=v2&length=17280&source_date=true&start=0")
    Call<VehicleDeviceResponseData> getOptimisedVehicleDeviceDataV2(
            @Query("serial_no") String serialNo,
            @Query("username") String username,
            @Query("password") String password,
            @Query("start_time") String startTime,
            @Query("end_time") String endTime
    );

//    @GET("data?version=v2&version=v1&d.mobile_alert=true")
    @GET("data?version=v2&d.mobile_alert=true")
    Call<VehicleDeviceResponseData> getAlertsForDevice(
            @Query("start_time") String startTime, //24 hours from now -- probably
            @Query("end_time") String endTime, // last updated time -- probably
            @Query("username") String username,
            @Query("password") String password,
            @Query("serial_no") String serialNumber
    );

    @GET("devices/assigned/vehicles")
    Call<SearchResponse> getVehicleKeyWord(
            @Query("username") String username,
            @Query("password") String password,
            @Query("term") String term
    );

    @GET("static/apis")
    Call<AboutResponseData> getAboutURLs();

    @PUT("user/profile/update")
    Call<ProfileUpdateResponse> updateProfile(
            @Query("username") String username,
            @Query("password") String password,
            @Body LoginData body
    );

    @PUT("vehicles/{reg_no}")
    Call<ProfileUpdateResponse> updateRegistration(
            @Path("reg_no") String registrationNumber,
            @Query("username") String username,
            @Query("password") String password,
            @Body UpRegister body
    );

}