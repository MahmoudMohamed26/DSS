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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class ManageAttendanceUI {
    // Constants for styling - matching the theme from ManageGradesUI
    private static final Color PRIMARY_COLOR = new Color(65, 105, 225);  // Royal Blue
    private static final Color SECONDARY_COLOR = new Color(70, 130, 180); // Steel Blue
    private static final Color BACKGROUND_COLOR = new Color(240, 248, 255); // Alice Blue
    private static final Color TEXT_COLOR = new Color(25, 25, 112); // Midnight Blue
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 20);
    private static final Font TABLE_HEADER_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font TABLE_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 14);

    private JFrame frame;
    private Connection connection;
    private JLabel statusLabel;
    private JLabel countLabel;
    private JTable attendanceTable;
    private DefaultTableModel tableModel;
    private AttendanceDAO attendanceDAO;
    private StudentDAO studentDAO;
    private SubjectDAO subjectDAO;
    private JTextField searchField;

    public ManageAttendanceUI(Connection connection) {
        this.connection = connection;
        this.attendanceDAO = new AttendanceDAOImpl(connection);
        this.studentDAO = new StudentDAOImpl(connection);
        this.subjectDAO = new SubjectDAOImpl(connection);

        frame = new JFrame("Manage Attendance");
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

        // Create status panel
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
        JLabel titleLabel = new JLabel("Attendance Management");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        // Current date on the right
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
        String[] columnNames = {"Student ID", "Student Name", "Subject ID", "Subject Name", "Date", "Present"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            // Make cells not editable directly in the table
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            // Define column types
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 5) { // "Present" column
                    return Boolean.class;
                }
                return super.getColumnClass(columnIndex);
            }
        };

        // Create and style the table
        attendanceTable = new JTable(tableModel);
        attendanceTable.setFont(TABLE_FONT);
        attendanceTable.setRowHeight(25);
        attendanceTable.setSelectionBackground(new Color(PRIMARY_COLOR.getRed(), PRIMARY_COLOR.getGreen(), PRIMARY_COLOR.getBlue(), 100));
        attendanceTable.setSelectionForeground(TEXT_COLOR);
        attendanceTable.setShowGrid(true);
        attendanceTable.setGridColor(new Color(230, 230, 250));
        attendanceTable.setFocusable(false);

        // Style the table header
        JTableHeader header = attendanceTable.getTableHeader();
        header.setFont(TABLE_HEADER_FONT);
        header.setBackground(PRIMARY_COLOR);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 35));

        // Set column widths
        attendanceTable.getColumnModel().getColumn(0).setPreferredWidth(80);    // Student ID
        attendanceTable.getColumnModel().getColumn(1).setPreferredWidth(200);   // Student Name
        attendanceTable.getColumnModel().getColumn(2).setPreferredWidth(80);    // Subject ID
        attendanceTable.getColumnModel().getColumn(3).setPreferredWidth(200);   // Subject Name
        attendanceTable.getColumnModel().getColumn(4).setPreferredWidth(120);   // Date
        attendanceTable.getColumnModel().getColumn(5).setPreferredWidth(80);    // Present

        // Add double-click functionality for editing
        attendanceTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedRow = attendanceTable.getSelectedRow();
                    if (selectedRow != -1) {
                        int studentId = (int) attendanceTable.getValueAt(selectedRow, 0);
                        int subjectId = (int) attendanceTable.getValueAt(selectedRow, 2);
                        LocalDate date = (LocalDate) attendanceTable.getValueAt(selectedRow, 4);
                        openEditAttendanceDialog(studentId, subjectId, date);
                    }
                }
            }
        });

        // Add scroll pane with custom styling
        JScrollPane scrollPane = new JScrollPane(attendanceTable);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5, 5, 5, 5),
                BorderFactory.createLineBorder(SECONDARY_COLOR, 1, true)
        ));

        // Search panel at the top
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setOpaque(false);
        JLabel searchLabel = new JLabel("Search Attendance:");
        searchLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchLabel.setForeground(TEXT_COLOR);

        searchField = new JTextField(20);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Add listener to search as you type
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                searchAttendance();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                searchAttendance();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                searchAttendance();
            }
        });

        JButton searchButton = createStyledButton("Search", e -> searchAttendance());

        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        // Add components to the panel
        tablePanel.add(searchPanel, BorderLayout.NORTH);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // Load initial data
        loadAttendance();

        return tablePanel;
    }

    private void searchAttendance() {
        String searchTerm = searchField.getText().trim().toLowerCase();
        statusLabel.setText("Searching for: " + searchTerm);

        if (searchTerm.isEmpty()) {
            loadAttendance(); // Load all attendance records if search field is empty
            return;
        }

        try {
            List<AttendanceDAO.Attendance> allAttendance = attendanceDAO.getAllAttendance();
            List<AttendanceDAO.Attendance> filteredAttendance = new ArrayList<>();

            // Filter attendance based on search term
            for (AttendanceDAO.Attendance attendance : allAttendance) {
                StudentDAO.Student student = studentDAO.readStudent(attendance.getStudentId());
                SubjectDAO.Subject subject = subjectDAO.readSubject(attendance.getSubjectId());

                if (String.valueOf(attendance.getStudentId()).contains(searchTerm) ||
                        String.valueOf(attendance.getSubjectId()).contains(searchTerm) ||
                        String.valueOf(attendance.getDate()).contains(searchTerm) ||
                        String.valueOf(attendance.isPresent()).contains(searchTerm) ||
                        student.getName().toLowerCase().contains(searchTerm) ||
                        subject.getName().toLowerCase().contains(searchTerm)) {
                    filteredAttendance.add(attendance);
                }
            }

            // Update table with filtered results
            updateTableWithAttendance(filteredAttendance);
            statusLabel.setText("Found " + filteredAttendance.size() + " matching attendance records");

        } catch (Exception ex) {
            ex.printStackTrace();
            showErrorMessage("Error searching attendance: " + ex.getMessage());
        }
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JButton addButton = createStyledButton("Add Attendance", e -> openAddAttendanceDialog());
        JButton editButton = createStyledButton("Edit Attendance", e -> {
            int selectedRow = attendanceTable.getSelectedRow();
            if (selectedRow == -1) {
                showErrorMessage("Please select an attendance record to edit.");
            } else {
                int studentId = (int) attendanceTable.getValueAt(selectedRow, 0);
                int subjectId = (int) attendanceTable.getValueAt(selectedRow, 2);
                LocalDate date = (LocalDate) attendanceTable.getValueAt(selectedRow, 4);
                openEditAttendanceDialog(studentId, subjectId, date);
            }
        });

        JButton deleteButton = createStyledButton("Delete Attendance", e -> deleteSelectedAttendance());
        JButton refreshButton = createStyledButton("Refresh", e -> loadAttendance());
        JButton closeButton = createStyledButton("Close", e -> frame.dispose());

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(closeButton);

        return buttonPanel;
    }

    private void openAddAttendanceDialog() {
        statusLabel.setText("Opening add attendance dialog...");

        // Create a custom dialog
        JDialog addDialog = new JDialog(frame, "Add Attendance", true);
        addDialog.setLayout(new BorderLayout());
        addDialog.setSize(450, 300);
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
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
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

        // Date field
        JLabel dateLabel = new JLabel("Date (YYYY-MM-DD):");
        dateLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        dateLabel.setForeground(TEXT_COLOR);

        JTextField dateField = new JTextField();
        dateField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dateField.setText(LocalDate.now().toString()); // Default to today's date

        // Present checkbox
        JLabel presentLabel = new JLabel("Present:");
        presentLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        presentLabel.setForeground(TEXT_COLOR);

        JCheckBox presentCheckBox = new JCheckBox();
        presentCheckBox.setSelected(true); // Default to present
        presentCheckBox.setOpaque(false);

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
        formPanel.add(dateLabel);
        formPanel.add(dateField);
        formPanel.add(presentLabel);
        formPanel.add(presentCheckBox);

        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);

        JButton saveButton = createStyledButton("Save", e -> {
            try {
                ComboItem selectedStudent = (ComboItem) studentComboBox.getSelectedItem();
                ComboItem selectedSubject = (ComboItem) subjectComboBox.getSelectedItem();
                String dateText = dateField.getText().trim();

                if (selectedStudent == null || selectedSubject == null || dateText.isEmpty()) {
                    JOptionPane.showMessageDialog(addDialog, "Please fill all fields.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Parse and validate date
                LocalDate date;
                try {
                    date = LocalDate.parse(dateText);
                } catch (DateTimeParseException ex) {
                    JOptionPane.showMessageDialog(addDialog, "Please enter a valid date in YYYY-MM-DD format.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                boolean present = presentCheckBox.isSelected();

                // Check if attendance already exists
                try {
                    AttendanceDAO.Attendance existingAttendance = attendanceDAO.readAttendance(selectedStudent.getId(), selectedSubject.getId(), date);
                    if (existingAttendance != null) {
                        JOptionPane.showMessageDialog(addDialog, "Attendance record already exists for this student, subject, and date.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } catch (Exception ignored) {
                    // Exception means attendance doesn't exist, which is what we want
                }

                // Save the new attendance
                attendanceDAO.createAttendance(selectedStudent.getId(), selectedSubject.getId(), date, present);

                // Refresh the table
                loadAttendance();
                statusLabel.setText("Attendance added successfully");

                // Close the dialog
                addDialog.dispose();
            } catch (Exception ex) {
                ex.printStackTrace();
                showErrorMessage("Error adding attendance: " + ex.getMessage());
            }
        });

        JButton cancelButton = createStyledButton("Cancel", e -> addDialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        // Add components to panel
        panel.add(new JLabel("Add New Attendance", SwingConstants.CENTER), BorderLayout.NORTH);
        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        // Add panel to dialog
        addDialog.add(panel);

        // Show dialog
        addDialog.setVisible(true);
    }

    private void openEditAttendanceDialog(int studentId, int subjectId, LocalDate date) {
        statusLabel.setText("Opening edit dialog for Student ID: " + studentId + ", Subject ID: " + subjectId + ", Date: " + date);

        try {
            // Fetch the attendance from the database
            AttendanceDAO.Attendance attendance = attendanceDAO.readAttendance(studentId, subjectId, date);
            StudentDAO.Student student = studentDAO.readStudent(studentId);
            SubjectDAO.Subject subject = subjectDAO.readSubject(subjectId);

            if (attendance == null) {
                showErrorMessage("Attendance not found for Student ID: " + studentId + ", Subject ID: " + subjectId + ", Date: " + date);
                return;
            }

            // Create a custom dialog
            JDialog editDialog = new JDialog(frame, "Edit Attendance", true);
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
            JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
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

            // Date Info (non-editable)
            JLabel dateLabel = new JLabel("Date:");
            dateLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            dateLabel.setForeground(TEXT_COLOR);

            JLabel dateValueLabel = new JLabel(date.toString());
            dateValueLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            dateValueLabel.setForeground(TEXT_COLOR);

            // Present checkbox (editable)
            JLabel presentLabel = new JLabel("Present:");
            presentLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            presentLabel.setForeground(TEXT_COLOR);

            JCheckBox presentCheckBox = new JCheckBox();
            presentCheckBox.setSelected(attendance.isPresent());
            presentCheckBox.setOpaque(false);

            formPanel.add(studentLabel);
            formPanel.add(studentValueLabel);
            formPanel.add(subjectLabel);
            formPanel.add(subjectValueLabel);
            formPanel.add(dateLabel);
            formPanel.add(dateValueLabel);
            formPanel.add(presentLabel);
            formPanel.add(presentCheckBox);

            // Create button panel
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.setOpaque(false);

            JButton updateButton = createStyledButton("Update", e -> {
                try {
                    boolean present = presentCheckBox.isSelected();

                    // Update the attendance
                    attendanceDAO.updateAttendance(studentId, subjectId, date, present);

                    // Refresh the table
                    loadAttendance();
                    statusLabel.setText("Attendance updated successfully");

                    // Close the dialog
                    editDialog.dispose();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showErrorMessage("Error updating attendance: " + ex.getMessage());
                }
            });

            JButton cancelButton = createStyledButton("Cancel", e -> editDialog.dispose());

            buttonPanel.add(updateButton);
            buttonPanel.add(cancelButton);

            // Add components to panel
            panel.add(new JLabel("Edit Attendance", SwingConstants.CENTER), BorderLayout.NORTH);
            panel.add(formPanel, BorderLayout.CENTER);
            panel.add(buttonPanel, BorderLayout.SOUTH);

            // Add panel to dialog
            editDialog.add(panel);

            // Show dialog
            editDialog.setVisible(true);

        } catch (Exception ex) {
            ex.printStackTrace();
            showErrorMessage("Error retrieving attendance data: " + ex.getMessage());
        }
    }

    private void deleteSelectedAttendance() {
        int selectedRow = attendanceTable.getSelectedRow();
        if (selectedRow == -1) {
            showErrorMessage("Please select an attendance record to delete.");
            return;
        }

        int studentId = (int) attendanceTable.getValueAt(selectedRow, 0);
        int subjectId = (int) attendanceTable.getValueAt(selectedRow, 2);
        LocalDate date = (LocalDate) attendanceTable.getValueAt(selectedRow, 4);
        String studentName = (String) attendanceTable.getValueAt(selectedRow, 1);
        String subjectName = (String) attendanceTable.getValueAt(selectedRow, 3);

        // Confirmation dialog with attendance details
        int response = JOptionPane.showConfirmDialog(
                frame,
                "Are you sure you want to delete the attendance record for student: " + studentName +
                        " in subject: " + subjectName + " on date: " + date + "?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (response == JOptionPane.YES_OPTION) {
            try {
                // Delete attendance using deleteAttendance
                attendanceDAO.deleteAttendance(studentId, subjectId, date);

                // Remove from table model
                tableModel.removeRow(selectedRow);
                statusLabel.setText("Attendance deleted successfully");

                // Update count label
                updateCountLabel();
            } catch (Exception ex) {
                ex.printStackTrace();
                showErrorMessage("Error deleting attendance: " + ex.getMessage());
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
        button.setPreferredSize(new Dimension(150, 36));

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
        statusPanel.setName("statusPanel");

        statusLabel = new JLabel("Ready");
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));

        countLabel = new JLabel("Total Attendance Records: 0");
        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        countLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(countLabel, BorderLayout.EAST);

        return statusPanel;
    }

    private void loadAttendance() {
        statusLabel.setText("Loading attendance records...");

        try {
            List<AttendanceDAO.Attendance> attendanceList = attendanceDAO.getAllAttendance();
            updateTableWithAttendance(attendanceList);
            statusLabel.setText("Attendance records loaded successfully");
        } catch (Exception e) {
            e.printStackTrace();
            showErrorMessage("Error loading attendance records: " + e.getMessage());
        }
    }

    private void updateTableWithAttendance(List<AttendanceDAO.Attendance> attendanceList) {
        // Clear existing rows
        tableModel.setRowCount(0);

        // Update the count label
        countLabel.setText("Total Attendance Records: " + attendanceList.size());

        // Populate table with data
        for (AttendanceDAO.Attendance attendance : attendanceList) {
            try {
                StudentDAO.Student student = studentDAO.readStudent(attendance.getStudentId());
                SubjectDAO.Subject subject = subjectDAO.readSubject(attendance.getSubjectId());

                Object[] row = {
                        attendance.getStudentId(),
                        student.getName(),
                        attendance.getSubjectId(),
                        subject.getName(),
                        attendance.getDate(),
                        attendance.isPresent()
                };
                tableModel.addRow(row);
            } catch (Exception e) {
                e.printStackTrace();
                // If we can't load student or subject, still show the attendance with placeholder
                Object[] row = {
                        attendance.getStudentId(),
                        "Unknown Student",
                        attendance.getSubjectId(),
                        "Unknown Subject",
                        attendance.getDate(),
                        attendance.isPresent()
                };
                tableModel.addRow(row);
            }
        }
    }

    private void updateCountLabel() {
        countLabel.setText("Total Attendance Records: " + tableModel.getRowCount());
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

    // Method to get the current AttendanceDAO
    public AttendanceDAO getAttendanceDAO() {
        return attendanceDAO;
    }

    // Method to set a new AttendanceDAO implementation
    public void setAttendanceDAO(AttendanceDAO attendanceDAO) {
        this.attendanceDAO = attendanceDAO;
        // Reload attendance with the new DAO
        loadAttendance();
    }

    // ComboItem class to hold both ID and name for combo boxes
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
            return name;
        }
    }
}