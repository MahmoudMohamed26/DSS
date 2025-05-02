package com.example.studentperformance.ui;


import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import com.example.studentperformance.DatabaseConnection;
import com.example.studentperformance.dao.AttendanceDAO;
import com.example.studentperformance.dao.AttendanceDAOImpl;
import com.example.studentperformance.dao.StudentDAO;
import com.example.studentperformance.dao.StudentDAOImpl;
import com.example.studentperformance.dao.SubjectDAO;
import com.example.studentperformance.dao.SubjectDAOImpl;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class AddAttendanceUI {

    private JFrame frame;
    private Connection connection;
    private DefaultTableModel tableModel;
    private JTextField studentIdField;
    private JTextField subjectIdField;
    private JTextField dateField;
    private JCheckBox presentCheckBox;

    public AddAttendanceUI(DefaultTableModel tableModel) {
        this.connection = DatabaseConnection.getConnection();
        this.tableModel = tableModel;
        frame = new JFrame("Add Attendance");
        frame.setResizable(false);
        frame.setPreferredSize(new Dimension(300, 250));
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        initializeUI();
    }

    private void initializeUI() {
        JPanel panel = new JPanel(new GridLayout(0, 2));
        JLabel studentIdLabel = new JLabel("Student ID:");
        studentIdField = new JTextField();
        JLabel subjectIdLabel = new JLabel("Subject ID:");
        subjectIdField = new JTextField();
        JLabel dateLabel = new JLabel("Date (yyyy-MM-dd): ");
        dateField = new JTextField();
        JLabel presentLabel = new JLabel("Present:");
        presentCheckBox = new JCheckBox();
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
                String studentIdText = studentIdField.getText();
                String subjectIdText = subjectIdField.getText();
                String dateText = dateField.getText();

                if (studentIdText.isEmpty() || subjectIdText.isEmpty() || dateText.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Please fill all fields", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    int studentId = Integer.parseInt(studentIdText);
                    int subjectId = Integer.parseInt(subjectIdText);
                    java.time.LocalDate date = java.time.LocalDate.parse(dateText, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    boolean present = presentCheckBox.isSelected();


                    AttendanceDAO attendanceDAO = new AttendanceDAOImpl(connection);
                    attendanceDAO.createAttendance(studentId, subjectId, date, present);
                    
                    StudentDAO studentDAO = new StudentDAOImpl(connection);
                    StudentDAO.Student student = studentDAO.readStudent(studentId);
                    SubjectDAO subjectDAO = new SubjectDAOImpl(connection);
                    SubjectDAO.Subject subject = subjectDAO.readSubject(subjectId);

                    Object[] row = {studentId, student.getName(), subjectId, subject.getName(), date.toString(), present};
                    tableModel.addRow(row);
                    frame.dispose();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Invalid ID format. Please enter a number.", "Error", JOptionPane.ERROR_MESSAGE);
                } catch (DateTimeParseException ex) {
                    JOptionPane.showMessageDialog(frame, "Invalid date format. Please use yyyy-MM-dd.", "Error", JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Error adding attendance: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        panel.add(studentIdLabel);
        panel.add(studentIdField);
        panel.add(subjectIdLabel);
        panel.add(subjectIdField);
        panel.add(dateLabel);
        panel.add(dateField);
        panel.add(presentLabel);
        panel.add(presentCheckBox);
        panel.add(addButton);
        panel.add(cancelButton);

        frame.add(panel);
        frame.pack();
    }

    public void setVisible(boolean visible) {
        frame.setVisible(visible);
    }
}