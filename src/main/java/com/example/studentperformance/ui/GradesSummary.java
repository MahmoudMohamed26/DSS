package com.example.studentperformance.ui;

import com.example.studentperformance.DatabaseConnection;
import com.example.studentperformance.dao.*;
import com.example.studentperformance.dao.GradeDAOImpl;
import com.example.studentperformance.dao.StudentDAOImpl;
import com.example.studentperformance.dao.SubjectDAOImpl;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

public class GradesSummary extends JFrame {
    // Color and font constants
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

    private Connection dbConnection;
    private GradeDAO gradeDAO;
    private StudentDAO studentDAO;
    private SubjectDAO subjectDAO;
    private AttendanceDAO attendanceDAO;
    private JTabbedPane tabbedPane;
    private JPanel overviewPanel;
    private JPanel studentPanel;
    private JPanel subjectPanel;
    private JPanel distributionPanel;

    private JComboBox<StudentDAO.Student> studentSelector;
    private JComboBox<SubjectDAO.Subject> subjectSelector;

    // For distribution panel
    private JComboBox<SubjectDAO.Subject> distributionSubjectSelector;
    private JComboBox<String> distributionViewSelector;

    // For subject panel
    private JComboBox<String> gradeRangeSelector;

    /**
     * Constructor that initializes the analytics dashboard with just a database connection
     */
    public GradesSummary() {
        this.dbConnection = DatabaseConnection.getConnection();

        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Apply custom UI settings
        applyCustomUI();

        // Initialize DAOs
        initializeDAOs();

        // Setup UI components
        setupUI();

        // Load data and refresh charts
        refreshData();
    }

    private void applyCustomUI() {
        // Set global UI properties
        UIManager.put("Panel.background", BACKGROUND_COLOR);
        UIManager.put("OptionPane.background", BACKGROUND_COLOR);
        UIManager.put("Button.background", PRIMARY_COLOR);
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Button.font", LABEL_FONT);
        UIManager.put("Label.foreground", TEXT_COLOR);
        UIManager.put("Label.font", LABEL_FONT);
        UIManager.put("ComboBox.background", Color.WHITE);
        UIManager.put("ComboBox.foreground", TEXT_COLOR);
        UIManager.put("ComboBox.selectionBackground", PRIMARY_COLOR);
        UIManager.put("ComboBox.selectionForeground", Color.WHITE);
        UIManager.put("ComboBox.font", LABEL_FONT);
        UIManager.put("TabbedPane.selected", SECONDARY_COLOR);
        UIManager.put("TabbedPane.background", BACKGROUND_COLOR);
        UIManager.put("TabbedPane.foreground", TEXT_COLOR);
        UIManager.put("TabbedPane.font", LABEL_FONT);
    }

    private void initializeDAOs() {
        try {
            // Assuming implementation classes exist with these names and constructors
            gradeDAO = new GradeDAOImpl(dbConnection);
            studentDAO = new StudentDAOImpl(dbConnection);
            subjectDAO = new SubjectDAOImpl(dbConnection);
            attendanceDAO = new AttendanceDAOImpl(dbConnection);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error initializing data access: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void setupUI() {
        setTitle("Student Grades Analytics");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BACKGROUND_COLOR);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(LABEL_FONT);
        tabbedPane.setBackground(BACKGROUND_COLOR);
        tabbedPane.setForeground(TEXT_COLOR);

        // Create panels for different analytics views
        overviewPanel = new JPanel(new BorderLayout());
        studentPanel = new JPanel(new BorderLayout());
        subjectPanel = new JPanel(new BorderLayout());
        distributionPanel = new JPanel(new BorderLayout());

        // Set background colors
        overviewPanel.setBackground(BACKGROUND_COLOR);
        studentPanel.setBackground(BACKGROUND_COLOR);
        subjectPanel.setBackground(BACKGROUND_COLOR);
        distributionPanel.setBackground(BACKGROUND_COLOR);

        // Setup control panels for each tab
        setupOverviewPanel();
        setupStudentPanel();
        setupSubjectPanel();
        setupDistributionPanel();

        // Add tabs
        tabbedPane.addTab("Overview", overviewPanel);
        tabbedPane.addTab("Student Performance", studentPanel);
        tabbedPane.addTab("Subject Analysis", subjectPanel);
        tabbedPane.addTab("Grade Distribution", distributionPanel);

        add(tabbedPane);
    }

    private void setupOverviewPanel() {
        // Overview panel doesn't need additional controls
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanel.setBackground(BACKGROUND_COLOR);
        controlPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(SECONDARY_COLOR),
                "Overview Controls",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                LABEL_FONT,
                TEXT_COLOR));

        JButton refreshBtn = new JButton("Refresh Data");
        refreshBtn.setBackground(PRIMARY_COLOR);
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFont(LABEL_FONT);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));

        refreshBtn.addActionListener(e -> {
            try {
                updateOverviewChart();
            } catch (Exception ex) {
                handleError("Error refreshing overview chart", ex);
            }
        });

        controlPanel.add(refreshBtn);
        overviewPanel.add(controlPanel, BorderLayout.NORTH);
    }

    private void setupStudentPanel() {
        JPanel studentControlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        studentControlPanel.setBackground(BACKGROUND_COLOR);
        studentControlPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(SECONDARY_COLOR),
                "Student Performance Controls",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                LABEL_FONT,
                TEXT_COLOR));

        // Student selector
        JLabel studentLabel = new JLabel("Select Student:");
        studentLabel.setFont(LABEL_FONT);
        studentLabel.setForeground(TEXT_COLOR);

        studentSelector = new JComboBox<>();
        studentSelector.setFont(LABEL_FONT);
        studentSelector.setBackground(Color.WHITE);
        studentSelector.setForeground(TEXT_COLOR);
        studentSelector.setPreferredSize(new Dimension(200, 30));

        // Add action listener to update chart when selection changes
        studentSelector.addActionListener(e -> updateStudentChart());

        studentControlPanel.add(studentLabel);
        studentControlPanel.add(studentSelector);

        studentPanel.add(studentControlPanel, BorderLayout.NORTH);
    }

    private void setupSubjectPanel() {
        JPanel subjectControlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        subjectControlPanel.setBackground(BACKGROUND_COLOR);
        subjectControlPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(SECONDARY_COLOR),
                "Subject Analysis Controls",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                LABEL_FONT,
                TEXT_COLOR));

        // Subject selector
        JLabel subjectLabel = new JLabel("Select Subject:");
        subjectLabel.setFont(LABEL_FONT);
        subjectLabel.setForeground(TEXT_COLOR);

        subjectSelector = new JComboBox<>();
        subjectSelector.setFont(LABEL_FONT);
        subjectSelector.setBackground(Color.WHITE);
        subjectSelector.setForeground(TEXT_COLOR);
        subjectSelector.setPreferredSize(new Dimension(200, 30));

        // Grade range filter
        JLabel rangeLabel = new JLabel("Grade Range:");
        rangeLabel.setFont(LABEL_FONT);
        rangeLabel.setForeground(TEXT_COLOR);

        gradeRangeSelector = new JComboBox<>(new String[]{"All Grades", "Above 90", "80-89", "70-79", "60-69", "Below 60"});
        gradeRangeSelector.setFont(LABEL_FONT);
        gradeRangeSelector.setBackground(Color.WHITE);
        gradeRangeSelector.setForeground(TEXT_COLOR);
        gradeRangeSelector.setPreferredSize(new Dimension(150, 30));

        // Add action listeners to update chart when selections change
        subjectSelector.addActionListener(e -> updateSubjectChart());
        gradeRangeSelector.addActionListener(e -> updateSubjectChart());

        subjectControlPanel.add(subjectLabel);
        subjectControlPanel.add(subjectSelector);
        subjectControlPanel.add(Box.createHorizontalStrut(15));
        subjectControlPanel.add(rangeLabel);
        subjectControlPanel.add(gradeRangeSelector);

        subjectPanel.add(subjectControlPanel, BorderLayout.NORTH);
    }

    private void setupDistributionPanel() {
        JPanel distributionControlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        distributionControlPanel.setBackground(BACKGROUND_COLOR);
        distributionControlPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(SECONDARY_COLOR),
                "Grade Distribution Controls",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                LABEL_FONT,
                TEXT_COLOR));

        // Subject filter for distribution
        JLabel subjectFilterLabel = new JLabel("Filter by Subject:");
        subjectFilterLabel.setFont(LABEL_FONT);
        subjectFilterLabel.setForeground(TEXT_COLOR);

        distributionSubjectSelector = new JComboBox<>();
        distributionSubjectSelector.setFont(LABEL_FONT);
        distributionSubjectSelector.setBackground(Color.WHITE);
        distributionSubjectSelector.setForeground(TEXT_COLOR);
        distributionSubjectSelector.setPreferredSize(new Dimension(200, 30));
        distributionSubjectSelector.addItem(null); // Add null option for "All Subjects"

        // View type selector
        JLabel viewTypeLabel = new JLabel("View Type:");
        viewTypeLabel.setFont(LABEL_FONT);
        viewTypeLabel.setForeground(TEXT_COLOR);

        distributionViewSelector = new JComboBox<>(new String[]{"Pie Chart", "Bar Chart"});
        distributionViewSelector.setFont(LABEL_FONT);
        distributionViewSelector.setBackground(Color.WHITE);
        distributionViewSelector.setForeground(TEXT_COLOR);
        distributionViewSelector.setPreferredSize(new Dimension(150, 30));

        // Add action listeners to update chart when selections change
        distributionSubjectSelector.addActionListener(e -> updateDistributionChart());
        distributionViewSelector.addActionListener(e -> updateDistributionChart());

        distributionControlPanel.add(subjectFilterLabel);
        distributionControlPanel.add(distributionSubjectSelector);
        distributionControlPanel.add(Box.createHorizontalStrut(15));
        distributionControlPanel.add(viewTypeLabel);
        distributionControlPanel.add(distributionViewSelector);

        distributionPanel.add(distributionControlPanel, BorderLayout.NORTH);
    }

    private void refreshData() {
        try {
            // Load students and subjects for dropdowns
            updateSelectors();

            // Update all charts
            updateOverviewChart();
            updateStudentChart();
            updateSubjectChart();
            updateDistributionChart();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading data: " + e.getMessage(),
                    "Data Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void updateSelectors() throws Exception {
        // Clear existing items
        studentSelector.removeAllItems();
        subjectSelector.removeAllItems();
        distributionSubjectSelector.removeAllItems();

        // Add null option for "All Subjects" in distribution selector
        distributionSubjectSelector.addItem(null);

        // Add students to selector
        List<StudentDAO.Student> students = studentDAO.getAllStudents();
        for (StudentDAO.Student student : students) {
            studentSelector.addItem(student);
        }

        // Add subjects to selector
        List<SubjectDAO.Subject> subjects = subjectDAO.getAllSubjects();
        for (SubjectDAO.Subject subject : subjects) {
            subjectSelector.addItem(subject);
            distributionSubjectSelector.addItem(subject);
        }

        // Set renderers for displaying names instead of object references
        studentSelector.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof StudentDAO.Student) {
                    value = ((StudentDAO.Student) value).getName();
                }
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (isSelected) {
                    c.setBackground(PRIMARY_COLOR);
                    c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(Color.WHITE);
                    c.setForeground(TEXT_COLOR);
                }
                return c;
            }
        });

        subjectSelector.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof SubjectDAO.Subject) {
                    value = ((SubjectDAO.Subject) value).getName();
                }
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (isSelected) {
                    c.setBackground(PRIMARY_COLOR);
                    c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(Color.WHITE);
                    c.setForeground(TEXT_COLOR);
                }
                return c;
            }
        });

        distributionSubjectSelector.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value == null) {
                    value = "All Subjects";
                } else if (value instanceof SubjectDAO.Subject) {
                    value = ((SubjectDAO.Subject) value).getName();
                }
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (isSelected) {
                    c.setBackground(PRIMARY_COLOR);
                    c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(Color.WHITE);
                    c.setForeground(TEXT_COLOR);
                }
                return c;
            }
        });
    }

    private void updateOverviewChart() throws Exception {
        // Calculate average grades by subject
        Map<Integer, String> subjectIdToName = new HashMap<>();
        for (SubjectDAO.Subject subject : subjectDAO.getAllSubjects()) {
            subjectIdToName.put(subject.getSubjectId(), subject.getName());
        }

        Map<Integer, List<Double>> gradesBySubject = new HashMap<>();
        for (GradeDAO.Grade grade : gradeDAO.getAllGrades()) {
            if (!gradesBySubject.containsKey(grade.getSubjectId())) {
                gradesBySubject.put(grade.getSubjectId(), new ArrayList<>());
            }
            gradesBySubject.get(grade.getSubjectId()).add(grade.getGrade());
        }

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Map.Entry<Integer, List<Double>> entry : gradesBySubject.entrySet()) {
            Integer subjectId = entry.getKey();
            List<Double> grades = entry.getValue();
            double average = grades.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            dataset.addValue(average, "Average Grade", subjectIdToName.getOrDefault(subjectId, "Unknown"));
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Average Grades by Subject",
                "Subject",
                "Average Grade",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        // Apply custom styling to chart
        styleBarChart(chart, "Average Grades by Subject");

        // Create and add chart panel to overview tab
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(800, 500));
        chartPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Remove existing chart if any
        for (Component comp : overviewPanel.getComponents()) {
            if (comp instanceof ChartPanel) {
                overviewPanel.remove(comp);
            }
        }

        overviewPanel.add(chartPanel, BorderLayout.CENTER);
        overviewPanel.revalidate();
        overviewPanel.repaint();
    }

    private void updateStudentChart() {
        try {
            StudentDAO.Student selectedStudent = (StudentDAO.Student) studentSelector.getSelectedItem();
            if (selectedStudent == null) return;

            int studentId = selectedStudent.getStudentId();
            Map<Integer, String> subjectIdToName = new HashMap<>();
            for (SubjectDAO.Subject subject : subjectDAO.getAllSubjects()) {
                subjectIdToName.put(subject.getSubjectId(), subject.getName());
            }

            // Get all grades for the selected student
            List<GradeDAO.Grade> studentGrades = new ArrayList<>();
            for (GradeDAO.Grade grade : gradeDAO.getAllGrades()) {
                if (grade.getStudentId() == studentId) {
                    studentGrades.add(grade);
                }
            }

            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            for (GradeDAO.Grade grade : studentGrades) {
                dataset.addValue(grade.getGrade(), "Grade",
                        subjectIdToName.getOrDefault(grade.getSubjectId(), "Unknown"));
            }

            JFreeChart chart = ChartFactory.createBarChart(
                    "Grades for " + selectedStudent.getName(),
                    "Subject",
                    "Grade",
                    dataset,
                    PlotOrientation.VERTICAL,
                    true,
                    true,
                    false
            );

            // Apply custom styling to chart
            styleBarChart(chart, "Grades for " + selectedStudent.getName());

            ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new Dimension(800, 500));
            chartPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

            // Replace the existing chart in the student panel
            for (Component comp : studentPanel.getComponents()) {
                if (comp instanceof ChartPanel) {
                    studentPanel.remove(comp);
                }
            }

            studentPanel.add(chartPanel, BorderLayout.CENTER);
            studentPanel.revalidate();
            studentPanel.repaint();
        } catch (Exception e) {
            handleError("Error updating student chart", e);
        }
    }

    private void updateSubjectChart() {
        try {
            SubjectDAO.Subject selectedSubject = (SubjectDAO.Subject) subjectSelector.getSelectedItem();
            if (selectedSubject == null) return;

            int subjectId = selectedSubject.getSubjectId();
            Map<Integer, String> studentIdToName = new HashMap<>();
            for (StudentDAO.Student student : studentDAO.getAllStudents()) {
                studentIdToName.put(student.getStudentId(), student.getName());
            }

            // Get all grades for the selected subject
            List<GradeDAO.Grade> subjectGrades = new ArrayList<>();
            for (GradeDAO.Grade grade : gradeDAO.getAllGrades()) {
                if (grade.getSubjectId() == subjectId) {
                    subjectGrades.add(grade);
                }
            }

            // Apply grade range filter
            String selectedGradeRange = (String) gradeRangeSelector.getSelectedItem();
            List<GradeDAO.Grade> filteredGrades = filterGradesByRange(subjectGrades, selectedGradeRange);

            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            for (GradeDAO.Grade grade : filteredGrades) {
                dataset.addValue(grade.getGrade(), "Grade",
                        studentIdToName.getOrDefault(grade.getStudentId(), "Unknown"));
            }

            // Calculate statistics for filtered grades
            double average = filteredGrades.stream()
                    .mapToDouble(GradeDAO.Grade::getGrade)
                    .average()
                    .orElse(0.0);

            double highest = filteredGrades.stream()
                    .mapToDouble(GradeDAO.Grade::getGrade)
                    .max()
                    .orElse(0.0);

            double lowest = filteredGrades.stream()
                    .mapToDouble(GradeDAO.Grade::getGrade)
                    .min()
                    .orElse(0.0);

            JFreeChart chart = ChartFactory.createBarChart(
                    "Grades for " + selectedSubject.getName() +
                            " (" + selectedGradeRange + ")",
                    "Student",
                    "Grade",
                    dataset,
                    PlotOrientation.VERTICAL,
                    true,
                    true,
                    false
            );

            // Apply custom styling to chart
            styleBarChart(chart, "Grades for " + selectedSubject.getName());

            // Add statistics as subtitle
            TextTitle subtitle = new TextTitle(
                    "Average: " + String.format("%.2f", average) +
                            " | Highest: " + String.format("%.2f", highest) +
                            " | Lowest: " + String.format("%.2f", lowest),
                    new Font("Segoe UI", Font.ITALIC, 14)
            );
            subtitle.setPaint(TEXT_COLOR);
            chart.addSubtitle(subtitle);

            ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new Dimension(800, 500));
            chartPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

            // Replace the existing chart in the subject panel
            for (Component comp : subjectPanel.getComponents()) {
                if (comp instanceof ChartPanel) {
                    subjectPanel.remove(comp);
                }
            }

            subjectPanel.add(chartPanel, BorderLayout.CENTER);
            subjectPanel.revalidate();
            subjectPanel.repaint();
        } catch (Exception e) {
            handleError("Error updating subject chart", e);
        }
    }

    private void updateDistributionChart() {
        try {
            // Get selected subject filter (if any)
            SubjectDAO.Subject selectedSubject = (SubjectDAO.Subject) distributionSubjectSelector.getSelectedItem();

            // Get all grades
            List<GradeDAO.Grade> allGrades = gradeDAO.getAllGrades();

            // Apply subject filter if selected
            if (selectedSubject != null) {
                int subjectId = selectedSubject.getSubjectId();
                allGrades = allGrades.stream()
                        .filter(grade -> grade.getSubjectId() == subjectId)
                        .collect(Collectors.toList());
            }

            // Create grade ranges (e.g., A, B, C, D, F)
            Map<String, Integer> gradeCounts = new LinkedHashMap<>(); // LinkedHashMap to maintain order
            gradeCounts.put("A (90-100)", 0);
            gradeCounts.put("B (80-89)", 0);
            gradeCounts.put("C (70-79)", 0);
            gradeCounts.put("D (60-69)", 0);
            gradeCounts.put("F (0-59)", 0);

            // Count grades in each range
            for (GradeDAO.Grade grade : allGrades) {
                double value = grade.getGrade();
                if (value >= 90) {
                    gradeCounts.put("A (90-100)", gradeCounts.get("A (90-100)") + 1);
                } else if (value >= 80) {
                    gradeCounts.put("B (80-89)", gradeCounts.get("B (80-89)") + 1);
                } else if (value >= 70) {
                    gradeCounts.put("C (70-79)", gradeCounts.get("C (70-79)") + 1);
                } else if (value >= 60) {
                    gradeCounts.put("D (60-69)", gradeCounts.get("D (60-69)") + 1);
                } else {
                    gradeCounts.put("F (0-59)", gradeCounts.get("F (0-59)") + 1);
                }
            }

            String chartTitle = "Grade Distribution" +
                    (selectedSubject != null ? " for " + selectedSubject.getName() : "");

            JFreeChart chart;
            String viewType = (String) distributionViewSelector.getSelectedItem();

            if ("Bar Chart".equals(viewType)) {
                // Create bar chart dataset
                DefaultCategoryDataset barDataset = new DefaultCategoryDataset();
                for (Map.Entry<String, Integer> entry : gradeCounts.entrySet()) {
                    barDataset.addValue(entry.getValue(), "Count", entry.getKey());
                }

                chart = ChartFactory.createBarChart(
                        chartTitle,
                        "Grade Range",
                        "Number of Students",
                        barDataset,
                        PlotOrientation.VERTICAL,
                        true,
                        true,
                        false
                );

                // Apply custom styling to bar chart
                styleBarChart(chart, chartTitle);
            } else {
                // Default to pie chart (includes "Pie Chart" selection)
                DefaultPieDataset pieDataset = new DefaultPieDataset();
                for (Map.Entry<String, Integer> entry : gradeCounts.entrySet()) {
                    pieDataset.setValue(entry.getKey(), entry.getValue());
                }

                chart = ChartFactory.createPieChart(
                        chartTitle,
                        pieDataset,
                        true,
                        true,
                        false
                );

                // Apply custom styling to pie chart
                stylePieChart(chart, chartTitle);
            }

            ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new Dimension(800, 500));
            chartPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

            // Replace existing chart
            for (Component comp : distributionPanel.getComponents()) {
                if (comp instanceof ChartPanel) {
                    distributionPanel.remove(comp);
                }
            }

            distributionPanel.add(chartPanel, BorderLayout.CENTER);
            distributionPanel.revalidate();
            distributionPanel.repaint();
        } catch (Exception e) {
            handleError("Error updating distribution chart", e);
        }
    }

    // Helper method to style bar charts consistently
    private void styleBarChart(JFreeChart chart, String title) {
        // Set chart background
        chart.setBackgroundPaint(BACKGROUND_COLOR);

        // Style the title
        chart.getTitle().setFont(TITLE_FONT);
        chart.getTitle().setPaint(TEXT_COLOR);

        // Style the plot
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(GRID_COLOR);
        plot.setRangeGridlinePaint(GRID_COLOR);
        plot.setOutlinePaint(SECONDARY_COLOR);

        // Style the renderer
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setSeriesPaint(0, PRIMARY_COLOR);
        renderer.setShadowVisible(false);
        renderer.setDrawBarOutline(true);
        renderer.setDefaultOutlinePaint(SECONDARY_COLOR);

        // Style the domain axis (x-axis)
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setLabelFont(LABEL_FONT);
        domainAxis.setTickLabelFont(AXIS_FONT);
        domainAxis.setLabelPaint(TEXT_COLOR);
        domainAxis.setTickLabelPaint(TEXT_COLOR);

        // Style the range axis (y-axis)
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setLabelFont(LABEL_FONT);
        rangeAxis.setTickLabelFont(AXIS_FONT);
        rangeAxis.setLabelPaint(TEXT_COLOR);
        rangeAxis.setTickLabelPaint(TEXT_COLOR);

        // Style the legend
        chart.getLegend().setBackgroundPaint(BACKGROUND_COLOR);
        chart.getLegend().setItemFont(LABEL_FONT);
        chart.getLegend().setItemPaint(TEXT_COLOR);
    }

    // Helper method to style pie charts consistently
    private void stylePieChart(JFreeChart chart, String title) {
        // Set chart background
        chart.setBackgroundPaint(BACKGROUND_COLOR);

        // Style the title
        chart.getTitle().setFont(TITLE_FONT);
        chart.getTitle().setPaint(TEXT_COLOR);

        // Style the plot
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlinePaint(SECONDARY_COLOR);
        plot.setLabelFont(AXIS_FONT);
        plot.setLabelBackgroundPaint(BACKGROUND_COLOR);
        plot.setLabelOutlinePaint(SECONDARY_COLOR);
        plot.setLabelShadowPaint(null);
        plot.setShadowPaint(null);

        // Set custom section colors
        plot.setSectionPaint("A (90-100)", new Color(0, 153, 51));  // Green
        plot.setSectionPaint("B (80-89)", new Color(102, 204, 0));  // Light Green
        plot.setSectionPaint("C (70-79)", new Color(255, 204, 0));  // Yellow
        plot.setSectionPaint("D (60-69)", new Color(255, 153, 0));  // Orange
        plot.setSectionPaint("F (0-59)", new Color(204, 0, 0));     // Red

        // Style the legend
        chart.getLegend().setBackgroundPaint(BACKGROUND_COLOR);
        chart.getLegend().setItemFont(LABEL_FONT);
        chart.getLegend().setItemPaint(TEXT_COLOR);
    }

    // Helper method to filter grades by grade range
    private List<GradeDAO.Grade> filterGradesByRange(List<GradeDAO.Grade> grades, String gradeRange) {
        if ("All Grades".equals(gradeRange)) {
            return grades;
        } else if ("Above 90".equals(gradeRange)) {
            return grades.stream()
                    .filter(grade -> grade.getGrade() >= 90)
                    .collect(Collectors.toList());
        } else if ("80-89".equals(gradeRange)) {
            return grades.stream()
                    .filter(grade -> grade.getGrade() >= 80 && grade.getGrade() < 90)
                    .collect(Collectors.toList());
        } else if ("70-79".equals(gradeRange)) {
            return grades.stream()
                    .filter(grade -> grade.getGrade() >= 70 && grade.getGrade() < 80)
                    .collect(Collectors.toList());
        } else if ("60-69".equals(gradeRange)) {
            return grades.stream()
                    .filter(grade -> grade.getGrade() >= 60 && grade.getGrade() < 70)
                    .collect(Collectors.toList());
        } else if ("Below 60".equals(gradeRange)) {
            return grades.stream()
                    .filter(grade -> grade.getGrade() < 60)
                    .collect(Collectors.toList());
        }

        return grades; // Default to all grades
    }

    private void handleError(String message, Exception e) {
        JOptionPane.showMessageDialog(this,
                message + ": " + e.getMessage(),
                "Chart Error",
                JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }

    public static void main(String[] args) {
        // This method provided for testing
        SwingUtilities.invokeLater(() -> {
            try {
                // Create a sample connection - this would be replaced with actual DB connection
                Connection conn = null; // Replace with real connection

                GradesSummary app = new GradesSummary();
                app.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Error starting application: " + e.getMessage(),
                        "Startup Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}