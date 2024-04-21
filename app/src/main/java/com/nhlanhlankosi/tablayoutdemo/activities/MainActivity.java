package com.nhlanhlankosi.tablayoutdemo.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nhlanhlankosi.tablayoutdemo.R;
import com.nhlanhlankosi.tablayoutdemo.infrastructure.SharedPreferencesHelper;
import com.nhlanhlankosi.tablayoutdemo.models.CowLocation;
import com.nhlanhlankosi.tablayoutdemo.models.User;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        CircleImageView userProfilePicInToolbar = findViewById(R.id.user_profile_pic_in_toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> startActivity(new Intent(MainActivity.this,
                AddCowActivity.class)));

        BottomNavigationView bottomNavView = findViewById(R.id.bottom_navigation);

        BadgeDrawable notificationCountBadge = bottomNavView.getOrCreateBadge(R.id.navigation_notifications);
        notificationCountBadge.setBackgroundColor(getResources().getColor(R.color.purple_200));
        notificationCountBadge.setBadgeGravity(BadgeDrawable.TOP_END);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_my_herd, R.id.navigation_watchboard, R.id.navigation_my_farm, R.id.navigation_notifications)
                .build();

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
            NavigationUI.setupWithNavController(bottomNavView, navController);
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        User currentUser = SharedPreferencesHelper.getUser(this);

        toolbar.setTitle("Welcome, " + currentUser.getUserName());

        if (!TextUtils.isEmpty(currentUser.getProfilePicUrl())) {

            Picasso.get()
                    .load(currentUser.getProfilePicUrl())
                    .placeholder(R.drawable.profile_pic_icon)
                    .fit()
                    .centerInside()
                    .into(userProfilePicInToolbar);

        }

        userProfilePicInToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent profileActivity = new Intent(getApplicationContext(), ProfileActivity.class);
                startActivity(profileActivity);

            }
        });

        requestLocationPermission();

    }

    @Override
    protected void onResume() {
        super.onResume();

        getCurrentLocation();
    }

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            /*
             * At this point permission is granted, therefore we should get the location &
             * store the latitude and longitude in share
             */

            getCurrentLocation();

        } else {

            //At this point, permission is denied, therefore we ask for permission
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION_REQUEST_CODE);

        }
    }

    private void getCurrentLocation() {

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(task -> {

            if (task.isSuccessful()) {

                Location currentLocation = task.getResult();

                CowLocation cowLocation = new CowLocation(currentLocation.getLatitude(), currentLocation.getLongitude());

                SharedPreferencesHelper.saveCowLocation(MainActivity.this, cowLocation);

            }

        });

    }

}
