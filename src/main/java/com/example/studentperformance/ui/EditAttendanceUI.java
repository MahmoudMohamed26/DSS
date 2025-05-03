package com.example.studentperformance.ui;

import com.example.studentperformance.DatabaseConnection;
import com.example.studentperformance.dao.AttendanceDAO;
import com.example.studentperformance.dao.AttendanceDAOImpl;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;

public class EditAttendanceUI {

    private JFrame frame;
    private DefaultTableModel tableModel;
    private JCheckBox presentCheckBox;
    private int studentId;
    private int subjectId;
    private LocalDate date;

    public EditAttendanceUI(DefaultTableModel tableModel, int studentId, int subjectId, LocalDate date) {
        this.tableModel = tableModel;
        this.studentId = studentId;
        this.subjectId = subjectId;
        this.date = date;
        frame = new JFrame("Edit Attendance");
        frame.setResizable(false);
        frame.setPreferredSize(new Dimension(300, 200));
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        initializeUI();
    }

    private void initializeUI() {
        JPanel panel = new JPanel(new GridLayout(3, 2));
        JLabel presentLabel = new JLabel("Present:");
        presentCheckBox = new JCheckBox();
        JButton editButton = new JButton("Edit");
        JButton cancelButton = new JButton("Cancel");

        cancelButton.addActionListener(e -> frame.dispose());

        editButton.addActionListener(e -> {
            boolean present = presentCheckBox.isSelected();
            try {
                AttendanceDAO attendanceDAO = new AttendanceDAOImpl(DatabaseConnection.getConnection());
                attendanceDAO.updateAttendance(studentId, subjectId, date, present);
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    if ((int) tableModel.getValueAt(i, 0) == studentId && (int) tableModel.getValueAt(i, 2) == subjectId && ((LocalDate)tableModel.getValueAt(i, 4)).isEqual(date))  {
                        tableModel.setValueAt(present, i, 5);
                        break;
                    }
                }
                frame.dispose();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error updating attendance: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        AttendanceDAO attendanceDAO = new AttendanceDAOImpl(DatabaseConnection.getConnection());
        try{
            AttendanceDAO.Attendance attendance = attendanceDAO.readAttendance(studentId, subjectId, date);
            presentCheckBox.setSelected(attendance.isPresent());
        }catch(Exception e){
            e.printStackTrace();
           JOptionPane.showMessageDialog(frame, "Error loading attendance: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        panel.add(presentLabel);
        panel.add(presentCheckBox);
        panel.add(editButton);
        panel.add(cancelButton);
        frame.add(panel);
        frame.pack();
    }

    public void setVisible(boolean visible) {
        frame.setVisible(visible);
    }
}