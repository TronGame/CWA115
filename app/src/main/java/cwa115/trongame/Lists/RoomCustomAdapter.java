package cwa115.trongame.Lists;

import java.util.List;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import cwa115.trongame.R;

public class RoomCustomAdapter extends BaseAdapter {
    private Context context;

    private List<RoomListItem> roomList;

    public RoomCustomAdapter(Context context, List<RoomListItem> roomList) {
        this.context = context;
        this.roomList = roomList;
    }

    public int getCount() {
        return roomList.size();
    }

    public Object getItem(int position) {
        return roomList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup viewGroup) {
        RoomListItem entry = roomList.get(position);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.basic_room_row, null);
        }
        TextView playerName = (TextView) convertView.findViewById(R.id.roomPlayerName);
        playerName.setText(entry.getPlayerName());
        playerName.setTextColor(entry.getColor());

        return convertView;
    }
}
