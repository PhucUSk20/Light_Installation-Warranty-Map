package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import java.util.List;

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
        boolean hasError = false;
        List<LightData> children = listData.get(listGroupTitles.get(groupPosition));
        if (children != null) {
            for (LightData child : children) {
                if (child.getError() > 0) {
                    hasError = true;
                    break;
                }
            }
        }
        if (hasError) {
            convertView.setBackgroundColor(parent.getContext().getResources().getColor(android.R.color.holo_red_light)); // Màu nền đỏ
        } else {
            convertView.setBackgroundColor(parent.getContext().getResources().getColor(android.R.color.background_light)); // Màu nền mặc định
        }
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        LightData lightData = (LightData) getChild(groupPosition, childPosition);

        String error = lightData.getError() > 0 ? "error" : "noerror";

        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        }
        TextView textView = convertView.findViewById(android.R.id.text1);
        textView.setText("          Đèn "+lightData.toString());
        if (lightData.getError() > 0) {
            convertView.setBackgroundColor(parent.getContext().getResources().getColor(android.R.color.holo_red_light)); // Màu nền đỏ
        } else {
            convertView.setBackgroundColor(parent.getContext().getResources().getColor(android.R.color.background_light)); // Màu nền mặc định
        }
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
        });

        return convertView;
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

