package com.universe.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

//database connection
public class DBConnection {

    private static DBConnection instance;
    private Connection connection;

    //sql server string
    private static final String URL =
            "jdbc:sqlserver://localhost;databaseName=universe_db;integratedSecurity=true;encrypt=false;";

    //private constructor
    private DBConnection() {
        try {
            connection = DriverManager.getConnection(URL);
            System.out.println("UniVerse Frontend: Connected to universe_db!");
        } catch (SQLException e) {
            System.out.println("Connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //singleton instance
    public static DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    //get connection
    public Connection getConnection() {
        return connection;
    }

    //close connection
    public static void closeConnection() {
        if (instance != null && instance.connection != null) {
            try {
                instance.connection.close();
                System.out.println("Connection closed.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}