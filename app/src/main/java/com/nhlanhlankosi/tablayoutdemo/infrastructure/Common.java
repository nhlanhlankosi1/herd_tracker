package com.nhlanhlankosi.tablayoutdemo.infrastructure;

import android.app.Activity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;

import com.nhlanhlankosi.tablayoutdemo.models.Cow;
import com.nhlanhlankosi.tablayoutdemo.models.User;

public class Common {
    public static final double NORMAL_COW_TEMPERATURE = 38.5;
    public static final double ALARMING_COW_TEMPERATURE = 39.5; //TEMP OVER 39.5°C MAY INDICATES FEVER

    private Common() {
    }

    public static void closeKeyboard(@NonNull Activity activity) {

        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }

        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

    }
}
