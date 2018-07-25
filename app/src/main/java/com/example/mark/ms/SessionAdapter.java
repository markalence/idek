package com.example.mark.ms;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class SessionAdapter extends RecyclerView.Adapter<SessionAdapter.ViewHolder> {

    ArrayList<HashMap<String, Object>> mDataset;
    Context mContext;
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    private String DATE_FORMAT = "EEEE, MMMM dd";
    private String HOUR_FORMAT = "kk:mm";
    private String UPCOMING_SESSIONS = "upcomingsessions";
    private String STUDENTS = "students";
    private String HOURS = "hours";
    private String DATE = "date";
    private String SCHEDULE = "schedule";
    private String FIRST_NAME = "firstName";
    private String LAST_NAME = "lastName";
    private String USERNAME = "username";
    private String TODAYSCHEDULE = "todayschedule";
    SimpleDateFormat sfd = new SimpleDateFormat(DATE_FORMAT);


    public SessionAdapter(Context context, ArrayList<HashMap<String, Object>> myDataset) {
        mDataset = myDataset;
        mContext = context;
    }

    @NonNull
    @Override
    public SessionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.session_item, parent, false);
        SessionAdapter.ViewHolder vh = new SessionAdapter.ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull SessionAdapter.ViewHolder holder, final int position) {


        //collection references for in case a session is deleted
        final CollectionReference scheduleReference = firestore.collection(SCHEDULE);

        holder.sessionHours.setText(mDataset.get(position).get(HOURS).toString());

        SimpleDateFormat hourFormat = new SimpleDateFormat(HOUR_FORMAT);
        final Timestamp timestamp = (Timestamp)mDataset.get(position).get(DATE);
        final Date dateRecord = timestamp.toDate();
        holder.sessionDate.setText(sfd.format(dateRecord) + " at " + hourFormat.format(dateRecord));


        //deletes a session if cancel button is clicked

        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Date today = new Date();

                if(sfd.format(today).equals(sfd.format(dateRecord))){

                    Query todaySchedule = firestore.collection(TODAYSCHEDULE)
                            .whereEqualTo(USERNAME, Login.username)
                            .whereEqualTo(HOURS,mDataset.get(position).get(HOURS))
                            .whereEqualTo(DATE, dateRecord);

                    todaySchedule.get(Source.SERVER)
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                    if(task.isSuccessful()){

                                        for(DocumentSnapshot doc : task.getResult()){

                                            firestore.collection(TODAYSCHEDULE)
                                                    .document(doc.getId())
                                                    .delete();
                                        }
                                    }

                                }
                            });
                }


                Query querySessions = scheduleReference
                        .whereEqualTo(HOURS, mDataset.get(position).get(HOURS))
                        .whereEqualTo(DATE, dateRecord)
                        .whereEqualTo(USERNAME, Login.username);

                querySessions.get(Source.SERVER).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {
                            for (final DocumentSnapshot doc : task.getResult()) {

                                scheduleReference.document(doc.getId()).delete();
                                Toast.makeText(mContext, "Session canceled", Toast.LENGTH_SHORT).show();

                                mDataset.remove(position);
                                notifyDataSetChanged();

                            }

                        } else {
                            Toast.makeText(mContext, "Error deleting session.", Toast.LENGTH_SHORT).show();
                        }

                    }
                });


            }
        });


    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView sessionDate;
        TextView sessionHours;
        TextView dateText;
        TextView hoursText;
        ImageButton button;

        public ViewHolder(View itemView) {
            super(itemView);
            button = itemView.findViewById(R.id.cancelButton);
            dateText = itemView.findViewById(R.id.dateText);
            hoursText = itemView.findViewById(R.id.hoursText);
            sessionDate = itemView.findViewById(R.id.sessionDate);
            sessionHours = itemView.findViewById(R.id.sessionHours);

        }
    }
}
