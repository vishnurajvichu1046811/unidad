package com.utracx.util.helper;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;

public class KeyBoardHelper {

    public static void hideKeyboard(@NonNull Context context, @NonNull View clickedView) {
        try {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(clickedView.getWindowToken(), 0);
            }
        } catch (Exception e) {
            Log.e("KEYBOARD_HELPER", "hideKeyboard: ", e);
        }
    }
}
