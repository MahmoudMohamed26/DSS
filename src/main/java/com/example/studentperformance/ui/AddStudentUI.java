package com.example.studentperformance.ui;

import com.example.studentperformance.dao.StudentDAOImpl;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;


public class AddStudentUI {

    private JFrame frame;
    private DefaultTableModel tableModel;
    private JTextField nameField;
    private JTextField studentIdField;
    private StudentDAOImpl studentDAO;
    public AddStudentUI(Connection connection, DefaultTableModel tableModel) {
        this.tableModel = tableModel;
        studentDAO = new StudentDAOImpl(connection);
        frame = new JFrame("Add Student");
        frame.setResizable(false);
        frame.setPreferredSize(new Dimension(300, 150));
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        initializeUI();
    }

    private void initializeUI() {
        JPanel panel = new JPanel(new GridLayout(4, 2));
        JLabel studentIdLabel = new JLabel("Student ID:");
        studentIdField = new JTextField();
        JLabel nameLabel = new JLabel("Name:");
        nameField = new JTextField();
        JButton addButton = new JButton("Add");
        JButton cancelButton = new JButton("Cancel");

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
            }
        });

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = nameField.getText();
                String studentIdText = studentIdField.getText();
                 if (name != null && !name.trim().isEmpty() && studentIdText != null && !studentIdText.trim().isEmpty()) {
                    try {
                        int studentId = Integer.parseInt(studentIdText);
                        studentDAO.createStudent(String.valueOf(studentId));
                        Object[] row = {studentId, name};
                        tableModel.addRow(row);
                        frame.dispose();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(frame, "Error adding student: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "Please enter a name", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        panel.add(studentIdLabel);
        panel.add(nameLabel);
        panel.add(nameField);
        panel.add(addButton);
        panel.add(cancelButton);
        frame.add(panel);
        frame.pack();
    }

    public void setVisible(boolean visible) {
        frame.setVisible(visible);
    }
}