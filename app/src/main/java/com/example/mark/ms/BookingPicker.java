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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class BookingPicker implements  DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener  {

    int year,month,day,hour,minute;
    int yearFinal,monthFinal,dayFinal,hourFinal,minuteFinal;
    Context mContext;
    DatePickerDialog mDatePickerDialog;
    TimePickerDialog mTimePickerDialog;
    LayoutInflater mInflater;
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    String[] values = new String[11];
    NumberPicker numberPicker;


    private String HOURS = "hours";
    private String STUDENTS = "students";
    private String UPCOMING_SESSIONS = "upcomingsessions";
    private String DATE_FORMAT = "yyyyMMdd";
    private String DATE = "date";
    private String SCHEDULE = "schedule";
    private String FIRST_NAME = "firstName";
    private String LAST_NAME = "lastName";

    static HashMap<String,Object> docData = new HashMap<>();

    BookingPicker(Context context,LayoutInflater inflater){

        Calendar calendar = Calendar.getInstance();

        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);
        mContext = context;
        mDatePickerDialog = new DatePickerDialog(mContext, BookingPicker.this,year,month,day);
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
        cal.set(Calendar.SECOND,0);
        cal.set(Calendar.MILLISECOND,0);

        Date date = cal.getTime();
        Timestamp timestamp = new Timestamp(date);
        //checks if student has a session on that day - forces them back to the date picker if they do. If they don't, do nothing
        CheckDateExists(timestamp);


    }


    public void CheckDateExists(final Timestamp timestamp){

        firestore.collection(STUDENTS).document(Login.username).collection(UPCOMING_SESSIONS)
                .get(Source.SERVER)
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                           @Override
                                           public void onComplete(@NonNull Task<QuerySnapshot> task) {



                                               if(task.isSuccessful()){
                                                   System.out.println(task.getResult().getDocuments());
                                                   for(DocumentSnapshot doc : task.getResult()){

                                                       Date date = (Date) doc.get(DATE);
                                                       Date date1 = timestamp.toDate();

                                                       SimpleDateFormat fmt = new SimpleDateFormat(DATE_FORMAT);
                                                       if(fmt.format(date1).equals(fmt.format(date))){
                                                           Toast.makeText(mContext,"You already have a session on this day!", Toast.LENGTH_SHORT).show();
                                                           docData.clear();
                                                           makeBooking();
                                                           break;
                                                       }

                                                   }

                                                   docData.put(DATE,timestamp);
                                                   numberPickerInit();

                                               }

                                           }
                                       }
                );

    }

    public void numberPickerInit(){


        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        LayoutInflater inflater = mInflater;
        View dialogView = mInflater.inflate(R.layout.booking_hours, null);
        builder.setView(dialogView);
        final AlertDialog alertDialog = builder.create();

        Button confirm = (Button)dialogView.findViewById(R.id.hourConfirm);
        Button cancel = (Button)dialogView.findViewById(R.id.hourCancel);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firestore.collection(STUDENTS).document(Login.username).collection(UPCOMING_SESSIONS).add(docData);
                docData.put(FIRST_NAME, Login.firstName);
                docData.put(LAST_NAME, Login.lastName);
                firestore.collection(SCHEDULE).add(docData);
                docData.clear();
                alertDialog.dismiss();}
        });


        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        docData.put(HOURS,1);


        //number picker starts at 1 and increments by 0.5 up until 6
        final NumberPicker numberPicker = (NumberPicker) dialogView.findViewById(R.id.numberPicker);
        numberPicker.setMaxValue(10);
        numberPicker.setMinValue(0);
        numberPicker.setWrapSelectorWheel(false);


        //filling the number picker with the correct values
        int pos = 0;

        for(double i = 1;i<=6;i+=0.5){

            values[pos]=String.valueOf(i).replace(".0","");
            ++pos;

        }

        numberPicker.setDisplayedValues(values);

        //when a number is selected, insert it into docData to be written to server

        numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int oldValue, int newValue) {

                if(docData.containsKey(HOURS)){

                    docData.remove(HOURS);
                    docData.put(HOURS,values[newValue]);

                }

                else {docData.put(HOURS,values[newValue]);}
            }


        });

    alertDialog.show();

    }


}
