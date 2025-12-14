package com.pos.ui;

import com.pos.manager.DataManager;
import com.pos.model.Transaction;
import com.pos.printer.ReceiptPrinter;


import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Transaction History Panel - Interface for viewing transaction history
 */
public class TransactionHistoryPanel extends JPanel {
    private DataManager dataManager;
    private MainPOS mainPOS;

    // Transaction table components
    private JTable transactionTable;
    private DefaultTableModel transactionTableModel;
    private JScrollPane tableScrollPane;

    // Filter components
    private JTextField searchField;
    private JComboBox<String> dateFilterComboBox;
    private JButton searchButton;
    private JButton clearFilterButton;

    // Action buttons
    private JButton viewDetailsButton;
    private JButton printReceiptButton;
    private JButton refreshButton;

    // Summary labels
    private JLabel totalTransactionsLabel;
    private JLabel totalSalesLabel;
    private JLabel averageSaleLabel;

    public TransactionHistoryPanel(DataManager dataManager, MainPOS mainPOS) {
        this.dataManager = dataManager;
        this.mainPOS = mainPOS;

        initializeComponents();
        setupLayout();
        setupEventHandlers();
        refreshTransactionTable();
    }

    private void initializeComponents() {
        // Transaction table
        String[] columns = {"ID", "Date", "Time", "Items", "Subtotal", "Tax", "Total", "Payment", "Customer"};
        transactionTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        transactionTable = new JTable(transactionTableModel);
        transactionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        transactionTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // Set column widths
        transactionTable.getColumnModel().getColumn(0).setPreferredWidth(60);  // ID
        transactionTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Date
        transactionTable.getColumnModel().getColumn(2).setPreferredWidth(80);  // Time
        transactionTable.getColumnModel().getColumn(3).setPreferredWidth(60);  // Items
        transactionTable.getColumnModel().getColumn(4).setPreferredWidth(80);  // Subtotal
        transactionTable.getColumnModel().getColumn(5).setPreferredWidth(60);  // Tax
        transactionTable.getColumnModel().getColumn(6).setPreferredWidth(80);  // Total
        transactionTable.getColumnModel().getColumn(7).setPreferredWidth(80);  // Payment
        transactionTable.getColumnModel().getColumn(8).setPreferredWidth(120); // Customer

        tableScrollPane = new JScrollPane(transactionTable);
        tableScrollPane.setPreferredSize(new Dimension(800, 300));

        // Filter components
        searchField = new JTextField(15);
        searchField.setToolTipText("Search by transaction ID or customer name");

        dateFilterComboBox = new JComboBox<>();
        dateFilterComboBox.addItem("All Time");
        dateFilterComboBox.addItem("Today");
        dateFilterComboBox.addItem("Last 7 Days");
        dateFilterComboBox.addItem("Last 30 Days");
        dateFilterComboBox.addItem("This Month");
        dateFilterComboBox.setSelectedItem("All Time");

        searchButton = new JButton("Search");
        searchButton.setMnemonic('S');

        clearFilterButton = new JButton("Clear Filters");
        clearFilterButton.setMnemonic('C');

        // Action buttons
        viewDetailsButton = new JButton("View Details");
        viewDetailsButton.setMnemonic('V');

        printReceiptButton = new JButton("Print Receipt");
        printReceiptButton.setMnemonic('P');

        refreshButton = new JButton("Refresh");
        refreshButton.setMnemonic('R');

        // Summary labels
        totalTransactionsLabel = new JLabel("Total Transactions: 0");
        totalSalesLabel = new JLabel("Total Sales: 0.00");
        averageSaleLabel = new JLabel("Average Sale: 0.00");
    }


    private void setupLayout() {
        setLayout(new BorderLayout());

        // Top panel - Navigation and filters
        JPanel topPanel = new JPanel(new BorderLayout());

        // Quick navigation panel
        JPanel navigationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        navigationPanel.setBorder(BorderFactory.createTitledBorder("Navigation"));

        JButton salesBtn = new JButton("🛒 Sales");
        salesBtn.addActionListener(e -> mainPOS.showPanel("Sales"));

        JButton productsBtn = new JButton("📦 Products");
        productsBtn.addActionListener(e -> mainPOS.showPanel("Products"));

        JButton transactionsBtn = new JButton("📋 History (Current)");
        transactionsBtn.setBackground(new Color(34, 139, 34));
        transactionsBtn.setForeground(Color.WHITE);
        transactionsBtn.setEnabled(false);

        JButton reportsBtn = new JButton("📊 Reports");
        reportsBtn.addActionListener(e -> mainPOS.showPanel("Reports"));

        navigationPanel.add(salesBtn);
        navigationPanel.add(productsBtn);
        navigationPanel.add(transactionsBtn);
        navigationPanel.add(reportsBtn);

        // Search panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Search & Filter"));
        filterPanel.add(new JLabel("Search:"));
        filterPanel.add(searchField);
        filterPanel.add(new JLabel("Date Range:"));
        filterPanel.add(dateFilterComboBox);
        filterPanel.add(searchButton);
        filterPanel.add(clearFilterButton);

        topPanel.add(navigationPanel, BorderLayout.WEST);
        topPanel.add(filterPanel, BorderLayout.EAST);

        // Center panel - Transaction table
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Transaction History"));
        tablePanel.add(tableScrollPane, BorderLayout.CENTER);

        // Bottom panel - Actions and summary
        JPanel bottomPanel = new JPanel(new BorderLayout());

        // Action buttons panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actionPanel.add(viewDetailsButton);
        actionPanel.add(printReceiptButton);
        actionPanel.add(refreshButton);

        // Summary panel
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        summaryPanel.add(totalTransactionsLabel);
        summaryPanel.add(totalSalesLabel);
        summaryPanel.add(averageSaleLabel);

        bottomPanel.add(actionPanel, BorderLayout.WEST);
        bottomPanel.add(summaryPanel, BorderLayout.EAST);

        // Add all panels to main panel
        add(topPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void setupEventHandlers() {
        // Search button
        searchButton.addActionListener(e -> performSearch());

        // Search field enter key
        searchField.addActionListener(e -> performSearch());

        // Clear filter button
        clearFilterButton.addActionListener(e -> clearFilters());

        // Action buttons
        viewDetailsButton.addActionListener(e -> viewTransactionDetails());
        printReceiptButton.addActionListener(e -> printTransactionReceipt());
        refreshButton.addActionListener(e -> refreshTransactionTable());

        // Double-click on table to view details
        transactionTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    viewTransactionDetails();
                }
            }
        });
    }

    private void performSearch() {
        String searchTerm = searchField.getText().trim();
        String dateFilter = (String) dateFilterComboBox.getSelectedItem();

        LocalDateTime startDate = null;
        LocalDateTime endDate = LocalDateTime.now();

        // Calculate date range
        switch (dateFilter) {
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
            case "All Time":
            default:
                startDate = null;
                break;
        }

        List<Transaction> transactions = dataManager.getTransactionsByDateRange(startDate, endDate);

        // Apply search filter
        if (!searchTerm.isEmpty()) {
            transactions = transactions.stream()
                    .filter(t -> String.valueOf(t.getTransactionId()).contains(searchTerm) ||
                               (t.getCustomerName() != null &&
                                t.getCustomerName().toLowerCase().contains(searchTerm.toLowerCase())))
                    .toList();
        }

        updateTransactionTable(transactions);
        updateSummaryLabels(transactions);
    }

    private void clearFilters() {
        searchField.setText("");
        dateFilterComboBox.setSelectedItem("All Time");
        refreshTransactionTable();
    }

    private void updateTransactionTable(List<Transaction> transactions) {
        transactionTableModel.setRowCount(0);


        for (Transaction transaction : transactions) {
            Object[] row = {
                transaction.getTransactionId(),
                transaction.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                transaction.getTimestamp().format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                transaction.getItemCount(),
                String.format("$%.2f", transaction.getSubtotal() / 100.0),
                String.format("$%.2f", transaction.getTax() / 100.0),
                String.format("$%.2f", transaction.getTotal() / 100.0),
                transaction.getPaymentMethod() != null ? transaction.getPaymentMethod() : "Cash",
                transaction.getCustomerName() != null ? transaction.getCustomerName() : ""
            };
            transactionTableModel.addRow(row);
        }
    }


    private void updateSummaryLabels(List<Transaction> transactions) {
        int totalTransactions = transactions.size();
        double totalSales = transactions.stream().mapToDouble(Transaction::getTotal).sum() / 100.0;
        double averageSale = totalTransactions > 0 ? totalSales / totalTransactions : 0.0;

        totalTransactionsLabel.setText(String.format("Total Transactions: %d", totalTransactions));
        totalSalesLabel.setText("Total Sales: " + String.format("$%.2f", totalSales));
        averageSaleLabel.setText("Average Sale: " + String.format("$%.2f", averageSale));
    }

    private void viewTransactionDetails() {
        int selectedRow = transactionTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a transaction to view details.",
                                        "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int transactionId = (Integer) transactionTableModel.getValueAt(selectedRow, 0);
        Transaction transaction = dataManager.getTransactionById(transactionId);

        if (transaction == null) {
            JOptionPane.showMessageDialog(this, "Transaction not found.",
                                        "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Create details dialog
        JDialog detailsDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                                          "Transaction Details - #" + transactionId, true);
        detailsDialog.setLayout(new BorderLayout());

        // Transaction info panel
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        infoPanel.add(new JLabel("Transaction ID:"), gbc);
        gbc.gridx = 1;
        infoPanel.add(new JLabel(String.valueOf(transaction.getTransactionId())), gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        infoPanel.add(new JLabel("Date/Time:"), gbc);
        gbc.gridx = 1;
        infoPanel.add(new JLabel(transaction.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))), gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        infoPanel.add(new JLabel("Customer:"), gbc);
        gbc.gridx = 1;
        infoPanel.add(new JLabel(transaction.getCustomerName() != null ? transaction.getCustomerName() : "N/A"), gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        infoPanel.add(new JLabel("Payment Method:"), gbc);
        gbc.gridx = 1;
        infoPanel.add(new JLabel(transaction.getPaymentMethod() != null ? transaction.getPaymentMethod() : "Cash"), gbc);

        // Items table
        String[] itemColumns = {"Product", "Quantity", "Unit Price", "Total"};
        DefaultTableModel itemTableModel = new DefaultTableModel(itemColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };


        for (Transaction.TransactionItem item : transaction.getItems()) {
            Object[] row = {
                item.getProduct().getName(),
                item.getQuantity(),
                String.format("$%.2f", item.getUnitPrice() / 100.0),
                String.format("$%.2f", item.getTotalPrice() / 100.0)
            };
            itemTableModel.addRow(row);
        }

        JTable itemTable = new JTable(itemTableModel);
        JScrollPane itemScrollPane = new JScrollPane(itemTable);
        itemScrollPane.setPreferredSize(new Dimension(400, 150));


        // Totals panel
        JPanel totalsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        totalsPanel.add(new JLabel("Subtotal: " + String.format("$%.2f", transaction.getSubtotal() / 100.0)));
        totalsPanel.add(new JLabel("Tax: " + String.format("$%.2f", transaction.getTax() / 100.0)));
        totalsPanel.add(new JLabel("Total: " + String.format("$%.2f", transaction.getTotal() / 100.0)));

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton printButton = new JButton("Print Receipt");
        JButton closeButton = new JButton("Close");


        printButton.addActionListener(e -> {
            ReceiptPrinter.displayReceiptDialog(transaction,
                (String) dataManager.getSetting("storeName"),
                (String) dataManager.getSetting("storeAddress"),
                transaction.getAmountPaid());
        });
        closeButton.addActionListener(e -> detailsDialog.dispose());

        buttonPanel.add(printButton);
        buttonPanel.add(closeButton);

        // Add components to dialog
        detailsDialog.add(infoPanel, BorderLayout.NORTH);
        detailsDialog.add(itemScrollPane, BorderLayout.CENTER);
        detailsDialog.add(totalsPanel, BorderLayout.SOUTH);
        detailsDialog.add(buttonPanel, BorderLayout.SOUTH);

        detailsDialog.pack();
        detailsDialog.setLocationRelativeTo(this);
        detailsDialog.setVisible(true);
    }

    private void printTransactionReceipt() {
        int selectedRow = transactionTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a transaction to print receipt.",
                                        "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int transactionId = (Integer) transactionTableModel.getValueAt(selectedRow, 0);
        Transaction transaction = dataManager.getTransactionById(transactionId);

        if (transaction == null) {
            JOptionPane.showMessageDialog(this, "Transaction not found.",
                                        "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }


        ReceiptPrinter.displayReceiptDialog(transaction,
            (String) dataManager.getSetting("storeName"),
            (String) dataManager.getSetting("storeAddress"),
            transaction.getAmountPaid());
    }

    private void refreshTransactionTable() {
        performSearch();
    }


    // Public methods for external access
    public void refreshData() {
        refreshTransactionTable();

        // Force UI repaint
        revalidate();
        repaint();
    }
}
