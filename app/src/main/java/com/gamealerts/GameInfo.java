package com.gamealerts;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GameInfo implements Serializable {
    public String mTeam1;
    public String mTeam2;
    public String mDate;
    public String mTime;

    public GameInfo(String team1, String team2, String date, String time)
    {
        mTeam1 = team1;
        mTeam2 = team2;
        mDate = date;
        mTime = time;
    }
    public boolean isGameToday() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String today = dateFormat.format(new Date());
        return this.mDate.equals(today);
    }
}
