package com.example.studentperformance.ui;

import com.example.studentperformance.DatabaseConnection;
import javax.swing.*;
import java.awt.*;

public class MainUI {

    private JFrame frame;
    private JPanel contentPane;

    public MainUI() {
        initializeUI();
    }

    private void initializeUI() {
        frame = new JFrame("Student Performance Monitoring");
        
        //menu bar
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenu helpMenu = new JMenu("Help");
        JMenuItem exitItem = new JMenuItem("Exit");
        JMenuItem aboutItem = new JMenuItem("About");
        
        // Exit action
        exitItem.addActionListener(e -> System.exit(0));
        
        // About action
        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(frame, "Student Performance Monitoring Application\nVersion 1.0\nCreated by [Your Name]", "About", JOptionPane.INFORMATION_MESSAGE));
        
        fileMenu.add(exitItem);
        helpMenu.add(aboutItem);
        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        frame.setJMenuBar(menuBar);
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setPreferredSize(new Dimension(800, 600));
        contentPane = new JPanel(new BorderLayout());
        frame.setContentPane(contentPane);
        
        // Center Panel with Buttons
        JPanel centerPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton manageStudentsButton = new JButton("Manage Students");
        JButton manageSubjectsButton = new JButton("Manage Subjects");
        JButton manageGradesButton = new JButton("Manage Grades");
        JButton manageAttendanceButton = new JButton("Manage Attendance");

        manageStudentsButton.addActionListener(e -> {
            ManageStudentsUI manageStudentsUI = new ManageStudentsUI(DatabaseConnection.getConnection());
            manageStudentsUI.setVisible(true);
        });

        manageSubjectsButton.addActionListener(e -> {
            ManageSubjectsUI manageSubjectsUI = new ManageSubjectsUI();
            manageSubjectsUI.setVisible(true);
        });

        manageGradesButton.addActionListener(e -> {
            ManageGradesUI manageGradesUI = new ManageGradesUI(DatabaseConnection.getConnection());
            manageGradesUI.setVisible(true);
        });

        manageAttendanceButton.addActionListener(e -> {
            ManageAttendanceUI manageAttendanceUI = new ManageAttendanceUI();
            manageAttendanceUI.setVisible(true);
        });

        centerPanel.add(manageStudentsButton);
        centerPanel.add(manageSubjectsButton);
        centerPanel.add(manageGradesButton);
        centerPanel.add(manageAttendanceButton);

        contentPane.add(centerPanel, BorderLayout.CENTER);

         // Adding some padding around the main panel
         JPanel paddingPanel = new JPanel(new BorderLayout());
         paddingPanel.add(contentPane, BorderLayout.CENTER);
        
        // Set the main padding panel as the content pane
        frame.setContentPane(paddingPanel);

        // Set background color
        centerPanel.setBackground(Color.LIGHT_GRAY);

        frame.pack();
        frame.setVisible(true);
    }

    public void setVisible(boolean visible) {
        frame.setVisible(visible);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainUI mainUI = new MainUI();            
            mainUI.setVisible(true);
        });
    }
}