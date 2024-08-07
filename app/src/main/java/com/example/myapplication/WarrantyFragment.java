package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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
// WarrantyFragment.java

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.PopupMenu;
import java.util.Collections;
import java.util.Comparator;

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
// WarrantyFragment.java

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.PopupMenu;
import android.widget.ProgressBar;

import androidx.core.view.MenuItemCompat;

import java.util.Collections;
import java.util.Comparator;

public class WarrantyFragment extends Fragment {

    private ListView listView;
    private WarrantyAdapter adapter;
    private List<WarrantyData> warrantyList;
    private ProgressBar progressBar;
    public WarrantyFragment() {
    }

    public static WarrantyFragment newInstance() {
        return new WarrantyFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true); // Add this line to enable the options menu
        View view = inflater.inflate(R.layout.fragment_warranty, container, false);
        progressBar = view.findViewById(R.id.progressBar);
        return view;
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

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_warranty, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_sort) {
            showSortMenu(item);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSortMenu(MenuItem item) {
        View anchor = MenuItemCompat.getActionView(item);
        if (anchor == null) {
            anchor = getActivity().findViewById(R.id.action_sort);
        }

        PopupMenu popup = new PopupMenu(getContext(), anchor);
        popup.getMenuInflater().inflate(R.menu.menu_sort_options, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.sort_by_date) {
                    sortByDate();
                    return true;
                } else if (itemId == R.id.sort_by_status) {
                    sortByStatus();
                    return true;
                } else if (itemId == R.id.sort_combined) {
                    sortCombined();
                    return true;
                }
                return false;
            }
        });

        popup.show();
    }

    private void sortByDate() {
        progressBar.setVisibility(View.VISIBLE);
        Collections.sort(warrantyList, new Comparator<WarrantyData>() {
            @Override
            public int compare(WarrantyData o1, WarrantyData o2) {
                return o2.getTimestamp().compareTo(o1.getTimestamp());
            }
        });
        adapter.notifyDataSetChanged();
        progressBar.setVisibility(View.GONE);
    }

    private void sortByStatus() {
        progressBar.setVisibility(View.VISIBLE);
        Collections.sort(warrantyList, new Comparator<WarrantyData>() {
            @Override
            public int compare(WarrantyData o1, WarrantyData o2) {
                return o1.getStatus().compareTo(o2.getStatus());
            }
        });
        adapter.notifyDataSetChanged();
        progressBar.setVisibility(View.GONE);
    }

    private void sortCombined() {
        progressBar.setVisibility(View.VISIBLE);
        Collections.sort(warrantyList, new Comparator<WarrantyData>() {
            @Override
            public int compare(WarrantyData o1, WarrantyData o2) {
                if (o1.getStatus().equals(o2.getStatus())) {
                    return o2.getTimestamp().compareTo(o1.getTimestamp());
                }
                return o1.getStatus().compareTo(o2.getStatus());
            }
        });
        adapter.notifyDataSetChanged();
        progressBar.setVisibility(View.GONE);
    }
    private void loadDataFromFirebase() {
        progressBar.setVisibility(View.VISIBLE);
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "default_username");

        FirebaseDatabase.getInstance().getReference("Warranty")
                .orderByChild("assignedTo")
                .equalTo(username)
                .addValueEventListener(new ValueEventListener() {
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
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle possible errors.
                        progressBar.setVisibility(View.GONE);
                        Log.e("Firebase", "Error loading Warranty data", databaseError.toException());
                    }
                });
    }
}