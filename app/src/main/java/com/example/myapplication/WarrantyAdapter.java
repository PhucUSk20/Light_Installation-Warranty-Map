package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class WarrantyAdapter extends BaseAdapter {
    private Context context;
    private List<WarrantyData> warrantyList;

    public WarrantyAdapter(Context context, List<WarrantyData> warrantyList) {
        this.context = context;
        this.warrantyList = warrantyList;
    }

    @Override
    public int getCount() {
        return warrantyList.size();
    }

    @Override
    public Object getItem(int position) {
        return warrantyList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_warranty, parent, false);
        }

        WarrantyData warrantyData = warrantyList.get(position);

        TextView lightIdTextView = convertView.findViewById(R.id.light_id);
        TextView statusTextView = convertView.findViewById(R.id.status);
        TextView timestampTextView = convertView.findViewById(R.id.timestamp);
        ImageView statusIcon = convertView.findViewById(R.id.status_icon);

        lightIdTextView.setText(warrantyData.getLightId());
        statusTextView.setText(warrantyData.getStatus());
        timestampTextView.setText(warrantyData.getTimestamp());

        if ("Processing".equals(warrantyData.getStatus())) {
            statusIcon.setImageResource(R.drawable.ic_processing);
            Animation rotateAnimation = AnimationUtils.loadAnimation(context, R.anim.progress_rotate);
            statusIcon.startAnimation(rotateAnimation);
        } else if ("Complete".equals(warrantyData.getStatus())) {
            statusIcon.setImageResource(R.drawable.ic_complete);
            statusIcon.clearAnimation();
        }

        return convertView;
    }
}
