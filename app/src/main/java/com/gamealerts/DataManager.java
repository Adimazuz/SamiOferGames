package com.gamealerts;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

public class DataManager {

    private SharedPreferences mSharedPreferences;
    private String GAMES_DATA_KEY = "games_data";
    private String IS_NOTIFICATIONS_ACTIVE = "notification_switch";
    private String LAST_UPDATE_DATE = "last_update_date";
    private String NOTIFICATION_HOUR = "notification_hour";
    private String NOTIFICATION_MINUTE = "notification_minute";
    private String NOTIFICATION_DAYS = "ml_list";
    private int mDaysBetweenFetching = 1;

    DataManager(Context context){
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
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

    public int getNotificationHour() {
        return mSharedPreferences.getInt(NOTIFICATION_HOUR,9);
    }
    public void setNotificationHour(int hour) {
        mSharedPreferences.edit().putInt(NOTIFICATION_HOUR, hour).apply();
    }

    public int getNotificationMinute() {
        return mSharedPreferences.getInt(NOTIFICATION_MINUTE,0);
    }

    public void setNotificationMinute(int minute) {
        mSharedPreferences.edit().putInt(NOTIFICATION_MINUTE, minute).apply();
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

    public boolean isTodayNotified(){
        Calendar calendar = Calendar.getInstance();
        int intDay = calendar.get(Calendar.DAY_OF_WEEK);
        DaysEnum day = DaysEnum.values()[intDay - 1];

        Set<String> notifiedDays = mSharedPreferences.getStringSet(NOTIFICATION_DAYS,  Collections.emptySet());
        boolean isNotificationsActive = mSharedPreferences.getBoolean(IS_NOTIFICATIONS_ACTIVE, true);
        return notifiedDays.contains(day.toString()) && isNotificationsActive;
    }

}

