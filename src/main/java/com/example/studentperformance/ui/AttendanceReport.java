package com.example.studentperformance.ui;

import com.example.studentperformance.DatabaseConnection;
import com.example.studentperformance.dao.*;
import com.example.studentperformance.dao.AttendanceDAO.Attendance;
import com.example.studentperformance.dao.StudentDAO.Student;
import com.example.studentperformance.dao.SubjectDAO.Subject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.sql.Connection;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class AttendanceReport extends JFrame {
    // Constants for styling - matching MainUI
    private static final Color PRIMARY_COLOR = new Color(65, 105, 225);  // Royal Blue
    private static final Color SECONDARY_COLOR = new Color(70, 130, 180); // Steel Blue
    private static final Color BACKGROUND_COLOR = new Color(240, 248, 255); // Alice Blue
    private static final Color TEXT_COLOR = new Color(25, 25, 112); // Midnight Blue
    private static final Color GRID_COLOR = new Color(200, 200, 200);
    private static final Color DATA_POINT_COLOR = new Color(255, 69, 0); // Red-Orange
    private static final Color LINE_COLOR = new Color(65, 105, 225); // Royal Blue
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 20);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font AXIS_FONT = new Font("Segoe UI", Font.PLAIN, 12);

    private JPanel contentPane;
    private JPanel chartPanel;
    private JComboBox<ComboItem<Integer>> studentComboBox;
    private JComboBox<String> periodComboBox;
    private JComboBox<ComboItem<Integer>> subjectComboBox;
    private JLabel statusLabel;

    // Data for the chart
    private Map<String, List<Double>> attendanceData;
    private List<String> dateLabels;
    private Integer selectedStudentId = -1; // -1 represents "All Students"
    private String selectedPeriod = "Last 30 Days";
    private Integer selectedSubjectId = -1; // -1 represents "All Subjects"

    // DAOs
    private StudentDAO studentDAO;
    private AttendanceDAO attendanceDAO;
    private SubjectDAO subjectDAO;



    // Debug flag
    private static final boolean DEBUG = true;

    // Helper class for combo boxes
    static class ComboItem<T> {
        private T value;
        private String label;

        public ComboItem(T value, String label) {
            this.value = value;
            this.label = label;
        }

        public T getValue() {
            return value;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    public AttendanceReport() {
        // Test database connection first
        testDatabaseConnection();

        // Initialize DAOs
        try {
            debug("Initializing DAOs...");
            Connection conn = DatabaseConnection.getConnection();
            if (conn == null) {
                throw new Exception("Database connection is null");
            }

            studentDAO = new StudentDAOImpl(conn);
            attendanceDAO = new AttendanceDAOImpl(conn);
            subjectDAO = new SubjectDAOImpl(conn);
            debug("DAOs initialized successfully");
        } catch (Exception e) {
            debug("Error initializing database connection: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error initializing database connection: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        initializeUI();
        loadDatabaseData();
    }

    private void debug(String message) {
        if (DEBUG) {
            System.out.println("[DEBUG] " + message);
        }
    }

    private void testDatabaseConnection() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn != null && !conn.isClosed()) {
                debug("Database connection successful!");
            } else {
                debug("Database connection is null or closed!");
            }
        } catch (Exception e) {
            debug("Database connection test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeUI() {
        setTitle("Student Attendance Report");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);

        contentPane = new JPanel() {
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
        contentPane.setLayout(new BorderLayout(10, 10));
        contentPane.setBorder(new EmptyBorder(20, 20, 20, 20));
        setContentPane(contentPane);

        // Header panel
        JPanel headerPanel = createHeaderPanel();
        contentPane.add(headerPanel, BorderLayout.NORTH);

        // Control panel (filters)
        JPanel controlPanel = createControlPanel();
        contentPane.add(controlPanel, BorderLayout.WEST);

        // Chart panel
        chartPanel = createChartPanel();
        contentPane.add(chartPanel, BorderLayout.CENTER);

        // Status panel
        JPanel statusPanel = createStatusPanel();
        contentPane.add(statusPanel, BorderLayout.SOUTH);

        // Add window listener to handle closing
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel titleLabel = new JLabel("Student Attendance Report");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        headerPanel.add(titleLabel, BorderLayout.CENTER);

        // Current date
        JLabel dateLabel = new JLabel(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy").format(LocalDate.now()));
        dateLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        dateLabel.setForeground(TEXT_COLOR);
        headerPanel.add(dateLabel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setOpaque(false);
        controlPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SECONDARY_COLOR, 1, true),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        controlPanel.setPreferredSize(new Dimension(200, 0));

        JLabel filterLabel = new JLabel("Filters");
        filterLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        filterLabel.setForeground(TEXT_COLOR);
        filterLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel studentLabel = new JLabel("Student:");
        studentLabel.setFont(LABEL_FONT);
        studentLabel.setForeground(TEXT_COLOR);
        studentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Will be populated in loadDatabaseData()
        studentComboBox = new JComboBox<>();
        studentComboBox.setFont(LABEL_FONT);
        studentComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        studentComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, studentComboBox.getPreferredSize().height));
        studentComboBox.addActionListener(e -> {
            if (studentComboBox.getSelectedItem() == null) return;
            ComboItem<Integer> selectedItem = (ComboItem<Integer>) studentComboBox.getSelectedItem();
            if (selectedItem != null) {
                selectedStudentId = selectedItem.getValue();
                loadDatabaseData();
                chartPanel.repaint();
                statusLabel.setText("Showing attendance data for " + selectedItem.toString());
            }
        });

        JLabel subjectLabel = new JLabel("Subject:");
        subjectLabel.setFont(LABEL_FONT);
        subjectLabel.setForeground(TEXT_COLOR);
        subjectLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Will be populated in loadDatabaseData()
        subjectComboBox = new JComboBox<>();
        subjectComboBox.setFont(LABEL_FONT);
        subjectComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        subjectComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, subjectComboBox.getPreferredSize().height));
        subjectComboBox.addActionListener(e -> {
            if (subjectComboBox.getSelectedItem() == null) return;
            ComboItem<Integer> selectedItem = (ComboItem<Integer>) subjectComboBox.getSelectedItem();
            if (selectedItem != null) {
                selectedSubjectId = selectedItem.getValue();
                loadDatabaseData();
                chartPanel.repaint();
                statusLabel.setText("Showing attendance for " + selectedItem.toString());
            }
        });

        JLabel periodLabel = new JLabel("Time Period:");
        periodLabel.setFont(LABEL_FONT);
        periodLabel.setForeground(TEXT_COLOR);
        periodLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        periodComboBox = new JComboBox<>(new String[]{"Last 7 Days", "Last 30 Days", "Last Semester (120 Days)", "Full Year"});
        periodComboBox.setSelectedIndex(1); // Default to "Last 30 Days"
        periodComboBox.setFont(LABEL_FONT);
        periodComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        periodComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, periodComboBox.getPreferredSize().height));
        periodComboBox.addActionListener(e -> {
            selectedPeriod = (String) periodComboBox.getSelectedItem();
            loadDatabaseData();
            chartPanel.repaint();
            statusLabel.setText("Showing attendance for " + selectedPeriod);
        });

        JButton refreshButton = new JButton("Refresh Data");
        refreshButton.setFont(LABEL_FONT);
        refreshButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        refreshButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, refreshButton.getPreferredSize().height));
        refreshButton.addActionListener(e -> {
            loadDatabaseData();
            chartPanel.repaint();
            statusLabel.setText("Data refreshed");
        });

        JButton exportButton = new JButton("Export Chart");
        exportButton.setFont(LABEL_FONT);
        exportButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        exportButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, exportButton.getPreferredSize().height));
        exportButton.addActionListener(e -> {
            statusLabel.setText("Chart export feature coming soon");
            JOptionPane.showMessageDialog(
                    this,
                    "The export feature will be available in the next update.",
                    "Coming Soon",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });

        // Debug button
        JButton debugButton = new JButton("Debug Info");
        debugButton.setFont(LABEL_FONT);
        debugButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        debugButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, debugButton.getPreferredSize().height));
        debugButton.addActionListener(e -> {
            showDebugInfo();
        });

        // Add components to control panel with spacing
        controlPanel.add(filterLabel);
        controlPanel.add(Box.createVerticalStrut(15));
        controlPanel.add(studentLabel);
        controlPanel.add(Box.createVerticalStrut(5));
        controlPanel.add(studentComboBox);
        controlPanel.add(Box.createVerticalStrut(15));
        controlPanel.add(subjectLabel);
        controlPanel.add(Box.createVerticalStrut(5));
        controlPanel.add(subjectComboBox);
        controlPanel.add(Box.createVerticalStrut(15));
        controlPanel.add(periodLabel);
        controlPanel.add(Box.createVerticalStrut(5));
        controlPanel.add(periodComboBox);
        controlPanel.add(Box.createVerticalStrut(25));
        controlPanel.add(refreshButton);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(exportButton);

        if (DEBUG) {
            controlPanel.add(Box.createVerticalStrut(10));
            controlPanel.add(debugButton);
        }



        return controlPanel;
    }

    private void showDebugInfo() {
        StringBuilder info = new StringBuilder();
        info.append("Database Connection: ");

        try {
            Connection conn = DatabaseConnection.getConnection();
            info.append(conn != null && !conn.isClosed() ? "OK" : "FAILED").append("\n");
        } catch (Exception e) {
            info.append("ERROR: ").append(e.getMessage()).append("\n");
        }

        info.append("\nDAO Status:\n");
        info.append("StudentDAO: ").append(studentDAO != null ? "Initialized" : "NULL").append("\n");
        info.append("SubjectDAO: ").append(subjectDAO != null ? "Initialized" : "NULL").append("\n");
        info.append("AttendanceDAO: ").append(attendanceDAO != null ? "Initialized" : "NULL").append("\n");

        info.append("\nData Status:\n");
        info.append("Selected Student: ").append(selectedStudentId).append("\n");
        info.append("Selected Subject: ").append(selectedSubjectId).append("\n");
        info.append("Selected Period: ").append(selectedPeriod).append("\n");
        info.append("Date Labels Count: ").append(dateLabels != null ? dateLabels.size() : "NULL").append("\n");
        info.append("Attendance Data Size: ").append(attendanceData != null ? attendanceData.size() : "NULL").append("\n");

        if (attendanceData != null && !attendanceData.isEmpty()) {
            info.append("\nAttendance Data Contents:\n");
            for (Map.Entry<String, List<Double>> entry : attendanceData.entrySet()) {
                info.append(entry.getKey()).append(": ")
                        .append(entry.getValue().size()).append(" records\n");
            }
        }

        JTextArea textArea = new JTextArea(info.toString());
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));

        JOptionPane.showMessageDialog(
                this,
                scrollPane,
                "Debug Information",
                JOptionPane.INFORMATION_MESSAGE
        );
    }



    private JPanel createChartPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                drawChart(g2);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SECONDARY_COLOR, 1, true),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        return panel;
    }

    private void drawChart(Graphics2D g2) {
        if (attendanceData == null || dateLabels == null || dateLabels.isEmpty()) {
            g2.setColor(TEXT_COLOR);
            g2.setFont(TITLE_FONT);
            g2.drawString("No attendance data available", 50, 50);

            // Additional debug info in chart
            if (DEBUG) {
                g2.setFont(new Font("Monospaced", Font.PLAIN, 12));
                g2.drawString("dateLabels: " + (dateLabels == null ? "null" : dateLabels.size() + " items"), 50, 80);
                g2.drawString("attendanceData: " + (attendanceData == null ? "null" : attendanceData.size() + " entries"), 50, 100);

                int line = 120;
                if (attendanceData != null) {
                    for (Map.Entry<String, List<Double>> entry : attendanceData.entrySet()) {
                        g2.drawString(entry.getKey() + ": " + entry.getValue().size() + " data points", 50, line);
                        line += 20;
                    }
                }
            }
            return;
        }

        // Set rendering hints for better quality
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Get panel dimensions
        int width = chartPanel.getWidth() - 40; // Account for padding
        int height = chartPanel.getHeight() - 60; // Account for padding and labels

        // Define chart area
        int chartX = 60; // Left margin for Y-axis labels
        int chartY = 30; // Top margin
        int chartWidth = width - chartX - 20; // Right margin
        int chartHeight = height - chartY - 60; // Bottom margin for X-axis labels

        // Draw chart background with subtle gradient
        GradientPaint gp = new GradientPaint(
                chartX, chartY, new Color(255, 255, 255),
                chartX, chartY + chartHeight, new Color(245, 245, 255)
        );
        g2.setPaint(gp);
        g2.fill(new Rectangle2D.Double(chartX, chartY, chartWidth, chartHeight));

        // Draw chart border
        g2.setColor(GRID_COLOR);
        g2.setStroke(new BasicStroke(1));
        g2.draw(new Rectangle2D.Double(chartX, chartY, chartWidth, chartHeight));

        // Draw chart title
        g2.setColor(TEXT_COLOR);
        g2.setFont(TITLE_FONT);
        String title = "Attendance Trend";
        FontMetrics titleMetrics = g2.getFontMetrics();
        int titleWidth = titleMetrics.stringWidth(title);
        g2.drawString(title, chartX + (chartWidth - titleWidth) / 2, chartY - 10);

        // Draw Y-axis label
        g2.setFont(LABEL_FONT);
        g2.rotate(-Math.PI / 2, chartX - 40, chartY + chartHeight / 2);
        g2.drawString("Attendance (%)", chartX - 40, chartY + chartHeight / 2);
        g2.rotate(Math.PI / 2, chartX - 40, chartY + chartHeight / 2);

        // Draw horizontal grid lines and Y-axis labels
        int numGridLines = 10;
        g2.setFont(AXIS_FONT);
        g2.setColor(GRID_COLOR);
        g2.setStroke(new BasicStroke(0.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, new float[]{2}, 0));
        FontMetrics metrics = g2.getFontMetrics();

        for (int i = 0; i <= numGridLines; i++) {
            int y = chartY + chartHeight - (i * chartHeight / numGridLines);
            g2.draw(new Line2D.Double(chartX, y, chartX + chartWidth, y));

            String label = (i * 10) + "%";
            int labelWidth = metrics.stringWidth(label);
            g2.setColor(TEXT_COLOR);
            g2.drawString(label, chartX - labelWidth - 5, y + 4);
            g2.setColor(GRID_COLOR);
        }

        // Draw X-axis labels (dates)
        int numDates = dateLabels.size();
        if (numDates == 0) return;

        int skipFactor = Math.max(1, numDates / 10);
        int labelHeight = metrics.getHeight();

        g2.setColor(TEXT_COLOR);
        for (int i = 0; i < numDates; i++) {
            if (i % skipFactor == 0 || i == numDates - 1) {
                String dateLabel = dateLabels.get(i);
                int x = chartX + (i * chartWidth / (numDates - 1));

                g2.setColor(GRID_COLOR);
                g2.draw(new Line2D.Double(x, chartY, x, chartY + chartHeight));

                g2.setColor(TEXT_COLOR);
                if (numDates > 15) {
                    g2.rotate(Math.PI / 4, x, chartY + chartHeight + 5);
                    g2.drawString(dateLabel, x, chartY + chartHeight + 5 + labelHeight);
                    g2.rotate(-Math.PI / 4, x, chartY + chartHeight + 5);
                } else {
                    int lw = metrics.stringWidth(dateLabel);
                    g2.drawString(dateLabel, x - lw / 2, chartY + chartHeight + labelHeight + 5);
                }
            }
        }

        // Prepare entries list for indexed iteration
        List<Map.Entry<String, List<Double>>> entries = new ArrayList<>(attendanceData.entrySet());

        // Draw data lines and legend for each dataset
        boolean dataDrawn = false;
        for (int idx = 0; idx < entries.size(); idx++) {
            Map.Entry<String, List<Double>> entry = entries.get(idx);
            String student = entry.getKey();
            List<Double> data = entry.getValue();
            if (data.isEmpty()) continue;

            // Determine colors
            Color lineColor = LINE_COLOR;
            Color pointColor = DATA_POINT_COLOR;
            if (selectedStudentId == -1) {
                int hash = student.hashCode();
                lineColor = new Color(Math.abs(hash) % 200 + 30, Math.abs(hash / 100) % 200 + 30, Math.abs(hash / 10000) % 200 + 30);
                pointColor = lineColor.brighter();
            }

            Path2D path = new Path2D.Double();
            boolean first = true;
            boolean anyValidPoints = false;

            for (int i = 0; i < Math.min(data.size(), numDates); i++) {
                double value = data.get(i);
                if (value < 0) continue;
                anyValidPoints = true;

                int x = chartX + (i * chartWidth / (numDates - 1));
                int y = chartY + chartHeight - (int) (value * chartHeight / 100);

                if (first) {
                    path.moveTo(x, y);
                    first = false;
                } else {
                    path.lineTo(x, y);
                }

                g2.setColor(pointColor);
                g2.fill(new Ellipse2D.Double(x - 4, y - 4, 8, 8));

                if (x - 5 <= mouseX && mouseX <= x + 5 && y - 5 <= mouseY && mouseY <= y + 5) {
                    String tooltip = student + ": " + String.format("%.1f%%", value) + " on " + dateLabels.get(i);
                    g2.setColor(new Color(50, 50, 50, 220));
                    int tooltipWidth = g2.getFontMetrics().stringWidth(tooltip) + 10;
                    int tooltipHeight = g2.getFontMetrics().getHeight() + 6;
                    g2.fillRoundRect(mouseX + 10, mouseY - tooltipHeight - 5, tooltipWidth, tooltipHeight, 5, 5);
                    g2.setColor(Color.WHITE);
                    g2.drawString(tooltip, mouseX + 15, mouseY - 10);
                }
            }

            if (anyValidPoints) {
                dataDrawn = true;
                g2.setColor(lineColor);
                g2.setStroke(new BasicStroke(2));
                g2.draw(path);


            }
        }

        if (!dataDrawn) {
            g2.setColor(TEXT_COLOR);
            g2.setFont(TITLE_FONT);
            String msg = "No attendance data available for the selected criteria";
            int msgWidth = g2.getFontMetrics().stringWidth(msg);
            g2.drawString(msg, chartX + (chartWidth - msgWidth) / 2, chartY + chartHeight / 2);
        }
    }

    // Mouse coordinates for tooltips
    private int mouseX = -1;
    private int mouseY = -1;

    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        statusLabel = new JLabel("Ready");
        statusLabel.setFont(LABEL_FONT);
        statusLabel.setForeground(SECONDARY_COLOR);

        panel.add(statusLabel, BorderLayout.WEST);

        // Add mouse motion listener for tooltips
        chartPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
                chartPanel.repaint();
            }
        });

        chartPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                mouseX = -1;
                mouseY = -1;
                chartPanel.repaint();
            }
        });

        return panel;
    }

    private void loadDatabaseData() {
        try {
            debug("Loading database data...");

            // Load students for combo box if not already loaded
            if (studentComboBox.getItemCount() == 0) {
                debug("Loading students for combo box...");

                // Add "All Students" option
                studentComboBox.addItem(new ComboItem<>(-1, "All Students"));

                List<Student> students = studentDAO.getAllStudents();
                debug("Retrieved " + students.size() + " students from database");

                for (Student student : students) {
                    studentComboBox.addItem(new ComboItem<>(student.getStudentId(), student.getName()));
                }
            }

            // Load subjects for combo box if not already loaded
            if (subjectComboBox.getItemCount() == 0) {
                debug("Loading subjects for combo box...");

                // Add "All Subjects" option
                subjectComboBox.addItem(new ComboItem<>(-1, "All Subjects"));

                List<Subject> subjects = subjectDAO.getAllSubjects();
                debug("Retrieved " + subjects.size() + " subjects from database");

                for (Subject subject : subjects) {
                    subjectComboBox.addItem(new ComboItem<>(subject.getSubjectId(), subject.getName()));
                }
            }

            // Calculate date range based on selected period
            LocalDate endDate = LocalDate.now();
            LocalDate startDate;

            switch (selectedPeriod) {
                case "Last 7 Days":
                    startDate = endDate.minusDays(6); // Include today
                    break;
                case "Last 30 Days":
                    startDate = endDate.minusDays(29); // Include today
                    break;
                case "Last Semester (120 Days)":
                    startDate = endDate.minusDays(119); // Include today
                    break;
                case "Full Year":
                    startDate = endDate.minusDays(364); // Include today
                    break;
                default:
                    startDate = endDate.minusDays(29); // Default to 30 days
            }

            debug("Date range: " + startDate + " to " + endDate);

            // Generate date labels based on selected period
            dateLabels = new ArrayList<>();
            DateTimeFormatter formatter;

            // Choose appropriate date format based on period
            if (selectedPeriod.equals("Last 7 Days")) {
                formatter = DateTimeFormatter.ofPattern("EEE, MMM d"); // e.g., "Mon, Jan 1"
            } else if (selectedPeriod.equals("Last 30 Days")) {
                formatter = DateTimeFormatter.ofPattern("MMM d"); // e.g., "Jan 1"
            } else {
                formatter = DateTimeFormatter.ofPattern("MMM d"); // e.g., "Jan 1"
            }

            LocalDate currentDate = startDate;
            while (!currentDate.isAfter(endDate)) {
                dateLabels.add(formatter.format(currentDate));
                currentDate = currentDate.plusDays(1);
            }

            debug("Generated " + dateLabels.size() + " date labels");

            // Load attendance data
            List<Attendance> allAttendance = attendanceDAO.getAllAttendance();
            debug("Retrieved " + allAttendance.size() + " attendance records from database");

            // Filter attendance records based on selected criteria
            List<Attendance> filteredAttendance = allAttendance.stream()
                    .filter(a -> a.getDate().isAfter(startDate.minusDays(1)) && !a.getDate().isAfter(endDate))
                    .filter(a -> selectedStudentId == -1 || a.getStudentId().equals(selectedStudentId))
                    .filter(a -> selectedSubjectId == -1 || a.getSubjectId().equals(selectedSubjectId))
                    .collect(Collectors.toList());

            debug("Filtered to " + filteredAttendance.size() + " relevant attendance records");

            // Process attendance data
            attendanceData = new HashMap<>();

            if (selectedStudentId == -1) {
                // For "All Students", group by student
                Map<Integer, String> studentNames = new HashMap<>();
                for (Student student : studentDAO.getAllStudents()) {
                    studentNames.put(student.getStudentId(), student.getName());
                }

                // Group attendance by student
                Map<Integer, List<Attendance>> attendanceByStudent = filteredAttendance.stream()
                        .collect(Collectors.groupingBy(Attendance::getStudentId));

                // Process each student's attendance
                for (Map.Entry<Integer, List<Attendance>> entry : attendanceByStudent.entrySet()) {
                    int studentId = entry.getKey();
                    List<Attendance> studentAttendance = entry.getValue();
                    String studentName = studentNames.getOrDefault(studentId, "Student " + studentId);

                    List<Double> attendancePercentages = calculateAttendancePercentages(studentAttendance, startDate, endDate);
                    attendanceData.put(studentName, attendancePercentages);
                }
            } else {
                // For a specific student, process their attendance
                String studentName = "";
                for (int i = 0; i < studentComboBox.getItemCount(); i++) {
                    ComboItem<Integer> item = (ComboItem<Integer>) studentComboBox.getItemAt(i);
                    if (item.getValue().equals(selectedStudentId)) {
                        studentName = item.toString();
                        break;
                    }
                }

                List<Double> attendancePercentages = calculateAttendancePercentages(filteredAttendance, startDate, endDate);
                attendanceData.put(studentName, attendancePercentages);
            }



            debug("Attendance data processed successfully");
        } catch (Exception e) {
            debug("Error loading database data: " + e.getMessage());
            e.printStackTrace();

            // Show error message
            JOptionPane.showMessageDialog(
                    this,
                    "Error loading data: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );

            // Initialize empty data
            dateLabels = new ArrayList<>();
            attendanceData = new HashMap<>();
        }
    }

    private List<Double> calculateAttendancePercentages(List<Attendance> attendanceRecords, LocalDate startDate, LocalDate endDate) {
        // Initialize result list with zeros for each day
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate) + 1; // Include end date
        List<Double> percentages = new ArrayList<>(Collections.nCopies((int) daysBetween, 0.0));

        // Count total records and present records for each day
        Map<LocalDate, Integer> totalRecords = new HashMap<>();
        Map<LocalDate, Integer> presentRecords = new HashMap<>();

        for (Attendance record : attendanceRecords) {
            LocalDate date = record.getDate();
            if (!date.isBefore(startDate) && !date.isAfter(endDate)) {
                totalRecords.put(date, totalRecords.getOrDefault(date, 0) + 1);
                if (record.isPresent()) {
                    presentRecords.put(date, presentRecords.getOrDefault(date, 0) + 1);
                }
            }
        }

        // Calculate percentages for days with records
        for (int i = 0; i < daysBetween; i++) {
            LocalDate currentDate = startDate.plusDays(i);
            int total = totalRecords.getOrDefault(currentDate, 0);

            if (total > 0) {
                int present = presentRecords.getOrDefault(currentDate, 0);
                double percentage = (double) present / total * 100.0;
                percentages.set(i, percentage);
            } else {
                // If no records for this date, mark as -1 (missing data)
                percentages.set(i, -1.0);
            }
        }

        return percentages;
    }



    public static void main(String[] args) {
        try {
            // Set look and feel to the system look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            AttendanceReport report = new AttendanceReport();
            report.setVisible(true);
        });
    }
}