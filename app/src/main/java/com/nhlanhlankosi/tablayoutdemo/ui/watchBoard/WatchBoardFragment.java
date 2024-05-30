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
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
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

    private TextView cowNamesTxt;
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

        cowNamesTxt = view.findViewById(R.id.cow_names_tv);
        dataNotFoundTxt = view.findViewById(R.id.data_not_found_txt);
        parentLinearLayout = view.findViewById(R.id.parent_linear_layout);
        temperatureChartContainer = view.findViewById(R.id.temperature_chart_container);
        motionSensorChartContainer = view.findViewById(R.id.motion_sensor_chart_container);
        heartRateChartContainer = view.findViewById(R.id.heart_rate_chart_container);
        dataNotFoundTxt.setVisibility(View.VISIBLE);
        dataNotFoundTxt.setText("To show cattle data, enter the cow name above");
        parentLinearLayout.setVisibility(View.GONE);
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

                    CowSensorData cowSensorData;

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

                    printOutCowNamesData();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        cattleSensorDataRef.addValueEventListener(cattleSensorDataRefListener);

        return view;
    }

    private void printOutCowNamesData() {
        // Print cow names
        // Create a StringBuilder and append the initial text
        StringBuilder cowNames = new StringBuilder("The cows in your herd are: ");

        // Append cow names
        for (CowSensorData cowSensorData : allCattleSensorDataList) {
            cowNames.append(cowSensorData.getCowName()).append(", ");
        }

        // Remove the last comma and space if the list is not empty
        if (cowNames.length() > "The cows in your herd are: ".length()) {
            cowNames.setLength(cowNames.length() - 2);
        }

        cowNamesTxt.setText(cowNames.toString());

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

        if (!cowName.isEmpty()) {

            //From the list of Cattle sensor data, get the data for the cow with name `cowName`
            CowSensorData cowSensorData = getCowSensorDataByName(allCattleSensorDataList, cowName);

            if (cowSensorData == null) {
                dataNotFoundTxt.setVisibility(View.VISIBLE);
                dataNotFoundTxt.setText("No data found. Make sure you have a cow named: " + cowName);
                parentLinearLayout.setVisibility(View.GONE);
                return;
            } else {
                dataNotFoundTxt.setVisibility(View.GONE);
                parentLinearLayout.setVisibility(View.VISIBLE);
            }

            // Add the entries to the respective data sets as before
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

            // Create and customize the data sets
            LineDataSet temperatureDataSet = new LineDataSet(temperatureEntries, "Temperature");
            LineDataSet motionSensorDataSet = new LineDataSet(motionSensorEntries, "Motion Sensor Data");
            LineDataSet heartRateDataSet = new LineDataSet(heartRateEntries, "Heart Rate");

// Customize and populate the charts
            customizeChart(temperatureChart, temperatureDataSet, "Temperature Over Time", Color.RED);
            customizeChart(motionSensorChart, motionSensorDataSet, "Motion Sensor Data Over Time", Color.BLUE);
            customizeChart(heartRateChart, heartRateDataSet, "Heart Rate Over Time", Color.GREEN);

        } else {
            // Show error message if cow name is empty
            Toast.makeText(getContext(), "Please enter a cow name", Toast.LENGTH_SHORT).show();
        }
    }

    private void customizeChart(LineChart chart, LineDataSet dataSet, String descriptionText, int color) {
        // Customize the data set
        dataSet.setColor(color);
        dataSet.setCircleColor(color);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(false);
        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(color);

        // Customize the chart description
        Description description = new Description();
        description.setText(descriptionText);
        description.setTextSize(12f);
        chart.setDescription(description);

        // Customize the chart legend
        Legend legend = chart.getLegend();
        legend.setForm(Legend.LegendForm.LINE);
        legend.setTextSize(12f);
        legend.setTextColor(color);

        // Customize the X axis
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                // Custom formatting for X axis labels
                return "Time"; // Replace with actual time formatting if necessary
            }
        });

        // Customize the Y axis
        YAxis leftAxis = chart.getAxisLeft();
        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false); // Disable right Y axis

        // Apply data to the chart
        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.invalidate(); // Refresh the chart
    }


    @Override
    public void onDetach() {
        super.onDetach();

        if (cattleSensorDataRef != null && cattleSensorDataRefListener != null) {
            cattleSensorDataRef.removeEventListener(cattleSensorDataRefListener);
        }

    }

}
