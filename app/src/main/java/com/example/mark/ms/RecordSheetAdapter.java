package com.example.mark.ms;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class RecordSheetAdapter extends RecyclerView.Adapter<RecordSheetAdapter.ViewHolder> {

    private ArrayList<HashMap<String, Object>> mDataset;
    private Context mContext;
    private String dateFormat = "dd MMMM";
    private SimpleDateFormat sfd = new SimpleDateFormat(dateFormat);
    Resources r;

    public RecordSheetAdapter(Context context, ArrayList<HashMap<String, Object>> myDataset) {
        this.mDataset = myDataset;
        this.mContext = context;
        r = this.mContext.getResources();
    }

    @Override
    public RecordSheetAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                            int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.record_sheet_item_fancy, parent, false);
        RecordSheetAdapter.ViewHolder vh = new RecordSheetAdapter.ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(RecordSheetAdapter.ViewHolder holder, int position) {

        holder.setItem(mDataset.get(holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView dateRecord;
        TextView commentRecord;
        TextView hoursRecord;
        TextView moduleRecord;

        public ViewHolder(View v) {
            super(v);
            dateRecord = v.findViewById(R.id.dateRecord);
            commentRecord = v.findViewById(R.id.commentRecord);
            hoursRecord = v.findViewById(R.id.hoursRecord);
            moduleRecord = v.findViewById(R.id.moduleRecord);
        }

        @SuppressLint("SetTextI18n")
        public void setItem(HashMap<String, Object> item) {

            this.moduleRecord.setText("Module: " +
                            item.get(r.getString(R.string.MODULE)).toString());

            this.commentRecord.setText("Comment: " +
                    item.get(r.getString(R.string.COMMENT)).toString());


            String hours = item.get(r.getString(R.string.HOURS)).toString();
            if (hours.equals("1")) {
                hours += " hour";
            } else {
                hours += " hours";
            }

            hoursRecord.setText(hours);
            Timestamp timestamp = (Timestamp) item.get(r.getString(R.string.DATE));
            Date sessionDate = timestamp.toDate();
            dateRecord.setText(sfd.format(sessionDate));
        }
    }

}


