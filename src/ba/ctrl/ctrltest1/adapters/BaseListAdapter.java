package ba.ctrl.ctrltest1.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import ba.ctrl.ctrltest1.R;
import ba.ctrl.ctrltest1.bases.Base;
import ba.ctrl.ctrltest1.database.DataSource;

public class BaseListAdapter extends ArrayAdapter<Base> {

    /**************************************************************************
     * Static fields and methods                                              *
     **************************************************************************/
    static class ViewHolder {
        public View base_connection_status;
        public ImageView base_icon;
        public TextView base_title;
        public TextView base_display_data;
        public TextView base_stamp;
        public TextView base_type;
        public TextView base_unseen_count;
    }

    /**************************************************************************
     * Private fields                                                         *
     **************************************************************************/
    private final Context context;
    private final ArrayList<Base> bases;

    private DataSource dataSource;

    /**************************************************************************
     * Constructors                                                           *
     **************************************************************************/
    public BaseListAdapter(Context context, ArrayList<Base> bases) {
        super(context, R.layout.main_activity_item, bases);
        this.context = context;
        this.bases = bases;
        dataSource = DataSource.getInstance(this.context);
        setNotifyOnChange(true);
    }

    // Refreshanje adaptera sa novim podacima. Scroll position ce biti zadrzan -
    // super!
    public void refill(ArrayList<Base> bases) {
        this.bases.clear();
        this.bases.addAll(bases);
        notifyDataSetChanged();
    }

    /**************************************************************************
     * Overridden parent methods                                              *
     **************************************************************************/
    /**
     * This method is called automatically when the user scrolls the ListView.
     * Updates the View of a single visible row, reflecting the list being
     * scrolled by the user.
     *
     * EXPLAINED HERE: http://www.youtube.com/watch?v=N6YdwzAvwOA
     */
    @Override
    public View getView(int position, View convert_view, ViewGroup parent) {
        View view = convert_view;

        Base base = bases.get(position);
        // if view is null, the view is newly inflated.
        // else, re-assign new values.
        ViewHolder view_holder;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.main_activity_item, null);

            // Set up the ViewHolder.
            view_holder = new ViewHolder();

            view_holder.base_connection_status = (View) view.findViewById(R.id.base_connection_status);
            view_holder.base_icon = (ImageView) view.findViewById(R.id.base_icon);
            view_holder.base_title = (TextView) view.findViewById(R.id.base_title);
            view_holder.base_display_data = (TextView) view.findViewById(R.id.base_display_data);
            view_holder.base_stamp = (TextView) view.findViewById(R.id.base_stamp);
            view_holder.base_type = (TextView) view.findViewById(R.id.base_type);
            view_holder.base_unseen_count = (TextView) view.findViewById(R.id.base_unseen_count);

            // Store the holder with the view.
            view.setTag(view_holder);
        }
        else {
            view_holder = (ViewHolder) view.getTag();
        }

        view_holder.base_connection_status.setBackgroundColor(base.getStatusColor());
        view_holder.base_icon.setImageResource(base.getBaseIconRID(context));
        view_holder.base_title.setText(base.getTitle());
        view_holder.base_display_data.setText(base.getDisplayData());
        //view_holder.base_stamp.setBackgroundColor(base.getStatusColor());
        view_holder.base_stamp.setText(DateUtils.getRelativeTimeSpanString(base.getStamp(), System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE));
        view_holder.base_type.setText(base.getBaseTypeTitle());

        int unseenCount = dataSource.getUnseenCount(base.getBaseid());
        if (unseenCount > 0) {
            view_holder.base_unseen_count.setVisibility(TextView.VISIBLE);
            view_holder.base_unseen_count.setText(String.valueOf(unseenCount));
        }
        else {
            view_holder.base_unseen_count.setVisibility(TextView.GONE);
        }

        return view;
    }
}
