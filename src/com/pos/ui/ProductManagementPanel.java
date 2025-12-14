package com.pos.ui;

import com.pos.manager.DataManager;
import com.pos.model.Product;


import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Product Management Panel - Interface for managing products
 */
public class ProductManagementPanel extends JPanel {
    private DataManager dataManager;
    private MainPOS mainPOS;


    // Product table components
    private JTable productTable;
    private DefaultTableModel productTableModel;
    private JScrollPane tableScrollPane;

    // Search components
    private JTextField searchField;
    private JComboBox<String> categoryFilterComboBox;
    private JButton searchButton;
    private JButton clearSearchButton;

    // Action buttons
    private JButton addProductButton;
    private JButton editProductButton;
    private JButton deleteProductButton;
    private JButton refreshButton;

    // Status label
    private JLabel statusLabel;

    public ProductManagementPanel(DataManager dataManager, MainPOS mainPOS) {
        this.dataManager = dataManager;
        this.mainPOS = mainPOS;

        initializeComponents();
        setupLayout();
        setupEventHandlers();
        refreshProductTable();
    }

    private void initializeComponents() {
        // Product table
        String[] columns = {"ID", "Name", "Description", "Price", "Category", "Stock", "Min Stock", "Barcode", "Status"};
        productTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        productTable = new JTable(productTableModel);
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        productTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // Set column widths
        productTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        productTable.getColumnModel().getColumn(1).setPreferredWidth(150); // Name
        productTable.getColumnModel().getColumn(2).setPreferredWidth(200); // Description
        productTable.getColumnModel().getColumn(3).setPreferredWidth(80);  // Price
        productTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Category
        productTable.getColumnModel().getColumn(5).setPreferredWidth(60);  // Stock
        productTable.getColumnModel().getColumn(6).setPreferredWidth(70);  // Min Stock
        productTable.getColumnModel().getColumn(7).setPreferredWidth(100); // Barcode
        productTable.getColumnModel().getColumn(8).setPreferredWidth(60);  // Status

        tableScrollPane = new JScrollPane(productTable);
        tableScrollPane.setPreferredSize(new Dimension(800, 400));

        // Search components
        searchField = new JTextField(20);
        searchField.setToolTipText("Search by product name, barcode, or description");

        categoryFilterComboBox = new JComboBox<>();
        categoryFilterComboBox.addItem("All Categories");

        searchButton = new JButton("Search");
        searchButton.setMnemonic('S');

        clearSearchButton = new JButton("Clear");
        clearSearchButton.setMnemonic('L');

        // Action buttons
        addProductButton = new JButton("Add Product");
        addProductButton.setMnemonic('A');
        addProductButton.setBackground(new Color(34, 139, 34));
        addProductButton.setForeground(Color.WHITE);

        editProductButton = new JButton("Edit Product");
        editProductButton.setMnemonic('E');

        deleteProductButton = new JButton("Delete Product");
        deleteProductButton.setMnemonic('D');
        deleteProductButton.setBackground(new Color(220, 20, 60));
        deleteProductButton.setForeground(Color.WHITE);

        refreshButton = new JButton("Refresh");
        refreshButton.setMnemonic('R');

        // Status label
        statusLabel = new JLabel("Ready");
        statusLabel.setBorder(BorderFactory.createLoweredBevelBorder());
    }


    private void setupLayout() {
        setLayout(new BorderLayout());

        // Top panel - Navigation and search
        JPanel topPanel = new JPanel(new BorderLayout());

        // Quick navigation panel
        JPanel navigationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        navigationPanel.setBorder(BorderFactory.createTitledBorder("Navigation"));

        JButton salesBtn = new JButton("🛒 Sales");
        salesBtn.addActionListener(e -> mainPOS.showPanel("Sales"));

        JButton productsBtn = new JButton("📦 Products (Current)");
        productsBtn.setBackground(new Color(34, 139, 34));
        productsBtn.setForeground(Color.WHITE);
        productsBtn.setEnabled(false);

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
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search & Filter"));
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(new JLabel("Category:"));
        searchPanel.add(categoryFilterComboBox);
        searchPanel.add(searchButton);
        searchPanel.add(clearSearchButton);

        topPanel.add(navigationPanel, BorderLayout.WEST);
        topPanel.add(searchPanel, BorderLayout.EAST);

        // Center panel - Product table
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Products"));
        tablePanel.add(tableScrollPane, BorderLayout.CENTER);

        // Bottom panel - Action buttons and status
        JPanel bottomPanel = new JPanel(new BorderLayout());

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(addProductButton);
        buttonPanel.add(editProductButton);
        buttonPanel.add(deleteProductButton);
        buttonPanel.add(refreshButton);

        bottomPanel.add(buttonPanel, BorderLayout.CENTER);
        bottomPanel.add(statusLabel, BorderLayout.SOUTH);

        // Add all panels to main panel
        add(topPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void setupEventHandlers() {
        // Search button
        searchButton.addActionListener(e -> performSearch());

        // Clear search button
        clearSearchButton.addActionListener(e -> clearSearch());

        // Search field enter key
        searchField.addActionListener(e -> performSearch());

        // Category filter
        categoryFilterComboBox.addActionListener(e -> performSearch());

        // Action buttons
        addProductButton.addActionListener(e -> addProduct());
        editProductButton.addActionListener(e -> editSelectedProduct());
        deleteProductButton.addActionListener(e -> deleteSelectedProduct());
        refreshButton.addActionListener(e -> refreshProductTable());

        // Double-click on table to edit
        productTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    editSelectedProduct();
                }
            }
        });
    }

    private void clearSearch() {
        searchField.setText("");
        categoryFilterComboBox.setSelectedIndex(0);
        performSearch();
    }


    private void performSearch() {
        String searchTerm = searchField.getText().trim();
        String selectedCategory = (String) categoryFilterComboBox.getSelectedItem();

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
        statusLabel.setText("Found " + products.size() + " products");
    }

    private void updateProductTable(List<Product> products) {
        productTableModel.setRowCount(0);

        for (Product product : products) {
            Object[] row = {
                product.getId(),
                product.getName(),
                product.getDescription() != null ? product.getDescription() : "",

                String.format("$%.2f", product.getPrice() / 100.0),
                product.getCategory(),
                product.getQuantity(),
                product.getMinStockLevel(),
                product.getBarcode() != null ? product.getBarcode() : "",
                product.isActive() ? "Active" : "Inactive"
            };
            productTableModel.addRow(row);
        }
    }

    private void addProduct() {
        ProductDialog dialog = new ProductDialog(mainPOS, dataManager, null);
        dialog.setVisible(true);

        if (dialog.isProductSaved()) {
            refreshProductTable();
            mainPOS.refreshSalesPanel();
            mainPOS.updateStatus("Product added successfully");
        }
    }

    private void editSelectedProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to edit.",
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

        ProductDialog dialog = new ProductDialog(mainPOS, dataManager, product);
        dialog.setVisible(true);

        if (dialog.isProductSaved()) {
            refreshProductTable();
            mainPOS.refreshSalesPanel();
            mainPOS.updateStatus("Product updated successfully");
        }
    }

    private void deleteSelectedProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to delete.",
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

        int result = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete the product '" + product.getName() + "'?\n" +
            "This action cannot be undone.",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            boolean deleted = dataManager.deleteProduct(productId);
            if (deleted) {
                refreshProductTable();
                dataManager.saveData();
                mainPOS.updateStatus("Product deleted successfully");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete product.",
                                            "Delete Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void refreshProductTable() {
        // Update category filter combo box
        categoryFilterComboBox.removeAllItems();
        categoryFilterComboBox.addItem("All Categories");
        for (String category : dataManager.getAllCategories()) {
            categoryFilterComboBox.addItem(category);
        }

        // Refresh product table
        updateProductTable(dataManager.getAllProducts());
        statusLabel.setText("Total products: " + dataManager.getAllProducts().size());
    }


    // Public methods for external access
    public void refreshData() {
        refreshProductTable();

        // Force UI repaint
        revalidate();
        repaint();
    }
}