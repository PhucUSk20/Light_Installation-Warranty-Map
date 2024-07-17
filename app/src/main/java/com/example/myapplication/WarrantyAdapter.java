package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Tách warrantyData.getLightId() thành Giatri1 và Giatri2
                String lightId = warrantyData.getLightId();
                String[] parts = lightId.split("-");
                if (parts.length != 2) {
                    Log.e("WarrantyAdapter", "Invalid lightId format: " + lightId);
                    return;
                }
                String Giatri1 = parts[0];
                String Giatri2 = parts[1];

                // Query Firebase để lấy location và error từ Giatri1 và Giatri2
                DatabaseReference locationRef = FirebaseDatabase.getInstance().getReference("Location");
                locationRef.orderByValue().equalTo(Giatri1).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String locationKey = snapshot.getKey();
                            DatabaseReference lightRef = FirebaseDatabase.getInstance().getReference("LightStreet").child(locationKey).child(Giatri2);
                            lightRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    String location = dataSnapshot.child("location").getValue(String.class);
                                    String errorValue2 = dataSnapshot.child("error").getValue(String.class);
                                    int errorValue = Integer.parseInt(errorValue2);
                                    String error = errorValue > 0 ? "error" : "noerror";
                                    if (location != null && errorValue2 != null) {
                                        // Chuyển dữ liệu sang Map Activity
                                        Intent intent = new Intent(context, Map.class);
                                        intent.putExtra("LOCATION", location);
                                        intent.putExtra("error",error); // Chuyển error thành kiểu int
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        context.startActivity(intent);
                                    } else {
                                        Log.e("WarrantyAdapter", "Location or error not found for Giatri1: " + Giatri1 + " and Giatri2: " + Giatri2);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Log.e("Firebase", "Error fetching Light data", databaseError.toException());
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("Firebase", "Error fetching Location data", databaseError.toException());
                    }
                });
            }
        });


        return convertView;
    }


}
