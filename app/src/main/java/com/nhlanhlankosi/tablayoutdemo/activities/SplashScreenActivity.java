package com.nhlanhlankosi.tablayoutdemo.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nhlanhlankosi.tablayoutdemo.R;
import com.nhlanhlankosi.tablayoutdemo.infrastructure.SharedPreferencesHelper;
import com.nhlanhlankosi.tablayoutdemo.models.User;

@SuppressLint("CustomSplashScreen")
public class SplashScreenActivity extends AppCompatActivity {
    private final Handler handlerDialog = new Handler();
    FirebaseAuth mAuth;
    private Runnable runnableDialog;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        // If the user has not been onboarded, onboard the user

        if (!restorePrefData()) {

            Intent onboardingActivity = new Intent(getApplicationContext(), OnboardingActivity.class);
            startActivity(onboardingActivity);
            finish();
        }

        mAuth = FirebaseAuth.getInstance();

        progressBar = findViewById(R.id.progress_bar);

        showLoadingDialogue();

    }

    private boolean restorePrefData() {

        SharedPreferences pref = getApplicationContext().getSharedPreferences("myPrefs", MODE_PRIVATE);
        return pref.getBoolean("isIntroOpened", false);

    }

    @Override
    protected void onStart() {
        super.onStart();

        checkUserSession();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (handlerDialog != null) {
            handlerDialog.removeCallbacks(runnableDialog);
        }
    }

    private void showLoadingDialogue() {

        progressBar.setVisibility(View.VISIBLE);

        runnableDialog = () -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Please check your internet and try again", Toast.LENGTH_SHORT).show();
        };

        handlerDialog.postDelayed(runnableDialog, 10000);

    }

    private void dismissDialog() {

        progressBar.setVisibility(View.GONE);

        if (handlerDialog != null) {
            handlerDialog.removeCallbacks(runnableDialog);
        }

    }

    private void checkUserSession() {

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
            String userId = user.getUid();
            usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    dismissDialog();
                    if (snapshot.exists()) {
                        //Save the user object into shared preferences
                        Toast.makeText(SplashScreenActivity.this, "User logged in successfully", Toast.LENGTH_SHORT).show();
                        User user = snapshot.getValue(User.class);
                        SharedPreferencesHelper.saveUser(SplashScreenActivity.this, user);

                        Intent openMainActivity = new Intent(SplashScreenActivity.this, MainActivity.class);
                        if (getIntent() != null && getIntent().getData() != null) {
                            openMainActivity.setData(getIntent().getData()); // Pass the deep link data to MainActivity
                        }
                        openMainActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(openMainActivity);
                        finish();

                    } else {
                        Toast.makeText(SplashScreenActivity.this, "User does not exist. Register instead!", Toast.LENGTH_SHORT).show();
                        Intent registerActivity = new Intent(getApplicationContext(), RegisterActivity.class);
                        startActivity(registerActivity);
                        finish();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    dismissDialog();
                    Intent registerActivity = new Intent(getApplicationContext(), RegisterActivity.class);
                    startActivity(registerActivity);
                    finish();

                }
            });

        } else {
            dismissDialog();
            Intent registerActivity = new Intent(getApplicationContext(), RegisterActivity.class);
            startActivity(registerActivity);
            finish();
        }

    }

}
