package com.example.mark.ms;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class Login extends AppCompatActivity {

    public static String username;
    public static String firstName;
    public static String lastName;
    public static String password;
    public static String grade;
    private static String FIRST_NAME = "firstName";
    private static String LAST_NAME = "lastName";
    private static String STUDENTS = "students";
    private static String GRADE = "grade";

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    CollectionReference cities = db.collection("students");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Button button = (Button)findViewById(R.id.loginbutton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText usernameText = (EditText) findViewById(R.id.username);
                EditText passwordText = (EditText) findViewById(R.id.password);
                String passwordEntry = passwordText.getText().toString();
                username = (usernameText.getText()).toString();
                password = ((Character) username.charAt(username.length() - 1)).toString();

                if (password.equals(password)) {

                    DocumentReference docRef = db.collection(STUDENTS).document(username);
                    docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        public static final String TAG = "test";

                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                    firstName = document.get(FIRST_NAME).toString();
                                    lastName = document.get(LAST_NAME).toString();
                                    grade = document.get(GRADE).toString();
                                    Toast.makeText(getBaseContext(),"Hi, " + document.get(FIRST_NAME), Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(Login.this,MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Log.d(TAG, "No such document");
                                }
                            } else {
                                Log.d(TAG, "get failed with ", task.getException());
                            }
                        }
                    });


                }

            }
        });

    }


}
