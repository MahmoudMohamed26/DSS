package com.example.studentperformance.ui;

import com.example.studentperformance.dao.StudentDAO;
import com.example.studentperformance.dao.StudentDAOImpl;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;

public class EditStudentUI {
    private JFrame frame;
    private Connection connection;
    private DefaultTableModel tableModel;
    private JTextField nameField;
    private int studentId;

    public EditStudentUI(Connection connection, DefaultTableModel tableModel, int studentId) {
        this.connection = connection;
        this.tableModel = tableModel;
        this.studentId = studentId;
        frame = new JFrame("Edit Student");
        frame.setResizable(false);
        frame.setPreferredSize(new Dimension(300, 150));
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        initializeUI();
    }

    private void initializeUI() {
        JPanel panel = new JPanel(new GridLayout(3, 2));
        JLabel nameLabel = new JLabel("Name:");
        nameField = new JTextField();
        JButton editButton = new JButton("Edit");
        JButton cancelButton = new JButton("Cancel");

        // Load student data
        StudentDAO studentDAO = new StudentDAOImpl(connection);
        try {
            StudentDAO.Student student = studentDAO.readStudent(studentId);
            nameField.setText(student.getName());
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error getting student data: " + e.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
        }

        cancelButton.addActionListener(e -> frame.dispose());

        editButton.addActionListener(e -> {
            String name = nameField.getText();
            if (name != null && !name.trim().isEmpty()) {
                try {
                    studentDAO.updateStudent(studentId, name);
                    for (int i = 0; i < tableModel.getRowCount(); i++) {
                        if ((int) tableModel.getValueAt(i, 0) == studentId) {
                            tableModel.setValueAt(name, i, 1);
                            break;
                        }
                    }
                    frame.dispose();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Error updating student: " + ex.getMessage(), 
                                                "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Please enter a name", 
                                            "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(nameLabel);
        panel.add(nameField);
        panel.add(editButton);
        panel.add(cancelButton);
        frame.add(panel);
        frame.pack();
    }

    public void setVisible(boolean visible) {
        frame.setVisible(visible);
    }
}