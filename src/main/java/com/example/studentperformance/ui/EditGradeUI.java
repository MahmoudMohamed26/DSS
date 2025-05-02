package com.example.studentperformance.ui;

import com.example.studentperformance.DatabaseConnection;
import com.example.studentperformance.dao.GradeDAO;
import com.example.studentperformance.dao.GradeDAOImpl;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;

public class EditGradeUI {

    private JFrame frame;
    private Connection connection;
    private DefaultTableModel tableModel;
    private JTextField gradeField;
    private int studentId;
    private int subjectId;

    public EditGradeUI(DefaultTableModel tableModel, int studentId, int subjectId) {
        this.connection = DatabaseConnection.getConnection();
        this.tableModel = tableModel;
        this.studentId = studentId;
        this.subjectId = subjectId;
        frame = new JFrame("Edit Grade");
        frame.setResizable(false);
        frame.setPreferredSize(new Dimension(300, 150));
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        initializeUI();
    }
    

    private void initializeUI() {
        JPanel panel = new JPanel(new GridLayout(3, 2));
        JLabel gradeLabel = new JLabel("Grade:");
        gradeField = new JTextField();
        JButton editButton = new JButton("Edit");
        JButton cancelButton = new JButton("Cancel");

        // Load grade data
        GradeDAO gradeDAO = new GradeDAOImpl(connection);
        try {
            GradeDAO.Grade grade = gradeDAO.readGrade(studentId, subjectId);
            gradeField.setText(String.valueOf(grade.getGrade()));
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error getting grade data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        cancelButton.addActionListener(e -> frame.dispose());

        editButton.addActionListener(e -> {
            String gradeText = gradeField.getText();
            if (gradeText == null || gradeText.trim().isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please enter a grade", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                double gradeValue = Double.parseDouble(gradeText);
                GradeDAO gradeDAO1 = new GradeDAOImpl(connection);
                gradeDAO1.updateGrade(studentId, subjectId, gradeValue);

                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    if ((int) tableModel.getValueAt(i, 0) == studentId && 
                        (int) tableModel.getValueAt(i, 2) == subjectId) {
                        tableModel.setValueAt(gradeValue, i, 4);
                        break;
                    }
                }
                frame.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Invalid grade format", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Error updating grade: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(gradeLabel);
        panel.add(gradeField);
        panel.add(editButton);
        panel.add(cancelButton);
        frame.add(panel);
        frame.pack();
    }

    public void setVisible(boolean visible) {
        frame.setVisible(visible);
    }
}