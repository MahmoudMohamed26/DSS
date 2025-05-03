package com.example.studentperformance.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import javax.swing.border.TitledBorder;
import org.jfree.data.category.*;
import org.jfree.chart.renderer.category.*;

import org.jfree.chart.labels.*;
import org.jfree.chart.axis.*;

public class GradesSummary extends JFrame {
    // Colors matching the main UI theme
    private static final Color PRIMARY_COLOR = new Color(65, 105, 225);  // Royal Blue
    private static final Color SECONDARY_COLOR = new Color(70, 130, 180); // Steel Blue
    private static final Color BACKGROUND_COLOR = new Color(240, 248, 255); // Alice Blue
    private static final Color TEXT_COLOR = new Color(25, 25, 112); // Midnight Blue
    private static final Color GOOD_PERFORMANCE = new Color(46, 139, 87); // Sea Green
    private static final Color BAD_PERFORMANCE = new Color(178, 34, 34);  // Firebrick Red
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font STATS_FONT = new Font("Segoe UI", Font.BOLD, 16);

    // Mock data for demonstration
    private final String[] subjects = {"Mathematics", "English", "Science", "History", "Art"};
    private final String[] students = {"Alex Smith", "Jamie Johnson", "Casey Wilson", "Morgan Lee", "Taylor Brown"};
    private final Random random = new Random(123); // Fixed seed for consistent results

    // Store grades data - indexed by [student][subject][period]
    // period 0 = previous term, period 1 = current term
    private final int[][][] gradesData;

    // UI Components
    private JComboBox<String> studentDropdown;
    private JComboBox<String> subjectDropdown;
    private JPanel statisticsPanel;
    private JPanel chartPanel;

    public GradesSummary() {
        // Initialize grades data
        gradesData = new int[students.length][subjects.length][2];
        generateGradesData();

        initUI();
    }

    private void generateGradesData() {
        for (int i = 0; i < students.length; i++) {
            for (int j = 0; j < subjects.length; j++) {
                // Previous term grades (60-95)
                gradesData[i][j][0] = 60 + random.nextInt(36);

                // Current term grades with some variance (-10 to +15 from previous)
                int change = random.nextInt(26) - 10;
                int newGrade = gradesData[i][j][0] + change;
                // Keep within 60-100 range
                gradesData[i][j][1] = Math.max(60, Math.min(100, newGrade));
            }
        }
    }

    private void initUI() {
        setTitle("Grades Summary");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 750);
        setLocationRelativeTo(null);

        // Main panel with gradient background
        JPanel mainPanel = new JPanel() {
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
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header panel
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Selection panel
        JPanel selectionPanel = createSelectionPanel();
        mainPanel.add(selectionPanel, BorderLayout.WEST);

        // Statistics panel
        statisticsPanel = new JPanel();
        statisticsPanel.setOpaque(false);
        statisticsPanel.setLayout(new BorderLayout());
        statisticsPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        mainPanel.add(statisticsPanel, BorderLayout.EAST);

        // Chart panel
        chartPanel = new JPanel();
        chartPanel.setOpaque(false);
        chartPanel.setLayout(new BorderLayout());
        mainPanel.add(chartPanel, BorderLayout.CENTER);

        // Initialize with overall view
        updateChartAndStats(null, null);

        setContentPane(mainPanel);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(10, 10));
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Student Performance Dashboard");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel subtitleLabel = new JLabel("Academic Year 2024-2025");
        subtitleLabel.setFont(LABEL_FONT);
        subtitleLabel.setForeground(TEXT_COLOR);
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel labelPanel = new JPanel(new GridLayout(2, 1));
        labelPanel.setOpaque(false);
        labelPanel.add(titleLabel);
        labelPanel.add(subtitleLabel);

        headerPanel.add(labelPanel, BorderLayout.CENTER);

        return headerPanel;
    }

    private JPanel createSelectionPanel() {
        JPanel selectionPanel = new JPanel();
        selectionPanel.setLayout(new BoxLayout(selectionPanel, BoxLayout.Y_AXIS));
        selectionPanel.setOpaque(false);
        selectionPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 10));
        selectionPanel.setPreferredSize(new Dimension(200, 600));

        // Student selection
        JLabel studentLabel = new JLabel("Select Student:");
        studentLabel.setFont(LABEL_FONT);
        studentLabel.setForeground(TEXT_COLOR);
        studentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        Vector<String> studentItems = new Vector<>();
        studentItems.add("All Students");
        studentItems.addAll(Arrays.asList(students));

        studentDropdown = new JComboBox<>(studentItems);
        studentDropdown.setFont(LABEL_FONT);
        studentDropdown.setBackground(Color.WHITE);
        studentDropdown.setForeground(TEXT_COLOR);
        studentDropdown.setAlignmentX(Component.LEFT_ALIGNMENT);
        studentDropdown.setMaximumSize(new Dimension(200, 30));

        // Subject selection
        JLabel subjectLabel = new JLabel("Select Subject:");
        subjectLabel.setFont(LABEL_FONT);
        subjectLabel.setForeground(TEXT_COLOR);
        subjectLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        Vector<String> subjectItems = new Vector<>();
        subjectItems.add("All Subjects");
        subjectItems.addAll(Arrays.asList(subjects));

        subjectDropdown = new JComboBox<>(subjectItems);
        subjectDropdown.setFont(LABEL_FONT);
        subjectDropdown.setBackground(Color.WHITE);
        subjectDropdown.setForeground(TEXT_COLOR);
        subjectDropdown.setAlignmentX(Component.LEFT_ALIGNMENT);
        subjectDropdown.setMaximumSize(new Dimension(200, 30));

        // Add action listeners
        studentDropdown.addActionListener(e -> updateSelection());
        subjectDropdown.addActionListener(e -> updateSelection());

        // Add some spacing
        selectionPanel.add(Box.createVerticalStrut(20));
        selectionPanel.add(studentLabel);
        selectionPanel.add(Box.createVerticalStrut(5));
        selectionPanel.add(studentDropdown);
        selectionPanel.add(Box.createVerticalStrut(20));
        selectionPanel.add(subjectLabel);
        selectionPanel.add(Box.createVerticalStrut(5));
        selectionPanel.add(subjectDropdown);

        return selectionPanel;
    }

    private void updateSelection() {
        String selectedStudent = studentDropdown.getSelectedIndex() == 0 ? null :
                (String) studentDropdown.getSelectedItem();
        String selectedSubject = subjectDropdown.getSelectedIndex() == 0 ? null :
                (String) subjectDropdown.getSelectedItem();

        updateChartAndStats(selectedStudent, selectedSubject);
    }

    private void updateChartAndStats(String selectedStudent, String selectedSubject) {
        // Clear existing components
        statisticsPanel.removeAll();
        chartPanel.removeAll();

        if (selectedStudent == null && selectedSubject == null) {
            // Show overall statistics
            showOverallStatistics();
        } else if (selectedStudent != null && selectedSubject != null) {
            // Show specific student and subject
            showSpecificStudentSubject(selectedStudent, selectedSubject);
        } else if (selectedStudent != null) {
            // Show all subjects for specific student
            showStudentAllSubjects(selectedStudent);
        } else {
            // Show all students for specific subject
            showSubjectAllStudents(selectedSubject);
        }

        // Refresh UI
        statisticsPanel.revalidate();
        statisticsPanel.repaint();
        chartPanel.revalidate();
        chartPanel.repaint();
    }

    private void showOverallStatistics() {
        // Create overall chart
        JFreeChart overallChart = createOverallChart();
        ChartPanel overallChartPanel = new ChartPanel(overallChart);
        overallChartPanel.setPreferredSize(new Dimension(600, 500));
        chartPanel.add(overallChartPanel, BorderLayout.CENTER);

        // Add overall statistics panel
        JPanel statsContent = new JPanel();
        statsContent.setLayout(new BoxLayout(statsContent, BoxLayout.Y_AXIS));
        statsContent.setOpaque(false);
        statsContent.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(SECONDARY_COLOR, 2, true),
                        "Overall Statistics",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        TITLE_FONT,
                        TEXT_COLOR
                ),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Calculate overall statistics
        double avgImprovement = calculateOverallImprovement();
        String improvementText = String.format("%.2f%%", avgImprovement);
        Color improvementColor = avgImprovement >= 0 ? GOOD_PERFORMANCE : BAD_PERFORMANCE;

        // Add statistics components
        addStatLabel(statsContent, "Class Average (Current):", calculateOverallAverage(1) + "%");
        addStatLabel(statsContent, "Class Average (Previous):", calculateOverallAverage(0) + "%");
        addStatLabel(statsContent, "Overall Improvement:", improvementText, improvementColor);
        addStatLabel(statsContent, "Top Subject:", findTopSubject());
        addStatLabel(statsContent, "Top Student:", findTopStudent());

        statisticsPanel.add(statsContent, BorderLayout.NORTH);
    }

    private void showSpecificStudentSubject(String student, String subject) {
        int studentIndex = Arrays.asList(students).indexOf(student);
        int subjectIndex = Arrays.asList(subjects).indexOf(subject);

        // Get previous and current grades
        int prevGrade = gradesData[studentIndex][subjectIndex][0];
        int currGrade = gradesData[studentIndex][subjectIndex][1];
        double improvement = ((double)currGrade - prevGrade) / prevGrade * 100;

        // Create bar chart for this specific student and subject
        JFreeChart chart = createSpecificChart(student, subject, prevGrade, currGrade);
        ChartPanel chartPanelComponent = new ChartPanel(chart);
        chartPanelComponent.setPreferredSize(new Dimension(600, 500));
        chartPanel.add(chartPanelComponent, BorderLayout.CENTER);

        // Create statistics panel
        JPanel statsContent = new JPanel();
        statsContent.setLayout(new BoxLayout(statsContent, BoxLayout.Y_AXIS));
        statsContent.setOpaque(false);
        statsContent.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(SECONDARY_COLOR, 2, true),
                        student + " - " + subject,
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        TITLE_FONT,
                        TEXT_COLOR
                ),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Add performance information
        addStatLabel(statsContent, "Current Grade:", currGrade + "%");
        addStatLabel(statsContent, "Previous Grade:", prevGrade + "%");

        String changeSign = improvement >= 0 ? "+" : "";
        String improvementText = changeSign + String.format("%.2f%%", improvement);
        Color improvementColor = improvement >= 0 ? GOOD_PERFORMANCE : BAD_PERFORMANCE;

        addStatLabel(statsContent, "Improvement:", improvementText, improvementColor);

        // Add comparison to class average
        double classAvg = calculateSubjectAverage(subjectIndex, 1);
        String comparisonText = String.format("%.2f%%", currGrade - classAvg);
        String comparisonPrefix = currGrade >= classAvg ? "+" : "";
        Color comparisonColor = currGrade >= classAvg ? GOOD_PERFORMANCE : BAD_PERFORMANCE;

        addStatLabel(statsContent, "vs. Class Average:", comparisonPrefix + comparisonText, comparisonColor);

        statisticsPanel.add(statsContent, BorderLayout.NORTH);
    }

    private void showStudentAllSubjects(String student) {
        int studentIndex = Arrays.asList(students).indexOf(student);

        // Create chart for student across all subjects
        JFreeChart chart = createStudentChart(student);
        ChartPanel chartPanelComponent = new ChartPanel(chart);
        chartPanelComponent.setPreferredSize(new Dimension(600, 500));
        chartPanel.add(chartPanelComponent, BorderLayout.CENTER);

        // Create statistics panel
        JPanel statsContent = new JPanel();
        statsContent.setLayout(new BoxLayout(statsContent, BoxLayout.Y_AXIS));
        statsContent.setOpaque(false);
        statsContent.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(SECONDARY_COLOR, 2, true),
                        student + " - Performance Summary",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        TITLE_FONT,
                        TEXT_COLOR
                ),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Calculate student statistics
        double avgCurrent = calculateStudentAverage(studentIndex, 1);
        double avgPrevious = calculateStudentAverage(studentIndex, 0);
        double improvement = (avgCurrent - avgPrevious) / avgPrevious * 100;

        // Add statistics
        addStatLabel(statsContent, "Current Average:", String.format("%.1f%%", avgCurrent));
        addStatLabel(statsContent, "Previous Average:", String.format("%.1f%%", avgPrevious));

        String changeSign = improvement >= 0 ? "+" : "";
        String improvementText = changeSign + String.format("%.2f%%", improvement);
        Color improvementColor = improvement >= 0 ? GOOD_PERFORMANCE : BAD_PERFORMANCE;

        addStatLabel(statsContent, "Overall Improvement:", improvementText, improvementColor);

        // Best and worst subjects
        String bestSubject = findBestSubject(studentIndex);
        String worstSubject = findWorstSubject(studentIndex);

        addStatLabel(statsContent, "Strongest Subject:", bestSubject);
        addStatLabel(statsContent, "Needs Improvement:", worstSubject);

        statisticsPanel.add(statsContent, BorderLayout.NORTH);
    }

    private void showSubjectAllStudents(String subject) {
        int subjectIndex = Arrays.asList(subjects).indexOf(subject);

        // Create chart for subject across all students
        JFreeChart chart = createSubjectChart(subject);
        ChartPanel chartPanelComponent = new ChartPanel(chart);
        chartPanelComponent.setPreferredSize(new Dimension(600, 500));
        chartPanel.add(chartPanelComponent, BorderLayout.CENTER);

        // Create statistics panel
        JPanel statsContent = new JPanel();
        statsContent.setLayout(new BoxLayout(statsContent, BoxLayout.Y_AXIS));
        statsContent.setOpaque(false);
        statsContent.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(SECONDARY_COLOR, 2, true),
                        subject + " - Class Performance",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        TITLE_FONT,
                        TEXT_COLOR
                ),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Calculate subject statistics
        double avgCurrent = calculateSubjectAverage(subjectIndex, 1);
        double avgPrevious = calculateSubjectAverage(subjectIndex, 0);
        double improvement = (avgCurrent - avgPrevious) / avgPrevious * 100;

        // Add statistics
        addStatLabel(statsContent, "Class Average (Current):", String.format("%.1f%%", avgCurrent));
        addStatLabel(statsContent, "Class Average (Previous):", String.format("%.1f%%", avgPrevious));

        String changeSign = improvement >= 0 ? "+" : "";
        String improvementText = changeSign + String.format("%.2f%%", improvement);
        Color improvementColor = improvement >= 0 ? GOOD_PERFORMANCE : BAD_PERFORMANCE;

        addStatLabel(statsContent, "Class Improvement:", improvementText, improvementColor);

        // Top and bottom performers
        String topStudent = findTopPerformer(subjectIndex);
        String bottomStudent = findBottomPerformer(subjectIndex);

        addStatLabel(statsContent, "Top Performer:", topStudent);
        addStatLabel(statsContent, "Needs Support:", bottomStudent);

        statisticsPanel.add(statsContent, BorderLayout.NORTH);
    }

    private void addStatLabel(JPanel panel, String label, String value) {
        addStatLabel(panel, label, value, TEXT_COLOR);
    }

    private void addStatLabel(JPanel panel, String label, String value, Color valueColor) {
        JPanel statPanel = new JPanel(new BorderLayout(10, 0));
        statPanel.setOpaque(false);
        statPanel.setMaximumSize(new Dimension(300, 35));
        statPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(LABEL_FONT);
        labelComponent.setForeground(TEXT_COLOR);

        JLabel valueComponent = new JLabel(value);
        valueComponent.setFont(STATS_FONT);
        valueComponent.setForeground(valueColor);

        statPanel.add(labelComponent, BorderLayout.WEST);
        statPanel.add(valueComponent, BorderLayout.EAST);

        panel.add(statPanel);
        panel.add(Box.createVerticalStrut(10));
    }

    // Chart creation methods
    private JFreeChart createOverallChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // Add current term data for each student in each subject
        for (int i = 0; i < students.length; i++) {
            for (int j = 0; j < subjects.length; j++) {
                dataset.addValue(gradesData[i][j][1], students[i], subjects[j]);
            }
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Overall Student Performance (Current Term)",
                "Subjects",
                "Grade",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        customizeChart(chart);
        return chart;
    }

    private JFreeChart createSpecificChart(String student, String subject, int prevGrade, int currGrade) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        dataset.addValue(prevGrade, "Previous Term", student);
        dataset.addValue(currGrade, "Current Term", student);

        JFreeChart chart = ChartFactory.createBarChart(
                subject + " - Performance Comparison",
                "Term",
                "Grade",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        customizeChart(chart);

        // Additional customization for this specific chart
        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();

        renderer.setSeriesPaint(0, SECONDARY_COLOR);
        renderer.setSeriesPaint(1, PRIMARY_COLOR);

        return chart;
    }

    private JFreeChart createStudentChart(String student) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        int studentIndex = Arrays.asList(students).indexOf(student);

        // Add both previous and current term data for all subjects
        for (int j = 0; j < subjects.length; j++) {
            dataset.addValue(gradesData[studentIndex][j][0], "Previous Term", subjects[j]);
            dataset.addValue(gradesData[studentIndex][j][1], "Current Term", subjects[j]);
        }

        JFreeChart chart = ChartFactory.createBarChart(
                student + " - Performance Across Subjects",
                "Subjects",
                "Grade",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        customizeChart(chart);

        // Additional customization
        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();

        renderer.setSeriesPaint(0, SECONDARY_COLOR);
        renderer.setSeriesPaint(1, PRIMARY_COLOR);

        return chart;
    }

    private JFreeChart createSubjectChart(String subject) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        int subjectIndex = Arrays.asList(subjects).indexOf(subject);

        // Add both previous and current term data for all students
        for (int i = 0; i < students.length; i++) {
            dataset.addValue(gradesData[i][subjectIndex][0], "Previous Term", students[i]);
            dataset.addValue(gradesData[i][subjectIndex][1], "Current Term", students[i]);
        }

        JFreeChart chart = ChartFactory.createBarChart(
                subject + " - Student Performance",
                "Students",
                "Grade",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        customizeChart(chart);

        // Additional customization
        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();

        renderer.setSeriesPaint(0, SECONDARY_COLOR);
        renderer.setSeriesPaint(1, PRIMARY_COLOR);

        // Rotate student labels for better visibility
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);

        return chart;
    }

    private void customizeChart(JFreeChart chart) {
        CategoryPlot plot = chart.getCategoryPlot();

        // Background
        chart.setBackgroundPaint(BACKGROUND_COLOR);
        plot.setBackgroundPaint(new Color(245, 250, 255));

        // Grid lines
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        // Bar renderer
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setItemMargin(0.1);


        for (int i = 0; i < students.length; i++) {
            int red = clamp(PRIMARY_COLOR.getRed() + (i * 30) % 100);
            int green = clamp(PRIMARY_COLOR.getGreen() + (i * 20) % 100);
            int blue = clamp(PRIMARY_COLOR.getBlue() + (i * 10) % 100);

            Color color = new Color(red, green, blue);
            renderer.setSeriesPaint(i, color);
        }

        // Axis customization
        CategoryAxis domainAxis = plot.getDomainAxis();
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();

        domainAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 12));
        domainAxis.setLabelFont(new Font("Segoe UI", Font.BOLD, 14));

        rangeAxis.setRange(0, 100);
        rangeAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 12));
        rangeAxis.setLabelFont(new Font("Segoe UI", Font.BOLD, 14));
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

    // Statistics calculation methods
    private double calculateOverallAverage(int period) {
        double sum = 0;
        int count = 0;

        for (int i = 0; i < students.length; i++) {
            for (int j = 0; j < subjects.length; j++) {
                sum += gradesData[i][j][period];
                count++;
            }
        }

        return sum / count;
    }

    private double calculateOverallImprovement() {
        double avgPrevious = calculateOverallAverage(0);
        double avgCurrent = calculateOverallAverage(1);

        return (avgCurrent - avgPrevious) / avgPrevious * 100;
    }

    private double calculateStudentAverage(int studentIndex, int period) {
        double sum = 0;

        for (int j = 0; j < subjects.length; j++) {
            sum += gradesData[studentIndex][j][period];
        }

        return sum / subjects.length;
    }

    private double calculateSubjectAverage(int subjectIndex, int period) {
        double sum = 0;

        for (int i = 0; i < students.length; i++) {
            sum += gradesData[i][subjectIndex][period];
        }

        return sum / students.length;
    }

    private String findTopSubject() {
        double bestImprovement = -100; // Initialize to a very low value
        String topSubject = "";

        for (int j = 0; j < subjects.length; j++) {
            double avgPrevious = calculateSubjectAverage(j, 0);
            double avgCurrent = calculateSubjectAverage(j, 1);
            double improvement = (avgCurrent - avgPrevious) / avgPrevious * 100;

            if (improvement > bestImprovement) {
                bestImprovement = improvement;
                topSubject = subjects[j];
            }
        }

        return topSubject + " (+" + String.format("%.1f", bestImprovement) + "%)";
    }

    private String findTopStudent() {
        double bestImprovement = -100; // Initialize to a very low value
        String topStudent = "";

        for (int i = 0; i < students.length; i++) {
            double avgPrevious = calculateStudentAverage(i, 0);
            double avgCurrent = calculateStudentAverage(i, 1);
            double improvement = (avgCurrent - avgPrevious) / avgPrevious * 100;

            if (improvement > bestImprovement) {
                bestImprovement = improvement;
                topStudent = students[i];
            }
        }

        return topStudent + " (+" + String.format("%.1f", bestImprovement) + "%)";
    }

    private String findBestSubject(int studentIndex) {
        int bestGrade = 0;
        String bestSubject = "";

        for (int j = 0; j < subjects.length; j++) {
            if (gradesData[studentIndex][j][1] > bestGrade) {
                bestGrade = gradesData[studentIndex][j][1];
                bestSubject = subjects[j];
            }
        }

        return bestSubject + " (" + bestGrade + "%)";
    }

    private String findWorstSubject(int studentIndex) {
        int worstGrade = 100;
        String worstSubject = "";

        for (int j = 0; j < subjects.length; j++) {
            if (gradesData[studentIndex][j][1] < worstGrade) {
                worstGrade = gradesData[studentIndex][j][1];
                worstSubject = subjects[j];
            }
        }

        return worstSubject + " (" + worstGrade + "%)";
    }

    private String findTopPerformer(int subjectIndex) {
        int bestGrade = 0;
        String bestStudent = "";

        for (int i = 0; i < students.length; i++) {
            if (gradesData[i][subjectIndex][1] > bestGrade) {
                bestGrade = gradesData[i][subjectIndex][1];
                bestStudent = students[i];
            }
        }

        return bestStudent + " (" + bestGrade + "%)";
    }

    private String findBottomPerformer(int subjectIndex) {
        int worstGrade = 100;
        String worstStudent = "";

        for (int i = 0; i < students.length; i++) {
            if (gradesData[i][subjectIndex][1] < worstGrade) {
                worstGrade = gradesData[i][subjectIndex][1];
                worstStudent = students[i];
            }
        }

        return worstStudent + " (" + worstGrade + "%)";
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GradesSummary app = new GradesSummary();
            app.setVisible(true);
        });
    }
}