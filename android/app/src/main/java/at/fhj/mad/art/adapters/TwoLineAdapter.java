package at.fhj.mad.art.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import at.fhj.mad.art.R;
import at.fhj.mad.art.model.Task;

/**
 * ArrayAdapter for the custom ListView
 */
public class TwoLineAdapter extends ArrayAdapter<Task> {

    public TwoLineAdapter(Context context, List<Task> objects) {
        super(context, R.layout.tasklist_item, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Task t = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.tasklist_item, parent, false);
        }
        TextView text1 = (TextView) convertView.findViewById(R.id.tasklist_item_message);
        TextView text2 = (TextView) convertView.findViewById(R.id.tasklist_item_time);

        text1.setText(t.getMessage());
        text2.setText("ID: " + t.getId() + " | " + getContext().getResources().getString(R.string.lineadapter_received_on) + " " + t.getDate() + " | " + t.getDayTime());

        return convertView;
    }
}