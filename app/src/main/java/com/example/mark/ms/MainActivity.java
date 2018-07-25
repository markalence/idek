package com.example.mark.ms;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.text.format.DateFormat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Document;

import java.io.IOException;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.Inflater;

import javax.annotation.Nullable;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    static TextView date;
    static TextView hours;
    static TextView module;
    static TextView comment;
    TextView nameView;
    TextView gradeView;

    private String HOURS = "hours";
    private String USERNAME = "username";
    private String RECORD_SHEETS = "recordsheets";
    private String DATE = "date";
    private String MODULE = "module";
    private String COMMENT = "comment";
    private String DATE_FORMAT = "dd/MM/yy";


    //--------time and date picker info--------
    int day, month, year, hour, minute;
    int dayFinal, monthFinal, hourFinal, yearFinal, minuteFinal;
    String[] values = new String[11];
    Map<String, Object> docData = new HashMap<>();
    NumberPicker numberPicker;
    public static boolean check = false;
    //--------time and date picker info--------


    //--------Adapter info--------
    recordSheetAdapter rsa;
    private RecyclerView recyclerView;
    private ArrayList<HashMap<String, String>> recordItems = new ArrayList<>();
    //--------Adapter info--------

    FirebaseFirestore firestore; //database instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        firestore = FirebaseFirestore.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        makeNavLayout();
        getData();

        date = findViewById(R.id.date);
        hours = findViewById(R.id.hours);
        module = findViewById(R.id.module);
        comment = findViewById(R.id.module);


    }

    //params - none
    //retrieves all record sheet items

    public void getData() {



        firestore.collection(RECORD_SHEETS)
                .whereEqualTo(USERNAME, Login.username)
                .orderBy(DATE, Query.Direction.DESCENDING)
                .get(Source.SERVER)
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if (task.isSuccessful()) {


                    for (DocumentSnapshot doc : task.getResult()) {


                        HashMap<String, String> map = new HashMap<>();
                        map.put(HOURS, doc.get(HOURS).toString());
                        SimpleDateFormat sfd = new SimpleDateFormat(DATE_FORMAT);
                        Date dateRecord = doc.getDate(DATE);
                        map.put(DATE, sfd.format(dateRecord).toString());
                        map.put(MODULE, doc.get(MODULE).toString());
                        map.put(COMMENT, doc.get(COMMENT).toString());
                        recordItems.add(map);


                    }

                    setRecyclerView();
                    rsa.notifyDataSetChanged();

                } else {
                    Log.d("BAD",task.getException().toString());
                    Toast.makeText(getBaseContext(), "Couldn't connect", Toast.LENGTH_SHORT).show();
                }

            }


        });


    }


    //initializes the recycler view
    public void setRecyclerView() {

        System.out.println(findViewById(R.id.recordSheet));
        recyclerView = findViewById(R.id.recordSheet);
        rsa = new recordSheetAdapter(getBaseContext(), recordItems);
        recyclerView.setAdapter(rsa);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


    }

    //initializes the navigation drawer layout

    public void makeNavLayout() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        ImageView header = navigationView.getHeaderView(0).findViewById(R.id.imageView);
        nameView = (TextView) navigationView.getHeaderView(0).findViewById(R.id.name);
        gradeView = (TextView) navigationView.getHeaderView(0).findViewById(R.id.grade);

        nameView.setText(Login.firstName + " " + Login.lastName);
        gradeView.setText("Grade " + Login.grade);


        Picasso.get().load(R.mipmap.header_background).into(header);


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.sessions) {
            Intent intent = new Intent(MainActivity.this, SessionActivity.class);
            startActivity(intent);
        } else if (id == R.id.book) {

            BookingPicker bookingPicker = new BookingPicker(this, this.getLayoutInflater());
            bookingPicker.makeBooking();

        }

        else if(id == R.id.record){

            BookingRecorder bookingRecorder = new BookingRecorder(this,this.getLayoutInflater());
            bookingRecorder.recordBooking();

        }
        else if (id == R.id.student_profile) {

        } else if (id == R.id.contact) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}