package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class UserProfileActivity extends AppCompatActivity {

    private static final String TAG = "UserProfileActivity";

    private TextView tv_bdate, tv_email, emp_name, tv_mob, tv_sex, usernameTextView, password;
    private ImageView profile_img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("User Profile");

        // Initialize Views
        tv_bdate = findViewById(R.id.tv_bdate);
        tv_email = findViewById(R.id.tv_email);
        emp_name = findViewById(R.id.emp_name);
        tv_mob = findViewById(R.id.tv_mob);
        tv_sex = findViewById(R.id.tv_sex);
        usernameTextView = findViewById(R.id.username);
        profile_img = findViewById(R.id.profile_img);

        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String userid = prefs.getString("userId", "");

        Log.d(TAG, "UserID from SharedPreferences: " + userid);

        // Retrieve user data from Firebase
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userid);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String birthDate = dataSnapshot.child("birthDate").getValue(String.class);
                    String email = dataSnapshot.child("email").getValue(String.class);
                    String imageUrl = dataSnapshot.child("imageUrl").getValue(String.class);
                    String name = dataSnapshot.child("name").getValue(String.class);
                    String phoneNumber = dataSnapshot.child("phoneNumber").getValue(String.class);
                    String gender = dataSnapshot.child("gender").getValue(String.class);
                    String password = dataSnapshot.child("password").getValue(String.class);
                    String username = dataSnapshot.child("username").getValue(String.class);

                    Log.d(TAG, "User data retrieved from Firebase:");
                    Log.d(TAG, "Birth Date: " + birthDate);
                    Log.d(TAG, "Email: " + email);
                    Log.d(TAG, "Image URL: " + imageUrl);
                    Log.d(TAG, "Name: " + name);
                    Log.d(TAG, "Phone Number: " + phoneNumber);
                    Log.d(TAG, "Gender: " + gender);
                    Log.d(TAG, "Password: " + password);
                    Log.d(TAG, "Username: " + username);

                    // Set retrieved data to TextViews
                    tv_bdate.setText(birthDate);
                    tv_email.setText(email);
                    emp_name.setText(name);
                    tv_mob.setText(phoneNumber);
                    tv_sex.setText(gender);
                    usernameTextView.setText(username);

                    // Load profile image using Picasso
                    Picasso.get().load(imageUrl).into(profile_img);
                } else {
                    Toast.makeText(UserProfileActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(UserProfileActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Failed to load user data", databaseError.toException());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.user_profile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_edit) {
            // Open EditProfileActivity
            Intent intent = new Intent(this, EditProfileActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
