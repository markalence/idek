package com.example.mark.ms;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.annotation.Nullable;

public class SessionActivity extends AppCompatActivity {

    FirebaseFirestore firestore;
    private ArrayList<HashMap<String, Object>> sessionItems;
    RecyclerView recyclerView;

    private String STUDENTS = "students";
    private String HOURS = "hours";
    private String DATE = "date";
    private String SCHEDULE = "schedule";
    private String USERNAME = "username";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session);

        sessionItems = new ArrayList<>();
        firestore = FirebaseFirestore.getInstance();

        toolbarInit();
        getData();
        recyclerViewInit();
    }

    public void getData() {

        Query upcomingSessions = firestore.collection(SCHEDULE)
                .whereEqualTo(USERNAME, Login.username)
                .orderBy(DATE, Query.Direction.DESCENDING);

        upcomingSessions.get(Source.SERVER)
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {

                            for (DocumentSnapshot doc : task.getResult()) {

                                System.out.println(doc.getData());
                                HashMap<String, Object> map = new HashMap<String, Object>();
                                map.put(HOURS, doc.get(HOURS));
                                map.put(DATE, doc.getTimestamp(DATE));
                                sessionItems.add(map);

                            }

                            recyclerViewInit();

                        } else {

                            Log.d("ERRORMAKEMESAD", task.getException().toString());
                            Toast.makeText(getBaseContext(), "Error. Please tell your manager.", Toast.LENGTH_SHORT).show();
                        }

                    }
                });

    }


    public void toolbarInit() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {

            finish();

        }

        return super.onOptionsItemSelected(item);
    }

    public void recyclerViewInit() {
        recyclerView = findViewById(R.id.sessionSheet);
        SessionAdapter sa = new SessionAdapter(this, sessionItems);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(sa);
        sa.notifyDataSetChanged();

    }


}
