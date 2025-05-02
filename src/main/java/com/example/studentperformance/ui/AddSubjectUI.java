package com.example.studentperformance.ui;

import com.example.studentperformance.DatabaseConnection;
import com.example.studentperformance.dao.SubjectDAO;
import com.example.studentperformance.dao.SubjectDAOImpl;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;

public class AddSubjectUI {

    private final JFrame frame;
    private JTextField nameField;

    public AddSubjectUI(Connection connection, DefaultTableModel tableModel) {
        frame = new JFrame("Add Subject");
        frame.setResizable(false);
        frame.setPreferredSize(new Dimension(300, 150));
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        initializeUI();
    }

    private void initializeUI() {
        JPanel panel = new JPanel(new GridLayout(3, 2));
        JLabel nameLabel = new JLabel("Name:");
        nameField = new JTextField();
        JButton addButton = new JButton("Add");
        JButton cancelButton = new JButton("Cancel");

        cancelButton.addActionListener(e -> frame.dispose());

        addButton.addActionListener(e -> {
            String name = nameField.getText();
            if (name != null && !name.trim().isEmpty()) {
                SubjectDAO subjectDAO = new SubjectDAOImpl(DatabaseConnection.getConnection());
                try {
                    subjectDAO.createSubject(name);
                    // Optionally, you can refresh the table or notify the user here
                    frame.dispose();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Error adding subject: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Please enter a name", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
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

    public JFrame getFrame() {
        return frame;
    }
}