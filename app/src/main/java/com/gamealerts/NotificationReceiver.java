package com.gamealerts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        NotificationSender notificationHelper = new NotificationSender(context);
        notificationHelper.sendNotificationIfNeeded();
    }
}
