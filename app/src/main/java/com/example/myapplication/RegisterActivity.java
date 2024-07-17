package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextUsername, editTextPassword, editTextConfirmPassword, editTextEmployeeCode;
    private Button buttonRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        editTextConfirmPassword = findViewById(R.id.confirm_password);
        buttonRegister = findViewById(R.id.register_button);
        editTextUsername = findViewById(R.id.username);
        editTextEmployeeCode = findViewById(R.id.employee_code);

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });
    }

    private void registerUser() {
        final String email = editTextEmail.getText().toString().trim();
        final String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();
        final String username = editTextUsername.getText().toString().trim();
        final String employeeCode = editTextEmployeeCode.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword) ||
                TextUtils.isEmpty(username) || TextUtils.isEmpty(employeeCode)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference employeeRef = FirebaseDatabase.getInstance().getReference("EmployeeID");
        employeeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean employeeExists = false;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    if (ds.getValue().equals(employeeCode)) {
                        employeeExists = true;
                        break;
                    }
                }

                if (!employeeExists) {
                    Toast.makeText(RegisterActivity.this, "Employee ID does not exist", Toast.LENGTH_SHORT).show();
                } else {
                    checkUserExistenceAndRegister(email, password, username, employeeCode);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RegisterActivity.this, "Failed to check Employee ID", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkUserExistenceAndRegister(final String email, final String password, final String username, final String employeeCode) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean userExists = false;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    User user = ds.getValue(User.class);
                    if (user != null && employeeCode.equals(user.getEmployeeId())) {
                        userExists = true;
                        break;
                    }
                }

                if (userExists) {
                    Toast.makeText(RegisterActivity.this, "Employee ID is already in use", Toast.LENGTH_SHORT).show();
                } else {
                    registerNewUser(email, password, username, employeeCode);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RegisterActivity.this, "Failed to check Users", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void registerNewUser(String email, String password, String username, String employeeCode) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
        String userId = usersRef.push().getKey();
        User user = new User(email, password, username, employeeCode);

        if (userId != null) {
            usersRef.child(userId).setValue(user);
            Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show();
        }
    }
}
