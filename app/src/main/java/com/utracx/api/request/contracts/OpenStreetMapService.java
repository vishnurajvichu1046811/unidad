package com.utracx.api.request.contracts;

import com.utracx.api.model.osm.nominatim.NominatimData;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface OpenStreetMapService {
    @GET("reverse?format=json")
    Call<NominatimData> getAddressFromLatLon(@Query("lat") double lat, @Query("lon") double lon);
}