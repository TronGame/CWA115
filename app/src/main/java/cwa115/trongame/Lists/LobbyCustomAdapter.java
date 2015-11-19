package cwa115.trongame.Lists;



import java.util.List;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import cwa115.trongame.R;

public class LobbyCustomAdapter extends BaseAdapter {
    private Context context;

    private List<LobbyListItem> lobbyList;

    public LobbyCustomAdapter(Context context, List<LobbyListItem> lobbyList) {
        this.context = context;
        this.lobbyList = lobbyList;
    }

    public int getCount() {
        return lobbyList.size();
    }

    public Object getItem(int position) {
        return lobbyList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup viewGroup) {
        LobbyListItem entry = lobbyList.get(position);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.basic_lobby_row, null);
        }
        TextView gameName = (TextView) convertView.findViewById(R.id.gameName);
        gameName.setText(entry.getGamename());

        TextView host = (TextView) convertView.findViewById(R.id.hostName);
        host.setText(entry.getHost());

        TextView players = (TextView) convertView.findViewById(R.id.nb_players);
        players.setText(entry.getPlayers());

        return convertView;
    }
}
