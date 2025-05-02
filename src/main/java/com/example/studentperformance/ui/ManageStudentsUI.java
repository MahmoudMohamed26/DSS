package com.example.studentperformance.ui;

import com.example.studentperformance.dao.StudentDAO;
import com.example.studentperformance.dao.StudentDAOImpl;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.util.List;

public class ManageStudentsUI {

    private JFrame frame;
    private Connection connection;

    public ManageStudentsUI(Connection connection) {
        this.connection = connection;
        frame = new JFrame("Manage Students");
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
        String[] columnNames = {"ID", "Name"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(table);
        contentPane.add(scrollPane, BorderLayout.CENTER);

        // load students to the table
        loadStudents(tableModel);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addButton = new JButton("Add");
        JButton editButton = new JButton("Edit");
        JButton deleteButton = new JButton("Delete");

        // Button actions
        addButton.addActionListener(e -> {
            AddStudentUI addStudentUI = new AddStudentUI(connection, tableModel);
            addStudentUI.setVisible(true);
        });

        editButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(frame, "Please select a student to edit.", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                int studentId = (int) table.getValueAt(selectedRow, 0);
                EditStudentUI editStudentUI = new EditStudentUI(connection, tableModel, studentId);
                editStudentUI.setVisible(true);
            }
        });

        deleteButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(frame, "Please select a student to delete.", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                int studentId = (int) table.getValueAt(selectedRow, 0);
                StudentDAO studentDAO = new StudentDAOImpl(connection);
                try {
                    studentDAO.deleteStudent(studentId);
                    tableModel.removeRow(selectedRow);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Error deleting student: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        contentPane.add(buttonPanel, BorderLayout.SOUTH);

        frame.pack();
    }

    private void loadStudents(DefaultTableModel tableModel) {
        StudentDAO studentDAO = new StudentDAOImpl(connection);
        List<StudentDAO.Student> students;
        try {
            students = studentDAO.getAllStudents();
            for (StudentDAO.Student student : students) {
                Object[] row = {student.getStudentId(), student.getName()};
                tableModel.addRow(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error loading students: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void setVisible(boolean visible) {
        frame.setVisible(visible);
    }
}