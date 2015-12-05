package cwa115.trongame.Lists;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import cwa115.trongame.R;
import cwa115.trongame.User.Friend;

public class FriendListAdapter extends BaseAdapter {
    private Context context;

    private List<FriendListItem> friendList;
    private boolean selectable, showPending;
    private Callback callback;

    public interface Callback{
        public void onFriendAccepted(Friend friend);
        public void onFriendRejected(Friend friend);
    }

    public FriendListAdapter(Context context, List<FriendListItem> friendList, boolean selectable, Callback callback) {
        this.context = context;
        this.friendList = friendList;
        this.selectable = selectable;
        if(selectable) callback=null;// If selectable list, don't show pending friends
        this.showPending = callback != null;
        this.callback = callback;
    }

    public int getCount() {
        return friendList.size();
    }

    public Object getItem(int position) {
        return friendList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public int getItemViewType(int position) {
        // Return which layoutType has to be used for the given row (position).
        if(!showPending) return 0;
        Friend player = friendList.get(position).getPlayer();
        if(!player.isPending()) return 0;
        return player.isInviter() ? 1 : 2;
    }
    // Together these methods provide correct information to receive a correct convertView in the getView-method
    public int getViewTypeCount() {
        return showPending ? 3 : 1;// Return how many different layoutTypes are specified.
    }

    public View getView(int position, View convertView, ViewGroup viewGroup) {
        FriendListItem entry = friendList.get(position);
        Friend player = entry.getPlayer();
        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.basic_friends_row, null);
            CheckBox playerCheckBox = (CheckBox) convertView.findViewById(R.id.friendCheckBox);
            playerCheckBox.setVisibility(selectable ? View.VISIBLE : View.GONE);
            if(selectable){
                playerCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        int pos = (int)buttonView.getTag();
                        friendList.get(pos).setSelected(isChecked);
                    }
                });
            }
            LinearLayout inviteeView = (LinearLayout)convertView.findViewById(R.id.inviteeView);
            LinearLayout inviterView = (LinearLayout)convertView.findViewById(R.id.inviterView);
            if(showPending && player.isPending()) {
                if (player.isInviter()) {
                    inviteeView.setVisibility(View.GONE);
                    inviterView.setVisibility(View.VISIBLE);
                } else {
                    inviteeView.setVisibility(View.VISIBLE);
                    inviterView.setVisibility(View.GONE);
                    convertView.findViewById(R.id.accept_button).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int pos = (int) v.getTag();
                            callback.onFriendAccepted(friendList.get(pos).getPlayer());
                        }
                    });
                    convertView.findViewById(R.id.reject_button).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int pos = (int) v.getTag();
                            callback.onFriendRejected(friendList.get(pos).getPlayer());
                        }
                    });
                }
            } else {
                inviteeView.setVisibility(View.GONE);
                inviterView.setVisibility(View.GONE);
            }
        }
        TextView playerName = (TextView) convertView.findViewById(R.id.friendName);
        playerName.setText(entry.getPlayerProfile().getName());
        if(selectable) {
            CheckBox playerCheckBox = (CheckBox) convertView.findViewById(R.id.friendCheckBox);
            playerCheckBox.setChecked(entry.isSelected());
            playerCheckBox.setTag(position);// Set tag to position so we can access entry by getting the tag
        }
        convertView.findViewById(R.id.accept_button).setTag(position);// Set tag to position so we can access entry by getting the tag
        convertView.findViewById(R.id.reject_button).setTag(position);// Set tag to position so we can access entry by getting the tag
        return convertView;
    }
}
