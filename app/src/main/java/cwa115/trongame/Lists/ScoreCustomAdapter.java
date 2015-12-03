package cwa115.trongame.Lists;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import cwa115.trongame.Game.GameSettings;
import cwa115.trongame.R;

public class ScoreCustomAdapter extends BaseAdapter {
    private Context context;

    private List<ScoreListItem> scoreList;

    public ScoreCustomAdapter(Context context, List<ScoreListItem> scoreList) {
        this.context = context;
        this.scoreList = scoreList;
    }

    public int getCount() {
        return scoreList.size();
    }

    public Object getItem(int position) {
        return scoreList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ScoreListItem entry = scoreList.get(position);
        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.basic_scoreboard_row, null);
        }
        TextView playerName = (TextView) convertView.findViewById(R.id.scorePlayerName);
        playerName.setText(entry.getPlayerName());
        TextView playerWins = (TextView) convertView.findViewById(R.id.scorePlayerWins);
        playerWins.setText(Integer.toString(entry.getGamesWon()));
        ImageView profilePicture = (ImageView) convertView.findViewById(R.id.scoreBoardPicture);
        if(entry.getPlayerPictureUrl()==null)
            profilePicture.setImageResource(R.mipmap.default_profile_picture);
        else
            GameSettings.drawableCache.fetchDrawableAsync(entry.getPlayerPictureUrl(), profilePicture);

        return convertView;
    }
}
