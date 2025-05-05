package com.example.studentperformance.ui;

import com.example.studentperformance.dao.*;

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

public class ManageGradesUI {
    // Constants for styling - matching ManageStudentsUI theme
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
    private JTable gradesTable;
    private DefaultTableModel tableModel;
    private GradeDAO gradeDAO;
    private StudentDAO studentDAO;
    private SubjectDAO subjectDAO;
    private JTextField searchField;

    public ManageGradesUI(Connection connection) {
        this.connection = connection;
        this.gradeDAO = new GradeDAOImpl(connection);
        this.studentDAO = new StudentDAOImpl(connection);
        this.subjectDAO = new SubjectDAOImpl(connection);

        frame = new JFrame("Manage Grades");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(new Dimension(900, 600));

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
        JLabel titleLabel = new JLabel("Grade Management");
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
        String[] columnNames = {"Student ID", "Student Name", "Subject ID", "Subject Name", "Grade"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            // Make cells not editable directly in the table
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Create and style the table
        gradesTable = new JTable(tableModel);
        gradesTable.setFont(TABLE_FONT);
        gradesTable.setRowHeight(25);
        gradesTable.setSelectionBackground(new Color(PRIMARY_COLOR.getRed(), PRIMARY_COLOR.getGreen(), PRIMARY_COLOR.getBlue(), 100));
        gradesTable.setSelectionForeground(TEXT_COLOR);
        gradesTable.setShowGrid(true);
        gradesTable.setGridColor(new Color(230, 230, 250));
        gradesTable.setFocusable(false);

        // Style the table header
        JTableHeader header = gradesTable.getTableHeader();
        header.setFont(TABLE_HEADER_FONT);
        header.setBackground(PRIMARY_COLOR);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 35));

        // Set column widths
        gradesTable.getColumnModel().getColumn(0).setPreferredWidth(80);    // Student ID - narrow
        gradesTable.getColumnModel().getColumn(1).setPreferredWidth(200);   // Student Name - wide
        gradesTable.getColumnModel().getColumn(2).setPreferredWidth(80);    // Subject ID - narrow
        gradesTable.getColumnModel().getColumn(3).setPreferredWidth(200);   // Subject Name - wide
        gradesTable.getColumnModel().getColumn(4).setPreferredWidth(80);    // Grade - narrow

        // Add double-click functionality for editing
        gradesTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedRow = gradesTable.getSelectedRow();
                    if (selectedRow != -1) {
                        int studentId = (int) gradesTable.getValueAt(selectedRow, 0);
                        int subjectId = (int) gradesTable.getValueAt(selectedRow, 2);
                        openEditGradeDialog(studentId, subjectId);
                    }
                }
            }
        });

        // Add scroll pane with custom styling
        JScrollPane scrollPane = new JScrollPane(gradesTable);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5, 5, 5, 5),
                BorderFactory.createLineBorder(SECONDARY_COLOR, 1, true)
        ));

        // Search panel at the top
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setOpaque(false);
        JLabel searchLabel = new JLabel("Search Grade:");
        searchLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchLabel.setForeground(TEXT_COLOR);

        searchField = new JTextField(20);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Add listener to search as you type
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                searchGrades();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                searchGrades();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                searchGrades();
            }
        });

        JButton searchButton = createStyledButton("Search", e -> searchGrades());

        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        // Add components to the panel
        tablePanel.add(searchPanel, BorderLayout.NORTH);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // Load initial data - statusLabel is now initialized
        loadGrades();

        return tablePanel;
    }

    private void searchGrades() {
        String searchTerm = searchField.getText().trim().toLowerCase();
        statusLabel.setText("Searching for: " + searchTerm);

        if (searchTerm.isEmpty()) {
            loadGrades(); // Load all grades if search field is empty
            return;
        }

        try {
            List<GradeDAO.Grade> allGrades = gradeDAO.getAllGrades();
            List<GradeDAO.Grade> filteredGrades = new ArrayList<>();

            // Filter grades based on search term
            for (GradeDAO.Grade grade : allGrades) {
                StudentDAO.Student student = studentDAO.readStudent(grade.getStudentId());
                SubjectDAO.Subject subject = subjectDAO.readSubject(grade.getSubjectId());

                if (String.valueOf(grade.getStudentId()).contains(searchTerm) ||
                        String.valueOf(grade.getSubjectId()).contains(searchTerm) ||
                        String.valueOf(grade.getGrade()).contains(searchTerm) ||
                        student.getName().toLowerCase().contains(searchTerm) ||
                        subject.getName().toLowerCase().contains(searchTerm)) {
                    filteredGrades.add(grade);
                }
            }

            // Update table with filtered results
            updateTableWithGrades(filteredGrades);
            statusLabel.setText("Found " + filteredGrades.size() + " matching grades");

        } catch (Exception ex) {
            ex.printStackTrace();
            showErrorMessage("Error searching grades: " + ex.getMessage());
        }
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JButton addButton = createStyledButton("Add Grade", e -> openAddGradeDialog());
        JButton editButton = createStyledButton("Edit Grade", e -> {
            int selectedRow = gradesTable.getSelectedRow();
            if (selectedRow == -1) {
                showErrorMessage("Please select a grade to edit.");
            } else {
                int studentId = (int) gradesTable.getValueAt(selectedRow, 0);
                int subjectId = (int) gradesTable.getValueAt(selectedRow, 2);
                openEditGradeDialog(studentId, subjectId);
            }
        });

        JButton deleteButton = createStyledButton("Delete Grade", e -> deleteSelectedGrade());
        JButton refreshButton = createStyledButton("Refresh", e -> loadGrades());
        JButton closeButton = createStyledButton("Close", e -> frame.dispose());

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(closeButton);

        return buttonPanel;
    }

    private void openAddGradeDialog() {
        statusLabel.setText("Opening add grade dialog...");

        // Create a custom dialog
        JDialog addDialog = new JDialog(frame, "Add Grade", true);
        addDialog.setLayout(new BorderLayout());
        addDialog.setSize(450, 250);
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
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        formPanel.setOpaque(false);

        // Student ComboBox
        JLabel studentLabel = new JLabel("Student:");
        studentLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        studentLabel.setForeground(TEXT_COLOR);

        JComboBox<ComboItem> studentComboBox = new JComboBox<>();
        studentComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Subject ComboBox
        JLabel subjectLabel = new JLabel("Subject:");
        subjectLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        subjectLabel.setForeground(TEXT_COLOR);

        JComboBox<ComboItem> subjectComboBox = new JComboBox<>();
        subjectComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Grade field
        JLabel gradeLabel = new JLabel("Grade:");
        gradeLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gradeLabel.setForeground(TEXT_COLOR);

        JTextField gradeField = new JTextField();
        gradeField.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Populate ComboBoxes
        try {
            List<StudentDAO.Student> students = studentDAO.getAllStudents();
            for (StudentDAO.Student student : students) {
                studentComboBox.addItem(new ComboItem(student.getStudentId(), student.getName()));
            }

            List<SubjectDAO.Subject> subjects = subjectDAO.getAllSubjects();
            for (SubjectDAO.Subject subject : subjects) {
                subjectComboBox.addItem(new ComboItem(subject.getSubjectId(), subject.getName()));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showErrorMessage("Error loading students or subjects: " + ex.getMessage());
        }

        formPanel.add(studentLabel);
        formPanel.add(studentComboBox);
        formPanel.add(subjectLabel);
        formPanel.add(subjectComboBox);
        formPanel.add(gradeLabel);
        formPanel.add(gradeField);

        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);

        JButton saveButton = createStyledButton("Save", e -> {
            try {
                ComboItem selectedStudent = (ComboItem) studentComboBox.getSelectedItem();
                ComboItem selectedSubject = (ComboItem) subjectComboBox.getSelectedItem();
                String gradeText = gradeField.getText().trim();

                if (selectedStudent == null || selectedSubject == null || gradeText.isEmpty()) {
                    JOptionPane.showMessageDialog(addDialog, "Please fill all fields.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Parse and validate grade
                double grade = Double.parseDouble(gradeText);
                if (grade < 0 || grade > 100) { // Assuming grades are between 0-100
                    JOptionPane.showMessageDialog(addDialog, "Grade must be between 0 and 100.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Check if grade already exists
                try {
                    GradeDAO.Grade existingGrade = gradeDAO.readGrade(selectedStudent.getId(), selectedSubject.getId());
                    if (existingGrade != null) {
                        JOptionPane.showMessageDialog(addDialog, "A grade already exists for this student and subject.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } catch (Exception ignored) {
                    // Exception means grade doesn't exist, which is what we want
                }

                // Save the new grade
                gradeDAO.createGrade(selectedStudent.getId(), selectedSubject.getId(), grade);

                // Refresh the table
                loadGrades();
                statusLabel.setText("Grade added successfully");

                // Close the dialog
                addDialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(addDialog, "Please enter a valid numeric grade.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                showErrorMessage("Error adding grade: " + ex.getMessage());
            }
        });

        JButton cancelButton = createStyledButton("Cancel", e -> addDialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        // Add components to panel
        panel.add(new JLabel("Add New Grade", SwingConstants.CENTER), BorderLayout.NORTH);
        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        // Add panel to dialog
        addDialog.add(panel);

        // Show dialog
        addDialog.setVisible(true);
    }

    private void openEditGradeDialog(int studentId, int subjectId) {
        statusLabel.setText("Opening edit dialog for Student ID: " + studentId + ", Subject ID: " + subjectId);

        try {
            // Fetch the grade from the database using readGrade
            GradeDAO.Grade grade = gradeDAO.readGrade(studentId, subjectId);
            StudentDAO.Student student = studentDAO.readStudent(studentId);
            SubjectDAO.Subject subject = subjectDAO.readSubject(subjectId);

            if (grade == null) {
                showErrorMessage("Grade not found for Student ID: " + studentId + " and Subject ID: " + subjectId);
                return;
            }

            // Create a custom dialog
            JDialog editDialog = new JDialog(frame, "Edit Grade", true);
            editDialog.setLayout(new BorderLayout());
            editDialog.setSize(450, 250);
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
            JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
            formPanel.setOpaque(false);

            // Student Info (non-editable)
            JLabel studentLabel = new JLabel("Student:");
            studentLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            studentLabel.setForeground(TEXT_COLOR);

            JLabel studentValueLabel = new JLabel(student.getName() + " (ID: " + student.getStudentId() + ")");
            studentValueLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            studentValueLabel.setForeground(TEXT_COLOR);

            // Subject Info (non-editable)
            JLabel subjectLabel = new JLabel("Subject:");
            subjectLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            subjectLabel.setForeground(TEXT_COLOR);

            JLabel subjectValueLabel = new JLabel(subject.getName() + " (ID: " + subject.getSubjectId() + ")");
            subjectValueLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            subjectValueLabel.setForeground(TEXT_COLOR);

            // Grade (editable)
            JLabel gradeLabel = new JLabel("Grade:");
            gradeLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            gradeLabel.setForeground(TEXT_COLOR);

            JTextField gradeField = new JTextField(String.valueOf(grade.getGrade()));
            gradeField.setFont(new Font("Segoe UI", Font.PLAIN, 14));

            formPanel.add(studentLabel);
            formPanel.add(studentValueLabel);
            formPanel.add(subjectLabel);
            formPanel.add(subjectValueLabel);
            formPanel.add(gradeLabel);
            formPanel.add(gradeField);

            // Create button panel
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.setOpaque(false);

            JButton updateButton = createStyledButton("Update", e -> {
                String gradeText = gradeField.getText().trim();
                if (gradeText.isEmpty()) {
                    JOptionPane.showMessageDialog(editDialog, "Please enter a grade.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    // Parse and validate grade
                    double newGrade = Double.parseDouble(gradeText);
                    if (newGrade < 0 || newGrade > 100) { // Assuming grades are between 0-100
                        JOptionPane.showMessageDialog(editDialog, "Grade must be between 0 and 100.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // Update the grade
                    gradeDAO.updateGrade(studentId, subjectId, newGrade);

                    // Refresh the table
                    loadGrades();
                    statusLabel.setText("Grade updated successfully");

                    // Close the dialog
                    editDialog.dispose();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(editDialog, "Please enter a valid numeric grade.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showErrorMessage("Error updating grade: " + ex.getMessage());
                }
            });

            JButton cancelButton = createStyledButton("Cancel", e -> editDialog.dispose());

            buttonPanel.add(updateButton);
            buttonPanel.add(cancelButton);

            // Add components to panel
            panel.add(new JLabel("Edit Grade", SwingConstants.CENTER), BorderLayout.NORTH);
            panel.add(formPanel, BorderLayout.CENTER);
            panel.add(buttonPanel, BorderLayout.SOUTH);

            // Add panel to dialog
            editDialog.add(panel);

            // Show dialog
            editDialog.setVisible(true);

        } catch (Exception ex) {
            ex.printStackTrace();
            showErrorMessage("Error retrieving grade data: " + ex.getMessage());
        }
    }

    private void deleteSelectedGrade() {
        int selectedRow = gradesTable.getSelectedRow();
        if (selectedRow == -1) {
            showErrorMessage("Please select a grade to delete.");
            return;
        }

        int studentId = (int) gradesTable.getValueAt(selectedRow, 0);
        int subjectId = (int) gradesTable.getValueAt(selectedRow, 2);
        String studentName = (String) gradesTable.getValueAt(selectedRow, 1);
        String subjectName = (String) gradesTable.getValueAt(selectedRow, 3);

        // Confirmation dialog with grade details
        int response = JOptionPane.showConfirmDialog(
                frame,
                "Are you sure you want to delete the grade for student: " + studentName +
                        " in subject: " + subjectName + "?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (response == JOptionPane.YES_OPTION) {
            try {
                // Delete grade using deleteGrade
                gradeDAO.deleteGrade(studentId, subjectId);

                // Remove from table model
                tableModel.removeRow(selectedRow);
                statusLabel.setText("Grade deleted successfully");

                // Update count label
                updateCountLabel();
            } catch (Exception ex) {
                ex.printStackTrace();
                showErrorMessage("Error deleting grade: " + ex.getMessage());
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

        countLabel = new JLabel("Total Grades: 0");
        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        countLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(countLabel, BorderLayout.EAST);

        return statusPanel;
    }

    private void loadGrades() {
        statusLabel.setText("Loading grades...");

        try {
            List<GradeDAO.Grade> grades = gradeDAO.getAllGrades();
            updateTableWithGrades(grades);
            statusLabel.setText("Grades loaded successfully");
        } catch (Exception e) {
            e.printStackTrace();
            showErrorMessage("Error loading grades: " + e.getMessage());
        }
    }

    private void updateTableWithGrades(List<GradeDAO.Grade> grades) {
        // Clear existing rows
        tableModel.setRowCount(0);

        try {
            // Update the count label
            countLabel.setText("Total Grades: " + grades.size());

            // Populate table with data
            for (GradeDAO.Grade grade : grades) {
                StudentDAO.Student student = studentDAO.readStudent(grade.getStudentId());
                SubjectDAO.Subject subject = subjectDAO.readSubject(grade.getSubjectId());

                Object[] row = {
                        grade.getStudentId(),
                        student.getName(),
                        grade.getSubjectId(),
                        subject.getName(),
                        grade.getGrade()
                };
                tableModel.addRow(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorMessage("Error populating table: " + e.getMessage());
        }
    }

    private void updateCountLabel() {
        countLabel.setText("Total Grades: " + tableModel.getRowCount());
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

    // Method to get the current GradeDAO
    public GradeDAO getGradeDAO() {
        return gradeDAO;
    }

    // Method to set a new GradeDAO implementation
    public void setGradeDAO(GradeDAO gradeDAO) {
        this.gradeDAO = gradeDAO;
        // Reload grades with the new DAO
        loadGrades();
    }

    // Helper class for ComboBox items
    private static class ComboItem {
        private int id;
        private String name;

        public ComboItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return name + " (ID: " + id + ")";
        }
    }
}