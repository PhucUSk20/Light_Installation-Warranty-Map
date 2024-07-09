package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class SettingFragment extends Fragment {

    ImageView imageAvatar;
    TextView textName;
    TextView textEmail;
    DatabaseReference usersRef;
    ProgressBar progressBar;
    LinearLayout settingsLayout,headerLayout;

    public SettingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        // Initialize views
        imageAvatar = view.findViewById(R.id.image_avatar);
        textName = view.findViewById(R.id.text_name);
        textEmail = view.findViewById(R.id.text_email);
        progressBar = view.findViewById(R.id.progressBar);
        settingsLayout = view.findViewById(R.id.layout_setting);
        headerLayout = view.findViewById(R.id.layout_header);
        usersRef = FirebaseDatabase.getInstance().getReference("Users");
        SharedPreferences prefs = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String username = prefs.getString("username", "");
        loadUserData(username);

        // Load avatar image using Picasso (replace with actual avatar URL or resource)
        Picasso.get().load(R.drawable.avatar_default).into(imageAvatar);

        // Set user info (replace with actual user data if available)
        textName.setText("John Doe");
        textEmail.setText("john.doe@example.com");

        // Set click listeners for settings items
        LinearLayout editProfileLayout = view.findViewById(R.id.layout_edit_profile);
        editProfileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle edit profile action
                Toast.makeText(requireContext(), "Edit Profile clicked", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(requireActivity(), UserProfileActivity.class));
            }
        });

        LinearLayout changePasswordLayout = view.findViewById(R.id.layout_change_password);
        changePasswordLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle change password action
                Toast.makeText(requireContext(), "Change Password clicked", Toast.LENGTH_SHORT).show();
                // Example: startActivity(new Intent(requireActivity(), ChangePasswordActivity.class));
            }
        });

        LinearLayout logoutLayout = view.findViewById(R.id.layout_logout);
        logoutLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle logout action
                logoutUser();
            }
        });

        LinearLayout switchAccountLayout = view.findViewById(R.id.layout_switch_account);
        switchAccountLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle switch account action
                Toast.makeText(requireContext(), "Switch Account clicked", Toast.LENGTH_SHORT).show();
                // Example: startActivity(new Intent(requireActivity(), SwitchAccountActivity.class));
            }
        });

        return view;
    }
    private void loadUserData(String username) {
        progressBar.setVisibility(View.VISIBLE);
        settingsLayout.setVisibility(View.GONE);
        headerLayout.setVisibility(View.GONE);
        usersRef.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        progressBar.setVisibility(View.GONE);
                        settingsLayout.setVisibility(View.VISIBLE);
                        headerLayout.setVisibility(View.VISIBLE);
                        String email = userSnapshot.child("email").getValue(String.class);
                        String imageUrl = userSnapshot.child("imageUrl").getValue(String.class);
                        String username = userSnapshot.child("username").getValue(String.class);

                        // Set user info
                        textEmail.setText(email);
                        textName.setText(username);
                        Picasso.get().load(imageUrl).into(imageAvatar);
                    }
                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "User not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Failed to load user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logoutUser() {
        // Clear login status and navigate to LoginActivity
        SharedPreferences prefs = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isLoggedIn", false);
        editor.remove("userId"); // Remove user ID if needed
        editor.apply();

        startActivity(new Intent(requireActivity(), LoginActivity.class));
        requireActivity().finish();
    }
}
