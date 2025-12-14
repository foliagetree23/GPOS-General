package com.pos.ui;

import com.pos.manager.DataManager;
import com.pos.printer.ReceiptPrinter;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Reports Panel - Interface for generating and viewing sales reports
 */
public class ReportsPanel extends JPanel {
    private DataManager dataManager;
    private MainPOS mainPOS;

    // Report type selection
    private JComboBox<String> reportTypeComboBox;
    private JButton generateReportButton;

    // Date range selection
    private JComboBox<String> dateRangeComboBox;
    private JTextField startDateField;
    private JTextField endDateField;

    // Report display
    private JTextArea reportTextArea;
    private JScrollPane reportScrollPane;

    // Report actions
    private JButton printReportButton;
    private JButton exportReportButton;

    public ReportsPanel(DataManager dataManager, MainPOS mainPOS) {
        this.dataManager = dataManager;
        this.mainPOS = mainPOS;

        initializeComponents();
        setupLayout();
        setupEventHandlers();

        generateDefaultReport();
    }

    private void initializeComponents() {
        // Report type selection
        reportTypeComboBox = new JComboBox<>();
        reportTypeComboBox.addItem("Sales Summary");
        reportTypeComboBox.addItem("Daily Sales");
        reportTypeComboBox.addItem("Sales by Category");
        reportTypeComboBox.addItem("Top Products");
        reportTypeComboBox.addItem("Low Stock Alert");
        reportTypeComboBox.setSelectedItem("Sales Summary");

        generateReportButton = new JButton("Generate Report");
        generateReportButton.setMnemonic('G');

        // Date range selection
        dateRangeComboBox = new JComboBox<>();
        dateRangeComboBox.addItem("Today");
        dateRangeComboBox.addItem("Last 7 Days");
        dateRangeComboBox.addItem("Last 30 Days");
        dateRangeComboBox.addItem("This Month");
        dateRangeComboBox.addItem("Last Month");
        dateRangeComboBox.addItem("Custom Range");
        dateRangeComboBox.setSelectedItem("This Month");

        startDateField = new JTextField(10);
        endDateField = new JTextField(10);
        startDateField.setEnabled(false);
        endDateField.setEnabled(false);

        // Report display
        reportTextArea = new JTextArea();
        reportTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        reportTextArea.setEditable(false);
        reportScrollPane = new JScrollPane(reportTextArea);
        reportScrollPane.setPreferredSize(new Dimension(600, 400));

        // Report actions
        printReportButton = new JButton("Print Report");
        printReportButton.setMnemonic('P');

        exportReportButton = new JButton("Export Report");
        exportReportButton.setMnemonic('E');
    }


    private void setupLayout() {
        setLayout(new BorderLayout());

        // Top panel - Navigation and report controls
        JPanel topPanel = new JPanel(new BorderLayout());

        // Quick navigation panel
        JPanel navigationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        navigationPanel.setBorder(BorderFactory.createTitledBorder("Navigation"));

        JButton salesBtn = new JButton("🛒 Sales");
        salesBtn.addActionListener(e -> mainPOS.showPanel("Sales"));

        JButton productsBtn = new JButton("📦 Products");
        productsBtn.addActionListener(e -> mainPOS.showPanel("Products"));

        JButton transactionsBtn = new JButton("📋 History");
        transactionsBtn.addActionListener(e -> mainPOS.showPanel("Transactions"));

        JButton reportsBtn = new JButton("📊 Reports (Current)");
        reportsBtn.setBackground(new Color(34, 139, 34));
        reportsBtn.setForeground(Color.WHITE);
        reportsBtn.setEnabled(false);

        navigationPanel.add(salesBtn);
        navigationPanel.add(productsBtn);
        navigationPanel.add(transactionsBtn);
        navigationPanel.add(reportsBtn);

        // Report controls panel
        JPanel controlPanel = new JPanel(new GridBagLayout());
        controlPanel.setBorder(BorderFactory.createTitledBorder("Report Controls"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        controlPanel.add(new JLabel("Report Type:"), gbc);
        gbc.gridx = 1;
        controlPanel.add(reportTypeComboBox, gbc);
        gbc.gridx = 2;
        controlPanel.add(generateReportButton, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        controlPanel.add(new JLabel("Date Range:"), gbc);
        gbc.gridx = 1;
        controlPanel.add(dateRangeComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        controlPanel.add(new JLabel("Start Date:"), gbc);
        gbc.gridx = 1;
        controlPanel.add(startDateField, gbc);
        gbc.gridx = 2;
        controlPanel.add(new JLabel("(YYYY-MM-DD)"), gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        controlPanel.add(new JLabel("End Date:"), gbc);
        gbc.gridx = 1;
        controlPanel.add(endDateField, gbc);
        gbc.gridx = 2;
        controlPanel.add(new JLabel("(YYYY-MM-DD)"), gbc);

        topPanel.add(navigationPanel, BorderLayout.WEST);
        topPanel.add(controlPanel, BorderLayout.CENTER);

        // Center panel - Report display
        JPanel reportPanel = new JPanel(new BorderLayout());
        reportPanel.setBorder(BorderFactory.createTitledBorder("Report Output"));
        reportPanel.add(reportScrollPane, BorderLayout.CENTER);

        // Bottom panel - Report actions
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actionPanel.add(printReportButton);
        actionPanel.add(exportReportButton);

        // Add all panels to main panel
        add(topPanel, BorderLayout.NORTH);
        add(reportPanel, BorderLayout.CENTER);
        add(actionPanel, BorderLayout.SOUTH);
    }

    private void setupEventHandlers() {
        // Report type change
        reportTypeComboBox.addActionListener(e -> updateDateRangeFields());

        // Date range change
        dateRangeComboBox.addActionListener(e -> updateDateRangeFields());

        // Generate report button
        generateReportButton.addActionListener(e -> generateReport());

        // Print report button
        printReportButton.addActionListener(e -> printReport());

        // Export report button
        exportReportButton.addActionListener(e -> exportReport());
    }

    private void updateDateRangeFields() {
        String selectedRange = (String) dateRangeComboBox.getSelectedItem();
        boolean customRange = "Custom Range".equals(selectedRange);

        startDateField.setEnabled(customRange);
        endDateField.setEnabled(customRange);

        if (!customRange) {
            startDateField.setText("");
            endDateField.setText("");
        }
    }

    private void generateReport() {
        String reportType = (String) reportTypeComboBox.getSelectedItem();
        String dateRange = (String) dateRangeComboBox.getSelectedItem();

        LocalDateTime startDate = null;
        LocalDateTime endDate = LocalDateTime.now();

        // Calculate date range
        switch (dateRange) {
            case "Today":
                startDate = LocalDateTime.now().toLocalDate().atStartOfDay();
                break;
            case "Last 7 Days":
                startDate = LocalDateTime.now().minusDays(7);
                break;
            case "Last 30 Days":
                startDate = LocalDateTime.now().minusDays(30);
                break;
            case "This Month":
                startDate = LocalDateTime.now().withDayOfMonth(1).toLocalDate().atStartOfDay();
                break;
            case "Last Month":
                LocalDateTime firstDayOfThisMonth = LocalDateTime.now().withDayOfMonth(1).toLocalDate().atStartOfDay();
                startDate = firstDayOfThisMonth.minusMonths(1);
                endDate = firstDayOfThisMonth.minusSeconds(1);
                break;
            case "Custom Range":
                try {
                    if (!startDateField.getText().trim().isEmpty()) {
                        startDate = LocalDateTime.parse(startDateField.getText().trim() + "T00:00:00");
                    }
                    if (!endDateField.getText().trim().isEmpty()) {
                        endDate = LocalDateTime.parse(endDateField.getText().trim() + "T23:59:59");
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Invalid date format. Please use YYYY-MM-DD format.",
                                                "Date Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                break;
        }

        // Generate the appropriate report
        String reportContent = "";
        switch (reportType) {
            case "Sales Summary":
                reportContent = generateSalesSummaryReport(startDate, endDate);
                break;
            case "Daily Sales":
                reportContent = generateDailySalesReport(startDate, endDate);
                break;
            case "Sales by Category":
                reportContent = generateSalesByCategoryReport(startDate, endDate);
                break;
            case "Top Products":
                reportContent = generateTopProductsReport(startDate, endDate);
                break;
            case "Low Stock Alert":
                reportContent = generateLowStockReport();
                break;
        }

        reportTextArea.setText(reportContent);
        reportTextArea.setCaretPosition(0); // Scroll to top
    }

    private void generateDefaultReport() {
        reportTextArea.setText(generateSalesSummaryReport(null, LocalDateTime.now()));
    }

    private String generateSalesSummaryReport(LocalDateTime startDate, LocalDateTime endDate) {
        StringBuilder report = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        report.append("=======================================\n");
        report.append("         SALES SUMMARY REPORT         \n");
        report.append("=======================================\n\n");

        report.append("Report Period: ");
        if (startDate != null) {
            report.append(startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        } else {
            report.append("All Time");
        }
        report.append(" to ").append(endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        report.append("\nGenerated: ").append(LocalDateTime.now().format(formatter)).append("\n\n");


        double totalSales = dataManager.getSalesForDateRange(startDate, endDate) / 100.0;
        int transactionCount = dataManager.getTransactionsByDateRange(startDate, endDate).size();
        double averageSale = transactionCount > 0 ? totalSales / transactionCount : 0.0;

        report.append("SUMMARY STATISTICS:\n");
        report.append("-".repeat(50)).append("\n");
        report.append(String.format("Total Transactions: %d\n", transactionCount));
        report.append("Total Sales: ").append(String.format("$%.2f", totalSales)).append("\n");
        report.append("Average Sale: ").append(String.format("$%.2f", averageSale)).append("\n");
        report.append("Tax Collected: ").append(String.format("$%.2f", totalSales * 0.08)).append("\n"); // Assuming 8% tax rate
        report.append("\n");

        return report.toString();
    }

    private String generateDailySalesReport(LocalDateTime startDate, LocalDateTime endDate) {
        StringBuilder report = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        report.append("=======================================\n");
        report.append("         DAILY SALES REPORT           \n");
        report.append("=======================================\n\n");

        report.append("Report Period: ");
        if (startDate != null) {
            report.append(startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        } else {
            report.append("All Time");
        }
        report.append(" to ").append(endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        report.append("\nGenerated: ").append(LocalDateTime.now().format(formatter)).append("\n\n");

        report.append(String.format("%-12s %15s %12s\n", "Date", "Transactions", "Sales"));
        report.append("=".repeat(40)).append("\n");

        LocalDateTime current = startDate != null ? startDate : LocalDateTime.now().minusDays(7);
        LocalDateTime end = endDate != null ? endDate : LocalDateTime.now();


        while (!current.isAfter(end)) {
            double dailySales = dataManager.getSalesForDate(current) / 100.0;
            int dailyTransactions = dataManager.getTransactionsByDateRange(
                current.toLocalDate().atStartOfDay(),
                current.toLocalDate().atTime(23, 59, 59)).size();

            report.append(String.format("%-12s %15d $%.2f\n",
                current.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                dailyTransactions, dailySales));

            current = current.plusDays(1);
        }

        return report.toString();
    }

    private String generateSalesByCategoryReport(LocalDateTime startDate, LocalDateTime endDate) {
        StringBuilder report = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        report.append("=======================================\n");
        report.append("      SALES BY CATEGORY REPORT        \n");
        report.append("=======================================\n\n");

        report.append("Report Period: ");
        if (startDate != null) {
            report.append(startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        } else {
            report.append("All Time");
        }
        report.append(" to ").append(endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        report.append("\nGenerated: ").append(LocalDateTime.now().format(formatter)).append("\n\n");

        Map<String, Double> salesByCategory = dataManager.getSalesByCategory();

        report.append(String.format("%-20s %12s\n", "Category", "Sales"));
        report.append("-".repeat(35)).append("\n");


        for (Map.Entry<String, Double> entry : salesByCategory.entrySet()) {
            report.append(String.format("%-20s $%.2f\n",
                entry.getKey(), entry.getValue() / 100.0));
        }

        return report.toString();
    }

    private String generateTopProductsReport(LocalDateTime startDate, LocalDateTime endDate) {
        StringBuilder report = new StringBuilder();

        report.append("=======================================\n");
        report.append("        TOP PRODUCTS REPORT           \n");
        report.append("=======================================\n\n");

        report.append("This feature requires additional implementation.\n");
        report.append("It would show products ranked by sales volume.\n");

        return report.toString();
    }

    private String generateLowStockReport() {
        StringBuilder report = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        report.append("=======================================\n");
        report.append("        LOW STOCK ALERT REPORT        \n");
        report.append("=======================================\n\n");

        report.append("Generated: ").append(LocalDateTime.now().format(formatter)).append("\n\n");

        Map<String, Integer> lowStockProducts = dataManager.getLowStockProducts();

        if (lowStockProducts.isEmpty()) {
            report.append("✓ All products are adequately stocked.\n");
        } else {
            report.append("⚠ LOW STOCK ALERTS:\n");
            report.append("-".repeat(40)).append("\n");
            report.append(String.format("%-25s %10s\n", "Product", "Stock Level"));
            report.append("-".repeat(40)).append("\n");

            for (Map.Entry<String, Integer> entry : lowStockProducts.entrySet()) {
                report.append(String.format("%-25s %10d\n", entry.getKey(), entry.getValue()));
            }
        }

        return report.toString();
    }

    private void printReport() {
        String reportContent = reportTextArea.getText();
        if (reportContent.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No report to print. Please generate a report first.",
                                        "No Report", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String reportType = (String) reportTypeComboBox.getSelectedItem();
        ReceiptPrinter.printTextReport(reportType + " Report", reportContent);
    }

    private void exportReport() {
        String reportContent = reportTextArea.getText();
        if (reportContent.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No report to export. Please generate a report first.",
                                        "No Report", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Report");
        String reportType = (String) reportTypeComboBox.getSelectedItem();
        fileChooser.setSelectedFile(new java.io.File(reportType.toLowerCase().replace(" ", "_") + "_report.txt"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                java.nio.file.Files.write(fileChooser.getSelectedFile().toPath(),
                                        reportContent.getBytes());
                JOptionPane.showMessageDialog(this, "Report exported successfully!",
                                            "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error exporting report: " + e.getMessage(),
                                            "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    // Public methods for external access
    public void refreshData() {
        generateReport();

        // Force UI repaint
        revalidate();
        repaint();
    }
}
