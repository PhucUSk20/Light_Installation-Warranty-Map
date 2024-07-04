package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class WarrantyFragment extends Fragment {

    private ListView listView;
    private WarrantyAdapter adapter;
    private List<WarrantyData> warrantyList;

    public WarrantyFragment() {
    }

    public static WarrantyFragment newInstance() {
        return new WarrantyFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_warranty, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        listView = view.findViewById(R.id.warranty_view);
        warrantyList = new ArrayList<>();
        adapter = new WarrantyAdapter(getContext(), warrantyList);
        listView.setAdapter(adapter);

        loadDataFromFirebase();
    }

    private void loadDataFromFirebase() {
        FirebaseDatabase.getInstance().getReference("Warranty").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                warrantyList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    WarrantyData warrantyData = snapshot.getValue(WarrantyData.class);
                    if (warrantyData != null) {
                        warrantyList.add(warrantyData);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors.
            }
        });
    }
}
