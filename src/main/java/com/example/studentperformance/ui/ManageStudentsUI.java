package com.example.studentperformance.ui;

import com.example.studentperformance.dao.StudentDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class ManageStudentsUI {
    // Constants for styling - matching ManageSubjectsUI theme
    private static final Color PRIMARY_COLOR = new Color(65, 105, 225);  // Royal Blue
    private static final Color SECONDARY_COLOR = new Color(70, 130, 180); // Steel Blue
    private static final Color BACKGROUND_COLOR = new Color(240, 248, 255); // Alice Blue
    private static final Color TEXT_COLOR = new Color(25, 25, 112); // Midnight Blue
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 20);
    private static final Font TABLE_HEADER_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font TABLE_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private static final int ANIMATION_DURATION = 200; // milliseconds

    private JFrame frame;
    private Connection connection;
    private JLabel statusLabel;
    private JLabel countLabel;
    private JTable studentsTable;
    private DefaultTableModel tableModel;
    private StudentDAO studentDAO;
    private JTextField searchField;

    public ManageStudentsUI(Connection connection, StudentDAO studentDAO) {
        this.connection = connection;
        this.studentDAO = studentDAO;

        frame = new JFrame("Manage Students");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(new Dimension(800, 600));

        initializeUI();
    }

    private void initializeUI() {
        // Main container with gradient background
        JPanel contentPane = new JPanel(new BorderLayout(10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                int w = getWidth();
                int h = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, BACKGROUND_COLOR, 0, h, new Color(230, 240, 250));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
                g2d.dispose();
            }
        };
        contentPane.setBorder(new EmptyBorder(20, 20, 20, 20));
        frame.setContentPane(contentPane);

        // Header panel with title
        JPanel headerPanel = createHeaderPanel();
        contentPane.add(headerPanel, BorderLayout.NORTH);

        // Create status panel BEFORE table panel to initialize statusLabel
        JPanel statusPanel = createStatusPanel();
        contentPane.add(statusPanel, BorderLayout.SOUTH);

        // Create table panel
        JPanel tablePanel = createTablePanel();
        contentPane.add(tablePanel, BorderLayout.CENTER);

        // Create button panel
        JPanel buttonPanel = createButtonPanel();
        contentPane.add(buttonPanel, BorderLayout.SOUTH);

        // Center on screen
        frame.setLocationRelativeTo(null);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        // Title
        JLabel titleLabel = new JLabel("Student Management");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        // Current date on the right (optional)
        JLabel dateLabel = new JLabel(new java.text.SimpleDateFormat("EEEE, MMMM d, yyyy").format(new java.util.Date()));
        dateLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        dateLabel.setForeground(TEXT_COLOR);
        headerPanel.add(dateLabel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout(0, 10));
        tablePanel.setOpaque(false);
        tablePanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        // Create the table model with columns
        String[] columnNames = {"ID", "Name"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            // Make cells not editable directly in the table
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Create and style the table
        studentsTable = new JTable(tableModel);
        studentsTable.setFont(TABLE_FONT);
        studentsTable.setRowHeight(25);
        studentsTable.setSelectionBackground(new Color(PRIMARY_COLOR.getRed(), PRIMARY_COLOR.getGreen(), PRIMARY_COLOR.getBlue(), 100));
        studentsTable.setSelectionForeground(TEXT_COLOR);
        studentsTable.setShowGrid(true);
        studentsTable.setGridColor(new Color(230, 230, 250));
        studentsTable.setFocusable(false);

        // Style the table header
        JTableHeader header = studentsTable.getTableHeader();
        header.setFont(TABLE_HEADER_FONT);
        header.setBackground(PRIMARY_COLOR);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 35));

        // Set column widths
        studentsTable.getColumnModel().getColumn(0).setPreferredWidth(80);    // ID - narrow
        studentsTable.getColumnModel().getColumn(1).setPreferredWidth(300);   // Name - wide

        // Add double-click functionality for editing
        studentsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedRow = studentsTable.getSelectedRow();
                    if (selectedRow != -1) {
                        int studentId = (int) studentsTable.getValueAt(selectedRow, 0);
                        openEditStudentDialog(studentId);
                    }
                }
            }
        });

        // Add scroll pane with custom styling
        JScrollPane scrollPane = new JScrollPane(studentsTable);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5, 5, 5, 5),
                BorderFactory.createLineBorder(SECONDARY_COLOR, 1, true)
        ));

        // Search panel at the top
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setOpaque(false);
        JLabel searchLabel = new JLabel("Search Student:");
        searchLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchLabel.setForeground(TEXT_COLOR);

        searchField = new JTextField(20);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Add listener to search as you type
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                searchStudents();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                searchStudents();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                searchStudents();
            }
        });

        JButton searchButton = createStyledButton("Search", e -> searchStudents());

        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        // Add components to the panel
        tablePanel.add(searchPanel, BorderLayout.NORTH);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // Load initial data - statusLabel is now initialized
        loadStudents();

        return tablePanel;
    }

    private void searchStudents() {
        String searchTerm = searchField.getText().trim().toLowerCase();
        statusLabel.setText("Searching for: " + searchTerm);

        if (searchTerm.isEmpty()) {
            loadStudents(); // Load all students if search field is empty
            return;
        }

        try {
            List<StudentDAO.Student> allStudents = studentDAO.getAllStudents();
            List<StudentDAO.Student> filteredStudents = new ArrayList<>();

            // Filter students based on search term
            for (StudentDAO.Student student : allStudents) {
                if (student.getName().toLowerCase().contains(searchTerm) ||
                        String.valueOf(student.getStudentId()).contains(searchTerm)) {
                    filteredStudents.add(student);
                }
            }

            // Update table with filtered results
            updateTableWithStudents(filteredStudents);
            statusLabel.setText("Found " + filteredStudents.size() + " matching students");

        } catch (Exception ex) {
            ex.printStackTrace();
            showErrorMessage("Error searching students: " + ex.getMessage());
        }
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JButton addButton = createStyledButton("Add Student", e -> openAddStudentDialog());
        JButton editButton = createStyledButton("Edit Student", e -> {
            int selectedRow = studentsTable.getSelectedRow();
            if (selectedRow == -1) {
                showErrorMessage("Please select a student to edit.");
            } else {
                int studentId = (int) studentsTable.getValueAt(selectedRow, 0);
                openEditStudentDialog(studentId);
            }
        });

        JButton deleteButton = createStyledButton("Delete Student", e -> deleteSelectedStudent());
        JButton refreshButton = createStyledButton("Refresh", e -> loadStudents());
        JButton closeButton = createStyledButton("Close", e -> frame.dispose());

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(closeButton);

        return buttonPanel;
    }

    // Modified to use a dialog instead of a separate frame
    private void openAddStudentDialog() {
        statusLabel.setText("Opening add student dialog...");

        // Create a custom dialog
        JDialog addDialog = new JDialog(frame, "Add Student", true);
        addDialog.setLayout(new BorderLayout());
        addDialog.setSize(400, 200);
        addDialog.setLocationRelativeTo(frame);

        // Create a panel with a gradient background
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                int w = getWidth();
                int h = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, BACKGROUND_COLOR, 0, h, new Color(230, 240, 250));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
                g2d.dispose();
            }
        };
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Create form panel
        JPanel formPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        formPanel.setOpaque(false);

        JLabel nameLabel = new JLabel("Student Name:");
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nameLabel.setForeground(TEXT_COLOR);

        JTextField nameField = new JTextField();
        nameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        formPanel.add(nameLabel);
        formPanel.add(nameField);

        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);

        JButton saveButton = createStyledButton("Save", e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(addDialog, "Please enter a student name.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                // Save the new student using createStudent
                studentDAO.createStudent(name);

                // Refresh the table
                loadStudents();
                statusLabel.setText("Student added successfully");

                // Close the dialog
                addDialog.dispose();
            } catch (Exception ex) {
                ex.printStackTrace();
                showErrorMessage("Error adding student: " + ex.getMessage());
            }
        });

        JButton cancelButton = createStyledButton("Cancel", e -> addDialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        // Add components to panel
        panel.add(new JLabel("Add New Student", SwingConstants.CENTER), BorderLayout.NORTH);
        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        // Add panel to dialog
        addDialog.add(panel);

        // Show dialog
        addDialog.setVisible(true);
    }

    // Modified to use a dialog instead of a separate frame
    private void openEditStudentDialog(int studentId) {
        statusLabel.setText("Opening edit dialog for Student ID: " + studentId);

        try {
            // Fetch the student from the database using readStudent
            StudentDAO.Student student = studentDAO.readStudent(studentId);

            if (student == null) {
                showErrorMessage("Student not found with ID: " + studentId);
                return;
            }

            // Create a custom dialog
            JDialog editDialog = new JDialog(frame, "Edit Student", true);
            editDialog.setLayout(new BorderLayout());
            editDialog.setSize(400, 200);
            editDialog.setLocationRelativeTo(frame);

            // Create a panel with a gradient background
            JPanel panel = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                    int w = getWidth();
                    int h = getHeight();
                    GradientPaint gp = new GradientPaint(0, 0, BACKGROUND_COLOR, 0, h, new Color(230, 240, 250));
                    g2d.setPaint(gp);
                    g2d.fillRect(0, 0, w, h);
                    g2d.dispose();
                }
            };
            panel.setBorder(new EmptyBorder(20, 20, 20, 20));

            // Create form panel
            JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 10));
            formPanel.setOpaque(false);

            JLabel idLabel = new JLabel("Student ID:");
            idLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            idLabel.setForeground(TEXT_COLOR);

            JLabel idValueLabel = new JLabel(String.valueOf(studentId));
            idValueLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            idValueLabel.setForeground(TEXT_COLOR);

            JLabel nameLabel = new JLabel("Student Name:");
            nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            nameLabel.setForeground(TEXT_COLOR);

            JTextField nameField = new JTextField(student.getName());
            nameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));

            formPanel.add(idLabel);
            formPanel.add(idValueLabel);
            formPanel.add(nameLabel);
            formPanel.add(nameField);

            // Create button panel
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.setOpaque(false);

            JButton updateButton = createStyledButton("Update", e -> {
                String name = nameField.getText().trim();
                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(editDialog, "Please enter a student name.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    // Update the student using updateStudent
                    studentDAO.updateStudent(studentId, name);

                    // Refresh the table
                    loadStudents();
                    statusLabel.setText("Student updated successfully");

                    // Close the dialog
                    editDialog.dispose();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showErrorMessage("Error updating student: " + ex.getMessage());
                }
            });

            JButton cancelButton = createStyledButton("Cancel", e -> editDialog.dispose());

            buttonPanel.add(updateButton);
            buttonPanel.add(cancelButton);

            // Add components to panel
            panel.add(new JLabel("Edit Student", SwingConstants.CENTER), BorderLayout.NORTH);
            panel.add(formPanel, BorderLayout.CENTER);
            panel.add(buttonPanel, BorderLayout.SOUTH);

            // Add panel to dialog
            editDialog.add(panel);

            // Show dialog
            editDialog.setVisible(true);

        } catch (Exception ex) {
            ex.printStackTrace();
            showErrorMessage("Error retrieving student data: " + ex.getMessage());
        }
    }

    private void deleteSelectedStudent() {
        int selectedRow = studentsTable.getSelectedRow();
        if (selectedRow == -1) {
            showErrorMessage("Please select a student to delete.");
            return;
        }

        int studentId = (int) studentsTable.getValueAt(selectedRow, 0);
        String studentName = (String) studentsTable.getValueAt(selectedRow, 1);

        // Confirmation dialog with student name
        int response = JOptionPane.showConfirmDialog(
                frame,
                "Are you sure you want to delete the student: " + studentName + "?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (response == JOptionPane.YES_OPTION) {
            try {
                // Delete student using deleteStudent
                studentDAO.deleteStudent(studentId);

                // Remove from table model
                tableModel.removeRow(selectedRow);
                statusLabel.setText("Student deleted successfully");

                // Update count label
                updateCountLabel();
            } catch (Exception ex) {
                ex.printStackTrace();
                showErrorMessage("Error deleting student: " + ex.getMessage());
            }
        }
    }

    private JButton createStyledButton(String text, ActionListener action) {
        JButton button = new JButton(text) {
            // Custom rendering for rounded corners and gradient
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int width = getWidth();
                int height = getHeight();

                // Draw gradient background
                GradientPaint gradient = new GradientPaint(
                        0, 0, PRIMARY_COLOR,
                        0, height, SECONDARY_COLOR
                );
                g2.setPaint(gradient);
                g2.fill(new RoundRectangle2D.Float(0, 0, width, height, 12, 12));

                // Add subtle shadow effect
                g2.setColor(new Color(0, 0, 0, 30));
                g2.fill(new RoundRectangle2D.Float(2, 2, width, height, 12, 12));

                g2.dispose();
                super.paintComponent(g);
            }
        };

        // Make button transparent so our custom painting shows
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setOpaque(false);

        // Set text properties
        button.setFont(BUTTON_FONT);
        button.setForeground(Color.WHITE);
        button.setPreferredSize(new Dimension(130, 36));

        // Add the action listener
        button.addActionListener(action);

        // Add hover effects
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
                button.setFont(new Font(button.getFont().getName(), Font.BOLD, button.getFont().getSize()));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                button.setFont(BUTTON_FONT);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                button.setFont(new Font(button.getFont().getName(), Font.BOLD, button.getFont().getSize() - 1));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                button.setFont(BUTTON_FONT);
            }
        });

        return button;
    }

    private JPanel createStatusPanel() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(new Color(240, 240, 240));
        statusPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        statusPanel.setName("statusPanel"); // Set a name to find it later

        statusLabel = new JLabel("Ready");
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));

        countLabel = new JLabel("Total Students: 0");
        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        countLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(countLabel, BorderLayout.EAST);

        return statusPanel;
    }

    private void loadStudents() {
        statusLabel.setText("Loading students...");

        try {
            List<StudentDAO.Student> students = studentDAO.getAllStudents();
            updateTableWithStudents(students);
            statusLabel.setText("Students loaded successfully");
        } catch (Exception e) {
            e.printStackTrace();
            showErrorMessage("Error loading students: " + e.getMessage());
        }
    }

    private void updateTableWithStudents(List<StudentDAO.Student> students) {
        // Clear existing rows
        tableModel.setRowCount(0);

        // Update the count label
        countLabel.setText("Total Students: " + students.size());

        // Populate table with data - only ID and Name
        for (StudentDAO.Student student : students) {
            Object[] row = {
                    student.getStudentId(),
                    student.getName()
            };
            tableModel.addRow(row);
        }
    }

    private void updateCountLabel() {
        countLabel.setText("Total Students: " + tableModel.getRowCount());
    }

    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(
                frame,
                message,
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    public void setVisible(boolean visible) {
        frame.setVisible(visible);
    }

    public JFrame getFrame() {
        return frame;
    }

    // Method to get the current StudentDAO
    public StudentDAO getStudentDAO() {
        return studentDAO;
    }

    // Method to set a new StudentDAO implementation
    public void setStudentDAO(StudentDAO studentDAO) {
        this.studentDAO = studentDAO;
        // Reload students with the new DAO
        loadStudents();
    }
}