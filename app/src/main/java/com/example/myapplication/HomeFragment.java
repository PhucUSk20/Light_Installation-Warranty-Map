package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
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

public class HomeFragment extends Fragment {
    private static final int REQUEST_CODE_SELECT_LOCATION = 1;
    ExpandableListView expandableListView;
    FloatingActionButton fab;
    ProgressBar progressBar;
    List<String> listGroupTitles;
    Map<String, List<LightData>> listData;
    MyExpandableListAdapter listAdapter;
    EditText editTextDialogLocation;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        expandableListView = view.findViewById(R.id.expandableListView);
        fab = view.findViewById(R.id.fab);
        progressBar = view.findViewById(R.id.progressBar);
        initializeData();
        listAdapter = new MyExpandableListAdapter(getContext());
        listAdapter.setData(listGroupTitles, listData);
        expandableListView.setAdapter(listAdapter);

        fab.setOnClickListener(v -> showCreateLightDialog());


        return view;
    }


    private void initializeData() {
        listGroupTitles = new ArrayList<>();
        listData = new HashMap<>();
        progressBar.setVisibility(View.VISIBLE);
        expandableListView.setVisibility(View.GONE);
        fab.setVisibility(View.GONE);
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
                        progressBar.setVisibility(View.GONE);
                        expandableListView.setVisibility(View.VISIBLE);
                        fab.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Failed to load data.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load location data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearData() {
        listGroupTitles.clear();
        listData.clear();
    }

    // Trong phương thức showCreateLightDialog()
    private void showCreateLightDialog() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_create_light, null);
        Spinner spinnerKhuvuc = dialogView.findViewById(R.id.spinnerKhuvuc);
        EditText editTextNewKhuvuc = dialogView.findViewById(R.id.editTextNewKhuvuc);
        editTextDialogLocation = dialogView.findViewById(R.id.editTextLocation);
        TextView textViewLightName = dialogView.findViewById(R.id.textViewLightName);

        RadioGroup radioGroup = dialogView.findViewById(R.id.radioGroup);
        RadioButton radioButtonOption1 = dialogView.findViewById(R.id.radioButtonOption1);
        RadioButton radioButtonOption2 = dialogView.findViewById(R.id.radioButtonOption2);
        Button buttonSelectLocation = dialogView.findViewById(R.id.btnChooseFromMap);


        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            // Xử lý thay đổi của RadioGroup
            if (checkedId == R.id.radioButtonOption1) {
                // Xử lý khi chọn Option 1
                spinnerKhuvuc.setVisibility(View.VISIBLE);
                editTextNewKhuvuc.setVisibility(View.GONE);
                if (spinnerKhuvuc.getSelectedItem() != null) {
                    String khuVuc = spinnerKhuvuc.getSelectedItem().toString();
                    String lightName = generateLightNameForOption1(khuVuc);
                    textViewLightName.setText("Name of Light: " + lightName);
                }
            } else if (checkedId == R.id.radioButtonOption2) {
                // Xử lý khi chọn Option 2
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

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, listGroupTitles);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerKhuvuc.setAdapter(spinnerAdapter);


        buttonSelectLocation.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SelectLocation.class);
            startActivityForResult(intent, REQUEST_CODE_SELECT_LOCATION);
        });

        new AlertDialog.Builder(getContext())
                .setTitle("Create New Light")
                .setView(dialogView)
                .setPositiveButton("Create", (dialog, which) -> {
                    String khuVuc = "";
                    String lightName = "";
                    String location = editTextDialogLocation.getText().toString().trim();

                    if (location.isEmpty()) {
                        Toast.makeText(getContext(), "Địa chỉ không được để trống", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int selectedRadioButtonId = radioGroup.getCheckedRadioButtonId();
                    if (selectedRadioButtonId == R.id.radioButtonOption1) {
                        if (spinnerKhuvuc.getSelectedItem() != null) {
                            khuVuc = spinnerKhuvuc.getSelectedItem().toString();
                            lightName = generateLightNameForOption1(khuVuc);
                            queryKhuVucNames(khuVuc, lightName, location);
                        }
                    } else if (selectedRadioButtonId == R.id.radioButtonOption2) {
                        khuVuc = editTextNewKhuvuc.getText().toString().trim();
                        if (!khuVuc.isEmpty()) {
                            lightName = generateLightNameForOption2(khuVuc);
                            createNewKhuvucInLocation(khuVuc, location);
                        }
                    }

                    if (lightName.isEmpty()) {
                        Toast.makeText(getContext(), "Không thể tạo tên đèn. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_LOCATION && resultCode == Activity.RESULT_OK && data != null) {
            double longitude = data.getDoubleExtra("longitude", 0.0);
            double latitude = data.getDoubleExtra("latitude", 0.0);
            if (editTextDialogLocation != null) {
                editTextDialogLocation.setText(longitude + "," + latitude);
            }
        }
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
                            Toast.makeText(getContext(), "Không tìm thấy khu vực trong Location", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(getContext(), "Lỗi khi truy vấn dữ liệu từ Firebase", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(getContext(), "Khuvuc created successfully", Toast.LENGTH_SHORT).show();
                            initializeData();
                            listAdapter = new MyExpandableListAdapter(getContext());
                            listAdapter.setData(listGroupTitles, listData);

                            expandableListView.setAdapter(listAdapter);
                        })
                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to create khuvuc", Toast.LENGTH_SHORT).show());
                FirebaseDatabase.getInstance().getReference("LightStreet")
                        .child(newKhuvucName)
                        .child("Light1")
                        .setValue(new LightData("Light1", 0, location, 1, 2000, 100, 5))
                        .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "StreetLight created successfully", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to create StreetLight", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to create khuvuc", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createNewLight(String khuVuc, String lightName, String location) {
        LightData newLightData = new LightData(lightName, 0, location, 1, 2000, 100, 5);
        FirebaseDatabase.getInstance().getReference("LightStreet")
                .child(khuVuc)
                .child(lightName)
                .setValue(newLightData)
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Light created successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to create light", Toast.LENGTH_SHORT).show());
    }
}
