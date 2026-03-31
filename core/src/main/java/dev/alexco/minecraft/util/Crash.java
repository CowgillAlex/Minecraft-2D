package dev.alexco.minecraft.util;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import dev.alexco.minecraft.Version;

/**
 * Utility class for displaying crash screens when the game encounters fatal errors.
 * Provides a user-friendly interface to view crash information and system details.
 */
public class Crash {

    /**
     * Displays a crash screen window when the game encounters a fatal error.
     * The screen shows system information, error details, and allows copying
     * the crash report for bug reporting purposes.
     *
     * @param error The throwable that caused the crash
     */
    public static void showCrashScreen(Throwable error) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = createCrashFrame();
            JPanel mainPanel = createMainPanel();

            // Add components to the main panel
            mainPanel.add(createTitleLabel());
            mainPanel.add(createFullReportScrollPane(error));
            mainPanel.add(createButtonPanel(error));

            // Wrap in scrollable container and display
            JScrollPane scrollPane = new JScrollPane(mainPanel);
            scrollPane.setPreferredSize(new Dimension(850, 650));

            frame.add(scrollPane);
            frame.setVisible(true);
            frame.toFront();
            frame.requestFocus();
        });
    }

    /**
     * Creates and configures the main crash window frame.
     *
     * @return Configured JFrame for the crash screen
     */
    private static JFrame createCrashFrame() {
        JFrame frame = new JFrame("Game Crashed - Error Report");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 700);
        frame.setLocationRelativeTo(null); // Centre on screen
        return frame;
    }

    /**
     * Creates the main panel container with proper layout and borders.
     *
     * @return Configured main panel
     */
    private static JPanel createMainPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        return panel;
    }

    /**
     * Creates the title label for the crash screen.
     *
     * @return Styled title label
     */
    private static JLabel createTitleLabel() {
        JLabel titleLabel = new JLabel("Oh no! The game crashed! Please report this on GitHub Issues");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        return titleLabel;
    }

    /**
     * Creates a scrollable text area containing the complete crash report
     * including system info, crash details, and stack trace.
     *
     * @param error The error that caused the crash
     * @return Scrollable pane with full crash report
     */
    private static JScrollPane createFullReportScrollPane(Throwable error) {
        String fullReport = generateFullReport(error);

        JTextArea reportArea = new JTextArea(fullReport);
        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        reportArea.setEditable(false);
        reportArea.setWrapStyleWord(true);
        reportArea.setLineWrap(true);
        reportArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(reportArea);
        scrollPane.setPreferredSize(new Dimension(800, 450));
        scrollPane.setBorder(BorderFactory.createTitledBorder("Crash Report - Select All (Ctrl+A) to Copy"));

        return scrollPane;
    }

    /**
     * Generates the complete crash report containing all relevant information.
     *
     * @param error The error that caused the crash
     * @return Complete formatted crash report
     */
    private static String generateFullReport(Throwable error) {

        return getSystemInfo() +
            getAdditionalInfo(error) +
            getStackTrace(error);
    }

    /**
     * Creates the button panel with copy and exit functionality.
     *
     * @param error The error that caused the crash (for copying full report)
     * @return Panel containing action buttons
     */
    private static JPanel createButtonPanel(Throwable error) {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        // Status label for copy feedback
        JLabel statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton copyButton = new JButton("Copy Full Report");
        copyButton.addActionListener(e -> {
            String fullReport = generateFullReport(error);
            boolean success = copyToClipboard(fullReport);
            if (success) {
                statusLabel.setText("Text copied successfully!");
                // Clear the message after 3 seconds
                javax.swing.Timer timer = new javax.swing.Timer(3000, evt -> statusLabel.setText(" "));
                timer.setRepeats(false);
                timer.start();
            } else {
                statusLabel.setText("Failed to copy text");
                javax.swing.Timer timer = new javax.swing.Timer(3000, evt -> statusLabel.setText(" "));
                timer.setRepeats(false);
                timer.start();
            }
        });

        JButton exitButton = new JButton("Exit Game");
        exitButton.addActionListener(e -> System.exit(1));

        buttonPanel.add(copyButton);
        buttonPanel.add(exitButton);

        // Add status label below buttons
        JPanel containerPanel = new JPanel();
        containerPanel.setLayout(new BoxLayout(containerPanel, BoxLayout.Y_AXIS));
        containerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        containerPanel.add(buttonPanel);
        containerPanel.add(statusLabel);

        return containerPanel;
    }

    /**
     * Gathers comprehensive system information for crash reporting.
     *
     * @return Formatted string containing system specifications
     */
    private static String getSystemInfo() {
        Runtime runtime = Runtime.getRuntime();

        return String.format(
            "=== SYSTEM INFORMATION ===\n" +
            "OS: %s (%s)\n" +
            "Java Version: %s\n" +
            "Java Vendor: %s\n" +
            "CPU Cores: %d\n" +
            "Allocated Memory: %d MB\n" +
            "Total Memory: %d MB\n" +
            "Free Memory: %d MB\n" +
            "Used Memory: %d MB\n",
            System.getProperty("os.name"),
            System.getProperty("os.arch"),
            System.getProperty("java.version"),
            System.getProperty("java.vendor"),
            runtime.availableProcessors(),
            runtime.maxMemory() / (1024 * 1024),
            runtime.totalMemory() / (1024 * 1024),
            runtime.freeMemory() / (1024 * 1024),
            (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
        );
    }

    /**
     * Gathers additional crash-specific information including thread state,
     * game version, and error details.
     *
     * @param error The error that caused the crash
     * @return Formatted string with additional crash information
     */
    private static String getAdditionalInfo(Throwable error) {
        StringBuilder info = new StringBuilder();

        info.append("\n=== CRASH INFORMATION ===\n");

        // Thread information
        Thread currentThread = Thread.currentThread();
        info.append(String.format("Thread: %s (State: %s)\n",
            currentThread.getName(), currentThread.getState()));

        // Game version
        info.append(String.format("Game Version: %s\n", Version.VERSION_STRING));

        // Java command line arguments
        info.append("Java Arguments: ");
        try {
            String javaCommand = System.getProperty("sun.java.command");
            if (javaCommand != null) {
                String[] args = javaCommand.split(" ");
                for (String arg : args) {
                    info.append(arg).append(" ");
                }
            } else {
                info.append("N/A");
            }
        } catch (Exception e) {
            info.append("Unable to retrieve");
        }
        info.append("\n");

        // Error information
        info.append(String.format("Error Type: %s\n", error.getClass().getSimpleName()));
        info.append(String.format("Error Message: %s\n",
            error.getMessage() != null ? error.getMessage() : "No message provided"));

        // Cause chain
        Throwable cause = error.getCause();
        if (cause != null) {
            info.append(String.format("Root Cause: %s - %s\n",
                cause.getClass().getSimpleName(),
                cause.getMessage() != null ? cause.getMessage() : "No message"));
        }

        return info.toString();
    }

    /**
     * Converts a throwable's stack trace to a formatted string.
     *
     * @param error The throwable to get the stack trace from
     * @return Formatted stack trace string
     */
    private static String getStackTrace(Throwable error) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        printWriter.println("\n=== STACK TRACE ===");
        error.printStackTrace(printWriter);

        return stringWriter.toString();
    }

    /**
     * Copies the provided text to the system clipboard.
     * Uses AWT clipboard since LibGDX clipboard may not be initialised during crashes.
     *
     * @param text The text to copy to clipboard
     * @return true if copying was successful, false otherwise
     */
    private static boolean copyToClipboard(String text) {
        try {
            StringSelection selection = new StringSelection(text);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, null);
            Logger.INFO("Crash report copied to clipboard successfully.");
            return true;
        } catch (Exception e) {
            Logger.INFO("Failed to copy crash report to clipboard: " + e.getMessage());
            return false;
        }
    }
}
