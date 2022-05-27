package com.melondev.poc_rfid;

import android.app.Application;
import android.util.Log;

import com.honeywell.rfidservice.RfidManager;
import com.honeywell.rfidservice.rfid.RfidReader;

public class MyApplication extends Application {
    private static MyApplication mInstance;
    public RfidManager rfidMgr;
    public RfidReader mRfidReader;
    public String macAddress;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        rfidMgr = RfidManager.getInstance(this);
        Log.e("MyApplication","onCreate");
    }

    public void refresh(){
        mInstance = this;
        rfidMgr = RfidManager.getInstance(this);
    }

    public static MyApplication getInstance() {
        return mInstance;
    }
}
