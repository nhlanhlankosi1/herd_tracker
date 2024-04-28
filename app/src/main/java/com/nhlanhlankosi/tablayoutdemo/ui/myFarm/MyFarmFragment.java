package com.nhlanhlankosi.tablayoutdemo.ui.myFarm;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nhlanhlankosi.tablayoutdemo.R;
import com.nhlanhlankosi.tablayoutdemo.infrastructure.SharedPreferencesHelper;
import com.nhlanhlankosi.tablayoutdemo.models.Geofence;
import com.nhlanhlankosi.tablayoutdemo.models.LatiLongi;
import com.nhlanhlankosi.tablayoutdemo.models.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MyFarmFragment extends Fragment implements OnMapReadyCallback {
    private GoogleMap mMap;
    private List<Polygon> polygons = new ArrayList<>();
    private List<LatLng> polygonPoints = new ArrayList<>();
    private LatLng firstlatLng;
    private DatabaseReference geoFenceCoordinatesRef;
    private DatabaseReference userGeoFenceCoordinatesRef;

    private ValueEventListener userGeoFenceCoordinatesRefListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        User currentUser = SharedPreferencesHelper.getUser(requireContext());
        geoFenceCoordinatesRef = FirebaseDatabase.getInstance().getReference("geo_fence_coordinates");

        userGeoFenceCoordinatesRef = geoFenceCoordinatesRef.child(currentUser.getUserId());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_farm, container, false);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-20.1563161, 28.5820653), 12));

        userGeoFenceCoordinatesRefListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    Geofence geofence = childSnapshot.getValue(Geofence.class);
                    if (geofence == null || mMap == null) {
                        return;
                    }
                    List<LatiLongi> coordinates = geofence.getCoordinates();
                    int color = geofence.getColor();
                    String name = geofence.getName();
                    List<LatLng> latLngs = getLatLngList(coordinates);
                    // Add the geofence to the map
                    Polygon polygon = mMap.addPolygon(new PolygonOptions()
                            .add(latLngs.toArray(new LatLng[0]))
                            .strokeWidth(2)
                            .strokeColor(color)
                            .fillColor(color));
                    // Add a marker with the geofence name
                    LatLng center = getPolygonCenter(latLngs);
                    mMap.addMarker(new MarkerOptions()
                            .position(center)
                            .title(name)
                            .snippet("")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };

        userGeoFenceCoordinatesRef.addValueEventListener(userGeoFenceCoordinatesRefListener);

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                new AlertDialog.Builder(getActivity())
                        .setMessage("Do you want to add a geo-fence at this location? Tap three more points on the map to create the polygon.")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                polygonPoints.add(latLng);
                                firstlatLng = latLng;
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                polygonPoints.add(latLng);
                mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                        .alpha(0.8f) // Makes the marker slightly transparent
                        .flat(true) // Allows the marker to be dragged
                        .visible(true) // Makes the marker visible
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                if (polygonPoints.size() == 4) {
                    Random random = new Random();
                    int color = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
                    Polygon polygon = mMap.addPolygon(new PolygonOptions()
                            .add(polygonPoints.get(0), polygonPoints.get(1), polygonPoints.get(2), polygonPoints.get(3))
                            .strokeWidth(2)
                            .strokeColor(color)
                            .fillColor(color));
                    polygons.add(polygon);
                    polygonPoints.clear();

                    //Add the first point the user choose
                    mMap.addMarker(new MarkerOptions()
                            .position(firstlatLng)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                            .alpha(0.8f) // Makes the marker slightly transparent
                            .flat(true) // Allows the marker to be dragged
                            .visible(true) // Makes the marker visible
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

                    // Add a name to the geofence
                    String geofenceName = "Farm " + (polygons.size() - 1);
                    polygon.setTag(geofenceName);
                    // Add a marker with the geofence name
                    LatLng center = getPolygonCenter(polygon.getPoints());
                    mMap.addMarker(new MarkerOptions()
                            .position(center)
                            .title(geofenceName)
                            .alpha(0.8f)
                            .snippet("")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                    List<LatiLongi> coordinates = getCoordinates(polygon.getPoints());

                    Geofence geofence = new Geofence(geofenceName, coordinates, color);
                    DatabaseReference newGeoFenceCoordinatesRef = userGeoFenceCoordinatesRef.push();
                    newGeoFenceCoordinatesRef.setValue(geofence);

                }
            }
        });

    }

    @Override
    public void onDetach() {
        super.onDetach();

        if (userGeoFenceCoordinatesRef != null && userGeoFenceCoordinatesRefListener != null) {
            userGeoFenceCoordinatesRef.removeEventListener(userGeoFenceCoordinatesRefListener);
        }

    }

    public List<LatiLongi> getCoordinates(List<LatLng> latLngList) {
        List<LatiLongi> latiLongiList = new ArrayList<>();
        for (LatLng latLng : latLngList) {
            LatiLongi latiLongi = new LatiLongi(latLng.latitude, latLng.longitude);
            latiLongiList.add(latiLongi);
        }
        return latiLongiList;
    }

    public List<LatLng> getLatLngList(List<LatiLongi> latiLongiList) {
        List<LatLng> latLngList = new ArrayList<>();
        for (LatiLongi latiLongi : latiLongiList) {
            LatLng latLng = new LatLng(latiLongi.getLatitude(), latiLongi.getLongitude());
            latLngList.add(latLng);
        }
        return latLngList;
    }

    // Helper method to get the center of a polygon
    private LatLng getPolygonCenter(List<LatLng> points) {
        double latitude = 0;
        double longitude = 0;
        int n = points.size();
        for (LatLng point : points) {
            latitude += point.latitude;
            longitude += point.longitude;
        }
        return new LatLng(latitude / n, longitude / n);
    }

}