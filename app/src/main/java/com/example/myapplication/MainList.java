package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MainList extends AppCompatActivity {

    ExpandableListView expandableListView;
    FloatingActionButton fab;
    List<String> listGroupTitles;
    Map<String, List<LightData>> listData;
    MyExpandableListAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainlist);

        expandableListView = findViewById(R.id.expandableListView);
        fab = findViewById(R.id.fab);

        initializeData();
        listAdapter = new MyExpandableListAdapter(this);
        listAdapter.setData(listGroupTitles, listData);
        expandableListView.setAdapter(listAdapter);

        fab.setOnClickListener(v -> showCreateLightDialog());
    }

    private void initializeData() {
        listGroupTitles = new ArrayList<>();
        listData = new HashMap<>();

        FirebaseDatabase.getInstance().getReference("Location").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot locationSnapshot) {
                Map<String, String> khuVucNames = new HashMap<>();
                for (DataSnapshot khuVucSnapshot : locationSnapshot.getChildren()) {
                    khuVucNames.put(khuVucSnapshot.getKey(), khuVucSnapshot.getValue(String.class));
                }

                FirebaseDatabase.getInstance().getReference("LightStreet").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot lightStreetSnapshot) {
                        clearData();
                        for (DataSnapshot areaSnapshot : lightStreetSnapshot.getChildren()) {
                            String areaKey = areaSnapshot.getKey();
                            String areaName = khuVucNames.get(areaKey);
                            if (areaName != null) {
                                listGroupTitles.add(areaName);
                                List<LightData> lights = new ArrayList<>();
                                for (DataSnapshot lightSnapshot : areaSnapshot.getChildren()) {
                                    LightData lightData = lightSnapshot.getValue(LightData.class);
                                    lights.add(lightData);
                                }
                                listData.put(areaName, lights);
                            }
                        }
                        listAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(MainList.this, "Failed to load data.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainList.this, "Failed to load location data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearData() {
        listGroupTitles.clear();
        listData.clear();
    }

    private void showCreateLightDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_create_light, null);
        Spinner spinnerKhuvuc = dialogView.findViewById(R.id.spinnerKhuvuc);
        EditText editTextNewKhuvuc = dialogView.findViewById(R.id.editTextNewKhuvuc);
        EditText editTextLocation = dialogView.findViewById(R.id.editTextLocation);
        TextView textViewLightName = dialogView.findViewById(R.id.textViewLightName);

        RadioGroup radioGroup = dialogView.findViewById(R.id.radioGroup);
        RadioButton radioButtonOption1 = dialogView.findViewById(R.id.radioButtonOption1);
        RadioButton radioButtonOption2 = dialogView.findViewById(R.id.radioButtonOption2);

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioButtonOption1) {
                spinnerKhuvuc.setVisibility(View.VISIBLE);
                editTextNewKhuvuc.setVisibility(View.GONE);
                if (spinnerKhuvuc.getSelectedItem() != null) {
                    String khuVuc = spinnerKhuvuc.getSelectedItem().toString();
                    String lightName = generateLightNameForOption1(khuVuc);
                    textViewLightName.setText("Name of Light: " + lightName);
                }
            } else if (checkedId == R.id.radioButtonOption2) {
                spinnerKhuvuc.setVisibility(View.GONE);
                editTextNewKhuvuc.setVisibility(View.VISIBLE);
                textViewLightName.setText("Name of Light: Light1");
            }
        });
        spinnerKhuvuc.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String khuVuc = parent.getItemAtPosition(position).toString();
                String lightName = generateLightNameForOption1(khuVuc); 
                textViewLightName.setText("Name of Light: " + lightName);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Không cần xử lý gì khi không có gì được chọn
            }
        });


        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listGroupTitles);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerKhuvuc.setAdapter(spinnerAdapter);

        new AlertDialog.Builder(this)
                .setTitle("Create New Light")
                .setView(dialogView)
                .setPositiveButton("Create", (dialog, which) -> {
                    String khuVuc = "";
                    String lightName = "";
                    String location = editTextLocation.getText().toString().trim();

                    if (location.isEmpty()) {
                        Toast.makeText(this, "Địa chỉ không được để trống", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int selectedRadioButtonId = radioGroup.getCheckedRadioButtonId();
                    if (selectedRadioButtonId == R.id.radioButtonOption1) {
                        if (spinnerKhuvuc.getSelectedItem() != null) {
                            khuVuc = spinnerKhuvuc.getSelectedItem().toString();
                            lightName = generateLightNameForOption1(khuVuc);
                            queryKhuVucNames( khuVuc,  lightName,  location);
                        }
                    } else if (selectedRadioButtonId == R.id.radioButtonOption2) {
                        khuVuc = editTextNewKhuvuc.getText().toString().trim();
                        if (!khuVuc.isEmpty()) {
                            lightName = generateLightNameForOption2(khuVuc);
                            createNewKhuvucInLocation(khuVuc, location);
                        }
                    }

                    if (lightName.isEmpty()) {
                        Toast.makeText(this, "Không thể tạo tên đèn. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    private void queryKhuVucNames(String khuVuc, String lightName, String location) {
        FirebaseDatabase.getInstance().getReference("Location")
                .orderByValue()
                .equalTo(khuVuc)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                String key = snapshot.getKey();
                                createNewLight(key, lightName, location);
                                return;
                            }
                        } else {
                            Toast.makeText(MainList.this, "Không tìm thấy khu vực trong Location", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(MainList.this, "Lỗi khi truy vấn dữ liệu từ Firebase", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private String generateLightNameForOption1(String khuVuc) {
        int maxNumber = 0;
        List<LightData> existingLights = listData.get(khuVuc);
        if (existingLights != null) {
            for (LightData light : existingLights) {
                if (light.getLightName().startsWith("Light")) {
                    try {
                        int number = Integer.parseInt(light.getLightName().substring(5));
                        if (number > maxNumber) {
                            maxNumber = number;
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return "Light" + (maxNumber + 1);
    }

    private String generateLightNameForOption2(String newKhuvuc) {
        return "Light1";
    }

    private void createNewKhuvucInLocation(String displayName, String location) {
        FirebaseDatabase.getInstance().getReference("Location").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int maxNumber = 0;
                for (DataSnapshot khuVucSnapshot : snapshot.getChildren()) {
                    String key = khuVucSnapshot.getKey();
                    if (key.startsWith("Khuvuc")) {
                        try {
                            int number = Integer.parseInt(key.substring(6));
                            if (number > maxNumber) {
                                maxNumber = number;
                            }
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }
                }
                String newKhuvucName = "Khuvuc" + (maxNumber + 1);
                FirebaseDatabase.getInstance().getReference("Location")
                        .child(newKhuvucName)
                        .setValue(displayName)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(MainList.this, "Khuvuc created successfully", Toast.LENGTH_SHORT).show();
                            initializeData();
                            listAdapter = new MyExpandableListAdapter(getApplicationContext());
                            listAdapter.setData(listGroupTitles, listData);

                            expandableListView.setAdapter(listAdapter);
                        })
                        .addOnFailureListener(e -> Toast.makeText(MainList.this, "Failed to create khuvuc", Toast.LENGTH_SHORT).show());
                FirebaseDatabase.getInstance().getReference("LightStreet")
                        .child(newKhuvucName)
                        .child("Light1")
                        .setValue(new LightData("Light1", 0, location, 1, 2000, 100, 5))
                        .addOnSuccessListener(aVoid -> Toast.makeText(MainList.this, "StreetLight created successfully", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(MainList.this, "Failed to create StreetLight", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainList.this, "Failed to create khuvuc", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createNewLight(String khuVuc, String lightName, String location) {
        LightData newLightData = new LightData(lightName, 0, location, 1, 2000, 100, 5);
        FirebaseDatabase.getInstance().getReference("LightStreet")
                .child(khuVuc)
                .child(lightName)
                .setValue(newLightData)
                .addOnSuccessListener(aVoid -> Toast.makeText(MainList.this, "Light created successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(MainList.this, "Failed to create light", Toast.LENGTH_SHORT).show());
    }
}
