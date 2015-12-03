package cwa115.trongame.Lists;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import cwa115.trongame.R;

public class StatsCustomAdapter extends BaseAdapter {
    private Context context;

    private List<StatsListItem> statsList;

    public StatsCustomAdapter(Context context, List<StatsListItem> statsList) {
        this.context = context;
        this.statsList = statsList;
    }

    public int getCount() {
        return statsList.size();
    }

    public Object getItem(int position) {
        return statsList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public int getItemViewType(int position) {
        return statsList.get(position).isHeader() ? 0 : 1;// Return which layoutType has to be used for the given row (position).
    }
    // Together these methods provide correct information to receive a correct convertView in the getView-method
    public int getViewTypeCount() {
        return 2;// Return how many different layoutTypes are specified.
    }

    public View getView(int position, View convertView, ViewGroup viewGroup) {
        StatsListItem entry = statsList.get(position);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(entry.isHeader() ? R.layout.header_stats_row : R.layout.basic_stats_row, null);
        }
        if(entry.isHeader()){
            TextView title = (TextView) convertView.findViewById(R.id.header_title);
            title.setText(entry.getTitle());
        }else{
            TextView propertyName = (TextView) convertView.findViewById(R.id.property);
            TextView propertyValue = (TextView) convertView.findViewById(R.id.value);
            propertyName.setText(entry.getPropertyName());
            propertyValue.setText(entry.getPropertyValue());
        }

        return convertView;
    }
}
