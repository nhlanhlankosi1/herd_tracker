package com.nhlanhlankosi.tablayoutdemo.infrastructure;

import android.app.Activity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;

import com.nhlanhlankosi.tablayoutdemo.models.CowSensorData;

import java.util.ArrayList;

public class Common {
    public static final double NORMAL_COW_TEMPERATURE = 38.5;
    public static final double ALARMING_COW_TEMPERATURE = 39.5; //TEMP OVER 39.5°C MAY INDICATES FEVER

    private Common() {
    }

    // Method to find an object by id
    public static CowSensorData getCowSensorDataByName(ArrayList<CowSensorData> allCattleSensorDataList,
                                                       String cowName) {
        for (CowSensorData cowSensorData : allCattleSensorDataList) {
            if (cowSensorData.getCowName().equals(cowName)) {
                return cowSensorData;
            }
        }
        return null;
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

    public static double getRandomCowTemperature() {
        // Define the range of normal body temperature for cows (38.5 - 39.5°C)
        double minTemp = 38.5;
        double maxTemp = 39.5;

        // Generate a random temperature within the range
        double randomTemp = minTemp + (Math.random() * (maxTemp - minTemp));

        // Round the temperature to one decimal place
        return Math.round(randomTemp * 10.0) / 10.0;
    }

    public static long getRandomCowHeartRate() {
        // Define the range of normal heart rate for cows (48 - 84 beats per minute)
        long minHeartRate = 48;
        long maxHeartRate = 84;

        // Generate a random heart rate within the range
        return (long) (Math.random() * (maxHeartRate - minHeartRate) + minHeartRate);
    }
}
