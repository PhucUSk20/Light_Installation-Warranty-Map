package com.example.myapplication;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
            getSupportActionBar().setTitle("Home"); // Set initial title
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();
                if (itemId == R.id.navigation_home) {
                    selectedFragment = new HomeFragment();
                    loadFragment(selectedFragment);
                    getSupportActionBar().setTitle("Home"); // Set title for Home
                    return true;
                } else if (itemId == R.id.navigation_historywarranty) {
                    selectedFragment = new WarrantyFragment();
                    loadFragment(selectedFragment);
                    getSupportActionBar().setTitle("Warranty History"); // Set title for Warranty
                    return true;
                } else if (itemId == R.id.navigation_setting) {
                    selectedFragment = new SettingFragment();
                    loadFragment(selectedFragment);
                    getSupportActionBar().setTitle("Settings"); // Set title for Settings
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }
}
