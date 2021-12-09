package com.example.gamealerts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import com.google.android.gms.tasks.Tasks;


public class MainActivity extends AppCompatActivity {

    String TAG = "test";
    ArrayList<GameInfo> mGames = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        updateGamesInfo();
    }

    public void  updateGamesInfo() {
        ArrayList<GameInfo> games = new ArrayList<>();
        CountDownLatch countDownLatch = new CountDownLatch(1);
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
                setContentView(R.layout.activity_main);
                TextView tv = (TextView)findViewById(R.id.mainText);
                tv.setText(text);

                int x =1;
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "Failed to read value.", databaseError.toException());
            }
        });

    }
}

