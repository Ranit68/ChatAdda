package com.example.e_commerce.Model;

public class CallHistory {
    private String callId;
    private String callerId;
    private String receiverId;
    private String callType;
    private String userName; // Display name (receiver for caller, caller for receiver)
    private String userProfilePic; // Profile picture of the other person
    private long timestamp;
    private long duration;

    public CallHistory() {
    }

    public CallHistory(String callId, String callerId, String receiverId, String callType, String userName, String userProfilePic, long timestamp, long duration) {
        this.callId = callId;
        this.callerId = callerId;
        this.receiverId = receiverId;
        this.callType = callType;
        this.userName = userName;
        this.userProfilePic = userProfilePic;
        this.timestamp = timestamp;
        this.duration = duration;
    }

    public String getCallId() {
        return callId;
    }

    public String getCallerId() {
        return callerId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public String getCallType() {
        return callType;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserProfilePic() {
        return userProfilePic;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getDuration() {
        return duration;
    }
}
