package com.example.mark.ms;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;

import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

public class CustomDialog extends Dialog {

    private String pickerMode;
    private View v;

    public CustomDialog(@NonNull Context context) {
        super(context);
        //this, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);

    }
    public CustomDialog(@NonNull Context context, View v) {
        super(context,android.R.style.Theme_DeviceDefault_Dialog_Alert);
        this.v = v;


    }

    @Override
    protected void onStart() {
        super.onStart();
        setContentView(v);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
