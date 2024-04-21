package com.nhlanhlankosi.tablayoutdemo.activities;

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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.installations.FirebaseInstallations;
import com.nhlanhlankosi.tablayoutdemo.R;
import com.nhlanhlankosi.tablayoutdemo.infrastructure.SharedPreferencesHelper;
import com.nhlanhlankosi.tablayoutdemo.models.User;

import java.util.Objects;

import dmax.dialog.SpotsDialog;

public class RegisterActivity extends AppCompatActivity {

    public static final long LOADING_DIALOG_TIME_OUT = 30000;
    private static final String TAG = RegisterActivity.class.getSimpleName();
    private final Handler handlerDialog = new Handler();
    TextInputEditText etRegEmail;
    TextInputEditText etRegPassword;
    TextInputEditText etRegUsername;
    TextView tvLoginHere;
    Button btnRegister;
    FirebaseAuth mAuth;
    private Runnable runnableDialog;
    private String fcmToken;
    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etRegEmail = findViewById(R.id.etRegEmail);
        etRegUsername = findViewById(R.id.etRegUsername);
        etRegPassword = findViewById(R.id.etRegPass);
        tvLoginHere = findViewById(R.id.tvLoginHere);
        btnRegister = findViewById(R.id.btnRegister);

        mAuth = FirebaseAuth.getInstance();

        dialog = new SpotsDialog.Builder().setContext(RegisterActivity.this)
                .setCancelable(false).build();

        btnRegister.setOnClickListener(view -> {
            closeKeyboard(RegisterActivity.this);
            showLoadingDialogue();
            createUser();
        });

        tvLoginHere.setOnClickListener(view -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        initialiseFCMToken();
    }

    @Override
    protected void onPause() {
        super.onPause();

        closeKeyboard(RegisterActivity.this);

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

    private void showLoadingDialogue() {

        if (dialog != null) {
            dialog.show();
        }

        runnableDialog = () -> {

            if (dialog.isShowing()) {

                dialog.dismiss();
                closeKeyboard(RegisterActivity.this);

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

    private void createUser() {

        String email = etRegEmail.getText().toString();
        String username = etRegUsername.getText().toString();
        String password = etRegPassword.getText().toString();

        if (TextUtils.isEmpty(email)) {
            dismissDialog();
            etRegEmail.setError("Email cannot be empty");
            etRegEmail.requestFocus();
        } else if (TextUtils.isEmpty(username)) {
            etRegUsername.setError("Username cannot be empty");
            dismissDialog();
            etRegUsername.requestFocus();
        } else if (TextUtils.isEmpty(password)) {
            dismissDialog();
            etRegPassword.setError("Password cannot be empty");
            etRegPassword.requestFocus();
        } else {
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        // Add the user to the Realtime Database
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
                            String userId = firebaseUser.getUid();

                            User user = new User(userId, username, email, "", fcmToken);

                            // Push the user object to the database under the userId node
                            usersRef.child(userId).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    dismissDialog();
                                    if (task.isSuccessful()) {
                                        Toast.makeText(RegisterActivity.this, "User registered successfully", Toast.LENGTH_SHORT).show();

                                        SharedPreferencesHelper.saveUser(RegisterActivity.this, user);

                                        Intent openMainActivity = new Intent(RegisterActivity.this, MainActivity.class);
                                        openMainActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(openMainActivity);
                                        finish();
                                    } else {
                                        Toast.makeText(RegisterActivity.this, "Registration Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            dismissDialog();
                            Toast.makeText(RegisterActivity.this, "Registration Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        dismissDialog();
                        Toast.makeText(RegisterActivity.this, "Registration Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
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
}