package com.gamealerts;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {
    String TAG = "SamiOferGameAlerts";
    private DataManager mDataManager;
    private ArrayList<GameInfo> mGames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).hide(); //hide the title bar

        mDataManager = new DataManager(this);
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            new AlarmSetter().setAlarm(getApplicationContext());
        }
    }

    private void getGamesDataFromRemote() {
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("games");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshots) {
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

    private void  updateGamesInfo(@NonNull ArrayList<GameInfo> gamesInfo) {
        TextView nextDateView = (TextView)findViewById(R.id.nextDate);
        TextView nextGameHourView = (TextView)findViewById(R.id.nextGameHour);
        TextView nextTeamsView = (TextView)findViewById(R.id.nextTeams);

        if (gamesInfo.size() == 0 ) {
            nextDateView.setText("אין משחקים קרובים");
            return;
        }
        GameInfo next_game = gamesInfo.get(0);
        List<GameInfo> later_games = gamesInfo.subList(1, gamesInfo.size());

        nextDateView.setText(next_game.mDate);
        nextGameHourView.setText(next_game.mTime);
        nextTeamsView.setText(String.format("%s - %s", next_game.mTeam1, gamesInfo.get(0).mTeam2));

        if(next_game.isGameToday()) {
            nextDateView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.alert));
            nextGameHourView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.alert));
        }

        ListView otherGamesView = (ListView) findViewById(R.id.my_list_view);
        GamesAdapter adapter = new GamesAdapter(later_games, this);
        otherGamesView.setAdapter(adapter);
    }

    public void refreshGamesClicked(View view) {
        getGamesDataFromRemote();
        Toast.makeText(getApplicationContext(),"Refreshed",Toast.LENGTH_SHORT).show();
    }

    public void settingsClicked(View view) {
        Intent myIntent = new Intent(this, SettingsActivity.class);
        this.startActivity(myIntent);
    }
}

