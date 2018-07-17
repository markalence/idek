package com.example.mark.ms;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class BookingRecorder implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private int year, month, day, hour, minute;
    private int yearFinal, monthFinal, dayFinal, hourFinal, minuteFinal;
    private Context mContext;
    private DatePickerDialog mDatePickerDialog;
    private TimePickerDialog mTimePickerDialog;
    private LayoutInflater mInflater;
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    private String[] values = new String[11];
    private NumberPicker numberPicker;


    private String HOURS = "hours";
    private String STUDENTS = "students";
    private String UPCOMING_SESSIONS = "upcomingsessions";
    private String RECORDED_SESSIONS = "recordedsessions";
    private String FIRST_NAME = "firstName";
    private String LAST_NAME = "lastName";
    private String MODULE = "module";
    private String USERNAME = "username";
    private String PENDING_RECORDSHEETS = "pendingrecordsheets";
    private String DATE_FORMAT = "yyyyMMdd";
    private HashMap<String, Object> docData = new HashMap<>();

    BookingRecorder(Context context, LayoutInflater inflater) {

        Calendar calendar = Calendar.getInstance();

        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        mContext = context;
        mDatePickerDialog = new DatePickerDialog(mContext, BookingRecorder.this, year, month, day);
        mTimePickerDialog = new TimePickerDialog(mContext, BookingRecorder.this, hour, minute, android.text.format.DateFormat.is24HourFormat(mContext));
        mInflater = inflater;
        docData.put(HOURS,1);
    }

    public void recordBooking() {

        mDatePickerDialog.show();

    }


    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

        yearFinal = year;
        monthFinal = month;
        dayFinal = dayOfMonth;
        Calendar calendar = Calendar.getInstance();
        hour = calendar.get(Calendar.HOUR_OF_DAY);

        mTimePickerDialog.show();


    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

        hourFinal = hourOfDay;
        minuteFinal = minute;

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, yearFinal);
        cal.set(Calendar.MONTH, monthFinal);
        cal.set(Calendar.DAY_OF_MONTH, dayFinal);
        cal.set(Calendar.HOUR_OF_DAY, hourFinal);
        cal.set(Calendar.MINUTE, minuteFinal);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        Date date = cal.getTime();
        Timestamp ts = new Timestamp(date);
        //checks if student has a session on that day - forces them back to the date picker if they do. If they don't, do nothing
        dateExists(ts);
        docData.put("date", ts);
        numberPickerInit();


    }


    public void dateExists(final Timestamp timestamp) {

        firestore.collection(STUDENTS).document(Login.username).collection(RECORDED_SESSIONS)
                .get(Source.SERVER)
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                           @Override
                                           public void onComplete(@NonNull Task<QuerySnapshot> task) {


                                               if (task.isSuccessful()) {

                                                   for (DocumentSnapshot doc : task.getResult()) {

                                                       Date date = (Date) doc.get("date");
                                                       Date date1 = timestamp.toDate();

                                                       SimpleDateFormat fmt = new SimpleDateFormat(DATE_FORMAT);
                                                       if (fmt.format(date1).equals(fmt.format(date))) {


                                                           Toast.makeText(mContext, "You already recorded a session on this day!", Toast.LENGTH_SHORT).show();
                                                           mTimePickerDialog.dismiss();
                                                           docData.clear();
                                                           recordBooking();
                                                           break;
                                                       }

                                                   }

                                               }

                                           }
                                       }
                );

    }

    public void numberPickerInit() {


        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        View dialogView = mInflater.inflate(R.layout.booking_hours, null);
        builder.setView(dialogView);
        setNumberPicker(dialogView);
        final AlertDialog hourPicker = builder.create();
        hourPicker.show();

        final Button hourCancel = dialogView.findViewById(R.id.hourCancel);
        Button hourConfirm = dialogView.findViewById(R.id.hourConfirm);

        hourCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hourPicker.dismiss();
            }
        });

        hourConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                hourPicker.dismiss();
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                View moduleView = mInflater.inflate(R.layout.module_recorder, null);
                builder.setView(moduleView);
                final EditText et = (EditText) moduleView.findViewById(R.id.moduleRecord);
                Button moduleCancel = (Button) moduleView.findViewById(R.id.moduleCancel);
                Button moduleConfirm = (Button) moduleView.findViewById(R.id.moduleConfirm);
                final AlertDialog moduleDialog = builder.create();


                moduleCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        moduleDialog.dismiss();

                    }
                });

                moduleConfirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        firestore.collection(STUDENTS).document(Login.username).collection(RECORDED_SESSIONS).add(docData);
                        docData.put(FIRST_NAME, Login.firstName);
                        docData.put(LAST_NAME, Login.lastName);
                        docData.put(MODULE, et.getText().toString());
                        docData.put(USERNAME,Login.username);
                        firestore.collection(PENDING_RECORDSHEETS).add(docData);
                        moduleDialog.dismiss();
                        Toast.makeText(mContext, "Thank you! Session has been recorded.", Toast.LENGTH_SHORT).show();
                        moduleDialog.dismiss();

                    }
                });

                moduleDialog.show();
            }
        });





    }

    public void setNumberPicker(View dialogView) {

        //number picker starts at 1 and increments by 0.5 up until 6
        final NumberPicker numberPicker = (NumberPicker) dialogView.findViewById(R.id.numberPicker);
        numberPicker.setMaxValue(10);
        numberPicker.setMinValue(0);
        numberPicker.setWrapSelectorWheel(false);


        //filling the number picker with the correct values
        int pos = 0;

        for (double i = 1; i <= 6; i += 0.5) {

            values[pos] = String.valueOf(i).replace(".0", "");
            ++pos;

        }

        numberPicker.setDisplayedValues(values);

        //when a number is selected, insert it into docData to be written to server

        numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int oldValue, int newValue) {

                if (docData.containsKey(HOURS)) {

                    docData.remove(HOURS);
                    docData.put(HOURS, values[newValue]);

                } else {
                    docData.put(HOURS, values[newValue]);
                }
            }


        });


    }


}
