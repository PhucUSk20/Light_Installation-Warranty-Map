package com.example.myapplication;

public class User {
    private String email;
    private String password;
    private String username;
    private String employeeId;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String email, String password, String username, String employeeId) {
        this.email = email;
        this.password = password;
        this.username = username;
        this.employeeId = employeeId;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public String getEmployeeId() {
        return employeeId;
    }
}
