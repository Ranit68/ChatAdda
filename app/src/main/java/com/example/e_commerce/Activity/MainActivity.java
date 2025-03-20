package com.example.e_commerce.Activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.e_commerce.R;
import com.example.e_commerce.Model.User;
import com.example.e_commerce.Adapter.UserAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private List<User> userList, filteredList;
    private UserAdapter userAdapter;
    private DatabaseReference usersRef, chatRef;
    private FirebaseUser currentUser;
    private TextView userNameText;
    private SearchView searchView;
    private ImageView profilePic;
    private LinearLayout mainLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        ImageView callHistory = findViewById(R.id.call_history);
        callHistory.setOnClickListener(v -> startActivity(new Intent(MainActivity.this,CallHistoryActivity.class)));

        mainLayout = findViewById(R.id.userList);
        searchView = findViewById(R.id.search);


        applyTheme();


//        boolean isDarkMode = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
//                == Configuration.UI_MODE_NIGHT_YES;
//
//
//
//        if (isDarkMode) {
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
//            mainLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.user_back));
//            searchView.setBackground(ContextCompat.getDrawable(this, R.drawable.search_gradient_dark));
//        } else {
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
//            mainLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.user_listing_gradient_light));
//            searchView.setBackground(ContextCompat.getDrawable(this, R.drawable.search_gradient));
//
//        }



        // Initialize Views
        recyclerView = findViewById(R.id.recyclerView1);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userNameText = findViewById(R.id.name);

        profilePic = findViewById(R.id.profilePic);

        // Open Dashboard on Profile Click
        profilePic.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, DashBoardActivity.class)));

        // Initialize Lists & Adapter
        userList = new ArrayList<>();
        filteredList = new ArrayList<>();
        userAdapter = new UserAdapter(this, filteredList);
        recyclerView.setAdapter(userAdapter);

        // Firebase References
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        usersRef = FirebaseDatabase.getInstance().getReference("Users");
        chatRef = FirebaseDatabase.getInstance().getReference("UserChats").child(currentUser.getUid());

        // Load User Data
        loadCurrentUserProfile();
        loadUsers();
        setupSearchView();
        listenForIncomingCalls();
    }

    private void applyTheme() {
        boolean isDarkMode = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;

        if (isDarkMode) {
            mainLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.user_back));
            searchView.setBackground(ContextCompat.getDrawable(this, R.drawable.search_gradient_dark));
        } else {
            mainLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.user_listing_gradient_light));
            searchView.setBackground(ContextCompat.getDrawable(this, R.drawable.search_gradient));
        }
    }

    // **Detect Theme Change Instantly**
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Check if dark mode changed
        boolean isDarkMode = (newConfig.uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;

        // Apply the theme change
        applyTheme();

        // Restart activity to apply the changes properly
        recreate();
    }

    private void loadUsers() {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                filteredList.clear();

                for (DataSnapshot data : snapshot.getChildren()) {
                    User user = data.getValue(User.class);
                    if (user != null && !user.getUserId().equals(currentUser.getUid())) {
                        user.setLastMessage(user.getAbout() != null ? user.getAbout() : "No about info"); // Set About in place of lastMessage
                        userList.add(user);
                    }
                }

                // Update filteredList and notify adapter
                filteredList.addAll(userList);
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Failed to load users", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchLastMessageTimestamps(List<User> tempUserList) {
        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (User user : tempUserList) {
                    String userId = user.getUserId();
                    DataSnapshot chatSnap = snapshot.child(userId);

                    if (chatSnap.exists()) {
                        user.setLastMessage(chatSnap.child("lastMessage").getValue(String.class));
                        Long timestamp = chatSnap.child("timestamp").getValue(Long.class);
                        user.setLastMessageTime(timestamp != null ? timestamp : 0);
                    } else {
                        user.setLastMessage("No messages yet");
                        user.setLastMessageTime(0);
                    }
                }

                // Sort users by latest message timestamp
                Collections.sort(tempUserList, (u1, u2) -> Long.compare(u2.getLastMessageTime(), u1.getLastMessageTime()));

                // Update UI
                userList.clear();
                userList.addAll(tempUserList);
                filteredList.clear();
                filteredList.addAll(userList);
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterUsers(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterUsers(newText);
                return true;
            }
        });
    }

    private void filterUsers(String query) {
        filteredList.clear();
        String lowerCaseQuery = query.toLowerCase();
        for (User user : userList) {
            if (user.getUsername().toLowerCase().contains(lowerCaseQuery)) {
                filteredList.add(user);
            }
        }
        userAdapter.notifyDataSetChanged();
    }

    private void loadCurrentUserProfile() {
        if (currentUser != null) {
            usersRef.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            userNameText.setText("Hello " + user.getUsername());
                            if (user.getImgUrl() != null && !user.getImgUrl().isEmpty()) {
                                Glide.with(MainActivity.this)
                                        .load(user.getImgUrl())
                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                                        .into(profilePic);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    private void listenForIncomingCalls() {
        DatabaseReference callRef = FirebaseDatabase.getInstance().getReference("calls").child(currentUser.getUid());

        callRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String callerId = snapshot.child("callerId").getValue(String.class);
                    if (callerId != null) {
                        showIncomingCallDialog(callerId);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    private void showIncomingCallDialog(String callerId) {
        new AlertDialog.Builder(this)
                .setTitle("Incoming Call")
                .setMessage("User " + callerId + " is calling you")
                .setPositiveButton("Accept", (dialog, which) -> acceptCall(callerId))
                .setNegativeButton("Reject", (dialog, which) -> rejectCall(callerId))
                .show();
    }

    private void acceptCall(String callerId) {
        FirebaseDatabase.getInstance().getReference("calls").child(currentUser.getUid()).removeValue();
        Intent intent = new Intent(this, CallActivity.class);
        intent.putExtra("channel", callerId);
        startActivity(intent);
    }

    private void rejectCall(String callerId) {
        FirebaseDatabase.getInstance().getReference("calls").child(currentUser.getUid()).removeValue();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCurrentUserProfile();
    }


}
