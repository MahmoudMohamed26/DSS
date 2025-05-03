package com.example.studentperformance.ui;

import com.example.studentperformance.DatabaseConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

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
    private JComboBox<String> studentComboBox;
    private JComboBox<String> periodComboBox;
    private JLabel statusLabel;

    // Data for the chart
    private Map<String, List<Integer>> attendanceData;
    private List<String> dateLabels;
    private String selectedStudent = "All Students";
    private String selectedPeriod = "Last 30 Days";

    public AttendanceReport() {
        initializeUI();
        loadMockData(); // In a real app, replace with loadDataFromDatabase()
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
        JLabel dateLabel = new JLabel(new SimpleDateFormat("EEEE, MMMM d, yyyy").format(new Date()));
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

        studentComboBox = new JComboBox<>(new String[]{"All Students", "John Smith", "Maria Garcia", "Ahmed Khan", "Emily Wong", "David Lee"});
        studentComboBox.setFont(LABEL_FONT);
        studentComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        studentComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, studentComboBox.getPreferredSize().height));
        studentComboBox.addActionListener(e -> {
            selectedStudent = (String) studentComboBox.getSelectedItem();
            loadMockData(); // In a real app, reload data from database
            chartPanel.repaint();
            statusLabel.setText("Showing attendance data for " + selectedStudent);
        });

        JLabel periodLabel = new JLabel("Time Period:");
        periodLabel.setFont(LABEL_FONT);
        periodLabel.setForeground(TEXT_COLOR);
        periodLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        periodComboBox = new JComboBox<>(new String[]{"Last 7 Days", "Last 30 Days", "Last Semester", "Full Year"});
        periodComboBox.setSelectedIndex(1); // Default to "Last 30 Days"
        periodComboBox.setFont(LABEL_FONT);
        periodComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        periodComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, periodComboBox.getPreferredSize().height));
        periodComboBox.addActionListener(e -> {
            selectedPeriod = (String) periodComboBox.getSelectedItem();
            loadMockData(); // In a real app, reload data from database
            chartPanel.repaint();
            statusLabel.setText("Showing attendance for " + selectedPeriod);
        });

        JButton refreshButton = new JButton("Refresh Data");
        refreshButton.setFont(LABEL_FONT);
        refreshButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        refreshButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, refreshButton.getPreferredSize().height));
        refreshButton.addActionListener(e -> {
            loadMockData(); // In a real app, reload data from database
            chartPanel.repaint();
            statusLabel.setText("Data refreshed for " + selectedStudent);
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

        // Add components to control panel with spacing
        controlPanel.add(filterLabel);
        controlPanel.add(Box.createVerticalStrut(15));
        controlPanel.add(studentLabel);
        controlPanel.add(Box.createVerticalStrut(5));
        controlPanel.add(studentComboBox);
        controlPanel.add(Box.createVerticalStrut(15));
        controlPanel.add(periodLabel);
        controlPanel.add(Box.createVerticalStrut(5));
        controlPanel.add(periodComboBox);
        controlPanel.add(Box.createVerticalStrut(25));
        controlPanel.add(refreshButton);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(exportButton);

        // Add a summary panel
        JPanel summaryPanel = createSummaryPanel();
        controlPanel.add(Box.createVerticalStrut(30));
        controlPanel.add(summaryPanel);

        return controlPanel;
    }

    private JPanel createSummaryPanel() {
        JPanel summaryPanel = new JPanel();
        summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS));
        summaryPanel.setOpaque(false);
        summaryPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        summaryPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel summaryLabel = new JLabel("Summary");
        summaryLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        summaryLabel.setForeground(TEXT_COLOR);
        summaryLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel avgAttendanceLabel = new JLabel("Average Attendance: 83%");
        avgAttendanceLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        avgAttendanceLabel.setForeground(TEXT_COLOR);
        avgAttendanceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel highestLabel = new JLabel("Highest Day: Monday (92%)");
        highestLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        highestLabel.setForeground(TEXT_COLOR);
        highestLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lowestLabel = new JLabel("Lowest Day: Friday (76%)");
        lowestLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lowestLabel.setForeground(TEXT_COLOR);
        lowestLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        summaryPanel.add(summaryLabel);
        summaryPanel.add(Box.createVerticalStrut(8));
        summaryPanel.add(avgAttendanceLabel);
        summaryPanel.add(Box.createVerticalStrut(5));
        summaryPanel.add(highestLabel);
        summaryPanel.add(Box.createVerticalStrut(5));
        summaryPanel.add(lowestLabel);

        return summaryPanel;
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

        // If too many dates, only show subset
        int skipFactor = Math.max(1, numDates / 10);
        int labelHeight = metrics.getHeight();

        g2.setColor(TEXT_COLOR);
        for (int i = 0; i < numDates; i++) {
            if (i % skipFactor == 0 || i == numDates - 1) {
                String dateLabel = dateLabels.get(i);
                int x = chartX + (i * chartWidth / (numDates - 1));

                // Draw vertical grid line
                g2.setColor(GRID_COLOR);
                g2.draw(new Line2D.Double(x, chartY, x, chartY + chartHeight));

                // Draw date label (rotated for better fit if many dates)
                g2.setColor(TEXT_COLOR);

                if (numDates > 15) {
                    // Rotated labels for many dates
                    g2.rotate(Math.PI / 4, x, chartY + chartHeight + 5);
                    g2.drawString(dateLabel, x, chartY + chartHeight + 5 + labelHeight);
                    g2.rotate(-Math.PI / 4, x, chartY + chartHeight + 5);
                } else {
                    // Horizontal labels for fewer dates
                    int labelWidth = metrics.stringWidth(dateLabel);
                    g2.drawString(dateLabel, x - labelWidth / 2, chartY + chartHeight + labelHeight + 5);
                }
            }
        }

        // Draw data lines for each dataset
        for (Map.Entry<String, List<Integer>> entry : attendanceData.entrySet()) {
            String student = entry.getKey();
            List<Integer> data = entry.getValue();

            // Skip if not the selected student and not showing "All Students"
            if (!selectedStudent.equals("All Students") && !student.equals(selectedStudent)) {
                continue;
            }

            // Generate a specific color based on student name (for when showing all students)
            Color lineColor = LINE_COLOR;
            Color pointColor = DATA_POINT_COLOR;

            if (selectedStudent.equals("All Students")) {
                // Generate colors based on student name hash
                int hash = student.hashCode();
                lineColor = new Color(
                        Math.abs(hash) % 200 + 30,
                        Math.abs(hash / 100) % 200 + 30,
                        Math.abs(hash / 10000) % 200 + 30
                );
                pointColor = lineColor.brighter();
            }

            // Draw the line
            Path2D path = new Path2D.Double();
            boolean first = true;

            for (int i = 0; i < Math.min(data.size(), numDates); i++) {
                int value = data.get(i);
                int x = chartX + (i * chartWidth / (numDates - 1));
                int y = chartY + chartHeight - (value * chartHeight / 100);

                if (first) {
                    path.moveTo(x, y);
                    first = false;
                } else {
                    path.lineTo(x, y);
                }

                // Draw data points
                g2.setColor(pointColor);
                g2.fill(new Ellipse2D.Double(x - 4, y - 4, 8, 8));
                g2.setColor(Color.WHITE);
                g2.fill(new Ellipse2D.Double(x - 2, y - 2, 4, 4));
            }

            // Draw the connecting line
            g2.setColor(lineColor);
            g2.setStroke(new BasicStroke(2f));
            g2.draw(path);

            // Add student label/legend if showing all students
            if (selectedStudent.equals("All Students")) {
                int lastIndex = Math.min(data.size(), numDates) - 1;
                if (lastIndex >= 0) {
                    int value = data.get(lastIndex);
                    int x = chartX + (lastIndex * chartWidth / (numDates - 1)) + 10;
                    int y = chartY + chartHeight - (value * chartHeight / 100);

                    g2.setColor(lineColor);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                    g2.drawString(student, x, y - 5);
                }
            }
        }
    }

    private JPanel createStatusPanel() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(new Color(240, 240, 240));
        statusPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        statusLabel = new JLabel("Ready - Showing attendance data");
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));

        JLabel versionLabel = new JLabel("v2.0");
        versionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        versionLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(versionLabel, BorderLayout.EAST);

        return statusPanel;
    }

    private void loadMockData() {
        // In a real application, this would be replaced with database queries
        // but for this example, we'll generate random data

        attendanceData = new HashMap<>();
        dateLabels = new ArrayList<>();

        // Generate date labels based on selected period
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd");
        int numDays;

        switch (selectedPeriod) {
            case "Last 7 Days":
                numDays = 7;
                break;
            case "Last 30 Days":
                numDays = 30;
                break;
            case "Last Semester":
                numDays = 120;
                break;
            case "Full Year":
                numDays = 365;
                break;
            default:
                numDays = 30;
        }

        // Generate dates (newest to oldest)
        for (int i = 0; i < numDays; i++) {
            cal.add(Calendar.DAY_OF_MONTH, -1);
            dateLabels.add(sdf.format(cal.getTime()));
        }
        Collections.reverse(dateLabels); // Reverse to show oldest to newest

        // If specific student selected
        if (!selectedStudent.equals("All Students")) {
            List<Integer> data = generateRandomAttendance(numDays, selectedStudent);
            attendanceData.put(selectedStudent, data);
        } else {
            // Generate data for all students
            String[] students = {"John Smith", "Maria Garcia", "Ahmed Khan", "Emily Wong", "David Lee"};
            for (String student : students) {
                List<Integer> data = generateRandomAttendance(numDays, student);
                attendanceData.put(student, data);
            }
        }

        // In a real app, update the summary panel with actual calculated values
        updateSummaryPanel();
    }

    private List<Integer> generateRandomAttendance(int numDays, String seed) {
        // Generate slightly realistic attendance data with some randomness
        // but maintain patterns (e.g., certain students tend to have better/worse attendance)

        List<Integer> data = new ArrayList<>();
        Random random = new Random(seed.hashCode()); // Use student name as seed for consistency

        // Base attendance rate varies by student (between 70% and 95%)
        int baseAttendance = 70 + random.nextInt(25);

        // Generate daily attendance with weekly patterns and small fluctuations
        for (int i = 0; i < numDays; i++) {
            int dayOfWeek = i % 7;

            // Lower attendance on Fridays and Mondays
            int dayFactor = 0;
            if (dayOfWeek == 0) dayFactor = -5; // Monday
            if (dayOfWeek == 4) dayFactor = -10; // Friday

            // Add random fluctuation between -8 and +8
            int fluctuation = random.nextInt(17) - 8;

            // Calculate final attendance percentage
            int attendance = baseAttendance + dayFactor + fluctuation;

            // Ensure attendance is between 0 and 100
            attendance = Math.max(0, Math.min(100, attendance));

            data.add(attendance);
        }

        return data;
    }

    private void updateSummaryPanel() {
        // In a real application, this would calculate actual summary statistics
        // For this example, we're just using fixed values
    }

    // In a real application, you would have a method like this:
    private void loadDataFromDatabase() {
        /*
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Sample query (adjust according to your database schema)
            String query = "SELECT s.student_name, a.date, a.attendance_percent " +
                          "FROM attendance a " +
                          "JOIN students s ON a.student_id = s.id " +
                          "WHERE a.date >= ? " +
                          "ORDER BY s.student_name, a.date";

            // Calculate date range based on selected period
            Calendar cal = Calendar.getInstance();
            java.sql.Date endDate = new java.sql.Date(cal.getTime().getTime());

            // Set start date based on selected period
            switch (selectedPeriod) {
                case "Last 7 Days":
                    cal.add(Calendar.DAY_OF_MONTH, -7);
                    break;
                case "Last 30 Days":
                    cal.add(Calendar.DAY_OF_MONTH, -30);
                    break;
                case "Last Semester":
                    cal.add(Calendar.MONTH, -6);
                    break;
                case "Full Year":
                    cal.add(Calendar.YEAR, -1);
                    break;
            }
            java.sql.Date startDate = new java.sql.Date(cal.getTime().getTime());

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setDate(1, startDate);

            ResultSet rs = stmt.executeQuery();

            // Process results
            attendanceData = new HashMap<>();
            dateLabels = new ArrayList<>();
            Set<String> uniqueDates = new TreeSet<>();

            // First pass to get all unique dates
            while (rs.next()) {
                String dateStr = new SimpleDateFormat("MMM dd").format(rs.getDate("date"));
                uniqueDates.add(dateStr);
            }

            // Convert sorted dates to list
            dateLabels.addAll(uniqueDates);

            // Reset result set
            rs = stmt.executeQuery();

            // Second pass to organize data by student
            while (rs.next()) {
                String student = rs.getString("student_name");
                String dateStr = new SimpleDateFormat("MMM dd").format(rs.getDate("date"));
                int attendancePercent = rs.getInt("attendance_percent");

                // If filtering by student
                if (!selectedStudent.equals("All Students") && !student.equals(selectedStudent)) {
                    continue;
                }

                // Add data
                if (!attendanceData.containsKey(student)) {
                    attendanceData.put(student, new ArrayList<>());
                }

                // Find index of this date
                int dateIndex = dateLabels.indexOf(dateStr);

                // Ensure list has enough space
                while (attendanceData.get(student).size() <= dateIndex) {
                    attendanceData.get(student).add(0); // Fill with zeros for missing dates
                }

                // Set the actual value
                attendanceData.get(student).set(dateIndex, attendancePercent);
            }

            // Calculate summary statistics
            updateSummaryPanel();

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error loading data: " + e.getMessage());
        }
        */
    }

    public static void main(String[] args) {
        try {
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