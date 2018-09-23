package com.example.mark.ms;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
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
    public static ArrayList<HashMap<String, String>> userDays;
    public static ArrayList<HashMap<String, String>> userContacts;
    public static ArrayList<HashMap<String, Object>> recordSheet;
    public static ArrayList<HashMap<String, Object>> upcomingSessions;
    private boolean sessionsLoaded = false;
    private boolean recordSheetLoaded = false;
    private boolean daysLoaded = false;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;
    private Resources r;

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


        r = getBaseContext().getResources();
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
                firstName = j.getString(r.getString(R.string.FIRST_NAME));
                lastName = j.getString(r.getString(R.string.LAST_NAME));
                grade = j.getString(r.getString(R.string.GRADE));
                username = j.getString(r.getString(R.string.USERNAME));
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
                EditText usernameText = findViewById(R.id.username);
                EditText passwordText = findViewById(R.id.password);
                username = (usernameText.getText()).toString();
                password = ((Character) username.charAt(username.length() - 1)).toString();
                setContentView(R.layout.login_loading);

                if (password.equals(password)) {

                    DocumentReference docRef = db.collection(r.getString(R.string.STUDENTS)).document(username);
                    docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    firstName = document.get(r.getString(R.string.FIRST_NAME)).toString();
                                    lastName = document.get(r.getString(R.string.LAST_NAME)).toString();
                                    grade = document.get(r.getString(R.string.GRADE)).toString();

                                    JSONObject j = new JSONObject();
                                    try {
                                        j.put(r.getString(R.string.FIRST_NAME), firstName);
                                        j.put(r.getString(R.string.LAST_NAME), lastName);
                                        j.put(r.getString(R.string.GRADE), grade);
                                        j.put(r.getString(R.string.USERNAME), username);
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
        userDays = new ArrayList<>();

        db.collection(r.getString(R.string.RECORDSHEETS))
                .whereEqualTo(r.getString(R.string.USERNAME), username)
                .orderBy(r.getString(R.string.DATE), Query.Direction.DESCENDING)
                .get(Source.SERVER)
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot doc : task.getResult()) {
                                recordSheet.add((HashMap<String, Object>) doc.getData());
                            }
                            recordSheetLoaded = true;
                            if (sessionsLoaded && recordSheetLoaded && daysLoaded) {
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

        db.collection(r.getString(R.string.SCHEDULE))
                .whereEqualTo(r.getString(R.string.USERNAME), Login.username)
                .orderBy(r.getString(R.string.DATE), Query.Direction.ASCENDING)
                .get(Source.SERVER).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {

                    for (DocumentSnapshot doc : task.getResult()) {
                        HashMap<String,Object> info = (HashMap<String, Object>) doc.getData();
                        info.put("id",doc.getId());
                        upcomingSessions.add(info);
                    }
                    sessionsLoaded = true;
                    if (sessionsLoaded && recordSheetLoaded && daysLoaded) {
                        Intent intent = new Intent(Login.this, MainActivity.class);
                        finish();
                        startActivity(intent);
                    }
                } else {
                    Toast.makeText(getBaseContext(), "Could not connect", Toast.LENGTH_SHORT).show();
                    System.out.println(task.getException());
                }
            }
        });

        db.collection(r.getString(R.string.STUDENTS))
                .document(username)
                .get(Source.SERVER)
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            userDays = new ArrayList<>();
                            for(HashMap map : (ArrayList<HashMap<String,String>>)task.getResult().get("days")){
                                userDays.add((HashMap<String, String>)map);
                            }

                            userContacts = (ArrayList<HashMap<String, String>>) task.getResult().get("contacts");

                            daysLoaded = true;
                            if (sessionsLoaded && recordSheetLoaded && daysLoaded) {
                                Intent intent = new Intent(Login.this, MainActivity.class);
                                finish();
                                startActivity(intent);
                            }
                        }
                    }
                });
    }
}
