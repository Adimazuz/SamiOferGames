package com.gamealerts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class DeviceBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            new AlarmSetter().setAlarm(context);
            Log.d("SOGADeviceBootReceiver", "boot alarm set");

        }
    }
}