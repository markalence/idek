package com.example.mark.ms;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.NumberPicker;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class BookingPicker implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    int year, month, day, hour, minute;
    int yearFinal, monthFinal, dayFinal, hourFinal, minuteFinal;
    Context mContext;
    DatePickerDialog mDatePickerDialog;
    TimePickerDialog mTimePickerDialog;
    LayoutInflater mInflater;
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    NumberPicker numberPicker;


    private String[] values;

    {
        values = new String[]{"1", "1.5", "2", "2.5", "3", "3.5", "4", "4.5", "5", "5.5", "6"};
    }


    private String HOURS = "hours";
    private String STUDENTS = "students";
    private String UPCOMING_SESSIONS = "upcomingsessions";
    private String DATE_FORMAT = "yyyyMMdd";
    private String DATE = "date";
    private String SCHEDULE = "schedule";
    private String FIRST_NAME = "firstName";
    private String LAST_NAME = "lastName";
    private String USERNAME = "username";
    private String TODAYSCHEDULE = "todayschedule";

    static HashMap<String, Object> docData = new HashMap<>();
    final SimpleDateFormat fmt = new SimpleDateFormat(DATE_FORMAT);

    BookingPicker(Context context, LayoutInflater inflater) {

        Calendar calendar = Calendar.getInstance();

        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);
        mContext = context;
        mDatePickerDialog = new DatePickerDialog(mContext, BookingPicker.this, year, month, day);
        mTimePickerDialog = new TimePickerDialog(mContext, BookingPicker.this, hour, minute, android.text.format.DateFormat.is24HourFormat(mContext));
        mInflater = inflater;
    }

    public void makeBooking() {

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
        Timestamp timestamp = new Timestamp(date);
        //checks if student has a session on that day - forces them back to the date picker if they do. If they don't, do nothing
        checkDateExists(timestamp);


    }


    public void checkDateExists(final Timestamp timestamp) {

        CollectionReference scheduleReference = firestore.collection(SCHEDULE);

        Query scheduleQuery = scheduleReference
                .whereEqualTo(USERNAME, Login.username);


        final Date calenderDate = timestamp.toDate(); // date being booked

        scheduleQuery.get(Source.SERVER).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                boolean bookingOnSameDay = false;
                if (task.isSuccessful()) {

                    for (DocumentSnapshot doc : task.getResult()) {

                        Timestamp sessionTimestamp = doc.getTimestamp(DATE);
                        Date sessionDate = sessionTimestamp.toDate(); //date being checked

                        if (fmt.format(sessionDate).equals(fmt.format(calenderDate))) {
                            bookingOnSameDay = true;
                        }


                    }

                    if (bookingOnSameDay) {
                        Toast.makeText(mContext, "You already have a session on this day!", Toast.LENGTH_SHORT).show();
                        docData.clear();
                        mDatePickerDialog.dismiss();
                        makeBooking();
                    } else {
                        docData.put(DATE, timestamp);
                        numberPickerInit();
                    }

                }

            }
        });


    }

    public void numberPickerInit() {


        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        View dialogView = mInflater.inflate(R.layout.booking_hours, null);
        builder.setView(dialogView);
        final AlertDialog alertDialog = builder.create();

        Button confirm = (Button) dialogView.findViewById(R.id.hourConfirm);
        Button cancel = (Button) dialogView.findViewById(R.id.hourCancel);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                docData.put(FIRST_NAME, Login.firstName);
                docData.put(LAST_NAME, Login.lastName);
                docData.put(USERNAME, Login.username);

                Date today = new Date();
                Timestamp bookingTimestamp = (Timestamp) docData.get(DATE);
                Date bookingDate = bookingTimestamp.toDate();

                if(fmt.format(today).equals(fmt.format(bookingDate))) {

                    firestore.collection(TODAYSCHEDULE).add(docData);
                }


                firestore.collection(SCHEDULE).add(docData).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {

                        if (task.isSuccessful()) {
                            docData.clear();
                            alertDialog.dismiss();
                        }

                    }
                });


            }
        });


        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        docData.put(HOURS, 1);


        //number picker starts at 1 and increments by 0.5 up until 6
        final NumberPicker numberPicker = (NumberPicker) dialogView.findViewById(R.id.numberPicker);
        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(10);
        numberPicker.setWrapSelectorWheel(false);


        numberPicker.setDisplayedValues(values);

        //when a number is selected, insert it into docData to be written to server

        numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int oldValue, int newValue) {

                docData.put(HOURS, values[newValue]);

            }


        });

        alertDialog.show();

    }


}
