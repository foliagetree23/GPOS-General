package com.pos.ui;

import com.pos.manager.DataManager;
import com.pos.model.Product;
import com.pos.model.Transaction;
import com.pos.printer.ReceiptPrinter;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Main POS Application Window
 */
public class MainPOS extends JFrame {
    private DataManager dataManager;
    
    // Main panels
    private CardLayout cardLayout;
    private JPanel mainPanel;
    


    // Menu components
    private JMenuBar menuBar;
    private JMenu fileMenu, salesMenu, productsMenu, reportsMenu, dataMenu, settingsMenu, helpMenu;
    
    // Status bar
    private JLabel statusLabel;
    private JLabel timeLabel;
    private Timer timeTimer;
    
    // Current transaction
    private Transaction currentTransaction;
    
    // UI panels
    private SalesPanel salesPanel;
    private ProductManagementPanel productPanel;
    private TransactionHistoryPanel transactionPanel;
    private ReportsPanel reportsPanel;
    private SettingsPanel settingsPanel;
    private PaymentDialog paymentDialog;
    
    public MainPOS() {

        setTitle("GPOS-General");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        

        // Initialize data manager with production-ready features
        dataManager = new DataManager();
        
        // Setup data integrity check and shutdown hook
        dataManager.checkDataIntegrity();
        dataManager.setupShutdownHook();
        
        // Initialize current transaction
        currentTransaction = new Transaction(dataManager.getNextTransactionId());
        
        // Initialize UI components
        initializeMenuBar();
        initializeMainPanel();
        initializeStatusBar();
        initializePanels();
        
        // Set up layout
        setJMenuBar(menuBar);
        add(mainPanel);
        add(statusBar(), BorderLayout.SOUTH);
        
        // Start time update timer
        startTimeUpdater();
        
        // Show sales panel by default
        showPanel("Sales");
        
        updateStatus("Ready");
    }
    
    private void initializeMenuBar() {
        menuBar = new JMenuBar();
        
        // File Menu
        fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        
        JMenuItem exportDataItem = new JMenuItem("Export Data");
        exportDataItem.setMnemonic(KeyEvent.VK_E);
        exportDataItem.addActionListener(e -> exportData());
        
        JMenuItem clearDataItem = new JMenuItem("Clear All Data");
        clearDataItem.setMnemonic(KeyEvent.VK_C);
        clearDataItem.addActionListener(e -> clearAllData());
        
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setMnemonic(KeyEvent.VK_X);
        exitItem.addActionListener(e -> System.exit(0));
        
        fileMenu.add(exportDataItem);
        fileMenu.addSeparator();
        fileMenu.add(clearDataItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        
        // Sales Menu
        salesMenu = new JMenu("Sales");
        salesMenu.setMnemonic(KeyEvent.VK_S);
        
        JMenuItem newTransactionItem = new JMenuItem("New Transaction");
        newTransactionItem.setMnemonic(KeyEvent.VK_N);
        newTransactionItem.addActionListener(e -> newTransaction());
        
        JMenuItem processPaymentItem = new JMenuItem("Process Payment");
        processPaymentItem.setMnemonic(KeyEvent.VK_P);
        processPaymentItem.addActionListener(e -> processPayment());
        
        salesMenu.add(newTransactionItem);
        salesMenu.add(processPaymentItem);
        
        // Products Menu
        productsMenu = new JMenu("Products");
        productsMenu.setMnemonic(KeyEvent.VK_P);
        
        JMenuItem manageProductsItem = new JMenuItem("Manage Products");
        manageProductsItem.setMnemonic(KeyEvent.VK_M);
        manageProductsItem.addActionListener(e -> showPanel("Products"));
        
        JMenuItem addProductItem = new JMenuItem("Add Product");
        addProductItem.setMnemonic(KeyEvent.VK_A);
        addProductItem.addActionListener(e -> addProduct());
        
        productsMenu.add(manageProductsItem);
        productsMenu.add(addProductItem);
        
        // Reports Menu
        reportsMenu = new JMenu("Reports");
        reportsMenu.setMnemonic(KeyEvent.VK_R);
        
        JMenuItem salesReportItem = new JMenuItem("Sales Report");
        salesReportItem.setMnemonic(KeyEvent.VK_S);
        salesReportItem.addActionListener(e -> showPanel("Reports"));
        
        JMenuItem transactionHistoryItem = new JMenuItem("Transaction History");
        transactionHistoryItem.setMnemonic(KeyEvent.VK_T);
        transactionHistoryItem.addActionListener(e -> showPanel("Transactions"));
        
        JMenuItem inventoryReportItem = new JMenuItem("Inventory Report");
        inventoryReportItem.setMnemonic(KeyEvent.VK_I);
        inventoryReportItem.addActionListener(e -> showInventoryReport());
        
        reportsMenu.add(salesReportItem);
        reportsMenu.add(transactionHistoryItem);

        reportsMenu.addSeparator();
        reportsMenu.add(inventoryReportItem);
        
        // Data Menu
        dataMenu = new JMenu("Data");
        dataMenu.setMnemonic(KeyEvent.VK_D);
        
        JMenuItem backupDataItem = new JMenuItem("Create Backup");
        backupDataItem.setMnemonic(KeyEvent.VK_B);
        backupDataItem.addActionListener(e -> createBackup());
        
        JMenuItem restoreDataItem = new JMenuItem("Restore from Backup");
        restoreDataItem.setMnemonic(KeyEvent.VK_R);
        restoreDataItem.addActionListener(e -> restoreFromBackup());
        
        JMenuItem dataStatsItem = new JMenuItem("Data Statistics");
        dataStatsItem.setMnemonic(KeyEvent.VK_S);
        dataStatsItem.addActionListener(e -> showDataStatistics());
        
        JMenuItem forceSaveItem = new JMenuItem("Force Save Now");
        forceSaveItem.setMnemonic(KeyEvent.VK_F);
        forceSaveItem.addActionListener(e -> forceSaveData());
        
        dataMenu.add(backupDataItem);
        dataMenu.add(restoreDataItem);
        dataMenu.addSeparator();
        dataMenu.add(dataStatsItem);

        dataMenu.add(forceSaveItem);
        
        // Settings Menu
        settingsMenu = new JMenu("Settings");
        settingsMenu.setMnemonic(KeyEvent.VK_T);
        
        JMenuItem appSettingsItem = new JMenuItem("Application Settings");
        appSettingsItem.setMnemonic(KeyEvent.VK_S);
        appSettingsItem.addActionListener(e -> showPanel("Settings"));
        
        settingsMenu.add(appSettingsItem);
        
        // Help Menu
        helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);
        
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.setMnemonic(KeyEvent.VK_A);
        aboutItem.addActionListener(e -> showAboutDialog());
        

        helpMenu.add(aboutItem);
        
        // Add menus to menu bar
        menuBar.add(fileMenu);
        menuBar.add(salesMenu);
        menuBar.add(productsMenu);
        menuBar.add(reportsMenu);
        menuBar.add(dataMenu);
        menuBar.add(settingsMenu);
        menuBar.add(helpMenu);
    }
    
    private void initializeMainPanel() {
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
    }
    
    private void initializeStatusBar() {
        statusLabel = new JLabel("Ready");
        statusLabel.setBorder(BorderFactory.createLoweredBevelBorder());
        
        timeLabel = new JLabel();
        timeLabel.setBorder(BorderFactory.createLoweredBevelBorder());
        updateTime();
    }
    
    private JPanel statusBar() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(timeLabel, BorderLayout.EAST);
        statusPanel.setPreferredSize(new Dimension(getWidth(), 25));
        return statusPanel;
    }
    


    private void initializePanels() {
        // Initialize panels
        salesPanel = new SalesPanel(dataManager, this);
        productPanel = new ProductManagementPanel(dataManager, this);
        transactionPanel = new TransactionHistoryPanel(dataManager, this);
        reportsPanel = new ReportsPanel(dataManager, this);
        settingsPanel = new SettingsPanel(dataManager, this);
        
        // Add panels to main panel
        mainPanel.add(salesPanel, "Sales");
        mainPanel.add(productPanel, "Products");
        mainPanel.add(transactionPanel, "Transactions");
        mainPanel.add(reportsPanel, "Reports");
        mainPanel.add(settingsPanel, "Settings");
    }
    
    private void startTimeUpdater() {
        timeTimer = new Timer(1000, e -> updateTime());
        timeTimer.start();
    }
    
    private void updateTime() {
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        timeLabel.setText(currentTime);
    }
    


    // Panel navigation methods
    public void showPanel(String panelName) {
        cardLayout.show(mainPanel, panelName);
        updateStatus("Viewing " + panelName + " panel");
        
        // Refresh panels when switching to ensure data is current
        if ("Sales".equals(panelName)) {
            salesPanel.refreshProductList();
        } else if ("Products".equals(panelName)) {
            productPanel.refreshData();
        }
    }
    

    
    // Status update methods
    public void updateStatus(String message) {
        statusLabel.setText(message);
    }
    
    // Transaction management methods
    public void newTransaction() {
        currentTransaction = new Transaction(dataManager.getNextTransactionId());
        salesPanel.setCurrentTransaction(currentTransaction);
        salesPanel.clearCart();
        salesPanel.refreshProductList();
        updateStatus("New transaction started");
    }
    
    public void processPayment() {
        if (currentTransaction.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No items in cart to process payment", 
                                        "Payment Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        PaymentDialog dialog = new PaymentDialog(this, currentTransaction, dataManager);
        this.paymentDialog = dialog; // Assign to member variable
        
        // Add a WindowListener to clear the reference when the dialog is closed
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                paymentDialog = null;
            }
        });
        
        dialog.setVisible(true);
        
        if (dialog.isPaymentCompleted()) {
            // Transaction completed successfully
            dataManager.addTransaction(currentTransaction);
            dataManager.saveData();
            
            // Show receipt
            ReceiptPrinter.displayReceiptDialog(
                currentTransaction,
                (String) dataManager.getSetting("storeName"),
                (String) dataManager.getSetting("storeAddress")
            );
            
            // Refresh the sales panel to update stock
            refreshSalesPanel();
        }
    }
    
    // Data management methods
    private void exportData() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export POS Data");
        fileChooser.setSelectedFile(new java.io.File("pos_data_export.txt"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            dataManager.exportData(fileChooser.getSelectedFile().getAbsolutePath());
            JOptionPane.showMessageDialog(this, "Data exported successfully!", 
                                        "Export Complete", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void clearAllData() {
        int result = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to clear all data? This action cannot be undone.",
            "Confirm Clear Data", JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (result == JOptionPane.YES_OPTION) {
            dataManager.clearAllData();
            
            // Refresh all panels
            salesPanel.refreshData();
            productPanel.refreshData();
            transactionPanel.refreshData();
            reportsPanel.refreshData();
            
            newTransaction();
            
            JOptionPane.showMessageDialog(this, "All data has been cleared.", 
                                        "Data Cleared", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    // Product management methods
    public void addProduct() {
        ProductDialog dialog = new ProductDialog(this, dataManager, null);
        dialog.setVisible(true);

        if (dialog.isProductSaved()) {
            productPanel.refreshData();
            salesPanel.refreshProductList();
            updateStatus("Product added successfully");
        }
    }

    public void refreshSalesPanel() {
        salesPanel.refreshProductList();
    }
    
    // Report methods
    private void showInventoryReport() {
        Map<String, Integer> lowStockProducts = dataManager.getLowStockProducts();
        
        StringBuilder report = new StringBuilder();
        report.append("=== INVENTORY REPORT ===\n");
        report.append("Generated: ").append(LocalDateTime.now()).append("\n\n");
        
        if (lowStockProducts.isEmpty()) {
            report.append("All products are adequately stocked.\n");
        } else {
            report.append("LOW STOCK ALERTS:\n");
            report.append(String.format("%-20s %10s\n", "Product", "Stock Level"));
            report.append("-".repeat(32) + "\n");
            

            for (Map.Entry<String, Integer> entry : lowStockProducts.entrySet()) {
                report.append(String.format("%-20s %10d\n", entry.getKey(), entry.getValue()));
            }
        }
        
        JTextArea textArea = new JTextArea(report.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));
        
        JOptionPane.showMessageDialog(this, scrollPane, "Inventory Report", 
                                    JOptionPane.INFORMATION_MESSAGE);
    }
    
    // Help methods
    private void showAboutDialog() {

        String message = "GPOS-General\n\n" +
                        "Version: 1.0\n" +
                        "Author: Foliage Tree\n" +
                        "Description: A comprehensive point of sale system\n" +
                        "for managing products, sales, and transactions.\n\n" +
                        "Features:\n" +
                        "• Product Management\n" +
                        "• Sales Processing\n" +
                        "• Transaction History\n" +
                        "• Sales Reports\n" +
                        "• Receipt Printing\n\n" +

                        "© 2025 GPOS-General";
        
        JOptionPane.showMessageDialog(this, message, "About POS System", 
                                    JOptionPane.INFORMATION_MESSAGE);
    }
    

    // Getter methods
    public DataManager getDataManager() {
        return dataManager;
    }
    
    public Transaction getCurrentTransaction() {
        return currentTransaction;
    }
    
    // Data management methods for production use
    private void createBackup() {
        try {
            dataManager.createBackup();
            JOptionPane.showMessageDialog(this, "Data backup created successfully!", 
                                        "Backup Complete", JOptionPane.INFORMATION_MESSAGE);
            updateStatus("Data backup created");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to create backup: " + e.getMessage(), 
                                        "Backup Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void restoreFromBackup() {
        List<String> backups = dataManager.getAvailableBackups();
        
        if (backups.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No backups available.", 
                                        "Restore Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Create dialog to select backup
        String selectedBackup = (String) JOptionPane.showInputDialog(this,
            "Select backup to restore:", "Restore from Backup",
            JOptionPane.QUESTION_MESSAGE, null, 
            backups.toArray(), backups.get(0));
        
        if (selectedBackup != null) {
            int result = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to restore from backup?\nCurrent data will be backed up first.",
                "Confirm Restore", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (result == JOptionPane.YES_OPTION) {
                String backupPath = selectedBackup.split(" \\(")[0];
                boolean success = dataManager.restoreFromBackup(backupPath);
                
                if (success) {
                    // Refresh all panels
                    salesPanel.refreshData();
                    productPanel.refreshData();
                    transactionPanel.refreshData();
                    reportsPanel.refreshData();
                    
                    JOptionPane.showMessageDialog(this, "Data restored successfully!", 
                                                "Restore Complete", JOptionPane.INFORMATION_MESSAGE);
                    updateStatus("Data restored from backup");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to restore from backup.", 
                                                "Restore Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    private void showDataStatistics() {
        String stats = dataManager.getDataStatistics();
        
        JTextArea textArea = new JTextArea(stats);
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 300));
        
        JOptionPane.showMessageDialog(this, scrollPane, "Data Statistics", 
                                    JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void forceSaveData() {
        dataManager.forceSave();
        JOptionPane.showMessageDialog(this, "Data saved successfully!", 
                                    "Save Complete", JOptionPane.INFORMATION_MESSAGE);
        updateStatus("Data saved");
    }
    

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            MainPOS pos = new MainPOS();
            pos.setVisible(true);
        });
    }
}
