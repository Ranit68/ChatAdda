package com.example.e_commerce;

import android.content.Context;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.IRtcEngineEventHandler;

public class AgoraManager {
    private static RtcEngine rtcEngine;
    private static final String APP_ID = "a3405ba1467d4edcbd75361ddc037c15"; // Replace with your actual App ID

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

// private String appId = "a3405ba1467d4edcbd75361ddc037c15";
//    private String channelName = "testChannel";
//    private String token = "007eJxTYOAoLz2Tpnr2ktSUx5uM1Xe6qi1NmHp6guV3Lk69/x6BZ98oMCQamxiYJiUampiZp5ikpiQnpZibGpsZpqQkGxibJxuaOs29md4QyMjw81YVEyMDBIL43AwlqcUlzhmJeXmpOQwMAEJsIx0=";
