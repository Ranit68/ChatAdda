package com.example.e_commerce.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.e_commerce.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

public class DashBoardActivity extends AppCompatActivity {
    private ImageView profileImage, back;
    private EditText nameInput, aboutInput;
    private Button saveButton;
    private FirebaseUser currentUser;
    private DatabaseReference userRef;
    private StorageReference storageRef;
    private Uri imageUri;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);

        profileImage = findViewById(R.id.profileImage);
        nameInput = findViewById(R.id.nameInput);
        aboutInput = findViewById(R.id.aboutInput);
        saveButton = findViewById(R.id.saveButton);
        back = findViewById(R.id.backD);

        back.setOnClickListener(v -> onBackPressed());

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());
        storageRef = FirebaseStorage.getInstance().getReference("Profile_images");

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Updating Profile...");
        progressDialog.setCancelable(false);

        loadUserProfile();

        profileImage.setOnClickListener(v -> selectImage());
        saveButton.setOnClickListener(v -> saveUserProfile());
    }

    private void loadUserProfile() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("username").getValue(String.class);
                    String about = snapshot.child("about").getValue(String.class);
                    String imgUrl = snapshot.child("imgUrl").getValue(String.class);

                    nameInput.setText(name != null ? name : "");
                    aboutInput.setText(about != null ? about : "");

                    if (imgUrl != null && !imgUrl.isEmpty()) {
                        Glide.with(DashBoardActivity.this)
                                .load(imgUrl)
                                .placeholder(R.drawable.avtar)
                                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                .into(profileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DashBoardActivity.this, "Failed to load profile!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            profileImage.setImageURI(imageUri);
        }
    }

    private void saveUserProfile() {
        String newName = nameInput.getText().toString().trim();
        String newAbout = aboutInput.getText().toString().trim();

        if (TextUtils.isEmpty(newName) || TextUtils.isEmpty(newAbout)) {
            Toast.makeText(this, "Fields cannot be empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();

        if (imageUri != null) {
            uploadImageAndSave(newName, newAbout);
        } else {
            updateUserData(newName, newAbout, null);
        }
    }

    private void uploadImageAndSave(String name, String about) {
        StorageReference fileRef = storageRef.child(currentUser.getUid()).child("profile.jpg");
        fileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot ->
                fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    updateUserData(name, about, uri.toString());
                })
        ).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(DashBoardActivity.this, "Image Upload Failed!", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateUserData(String name, String about, String imgUrl) {
        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put("username", name);
        userMap.put("about", about);
        if (imgUrl != null) {
            userMap.put("imgUrl", imgUrl);
        }

        userRef.updateChildren(userMap).addOnCompleteListener(task -> {
            progressDialog.dismiss();
            if (task.isSuccessful()) {
                Toast.makeText(DashBoardActivity.this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                loadUserProfile();
            } else {
                Toast.makeText(DashBoardActivity.this, "Profile Update Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initiateCall(String receiverId) {
        DatabaseReference callRef = FirebaseDatabase.getInstance().getReference("calls").child(receiverId);
        HashMap<String, String> callData = new HashMap<>();
        callData.put("callerId", currentUser.getUid());
        callData.put("status", "calling");

        callRef.setValue(callData).addOnSuccessListener(aVoid -> {
            Intent intent = new Intent(this, CallActivity.class);
            intent.putExtra("channel", receiverId);
            startActivity(intent);
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to initiate call", Toast.LENGTH_SHORT).show();
        });
    }
}