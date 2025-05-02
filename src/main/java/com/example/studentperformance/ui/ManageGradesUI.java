package com.example.studentperformance.ui;

import com.example.studentperformance.dao.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class ManageGradesUI implements ActionListener {

    private JFrame frame;
    private GradeDAO gradeDAO;
    private StudentDAO studentDAO;
    private SubjectDAO subjectDAO;

    public ManageGradesUI(Connection connection) {
        frame = new JFrame("Manage Grades");
        this.gradeDAO = new GradeDAOImpl(connection);
        this.studentDAO = new StudentDAOImpl(connection);
        this.subjectDAO = new SubjectDAOImpl(connection);
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
        String[] columnNames = {"Student ID", "Student Name", "Subject ID", "Subject Name", "Grade"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        contentPane.add(scrollPane, BorderLayout.CENTER);

        // Load grades to the table
        loadGrades(tableModel);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addButton = new JButton("Add");
        JButton editButton = new JButton("Edit");
        JButton deleteButton = new JButton("Delete");

        // Button actions
        addButton.addActionListener(e -> {
            new AddGradeUI(gradeDAO.getConnection(), tableModel);
            System.out.println("Add Grade");
        });
        editButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(frame, "Please select a grade to edit", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                int studentId = (int) table.getValueAt(selectedRow, 0);
                int subjectId = (int) table.getValueAt(selectedRow, 2);

                EditGradeUI editGradeUI = new EditGradeUI( tableModel, studentId, subjectId);
                editGradeUI.setVisible(true);
            }
        });
        deleteButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(frame, "Please select a grade to delete.", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                int studentId = (int) table.getValueAt(selectedRow, 0);
                int subjectId = (int) table.getValueAt(selectedRow, 2);
                try {
                    gradeDAO.deleteGrade(studentId, subjectId);
                    tableModel.removeRow(selectedRow);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Error deleting grade: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Error deleting grade: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        contentPane.add(buttonPanel, BorderLayout.SOUTH);

        frame.pack();
    }

    public void setVisible(boolean visible) {
        frame.setVisible(visible);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Empty implementation since we're using lambda expressions
    }

    private void loadGrades(DefaultTableModel tableModel) {
        try {
            List<GradeDAO.Grade> grades = gradeDAO.getAllGrades();
            for (GradeDAO.Grade grade : grades) {
                StudentDAO.Student student = studentDAO.readStudent(grade.getStudentId());
                SubjectDAO.Subject subject = subjectDAO.readSubject(grade.getSubjectId());
                Object[] row = {student.getStudentId(), student.getName(),
                        subject.getSubjectId(), subject.getName(),
                        grade.getGrade()};
                tableModel.addRow(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error loading grades: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
