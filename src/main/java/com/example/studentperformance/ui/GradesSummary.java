package com.example.studentperformance.ui;

import com.example.studentperformance.DatabaseConnection;
import com.example.studentperformance.dao.*;
import com.example.studentperformance.dao.GradeDAOImpl;
import com.example.studentperformance.dao.StudentDAOImpl;
import com.example.studentperformance.dao.SubjectDAOImpl;

import javax.swing.*;
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
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

public class GradesSummary extends JFrame {
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

        // Initialize DAOs
        initializeDAOs();

        // Setup UI components
        setupUI();

        // Load data and refresh charts
        refreshData();
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

        tabbedPane = new JTabbedPane();

        // Create panels for different analytics views
        overviewPanel = new JPanel(new BorderLayout());
        studentPanel = new JPanel(new BorderLayout());
        subjectPanel = new JPanel(new BorderLayout());
        distributionPanel = new JPanel(new BorderLayout());

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
        JPanel controlPanel = new JPanel();
        JButton refreshBtn = new JButton("Refresh");
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
        JPanel studentControlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        // Student selector
        studentSelector = new JComboBox<>();
        studentControlPanel.add(new JLabel("Select Student:"));
        studentControlPanel.add(studentSelector);

        // Add action listener to update chart when selection changes
        studentSelector.addActionListener(e -> updateStudentChart());

        studentPanel.add(studentControlPanel, BorderLayout.NORTH);
    }

    private void setupSubjectPanel() {
        JPanel subjectControlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        // Subject selector
        subjectSelector = new JComboBox<>();
        subjectControlPanel.add(new JLabel("Select Subject:"));
        subjectControlPanel.add(subjectSelector);

        // Grade range filter
        gradeRangeSelector = new JComboBox<>(new String[]{"All Grades", "Above 90", "80-89", "70-79", "60-69", "Below 60"});
        subjectControlPanel.add(new JLabel("Grade Range:"));
        subjectControlPanel.add(gradeRangeSelector);

        // Add action listeners to update chart when selections change
        subjectSelector.addActionListener(e -> updateSubjectChart());
        gradeRangeSelector.addActionListener(e -> updateSubjectChart());

        subjectPanel.add(subjectControlPanel, BorderLayout.NORTH);
    }

    private void setupDistributionPanel() {
        JPanel distributionControlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        // Subject filter for distribution
        distributionSubjectSelector = new JComboBox<>();
        distributionSubjectSelector.addItem(null); // Add null option for "All Subjects"
        distributionControlPanel.add(new JLabel("Filter by Subject:"));
        distributionControlPanel.add(distributionSubjectSelector);

        // View type selector
        distributionViewSelector = new JComboBox<>(new String[]{"Pie Chart", "Bar Chart"});
        distributionControlPanel.add(new JLabel("View Type:"));
        distributionControlPanel.add(distributionViewSelector);

        // Add action listeners to update chart when selections change
        distributionSubjectSelector.addActionListener(e -> updateDistributionChart());
        distributionViewSelector.addActionListener(e -> updateDistributionChart());

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
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });

        subjectSelector.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof SubjectDAO.Subject) {
                    value = ((SubjectDAO.Subject) value).getName();
                }
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
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
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
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

        // Customize chart appearance
        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(79, 129, 189));

        // Create and add chart panel to overview tab
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(800, 500));

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

            ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new Dimension(800, 500));

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
                            " (" + selectedGradeRange + ")" +
                            " (Avg: " + String.format("%.2f", average) +
                            ", High: " + String.format("%.2f", highest) +
                            ", Low: " + String.format("%.2f", lowest) + ")",
                    "Student",
                    "Grade",
                    dataset,
                    PlotOrientation.VERTICAL,
                    true,
                    true,
                    false
            );

            ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new Dimension(800, 500));

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
            Map<String, Integer> gradeCounts = new HashMap<>();
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

            JFreeChart chart;
            String viewType = (String) distributionViewSelector.getSelectedItem();

            if ("Bar Chart".equals(viewType)) {
                // Create bar chart dataset
                DefaultCategoryDataset barDataset = new DefaultCategoryDataset();
                for (Map.Entry<String, Integer> entry : gradeCounts.entrySet()) {
                    barDataset.addValue(entry.getValue(), "Count", entry.getKey());
                }

                chart = ChartFactory.createBarChart(
                        "Grade Distribution" + (selectedSubject != null ? " for " + selectedSubject.getName() : ""),
                        "Grade Range",
                        "Number of Students",
                        barDataset,
                        PlotOrientation.VERTICAL,
                        true,
                        true,
                        false
                );
            } else {
                // Default to pie chart (includes "Pie Chart" selection)
                DefaultPieDataset pieDataset = new DefaultPieDataset();
                for (Map.Entry<String, Integer> entry : gradeCounts.entrySet()) {
                    pieDataset.setValue(entry.getKey(), entry.getValue());
                }

                chart = ChartFactory.createPieChart(
                        "Grade Distribution" + (selectedSubject != null ? " for " + selectedSubject.getName() : ""),
                        pieDataset,
                        true,
                        true,
                        false
                );
            }

            ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new Dimension(800, 500));

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