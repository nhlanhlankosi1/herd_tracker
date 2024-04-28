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
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nhlanhlankosi.tablayoutdemo.R;
import com.nhlanhlankosi.tablayoutdemo.infrastructure.Common;
import com.nhlanhlankosi.tablayoutdemo.infrastructure.SharedPreferencesHelper;
import com.nhlanhlankosi.tablayoutdemo.models.Cow;
import com.nhlanhlankosi.tablayoutdemo.models.CowLocation;
import com.nhlanhlankosi.tablayoutdemo.models.User;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;

public class AddCowActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 1;
    private final Handler handlerDialog = new Handler();

    private CircleImageView cowPicture;
    private TextInputEditText cowName;
    private TextInputEditText cowCollarId;
    private TextInputEditText cowGender;
    private TextInputEditText cowBreed;

    private Runnable runnableDialog;
    private AlertDialog dialog;

    private Uri pickedImgUri;
    private String cowPicUrl = "";

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_cow);

        cowPicture = findViewById(R.id.cow_picture);
        cowName = findViewById(R.id.name);
        cowCollarId = findViewById(R.id.collar_id);
        cowGender = findViewById(R.id.gender);
        cowBreed = findViewById(R.id.breed);

        dialog = new SpotsDialog.Builder().setContext(AddCowActivity.this)
                .setCancelable(false).build();

        Button btnAddCow = findViewById(R.id.add_cow_btn);

        btnAddCow.setOnClickListener(view -> {
            closeKeyboard(AddCowActivity.this);
            showLoadingDialogue();
            addCowToHerd();
        });

        cowPicture.setOnTouchListener((v, event) -> {

            if (Build.VERSION.SDK_INT >= 22) {

                requestGalleryAccessPermission();

            } else {

                openGallery();

            }

            v.performClick();
            return false;

        });

    }

    private void requestGalleryAccessPermission() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
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

    private void openGallery() {

        showLoadingDialogue();

        Intent openGallery = new Intent(Intent.ACTION_GET_CONTENT);
        openGallery.setType("image/*");
        startActivityForResult(openGallery, REQUEST_CODE);

    }

    @Override
    protected void onPause() {
        super.onPause();

        closeKeyboard(AddCowActivity.this);

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
                closeKeyboard(AddCowActivity.this);

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

    private void addCowToHerd() {

        String name = Objects.requireNonNull(cowName.getText()).toString();
        String gender = Objects.requireNonNull(cowGender.getText()).toString();
        String collarId = Objects.requireNonNull(cowCollarId.getText()).toString();
        String breed = Objects.requireNonNull(cowBreed.getText()).toString();

        if (TextUtils.isEmpty(name)) {
            dismissDialog();
            cowName.setError("Please specify the name of your cow");
            cowName.requestFocus();
        } else if (TextUtils.isEmpty(gender)) {
            cowGender.setError("Please specify the gender of your cow e.g. Male");
            dismissDialog();
            cowGender.requestFocus();
        } else if (TextUtils.isEmpty(collarId)) {
            dismissDialog();
            cowCollarId.setError("Collar Id cannot be empty");
            cowCollarId.requestFocus();
        } else {

            DatabaseReference herdsRef = FirebaseDatabase.getInstance().getReference("herds");

            User savedUser = SharedPreferencesHelper.getUser(this);
            CowLocation cowLocation = SharedPreferencesHelper.getCowLocation(this);

            if (savedUser != null) {

                String userId = savedUser.getUserId();
                DatabaseReference cowRef = herdsRef.child(userId).push();
                String cowId = cowRef.getKey();

                double longitude = cowLocation != null ? cowLocation.getLongitude() : 0;
                double latitude = cowLocation != null ? cowLocation.getLatitude() : 0;

                final Cow cow = new Cow(name, cowId, cowPicUrl, collarId, gender, breed, Common.getRandomCowHeartRate(),
                        Common.getRandomCowTemperature(), longitude, latitude, userId);

                // Push the cow object to the database under the cowId node
                cowRef.setValue(cow).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        dismissDialog();
                        if (task.isSuccessful()) {
                            Toast.makeText(AddCowActivity.this, "Cow added to herd successfully", Toast.LENGTH_SHORT).show();
                            AddCowActivity.super.onBackPressed();
                        } else {
                            Toast.makeText(AddCowActivity.this, "Registration Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            } else {
                // No user object found in SharedPreferences
                Toast.makeText(this, "Failed to add " + name + " to your herd retry", Toast.LENGTH_SHORT).show();
            }

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

                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("cow_pictures");

                if (pickedImgUri.getLastPathSegment() != null) {

                    StorageReference imageFilePath = storageReference.child(pickedImgUri.getLastPathSegment());

                    UploadTask uploadCompressedProfilePic = imageFilePath.putBytes(compressedImageInBytes);
                    uploadCompressedProfilePic.addOnSuccessListener(taskSnapshot -> {

                        //image upload successful, now we can get our image url
                        imageFilePath.getDownloadUrl().addOnSuccessListener(uri -> {

                            dismissDialog();

                            cowPicUrl = uri.toString();

                            if (!TextUtils.isEmpty(cowPicUrl)) {

                                Picasso.get()
                                        .load(cowPicUrl)
                                        .placeholder(R.drawable.cow_pic_place_holder)
                                        .fit()
                                        .centerInside()
                                        .into(cowPicture);

                            }

                            Toast.makeText(this, "Cow image uploaded", Toast.LENGTH_SHORT).show();

                        });

                    }).addOnFailureListener(e -> {

                        dismissDialog();

                        Toast.makeText(this, "Failed to upload cow image. Please try again", Toast.LENGTH_SHORT).show();

                    });

                } else {

                    dismissDialog();

                }

            } catch (Exception e) {

                dismissDialog();

                Toast.makeText(this, "Failed to upload cow image. Please try again", Toast.LENGTH_SHORT).show();

                e.printStackTrace();

            }

        } else {

            dismissDialog();

        }
    }

}