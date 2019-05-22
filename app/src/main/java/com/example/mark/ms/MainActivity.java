package com.example.mark.ms;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.provider.SyncStateContract;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mark.ms.Service.MyNotificationManager;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.picasso.Picasso;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    static View view;
    private Resources r;
    private TextView nameView;
    private TextView gradeView;
    private boolean exit = false;
    private RecordSheetAdapter rsa;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        makeNavLayout();
        setRecyclerView();
        view = findViewById(android.R.id.content);
        r = getBaseContext().getResources();
        System.out.println(FirebaseInstanceId.getInstance().getId() + "     ID");

        //rara

        /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel("scaance", "some ass", importance);
            mChannel.setDescription("good app");
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            mNotificationManager.createNotificationChannel(mChannel);
        }

        *//*
         * Displaying a notification locally
         *//*
        MyNotificationManager.getInstance(this).displayNotification("Greetings", "Hello how are you?");*/


    }

    public void setRecyclerView() {
        recyclerView = findViewById(R.id.recordSheet);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        rsa = new RecordSheetAdapter(this, Login.recordSheet);
        recyclerView.setAdapter(rsa);
        System.out.println((recyclerView.getAdapter().getItemCount()));
    }

    public void makeNavLayout() {

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        ImageView header = navigationView.getHeaderView(0).findViewById(R.id.imageView);

        nameView = navigationView.getHeaderView(0).findViewById(R.id.name);
        gradeView = navigationView.getHeaderView(0).findViewById(R.id.grade);
        nameView.setText(Login.firstName + " " + Login.lastName);
        gradeView.setText("Grade " + Login.grade);
        Picasso.get().load(R.mipmap.header_background).into(header);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (!exit) {
                exit = true;
                Toast.makeText(getBaseContext(), "Press back again to exit", Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        exit = false;
                    }
                }, 2000);
            } else {

                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.sessions) {
            Intent intent = new Intent(MainActivity.this, SessionActivity.class);
            startActivity(intent);
        } else if (id == R.id.book) {
            BookingPicker bookingPicker = new BookingPicker(this, this.getLayoutInflater());
            bookingPicker.makeBooking();
            //startAnimationFromBackgroundThread();
        } else if (id == R.id.record) {
            BookingRecorder bookingRecorder = new BookingRecorder(this, this.getLayoutInflater());
            bookingRecorder.recordBooking();
        } else if (id == R.id.student_profile) {
            Intent intent = new Intent(MainActivity.this, MyProfileActivity.class);
            startActivity(intent);
        } else if (id == R.id.contact) {

        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void startAnimationFromBackgroundThread() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                // this runs on a background thread
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //dateDialog.show();
                    }
                });
            }
        });
    }

}