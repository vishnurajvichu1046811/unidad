package com.utracx.api.request;

import android.util.Log;

import com.utracx.api.request.contracts.APIService;
import com.utracx.api.request.contracts.OpenStreetMapService;

import static com.utracx.api.request.APIRetrofitClient.getRetrofitClient;
import static com.utracx.api.request.OpenStreetMapRetrofitClient.getOpenStreetMapClient;
import static com.utracx.api.request.OpenStreetMapRetrofitClient.osmClient;

public class ApiUtils {
    private static final ApiUtils INSTANCE = new ApiUtils();

    private APIService SO_SERVICE;
    private OpenStreetMapService OPEN_STREET_MAP_SERVICE;

    private ApiUtils() {
        SO_SERVICE = getRetrofitClient().create(APIService.class);
        OPEN_STREET_MAP_SERVICE = getOpenStreetMapClient().create(OpenStreetMapService.class);
    }

    public static ApiUtils getInstance() {
        return INSTANCE;
    }

    public static void cancelAllSOSRequest() {
        APIRetrofitClient.cancelAllWebCalls();
    }

    public static void cancelAllOpenStreetMapsRequest() {
        if (osmClient != null && osmClient.dispatcher().queuedCallsCount() > 0) {
            osmClient.dispatcher().cancelAll();
        }
    }

    public APIService getSOService() {

        return SO_SERVICE;
    }

    public OpenStreetMapService getOpenStreetMapService() {
        return OPEN_STREET_MAP_SERVICE;
    }
}
