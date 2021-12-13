package com.example.gamealerts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.CountDownLatch;


public class MainActivity extends AppCompatActivity {

    String TAG = "test";
    ArrayList<GameInfo> mGames = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        updateGamesInfo();

        setAlarm();

    }

    public void setAlarm() {
//        Calendar calendar = Calendar.getInstance();
//        calendar.set(Calendar.HOUR_OF_DAY, 23);
//        calendar.set(Calendar.MINUTE, 47);
//        calendar.set(Calendar.SECOND, 0);
//
//        if (calendar.getTime().compareTo(new Date()) < 0)
//            calendar.add(Calendar.DAY_OF_MONTH, 1);

        Intent intent = new Intent(getApplicationContext(), NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 5, pendingIntent); // Millisec * Second * Minute

//        if (alarmManager != null) {
//            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
//        }

    }

    public void  updateGamesInfo() {
        ArrayList<GameInfo> games = new ArrayList<>();
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("games");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshots) {
                for (DataSnapshot dataSnapshot : dataSnapshots.getChildren()) {
                    String team1 = (String) dataSnapshot.child("team1").getValue();
                    String team2 = (String) dataSnapshot.child("team2").getValue();
                    String date = (String) dataSnapshot.child("date").getValue();
                    String time = (String) dataSnapshot.child("time").getValue();
                    games.add(new GameInfo(team1, team2, date, time));
                }
                String text = "";
                for (GameInfo game : games) {
                    text += game.mTeam1 + " - " + game.mTeam2 + " " + game.mDate + " " + game.mTime + "\n\n";
                }
                String nextGames = "";
                for(int i=0; i< games.size(); i++)
                {
                    if(i == 0)
                    {
                        setContentView(R.layout.activity_main);
                        TextView nextDateView = (TextView)findViewById(R.id.nextDate);
                        TextView nextTeamsView = (TextView)findViewById(R.id.nextTeams);
                        nextDateView.setText(games.get(0).mDate + ' ' +  games.get(0).mTime);
                        nextTeamsView.setText(games.get(0).mTeam1 + " - " + games.get(0).mTeam2);
                    }
                    else
                    {
                        for (GameInfo game : games) {
                            nextGames += game.mTeam1 + " - " + game.mTeam2 + " " + game.mDate + " " + game.mTime + "\n\n";
                        }
                    }
                }
                TextView otherGamesView =  (TextView)findViewById(R.id.otherGames);
                otherGamesView.setText(nextGames);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "Failed to read value.", databaseError.toException());
            }
        });

    }
}

