package com.utracx.util;

import android.text.style.AbsoluteSizeSpan;

public final class ConstantVariables {
    public static final int SPEED_ROUND = 1;
    public static final int STATE_LOADING_DATA = 1;
    public static final int STATE_NO_DATA = 2;
    public static final int STATE_DATA_LOADED = 3;
    public static final String KEY_DATE_SELECTED = "key_selected_date";
    public static final long ONE_DAY_END_MILLISECOND = 86399999L;
    public static final String VEHICLE_MOVE_TYPE = "vehicle_move_type";
    public static final String VEHICLE_MODE_MOVING = "M";
    public static final String VEHICLE_MODE_IDLE = "H";
    public static final String VEHICLE_MODE_SLEEP = "S";
    public static final String VEHICLE_MODE_NON_COMMUNICATING = "NA";
    public static final String VEHICLE_MODE_ONLINE = "ONLINE";
    public static final String VEHICLE_MODE_ALL = "ALL";
    public static final long THRESHOLD_TIME_FOR_P_AND_S_MARKER = 1200000; //20 mins - 20*60*1000
    public static final long THRESHOLD_TIME_FOR_NON_COMM_VEHICLE = 600000; //10 mins
    public static final float THRESHOLD_DISTANCE_FOR_ADDRESS = 0.05f; //50 mtrs
    public static final float THRESHOLD_DISTANCE_FOR_MARKER_ADD = 0.150f;
    public static final double TOTAL_DISTANCE_THRESHOLD_FOR_INVALID_ROUTE = 15.0;
    public static final double MAX_SPEED_THRESHOLD_FOR_INVALID_ROUTE = 10.0;
    public static final AbsoluteSizeSpan HEADING_SPAN = new AbsoluteSizeSpan(16, true);
    public static final AbsoluteSizeSpan NORMAL_SPAN = new AbsoluteSizeSpan(10, true);
    public static final String REPORTS_WEB_URL = "http://web.utracx.com:8080/utracx/report?username=%s&password=%s";
    public static final String REMOTE_MESSAGE_BUNDLE_KEY = "REMOTE_MESSAGE_BUNDLE_KEY";
    public static final int NOTIFICATION_REQUEST_CODE = 1001;
    public static final Integer APP_UPDATE_REQUEST_CODE = 1002;

    private ConstantVariables() {
    }
}
