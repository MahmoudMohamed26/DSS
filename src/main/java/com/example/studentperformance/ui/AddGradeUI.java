package com.example.studentperformance.ui;

import com.example.studentperformance.dao.GradeDAO;
import com.example.studentperformance.dao.GradeDAOImpl;
import com.example.studentperformance.dao.StudentDAO;
import com.example.studentperformance.dao.StudentDAOImpl;
import com.example.studentperformance.dao.SubjectDAO;
import com.example.studentperformance.dao.SubjectDAOImpl;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;

public class AddGradeUI {
    private Connection connection;
    private DefaultTableModel tableModel;
    private JTextField studentIdField;
    private JTextField subjectIdField;
    private JTextField gradeField;

    public AddGradeUI(Connection connection, DefaultTableModel tableModel) {
        this.connection = connection;
        this.tableModel = tableModel;
        initializeUI();
    }
    private JFrame frame;
    public void setVisible(boolean visible) {
        frame.setVisible(visible);
    }

    private void initializeUI() {
        JPanel panel = new JPanel(new GridLayout(4, 2));
        JLabel studentIdLabel = new JLabel("Student ID:");
        studentIdField = new JTextField();
        JLabel subjectIdLabel = new JLabel("Subject ID:");
        subjectIdField = new JTextField();
        JLabel gradeLabel = new JLabel("Grade:");
        gradeField = new JTextField();
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
                String studentIdText = studentIdField.getText().trim();
                String subjectIdText = subjectIdField.getText().trim();
                String gradeText = gradeField.getText().trim();

                if (studentIdText.isEmpty() || subjectIdText.isEmpty() || gradeText.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try{
                     int studentId = Integer.parseInt(studentIdText);
                     int subjectId = Integer.parseInt(subjectIdText);
                     int grade = Integer.parseInt(gradeText);

                     GradeDAO gradeDAO = new GradeDAOImpl(connection);
                     StudentDAO studentDAO = new StudentDAOImpl(connection);
                     SubjectDAO subjectDAO = new SubjectDAOImpl(connection);

                     String studentName = studentDAO.readStudent(studentId).getName();
                     String subjectName = subjectDAO.readSubject(subjectId).getName() ;

                      gradeDAO.createGrade(studentId, subjectId, grade);


                     Object[] row = {studentId, studentName, subjectId, subjectName, grade};




                     tableModel.addRow(row);
                     frame.dispose();
                }catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Please enter valid numeric values.", "Error", JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Error adding grade: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        panel.add(studentIdLabel);
        panel.add(studentIdField);
        panel.add(subjectIdLabel);
        panel.add(subjectIdField);
        panel.add(gradeLabel);
        panel.add(gradeField);
        panel.add(addButton);
        panel.add(cancelButton);
        frame.add(panel);
        frame.pack();
    }
    public AddGradeUI() {
        frame = new JFrame("Add Grade");
        frame.setResizable(false);
        frame.setPreferredSize(new Dimension(300, 200));
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        initializeUI();
    }

}