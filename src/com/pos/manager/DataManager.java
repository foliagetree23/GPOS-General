package com.pos.manager;

import com.pos.model.Product;
import com.pos.model.Transaction;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Data Manager for handling production-ready file-based data persistence
 * Provides permanent disk storage with auto-save, backup, and integrity checks
 */
public class DataManager {

    private static final String DATA_DIR = "pos_data";
    private static final String PRODUCTS_FILE = DATA_DIR + "/products.dat";
    private static final String TRANSACTIONS_FILE = DATA_DIR + "/transactions.dat";
    private static final String SETTINGS_FILE = DATA_DIR + "/settings.dat";
    private static final String BACKUP_DIR = DATA_DIR + "/backups";
    private static final long AUTO_SAVE_INTERVAL_MS = 5000; // Auto-save every 5 seconds
    private static final int MAX_BACKUPS = 10;


    private List<Product> products;
    private List<Transaction> transactions;
    private Map<String, Object> settings;
    private int nextProductId;
    private int nextTransactionId;
    private Timer autoSaveTimer;
    private boolean dataChanged;


    public DataManager() {
        this.products = new ArrayList<>();
        this.transactions = new ArrayList<>();
        this.settings = new HashMap<>();
        this.nextProductId = 1;
        this.nextTransactionId = 1;
        this.dataChanged = false;

        initializeDataDirectory();
        loadData();
        startAutoSave();
    }


    /**
     * Initialize the data directory
     */
    private void initializeDataDirectory() {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
            Files.createDirectories(Paths.get(BACKUP_DIR));
        } catch (IOException e) {
            System.err.println("Failed to create data directory: " + e.getMessage());
        }
    }

    /**
     * Start auto-save timer for automatic data persistence
     */
    private void startAutoSave() {
        autoSaveTimer = new Timer(true);
        autoSaveTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (dataChanged) {
                    saveData();
                }
            }
        }, AUTO_SAVE_INTERVAL_MS, AUTO_SAVE_INTERVAL_MS);
    }

    /**
     * Stop auto-save timer
     */
    public void stopAutoSave() {
        if (autoSaveTimer != null) {
            autoSaveTimer.cancel();
        }
    }


    /**
     * Create backup of all data files
     */
    public void createBackup() {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String backupPath = BACKUP_DIR + "/backup_" + timestamp;

            // Create backup directory
            Files.createDirectories(Paths.get(backupPath));

            // Copy all data files to backup
            copyFile(PRODUCTS_FILE, backupPath + "/products.dat");
            copyFile(TRANSACTIONS_FILE, backupPath + "/transactions.dat");
            copyFile(SETTINGS_FILE, backupPath + "/settings.dat");

            // Clean up old backups
            cleanupOldBackups();

            System.out.println("Data backup created: " + backupPath);
        } catch (IOException e) {
            System.err.println("Failed to create backup: " + e.getMessage());
        }
    }

    /**
     * Copy file utility method
     */
    private void copyFile(String source, String destination) throws IOException {
        File sourceFile = new File(source);
        if (sourceFile.exists()) {
            Files.copy(sourceFile.toPath(), Paths.get(destination), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * Clean up old backup files
     */
    private void cleanupOldBackups() {
        try {
            File backupDir = new File(BACKUP_DIR);
            File[] backups = backupDir.listFiles((dir, name) -> name.startsWith("backup_"));

            if (backups != null && backups.length > MAX_BACKUPS) {
                Arrays.sort(backups, Comparator.comparing(File::lastModified).reversed());

                // Keep only the most recent MAX_BACKUPS
                for (int i = MAX_BACKUPS; i < backups.length; i++) {
                    deleteDirectory(backups[i]);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to cleanup old backups: " + e.getMessage());
        }
    }

    /**
     * Recursively delete directory
     */
    private void deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            directory.delete();
        }
    }

    /**
     * Restore data from backup
     */
    public boolean restoreFromBackup(String backupPath) {
        try {
            Path sourceDir = Paths.get(backupPath);
            if (!Files.exists(sourceDir)) {
                return false;
            }

            // Create backup before restore
            createBackup();

            // Copy files from backup
            copyFile(backupPath + "/products.dat", PRODUCTS_FILE);
            copyFile(backupPath + "/transactions.dat", TRANSACTIONS_FILE);
            copyFile(backupPath + "/settings.dat", SETTINGS_FILE);

            // Reload data
            loadData();
            dataChanged = false;

            return true;
        } catch (IOException e) {
            System.err.println("Failed to restore from backup: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get list of available backups
     */
    public List<String> getAvailableBackups() {
        List<String> backups = new ArrayList<>();
        try {
            File backupDir = new File(BACKUP_DIR);
            File[] backupFiles = backupDir.listFiles((dir, name) -> name.startsWith("backup_"));

            if (backupFiles != null) {
                for (File backup : backupFiles) {
                    if (backup.isDirectory()) {
                        String name = backup.getName();
                        String timestamp = name.substring("backup_".length());
                        LocalDateTime dateTime = LocalDateTime.parse(timestamp, DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                        backups.add(backup.getAbsolutePath() + " (" + dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + ")");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to list backups: " + e.getMessage());
        }
        return backups;
    }

    /**
     * Load all data from files
     */
    private void loadData() {
        loadProducts();
        loadTransactions();
        loadSettings();
    }


    /**
     * Save all data to files
     */
    public void saveData() {
        try {
            saveProducts();
            saveTransactions();
            saveSettings();
            dataChanged = false;
        } catch (Exception e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
    }

    /**
     * Product Management Methods
     */

    private void loadProducts() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(PRODUCTS_FILE))) {
            @SuppressWarnings("unchecked")
            List<Product> loadedProducts = (List<Product>) ois.readObject();
            this.products = loadedProducts != null ? loadedProducts : new ArrayList<>();

            // Find the highest product ID for nextProductId
            this.nextProductId = products.stream()
                    .mapToInt(Product::getId)
                    .max()
                    .orElse(0) + 1;

        } catch (FileNotFoundException e) {
            // First time running - initialize with sample products
            initializeSampleProducts();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading products: " + e.getMessage());
            this.products = new ArrayList<>();
        }
    }

    private void saveProducts() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(PRODUCTS_FILE))) {
            oos.writeObject(products);
        } catch (IOException e) {
            System.err.println("Error saving products: " + e.getMessage());
        }
    }


    private void initializeSampleProducts() {
        products.addAll(Arrays.asList(
            new Product(1, "Coffee", "Fresh brewed coffee", 250, "Beverages", 100, 10, "COF001"),
            new Product(2, "Sandwich", "Ham and cheese sandwich", 599, "Food", 50, 5, "SAN001"),
            new Product(3, "Notebook", "Spiral bound notebook", 350, "Stationery", 25, 3, "NOT001"),
            new Product(4, "Water Bottle", "500ml mineral water", 150, "Beverages", 200, 20, "WAT001"),
            new Product(5, "Chocolate Bar", "Milk chocolate bar", 199, "Snacks", 75, 10, "CHO001")
        ));
        nextProductId = 6;
        saveProducts();
    }

    public List<Product> getAllProducts() {
        return new ArrayList<>(products);
    }

    public List<Product> getProductsByCategory(String category) {
        if (category == null || category.isEmpty()) {
            return getAllProducts();
        }
        return products.stream()
                .filter(p -> category.equals(p.getCategory()))
                .collect(Collectors.toList());
    }

    public List<String> getAllCategories() {
        return products.stream()
                .map(Product::getCategory)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public Product getProductById(int id) {
        return products.stream()
                .filter(p -> p.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public Product getProductByBarcode(String barcode) {
        return products.stream()
                .filter(p -> barcode.equals(p.getBarcode()))
                .findFirst()
                .orElse(null);
    }

    public List<Product> searchProducts(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllProducts();
        }

        String lowerSearchTerm = searchTerm.toLowerCase();
        return products.stream()
                .filter(p -> String.valueOf(p.getId()).contains(lowerSearchTerm) ||
                           p.getName().toLowerCase().contains(lowerSearchTerm) ||
                           (p.getDescription() != null && p.getDescription().toLowerCase().contains(lowerSearchTerm)) ||
                           (p.getBarcode() != null && p.getBarcode().toLowerCase().contains(lowerSearchTerm)))
                .collect(Collectors.toList());
    }


    public boolean addProduct(Product product) {
        if (product == null) return false;

        if (product.getId() == 0) {
            product.setId(nextProductId++);
        } else {
            if (product.getId() >= nextProductId) {
                nextProductId = product.getId() + 1;
            }
        }
        boolean added = products.add(product);
        if (added) {
            dataChanged = true;
        }
        return added;
    }


    public boolean updateProduct(Product updatedProduct) {
        if (updatedProduct == null) return false;

        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getId() == updatedProduct.getId()) {
                products.set(i, updatedProduct);
                dataChanged = true;
                return true;
            }
        }
        return false;
    }

    public boolean deleteProduct(int productId) {
        boolean removed = products.removeIf(p -> p.getId() == productId);
        if (removed) {
            dataChanged = true;
        }
        return removed;
    }

    /**
     * Transaction Management Methods
     */

    private void loadTransactions() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(TRANSACTIONS_FILE))) {
            @SuppressWarnings("unchecked")
            List<Transaction> loadedTransactions = (List<Transaction>) ois.readObject();
            this.transactions = loadedTransactions != null ? loadedTransactions : new ArrayList<>();

            // Find the highest transaction ID
            this.nextTransactionId = transactions.stream()
                    .mapToInt(Transaction::getTransactionId)
                    .max()
                    .orElse(0) + 1;

        } catch (FileNotFoundException e) {
            this.transactions = new ArrayList<>();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading transactions: " + e.getMessage());
            this.transactions = new ArrayList<>();
        }
    }

    private void saveTransactions() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(TRANSACTIONS_FILE))) {
            oos.writeObject(transactions);
        } catch (IOException e) {
            System.err.println("Error saving transactions: " + e.getMessage());
        }
    }

    public List<Transaction> getAllTransactions() {
        return new ArrayList<>(transactions);
    }

    public List<Transaction> getTransactionsByDateRange(LocalDateTime start, LocalDateTime end) {
        return transactions.stream()
                .filter(t -> {
                    LocalDateTime timestamp = t.getTimestamp();
                    return (start == null || !timestamp.isBefore(start)) &&
                           (end == null || !timestamp.isAfter(end));
                })
                .sorted((t1, t2) -> t2.getTimestamp().compareTo(t1.getTimestamp()))
                .collect(Collectors.toList());
    }

    public Transaction getTransactionById(int transactionId) {
        return transactions.stream()
                .filter(t -> t.getTransactionId() == transactionId)
                .findFirst()
                .orElse(null);
    }

    public int getNextTransactionId() {
        return nextTransactionId++;
    }


    public boolean addTransaction(Transaction transaction) {
        if (transaction == null) return false;

        transaction.setTransactionId(getNextTransactionId());
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setCompleted(true);

        boolean added = transactions.add(transaction);

        // Update product quantities
        for (Transaction.TransactionItem item : transaction.getItems()) {
            Product product = getProductById(item.getProduct().getId());
            if (product != null) {
                product.setQuantity(product.getQuantity() - item.getQuantity());
                updateProduct(product);
            }
        }

        if (added) {
            dataChanged = true;
        }

        return added;
    }

    /**
     * Settings Management
     */

    private void loadSettings() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(SETTINGS_FILE))) {
            @SuppressWarnings("unchecked")
            Map<String, Object> loadedSettings = (Map<String, Object>) ois.readObject();
            this.settings = loadedSettings != null ? loadedSettings : new HashMap<>();

            // Set default settings if not present
            if (!settings.containsKey("taxRate")) {
                settings.put("taxRate", 0.08);
            }

            if (!settings.containsKey("storeName")) {
                settings.put("storeName", "GPOS-General");
            }
            if (!settings.containsKey("storeAddress")) {
                settings.put("storeAddress", "123 Main Street");
            }

        } catch (FileNotFoundException e) {

            // Initialize default settings
            settings.put("taxRate", 0.08);
            settings.put("currency", "IDR");
            settings.put("storeName", "GPOS-General");
            settings.put("storeAddress", "123 Main Street");
            saveSettings();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading settings: " + e.getMessage());
            settings = new HashMap<>();
        }
    }

    private void saveSettings() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SETTINGS_FILE))) {
            oos.writeObject(settings);
        } catch (IOException e) {
            System.err.println("Error saving settings: " + e.getMessage());
        }
    }

    public Object getSetting(String key) {
        return settings.get(key);
    }



    public void setSetting(String key, Object value) {
        settings.put(key, value);
        dataChanged = true;
    }

    /**
     * Reports and Analytics
     */


    public double getTotalSales() {
        return transactions.stream()
                .mapToDouble(Transaction::getTotal)
                .sum() / 100.0; // Convert from cents to dollars
    }


    public double getSalesForDate(LocalDateTime date) {
        return transactions.stream()
                .filter(t -> t.getTimestamp().toLocalDate().equals(date.toLocalDate()))
                .mapToDouble(Transaction::getTotal)
                .sum() / 100.0; // Convert from cents to dollars
    }

    public double getSalesForDateRange(LocalDateTime start, LocalDateTime end) {
        return getTransactionsByDateRange(start, end).stream()
                .mapToDouble(Transaction::getTotal)
                .sum() / 100.0; // Convert from cents to dollars
    }

    public Map<String, Double> getSalesByCategory() {
        Map<String, Double> salesByCategory = new HashMap<>();

        for (Transaction transaction : transactions) {
            for (Transaction.TransactionItem item : transaction.getItems()) {
                String category = item.getProduct().getCategory();
                double sales = item.getTotalPrice() / 100.0; // Convert from cents to dollars
                salesByCategory.put(category, salesByCategory.getOrDefault(category, 0.0) + sales);
            }
        }

        return salesByCategory;
    }

    public Map<String, Integer> getLowStockProducts() {
        Map<String, Integer> lowStock = new HashMap<>();

        for (Product product : products) {
            if (product.isLowStock()) {
                lowStock.put(product.getName(), product.getQuantity());
            }
        }

        return lowStock;
    }


    /**
     * Data Management
     */

    public void clearAllData() {
        // Create backup before clearing
        createBackup();

        products.clear();
        transactions.clear();
        settings.clear();
        nextProductId = 1;
        nextTransactionId = 1;

        // Re-initialize settings
        loadSettings();

        saveData();
        dataChanged = false;
    }

    /**
     * Check data integrity and repair if necessary
     */
    public boolean checkDataIntegrity() {
        try {
            // Check for data files existence
            boolean productsFileExists = Files.exists(Paths.get(PRODUCTS_FILE));
            boolean transactionsFileExists = Files.exists(Paths.get(TRANSACTIONS_FILE));
            boolean settingsFileExists = Files.exists(Paths.get(SETTINGS_FILE));

            if (!productsFileExists || !transactionsFileExists || !settingsFileExists) {
                System.out.println("Some data files are missing, restoring from backup...");

                List<String> backups = getAvailableBackups();
                if (!backups.isEmpty()) {
                    String latestBackup = backups.get(0).split(" \\(")[0];
                    return restoreFromBackup(latestBackup);
                }
            }

            // Validate data integrity
            validateProducts();
            validateTransactions();
            validateSettings();

            return true;
        } catch (Exception e) {
            System.err.println("Data integrity check failed: " + e.getMessage());
            return false;
        }
    }


    /**
     * Validate products data
     */
    private void validateProducts() {
        products.removeIf(product -> product == null || product.getName() == null || product.getName().trim().isEmpty());

        // Ensure no duplicate IDs
        Set<Integer> seenIds = new HashSet<>();
        products.removeIf(product -> {
            if (product == null) return true;
            if (seenIds.contains(product.getId())) {
                product.setId(nextProductId++);
            }
            return !seenIds.add(product.getId());
        });
    }



    /**
     * Validate transactions data
     */
    private void validateTransactions() {
        // Remove invalid transactions and transactions with invalid products in one pass
        transactions.removeIf(transaction -> {
            if (transaction == null || transaction.getItems() == null) return true;
            return transaction.getItems().stream()
                .anyMatch(item -> item.getProduct() == null || getProductById(item.getProduct().getId()) == null);
        });
    }

    /**
     * Validate settings data
     */
    private void validateSettings() {
        // Ensure all required settings exist
        if (!settings.containsKey("taxRate")) settings.put("taxRate", 0.08);
        if (!settings.containsKey("storeName")) settings.put("storeName", "GPOS-General");
        if (!settings.containsKey("storeAddress")) settings.put("storeAddress", "123 Main Street");

        // Validate tax rate
        Object taxRate = settings.get("taxRate");
        if (!(taxRate instanceof Double) || (Double)taxRate < 0 || (Double)taxRate > 1) {
            settings.put("taxRate", 0.08);
        }
    }

    /**
     * Force immediate data save (manual save)
     */
    public void forceSave() {
        saveData();
    }

    /**
     * Get data statistics
     */
    public String getDataStatistics() {
        StringBuilder stats = new StringBuilder();
        stats.append("=== Data Statistics ===\n");
        stats.append("Products: ").append(products.size()).append("\n");
        stats.append("Transactions: ").append(transactions.size()).append("\n");
        stats.append("Total Sales: $").append(String.format("%.2f", getTotalSales())).append("\n");
        stats.append("Last Auto-Save: ").append(dataChanged ? "Pending" : "Up to date").append("\n");

        // Backup info
        List<String> backups = getAvailableBackups();
        stats.append("Available Backups: ").append(backups.size()).append("\n");
        if (!backups.isEmpty()) {
            stats.append("Latest Backup: ").append(backups.get(0)).append("\n");
        }

        return stats.toString();
    }

    /**
     * Shutdown hook to ensure data is saved before application exits
     */
    public void setupShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (dataChanged) {
                System.out.println("Saving data before shutdown...");
                saveData();
            }
            stopAutoSave();
        }));
    }


    public void exportData(String filePath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("=== POS System Data Export ===");
            writer.println("Export Date: " + LocalDateTime.now());
            writer.println();

            // Export products
            writer.println("=== PRODUCTS ===");
            writer.printf("%-5s %-20s %-10s %-15s %-10s%n", "ID", "Name", "Price", "Category", "Stock");
            writer.println("-".repeat(70));
            for (Product product : products) {
                writer.printf("%-5d %-20s $%-9.2f %-15s %-10d%n",
                    product.getId(), product.getName(), product.getPrice() / 100.0,
                    product.getCategory(), product.getQuantity());
            }
            writer.println();

            // Export transactions
            writer.println("=== TRANSACTIONS ===");
            writer.printf("%-10s %-20s %-15s %-10s%n", "ID", "Date", "Items", "Total");
            writer.println("-".repeat(60));
            for (Transaction transaction : transactions) {
                writer.printf("%-10d %-20s %-15d $%-9.2f%n",
                    transaction.getTransactionId(),
                    transaction.getTimestamp(),
                    transaction.getItemCount(),
                    transaction.getTotal() / 100.0);
            }
            writer.println();

            // Export summary
            writer.println("=== SUMMARY ===");
            writer.println("Total Products: " + products.size());
            writer.println("Total Transactions: " + transactions.size());
            writer.println("Total Sales: $" + String.format("%.2f", getTotalSales() / 100.0));


        } catch (IOException e) {
            System.err.println("Error exporting data: " + e.getMessage());
        }
    }
}
