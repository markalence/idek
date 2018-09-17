package com.example.mark.ms;

import android.content.res.Resources;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

import java.util.HashMap;

import static android.support.v7.widget.helper.ItemTouchHelper.*;

public class SessionSwipeController extends ItemTouchHelper.Callback {

    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private boolean clickable;

    @Override
    public int getMovementFlags(RecyclerView recyclerView, final RecyclerView.ViewHolder viewHolder) {
        return makeMovementFlags(0, RIGHT);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

        clickable = true;

        final SessionAdapter sessionAdapter = (SessionAdapter) SessionActivity.recyclerView.getAdapter();
        final int copyPosition = viewHolder.getAdapterPosition();
        final HashMap<String, Object> copyMap = sessionAdapter.mDataset.get(copyPosition);

        final Snackbar snackbar = Snackbar
                .make(SessionActivity.sessionView, "Session canceled.", Snackbar.LENGTH_LONG);
        snackbar.setAction("UNDO", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (clickable) {
                    sessionAdapter.mDataset.add(copyPosition, copyMap);
                    sessionAdapter.notifyItemInserted(copyPosition);
                    clickable = false;
                    firestore.collection("schedule")
                            .add(copyMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if (task.isSuccessful()) {

                            } else {
                                Toast.makeText(sessionAdapter.mContext, "Couldn't re-add session at this time", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
        snackbar.setActionTextColor(Color.rgb(25, 172, 172));

        if (viewHolder.getAdapterPosition() != -1) {
            sessionAdapter.mDataset.remove(copyPosition);
            sessionAdapter.notifyItemRemoved(copyPosition);

            Query query = firestore.collection("schedule")
                    .whereEqualTo("username",copyMap.get("username"))
                    .whereEqualTo("date",copyMap.get("date"));

           query.get(Source.SERVER).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
               @Override
               public void onComplete(@NonNull Task<QuerySnapshot> task) {
                   if (task.isSuccessful()) {

                       for (int i = 0; i<task.getResult().size();++i){
                           if(i==0){firestore.collection("schedule")
                                   .document(task.getResult().getDocuments().get(0).getId())
                                   .delete();}
                       }
                   }
               }
           });

            snackbar.show();
        }
    }
}
