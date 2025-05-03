package com.example.studentperformance.ui;

import com.example.studentperformance.dao.SubjectDAO;
import com.example.studentperformance.DatabaseConnection;
import com.example.studentperformance.dao.SubjectDAOImpl;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ManageSubjectsUI {
    // Constants for styling - matching MainUI theme
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
    private JLabel statusLabel;
    private JLabel countLabel;
    private JTable subjectsTable;
    private DefaultTableModel tableModel;
    private SubjectDAO subjectDAO;
    private JTextField searchField;

    public ManageSubjectsUI() {
        frame = new JFrame("Manage Subjects");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(new Dimension(800, 600));

        // Initialize the SubjectDAO
        this.subjectDAO = new SubjectDAOImpl(DatabaseConnection.getConnection());

        initializeUI();
    }

    // Constructor that accepts a SubjectDAO implementation
    public ManageSubjectsUI(SubjectDAO subjectDAO) {
        frame = new JFrame("Manage Subjects");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(new Dimension(800, 600));

        // Use the provided SubjectDAO
        this.subjectDAO = subjectDAO;

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
        JLabel titleLabel = new JLabel("Subject Management");
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

        // Create the table model with columns - only using ID and Name as in original code
        String[] columnNames = {"ID", "Name"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            // Make cells not editable directly in the table
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Create and style the table
        subjectsTable = new JTable(tableModel);
        subjectsTable.setFont(TABLE_FONT);
        subjectsTable.setRowHeight(25);
        subjectsTable.setSelectionBackground(new Color(PRIMARY_COLOR.getRed(), PRIMARY_COLOR.getGreen(), PRIMARY_COLOR.getBlue(), 100));
        subjectsTable.setSelectionForeground(TEXT_COLOR);
        subjectsTable.setShowGrid(true);
        subjectsTable.setGridColor(new Color(230, 230, 250));
        subjectsTable.setFocusable(false);

        // Style the table header
        JTableHeader header = subjectsTable.getTableHeader();
        header.setFont(TABLE_HEADER_FONT);
        header.setBackground(PRIMARY_COLOR);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 35));

        // Set column widths - only ID and Name
        subjectsTable.getColumnModel().getColumn(0).setPreferredWidth(80);    // ID - narrow
        subjectsTable.getColumnModel().getColumn(1).setPreferredWidth(300);   // Name - wide

        // Add double-click functionality for editing
        subjectsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedRow = subjectsTable.getSelectedRow();
                    if (selectedRow != -1) {
                        int subjectId = (int) subjectsTable.getValueAt(selectedRow, 0);
                        openEditSubjectDialog(subjectId);
                    }
                }
            }
        });

        // Add scroll pane with custom styling
        JScrollPane scrollPane = new JScrollPane(subjectsTable);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5, 5, 5, 5),
                BorderFactory.createLineBorder(SECONDARY_COLOR, 1, true)
        ));

        // Search panel at the top
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setOpaque(false);
        JLabel searchLabel = new JLabel("Search Subject:");
        searchLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchLabel.setForeground(TEXT_COLOR);

        searchField = new JTextField(20);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Add listener to search as you type
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                searchSubjects();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                searchSubjects();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                searchSubjects();
            }
        });

        JButton searchButton = createStyledButton("Search", e -> searchSubjects());

        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        // Add components to the panel
        tablePanel.add(searchPanel, BorderLayout.NORTH);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // Load initial data - statusLabel is now initialized
        loadSubjects();

        return tablePanel;
    }

    private void searchSubjects() {
        String searchTerm = searchField.getText().trim().toLowerCase();
        statusLabel.setText("Searching for: " + searchTerm);

        if (searchTerm.isEmpty()) {
            loadSubjects(); // Load all subjects if search field is empty
            return;
        }

        try {
            List<SubjectDAO.Subject> allSubjects = subjectDAO.getAllSubjects();
            List<SubjectDAO.Subject> filteredSubjects = new ArrayList<>();

            // Filter subjects based on search term
            for (SubjectDAO.Subject subject : allSubjects) {
                if (subject.getName().toLowerCase().contains(searchTerm) ||
                        String.valueOf(subject.getSubjectId()).contains(searchTerm)) {
                    filteredSubjects.add(subject);
                }
            }

            // Update table with filtered results
            updateTableWithSubjects(filteredSubjects);
            statusLabel.setText("Found " + filteredSubjects.size() + " matching subjects");

        } catch (Exception ex) {
            ex.printStackTrace();
            showErrorMessage("Error searching subjects: " + ex.getMessage());
        }
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JButton addButton = createStyledButton("Add Subject", e -> openAddSubjectDialog());
        JButton editButton = createStyledButton("Edit Subject", e -> {
            int selectedRow = subjectsTable.getSelectedRow();
            if (selectedRow == -1) {
                showErrorMessage("Please select a Subject to edit.");
            } else {
                int subjectId = (int) subjectsTable.getValueAt(selectedRow, 0);
                openEditSubjectDialog(subjectId);
            }
        });

        JButton deleteButton = createStyledButton("Delete Subject", e -> deleteSelectedSubject());
        JButton refreshButton = createStyledButton("Refresh", e -> loadSubjects());
        JButton closeButton = createStyledButton("Close", e -> frame.dispose());

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(closeButton);

        return buttonPanel;
    }

    private void openAddSubjectDialog() {
        statusLabel.setText("Opening add subject dialog...");

        // Create input dialog
        String subjectName = JOptionPane.showInputDialog(
                frame,
                "Enter new subject name:",
                "Add Subject",
                JOptionPane.PLAIN_MESSAGE
        );

        // Check if user canceled or entered empty string
        if (subjectName != null && !subjectName.trim().isEmpty()) {
            try {
                // Create new subject using DAO
                subjectDAO.createSubject(subjectName.trim());
                statusLabel.setText("Subject added successfully");

                // Reload subjects to show the new addition
                loadSubjects();
            } catch (Exception ex) {
                ex.printStackTrace();
                showErrorMessage("Error adding subject: " + ex.getMessage());
            }
        } else if (subjectName != null) {
            // User entered empty string
            showErrorMessage("Subject name cannot be empty");
        }
    }

    private void openEditSubjectDialog(int subjectId) {
        statusLabel.setText("Opening edit dialog for Subject ID: " + subjectId);

        try {
            // Get current subject data
            SubjectDAO.Subject subject = subjectDAO.readSubject(subjectId);

            if (subject != null) {
                // Show dialog with current name
                String newName = JOptionPane.showInputDialog(
                        frame,
                        "Edit subject name:",
                        "Edit Subject",
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        null,
                        subject.getName()
                ).toString();

                // Check if user canceled or entered empty string
                if (newName != null && !newName.trim().isEmpty()) {
                    // Update subject using DAO
                    subjectDAO.updateSubject(subjectId, newName.trim());
                    statusLabel.setText("Subject updated successfully");

                    // Reload subjects to show the update
                    loadSubjects();
                } else if (newName != null) {
                    showErrorMessage("Subject name cannot be empty");
                }
            } else {
                showErrorMessage("Subject not found with ID: " + subjectId);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showErrorMessage("Error editing subject: " + ex.getMessage());
        }
    }

    private void deleteSelectedSubject() {
        int selectedRow = subjectsTable.getSelectedRow();
        if (selectedRow == -1) {
            showErrorMessage("Please select a Subject to delete.");
            return;
        }

        int subjectId = (int) subjectsTable.getValueAt(selectedRow, 0);
        String subjectName = (String) subjectsTable.getValueAt(selectedRow, 1);

        // Confirmation dialog with subject name
        int response = JOptionPane.showConfirmDialog(
                frame,
                "Are you sure you want to delete the subject: " + subjectName + "?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (response == JOptionPane.YES_OPTION) {
            try {
                // Delete subject using DAO
                subjectDAO.deleteSubject(subjectId);

                // Remove from table model
                tableModel.removeRow(selectedRow);
                statusLabel.setText("Subject deleted successfully");

                // Update count label
                updateCountLabel();
            } catch (SQLException ex) {
                ex.printStackTrace();
                showErrorMessage("Error deleting subject: " + ex.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
                showErrorMessage("Unexpected error: " + ex.getMessage());
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

        countLabel = new JLabel("Total Subjects: 0");
        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        countLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(countLabel, BorderLayout.EAST);

        return statusPanel;
    }

    private void loadSubjects() {
        statusLabel.setText("Loading subjects...");

        try {
            List<SubjectDAO.Subject> subjects = subjectDAO.getAllSubjects();
            updateTableWithSubjects(subjects);
            statusLabel.setText("Subjects loaded successfully");
        } catch (Exception e) {
            e.printStackTrace();
            showErrorMessage("Error loading subjects: " + e.getMessage());
        }
    }

    private void updateTableWithSubjects(List<SubjectDAO.Subject> subjects) {
        // Clear existing rows
        tableModel.setRowCount(0);

        // Update the count label
        countLabel.setText("Total Subjects: " + subjects.size());

        // Populate table with data - only ID and Name
        for (SubjectDAO.Subject subject : subjects) {
            Object[] row = {
                    subject.getSubjectId(),
                    subject.getName()
            };
            tableModel.addRow(row);
        }
    }

    private void updateCountLabel() {
        countLabel.setText("Total Subjects: " + tableModel.getRowCount());
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

    // Method to get the current SubjectDAO
    public SubjectDAO getSubjectDAO() {
        return subjectDAO;
    }

    // Method to set a new SubjectDAO implementation
    public void setSubjectDAO(SubjectDAO subjectDAO) {
        this.subjectDAO = subjectDAO;
        // Reload subjects with the new DAO
        loadSubjects();
    }
}