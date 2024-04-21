package com.nhlanhlankosi.tablayoutdemo.activities;

import static com.nhlanhlankosi.tablayoutdemo.activities.RegisterActivity.LOADING_DIALOG_TIME_OUT;
import static com.nhlanhlankosi.tablayoutdemo.infrastructure.Common.closeKeyboard;
import static com.nhlanhlankosi.tablayoutdemo.infrastructure.PictureFormatter.rotateImageIfRequired;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nhlanhlankosi.tablayoutdemo.R;
import com.nhlanhlankosi.tablayoutdemo.infrastructure.SharedPreferencesHelper;
import com.nhlanhlankosi.tablayoutdemo.models.User;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;

public class ProfileActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 1;
    private final Handler handlerDialog = new Handler();
    private Runnable runnableDialog;
    private CircleImageView userProfilePic;
    private TextInputEditText userNameTxt;
    private TextInputEditText emailTxt;
    private Uri pickedImgUri;
    private AlertDialog dialog;

    private DatabaseReference profilePicUrlRef;
    private TextView numberOfCattleText;
    private User currentUser;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        dialog = new SpotsDialog.Builder().setContext(ProfileActivity.this)
                .setCancelable(false).build();

        userProfilePic = findViewById(R.id.profile_pic_img_view);
        userNameTxt = findViewById(R.id.name_txt);
        emailTxt = findViewById(R.id.email_txt);
        numberOfCattleText = findViewById(R.id.num_of_cattle_text);
        Button editProfileBtn = findViewById(R.id.edit_profile_btn);
        Button logoutBtn = findViewById(R.id.logout_btn);

        String username1 = userNameTxt.getText().toString();

        String email1 = emailTxt.getText().toString();

        currentUser = SharedPreferencesHelper.getUser(this);

        userNameTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (currentUser != null) {
                    // Use the retrieved user object
                    if (!s.toString().equals(currentUser.getUserName())) {
                        editProfileBtn.setEnabled(true);
                    } else if (s.toString().equals(currentUser.getUserName())
                            && email1.equals(currentUser.getEmail())) {
                        editProfileBtn.setEnabled(false);
                    }

                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        emailTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (currentUser != null) {
                    if (!s.toString().equals(currentUser.getEmail())) {
                        editProfileBtn.setEnabled(true);
                    } else if (s.toString().equals(currentUser.getEmail())
                            && username1.equals(currentUser.getUserName())) {
                        editProfileBtn.setEnabled(false);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        profilePicUrlRef = FirebaseDatabase.getInstance().getReference("users")
                .child(currentUser.getUserId()).child("profilePicUrl");

        if (!TextUtils.isEmpty(currentUser.getProfilePicUrl())) {

            Picasso.get()
                    .load(currentUser.getProfilePicUrl())
                    .placeholder(R.drawable.profile_pic_icon)
                    .fit()
                    .centerInside()
                    .into(userProfilePic);

        }

        userNameTxt.setText(currentUser.getUserName());
        emailTxt.setText(currentUser.getEmail());

        setTheNumberOfCattle();

        userProfilePic.setOnTouchListener((v, event) -> {

            if (Build.VERSION.SDK_INT >= 22) {

                requestGalleryAccessPermission();

            } else {

                openGallery();

            }

            v.performClick();
            return false;

        });

        editProfileBtn.setOnClickListener(view -> {
            closeKeyboard(ProfileActivity.this);
            showLoadingDialogue();
            updateUserInfo();
        });

        logoutBtn.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            SharedPreferencesHelper.deleteUser(this);
            Intent openMainActivity = new Intent(ProfileActivity.this, LoginActivity.class);
            openMainActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(openMainActivity);
            finish();
        });

    }

    private void setTheNumberOfCattle() {

        DatabaseReference userHerd = FirebaseDatabase.getInstance().getReference("herds")
                .child(currentUser.getUserId());

        userHerd.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int numberOfCattle = 0;
                if (snapshot.exists()) {
                    numberOfCattle = (int) snapshot.getChildrenCount();
                }

                numberOfCattleText.setText(numberOfCattle + "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void updateUserInfo() {

        String username = userNameTxt.getText().toString();
        String email = emailTxt.getText().toString();

        if (TextUtils.isEmpty(email)) {
            dismissDialog();
            emailTxt.setError("Email cannot be empty");
            emailTxt.requestFocus();
        } else if (TextUtils.isEmpty(username)) {
            userNameTxt.setError("Username cannot be empty");
            dismissDialog();
            userNameTxt.requestFocus();
        } else {

            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users")
                    .child(currentUser.getUserId());

            User user = currentUser;
            user.setUserName(username);
            user.setEmail(email);

            userRef.setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    dismissDialog();
                    if (task.isSuccessful()) {
                        Toast.makeText(ProfileActivity.this, "User info updated", Toast.LENGTH_SHORT).show();
                        SharedPreferencesHelper.saveUser(ProfileActivity.this, user);
                    } else {
                        Toast.makeText(ProfileActivity.this, "Failed to update info: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }

    private void openGallery() {

        showLoadingDialogue();

        Intent openGallery = new Intent(Intent.ACTION_GET_CONTENT);
        openGallery.setType("image/*");
        startActivityForResult(openGallery, REQUEST_CODE);

    }

    //method for asking for permission to open gallery for prof pic uploads
    private void requestGalleryAccessPermission() {

        if (ContextCompat.checkSelfPermission(ProfileActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(ProfileActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                Toast.makeText(this, "Please Accept To Grant Permission", Toast.LENGTH_LONG).show();

            } else {

                int PReqCode = 1;
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PReqCode);

            }

        } else {

            openGallery();

        }
    }

    private void showLoadingDialogue() {

        if (dialog != null) {
            dialog.show();
        }

        runnableDialog = () -> {

            if (dialog.isShowing()) {

                dialog.dismiss();
                closeKeyboard(ProfileActivity.this);

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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE && data != null) {
            //the user has successfully picked an image
            //we need to save its reference to a Uri value

            try {

                pickedImgUri = data.getData();

                Bitmap finalBmpFile = null;
                try {
                    Bitmap bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), pickedImgUri);
                    finalBmpFile = rotateImageIfRequired(this, bmp, pickedImgUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                //here you can choose quality factor in third parameter(ex. i choose 25)
                assert finalBmpFile != null;
                finalBmpFile.compress(Bitmap.CompressFormat.JPEG, 25, byteArrayOutputStream);
                byte[] compressedImageInBytes = byteArrayOutputStream.toByteArray();

                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("profile_pictures");

                if (pickedImgUri.getLastPathSegment() != null) {

                    StorageReference imageFilePath = storageReference.child(pickedImgUri.getLastPathSegment());

                    UploadTask uploadCompressedProfilePic = imageFilePath.putBytes(compressedImageInBytes);
                    uploadCompressedProfilePic.addOnSuccessListener(taskSnapshot -> {

                        //image upload successful, now we can get our image url
                        imageFilePath.getDownloadUrl().addOnSuccessListener(uri -> {

                            dismissDialog();

                            String newProfilePicUrl = uri.toString();
                            profilePicUrlRef.setValue(newProfilePicUrl);

                            currentUser.setProfilePicUrl(newProfilePicUrl);
                            SharedPreferencesHelper.saveUser(ProfileActivity.this, currentUser);

                            if (!TextUtils.isEmpty(currentUser.getProfilePicUrl())) {

                                Picasso.get()
                                        .load(currentUser.getProfilePicUrl())
                                        .placeholder(R.drawable.profile_pic_icon)
                                        .fit()
                                        .centerInside()
                                        .into(userProfilePic);
                            }

                            Toast.makeText(this, "Profile Picture Updated", Toast.LENGTH_SHORT).show();

                        });

                    }).addOnFailureListener(e -> {

                        dismissDialog();

                        Toast.makeText(this, "Failed To Update Profile Picture\n" + e.getMessage(), Toast.LENGTH_SHORT).show();

                    });

                } else {

                    dismissDialog();

                }

            } catch (Exception e) {

                dismissDialog();

                Toast.makeText(this, "Failed To Update Profile Picture\n" + e.getMessage(), Toast.LENGTH_SHORT).show();

                e.printStackTrace();

            }

        } else {

            dismissDialog();

        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        closeKeyboard(ProfileActivity.this);

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

}
