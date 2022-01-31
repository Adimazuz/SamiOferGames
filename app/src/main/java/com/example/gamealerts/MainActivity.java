package com.example.gamealerts;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;


public class MainActivity extends AppCompatActivity {
    String TAG = "SamiOferGameAlerts";

    private DataManager mDataManager;
    private ArrayList<GameInfo> mGames = new ArrayList<>();

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide(); //hide the title bar

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
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 10);
        calendar.set(Calendar.SECOND, randomSecond);

        Intent intent = new Intent(getApplicationContext(), NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        if (calendar.getTime().compareTo(new Date()) < 0)
            calendar.add(Calendar.DAY_OF_MONTH, 1);

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);

//        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 5, pendingIntent); // Millisec * Second * Minute

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
        SwitchCompat s =  (SwitchCompat) findViewById(R.id.alertToggle);
        s.setChecked(isNotificationsActive);
    }

    private void  updateGamesInfo(ArrayList<GameInfo> gamesInfo) {
        gamesInfo.subList(1,gamesInfo.size() - 1);
        GameInfo next_game = gamesInfo.get(0);

        TextView nextDateView = (TextView)findViewById(R.id.nextDate);
        TextView nextGameHourView = (TextView)findViewById(R.id.nextGameHour);
        TextView nextTeamsView = (TextView)findViewById(R.id.nextTeams);
        nextDateView.setText(next_game.mDate);
        nextGameHourView.setText(next_game.mTime);
        nextTeamsView.setText(next_game.mTeam1 + " - " + gamesInfo.get(0).mTeam2);

//        List<GameInfo> nextGames =new ArrayList<GameInfo>();

//        for(int i=0; i< gamesInfo.size(); i++)
//        {
//            if(i == 0)
//            {
//                TextView nextDateView = (TextView)findViewById(R.id.nextDate);
//                TextView nextGameHourView = (TextView)findViewById(R.id.nextGameHour);
//                TextView nextTeamsView = (TextView)findViewById(R.id.nextTeams);
//                nextDateView.setText(gamesInfo.get(0).mDate);
//                nextGameHourView.setText(gamesInfo.get(0).mTime);
//                nextTeamsView.setText(gamesInfo.get(0).mTeam1 + " - " + gamesInfo.get(0).mTeam2);
//            }
//            else
//            {
//                nextGames.add(gamesInfo.get(i).mDate + " " + gamesInfo.get(i).mTime + " : "
//                        + gamesInfo.get(i).mTeam1 + " - " + gamesInfo.get(i).mTeam2 + " " + "\n\n");
//            }
//        }
        ListView otherGamesView =  (ListView)findViewById(R.id.my_list_view);
        GamesAdapter adapter = new GamesAdapter(gamesInfo.subList(1,gamesInfo.size() - 1), this);
        otherGamesView.setAdapter(adapter);
    }

    public void onAlertsToggleClick(View view) {
        SwitchCompat s = (SwitchCompat) view;
        mDataManager.setIsNotificationsActive(s.isChecked());
    }

    public void openSettingsActivity(View view) {
//        Intent intent = new Intent(this, SettingsActivity.class);
//        startActivity(intent);
    }
}

