package com.example.mark.ms;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.common.SignInButton;

import java.util.ArrayList;
import java.util.HashMap;

public class DayAdapter extends RecyclerView.Adapter<DayAdapter.ViewHolder> {

    Context mContext;
    LayoutInflater mInflater;
    NumberPicker hourPicker;
    NumberPicker timePicker;
    ArrayList<HashMap<String, String>> copyDays;
    Resources r;

    DayAdapter(Context context, LayoutInflater inflater) {
        mContext = context;
        mInflater = inflater;
        r = mContext.getResources();
        copyDays = (ArrayList<HashMap<String, String>>) Login.userDays.clone();
    }

    @NonNull
    @Override
    public DayAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.day_item, parent, false);
        DayAdapter.ViewHolder vh = new DayAdapter.ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull DayAdapter.ViewHolder holder, int position) {

        holder.setItem(position);

    }

    @Override
    public int getItemCount() {
        return Login.userDays.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView dayData;
        ImageButton deleteDay;
        ImageButton editDay;

        public ViewHolder(View itemView) {
            super(itemView);
            dayData = itemView.findViewById(R.id.dayItemText);
            editDay = itemView.findViewById(R.id.dayEditImage);
            deleteDay = itemView.findViewById(R.id.dayCancelImage);


        }

        @SuppressLint("SetTextI18n")
        public void setItem(int position) {
            dayData.setText("Day: " + Login.userDays.get(position).get("day")
                    + "\n" + "Time: " + Login.userDays.get(position).get("time")
                    + "\n" + "Hours: " + Login.userDays.get(position).get("hours"));

            handleButtons(position);

        }

        private void handleButtons(final int position) {

            final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

            editDay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View t) {

                    copyDays.get(position).put(r.getString(R.string.HOURS), "1");
                    copyDays.get(position).put("time", "14:00");
                    numberPickerInit(position);

                }
            });

            deleteDay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View t) {

                    View v = mInflater.inflate(R.layout.day_cancel_dialog, null);
                    Button cancel = v.findViewById(R.id.dialogCancel);
                    Button confirm = v.findViewById(R.id.dialogConfirm);
                    builder.setView(v);
                    final AlertDialog alertDialog = builder.create();

                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.dismiss();
                        }
                    });

                    confirm.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            System.out.println("NACE");
                            alertDialog.dismiss();
                        }
                    });

                    alertDialog.show();

                }
            });


        }

        public void numberPickerInit(final int position) {

            final View pickerView = mInflater.inflate(R.layout.booking_hours, null);
            final AlertDialog.Builder timeBuilder = new AlertDialog.Builder(mContext);
            timeBuilder.setView(pickerView);

            final AlertDialog timeDialog = timeBuilder.create();
            final Button timeCancel = pickerView.findViewById(R.id.hourCancel);
            Button timeConfirm = pickerView.findViewById(R.id.hourConfirm);
            timePicker = pickerView.findViewById(R.id.numberPicker);
            timePicker.setDisplayedValues(r.getStringArray(R.array.weekDayTimeArray));
            timePicker.setMinValue(0);
            timePicker.setMaxValue(r.getStringArray(R.array.weekDayTimeArray).length - 1);


            timePicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                @Override
                public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                    copyDays.get(position).put("time", r.getStringArray(R.array.weekDayTimeArray)[newVal]);
                }
            });

            timeCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    copyDays = (ArrayList<HashMap<String, String>>) Login.userDays.clone();
                    timeDialog.dismiss();
                }
            });

            timeConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    View newView = mInflater.inflate(R.layout.booking_hours, null);
                    hourPicker = newView.findViewById(R.id.numberPicker);
                    hourPicker.setDisplayedValues(r.getStringArray(R.array.hourArray));
                    hourPicker.setMaxValue(r.getStringArray(R.array.hourArray).length - 1);
                    hourPicker.setMinValue(0);
                    Button hourCancel = newView.findViewById(R.id.hourCancel);
                    Button hourConfirm = newView.findViewById(R.id.hourConfirm);
                    timeDialog.setContentView(newView);


                    hourPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                        @Override
                        public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                            copyDays.get(position).put(r.getString(R.string.HOURS), r.getStringArray(R.array.hourArray)[newVal]);
                        }
                    });

                    hourCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            copyDays = (ArrayList<HashMap<String, String>>) Login.userDays.clone();
                            timeDialog.dismiss();
                        }
                    });

                    hourConfirm.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Login.userDays = (ArrayList<HashMap<String, String>>) copyDays.clone();
                            notifyItemChanged(position);
                            timeDialog.dismiss();
                        }
                    });
                    timeDialog.show();
                }
            });

            timeDialog.show();
        }


    }
}
