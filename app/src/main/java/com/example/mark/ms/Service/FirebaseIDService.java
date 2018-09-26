package com.example.mark.ms.Service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.media.RingtoneManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.mark.ms.MainActivity;
import com.example.mark.ms.NotificationReceiver;
import com.example.mark.ms.R;
import com.google.common.base.Defaults;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.util.HashMap;
import java.util.Random;

public class FirebaseIDService extends FirebaseMessagingService {

    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    String firstName, lastName, username, grade;
    private SharedPreferences.Editor mEditor;


    @Override
    public void onNewToken(String s) {
        SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mEditor = mSharedPreferences.edit();
        mEditor.putString("deviceToken", s);
        mEditor.commit();

        super.onNewToken(s);
        Log.d("NEW TOKEN: ", s);
    }

    private static int NOTIFICATION_ID = 0;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        final String KEY_TEXT_REPLY = "key_text_reply";

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            RemoteInput remoteInput = new RemoteInput.Builder(KEY_TEXT_REPLY)
                    .setLabel("Module")
                    .build();

            Intent intent = new Intent(this, NotificationReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            Notification.Action action = new Notification.Action.Builder(
                    Icon.createWithResource(this, R.mipmap.ic_launcher_round),
                    "Record Module",
                    pendingIntent)
                    .addRemoteInput(remoteInput)
                    .build();

            Notification notification = new Notification.Builder(getApplicationContext())
                    .setContentTitle("Please fill in your record sheet :)")
                    .setContentText("2 Hours")
                    .setContentIntent(pendingIntent)
                    .setColor(Color.rgb(25, 205, 205))
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .addAction(action)
                    .build();


            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
            notificationManagerCompat.notify(0, notification);

        } else {

            Intent intent = new Intent(this, RecordSheetDialog.class);
            JSONObject j = new JSONObject(remoteMessage.getData());
            intent.putExtra("documentData", j.toString());
            System.out.println(j.toString());
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            if (android.os.Build.VERSION.SDK_INT == android.os.Build.VERSION_CODES.M) {
                System.out.println("THIS IS TRUE");

                Notification.Action action = new Notification.Action.Builder(
                        Icon.createWithResource(this, R.mipmap.ic_launcher_round),
                        "Record Module",
                        pendingIntent)
                        .build();

                Notification notification = new Notification.Builder(getApplicationContext())
                        .setContentTitle("Please fill in your record sheet.")
                        .setContentText(remoteMessage.getData().get("hours") + " hours")
                        .setContentIntent(pendingIntent)
                        .setColor(Color.rgb(25, 205, 205))
                        .setSmallIcon(R.mipmap.ic_launcher_round)
                        .addAction(action)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setPriority(Notification.PRIORITY_HIGH)
                        .build();

                NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
                notificationManagerCompat.notify(1, notification);
            } else {


                Intent mIntent = new Intent(this, RecordSheetDialog.class);
                mIntent.putExtra("documentData", j.toString());
                PendingIntent mPendingIntent = PendingIntent.getActivity(this, 2, mIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                NotificationCompat.Action action1 = new NotificationCompat.Action((R.drawable.ic_menu_send), "Record session", mPendingIntent);

                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, KEY_TEXT_REPLY)
                        .setSmallIcon(R.mipmap.ic_launcher_round)
                        .setContentTitle("Please fill in your record sheet.")
                        .setContentIntent(mPendingIntent)
                        .addAction(action1)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setPriority(NotificationCompat.PRIORITY_HIGH);

                Notification notification = mBuilder.build();

                NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
                notificationManagerCompat.notify(2, notification);
            }
        }

    }
}
//dDZpbYJC1hI:APA91bFuu2zrvSafzBehgr3EK4FifPTLiK9DwEmuDzvOxkIlwmlnWlqUo9EMKboVHcmMlxIaLLdmo2qkc7r1gcLNzbxFPnyO2XwpUoZxBet3pEOjrg1dOYtFbe3wvmBzNGNjViSYpgYa