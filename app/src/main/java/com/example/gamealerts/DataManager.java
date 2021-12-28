package com.example.gamealerts;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class DataManager {

    private SharedPreferences mSharedPreferences;
    private String GAMES_DATA_KEY = "games_data";
    private String IS_NOTIFICATIONS_ACTIVE = "is_notifications_active";
    private String LAST_UPDATE_DATE = "last_update_date";
    private int mDaysBetweenFetching = 3;

    DataManager(Context context){
        mSharedPreferences = context.getSharedPreferences("com.example.gamealerts", Context.MODE_PRIVATE);
    }

    public void setGamesData(ArrayList<GameInfo> gamesData) {
        String serliezedGameData = "";
        try {
            serliezedGameData = ObjectSerializer.serialize(gamesData);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mSharedPreferences.edit().putString(GAMES_DATA_KEY, serliezedGameData).apply();
        setLastRemoteFetchDate(new Date());
    }

    public ArrayList<GameInfo> getLocalGamesData() {
        try {
            String gamesDataString = mSharedPreferences.getString(GAMES_DATA_KEY,"");
            ArrayList<GameInfo> games = (ArrayList<GameInfo>) ObjectSerializer.deserialize(gamesDataString);
            return filterPassedGames(games);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<GameInfo>();
    }

    public ArrayList<GameInfo> getGamesData() {
        int retries = 5;
        int sleepDuration = 100;

        ArrayList<GameInfo> allGames = getLocalGamesData();
        if(allGames.size() == 0) {
            getGamesDataFromRemote();
        }
        while(allGames.size() == 0 && retries > 0) {
            try {
                Thread.sleep(sleepDuration);
                allGames = getLocalGamesData();
                retries--;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return allGames;
    }

    public ArrayList<GameInfo> filterPassedGames(ArrayList<GameInfo> gamesInfo) {
        Date today = new Date();
        ArrayList<GameInfo> res = new ArrayList<GameInfo>();
        for (GameInfo game: gamesInfo)
        {
            try {
                SimpleDateFormat date_format = new SimpleDateFormat("dd/MM/yyyy");
                Date gameDate=date_format.parse(game.mDate);
                if( gameDate.after(today) || game.mDate.equals(date_format.format(today)))
                {
                    res.add(game);
                }

            } catch (Exception e) {
                continue;
            }
        }
        return res;
    }

    public boolean getIsNotificationsActive() {
        return mSharedPreferences.getBoolean(IS_NOTIFICATIONS_ACTIVE,true);
    }

    public void setIsNotificationsActive(boolean isActive) {
        mSharedPreferences.edit().putBoolean(IS_NOTIFICATIONS_ACTIVE,isActive).apply();
    }

    public void setLastRemoteFetchDate(Date date) {
        String serliezedData = "";
        try {
            serliezedData = ObjectSerializer.serialize(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mSharedPreferences.edit().putString(LAST_UPDATE_DATE, serliezedData).apply();
    }

    public Date getLastRemoteFetchDate() {
        try {
            String dataString = mSharedPreferences.getString(LAST_UPDATE_DATE,"");
            if( dataString == "") return null;
            return (Date)ObjectSerializer.deserialize(dataString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isRemoteDataFetchingNeeded() {
        ArrayList<GameInfo> localData = getLocalGamesData();
        Date lastUpdateDate = getLastRemoteFetchDate();
        if(lastUpdateDate == null) return true;
        long difference = new Date().getTime() - lastUpdateDate.getTime();
        float daysBetween = (difference / (1000*60*60*24));

        return ( localData.size() == 0 || daysBetween > mDaysBetweenFetching);
    }

    public void getGamesDataFromRemote() {
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("games");
        myRef.addValueEventListener(new ValueEventListener() {
            private String TAG =  "samiOferGames";

            @Override
            public void onDataChange(DataSnapshot dataSnapshots) {
                ArrayList<GameInfo> games = new ArrayList<>();
                for (DataSnapshot dataSnapshot : dataSnapshots.getChildren()) {
                    String team1 = (String) dataSnapshot.child("team1").getValue();
                    String team2 = (String) dataSnapshot.child("team2").getValue();
                    String date = (String) dataSnapshot.child("date").getValue();
                    String time = (String) dataSnapshot.child("time").getValue();
                    games.add(new GameInfo(team1, team2, date, time));
                }
                games = filterPassedGames(games);
                setGamesData(games);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "Failed to read value.", databaseError.toException());
            }
        });
    }

    public void setIsDayNotified(Days day, boolean notify){
        mSharedPreferences.edit().putBoolean(day.name(), notify).apply();
    }

    public boolean getIsDayNotified(Days day){
        return mSharedPreferences.getBoolean(day.name(),true);
    }

}

