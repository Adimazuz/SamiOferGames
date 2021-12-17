package com.example.gamealerts;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;


public class MainActivity extends AppCompatActivity {
    String TAG = "SamiOferGameAlerts";

    private DataManager mDataManager;
    private ArrayList<GameInfo> mGames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDataManager = new DataManager(this);

        updateNotificationsToggleUI();
        if(mDataManager.isRemoteDataFetchingNeeded()) {
            getGamesDataFromRemote();
        }
        else {
            mGames = mDataManager.getLocalGamesData();
            updateGamesInfo(mGames);
        }
        setNotificationsAlarm();
    }

    private void setNotificationsAlarm() {
        Random rand = new Random();
        int max = 60;
        int min = 0;
        int randomSecond = rand.nextInt((max - min) + 1) + min;
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 9);
        calendar.set(Calendar.MINUTE, 00);
        calendar.set(Calendar.SECOND, randomSecond);

        Intent intent = new Intent(getApplicationContext(), NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 5, pendingIntent); // Millisec * Second * Minute

//        if (alarmManager != null) {
//            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
//        }

    }

    private void getGamesDataFromRemote() {
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("games");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshots) {
                mGames = new ArrayList<>();
                for (DataSnapshot dataSnapshot : dataSnapshots.getChildren()) {
                    String team1 = (String) dataSnapshot.child("team1").getValue();
                    String team2 = (String) dataSnapshot.child("team2").getValue();
                    String date = (String) dataSnapshot.child("date").getValue();
                    String time = (String) dataSnapshot.child("time").getValue();
                    mGames.add(new GameInfo(team1, team2, date, time));
                }
                mGames = mDataManager.filterPassedGames(mGames);
                mDataManager.setGamesData(mGames);
                updateGamesInfo(mGames);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "Failed to read value.", databaseError.toException());
            }
        });
    }

    private void updateNotificationsToggleUI() {
        boolean isNotificationsActive = mDataManager.getIsNotificationsActive();
        Switch s =  (Switch) findViewById(R.id.alertToggle);
        s.setChecked(isNotificationsActive);
    }

    private void  updateGamesInfo(ArrayList<GameInfo> gamesInfo) {
        String nextGames = "";
        for(int i=0; i< gamesInfo.size(); i++)
        {
            if(i == 0)
            {
                TextView nextDateView = (TextView)findViewById(R.id.nextDate);
                TextView nextTeamsView = (TextView)findViewById(R.id.nextTeams);
                nextDateView.setText(gamesInfo.get(0).mDate + ' ' +  gamesInfo.get(0).mTime);
                nextTeamsView.setText(gamesInfo.get(0).mTeam1 + " - " + gamesInfo.get(0).mTeam2);
            }
            else
            {
                for (GameInfo game : gamesInfo) {
                    nextGames += game.mDate + " " + game.mTime + " : "
                            + game.mTeam1 + " - " + game.mTeam2 + " " + "\n\n";
                }
            }
        }
        TextView otherGamesView =  (TextView)findViewById(R.id.otherGames);
        otherGamesView.setText(nextGames);
    }

    public void onAlertsToggleClick(View view) {
        Switch s = (Switch) view;
        mDataManager.setIsNotificationsActive(s.isChecked());
    }

    public void openSettingsActivity(View view) {
//        Intent intent = new Intent(this, SettingsActivity.class);
//        startActivity(intent);
    }
}

