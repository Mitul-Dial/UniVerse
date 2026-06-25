package com.universe.models;

//session singleton
public class Session {
    private static Session instance;
    private User loggedInUser;

    //private constructor
    private Session() {}

    //get instance
    public static Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }

    //get user
    public User getLoggedInUser() {
        return loggedInUser;
    }

    //set user
    public void setLoggedInUser(User loggedInUser) {
        this.loggedInUser = loggedInUser;
    }
}
