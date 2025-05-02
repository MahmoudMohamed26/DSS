package com.example.studentperformance.ui;



import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import com.example.studentperformance.DatabaseConnection;
import com.example.studentperformance.dao.AbstractDAO;
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
import java.time.LocalDate;

public class ManageAttendanceUI extends JFrame{

    private final JFrame frame;
    private final Connection connecection;

    public ManageAttendanceUI() {
        connecection=DatabaseConnection.getConnection();
        frame = new JFrame("Manage Attendance");
        frame.setResizable(false);
        frame.setPreferredSize(new Dimension(600, 400));
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        initializeUI();
    }

    private void initializeUI() {
        // Content Pane
        JPanel contentPane = new JPanel(new BorderLayout());
        frame.setContentPane(contentPane);

        // Table
        String[] columnNames = {"Student ID", "Student Name", "Subject ID", "Subject Name", "Date", "Present"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        contentPane.add(scrollPane, BorderLayout.CENTER);

        loadAttendance(tableModel);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addButton = new JButton("Add");
        JButton editButton = new JButton("Edit");
        JButton deleteButton = new JButton("Delete");

        addButton.addActionListener(e -> {
                AddAttendanceUI addAttendanceUI = new AddAttendanceUI(tableModel);
                addAttendanceUI.setVisible(true);
        });
        
        editButton.addActionListener(new ActionListener(){
          

           @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(frame, "Please select an attendance to edit.", "Error", JOptionPane.ERROR_MESSAGE);
                   return;
                }
                int studentId = (int) table.getValueAt(selectedRow, 0);
                int subjectId = (int) table.getValueAt(selectedRow, 2);
                LocalDate date = (LocalDate) table.getValueAt(selectedRow, 4);
                
                EditAttendanceUI editAttendanceUI = new EditAttendanceUI(tableModel, studentId, subjectId, date);

                editAttendanceUI.setVisible(true);
            }
        });
        
        deleteButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(frame, "Please select an attendance to delete.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                int studentId = (int) table.getValueAt(selectedRow, 0);
                int subjectId = (int) table.getValueAt(selectedRow, 2);
                LocalDate date = (LocalDate) table.getValueAt(selectedRow, 4);

                AttendanceDAO attendanceDAO = new AttendanceDAOImpl(connecection);
                try {
                    attendanceDAO.deleteAttendance(studentId, subjectId, date);
                    tableModel.removeRow(selectedRow);
                }catch (AbstractDAO.NotFoundException ex){
                    JOptionPane.showMessageDialog(frame, "Error deleting attendance: Attendance not found ", "Error", JOptionPane.ERROR_MESSAGE);
                }catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Error deleting attendance: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);

                }

            }
        });

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        contentPane.add(buttonPanel, BorderLayout.SOUTH);

        frame.pack();
        frame.setLocationRelativeTo(null);
    }

    public void setVisible(boolean visible) {
        frame.setVisible(visible);
    }

    private void loadAttendance(DefaultTableModel tableModel){
        AttendanceDAO attendanceDAO = new AttendanceDAOImpl(connecection);
        StudentDAO studentDAO = new StudentDAOImpl(connecection);
        SubjectDAO subjectDAO = new SubjectDAOImpl(connecection);
        try{
            java.util.List<AttendanceDAO.Attendance> attendances = attendanceDAO.getAllAttendance();
            for(AttendanceDAO.Attendance attendance : attendances){
                String studentName = "N/A";
                String subjectName = "N/A";
                if (studentDAO.readStudent(attendance.getStudentId()) != null){
                    studentName = studentDAO.readStudent(attendance.getStudentId()).getName();
                }if (subjectDAO.readSubject(attendance.getSubjectId()) != null){
                    subjectName = subjectDAO.readSubject(attendance.getSubjectId()).getName();
                } 
                Object[] row = {attendance.getStudentId(),studentName, attendance.getSubjectId(), subjectName, attendance.getDate()};

                tableModel.addRow(row);
            }

        }catch (Exception e){
            e.printStackTrace();
             JOptionPane.showMessageDialog(frame, "Error loading attendance: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}