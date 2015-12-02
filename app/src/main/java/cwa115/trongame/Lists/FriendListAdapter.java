package cwa115.trongame.Lists;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.List;

import cwa115.trongame.R;

public class FriendListAdapter extends BaseAdapter {
    private Context context;

    private List<FriendListItem> friendList;
    private boolean selectable;

    public FriendListAdapter(Context context, List<FriendListItem> friendList, boolean selectable) {
        this.context = context;
        this.friendList = friendList;
        this.selectable = selectable;
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

    public View getView(int position, View convertView, ViewGroup viewGroup) {
        FriendListItem entry = friendList.get(position);
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
        }
        TextView playerName = (TextView) convertView.findViewById(R.id.friendName);
        playerName.setText(entry.getPlayerName());
        if(selectable) {
            CheckBox playerCheckBox = (CheckBox) convertView.findViewById(R.id.friendCheckBox);
            playerCheckBox.setChecked(entry.isSelected());
            playerCheckBox.setTag(position);// Set tag to position so we can access entry by getting the tag
        }
        return convertView;
    }
}
