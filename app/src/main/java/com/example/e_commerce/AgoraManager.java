package com.example.e_commerce;

import android.content.Context;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.IRtcEngineEventHandler;

public class AgoraManager {
    private static RtcEngine rtcEngine;
    private static final String APP_ID = "ENTE_YOUR_APP_ID"; // Replace with your actual App ID

    public static RtcEngine getRtcEngine(Context context) {
        if (rtcEngine == null) {
            try {
                rtcEngine = RtcEngine.create(context, APP_ID, new IRtcEngineEventHandler() {});
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return rtcEngine;
    }
}