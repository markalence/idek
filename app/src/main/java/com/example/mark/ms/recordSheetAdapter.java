package com.example.mark.ms;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextClock;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class recordSheetAdapter extends RecyclerView.Adapter<recordSheetAdapter.ViewHolder> {


    public ArrayList<HashMap<String,String>> mDataset;
    public Context mContext;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView dateRecord;
        TextView hoursRecord;
        TextView moduleRecord;
        TextView commentRecord;


        public ViewHolder(View v) {

            super(v);

            dateRecord = v.findViewById(R.id.dateRecord);
            hoursRecord = v.findViewById(R.id.hoursRecord);
            moduleRecord = v.findViewById(R.id.moduleRecord);
            commentRecord = v.findViewById(R.id.commentRecord);



        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public recordSheetAdapter(Context context,ArrayList<HashMap<String,String>> myDataset) {
        mDataset = myDataset;
        mContext=context;
        if(!myDataset.isEmpty()){
            notifyDataSetChanged();
            System.out.println(mDataset + " ????");
        }
    }



    // Create new views (invoked by the layout manager)
    @Override
    public recordSheetAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view


        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.record_sheet_item, parent, false);
        recordSheetAdapter.ViewHolder vh = new recordSheetAdapter.ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(recordSheetAdapter.ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        holder.dateRecord.setText((CharSequence) mDataset.get(position).get("date"));
        holder.moduleRecord.setText((CharSequence) mDataset.get(position).get("module"));
        holder.hoursRecord.setText((CharSequence) mDataset.get(position).get("hours"));
        holder.commentRecord.setText((CharSequence) mDataset.get(position).get("comment"));


        holder.dateRecord.setWidth(MainActivity.date.getWidth());
        holder.moduleRecord.setWidth(MainActivity.module.getWidth());
        holder.hoursRecord.setWidth(MainActivity.hours.getWidth());
        holder.commentRecord.setWidth(MainActivity.comment.getWidth());

        Log.d("AWE", mDataset.get(position).get("date"));


    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {

        return mDataset.size();
    }
}


