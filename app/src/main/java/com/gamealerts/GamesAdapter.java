package com.gamealerts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import java.util.List;

public class GamesAdapter extends BaseAdapter {
    private List<GameInfo> games;
    private Context context;

    public GamesAdapter(List<GameInfo> games, Context context) {
        this.games = games;
        this.context = context;
    }

    @Override
    public int getCount() {
        return games.size();
    }

    @Override
    public Object getItem(int i) {
        return games.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view==null) {
            LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.game_row,viewGroup,false);
        }
        GameInfo gameInfo = games.get(i);
        TextView dateTv =  (TextView)view.findViewById(R.id.row_date);
        TextView timeTv =  (TextView)view.findViewById(R.id.row_time);
        TextView teamsTv = (TextView)view.findViewById(R.id.row_teams);

        dateTv.setText(gameInfo.mDate);
        timeTv.setText(gameInfo.mTime);
        teamsTv.setText(gameInfo.mTeam1 + " : " + gameInfo.mTeam2);
        return view;
    }
}
