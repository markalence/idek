package com.example.mark.ms;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;

import com.example.mark.ms.Service.RecordSheetDialog;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;

public class NotificationReceiver extends BroadcastReceiver {

    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    String firstName, lastName, username, grade;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);

        //if there is some input
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            CharSequence name = remoteInput.getCharSequence("key_text_reply");
            System.out.println(name);
            SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            String userData = mSharedPreferences.getString("userData", "empty");
            HashMap<String, Object> map = new HashMap<>();
            JSONObject j = null;
            try {
                j = new JSONObject(userData);
                Resources r = context.getResources();
                firstName = j.getString(r.getString(R.string.FIRST_NAME));
                lastName = j.getString(r.getString(R.string.LAST_NAME));
                grade = j.getString(r.getString(R.string.GRADE));
                username = j.getString(r.getString(R.string.USERNAME));
            } catch (JSONException e) {
                e.printStackTrace();
            }


            map.put("module", (String) name);
            map.put("username", username);
            map.put("lastName", lastName);
            map.put("grade", grade);
            map.put("hours", 2);
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 12);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            map.put("date", calendar.getTime());
            firestore.collection("test").add(map);
            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(0);

        } else {

            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(1);
            Intent nIntent = new Intent(context, RecordSheetDialog.class);
            nIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            nIntent.putExtra("documentData", intent.getExtras().get("documentData").toString());
            context.startActivity(nIntent);

        }

    }
}
