package com.example.studentperformance.ui;

import com.example.studentperformance.DatabaseConnection;
import com.example.studentperformance.dao.SubjectDAO;
import com.example.studentperformance.dao.SubjectDAOImpl;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;

public class EditSubjectUI {

    private final JFrame frame;
    private DefaultTableModel tableModel;
    private JTextField nameField;
    private int subjectId;

    public EditSubjectUI(Connection connection, DefaultTableModel tableModel, int subjectId) {
        this.tableModel = tableModel;
        this.subjectId = subjectId;
        frame = new JFrame("Edit Subject");
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

        SubjectDAO subjectDAO = new SubjectDAOImpl(DatabaseConnection.getConnection());
        try {
            SubjectDAO.Subject subject = subjectDAO.readSubject(subjectId);
            nameField.setText(subject.getName());
        } catch (Exception e) {       
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error getting subject data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
            }
        });

        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newName = nameField.getText();
                if (newName == null || newName.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Please enter a name", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                SubjectDAO subjectDAO = new SubjectDAOImpl(DatabaseConnection.getConnection());
                try {
                    subjectDAO.updateSubject(subjectId, newName);
                    for (int i = 0; i < tableModel.getRowCount(); i++) {
                        if ((int) tableModel.getValueAt(i, 0) == subjectId) {
                            tableModel.setValueAt(newName, i, 1);
                            break;
                        }
                    }
                    frame.dispose();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Error editing subject: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
               
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