package com.example.myapplication;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MyExpandableListAdapter extends BaseExpandableListAdapter {

    private List<String> listGroupTitles;
    private java.util.Map<String, List<LightData>> listData; // Change to store LightData objects
    private Context context;

    public MyExpandableListAdapter(Context context) {
        this.context = context;
    }

    public void setData(List<String> listGroupTitles, java.util.Map<String, List<LightData>> listData) {
        this.listGroupTitles = listGroupTitles;
        this.listData = listData;
    }

    @Override
    public int getGroupCount() {
        return listGroupTitles.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return listData.get(listGroupTitles.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return listGroupTitles.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return listData.get(listGroupTitles.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String groupTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_expandable_list_item_1, parent, false);
        }
        TextView textView = convertView.findViewById(android.R.id.text1);
        textView.setText(groupTitle);

        boolean hasError1 = false;
        boolean hasError2 = false;
        List<LightData> children = listData.get(listGroupTitles.get(groupPosition));
        if (children != null) {
            for (LightData child : children) {
                if (child.getError() == 1) {
                    hasError1 = true;
                    break;
                } else if (child.getError() == 2) {
                    hasError2 = true;
                }
            }
        }

        if (hasError1) {
            convertView.setBackgroundColor(parent.getContext().getResources().getColor(android.R.color.holo_red_light)); // Màu nền đỏ
        } else if (hasError2) {
            convertView.setBackgroundColor(parent.getContext().getResources().getColor(android.R.color.holo_orange_light)); // Màu nền vàng
        } else {
            convertView.setBackgroundColor(parent.getContext().getResources().getColor(android.R.color.background_light)); // Màu nền mặc định
        }

        return convertView;
    }


    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        LightData lightData = (LightData) getChild(groupPosition, childPosition);
        String groupTitle = (String) getGroup(groupPosition);
        String error = lightData.getError() > 0 ? "error" : "noerror";

        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        }
        TextView textView = convertView.findViewById(android.R.id.text1);
        textView.setText("          Đèn "+lightData.toString());
        if (lightData.getError() == 1) {
            convertView.setBackgroundColor(parent.getContext().getResources().getColor(android.R.color.holo_red_light)); // Màu nền đỏ
        } else if (lightData.getError() == 2) {
            convertView.setBackgroundColor(parent.getContext().getResources().getColor(android.R.color.holo_orange_light)); // Màu nền vàng
        } else {
            convertView.setBackgroundColor(parent.getContext().getResources().getColor(android.R.color.background_light)); // Màu nền mặc định
        }
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (lightData.getError() == 1) {
                    // Xây dựng Dialog xác nhận
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Xác nhận sửa đèn");
                    builder.setMessage("Đèn này đang bị hỏng, bạn có muốn nhận nhiệm vụ sửa đèn này không?");
                    builder.setPositiveButton("Có", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // Xử lý khi người dùng chọn Có
                            // Ví dụ: gửi yêu cầu sửa đèn đến hệ thống
                            handleFixLightTask(groupTitle,lightData);
                        }
                    });
                    builder.setNegativeButton("Không", null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    String location = lightData.getLocation();
                    if (isValidLocationFormat(location)) {
                        Intent intent = new Intent(context, Map.class);
                        intent.putExtra("LOCATION", location);
                        intent.putExtra("error", error);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    } else {
                        Log.e("MyExpandableListAdapter", "Invalid location format: " + location);
                    }
                }
            }
        });

        return convertView;
    }
    private void handleFixLightTask(String groupTitle, LightData lightData) {
        DatabaseReference locationRef = FirebaseDatabase.getInstance().getReference("Location");
        locationRef.orderByValue().equalTo(groupTitle).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String locationKey = snapshot.getKey();
                    DatabaseReference specificLocationRef = FirebaseDatabase.getInstance().getReference("LightStreet").child(locationKey);
                    specificLocationRef.orderByChild("lightName").equalTo(lightData.getLightName()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                String lightKey = snapshot.getKey();
                                DatabaseReference lightRef = specificLocationRef.child(lightKey).child("error");
                                lightRef.setValue(2);
                                DatabaseReference warrantyRef = FirebaseDatabase.getInstance().getReference("Warranty");
                                warrantyRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        long newId = dataSnapshot.getChildrenCount() + 1; // Get the next available ID
                                        DatabaseReference newTaskRef = warrantyRef.child(String.valueOf(newId));
                                        String lightid = groupTitle + "-" + lightData.getLightName();
                                        newTaskRef.child("lightid").setValue(lightid);
                                        newTaskRef.child("status").setValue("Processing");
                                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy-HH:mm", Locale.getDefault());
                                        String currentTime = sdf.format(new Date());
                                        newTaskRef.child("timestamp").setValue(currentTime);
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        Log.e("Firebase", "Error fetching Warranty data", databaseError.toException());
                                    }
                                });
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e("Firebase", "Error querying light node", databaseError.toException());
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Error querying location node", databaseError.toException());
            }
        });
    }




    private boolean isValidLocationFormat(String location) {
        String[] parts = location.split(",");
        if (parts.length != 2) {
            return false;
        }
        try {
            Double.parseDouble(parts[0]);
            Double.parseDouble(parts[1]);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}

