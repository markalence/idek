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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class SessionAdapter extends RecyclerView.Adapter<SessionAdapter.ViewHolder> {

    ArrayList<HashMap<String,Object>> mDataset;
    Context mContext;
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public SessionAdapter(Context context, ArrayList<HashMap<String,Object>> myDataset) {
        mDataset = myDataset;
        mContext=context;
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
        final CollectionReference collectionReference= firestore.collection("students").document("grahams").
                collection("upcomingsessions");

        holder.sessionHours.setText(mDataset.get(position).get("hours").toString());
        SimpleDateFormat sfd = new SimpleDateFormat("EEEE, MMMM dd");
        SimpleDateFormat hourFormat = new SimpleDateFormat(("kk:mm"));
        Date dateRecord = (Date) mDataset.get(position).get("date");
        final Timestamp ts = new Timestamp(dateRecord);

        holder.sessionDate.setText(sfd.format(dateRecord).toString() + " at " + hourFormat.format(dateRecord));


        //deletes a session if cancel button is clicked

        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext,"Session canceled",Toast.LENGTH_SHORT).show();

                System.out.println(mDataset.get(position).get("hours") + "       " + mDataset.get(position).get("date"));



               Query query = collectionReference.whereEqualTo("hours",mDataset.get(position).get("hours"))
                       .whereEqualTo("date",ts);

               query.get(Source.SERVER).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                   @Override
                   public void onComplete(@NonNull Task<QuerySnapshot> task) {

                       if(task.isSuccessful()){

                           for (DocumentSnapshot doc : task.getResult()){

                               collectionReference.document(doc.getId()).delete();
                               mDataset.remove(position);
                               notifyDataSetChanged();

                           }

                       }

                       else {Toast.makeText(mContext,"Shit buddy",Toast.LENGTH_SHORT).show();}

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
