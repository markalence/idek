package com.example.mark.ms;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;

public class SessionActivity extends AppCompatActivity {

    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    static ArrayList<HashMap<String, Object>> sessionItems = new ArrayList<>();
    static RecyclerView recyclerView;
    static SessionAdapter sessionAdapter;
    static ImageButton deleteButton;
    static Toolbar toolbar;
    static TextView toolbarTitle;
    SessionSwipeController swipeController;
    static View sessionView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session);
        swipeController =  new SessionSwipeController(this,getLayoutInflater());
        sessionItems.clear();
        sessionView = findViewById(android.R.id.content);
        deleteButton = findViewById(R.id.deleteButton);
        deleteButton.setVisibility(View.INVISIBLE);
        toolbarTitle = findViewById(R.id.toolbarTitle);
        toolbarInit();
        recyclerViewInit();
    }


    public void toolbarInit() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(SessionActivity.this, MainActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    public void recyclerViewInit() {
        recyclerView = findViewById(R.id.sessionSheet);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        sessionAdapter = new SessionAdapter(this, Login.upcomingSessions);
        recyclerView.setAdapter(sessionAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeController);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
