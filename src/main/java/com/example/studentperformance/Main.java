package com.example.studentperformance;

import com.example.studentperformance.ui.MainUI;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("Application started");

        try {
            DatabaseConnection.getInstance(); // Initialize database connection
//            init_db.initializeDatabase(); // Initialize database schema

            SwingUtilities.invokeLater(() -> {
                MainUI mainUI = new MainUI();
                mainUI.setVisible(true);
            });
        } catch (Exception e) {
            System.err.println("Failed to start application: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } finally {
            // Clean up resources if needed
        }
    }
}
