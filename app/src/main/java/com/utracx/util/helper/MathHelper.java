package com.utracx.util.helper;

import android.util.Log;

public class MathHelper {
    private static final String TAG = "MathHelper";

    public static double round(Double value, int precision) {
        if (value != null) {
            try {
                int scale = (int) Math.pow(10, precision);
                return (double) Math.round(value * scale) / scale;
            } catch (Exception e) {
                Log.e(TAG, "round: failed to parse", e);
            }
        }
        return 0.0d;
    }

    public static float roundFloat(float value, int precision) {
        try {
            int scale = (int) Math.pow(10, precision);
            return (float) Math.round(value * scale) / scale;
        } catch (Exception e) {
            Log.e(TAG, "round: failed to parse", e);
        }
        return 0.0f;
    }
}
