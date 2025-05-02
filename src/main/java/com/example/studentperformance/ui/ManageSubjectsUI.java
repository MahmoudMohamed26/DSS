package com.example.studentperformance.ui;

import com.example.studentperformance.dao.SubjectDAO;
import com.example.studentperformance.DatabaseConnection;
import com.example.studentperformance.dao.SubjectDAOImpl;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

import java.util.List;
import java.util.ArrayList;

public class ManageSubjectsUI {

    private JFrame frame;

    public ManageSubjectsUI() {
        frame = new JFrame("Manage Subjects");
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

        // Load subjects to the table
        loadSubjects(tableModel);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addButton = new JButton("Add");
        JButton editButton = new JButton("Edit");
        JButton deleteButton = new JButton("Delete");

        // Button actions
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AddSubjectUI addSubjectUI = new AddSubjectUI(DatabaseConnection.getConnection(), tableModel);
                JFrame frame = addSubjectUI.getFrame();
                frame.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosed(java.awt.event.WindowEvent e) {
                        tableModel.setRowCount(0);
                        loadSubjects(tableModel);
                    }
                });
                addSubjectUI.setVisible(true);
            }
        });

        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JTable table = (JTable) scrollPane.getViewport().getView();
                int selectedRow = table.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(frame, "Please select a Subject to edit.", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    int subjectId = (int) table.getValueAt(selectedRow, 0);
                    EditSubjectUI editSubjectUI = new EditSubjectUI(DatabaseConnection.getConnection(), tableModel, subjectId);
                    editSubjectUI.setVisible(true);
                }
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JTable table = (JTable) scrollPane.getViewport().getView();
                int selectedRow = table.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(frame, "Please select a subject to delete.", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    int subjectId = (int) table.getValueAt(selectedRow, 0);
                    SubjectDAO subjectDAO = new SubjectDAOImpl(DatabaseConnection.getConnection());
                    try {
                        subjectDAO.deleteSubject(subjectId);
                        tableModel.removeRow(selectedRow);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(frame, "Error deleting subject: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
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

    private void loadSubjects(DefaultTableModel tableModel) {
        SubjectDAO subjectDAO = new SubjectDAOImpl(DatabaseConnection.getConnection());
        List<SubjectDAO.Subject> subjects;
        try {
            subjects = new ArrayList<>(subjectDAO.getAllSubjects());
            for (SubjectDAO.Subject subject : subjects) {
                Object[] row = {subject.getSubjectId(), subject.getName()};
                tableModel.addRow(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error loading subjects: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
