package com.utracx.util.helper;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.utracx.api.model.rest.about.AboutResponseData;
import com.utracx.api.model.rest.login.LoginData;
import com.utracx.api.model.rest.login.LoginResponseData;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import static android.content.Context.MODE_PRIVATE;

public final class SharedPreferencesHelper {
    private static final String LOGIN_STATE_PREFERENCE = "LoginState";
    private static final String USERNAME_PREFERENCE_KEY = "Username";
    private static final String PASSWORD_PREFERENCE_KEY = "Password";
    private static final String UUID_PREFERENCE_KEY = "UUID";
    private static final String UMAIL_PREFERENCE_KEY = "UMAIL";
    private static final String UMOB_PREFERENCE_KEY = "UMOB";
    private static final String UFULLNAME_PREFERENCE_KEY = "UFNAME";
    private static final String UADDRESS_PREFERENCE_KEY = "UADDRESS";
    private static final String HOME_URL_PREF_KEY = "home_url";
    private static final String CONTACT_URL_PREF_KEY = "contact_url";
    private static final String PRIVACY_URL_PREF_KEY = "privacy_url";
    private static final String TERMS_URL_PREF_KEY = "terms_url";
    private static final String USER_DETAILS = "current_user_details";

    private SharedPreferencesHelper() {
    }

    public static void saveUserEmail(@NonNull Activity activity, @NonNull String username) {
        activity.getSharedPreferences(LOGIN_STATE_PREFERENCE, MODE_PRIVATE).edit()
                .putString(USERNAME_PREFERENCE_KEY, username)
                .apply();
    }

    public static String getUserEmail(@NonNull Activity activity) {
        return activity.getSharedPreferences(LOGIN_STATE_PREFERENCE, MODE_PRIVATE)
                .getString(USERNAME_PREFERENCE_KEY, "");
    }

    public static void saveUserEmail_(@NonNull Activity activity, @NonNull String useremail) {
        activity.getSharedPreferences(LOGIN_STATE_PREFERENCE, MODE_PRIVATE).edit()
                .putString(UMAIL_PREFERENCE_KEY, useremail)
                .apply();
    }

    public static String getUserEmail_(@NonNull Activity activity) {
        return activity.getSharedPreferences(LOGIN_STATE_PREFERENCE, MODE_PRIVATE)
                .getString(UMAIL_PREFERENCE_KEY, "");
    }



    public static void deleteUsernamePassword(@NonNull Activity activity) {
        activity.getSharedPreferences(LOGIN_STATE_PREFERENCE, MODE_PRIVATE).edit()
                .putString(USERNAME_PREFERENCE_KEY, "")
                .putString(PASSWORD_PREFERENCE_KEY, "")
                .apply();
    }

    public static void saveUserMobile(@NonNull Activity activity, @NonNull String umobile) {
        activity.getSharedPreferences(LOGIN_STATE_PREFERENCE, MODE_PRIVATE).edit()
                .putString(UMOB_PREFERENCE_KEY, umobile)
                .apply();
    }

    public static String getUserMobile(@NonNull Activity activity) {
        return activity.getSharedPreferences(LOGIN_STATE_PREFERENCE, MODE_PRIVATE)
                .getString(UMOB_PREFERENCE_KEY, "");
    }
    public static void saveUserFullname(@NonNull Activity activity, @NonNull String ufullname) {
        activity.getSharedPreferences(LOGIN_STATE_PREFERENCE, MODE_PRIVATE).edit()
                .putString(UFULLNAME_PREFERENCE_KEY, ufullname)
                .apply();
    }

    public static String getUserFullname(@NonNull Activity activity) {
        return activity.getSharedPreferences(LOGIN_STATE_PREFERENCE, MODE_PRIVATE)
                .getString(UFULLNAME_PREFERENCE_KEY, "");
    }

    public static void saveUserAddress(@NonNull Activity activity, @NonNull String uaddress) {
        activity.getSharedPreferences(LOGIN_STATE_PREFERENCE, MODE_PRIVATE).edit()
                .putString(UADDRESS_PREFERENCE_KEY, uaddress)
                .apply();
    }

    public static String getUserAddress(@NonNull Activity activity) {
        return activity.getSharedPreferences(LOGIN_STATE_PREFERENCE, MODE_PRIVATE)
                .getString(UADDRESS_PREFERENCE_KEY, "");
    }


    public static void saveUserPassword(@NonNull Activity activity, @NonNull String upass) {
        activity.getSharedPreferences(LOGIN_STATE_PREFERENCE, MODE_PRIVATE).edit()
                .putString(PASSWORD_PREFERENCE_KEY, upass)
                .apply();
    }

    public static String getPassword(@NonNull Activity activity) {
        return activity.getSharedPreferences(LOGIN_STATE_PREFERENCE, MODE_PRIVATE)
                .getString(PASSWORD_PREFERENCE_KEY, "");
    }

    public static void saveUUID(@NonNull Activity activity, String uniqueID) {
        activity.getSharedPreferences(LOGIN_STATE_PREFERENCE, MODE_PRIVATE).edit()
                .putString(UUID_PREFERENCE_KEY, uniqueID)
                .apply();
    }

    public static void deleteUUID(@NonNull Activity activity) {
        activity.getSharedPreferences(LOGIN_STATE_PREFERENCE, MODE_PRIVATE).edit()
                .putString(UUID_PREFERENCE_KEY, "")
                .apply();
    }

    public static String getUUID(@NonNull Activity activity) {
        return activity.getSharedPreferences(LOGIN_STATE_PREFERENCE, MODE_PRIVATE)
                .getString(UUID_PREFERENCE_KEY, "");
    }


    public static void clearPreferences(@NonNull Activity activity) {
        activity.getSharedPreferences(LOGIN_STATE_PREFERENCE, MODE_PRIVATE)
                .edit().clear().apply();
    }

    @Nullable
    public static AboutResponseData getAboutResponseData(@NotNull Activity activity) {
        SharedPreferences prefs = activity.getSharedPreferences(LOGIN_STATE_PREFERENCE, MODE_PRIVATE);

        if (prefs.contains(CONTACT_URL_PREF_KEY) && prefs.contains(PRIVACY_URL_PREF_KEY)
                && prefs.contains(HOME_URL_PREF_KEY) && prefs.contains(TERMS_URL_PREF_KEY)
                && prefs.getString(HOME_URL_PREF_KEY, null) != null
                && prefs.getString(CONTACT_URL_PREF_KEY, null) != null
                && prefs.getString(PRIVACY_URL_PREF_KEY, null) != null
                && prefs.getString(TERMS_URL_PREF_KEY, null) != null) {

            try {
                return new AboutResponseData(
                        prefs.getString(HOME_URL_PREF_KEY, null),
                        prefs.getString(CONTACT_URL_PREF_KEY, null),
                        prefs.getString(PRIVACY_URL_PREF_KEY, null),
                        prefs.getString(TERMS_URL_PREF_KEY, null)
                );
            } catch (Exception e) {
                Log.e("SHARED_PREF_UTIL", "getAboutResponseData: ", e);
            }
        }

        return null;
    }

    public static void saveAboutURL(@NonNull Activity activity,
                                    @NonNull String homePageURL,
                                    @NonNull String contactUsURL,
                                    @NonNull String privacyPolicyURL,
                                    @NonNull String termsAndConditionsURL) {
        activity.getSharedPreferences(LOGIN_STATE_PREFERENCE, MODE_PRIVATE).edit()
                .putString(HOME_URL_PREF_KEY, homePageURL)
                .putString(CONTACT_URL_PREF_KEY, contactUsURL)
                .putString(PRIVACY_URL_PREF_KEY, privacyPolicyURL)
                .putString(TERMS_URL_PREF_KEY, termsAndConditionsURL)
                .apply();
    }


    public static Object bundleToJson(Bundle bundle) {
        if (bundle == null) return null;
        JSONObject jsonObject = new JSONObject();

        for (String key : bundle.keySet()) {
            Object obj = bundle.get(key);
            try {
                jsonObject.put(key,bundle.get(key));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return jsonObject.toString();

    }

    public static void saveUserDetails(Activity activity,String userJson) {
        activity.getSharedPreferences(LOGIN_STATE_PREFERENCE, MODE_PRIVATE).edit()
                .putString(USER_DETAILS, userJson)
                .apply();
    }
    public static LoginData getUserDetails(Activity activity){
        Gson gson = new Gson();
        if(fetchUserFromSharedPref(activity) != null) {
            JSONObject userObj = fetchUserFromSharedPref(activity);
            LoginData userData = gson.fromJson(userObj.toString(),LoginData.class);

            return userData;
        }
        return null;
    }

    public static JSONObject fetchUserFromSharedPref(Activity activity){
        try {
            return new JSONObject(activity.getSharedPreferences(LOGIN_STATE_PREFERENCE, MODE_PRIVATE).getString(USER_DETAILS, ""));
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
