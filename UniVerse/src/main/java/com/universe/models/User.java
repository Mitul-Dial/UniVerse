package com.universe.models;

//abstract user
public abstract class User {
    protected String userID;
    protected String name;
    protected String email;
    protected String password;

    //constructor
    public User(String userID, String name, String email, String password) {
        this.userID = userID;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    //auth methods
    public abstract boolean login();
    public abstract void logout();

    //password check
    public boolean checkPassword(String inputPassword) {
        return this.password != null && this.password.equals(inputPassword);
    }

    //email validation
    public boolean isEmailValid() {
        return email != null && email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
    }

    //profile check
    public boolean isProfileComplete() {
        return userID != null && !userID.isBlank()
                && name   != null && !name.isBlank()
                && email  != null && !email.isBlank()
                && password != null && !password.isBlank();
    }

    //getters setters
    public String getUserID()   { return userID; }
    public void setUserID(String userID) { this.userID = userID; }

    public String getName()     { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail()    { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    //to string
    @Override
    public String toString() {
        return "User{userID='" + userID + "', name='" + name + "', email='" + email + "'}";
    }
}