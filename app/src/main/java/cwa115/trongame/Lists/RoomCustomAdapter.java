package cwa115.trongame.Lists;

import java.util.List;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import cwa115.trongame.Game.GameSettings;
import cwa115.trongame.R;

public class RoomCustomAdapter extends BaseAdapter {
    private Context context;

    private List<RoomListItem> roomList;
    private boolean isOwner;
    private int ownerId;
    private Callback callback;

    public interface Callback{
        public void OnPlayerKick(int playerId, String playerName);
    }

    public RoomCustomAdapter(Context context, List<RoomListItem> roomList, int ownerId, Callback callback) {
        this.context = context;
        this.roomList = roomList;
        this.ownerId = ownerId;
        this.isOwner = ownerId == GameSettings.getUserId();
        this.callback = callback;
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

    public int getItemViewType(int position) {
        return isOwner ? (roomList.get(position).getPlayerId()==ownerId ? 0 : 1) : 0;// Return which layoutType has to be used for the given row (position).
    }
    // Together these methods provide correct information to receive a correct convertView in the getView-method
    public int getViewTypeCount() {
        return isOwner ? 2 : 1;// Return how many different layoutTypes are specified.
    }

    public View getView(int position, View convertView, ViewGroup viewGroup) {
        RoomListItem entry = roomList.get(position);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.basic_room_row, null);
            if(isOwner){
                View kickPlayerButton = convertView.findViewById(R.id.kick_player);
                if(entry.getPlayerId()!=ownerId) kickPlayerButton.setVisibility(View.VISIBLE);
                kickPlayerButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        RoomListItem clickedItem = roomList.get((int)v.getTag());
                        if(callback!=null)
                            callback.OnPlayerKick(clickedItem.getPlayerId(), clickedItem.getPlayerName());
                    }
                });
            }
        }
        TextView playerName = (TextView) convertView.findViewById(R.id.roomPlayerName);
        playerName.setText(entry.getPlayerName());
        playerName.setTextColor(entry.getColor());
        // Set tag to position so we can access clickedItem in onclick:
        convertView.findViewById(R.id.kick_player).setTag(position);

        return convertView;
    }
}
