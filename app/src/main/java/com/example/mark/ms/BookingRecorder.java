package com.example.mark.ms;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
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

import io.fabric.sdk.android.services.common.ApiKey;

public class BookingRecorder implements OnDateSetListener {


    private int year, month, day;
    private Context mContext;
    private LayoutInflater mInflater;
    private DatePickerDialog mDatePickerDialog;
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private AlertDialog recordDialog;
    private View pickerView;
    private Resources r;
    private SimpleDateFormat sdf = new SimpleDateFormat("yy/mm/dd");


    private HashMap<String, Object> docData = new HashMap<>();

    BookingRecorder(Context context, LayoutInflater inflater) {

        Calendar calendar = Calendar.getInstance();
        mInflater = inflater;
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        mContext = context;
        r = mContext.getResources();
        docData.put(r.getString(R.string.HOURS), "1");

        mDatePickerDialog = new DatePickerDialog(mContext,
                R.style.CustomDialogTheme,
                BookingRecorder.this,
                year, month, day);

        mDatePickerDialog.getDatePicker().setMaxDate(calendar.getTime().getTime());
        calendar.add(Calendar.WEEK_OF_YEAR, -2);
        Date twoWeeksPrior = calendar.getTime();
        mDatePickerDialog.getDatePicker().setMinDate(twoWeeksPrior.getTime());
        mDatePickerDialog.setTitle(null);

        pickerView = mInflater.inflate(R.layout.booking_hours, null);

        View dateTitleView = mInflater.inflate(R.layout.date_picker_title,null);
        mDatePickerDialog.setCustomTitle(dateTitleView);

        final AlertDialog.Builder recordBuilder = new AlertDialog.Builder(mContext);
        setNumberPicker(pickerView);
        recordBuilder.setView(pickerView);
        recordDialog = recordBuilder.create();
    }

    public void recordBooking() {
        mDatePickerDialog.show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

        if (view.isShown()) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.HOUR_OF_DAY, 12);
            calendar.set(Calendar.MILLISECOND, 0);
            Timestamp timestamp = new Timestamp(calendar.getTime());
            checkDateIsValid(timestamp);
        }
    }


    public void checkDateIsValid(final Timestamp timestamp) {

        // TO DO LOAD RECORDED SESSIONS IN LOGIN ACTIVITY
        firestore.collection(r.getString(R.string.RECORDED_SESSIONS))
                .whereEqualTo(r.getString(R.string.USERNAME), Login.username)
                .get(Source.SERVER)
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        boolean bool = false;
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot doc : task.getResult()) {
                                if (sdf.format(doc.getDate(r.getString(R.string.DATE)))
                                        .equals(sdf.format(timestamp.toDate()))) {
                                    mDatePickerDialog.show();
                                    Toast.makeText(mContext, "Session already recorded on this day.", Toast.LENGTH_SHORT).show();
                                    recordDialog.dismiss();
                                    bool = true;
                                }
                            }
                            if (!bool) {
                                docData.put(r.getString(R.string.DATE), timestamp);
                            }
                        }
                    }
                });

        numberPickerInit();
    }

    public void numberPickerInit() {

        final View moduleView = mInflater.inflate(R.layout.record_module, null);
        final Button hourCancel = pickerView.findViewById(R.id.hourCancel);
        final Button hourConfirm = pickerView.findViewById(R.id.hourConfirm);
        final Button moduleCancel = moduleView.findViewById(R.id.moduleCancel);
        final Button moduleConfirm = moduleView.findViewById(R.id.moduleConfirm);
        final EditText et = moduleView.findViewById(R.id.moduleText);

        hourCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordDialog.dismiss();
            }
        });

        hourConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                recordDialog.setContentView(moduleView);
            }
        });

        moduleConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT <= 20) {
                    recordDialog.dismiss();
                }
                docData.put(r.getString(R.string.FIRST_NAME), Login.firstName);
                docData.put(r.getString(R.string.LAST_NAME), Login.lastName);
                docData.put(r.getString(R.string.MODULE), et.getText().toString());
                docData.put(r.getString(R.string.USERNAME), Login.username);
                firestore.collection(r.getString(R.string.RECORDED_SESSIONS)).add(docData);
                firestore.collection(r.getString(R.string.PENDING_RECORDSHEETS)).add(docData);
                firestore.collection(r.getString(R.string.SCHEDULE))
                        .whereEqualTo(r.getString(R.string.USERNAME), Login.username)
                        .get(Source.SERVER)
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    updateSchedule();
                                }
                            }
                        });
                recordDialog.dismiss();
                Toast.makeText(mContext, "Thank you! Session has been recorded.", Toast.LENGTH_SHORT).show();
            }
        });

        moduleCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordDialog.dismiss();
            }
        });
        recordDialog.show();
    }

    public void setNumberPicker(View dialogView) {

        final NumberPicker numberPicker = dialogView.findViewById(R.id.numberPicker);
        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(10);
        numberPicker.setWrapSelectorWheel(false);
        numberPicker.setDisplayedValues(r.getStringArray(R.array.hourArray));

        numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int oldValue, int newValue) {
                docData.put(r.getString(R.string.HOURS), r.getStringArray(R.array.hourArray)[newValue]);
            }
        });
    }


    public void updateSchedule() {

        final SimpleDateFormat sfd = new SimpleDateFormat("ddMMyy");

        for (int i = 0; i < Login.upcomingSessions.size(); ++i) {

            Timestamp scheduledTimestamp;
            scheduledTimestamp = (Timestamp) Login.
                    upcomingSessions.get(i).get(r.getString(R.string.DATE));
            Date scheduledDate = scheduledTimestamp.toDate();

            Timestamp recordedTimestamp = (Timestamp) docData.get(r.getString(R.string.DATE));
            Date recordedDate = recordedTimestamp.toDate();

            if (sfd.format(scheduledDate).equals(sfd.format(recordedDate))) {
                Login.upcomingSessions.remove(i);
                break;
            }
        }

        Query deleteScheduleItem = firestore.collection(r.getString(R.string.SCHEDULE))
                .whereEqualTo(r.getString(R.string.USERNAME), Login.username);

        deleteScheduleItem.get(Source.SERVER)
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {
                            for (DocumentSnapshot doc : task.getResult()) {
                                Date scheduledDate = doc.getTimestamp(r.getString(R.string.DATE)).toDate();
                                Timestamp recordedTimestamp = (Timestamp) docData.get(r.getString(R.string.DATE));
                                Date recordedDate = recordedTimestamp.toDate();
                                if (sfd.format(scheduledDate).equals(sfd.format(recordedDate))) {
                                    firestore.collection(r.getString(R.string.SCHEDULE)).document(doc.getId()).delete();
                                }
                            }
                        }
                    }
                });
    }
}
