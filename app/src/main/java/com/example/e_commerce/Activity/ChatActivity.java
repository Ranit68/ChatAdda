package com.example.e_commerce.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.e_commerce.Adapter.ChatAdapter;
import com.example.e_commerce.Model.Chat;
import com.example.e_commerce.Model.User;
import com.example.e_commerce.R;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.google.firebase.storage.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private EditText messageInput;
    private ImageView sendButton, profilePic, back, imageButton, call;
    private TextView receiverName;
    private RecyclerView recyclerView;
    private List<Chat> chatList;
    private ChatAdapter chatAdapter;
    private DatabaseReference chatRef, userRef, callRef;
    private FirebaseUser currentUser;
    private String receiverId;
    private StorageReference storageRef;
    private Uri imageUri;
    private ConstraintLayout chatLayout;
    private ProgressDialog progressDialog;
    private LinearLayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        FirebaseApp.initializeApp(this);
        initializeViews();
        setupFirebase();
        loadReceiverInfo();
        loadMessages();
        listenForIncomingCalls();


        chatLayout = findViewById(R.id.chatLayout);

        sendButton.setOnClickListener(v -> sendMessage());
        imageButton.setOnClickListener(v -> selectImage());
        back.setOnClickListener(v -> onBackPressed());
        call.setOnClickListener(v -> initiateCall());
        applyTheme();
    }

    private void applyTheme() {
        boolean isDarkMode = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;

        if (isDarkMode) {
            chatLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.user_back));
            int color =  isDarkMode ? ContextCompat.getColor(this, R.color.imageButtonTint) : ContextCompat.getColor(this, R.color.imageButtonTint);
            imageButton.setColorFilter(color, PorterDuff.Mode.SRC_IN);
            back.setColorFilter(color, PorterDuff.Mode.SRC_IN);
            sendButton.setColorFilter(color, PorterDuff.Mode.SRC_IN);
            call.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        } else {
            chatLayout.setBackgroundResource(R.drawable.chat_back_light);
        }
    }
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        boolean isDarkMode = (newConfig.uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        applyTheme();
        recreate();
    }

    private void initializeViews() {
        messageInput = findViewById(R.id.messageInput1);
        sendButton = findViewById(R.id.sendButton1);
        imageButton = findViewById(R.id.attachButton);
        recyclerView = findViewById(R.id.recyclerView2);
        profilePic = findViewById(R.id.prPic);
        receiverName = findViewById(R.id.rName);
        back = findViewById(R.id.backC);
        call = findViewById(R.id.call);

        layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Ensures user starts at bottom
        recyclerView.setLayoutManager(layoutManager);

        chatList = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, chatList, FirebaseAuth.getInstance().getUid());
        recyclerView.setAdapter(chatAdapter);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading...");
        progressDialog.setCancelable(false);
    }
    private void listenForIncomingCalls() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference callRef = FirebaseDatabase.getInstance().getReference("calls").child(currentUserId);

        callRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                String callerId = snapshot.child("callerId").getValue(String.class);
                String status = snapshot.child("status").getValue(String.class);

                if ("ringing".equals(status)) {
                    showIncomingCallDialog(callerId);
                } else if ("ended".equals(status)) {
                    callRef.removeEventListener(this); // Stop listening after call ends
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error listening for calls: " + error.getMessage());
            }
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
        FirebaseDatabase.getInstance().getReference("calls").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();
        Intent intent = new Intent(this, CallActivity.class);
        intent.putExtra("channel", callerId);
        startActivity(intent);
    }
    private void rejectCall(String callerId) {
        FirebaseDatabase.getInstance().getReference("calls").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();
    }

    private void setupFirebase() {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        receiverId = getIntent().getStringExtra("userId");
        chatRef = FirebaseDatabase.getInstance().getReference("Chats");
        callRef = FirebaseDatabase.getInstance().getReference("calls");
        userRef = FirebaseDatabase.getInstance().getReference("Users");
        storageRef = FirebaseStorage.getInstance().getReference("chat_images");
    }

    private void loadReceiverInfo() {
        if (receiverId == null) return;
        userRef.child(receiverId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User receiver = snapshot.getValue(User.class);
                    if (receiver != null) {
                        receiverName.setText(receiver.getUsername());
                        Glide.with(ChatActivity.this)
                                .load(receiver.getImgUrl())
                                .placeholder(R.drawable.avtar)
                                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                .into(profilePic);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Failed to load receiver info", error.toException());
            }
        });
    }

    private void sendMessage() {
        String message = messageInput.getText().toString().trim();
        if (!TextUtils.isEmpty(message)) {
            String chatId = chatRef.push().getKey();
            if (chatId != null) {
                Chat chat = new Chat(currentUser.getUid(), receiverId, message, System.currentTimeMillis(), false, false);
                chatRef.child(chatId).setValue(chat).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        messageInput.setText("");
                        updateLastMessage(message);
                    }
                }).addOnFailureListener(e -> Log.e("Firebase", "Failed to send message", e));
            }
        }
    }

    private void updateLastMessage(String lastMsg) {
        DatabaseReference lastMsgRef = FirebaseDatabase.getInstance().getReference("LastMessages");
        HashMap<String, Object> lastMessageMap = new HashMap<>();
        lastMessageMap.put("message", lastMsg);
        lastMessageMap.put("timestamp", System.currentTimeMillis());

        lastMsgRef.child(currentUser.getUid()).child(receiverId).setValue(lastMessageMap);
        lastMsgRef.child(receiverId).child(currentUser.getUid()).setValue(lastMessageMap);
    }

    private void loadMessages() {
        chatRef.orderByChild("timestamp").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                Chat chat = snapshot.getValue(Chat.class);
                if (chat != null && isRelevantChat(chat)) {
                    if (chat.getReceiverId().equals(currentUser.getUid()) && !chat.isRecived()) {
                        snapshot.getRef().child("received").setValue(true);
                    }
                    chatList.add(chat);
                    chatAdapter.notifyItemInserted(chatList.size() - 1);
                    recyclerView.scrollToPosition(chatList.size() - 1); // Always scroll to bottom
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void initiateCall() {
        if (receiverId == null || currentUser == null) return;

        String callerId = currentUser.getUid();
        DatabaseReference callRef = FirebaseDatabase.getInstance().getReference("calls").child(receiverId);

        HashMap<String, Object> callData = new HashMap<>();
        callData.put("callerId", callerId);
        callData.put("status", "ringing");

        callRef.setValue(callData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Intent intent = new Intent(ChatActivity.this, CallActivity.class);
                intent.putExtra("channel", callerId);
                intent.putExtra("receiverId", receiverId);
                startActivity(intent);
            }
        }).addOnFailureListener(e -> Log.e("Firebase", "Failed to initiate call", e));
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 1);
    }

    private boolean isRelevantChat(Chat chat) {
        if (chat == null || currentUser == null || receiverId == null) return false;

        String senderId = chat.getSenderId();
        String receiverIdFromChat = chat.getReceiverId();
        String currentUserId = currentUser.getUid();

        if (senderId == null || receiverIdFromChat == null || currentUserId == null) return false;

        return (senderId.equals(currentUserId) && receiverIdFromChat.equals(receiverId)) ||
                (senderId.equals(receiverId) && receiverIdFromChat.equals(currentUserId));
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (receiverId != null) {
            chatRef.orderByChild("receiverId").equalTo(currentUser.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot chatSnapshot : snapshot.getChildren()) {
                                chatSnapshot.getRef().child("seen").setValue(true);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
        }
    }
}
