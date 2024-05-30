package com.nhlanhlankosi.tablayoutdemo.ui.myFarm;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.nhlanhlankosi.tablayoutdemo.listAdapters.CattleAdapter;
import com.nhlanhlankosi.tablayoutdemo.models.Cow;
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

    private DatabaseReference herdsRef;
    private List<Cow> allCattleList = new ArrayList<>();

    private ValueEventListener userGeoFenceCoordinatesRefListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        User currentUser = SharedPreferencesHelper.getUser(requireContext());
        geoFenceCoordinatesRef = FirebaseDatabase.getInstance().getReference("geo_fence_coordinates");

        userGeoFenceCoordinatesRef = geoFenceCoordinatesRef.child(currentUser.getUserId());

        // Fetch all cattle data
        herdsRef = FirebaseDatabase.getInstance().getReference("herds").child(currentUser.getUserId());
        herdsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot cattleSnapshot : snapshot.getChildren()) {
                    Cow cow = cattleSnapshot.getValue(Cow.class);
                    if (cow != null) {
                        allCattleList.add(cow);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle potential errors here
            }
        });

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
                            .fillColor(color)
                            .clickable(true)); // Make the polygon clickable
                    // Add a marker with the geofence name
                    LatLng center = getPolygonCenter(latLngs);
                    mMap.addMarker(new MarkerOptions()
                            .position(center)
                            .title(name)
                            .snippet("")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                    polygon.setTag(geofence);
                    polygons.add(polygon);
                }

                placeCowMarkers();  // Place cow markers after fetching data
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
                            .fillColor(color)
                            .clickable(true)); // Make the polygon clickable
                    polygons.add(polygon);
                    polygonPoints.clear();

                    if (firstlatLng == null) {
                        return;
                    }

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

                    Geofence geofence = new Geofence("", geofenceName, coordinates, color);
                    DatabaseReference newGeoFenceCoordinatesRef = userGeoFenceCoordinatesRef.push();
                    String geoFenceId = newGeoFenceCoordinatesRef.getKey();
                    geofence.setId(geoFenceId);
                    newGeoFenceCoordinatesRef.setValue(geofence);
                }
            }
        });

        mMap.setOnPolygonClickListener(new GoogleMap.OnPolygonClickListener() {
            @Override
            public void onPolygonClick(@NonNull Polygon polygon) {
                Geofence geofence = (Geofence) polygon.getTag();
                if (geofence != null) {
                    showGeofenceOptionsDialog(geofence);
                }
            }
        });

    }

    private void placeCowMarkers() {
        for (Cow cow : allCattleList) {
            LatLng cowLatLng = new LatLng(cow.getLatitude(), cow.getLongitude());
            mMap.addMarker(new MarkerOptions()
                    .position(cowLatLng)
                    .title(cow.getName())
                    .icon(BitmapDescriptorFactory.fromBitmap(resizeBitmap(R.drawable.img1, 100, 100))));
        }
    }

    private Bitmap resizeBitmap(int drawableRes, int width, int height) {
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), drawableRes);
        return Bitmap.createScaledBitmap(imageBitmap, width, height, false);
    }

    private void showGeofenceOptionsDialog(Geofence geofence) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Geofence Options");
        builder.setItems(new CharSequence[]{"Name Geofence", "Add Cow"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        showNameGeofenceDialog(geofence);
                        break;
                    case 1:
                        addCowToGeofence(geofence);
                        break;
                }
            }
        });
        builder.show();
    }

    private void showNameGeofenceDialog(Geofence geofence) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Name Geofence");

        final EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = input.getText().toString();
                geofence.setName(name);
                userGeoFenceCoordinatesRef.child(geofence.getId()).setValue(geofence);
                Toast.makeText(getActivity(), "Geofence named: " + name, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void addCowToGeofence(Geofence geofence) {
        if (allCattleList.isEmpty()) {
            Toast.makeText(getActivity(), "No cattle available", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a dialog to show the cattle list
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select a Cow");

        // Create a RecyclerView to display the cattle
        RecyclerView recyclerView = new RecyclerView(getActivity());
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        CattleAdapter adapter = new CattleAdapter(allCattleList, new CattleAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Cow cow) {
                // When a cow is selected, update its location
                List<LatLng> geofencePoints = getLatLngList(geofence.getCoordinates());
                LatLng randomPoint = getRandomPointInPolygon(geofencePoints);
                cow.setLatitude(randomPoint.latitude);
                cow.setLongitude(randomPoint.longitude);
                herdsRef.child(cow.getId()).setValue(cow).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getActivity(), "Cow added to geofence: " + cow.getName(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), "Failed to add cow to geofence: " + cow.getName(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        recyclerView.setAdapter(adapter);

        builder.setView(recyclerView);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    // Helper method to get a random point inside a polygon
    private LatLng getRandomPointInPolygon(List<LatLng> polygon) {
        Random random = new Random();
        double minLat = Double.MAX_VALUE;
        double maxLat = Double.MIN_VALUE;
        double minLng = Double.MAX_VALUE;
        double maxLng = Double.MIN_VALUE;

        for (LatLng point : polygon) {
            minLat = Math.min(minLat, point.latitude);
            maxLat = Math.max(maxLat, point.latitude);
            minLng = Math.min(minLng, point.longitude);
            maxLng = Math.max(maxLng, point.longitude);
        }

        LatLng randomPoint;
        do {
            double randomLat = minLat + (maxLat - minLat) * random.nextDouble();
            double randomLng = minLng + (maxLng - minLng) * random.nextDouble();
            randomPoint = new LatLng(randomLat, randomLng);
        } while (!isPointInPolygon(randomPoint, polygon));

        return randomPoint;
    }

    // Helper method to check if a point is inside a polygon
    private boolean isPointInPolygon(LatLng point, List<LatLng> polygon) {
        boolean result = false;
        int j = polygon.size() - 1;
        for (int i = 0; i < polygon.size(); i++) {
            if (polygon.get(i).longitude < point.longitude && polygon.get(j).longitude >= point.longitude
                    || polygon.get(j).longitude < point.longitude && polygon.get(i).longitude >= point.longitude) {
                if (polygon.get(i).latitude + (point.longitude - polygon.get(i).longitude) / (polygon.get(j).longitude - polygon.get(i).longitude) * (polygon.get(j).latitude - polygon.get(i).latitude) < point.latitude) {
                    result = !result;
                }
            }
            j = i;
        }
        return result;
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