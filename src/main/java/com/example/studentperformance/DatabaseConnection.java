package com.example.studentperformance;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {    
    private static final String DB_URL = "jdbc:sqlite:student_performance.db";
    private Connection connection;
    private static DatabaseConnection instance;
    
    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite JDBC Driver not found.", e);
        }
    }

    private DatabaseConnection() {
        connect();
    }

    private void connect() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL);
                // Enable foreign keys
                connection.createStatement().execute("PRAGMA foreign_keys = ON");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to database", e);
        }
    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public static Connection getConnection() {
        Connection conn = getInstance().connection;
        try {
            if (conn == null || conn.isClosed()) {
                getInstance().connect();
                conn = getInstance().connection;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database connection is not valid", e);
        }
        return conn;
    }
}