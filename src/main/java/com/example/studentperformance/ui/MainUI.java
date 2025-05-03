package com.example.studentperformance.ui;

import com.example.studentperformance.DatabaseConnection;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

public class MainUI {
    // Constants for styling
    private static final Color PRIMARY_COLOR = new Color(65, 105, 225);  // Royal Blue
    private static final Color SECONDARY_COLOR = new Color(70, 130, 180); // Steel Blue
    private static final Color BACKGROUND_COLOR = new Color(240, 248, 255); // Alice Blue
    private static final Color TEXT_COLOR = new Color(25, 25, 112); // Midnight Blue
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font MENU_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 16);
    private static final int ANIMATION_DURATION = 200; // milliseconds

    private JFrame frame;
    private JPanel contentPane;
    private JLabel statusLabel;
    private Map<JButton, Timer> hoverTimers = new HashMap<>();
    private Map<JButton, Float> buttonScales = new HashMap<>();

    // Icons for the menu items and buttons
    private ImageIcon studentIcon;
    private ImageIcon subjectIcon;
    private ImageIcon gradeIcon;
    private ImageIcon attendanceIcon;
    private ImageIcon logoIcon;

    public MainUI() {
        loadIcons();
        initializeUI();
    }

    private void loadIcons() {
        try {
            // Load icons from resources (You'll need to add these to your project resources)
            ClassLoader classLoader = getClass().getClassLoader();

            // Try to load the icons, or use placeholders if not found
            try (InputStream is = classLoader.getResourceAsStream("icons/student.png")) {
                studentIcon = is != null ? new ImageIcon(ImageIO.read(is)) : createPlaceholderIcon("Students", PRIMARY_COLOR);
            }

            try (InputStream is = classLoader.getResourceAsStream("icons/subject.png")) {
                subjectIcon = is != null ? new ImageIcon(ImageIO.read(is)) : createPlaceholderIcon("Subjects", SECONDARY_COLOR);
            }

            try (InputStream is = classLoader.getResourceAsStream("icons/grade.png")) {
                gradeIcon = is != null ? new ImageIcon(ImageIO.read(is)) : createPlaceholderIcon("Grades", new Color(60, 179, 113));
            }

            try (InputStream is = classLoader.getResourceAsStream("icons/attendance.png")) {
                attendanceIcon = is != null ? new ImageIcon(ImageIO.read(is)) : createPlaceholderIcon("Attendance", new Color(255, 165, 0));
            }

            try (InputStream is = classLoader.getResourceAsStream("icons/logo.png")) {
                logoIcon = is != null ? new ImageIcon(ImageIO.read(is)) : createPlaceholderIcon("SPM", PRIMARY_COLOR);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Create placeholder icons if loading fails
            studentIcon = createPlaceholderIcon("Students", PRIMARY_COLOR);
            subjectIcon = createPlaceholderIcon("Subjects", SECONDARY_COLOR);
            gradeIcon = createPlaceholderIcon("Grades", new Color(60, 179, 113));
            attendanceIcon = createPlaceholderIcon("Attendance", new Color(255, 165, 0));
            logoIcon = createPlaceholderIcon("SPM", PRIMARY_COLOR);
        }
    }

    private ImageIcon createPlaceholderIcon(String text, Color bgColor) {
        // Create a placeholder icon with text
        int width = 64;
        int height = 64;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(bgColor);
        g2d.fillRoundRect(0, 0, width, height, 16, 16);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getHeight();
        g2d.drawString(text, (width - textWidth) / 2, height / 2 + textHeight / 4);

        g2d.dispose();
        return new ImageIcon(image);
    }

    private void initializeUI() {
        frame = new JFrame("Student Performance Monitoring");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(true);
        frame.setSize(new Dimension(900, 700));

        // Custom decorated frame (optional)
        // frame.setUndecorated(true);
        // frame.setShape(new RoundRectangle2D.Double(0, 0, frame.getWidth(), frame.getHeight(), 20, 20));

        // Set application icon
        if (logoIcon != null) {
            frame.setIconImage(logoIcon.getImage());
        }

        // Create modern menu bar
        JMenuBar menuBar = createModernMenuBar();
        frame.setJMenuBar(menuBar);

        // Create main content pane with gradient background
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

        // Add header with logo and title
        JPanel headerPanel = createHeaderPanel();
        contentPane.add(headerPanel, BorderLayout.NORTH);

        // Create main dashboard panel with animation effects
        JPanel dashboardPanel = createDashboardPanel();
        contentPane.add(dashboardPanel, BorderLayout.CENTER);

        // Add status bar at the bottom
        JPanel statusPanel = createStatusPanel();
        contentPane.add(statusPanel, BorderLayout.SOUTH);

        frame.setContentPane(contentPane);
        frame.setLocationRelativeTo(null); // Center on screen

        // Add a window listener to show welcome message
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                // Show welcome animation
                showWelcomeAnimation();
            }
        });
    }

    private JMenuBar createModernMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(PRIMARY_COLOR);
        menuBar.setBorder(BorderFactory.createEmptyBorder());

        // File Menu
        JMenu fileMenu = createMenu("File", Color.WHITE);

        JMenuItem dashboardItem = createMenuItem("Dashboard", e -> {
            // Reset to main dashboard view
            statusLabel.setText("Dashboard loaded");
        });

        JMenuItem exportItem = createMenuItem("Export Data", e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Export Data");
            int userSelection = fileChooser.showSaveDialog(frame);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                statusLabel.setText("Data exported to: " + fileChooser.getSelectedFile().getName());
            }
        });

        JMenuItem exitItem = createMenuItem("Exit", e -> {
            // Show confirmation dialog before exiting
            int response = JOptionPane.showConfirmDialog(
                    frame,
                    "Are you sure you want to exit?",
                    "Confirm Exit",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (response == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });

        fileMenu.add(dashboardItem);
        fileMenu.add(exportItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        // Reports Menu
        JMenu reportsMenu = createMenu("Reports", Color.WHITE);

        JMenuItem gradeSummaryItem = createMenuItem("Grade Summary", e -> {
            statusLabel.setText("Grade summary report selected");
            GradesSummary gradesSummary = new GradesSummary();
            gradesSummary.setVisible(true);
        });

        JMenuItem attendanceReportItem = createMenuItem("Attendance Report", e -> {
            statusLabel.setText("Attendance report selected");
            AttendanceReport attendanceReport = new AttendanceReport();
            attendanceReport.setVisible(true);
        });

        JMenuItem progressReportItem = createMenuItem("Student Progress", e -> {
            statusLabel.setText("Student progress report selected");
            JOptionPane.showMessageDialog(
                    frame,
                    "Student progress report will be available in the next update.",
                    "Coming Soon",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });

        reportsMenu.add(gradeSummaryItem);
        reportsMenu.add(attendanceReportItem);
        reportsMenu.add(progressReportItem);

        // Help Menu
        JMenu helpMenu = createMenu("Help", Color.WHITE);

        JMenuItem userGuideItem = createMenuItem("User Guide", e -> {
            statusLabel.setText("User guide selected");
            JOptionPane.showMessageDialog(
                    frame,
                    "The user guide will open in a separate window.",
                    "User Guide",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });

        JMenuItem aboutItem = createMenuItem("About", e -> {
            // Create a custom about dialog
            JDialog aboutDialog = new JDialog(frame, "About Student Performance Monitoring", true);
            aboutDialog.setLayout(new BorderLayout());

            JPanel aboutPanel = new JPanel();
            aboutPanel.setLayout(new BoxLayout(aboutPanel, BoxLayout.Y_AXIS));
            aboutPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            aboutPanel.setBackground(BACKGROUND_COLOR);

            JLabel titleLabel = new JLabel("Student Performance Monitoring");
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
            titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel versionLabel = new JLabel("Version 2.0");
            versionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            versionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel descriptionLabel = new JLabel("<html><div style='text-align: center; width: 300px;'>" +
                    "A comprehensive solution for educational institutions to track and analyze " +
                    "student performance including grades, assignments, and attendance.</div></html>");
            descriptionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JButton closeButton = new JButton("Close");
            closeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            closeButton.addActionListener(event -> aboutDialog.dispose());

            aboutPanel.add(Box.createVerticalStrut(10));
            aboutPanel.add(titleLabel);
            aboutPanel.add(Box.createVerticalStrut(10));
            aboutPanel.add(versionLabel);
            aboutPanel.add(Box.createVerticalStrut(20));
            aboutPanel.add(descriptionLabel);
            aboutPanel.add(Box.createVerticalStrut(20));
            aboutPanel.add(closeButton);

            aboutDialog.add(aboutPanel, BorderLayout.CENTER);
            aboutDialog.setSize(400, 300);
            aboutDialog.setLocationRelativeTo(frame);
            aboutDialog.setVisible(true);
        });

        helpMenu.add(userGuideItem);
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(reportsMenu);
        menuBar.add(helpMenu);

        return menuBar;
    }

    private JMenu createMenu(String title, Color foreground) {
        JMenu menu = new JMenu(title);
        menu.setForeground(foreground);
        menu.setFont(MENU_FONT);
        return menu;
    }

    private JMenuItem createMenuItem(String title, ActionListener action) {
        JMenuItem item = new JMenuItem(title);
        item.setFont(MENU_FONT);
        item.addActionListener(action);

        // Add hover effect
        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                item.setBackground(SECONDARY_COLOR);
                item.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                item.setBackground(UIManager.getColor("MenuItem.background"));
                item.setForeground(UIManager.getColor("MenuItem.foreground"));
            }
        });

        return item;
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        // Logo on the left
        JLabel logoLabel = new JLabel(logoIcon);
        logoLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));
        headerPanel.add(logoLabel, BorderLayout.WEST);

        // Title in the center
        JLabel titleLabel = new JLabel("Student Performance Monitoring System");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        // Current date/time on the right
        JLabel dateLabel = new JLabel(new java.text.SimpleDateFormat("EEEE, MMMM d, yyyy").format(new java.util.Date()));
        dateLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        dateLabel.setForeground(TEXT_COLOR);
        headerPanel.add(dateLabel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createDashboardPanel() {
        JPanel dashboardPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        dashboardPanel.setOpaque(false);

        // Create the four main action buttons with modern design
        JButton manageStudentsButton = createDashboardButton("Manage Students", studentIcon, e -> {
            statusLabel.setText("Opening student management...");
            ManageStudentsUI manageStudentsUI = new ManageStudentsUI(DatabaseConnection.getConnection());
            manageStudentsUI.setVisible(true);
        });

        JButton manageSubjectsButton = createDashboardButton("Manage Subjects", subjectIcon, e -> {
            statusLabel.setText("Opening subject management...");
            ManageSubjectsUI manageSubjectsUI = new ManageSubjectsUI();
            manageSubjectsUI.setVisible(true);
        });

        JButton manageGradesButton = createDashboardButton("Manage Grades", gradeIcon, e -> {
            statusLabel.setText("Opening grade management...");
            ManageGradesUI manageGradesUI = new ManageGradesUI(DatabaseConnection.getConnection());
            manageGradesUI.setVisible(true);
        });

        JButton manageAttendanceButton = createDashboardButton("Manage Attendance", attendanceIcon, e -> {
            statusLabel.setText("Opening attendance management...");
            ManageAttendanceUI manageAttendanceUI = new ManageAttendanceUI();
            manageAttendanceUI.setVisible(true);
        });

        dashboardPanel.add(manageStudentsButton);
        dashboardPanel.add(manageSubjectsButton);
        dashboardPanel.add(manageGradesButton);
        dashboardPanel.add(manageAttendanceButton);

        return dashboardPanel;
    }

    private JButton createDashboardButton(String text, ImageIcon icon, ActionListener action) {
        JButton button = new JButton(text) {
            // Custom rendering for rounded corners and gradient
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int width = getWidth();
                int height = getHeight();
                float scale = buttonScales.getOrDefault(this, 1.0f);

                // Apply scale transformation
                if (scale != 1.0f) {
                    int scaledWidth = (int) (width * scale);
                    int scaledHeight = (int) (height * scale);
                    int x = (width - scaledWidth) / 2;
                    int y = (height - scaledHeight) / 2;

                    g2.translate(x, y);
                    g2.scale(scale, scale);
                }

                // Draw gradient background
                GradientPaint gradient = new GradientPaint(
                        0, 0, PRIMARY_COLOR,
                        0, height, SECONDARY_COLOR
                );
                g2.setPaint(gradient);
                g2.fill(new RoundRectangle2D.Float(0, 0, width, height, 20, 20));

                // Add subtle shadow effect
                g2.setColor(new Color(0, 0, 0, 30));
                g2.fill(new RoundRectangle2D.Float(3, 3, width, height, 20, 20));

                // Draw button content
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

        // Set icon and text positioning
        button.setIcon(icon);
        button.setVerticalTextPosition(SwingConstants.BOTTOM);
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setIconTextGap(10);

        // Initialize the button scale
        buttonScales.put(button, 1.0f);

        // Add the action listener
        button.addActionListener(action);

        // Add hover animation
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                animateButtonScale(button, 1.0f, 1.05f);
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                animateButtonScale(button, buttonScales.get(button), 1.0f);
                button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }

            @Override
            public void mousePressed(MouseEvent e) {
                animateButtonScale(button, buttonScales.get(button), 0.95f);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                animateButtonScale(button, buttonScales.get(button), 1.0f);
            }
        });

        return button;
    }

    private void animateButtonScale(JButton button, float startScale, float endScale) {
        // Cancel any existing animation
        Timer oldTimer = hoverTimers.get(button);
        if (oldTimer != null) {
            oldTimer.stop();
        }

        final long startTime = System.currentTimeMillis();
        final float scaleDiff = endScale - startScale;

        Timer timer = new Timer(10, null);
        timer.addActionListener(e -> {
            long elapsed = System.currentTimeMillis() - startTime;
            float progress = Math.min(1.0f, (float) elapsed / ANIMATION_DURATION);

            // Ease in-out function
            progress = progress < 0.5
                    ? 2 * progress * progress
                    : 1 - (float)(Math.pow(-2 * progress + 2, 2) / 2);

            float newScale = startScale + scaleDiff * progress;
            buttonScales.put(button, newScale);
            button.repaint();

            if (progress >= 1.0) {
                timer.stop();
                hoverTimers.remove(button);
            }
        });

        hoverTimers.put(button, timer);
        timer.start();
    }

    private JPanel createStatusPanel() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(new Color(240, 240, 240));
        statusPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        statusLabel = new JLabel("Ready");
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));

        JLabel versionLabel = new JLabel("v2.0");
        versionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        versionLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(versionLabel, BorderLayout.EAST);

        return statusPanel;
    }

    private void showWelcomeAnimation() {
        // This method shows a welcome message with animation
        final JWindow splashScreen = new JWindow(frame);
        splashScreen.setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                int w = getWidth();
                int h = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, PRIMARY_COLOR, w, h, SECONDARY_COLOR);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
                g2d.dispose();
            }
        };

        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel iconLabel = new JLabel(logoIcon);
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel welcomeLabel = new JLabel("Welcome to Student Performance Monitoring System");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel loadingLabel = new JLabel("Loading...");
        loadingLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        loadingLabel.setForeground(Color.WHITE);
        loadingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        contentPanel.add(Box.createVerticalGlue());
        contentPanel.add(iconLabel);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(welcomeLabel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(loadingLabel);
        contentPanel.add(Box.createVerticalGlue());

        splashScreen.add(contentPanel);
        splashScreen.setSize(400, 250);
        splashScreen.setLocationRelativeTo(null);
        splashScreen.setOpacity(0.0f);
        splashScreen.setVisible(true);

        // Fade-in animation
        Timer fadeInTimer = new Timer(20, null);
        fadeInTimer.addActionListener(e -> {
            float opacity = splashScreen.getOpacity();
            opacity += 0.05f;
            splashScreen.setOpacity(Math.min(opacity, 0.95f));

            if (opacity >= 0.95f) {
                fadeInTimer.stop();

                // Delay before closing
                Timer delayTimer = new Timer(1500, e1 -> {
                    // Fade-out animation
                    Timer fadeOutTimer = new Timer(20, null);
                    fadeOutTimer.addActionListener(e2 -> {
                        float currentOpacity = splashScreen.getOpacity();
                        currentOpacity -= 0.05f;
                        splashScreen.setOpacity(Math.max(0.0f, currentOpacity));

                        if (currentOpacity <= 0.0f) {
                            fadeOutTimer.stop();
                            splashScreen.dispose();
                            statusLabel.setText("Welcome to Student Performance Monitoring System");
                        }
                    });
                    fadeOutTimer.start();
                });
                delayTimer.setRepeats(false);
                delayTimer.start();
            }
        });
        fadeInTimer.start();
    }

    public void setVisible(boolean visible) {
        frame.setVisible(visible);
    }

    public static void main(String[] args) {
        try {
            // Set system look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            // Customize some UI defaults for a more modern look
            UIManager.put("Button.arc", 15);
            UIManager.put("Component.arc", 15);
            UIManager.put("ProgressBar.arc", 15);
            UIManager.put("TextComponent.arc", 15);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Ensure the UI is created on the EDT
        SwingUtilities.invokeLater(() -> {
            MainUI mainUI = new MainUI();
            mainUI.setVisible(true);
        });
    }
}