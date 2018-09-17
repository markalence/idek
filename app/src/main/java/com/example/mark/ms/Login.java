package com.example.mark.ms;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.github.ybq.android.spinkit.style.WanderingCubes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

public class Login extends AppCompatActivity {

    public static String username;
    public static String firstName;
    public static String lastName;
    public static String password;
    public static String grade;
    private String FIRST_NAME = "firstName";
    private String LAST_NAME = "lastName";
    private String STUDENTS = "students";
    private String GRADE = "grade";
    private String USERNAME = "username";
    private String DATE = "date";
    private String RECORD_SHEETS = "recordsheets";
    private String SCHEDULE = "schedule";
    private boolean sessionsLoaded = false;
    private boolean recordSheetLoaded = false;
    public static ArrayList<HashMap<String, Object>> recordSheet;
    public static ArrayList<HashMap<String, Object>> upcomingSessions;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;
    public static Dialog dateDialog;
    public static View dateDialogView;
    public static ConstraintLayout cl;


    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .setPersistenceEnabled(false)
                .build();
        db.setFirestoreSettings(settings);


        setContentView(R.layout.login_loading);
        ProgressBar progressBar = findViewById(R.id.progress);
        WanderingCubes wc = new WanderingCubes();
        progressBar.setIndeterminateDrawable(wc);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mEditor = mSharedPreferences.edit();
        String userData = mSharedPreferences.getString("userData", "empty");

        if (userData.equals("empty")) {
            getLoginInfo();

        } else {

            try {
                JSONObject j = new JSONObject(userData);
                firstName = j.getString(FIRST_NAME);
                lastName = j.getString(LAST_NAME);
                grade = j.getString(GRADE);
                username = j.getString(USERNAME);
                getData();
            } catch (JSONException e) {
                e.printStackTrace();
                getLoginInfo();
            }
        }

    }

    public void getLoginInfo() {

        setContentView(R.layout.activity_login);
        Button button = (Button) findViewById(R.id.loginbutton);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText usernameText = (EditText) findViewById(R.id.username);
                EditText passwordText = (EditText) findViewById(R.id.password);
                username = (usernameText.getText()).toString();
                password = ((Character) username.charAt(username.length() - 1)).toString();

                if (password.equals(password)) {

                    DocumentReference docRef = db.collection(STUDENTS).document(username);
                    docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    firstName = document.get(FIRST_NAME).toString();
                                    lastName = document.get(LAST_NAME).toString();
                                    grade = document.get(GRADE).toString();

                                    JSONObject j = new JSONObject();
                                    try {
                                        j.put(FIRST_NAME, firstName);
                                        j.put(LAST_NAME, lastName);
                                        j.put(GRADE, grade);
                                        j.put(USERNAME, username);
                                        mEditor.putString("userData", j.toString());
                                        mEditor.commit();
                                        getData();

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                } else {
                                }
                            } else {
                            }
                        }
                    });
                }
            }
        });
    }

    public void getData() {

        recordSheet = new ArrayList<>();
        upcomingSessions = new ArrayList<>();

        db.collection(RECORD_SHEETS)
                .whereEqualTo(USERNAME, username)
                .orderBy(DATE, Query.Direction.DESCENDING)
                .get(Source.SERVER)
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot doc : task.getResult()) {
                                recordSheet.add((HashMap<String, Object>) doc.getData());
                            }
                            recordSheetLoaded = true;
                            if (sessionsLoaded && recordSheetLoaded) {
                                finish();
                                Intent intent = new Intent(Login.this, MainActivity.class);
                                startActivity(intent);

                            }
                        } else {
                            Log.d("BAD", task.getException().toString());
                            Toast.makeText(getBaseContext(), "Couldn't connect", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        db.collection(SCHEDULE)
                .whereEqualTo(USERNAME, Login.username)
                .orderBy(DATE, Query.Direction.ASCENDING)
                .get(Source.SERVER).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {

                    for (DocumentSnapshot doc : task.getResult()) {
                        upcomingSessions.add((HashMap<String, Object>) doc.getData());
                    }
                    sessionsLoaded = true;
                    if (sessionsLoaded && recordSheetLoaded) {
                        Intent intent = new Intent(Login.this, MainActivity.class);
                        finish();
                        startActivity(intent);
                        System.out.println("SCHEDULE SUCCESS  " + upcomingSessions);

                    }
                } else {
                    Toast.makeText(getBaseContext(), "Could not connect", Toast.LENGTH_SHORT).show();
                    System.out.println(task.getException());
                }
            }
        });

    }
}
