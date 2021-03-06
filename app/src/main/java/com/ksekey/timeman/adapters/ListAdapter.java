package com.ksekey.timeman.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ksekey.timeman.R;
import com.ksekey.timeman.models.TimeEntry;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by kk on 26/12/2017.
 */

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {
    private List<TimeEntry> mDataset = new ArrayList<>();
    private OnClickItem onClickItem;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

    public void setDataset( List<TimeEntry> mDataset) {
        this.mDataset = mDataset;
        notifyDataSetChanged();
    }

    public void setOnClickItem(OnClickItem onClickItem) {
        this.onClickItem = onClickItem;
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView taskName;
        public TextView description;
        public TextView timeInMinutes;
        public TextView date;
        public TextView numWeek;

        public ViewHolder(View v) {
            super(v);
            taskName = v.findViewById(R.id.task_name);
            timeInMinutes = v.findViewById(R.id.time_in_minutes);
            description = v.findViewById(R.id.description);
            date = v.findViewById(R.id.date);
            numWeek=v.findViewById(R.id.number_of_week);
        }
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item, parent, false));
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.taskName.setText(mDataset.get(position).getTask().getName());
        holder.description.setText(mDataset.get(position).getDescription());
        holder.timeInMinutes.setText(String.valueOf(mDataset.get(position).getTimeInMinutes()));
        holder.timeInMinutes.append(" min");
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickItem.onClick(mDataset.get(position));
            }
        });
        holder.date.setText(dateFormat.format(mDataset.get(position).getDate()));

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(mDataset.get(position).getDate());
        int week = calendar.get(Calendar.WEEK_OF_YEAR);
        holder.numWeek.setText(String.valueOf(week));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public interface OnClickItem{
        void onClick(TimeEntry timeEntry);
    }
}
