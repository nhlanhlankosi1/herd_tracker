package com.nhlanhlankosi.tablayoutdemo.activities;

import static com.nhlanhlankosi.tablayoutdemo.activities.CowInfoActivity.COW_NAME;
import static com.nhlanhlankosi.tablayoutdemo.activities.CowInfoActivity.LATITUDE;
import static com.nhlanhlankosi.tablayoutdemo.activities.CowInfoActivity.LONGITUDE;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nhlanhlankosi.tablayoutdemo.R;

public class CowLocationActivity extends FragmentActivity implements OnMapReadyCallback {

    private final Double INITIAL_LATITUDE = -20.1563161;
    private final Double INITIAL_LONGITUDE = 28.5820653;
    private GoogleMap mMap;
    private LatLng pinLocation;
    private Marker pinMarker;
    private String cowName = "Cow";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cow_location);

        // Retrieve latitude and longitude values from the intent extras
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey(COW_NAME)
                && extras.containsKey(LATITUDE) && extras.containsKey(LONGITUDE)) {
            cowName = extras.getString(COW_NAME);
            double latitude = extras.getDouble(LATITUDE);
            double longitude = extras.getDouble(LONGITUDE);
            pinLocation = new LatLng(latitude, longitude);
        } else {
            // Handle the case where latitude and longitude values are not provided
            // For example, display a default location or show an error message
            pinLocation = new LatLng(INITIAL_LATITUDE, INITIAL_LONGITUDE); // Default location (CR. 9TH AVE, RGM STREET)
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        // Load the custom marker icon from resources
        Bitmap originalMarkerBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img1);

        // Define the desired dimensions for the resized marker icon
        int width = (int) getResources().getDimension(R.dimen.custom_marker_width); // Use your desired width
        int height = (int) getResources().getDimension(R.dimen.custom_marker_height); // Use your desired height

        // Resize the original marker bitmap
        Bitmap resizedMarkerBitmap = Bitmap.createScaledBitmap(originalMarkerBitmap, width, height, false);

        // Create a BitmapDescriptor from the resized marker bitmap
        BitmapDescriptor customMarkerIcon = BitmapDescriptorFactory.fromBitmap(resizedMarkerBitmap);

        // Add a marker at the specified location with the resized custom marker icon
        pinMarker = mMap.addMarker(new MarkerOptions()
                .position(pinLocation)
                .title(cowName + "'s location")
                .icon(customMarkerIcon));

        // Move the camera to the specified location and zoom in
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pinLocation, 17)); // Change the zoom level as needed
    }

}
