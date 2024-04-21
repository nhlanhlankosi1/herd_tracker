package com.nhlanhlankosi.tablayoutdemo.activities;

import static com.nhlanhlankosi.tablayoutdemo.activities.RegisterActivity.LOADING_DIALOG_TIME_OUT;
import static com.nhlanhlankosi.tablayoutdemo.infrastructure.Common.closeKeyboard;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.installations.FirebaseInstallations;
import com.nhlanhlankosi.tablayoutdemo.R;
import com.nhlanhlankosi.tablayoutdemo.infrastructure.SharedPreferencesHelper;
import com.nhlanhlankosi.tablayoutdemo.models.User;

import java.util.Objects;

import dmax.dialog.SpotsDialog;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();
    private final Handler handlerDialog = new Handler();
    TextInputEditText etLoginEmail;
    TextInputEditText etLoginPassword;
    TextView tvRegisterHere;
    Button btnLogin;
    FirebaseAuth mAuth;
    private Runnable runnableDialog;
    private FirebaseAuth.AuthStateListener authStateListener;
    private String fcmToken;

    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dialog = new SpotsDialog.Builder().setContext(LoginActivity.this)
                .setCancelable(false).build();

        authStateListener = firebaseAuth -> {

            FirebaseUser user = firebaseAuth.getCurrentUser();

            if (user != null) {
                DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
                String userId = user.getUid();
                usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        dismissDialog();
                        if (snapshot.exists()) {
                            //Set current user
                            Toast.makeText(LoginActivity.this, "User logged in successfully", Toast.LENGTH_SHORT).show();

                            User user = snapshot.getValue(User.class);
                            SharedPreferencesHelper.saveUser(LoginActivity.this, user);

                            Intent openMainActivity = new Intent(LoginActivity.this, MainActivity.class);
                            openMainActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(openMainActivity);
                            finish();

                        } else {
                            Toast.makeText(LoginActivity.this, "User does not exist. Register instead!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            } else {
                dismissDialog();
            }

        };

        etLoginEmail = findViewById(R.id.etLoginEmail);
        etLoginPassword = findViewById(R.id.etLoginPass);
        tvRegisterHere = findViewById(R.id.tvRegisterHere);
        btnLogin = findViewById(R.id.btnLogin);

        mAuth = FirebaseAuth.getInstance();

        btnLogin.setOnClickListener(view -> {
            closeKeyboard(LoginActivity.this);
            showLoadingDialogue();
            loginUser();
        });

        tvRegisterHere.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            finish();
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        closeKeyboard(LoginActivity.this);

        if (handlerDialog != null) {
            handlerDialog.removeCallbacks(runnableDialog);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mAuth != null && authStateListener != null) {
            mAuth.addAuthStateListener(authStateListener);
        }

        initialiseFCMToken();

    }

    private void showLoadingDialogue() {

        if (dialog != null) {
            dialog.show();
        }

        runnableDialog = () -> {

            if (dialog.isShowing()) {

                dialog.dismiss();
                closeKeyboard(LoginActivity.this);

                Toast.makeText(this, "Please check your internet and try again", Toast.LENGTH_SHORT).show();

            }
        };

        handlerDialog.postDelayed(runnableDialog, LOADING_DIALOG_TIME_OUT);

    }

    private void dismissDialog() {

        if (dialog != null) {
            dialog.dismiss();
        }

        if (handlerDialog != null) {
            handlerDialog.removeCallbacks(runnableDialog);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (handlerDialog != null) {
            handlerDialog.removeCallbacks(runnableDialog);
        }

    }

    private void initialiseFCMToken() {

        FirebaseInstallations.getInstance().getToken(true)
                .addOnCompleteListener(task -> {

                    if (!task.isSuccessful()) {
                        Log.w(TAG, "getInstanceId failed", task.getException());
                        return;
                    }

                    // Get new Instance ID token
                    fcmToken = Objects.requireNonNull(task.getResult()).getToken();

                    String msg = getString(R.string.msg_token_fmt, fcmToken);
                    Log.d(TAG, msg);

                });
    }

    @Override
    protected void onStop() {

        if (mAuth != null && authStateListener != null) {
            mAuth.removeAuthStateListener(authStateListener);
        }

        super.onStop();

    }

    private void loginUser() {
        String email = etLoginEmail.getText().toString();
        String password = etLoginPassword.getText().toString();

        if (TextUtils.isEmpty(email)) {
            etLoginEmail.setError("Email cannot be empty");
            etLoginEmail.requestFocus();
            dismissDialog();
        } else if (TextUtils.isEmpty(password)) {
            etLoginPassword.setError("Password cannot be empty");
            etLoginPassword.requestFocus();
            dismissDialog();
        } else {
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (!task.isSuccessful()) {
                        dismissDialog();
                        Toast.makeText(LoginActivity.this, "Log in Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

}