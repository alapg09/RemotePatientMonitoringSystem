package com.rpms.UserManagement;

import java.io.Serializable;
// not abstract because there are no abstract methods so no need
public abstract class User implements Serializable {
    // basic data fields that all users will possess
    protected final String id;
    protected final String name;
    protected String phoneNumber;
    protected String email;
    protected String username;
    protected String password;

    // Add serialVersionUID
    private static final long serialVersionUID = 1L;


    // constructor
    public User(String id, String name,String phoneNumber, String email, String username, String password) {
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.username = username;
        // to ensure validation
        setPassword(password);
    }
    // getters
    public String getPassword() { return password; }
    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getUsername() { return username; }
    // no getter for password

    // for login purposes
    public boolean checkPassword(String inputPassword) {
        return this.password.equals(inputPassword);
    }
    public boolean checkUsername(String inputUsername) {
        return this.username.equals(inputUsername);
    }

    // setters
    // no setter for id and name because it should not be changed once created
    public void setEmail(String email) { this.email = email; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setUsername(String username) { this.username = username; }


    public void setPassword(String password) {
        this.password = password;
    }

    // for GUI
    public abstract String getRole();

    @Override
    public String toString() {
        return "ID: " + id + ", Name: " + name + ", Email: " + email;
    }
}
