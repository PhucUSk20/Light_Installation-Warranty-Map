package com.example.myapplication;

import static androidx.fragment.app.FragmentManager.TAG;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Objects;

public class EditProfileActivity extends AppCompatActivity {
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    ImageView imageView;
    private final int SELECT_PHOTO = 1;
    private DatabaseReference databaseReference;
    private Uri imageUri;
    private String filename;
    private Button btnSave;
    private EditText etName, etEmail, etPhoneNumber, etGender, etBirthDate;
    private TextView tv_alert,tv_emp_id;
    private AlertDialog alertDialog;
    private String uploadedUrl;
    private String userid;
    private String username;
    private String password;
    SharedPreferences sharedPref;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Edit Profile");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        sharedPref = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        userid = sharedPref.getString("userId", null);
        username = sharedPref.getString("username", null);
        password = sharedPref.getString("password", null);  // Changed to get password instead of username

        imageView = findViewById(R.id.img_profile_edit);
        btnSave = findViewById(R.id.btn_save);
        etName = findViewById(R.id.emp_name);
        etEmail = findViewById(R.id.et_email_text);
        etBirthDate = findViewById(R.id.et_date_of_birth);
        etPhoneNumber = findViewById(R.id.et_mobile_number);
        etGender = findViewById(R.id.et_gender);
        tv_emp_id = findViewById(R.id.tv_emp_id);
        initializeProgressBarDialog();
        databaseReference.child(userid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String employeeId = dataSnapshot.child("employeeID").getValue(String.class);
                    if (employeeId != null) {
                        // Update the TextView with employeeId
                        tv_emp_id.setText(employeeId);
                    } else {
                        // Handle case where employeeId is null
                        tv_emp_id.setText("Not Assigned");
                    }
                } else {
                    // Handle case where user data doesn't exist
                    tv_emp_id.setText("User not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(EditProfileActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();

            }
        });
        databaseReference.child(userid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Lấy dữ liệu từ Firebase
                    String name = dataSnapshot.child("name").getValue(String.class);
                    String email = dataSnapshot.child("email").getValue(String.class);
                    String phoneNumber = dataSnapshot.child("phoneNumber").getValue(String.class);
                    String gender = dataSnapshot.child("gender").getValue(String.class);
                    String birthDate = dataSnapshot.child("birthDate").getValue(String.class);
                    String imageUrl = dataSnapshot.child("imageUrl").getValue(String.class);

                    // Cập nhật lên các EditText
                    etName.setText(name);
                    etEmail.setText(email);
                    etPhoneNumber.setText(phoneNumber);
                    etGender.setText(gender);
                    etBirthDate.setText(birthDate);

                    // Nếu có imageUrl thì hiển thị lên ImageView
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        // Sử dụng thư viện Picasso để tải hình ảnh từ URL vào ImageView
                        Picasso.get().load(imageUrl).into(imageView);
                    }
                } else {
                    // Handle case where user data doesn't exist
                    Toast.makeText(EditProfileActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(EditProfileActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, SELECT_PHOTO);
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateFields()) {
                    // If all valid then update the profile
                    if (filename != null)
                        uploadImageToFirebaseStorage();
                    else
                        updateProfile();
                }
            }
        });
    }
    private void initializeProgressBarDialog() {
        View v = getLayoutInflater().inflate(R.layout.alert_progress,null);
        progressBar = v.findViewById(R.id.progressBar2);
        tv_alert = v.findViewById(R.id.alert_tv);
        ProgressBar progressBar = new ProgressBar(EditProfileActivity.this);
        progressBar.setPadding(10,30,10,30);

        final AlertDialog.Builder alertDialogbuilder = new AlertDialog.Builder(EditProfileActivity.this);
        alertDialog = alertDialogbuilder.create();
        alertDialog.setCancelable(false);
        alertDialog.setView(v);
    }
    private void updateProfile() {
        if(!alertDialog.isShowing()) {
            tv_alert.setText("Updating Profile...");
            alertDialog.show();
        }
        String name = etName.getText().toString();
        String email = etEmail.getText().toString();
        String phoneNumber = etPhoneNumber.getText().toString();
        String gender = etGender.getText().toString();
        String birthDate = etBirthDate.getText().toString();

        HashMap<String, Object> profileData = new HashMap<>();
        profileData.put("name", name);
        profileData.put("username", username);
        profileData.put("password", password);
        profileData.put("email", email);
        profileData.put("phoneNumber", phoneNumber);
        profileData.put("gender", gender);
        profileData.put("birthDate", birthDate);
        if (uploadedUrl != null) {
            profileData.put("imageUrl", uploadedUrl);
        }
        String employeeId = tv_emp_id.getText().toString();
        profileData.put("employeeID", employeeId);
        databaseReference.child(userid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Lấy các giá trị hiện tại từ Firebase
                    Integer taskAssigned = dataSnapshot.child("taskAssigned").getValue(Integer.class);
                    Integer taskComplete = dataSnapshot.child("taskComplete").getValue(Integer.class);

                    // Đặt các giá trị hiện tại vào map nếu chưa có giá trị mới
                    if (taskAssigned != null) {
                        profileData.put("taskAssigned", taskAssigned);
                    }
                    if (taskComplete != null) {
                        profileData.put("taskComplete", taskComplete);
                    }

                    // Cập nhật dữ liệu trên Firebase
                    databaseReference.child(userid).updateChildren(profileData).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(EditProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(EditProfileActivity.this, UserProfileActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(EditProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                            }
                            alertDialog.dismiss();
                        }
                    });
                } else {
                    Toast.makeText(EditProfileActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(EditProfileActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    private boolean validateFields() {
        boolean isValid = true;
        if (etName.getText().length() <= 0) {
            etName.setError("Enter name");
            isValid = false;
        } else if (!isValidEmail(etEmail.getText().toString())) {
            etEmail.setError("Enter valid email");
            isValid = false;
        } else if (etPhoneNumber.getText().toString().length() != 10) {
            etPhoneNumber.setError("Enter valid mobile number");
            isValid = false;
        } else if (etBirthDate.getText().toString().length() <= 0) {
            etBirthDate.setError("Enter birth date");
            isValid = false;
        }
        return isValid;
    }

    private void uploadImageToFirebaseStorage() {
        tv_alert.setText("Updating Profile...");
        alertDialog.show();

        final StorageReference reference = storageReference.child(username + "_" + filename);
        UploadTask uploadTask = reference.putFile(imageUri);
        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful())
                    throw Objects.requireNonNull(task.getException());
                return reference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri uri = task.getResult();
                    uploadedUrl = uri.toString();
                    updateProfile();
                } else {
                    Toast.makeText(EditProfileActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    alertDialog.dismiss(); // Dismiss the dialog in case of failure
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case SELECT_PHOTO:
                if (resultCode == RESULT_OK) {
                    try {
                        imageUri = data.getData();
                        Cursor c = getContentResolver().query(imageUri, null, null, null, null);
                        int n = 0;
                        if (c != null) {
                            n = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                            c.moveToFirst();
                            filename = c.getString(n);
                            c.close();
                        }
                        final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                        final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                        imageView.setImageBitmap(selectedImage);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }
}
