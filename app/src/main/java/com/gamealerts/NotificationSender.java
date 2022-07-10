package com.gamealerts;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


class NotificationSender {
    private Context mContext;
    private DataManager mDataManager;
    private static final String NOTIFICATION_CHANNEL_ID = "10001";

    NotificationSender(Context context) {
        mContext = context;
        mDataManager = new DataManager(context);
    }

    public void sendNotificationIfNeeded() {
        mDataManager.getGamesDataFromRemote();
        ArrayList<GameInfo> allGames = mDataManager.getGamesData();
        if(allGames.size() == 0) {
            return;
        }
        GameInfo closestGame = allGames.get(0);
        if (mDataManager.isTodayNotified() && closestGame.isGameToday()) {
            createGameNotification(closestGame);
        }
    }

    private void createGameNotification(GameInfo gameInfo) {
        String notificationContent = String.format("משחק יתקיים היום בשעה %s הזהר מפקקים", gameInfo.mTime);
        String notificationTitle = "משחק היום בסמי עופר";

        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(mContext, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_MUTABLE);
//        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_MUTABLE);

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "SamiOferGamesNotifications";
            String description = "desc";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = mContext.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.stadium)
                .setContentTitle(notificationTitle)
                .setContentText(notificationContent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(0, builder.build());
    }


}
