package com.nhlanhlankosi.tablayoutdemo.activities;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nhlanhlankosi.tablayoutdemo.R;
import com.nhlanhlankosi.tablayoutdemo.infrastructure.SharedPreferencesHelper;
import com.nhlanhlankosi.tablayoutdemo.models.CowLocation;
import com.nhlanhlankosi.tablayoutdemo.models.Notification;
import com.nhlanhlankosi.tablayoutdemo.models.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    public static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 2;
    private static final int REQUEST_LOCATION_PERMISSION_REQUEST_CODE = 1;
    ArrayList<Notification> notificationsList = new ArrayList<>();
    private FusedLocationProviderClient fusedLocationProviderClient;
    private User currentUser;
    private CircleImageView userProfilePicInToolbar;
    private Toolbar toolbar;
    private DatabaseReference userNotificationsRef;
    private ValueEventListener userNotificationsRefListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.dark_grey));

        userProfilePicInToolbar = findViewById(R.id.user_profile_pic_in_toolbar);

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

        currentUser = SharedPreferencesHelper.getUser(this);

        showUserNameAndProfilePicOnToolbar();

        userNotificationsRef = FirebaseDatabase.getInstance().getReference("notifications")
                .child(currentUser.getUserId());

        userProfilePicInToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent profileActivity = new Intent(getApplicationContext(), ProfileActivity.class);
                startActivity(profileActivity);

            }
        });

        handleIntent(getIntent());

        requestLocationPermission();

        requestNotificationsPermission();

        userNotificationsRefListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    notificationsList.clear();

                    for (DataSnapshot notificationSnapShot : snapshot.getChildren()) {
                        Notification notification = notificationSnapShot.getValue(Notification.class);

                        if (notification == null) {
                            return;
                        }

                        notificationsList.add(notification);
                    }

                    showNotification(notificationsList.get(notificationsList.size() - 1));

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        userNotificationsRef.addValueEventListener(userNotificationsRefListener);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent != null && intent.getData() != null) {
            Uri data = intent.getData();
            NavController navController = ((NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment)).getNavController();
            navController.handleDeepLink(intent);
        }
    }

    private void requestNotificationsPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQUEST_CODE);
            } else {
                // Permission is already granted, you can show notifications or do other tasks
            }
        } else {
            // Permission is not required for Android 12 and lower, show the notification
        }

    }

    private void showUserNameAndProfilePicOnToolbar() {

        if (currentUser == null) {
            return;
        }

        toolbar.setTitle("Welcome, " + currentUser.getUserName());
        if (!TextUtils.isEmpty(currentUser.getProfilePicUrl())) {

            Picasso.get()
                    .load(currentUser.getProfilePicUrl())
                    .placeholder(R.drawable.profile_pic_icon)
                    .fit()
                    .centerInside()
                    .into(userProfilePicInToolbar);

        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        getCurrentLocation();

        showUserNameAndProfilePicOnToolbar();

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

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "default_channel_id";
            String channelName = "Default Channel";
            String channelDescription = "This is the default notification channel";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            channel.setDescription(channelDescription);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void showNotification(Notification notification) {
        createNotificationChannel();

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("myapp://notification"));
        intent.setClass(this, SplashScreenActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default_channel_id")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setContentTitle(notification.getTitle())
                .setContentText(notification.getMessage())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(1, builder.build());
    }

    private void getCurrentLocation() {

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(task -> {

            if (task.isSuccessful()) {

                Location currentLocation = task.getResult();

                if (currentLocation != null) {
                    CowLocation cowLocation = new CowLocation(currentLocation.getLatitude(), currentLocation.getLongitude());
                    SharedPreferencesHelper.saveCowLocation(MainActivity.this, cowLocation);
                }

            }

        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can show notifications
                Toast.makeText(this, "Notifications permitted successfully", Toast.LENGTH_SHORT).show();
            } else {
                // Permission denied, you can't show notifications
                Toast.makeText(this, "Please allow notifications to get realtime data", Toast.LENGTH_LONG).show();
            }
        }

        if (requestCode == REQUEST_LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can show notifications
                Toast.makeText(this, "Location services successfully permitted", Toast.LENGTH_SHORT).show();
            } else {
                // Permission denied, you can't show notifications
                Toast.makeText(this, "Please allow notifications to get realtime data", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (userNotificationsRef != null && userNotificationsRefListener != null) {
            userNotificationsRef.removeEventListener(userNotificationsRefListener);
        }
        super.onDestroy();
    }
}
