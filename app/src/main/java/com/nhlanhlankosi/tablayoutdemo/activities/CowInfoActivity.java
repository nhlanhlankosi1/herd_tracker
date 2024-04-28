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
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
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
import com.nhlanhlankosi.tablayoutdemo.models.Cow;
import com.nhlanhlankosi.tablayoutdemo.models.User;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;

public class CowInfoActivity extends AppCompatActivity {
    public static final String COW_NAME = "com.nhlanhlankosi.tablayoutdemo.activities.CowInfoActivity.COW_NAME";
    public static final String COW_ID = "com.nhlanhlankosi.tablayoutdemo.activities.CowInfoActivity.COW_ID";
    public static final String COW_PIC_URL = "com.nhlanhlankosi.tablayoutdemo.activities.CowInfoActivity.COW_PIC_URL";
    public static final String COLLAR_ID = "com.nhlanhlankosi.tablayoutdemo.activities.CowInfoActivity.COLLAR_ID";
    public static final String GENDER = "com.nhlanhlankosi.tablayoutdemo.activities.CowInfoActivity.GENDER";
    public static final String BREED = "com.nhlanhlankosi.tablayoutdemo.activities.CowInfoActivity.BREED";
    public static final String HEART_RATE = "com.nhlanhlankosi.tablayoutdemo.activities.CowInfoActivity.HEART_RATE";
    public static final String TEMPERATURE = "com.nhlanhlankosi.tablayoutdemo.activities.CowInfoActivity.TEMPERATURE";
    public static final String LONGITUDE = "com.nhlanhlankosi.tablayoutdemo.activities.CowInfoActivity.LONGITUDE";
    public static final String LATITUDE = "com.nhlanhlankosi.tablayoutdemo.activities.CowInfoActivity.LATITUDE";
    private static final int REQUEST_CODE = 1;
    private final Handler handlerDialog = new Handler();
    private CircleImageView cowPicture;
    private TextInputEditText cowNameTxtEdit;
    private TextInputEditText cowCollarId;
    private Runnable runnableDialog;
    private AlertDialog dialog;

    private Uri pickedImgUri;
    private String cowName = "";
    private String cowId = "";
    private String cowPicUrl = "";
    private String collarId = "";
    private String gender = "";
    private String breed = "";
    private Long heartRate = 0L;
    private Double temperature = 0.0;
    private Double longitude = 0.0;
    private Double latitude = 0.0;
    private Button cowInfoShowEditCowInfoFieldsBtn;
    private Button cowInfoHideEditCowInfoFieldsBtn;
    private LinearLayout editCowInfoFieldsContainer;

    private DatabaseReference cowRef;
    private ValueEventListener cowRefListener;
    private User savedUser;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cow_info);

        savedUser = SharedPreferencesHelper.getUser(this);

        if (getIntent().getExtras() != null) {

            Bundle cowBundle = getIntent().getExtras();
            cowName = cowBundle.getString(COW_NAME);
            cowId = cowBundle.getString(COW_ID);
            cowPicUrl = cowBundle.getString(COW_PIC_URL);
            collarId = cowBundle.getString(COLLAR_ID);
            gender = cowBundle.getString(GENDER);
            breed = cowBundle.getString(BREED);
            heartRate = cowBundle.getLong(HEART_RATE);
            temperature = cowBundle.getDouble(TEMPERATURE);
            longitude = cowBundle.getDouble(LONGITUDE);
            latitude = cowBundle.getDouble(LATITUDE);

        }

        cowRef = FirebaseDatabase.getInstance().getReference("herds").child(savedUser.getUserId())
                .child(cowId);

        cowPicture = findViewById(R.id.cow_picture);
        TextView cowInfoNameTxt = findViewById(R.id.cow_info_name_txt);
        TextView cowInfoIdTxt = findViewById(R.id.cow_info_id_txt);
        TextView cowInfoCollarIdTxt = findViewById(R.id.cow_info_collar_id_txt);
        TextView cowInfoGenderTxt = findViewById(R.id.cow_info_gender_txt);
        TextView cowInfoBreedTxt = findViewById(R.id.cow_info_breed_txt);
        TextView cowInfoTempTxt = findViewById(R.id.cow_info_temp_txt);
        TextView cowInfoHeartRateTxt = findViewById(R.id.cow_info_heart_rate_txt);

        Button cowShowOnMapBtn = findViewById(R.id.cow_show_on_map_btn);

        cowInfoShowEditCowInfoFieldsBtn = findViewById(R.id.cow_info_show_edit_cow_info_fields_btn);
        cowInfoHideEditCowInfoFieldsBtn = findViewById(R.id.cow_info_hide_edit_cow_info_fields_btn);

        editCowInfoFieldsContainer = findViewById(R.id.edit_cow_info_fields_container);
        cowNameTxtEdit = findViewById(R.id.name);
        cowCollarId = findViewById(R.id.collar_id);

        Button updateCowInfoBtn = findViewById(R.id.update_cow_info_btn);

        dialog = new SpotsDialog.Builder().setContext(CowInfoActivity.this)
                .setCancelable(false).build();

        if (!TextUtils.isEmpty(cowPicUrl)) {

            Picasso.get()
                    .load(cowPicUrl)
                    .placeholder(R.drawable.cow_pic_place_holder)
                    .fit()
                    .centerInside()
                    .into(cowPicture);

        }

        cowInfoNameTxt.setText(cowName);
        cowInfoIdTxt.setText(cowId);
        cowInfoCollarIdTxt.setText(collarId);
        cowInfoGenderTxt.setText(gender);
        cowInfoBreedTxt.setText(breed);

        cowInfoHeartRateTxt.setText(String.format(Locale.ENGLISH, "%d bpm", heartRate));
        cowInfoTempTxt.setText(String.format(Locale.ENGLISH, "%s °C", temperature));

        cowNameTxtEdit.setText(cowName);
        cowCollarId.setText(collarId);
        cowShowOnMapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(CowInfoActivity.this, CowLocationActivity.class);
                intent.putExtra(COW_NAME, cowName);
                intent.putExtra(LATITUDE, latitude);
                intent.putExtra(LONGITUDE, longitude);
                startActivity(intent);
            }
        });
        cowInfoShowEditCowInfoFieldsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editCowInfoFieldsContainer.setVisibility(View.VISIBLE);
                cowInfoShowEditCowInfoFieldsBtn.setVisibility(View.GONE);
                cowInfoHideEditCowInfoFieldsBtn.setVisibility(View.VISIBLE);
            }
        });

        cowInfoHideEditCowInfoFieldsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editCowInfoFieldsContainer.setVisibility(View.GONE);
                cowInfoHideEditCowInfoFieldsBtn.setVisibility(View.GONE);
                cowInfoShowEditCowInfoFieldsBtn.setVisibility(View.VISIBLE);
            }
        });

        updateCowInfoBtn.setOnClickListener(view -> {
            closeKeyboard(CowInfoActivity.this);
            showLoadingDialogue();
            updateCowInfo();
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

        //This listener is set up to update name of cow and collarId
        cowRefListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    Cow cow = snapshot.getValue(Cow.class);
                    if (cow != null) {
                        cowInfoNameTxt.setText(cow.getName());
                        cowInfoCollarIdTxt.setText(cow.getCollarId());
                        cowNameTxtEdit.setText(cow.getName());
                        cowCollarId.setText(cow.getCollarId());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        cowRef.addValueEventListener(cowRefListener);

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

        closeKeyboard(CowInfoActivity.this);

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

        if (cowRef != null && cowRefListener != null) {
            cowRef.removeEventListener(cowRefListener);
        }

    }

    private void showLoadingDialogue() {

        if (dialog != null) {
            dialog.show();
        }

        runnableDialog = () -> {

            if (dialog.isShowing()) {

                dialog.dismiss();
                closeKeyboard(CowInfoActivity.this);

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

    private void updateCowInfo() {

        String name = Objects.requireNonNull(cowNameTxtEdit.getText()).toString();
        String collarId = Objects.requireNonNull(cowCollarId.getText()).toString();

        if (TextUtils.isEmpty(name)) {
            dismissDialog();
            cowNameTxtEdit.setError("Please specify the name of your cow");
            cowNameTxtEdit.requestFocus();
        } else if (TextUtils.isEmpty(collarId)) {
            dismissDialog();
            cowCollarId.setError("Collar Id cannot be empty");
            cowCollarId.requestFocus();
        } else {

            if (savedUser != null) {

                HashMap<String, Object> cowInfoMap = new HashMap<>();
                cowInfoMap.put("name", name);
                cowInfoMap.put("collarId", collarId);
                cowRef.updateChildren(cowInfoMap).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {

                        dismissDialog();
                        if (task.isSuccessful()) {
                            Toast.makeText(CowInfoActivity.this, "Cow info updated successfully", Toast.LENGTH_SHORT).show();

                            editCowInfoFieldsContainer.setVisibility(View.GONE);
                            cowInfoHideEditCowInfoFieldsBtn.setVisibility(View.GONE);
                            cowInfoShowEditCowInfoFieldsBtn.setVisibility(View.VISIBLE);

                        } else {
                            Toast.makeText(CowInfoActivity.this, "Cow info update error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }
                });

            } else {
                // No user object found in SharedPreferences
                Toast.makeText(this, "Failed to update info, please try again", Toast.LENGTH_SHORT).show();
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