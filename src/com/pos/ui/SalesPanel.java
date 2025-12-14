package com.pos.ui;

import com.pos.manager.DataManager;
import com.pos.model.Product;
import com.pos.model.Transaction;


import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Sales Panel - Main interface for processing sales transactions
 */
public class SalesPanel extends JPanel {
    private DataManager dataManager;
    private MainPOS mainPOS;

    // Product search components
    private JTextField searchField;
    private JComboBox<String> categoryComboBox;
    private JButton searchButton;
    private JButton clearSearchButton;
    private JTable productTable;
    private DefaultTableModel productTableModel;

    // Shopping cart components
    private JTable cartTable;
    private DefaultTableModel cartTableModel;
    private JButton addToCartButton;
    private JButton removeFromCartButton;
    private JButton clearCartButton;

    // Transaction summary components
    private JLabel subtotalLabel;
    private JLabel taxLabel;
    private JLabel totalLabel;
    private JButton processPaymentButton;

    // Current transaction
    private Transaction currentTransaction;

    public SalesPanel(DataManager dataManager, MainPOS mainPOS) {
        this.dataManager = dataManager;
        this.mainPOS = mainPOS;
        this.currentTransaction = mainPOS.getCurrentTransaction();

        initializeComponents();
        setupLayout();
        setupEventHandlers();
        refreshProductList();
    }

    private void initializeComponents() {
        // Product search section
        searchField = new JTextField(20);
        searchField.setToolTipText("Search by product name, barcode, or description");

        categoryComboBox = new JComboBox<>();
        categoryComboBox.addItem("All Categories");

        searchButton = new JButton("Search");
        searchButton.setMnemonic('S');

        clearSearchButton = new JButton("Clear");
        clearSearchButton.setMnemonic('L');

        // Product table
        String[] productColumns = {"ID", "Name", "Price", "Category", "Stock", "Barcode"};
        productTableModel = new DefaultTableModel(productColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        productTable = new JTable(productTableModel);
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        productTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        productTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        productTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        productTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        productTable.getColumnModel().getColumn(4).setPreferredWidth(60);
        productTable.getColumnModel().getColumn(5).setPreferredWidth(100);

        // Cart table
        String[] cartColumns = {"Product", "Quantity", "Unit Price", "Total"};
        cartTableModel = new DefaultTableModel(cartColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1; // Only quantity column is editable
            }
        };
        cartTable = new JTable(cartTableModel);
        cartTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cartTable.getColumnModel().getColumn(0).setPreferredWidth(200);
        cartTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        cartTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        cartTable.getColumnModel().getColumn(3).setPreferredWidth(100);

        // Cart buttons
        addToCartButton = new JButton("Add to Cart");
        addToCartButton.setMnemonic('A');

        removeFromCartButton = new JButton("Remove Item");
        removeFromCartButton.setMnemonic('R');

        clearCartButton = new JButton("Clear Cart");
        clearCartButton.setMnemonic('C');

        // Transaction summary
        subtotalLabel = new JLabel("0");
        taxLabel = new JLabel("0");
        totalLabel = new JLabel("0");

        processPaymentButton = new JButton("Process Payment");
        processPaymentButton.setMnemonic('P');
        processPaymentButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        processPaymentButton.setBackground(new Color(34, 139, 34));
        processPaymentButton.setForeground(Color.WHITE);
    }


    private void setupLayout() {
        setLayout(new BorderLayout());

        // Top panel - Navigation and search
        JPanel topPanel = new JPanel(new BorderLayout());

        // Quick navigation panel
        JPanel navigationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        navigationPanel.setBorder(BorderFactory.createTitledBorder("Navigation"));

        JButton salesBtn = new JButton("🛒 Sales (Current)");
        salesBtn.setBackground(new Color(34, 139, 34));
        salesBtn.setForeground(Color.WHITE);
        salesBtn.setEnabled(false);

        JButton productsBtn = new JButton("📦 Products");
        productsBtn.addActionListener(e -> mainPOS.showPanel("Products"));

        JButton transactionsBtn = new JButton("📋 History");
        transactionsBtn.addActionListener(e -> mainPOS.showPanel("Transactions"));

        JButton reportsBtn = new JButton("📊 Reports");
        reportsBtn.addActionListener(e -> mainPOS.showPanel("Reports"));

        navigationPanel.add(salesBtn);
        navigationPanel.add(productsBtn);
        navigationPanel.add(transactionsBtn);
        navigationPanel.add(reportsBtn);

        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Product Search"));
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(new JLabel("Category:"));
        searchPanel.add(categoryComboBox);
        searchPanel.add(searchButton);
        searchPanel.add(clearSearchButton);

        topPanel.add(navigationPanel, BorderLayout.WEST);
        topPanel.add(searchPanel, BorderLayout.EAST);

        // Center panel - Product list and cart
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 0));

        // Product list panel
        JPanel productPanel = new JPanel(new BorderLayout());
        productPanel.setBorder(BorderFactory.createTitledBorder("Available Products (Double-click to add to cart)"));
        JScrollPane productScrollPane = new JScrollPane(productTable);
        productScrollPane.setPreferredSize(new Dimension(400, 300));
        productPanel.add(productScrollPane, BorderLayout.CENTER);

        JPanel productButtonPanel = new JPanel(new FlowLayout());
        productButtonPanel.add(addToCartButton);
        productPanel.add(productButtonPanel, BorderLayout.SOUTH);

        // Cart panel
        JPanel cartPanel = new JPanel(new BorderLayout());
        cartPanel.setBorder(BorderFactory.createTitledBorder("Shopping Cart (Edit quantities directly)"));
        JScrollPane cartScrollPane = new JScrollPane(cartTable);
        cartScrollPane.setPreferredSize(new Dimension(400, 300));
        cartPanel.add(cartScrollPane, BorderLayout.CENTER);

        JPanel cartButtonPanel = new JPanel(new FlowLayout());
        cartButtonPanel.add(removeFromCartButton);
        cartButtonPanel.add(clearCartButton);
        cartPanel.add(cartButtonPanel, BorderLayout.SOUTH);

        centerPanel.add(productPanel);
        centerPanel.add(cartPanel);

        // Bottom panel - Transaction summary and payment
        JPanel summaryPanel = new JPanel(new BorderLayout());
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Transaction Summary & Payment"));

        JPanel summaryLabelsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        summaryLabelsPanel.add(new JLabel("Subtotal:"), gbc);
        gbc.gridx = 1;
        summaryLabelsPanel.add(subtotalLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        summaryLabelsPanel.add(new JLabel("Tax:"), gbc);
        gbc.gridx = 1;
        summaryLabelsPanel.add(taxLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        summaryLabelsPanel.add(new JLabel("Total:"), gbc);
        gbc.gridx = 1;
        summaryLabelsPanel.add(totalLabel, gbc);

        // Payment actions panel
        JPanel paymentPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        paymentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton newTransactionBtn = new JButton("🔄 New Transaction");
        newTransactionBtn.addActionListener(e -> mainPOS.newTransaction());

        paymentPanel.add(newTransactionBtn);
        paymentPanel.add(processPaymentButton);

        summaryPanel.add(summaryLabelsPanel, BorderLayout.WEST);
        summaryPanel.add(paymentPanel, BorderLayout.EAST);

        // Add all panels to main panel
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(summaryPanel, BorderLayout.SOUTH);
    }

    private void setupEventHandlers() {
        // Search button
        searchButton.addActionListener(e -> performSearch());

        // Clear search button
        clearSearchButton.addActionListener(e -> clearSearch());

        // Search field enter key
        searchField.addActionListener(e -> performSearch());

        // Category combo box
        categoryComboBox.addActionListener(e -> performSearch());

        // Add to cart button
        addToCartButton.addActionListener(e -> addSelectedProductToCart());

        // Remove from cart button
        removeFromCartButton.addActionListener(e -> removeSelectedItemFromCart());

        // Clear cart button
        clearCartButton.addActionListener(e -> clearCart());

        // Process payment button
        processPaymentButton.addActionListener(e -> mainPOS.processPayment());

        // Cart table cell editing
        cartTableModel.addTableModelListener(e -> {
            if (e.getColumn() == 1) { // Quantity column
                updateCartItemQuantity(e.getFirstRow());
            }
        });

        // Double-click on product table to add to cart
        productTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    addSelectedProductToCart();
                }
            }
        });
    }

    private void clearSearch() {
        searchField.setText("");
        categoryComboBox.setSelectedIndex(0);
        performSearch();
    }


    private void performSearch() {
        String searchTerm = searchField.getText().trim();
        String selectedCategory = (String) categoryComboBox.getSelectedItem();

        List<Product> products;
        if (selectedCategory != null && !selectedCategory.equals("All Categories")) {
            products = dataManager.searchProducts(searchTerm);
            products = products.stream()
                    .filter(p -> selectedCategory.equals(p.getCategory()))
                    .toList();
        } else {
            products = dataManager.searchProducts(searchTerm);
        }

        updateProductTable(products);
    }

    private void updateProductTable(List<Product> products) {
        productTableModel.setRowCount(0);

        for (Product product : products) {
            if (product.getQuantity() > 0) {
                Object[] row = {
                    product.getId(),
                    product.getName(),

                    String.format("$%.2f", product.getPrice() / 100.0),
                    product.getCategory(),
                    product.getQuantity(),
                    product.getBarcode() != null ? product.getBarcode() : ""
                };
                productTableModel.addRow(row);
            }
        }
    }

    private void addSelectedProductToCart() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to add to cart.",
                                        "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int productId = (Integer) productTableModel.getValueAt(selectedRow, 0);
        Product product = dataManager.getProductById(productId);

        if (product == null) {
            JOptionPane.showMessageDialog(this, "Product not found.",
                                        "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (product.getQuantity() <= 0) {
            JOptionPane.showMessageDialog(this, "The product '" + product.getName() + "' is out of stock and cannot be added to the cart.",
                                        "Out of Stock", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Ask for quantity
        String quantityStr = JOptionPane.showInputDialog(this,
            "Enter quantity for " + product.getName() + ":", "1");

        if (quantityStr == null || quantityStr.trim().isEmpty()) {
            return;
        }

        try {
            int quantity = Integer.parseInt(quantityStr.trim());
            if (quantity <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be greater than 0.",
                                            "Invalid Quantity", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (quantity > product.getQuantity()) {
                JOptionPane.showMessageDialog(this,
                    "Not enough stock. Available: " + product.getQuantity(),
                    "Insufficient Stock", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Note: Auto-notification for minimum stock level.
            // If the stock will fall below the minimum level, a confirmation dialog is shown.
            if (product.getQuantity() - quantity <= product.getMinStockLevel()) {
                int result = JOptionPane.showConfirmDialog(this,
                    "Minimum stock for this item has been reached. Would you like to continue?",
                    "Low Stock Warning",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
                if (result == JOptionPane.NO_OPTION) {
                    return;
                }
            }

            currentTransaction.addItem(product, quantity);
            updateCartTable();
            updateTransactionSummary();

            mainPOS.updateStatus("Added " + quantity + " x " + product.getName() + " to cart");

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number for quantity.",
                                        "Invalid Input", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removeSelectedItemFromCart() {
        int selectedRow = cartTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item to remove from cart.",
                                        "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String productName = (String) cartTableModel.getValueAt(selectedRow, 0);

        // Find the product in the transaction
        for (Transaction.TransactionItem item : currentTransaction.getItems()) {
            if (item.getProduct().getName().equals(productName)) {
                currentTransaction.removeItem(item.getProduct().getId());
                break;
            }
        }

        updateCartTable();
        updateTransactionSummary();

        mainPOS.updateStatus("Removed " + productName + " from cart");
    }

    private void updateCartItemQuantity(int row) {
        if (row < 0 || row >= cartTableModel.getRowCount()) return;

        String productName = (String) cartTableModel.getValueAt(row, 0);
        Object quantityObj = cartTableModel.getValueAt(row, 1);

        int newQuantity;
        try {
            if (quantityObj instanceof Integer) {
                newQuantity = (Integer) quantityObj;
            } else {
                newQuantity = Integer.parseInt(quantityObj.toString());
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid quantity format.", "Error", JOptionPane.ERROR_MESSAGE);
            updateCartTable(); // Revert the change in the table
            return;
        }

        for (Transaction.TransactionItem item : currentTransaction.getItems()) {
            if (item.getProduct().getName().equals(productName)) {
                if (newQuantity <= 0) {
                    currentTransaction.removeItem(item.getProduct().getId());
                } else {
                    Product product = dataManager.getProductById(item.getProduct().getId());
                    if (newQuantity > product.getQuantity()) {
                        JOptionPane.showMessageDialog(this,
                            "Not enough stock. Available: " + product.getQuantity(),
                            "Insufficient Stock", JOptionPane.WARNING_MESSAGE);
                    } else {
                        currentTransaction.updateItemQuantity(item.getProduct().getId(), newQuantity);
                    }
                }
                break;
            }
        }

        updateCartTable();
        updateTransactionSummary();
    }





    private void updateCartTable() {
        cartTableModel.setRowCount(0);

        for (Transaction.TransactionItem item : currentTransaction.getItems()) {

            Object[] row = {
                item.getProduct().getName(),
                item.getQuantity(),
                String.format("$%.2f", item.getUnitPrice() / 100.0),
                String.format("$%.2f", item.getTotalPrice() / 100.0)
            };
            cartTableModel.addRow(row);
        }
    }


    private void updateTransactionSummary() {
        subtotalLabel.setText(String.format("$%.2f", currentTransaction.getSubtotal() / 100.0));
        taxLabel.setText(String.format("$%.2f", currentTransaction.getTax() / 100.0));
        totalLabel.setText(String.format("$%.2f", currentTransaction.getTotal() / 100.0));
    }

    // Public methods for external access
    public void refreshProductList() {
        // Update category combo box
        categoryComboBox.removeAllItems();
        categoryComboBox.addItem("All Categories");
        for (String category : dataManager.getAllCategories()) {
            categoryComboBox.addItem(category);
        }

        // Refresh product table
        updateProductTable(dataManager.getAllProducts());
    }


    public void refreshData() {
        // Force complete refresh of all components
        refreshProductList();
        updateCartTable();
        updateTransactionSummary();

        // Force UI repaint
        revalidate();
        repaint();
    }


    public void clearCart() {
        int result = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to clear the shopping cart?",
            "Clear Cart", JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            currentTransaction.clear();
            updateCartTable();
            updateTransactionSummary();
            mainPOS.updateStatus("Cart cleared");
        }
    }

    public void setCurrentTransaction(Transaction transaction) {
        this.currentTransaction = transaction;
    }
}
