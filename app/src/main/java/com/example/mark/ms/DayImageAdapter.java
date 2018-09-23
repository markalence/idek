package com.example.mark.ms;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.autofill.AutofillValue;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;

import java.util.ArrayList;
import java.util.HashMap;

import static android.view.View.AUTOFILL_TYPE_NONE;
import static android.widget.TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM;

public class DayImageAdapter extends BaseAdapter {

    ArrayList<HashMap<String,String>> mDataset;
    Context mContext;

    DayImageAdapter(ArrayList<HashMap<String,String>> days, Context context){
        mDataset = days;
        mContext = context;
    }

    @Override
    public int getCount() {
        return mDataset.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView textView;
        if(convertView == null){
        textView = new TextView(mContext);
        int width = Resources.getSystem().getDisplayMetrics().widthPixels;
        int heightPixels = Resources.getSystem().getDisplayMetrics().heightPixels;
        textView.setLayoutParams(new GridView.LayoutParams(width/3,heightPixels/4));
        }
        else{
            textView = (TextView) convertView;
        }

        String day = mDataset.get(position).get("day");
        String hours = mDataset.get(position).get("hours");
        String time = mDataset.get(position).get("time");

        if(hours.equals("1")){
            hours += " hour";
        }
        else{
            hours += " hours";
        }

        textView.setText(day + "\n" + time + "\n" + hours);
        textView.setGravity(Gravity.CENTER);
        textView.setBackgroundColor(Color.rgb(46, 64, 83));
        textView.setTextSize((9 *Resources.getSystem().getDisplayMetrics().density));
        textView.setTextColor(Color.WHITE);

        return textView;
    }
}
