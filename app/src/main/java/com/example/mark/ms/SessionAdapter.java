package com.example.mark.ms;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.renderscript.Sampler;
import android.support.annotation.FontRes;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

public class SessionAdapter extends RecyclerView.Adapter<SessionAdapter.ViewHolder> {

    ArrayList<HashMap<String, Object>> mDataset;
    Context mContext;
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    private String DATE_FORMAT_LONG = "EEEE, MMMM dd";
    private String DATE_FORMAT_SHORT = "dd/MM";
    private String HOUR_FORMAT = "kk:mm";
    private ArrayList<HashMap<String, Object>> selectedItems;
    private ArrayList<HashMap<String, Object>> copyItems;
    private ArrayList<Integer> indexList;
    private ArrayList<HashMap<String,Object>> copyDataset;
    Resources r;
    SimpleDateFormat simpleDateFormatLong = new SimpleDateFormat(DATE_FORMAT_LONG);
    SimpleDateFormat simpleDateFormatShort = new SimpleDateFormat(DATE_FORMAT_SHORT);
    boolean clickable;


    public SessionAdapter(Context context, ArrayList<HashMap<String, Object>> myDataset) {
        mDataset = myDataset;
        mContext = context;
        selectedItems = new ArrayList<>();
        copyItems = new ArrayList<>();
        indexList = new ArrayList<>();
        r = mContext.getResources();

        SessionActivity.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                deleteSessions();
            }
        });
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
    public void onBindViewHolder(@NonNull final SessionAdapter.ViewHolder holder, final int position) {

        holder.side = "front";
        holder.sessionLayout.setBackgroundResource(R.drawable.bottomline);
        SimpleDateFormat hourFormat = new SimpleDateFormat(HOUR_FORMAT);
        final Timestamp timestamp = (Timestamp) mDataset.get(position).get(r.getString(R.string.DATE));
        final Date dateRecord = timestamp.toDate();
        String dateStringLong = simpleDateFormatLong.format(dateRecord) + "\n" + hourFormat.format(dateRecord);
        String hourString = mDataset.get(position).get(r.getString(R.string.HOURS)).toString() + " hours";
        holder.sessionInfo.setText(dateStringLong + " for " + hourString);
        holder.documentId = (String) mDataset.get(position).get("docId");
        final String dateStringShort = simpleDateFormatShort.format(dateRecord);
        showFrontOfDrawable(holder,position,dateStringShort);

        final TextDrawable drawableFront = TextDrawable.builder()
                .beginConfig()
                .fontSize((int)((18 *Resources.getSystem().getDisplayMetrics().density)))
                .endConfig()
                .buildRound(dateStringShort, ColorGenerator.MATERIAL.getRandomColor());
        drawableFront.setPadding(30, 30, 30, 30);

        holder.imageView.setImageDrawable(drawableFront);
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ObjectAnimator oa1 = ObjectAnimator.ofFloat(holder.imageView, "scaleY", 1f, 0f);
                oa1.setDuration(90);
                final ObjectAnimator oa2 = ObjectAnimator.ofFloat(holder.imageView, "scaleY", 0f, 1f);
                oa2.setDuration(90);
                oa1.setInterpolator(new DecelerateInterpolator());
                oa2.setInterpolator(new AccelerateDecelerateInterpolator());

                oa1.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        if (holder.side.equals("front")) {

                            showBackOfDrawable(holder, position);

                        } else {

                            showFrontOfDrawable(holder, position, dateStringShort);

                        }
                        oa2.start();
                    }
                });
                oa1.start();
            }
        });
        //holder.imageView.setPadding(20,20,20,20)

    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView sessionInfo;
        String documentId;
        ConstraintLayout sessionLayout;
        ImageView imageView;
        String side;

        public ViewHolder(View itemView) {
            super(itemView);
            sessionInfo = itemView.findViewById(R.id.scheduleDetails);
            sessionLayout = itemView.findViewById(R.id.sessionLayout);
            imageView = itemView.findViewById(R.id.sessionInfo);
            side = "front";
        }
    }

    public void deleteSessions() {

        copyDataset = (ArrayList<HashMap<String,Object>>) mDataset.clone();
        for (int i = 0; i < selectedItems.size(); ++i) {

            Query deleteQuery = firestore.collection(r.getString(R.string.SCHEDULE))
                    .whereEqualTo(r.getString(R.string.USERNAME), selectedItems.get(i).get(r.getString(R.string.USERNAME)))
                    .whereEqualTo(r.getString(R.string.DATE), selectedItems.get(i).get(r.getString(R.string.DATE)));

            copyItems.add(selectedItems.get(i));
            int index = copyDataset.indexOf(selectedItems.get(i));
            indexList.add(index);
            int a = mDataset.indexOf(selectedItems.get(i));
            mDataset.remove(selectedItems.get(i));
            mDataset.indexOf(selectedItems.get(i));
            notifyItemRemoved(a);

            deleteQuery.get(Source.SERVER)
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {

                                for (int i = 0; i<task.getResult().size();++i){
                                    if(i==0){firestore.collection(r.getString(R.string.SCHEDULE))
                                        .document(task.getResult().getDocuments().get(0).getId())
                                    .delete();}
                                }
                            }
                        }
                    });

        }


        selectedItems.clear();
        SessionActivity.toolbarTitle.setVisibility(View.VISIBLE);
        SessionActivity.toolbar.setBackgroundColor(Color.rgb(25, 205, 205));
        SessionActivity.deleteButton.setVisibility(View.INVISIBLE);
        clickable = true;
        final Snackbar snackbar = Snackbar
                .make(SessionActivity.sessionView, "Session canceled.", Snackbar.LENGTH_LONG);
        snackbar.setAction("UNDO", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (clickable) {

                    clickable = false;
                    selectedItems.clear();
                    ArrayList<Integer> orderedList = (ArrayList<Integer>) indexList.clone();
                    Collections.sort(orderedList);
                    for (int i = 0; i < copyItems.size(); ++i) {
                        firestore.collection(r.getString(R.string.SCHEDULE))
                                .add(copyItems.get(indexList.indexOf(orderedList.get(i))));

                        mDataset.add(orderedList.get(i), copyItems.get(indexList.indexOf(orderedList.get(i))));
                        notifyItemInserted(orderedList.get(i));

                    }
                    indexList.clear();
                    copyItems.clear();

                }
            }
        });

        snackbar.setActionTextColor(Color.rgb(25, 172, 172));
        snackbar.show();
    }

    public void showBackOfDrawable(ViewHolder holder, int position) {

        holder.imageView.setImageResource(R.drawable.ic_check_white_24dp);
        holder.imageView.setBackgroundResource(R.drawable.circle);
        holder.imageView.setPadding(30, 30, 30, 30);
        holder.sessionLayout.setBackgroundColor(Color.rgb(200, 200, 200));
        selectedItems.add(mDataset.get(position));
        holder.side = "back";
        SessionActivity.toolbar.setBackgroundColor(Color.rgb(190, 190, 190));
        SessionActivity.toolbarTitle.setVisibility(View.INVISIBLE);
        SessionActivity.deleteButton.setVisibility(View.VISIBLE);
        SessionActivity.deleteButton.setFocusable(true);


    }

    public void showFrontOfDrawable(ViewHolder holder, int position, String dateStringShort) {

        final TextDrawable drawableFront = TextDrawable.builder()
                .beginConfig()
                .fontSize((int)((18 *Resources.getSystem().getDisplayMetrics().density)))
                .endConfig()
                .buildRound(dateStringShort, ColorGenerator.MATERIAL.getRandomColor());
        drawableFront.setPadding(30, 30, 30, 30);

        holder.sessionLayout.setBackgroundResource(R.drawable.bottomline);

        holder.imageView.setImageDrawable(drawableFront);
        holder.imageView.setBackgroundColor(Color.TRANSPARENT);
        holder.imageView.setPadding(0, 0, 0, 0);
        holder.side = "front";
        if (!selectedItems.isEmpty()) {
            selectedItems.remove(mDataset.get(position));
        }
        if (selectedItems.isEmpty()) {
            SessionActivity.toolbarTitle.setVisibility(View.VISIBLE);
            SessionActivity.toolbar.setBackgroundColor(Color.rgb(25, 205, 205));
            SessionActivity.deleteButton.setVisibility(View.INVISIBLE);
        }
    }
}
