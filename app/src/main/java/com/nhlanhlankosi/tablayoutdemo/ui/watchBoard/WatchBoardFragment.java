package com.nhlanhlankosi.tablayoutdemo.ui.watchBoard;

import static com.nhlanhlankosi.tablayoutdemo.infrastructure.Common.getCowSensorDataByName;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.nhlanhlankosi.tablayoutdemo.R;
import com.nhlanhlankosi.tablayoutdemo.infrastructure.SharedPreferencesHelper;
import com.nhlanhlankosi.tablayoutdemo.models.CowLocation;
import com.nhlanhlankosi.tablayoutdemo.models.CowSensorData;
import com.nhlanhlankosi.tablayoutdemo.models.User;

import java.util.ArrayList;
import java.util.List;

public class WatchBoardFragment extends Fragment {

    ArrayList<CowSensorData> allCattleSensorDataList = new ArrayList<>();
    private LinearLayout parentLinearLayout;
    private LinearLayout temperatureChartContainer;
    private LinearLayout motionSensorChartContainer;
    private LinearLayout heartRateChartContainer;
    private TextView temperatureHeading;
    private TextView motionSensorHeading;
    private TextView heartRateHeading;
    private TextView dataNotFoundTxt;
    private LineChart temperatureChart;
    private LineChart motionSensorChart;
    private LineChart heartRateChart;
    private Toolbar toolbar;

    private DatabaseReference cattleSensorDataRef;

    private ValueEventListener cattleSensorDataRefListener;

    public WatchBoardFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        User currentUser = SharedPreferencesHelper.getUser(this.requireContext());
        cattleSensorDataRef = FirebaseDatabase.getInstance().getReference("cattle_sensor_data")
                .child(currentUser.getUserId());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_watch_board, container, false);

        toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);

        dataNotFoundTxt = view.findViewById(R.id.data_not_found_txt);
        parentLinearLayout = view.findViewById(R.id.gridLayout);
        temperatureChartContainer = view.findViewById(R.id.temperature_chart_container);
        motionSensorChartContainer = view.findViewById(R.id.motion_sensor_chart_container);
        heartRateChartContainer = view.findViewById(R.id.heart_rate_chart_container);
        dataNotFoundTxt.setVisibility(View.GONE);
        temperatureHeading = view.findViewById(R.id.temperature_heading);
        motionSensorHeading = view.findViewById(R.id.motion_sensor_heading);
        heartRateHeading = view.findViewById(R.id.heart_rate_heading);
        temperatureChart = view.findViewById(R.id.temperature_chart);
        motionSensorChart = view.findViewById(R.id.motion_sensor_chart);
        heartRateChart = view.findViewById(R.id.heart_rate_chart);

        // Get the width of the screen
        DisplayMetrics displayMetrics = new DisplayMetrics();
        requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;

        // Set the width of the LinearLayout to half of the screen width
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(screenWidth / 2, LinearLayout.LayoutParams.WRAP_CONTENT);

        temperatureChartContainer.setLayoutParams(layoutParams);
        motionSensorChartContainer.setLayoutParams(layoutParams);
        heartRateChartContainer.setLayoutParams(layoutParams);
        cattleSensorDataRefListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    allCattleSensorDataList.clear();

                    CowSensorData cowSensorData = new CowSensorData();

                    for (DataSnapshot cowSensorDataSnapShot : snapshot.getChildren()) {

                        GenericTypeIndicator<CowSensorData> cowSensorDataGenericTypeIndicator
                                = new GenericTypeIndicator<CowSensorData>() {
                            @NonNull
                            @Override
                            public String toString() {
                                return super.toString();
                            }
                        };

                        cowSensorData = cowSensorDataSnapShot.getValue(cowSensorDataGenericTypeIndicator);

                        allCattleSensorDataList.add(cowSensorData);

                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        cattleSensorDataRef.addValueEventListener(cattleSensorDataRefListener);

        return view;
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_my_herd_search_all_cattle_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);

        MenuItem menuItem = menu.findItem(R.id.my_herd_fragment_search_view);
        menuItem.expandActionView();
        menuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return false;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                closeKeyboard();
                return true;
            }
        });

        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setQueryHint(getString(R.string.search_for_a_cow));

        // Change the text color of query text and hint
        int textColor = Color.parseColor("#888888"); // Light grey color
        int hintColor = Color.parseColor("#888888"); // Light grey color

        // Find the EditText inside the SearchView
        EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        // Change the text color
        searchEditText.setTextColor(textColor);
        // Change the hint color
        searchEditText.setHintTextColor(hintColor);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String cowName) {
                fetchAndDisplayData(cowName);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void closeKeyboard() {
        View view = this.requireActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) this.requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void fetchAndDisplayData(String cowName) {
        // Get cow name from EditText input

        if (!cowName.isEmpty()) {

            //From the list of Cattle sensor data, get the data for the cow with name `cowName`
            CowSensorData cowSensorData = getCowSensorDataByName(allCattleSensorDataList, cowName);

            if (cowSensorData == null) {
                dataNotFoundTxt.setVisibility(View.VISIBLE);
                parentLinearLayout.setVisibility(View.GONE);
                return;
            } else {
                dataNotFoundTxt.setVisibility(View.GONE);
                parentLinearLayout.setVisibility(View.VISIBLE);
            }

            List<Entry> temperatureEntries = new ArrayList<>();
            for (int i = 0; i < cowSensorData.getTemperatures().size(); i++) {
                temperatureEntries.add(new Entry(i + 1, new Float(cowSensorData.getTemperatures().get(i))));
            }

            List<Entry> motionSensorEntries = new ArrayList<>();
            for (int i = 0; i < cowSensorData.getLocations().size(); i++) {
                CowLocation location = cowSensorData.getLocations().get(i);
                motionSensorEntries.add(new Entry(i + 1, new Float(location.getLatitude()))); // Assuming you want to use latitude as the motion sensor value
            }

            List<Entry> heartRateEntries = new ArrayList<>();
            for (int i = 0; i < cowSensorData.getHeartRates().size(); i++) {
                heartRateEntries.add(new Entry(i + 1, cowSensorData.getHeartRates().get(i)));
            }

            // Populate temperature chart
            LineDataSet temperatureDataSet = new LineDataSet(temperatureEntries, "Temperature");
            temperatureDataSet.setDrawCircles(true);
            LineData temperatureData = new LineData(temperatureDataSet);
            temperatureChart.setData(temperatureData);
            temperatureChart.invalidate();

            // Populate motionSensor chart
            LineDataSet motionSensorData = new LineDataSet(motionSensorEntries, "Motion Sensor Data");
            motionSensorData.setDrawCircles(true);
            LineData motionData = new LineData(motionSensorData);
            motionSensorChart.setData(motionData);
            motionSensorChart.invalidate();

            // Populate heart rate chart
            LineDataSet heartRateDataSet = new LineDataSet(heartRateEntries, "Heart Rate");
            heartRateDataSet.setDrawCircles(true);
            LineData heartRateData = new LineData(heartRateDataSet);
            heartRateChart.setData(heartRateData);
            heartRateChart.invalidate();

        } else {
            // Show error message if cow name is empty
            Toast.makeText(getContext(), "Please enter a cow name", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        if (cattleSensorDataRef != null && cattleSensorDataRefListener != null) {
            cattleSensorDataRef.removeEventListener(cattleSensorDataRefListener);
        }

    }

}
