package com.example.e_commerce.Activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.e_commerce.Model.CallHistory;
import com.example.e_commerce.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.rtc2.video.VideoCanvas;

public class CallActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1001;
    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
    };

    private static final String APP_ID = "ENTER_YOU_APP_ID";
    private static final String CHANNEL_NAME = "callingPerson";
    private static final String TOKEN = "ENTER_YOUR_TOKEN";

    private RtcEngine agoraEngine;
    private ImageView switchCameraButton;
    private SurfaceView localSurfaceView, remoteSurfaceView;
    private boolean isJoined = false;
    private String receiverId;
    private long callStartTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        switchCameraButton = findViewById(R.id.switch_camera);
        receiverId = getIntent().getStringExtra("receiverId");

        if (!checkPermissions()) {
            requestPermissions();
        } else {
            initializeCall();
        }

        switchCameraButton.setOnClickListener(v -> switchCamera());
        findViewById(R.id.endCallButton).setOnClickListener(v -> leaveCall());
    }

    private void initializeCall() {
        setupVideoSDKEngine();
        joinCall();
    }

    private void switchCamera() {
        if (agoraEngine != null) {
            agoraEngine.switchCamera(); // Switch between front and rear cameras
        }
    }

    private void setupVideoSDKEngine() {
        try {
            RtcEngineConfig config = new RtcEngineConfig();
            config.mContext = getBaseContext();
            config.mAppId = APP_ID;
            config.mEventHandler = mRtcHandler;
            agoraEngine = RtcEngine.create(config);
            agoraEngine.enableVideo();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to initialize Agora Engine", Toast.LENGTH_SHORT).show();
        }
    }

    private final IRtcEngineEventHandler mRtcHandler = new IRtcEngineEventHandler() {
        @Override
        public void onUserJoined(int uid, int elapsed) {
            runOnUiThread(() -> setupRemoteVideo(uid));
        }

        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            isJoined = true;
            callStartTime = System.currentTimeMillis(); // Start call time
        }

        @Override
        public void onUserOffline(int uid, int reason) {
            runOnUiThread(() -> leaveCall());
        }
    };

    private void setupLocalVideo() {
        FrameLayout localContainer = findViewById(R.id.localVideoContainer);

        if (localSurfaceView == null) {
            localSurfaceView = new SurfaceView(getBaseContext());
            localSurfaceView.setZOrderMediaOverlay(true); // Ensure local video appears on top
        }

        localContainer.removeAllViews();
        localContainer.addView(localSurfaceView);

        VideoCanvas localCanvas = new VideoCanvas(localSurfaceView, VideoCanvas.RENDER_MODE_HIDDEN, 0);
        agoraEngine.setupLocalVideo(localCanvas);

        localSurfaceView.setVisibility(View.VISIBLE);
    }

    private void setupRemoteVideo(int uid) {
        FrameLayout remoteContainer = findViewById(R.id.remoteVideoContainer);

        if (remoteSurfaceView == null) {
            remoteSurfaceView = new SurfaceView(getBaseContext());
        }

        remoteContainer.removeAllViews();
        remoteContainer.addView(remoteSurfaceView);

        VideoCanvas remoteCanvas = new VideoCanvas(remoteSurfaceView, VideoCanvas.RENDER_MODE_HIDDEN, uid);
        agoraEngine.setupRemoteVideo(remoteCanvas);

        remoteSurfaceView.setVisibility(View.VISIBLE);
    }

    private void joinCall() {
        ChannelMediaOptions options = new ChannelMediaOptions();
        options.channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION;
        options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;

        setupLocalVideo();
        agoraEngine.startPreview();
        agoraEngine.joinChannel(TOKEN, CHANNEL_NAME, 0, options);

        findViewById(R.id.endCallButton).setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        leaveCall();
    }

    private void leaveCall() {
        if (agoraEngine != null) {
            agoraEngine.leaveChannel();
            agoraEngine.stopPreview();
            RtcEngine.destroy();
        }

        long callDuration = (System.currentTimeMillis() - callStartTime) / 1000; // Duration in seconds
        saveCallHistory("Outgoing", receiverId, callDuration);

        runOnUiThread(() -> {
            onBackPressed();
        });

        notifyReceiverCallEnded();
    }

    private void notifyReceiverCallEnded() {
        DatabaseReference callRef = FirebaseDatabase.getInstance().getReference("calls").child(receiverId);
        callRef.child("status").setValue("ended");
    }

    private void saveCallHistory(String callType, String receiverId, long duration) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        String callerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");

        userRef.child(receiverId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot receiverSnapshot) {
                if (receiverSnapshot.exists()) {
                    String receiverName = receiverSnapshot.child("username").getValue(String.class);
                    String receiverProfilePic = receiverSnapshot.child("profilePic").getValue(String.class);

                    userRef.child(callerId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot callerSnapshot) {
                            if (callerSnapshot.exists()) {
                                String callerName = callerSnapshot.child("username").getValue(String.class);
                                String callerProfilePic = callerSnapshot.child("imgUrl").getValue(String.class);

                                DatabaseReference callHistoryRef = FirebaseDatabase.getInstance().getReference("CallHistory");
                                String callId = callHistoryRef.child(callerId).push().getKey();
                                long timestamp = System.currentTimeMillis();

                                CallHistory callHistoryForCaller = new CallHistory(
                                        callId, callerId, receiverId, callType, receiverName, receiverProfilePic, timestamp, duration
                                );

                                CallHistory callHistoryForReceiver = new CallHistory(
                                        callId, receiverId, callerId, "Incoming", callerName, callerProfilePic, timestamp, duration
                                );

                                if (callId != null) {
                                    callHistoryRef.child(callerId).child(callId).setValue(callHistoryForCaller);
                                    callHistoryRef.child(receiverId).child(callId).setValue(callHistoryForReceiver);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }


    private boolean checkPermissions() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                initializeCall();
            } else {
                showPermissionDeniedDialog();
            }
        }
    }

    private void showPermissionDeniedDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Permissions Required")
                .setMessage("Camera and Microphone permissions are required for video calls.")
                .setPositiveButton("Retry", (dialog, which) -> requestPermissions())
                .setNegativeButton("Exit", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }
}
