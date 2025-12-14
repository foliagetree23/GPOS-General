

package com.pos.ui;

import com.pos.manager.DataManager;
import com.pos.model.Product;
import com.pos.ui.util.CurrencyAwareDocumentFilter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Product Dialog - For adding/editing products
 */
class ProductDialog extends JDialog {
    private DataManager dataManager;
    private Product existingProduct;
    private boolean productSaved;





    // Form components
    private JTextField idField;
    private JTextField nameField;
    private JTextArea descriptionArea;
    private JScrollPane descriptionScroll;


    private JTextField priceField;
    private JComboBox<String> categoryComboBox;
    private JTextField quantityField;
    private JTextField minStockField;
    private JTextField barcodeField;
    private JCheckBox activeCheckBox;
    private JButton addCategoryBtn;



    private JButton saveButton;
    private JButton cancelButton;

    public ProductDialog(Frame parent, DataManager dataManager, Product product) {
        super(parent, product == null ? "Add Product" : "Edit Product", true);
        this.dataManager = dataManager;
        this.existingProduct = product;
        this.productSaved = false;

        initializeComponents();
        setupLayout();
        setupEventHandlers();

        if (product != null) {
            populateFields(product);
        }


        pack();
        if (parent != null) {
            setLocationRelativeTo(parent);
        }
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }



    private void initializeComponents() {

        // Form fields
        idField = new JTextField(10);
        nameField = new JTextField(20);
        descriptionArea = new JTextArea(3, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionScroll = new JScrollPane(descriptionArea);



        priceField = new JTextField();
        priceField.setColumns(15); // Set visible columns but allow limitless input
        
        // Apply currency-aware document filter to price field
        javax.swing.text.Document priceDocument = priceField.getDocument();
        if (priceDocument instanceof javax.swing.text.PlainDocument) {
            ((javax.swing.text.PlainDocument) priceDocument).setDocumentFilter(new CurrencyAwareDocumentFilter());
        }












        // Category components
        categoryComboBox = new JComboBox<>();
        categoryComboBox.addItem("");
        for (String category : dataManager.getAllCategories()) {
            categoryComboBox.addItem(category);
        }

        quantityField = new JTextField(10);
        minStockField = new JTextField(10);
        barcodeField = new JTextField(15);
        activeCheckBox = new JCheckBox("Active", true);
        addCategoryBtn = new JButton("+");
        addCategoryBtn.setToolTipText("Add New Category");
        addCategoryBtn.setPreferredSize(new Dimension(25, 25));

        // Buttons
        saveButton = new JButton("Save");
        saveButton.setMnemonic('S');

        cancelButton = new JButton("Cancel");
        cancelButton.setMnemonic('C');
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Row 0: ID
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("ID:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(idField, gbc);

        // Row 1: Name
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(nameField, gbc);

        // Row 2: Description
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(descriptionScroll, gbc);



        // Row 3: Price Input
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Price:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 1;
        formPanel.add(priceField, gbc);

        gbc.gridx = 2; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 3; gbc.gridwidth = 1;



        // Category combo box with add button
        JPanel categoryPanel = new JPanel(new BorderLayout(2, 0));
        categoryPanel.add(categoryComboBox, BorderLayout.CENTER);

        addCategoryBtn.setText("...");
        addCategoryBtn.setToolTipText("Add New Category");
        addCategoryBtn.setPreferredSize(new Dimension(30, 22));
        addCategoryBtn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        categoryPanel.add(addCategoryBtn, BorderLayout.EAST);


        formPanel.add(categoryPanel, gbc);




        // Row 5: Quantity and Min Stock
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Quantity:"), gbc);
        gbc.gridx = 1;
        formPanel.add(quantityField, gbc);
        gbc.gridx = 2;
        formPanel.add(new JLabel("Min Stock:"), gbc);
        gbc.gridx = 3;
        formPanel.add(minStockField, gbc);

        // Row 6: Barcode and Active
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Barcode:"), gbc);
        gbc.gridx = 1;
        formPanel.add(barcodeField, gbc);
        gbc.gridx = 2; gbc.gridwidth = 2;
        formPanel.add(activeCheckBox, gbc);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }



    private void setupEventHandlers() {
        saveButton.addActionListener(e -> saveProduct());
        cancelButton.addActionListener(e -> dispose());

        // Add category button handler
        addCategoryBtn.addActionListener(e -> addNewCategory());



        // Enter key on any field saves
        ActionListener saveAction = e -> saveProduct();
        idField.addActionListener(saveAction);
        nameField.addActionListener(saveAction);
        priceField.addActionListener(saveAction);
        quantityField.addActionListener(saveAction);
        minStockField.addActionListener(saveAction);
        barcodeField.addActionListener(saveAction);
    }

    private void addNewCategory() {
        String categoryName = JOptionPane.showInputDialog(this,
            "Enter new category name:", "Add New Category",
            JOptionPane.QUESTION_MESSAGE);

        if (categoryName != null && !categoryName.trim().isEmpty()) {
            String trimmedCategory = categoryName.trim();

            // Check if category already exists
            boolean exists = false;
            for (int i = 0; i < categoryComboBox.getItemCount(); i++) {
                if (trimmedCategory.equals(categoryComboBox.getItemAt(i))) {
                    exists = true;
                    break;
                }
            }

            if (!exists) {
                categoryComboBox.addItem(trimmedCategory);
                categoryComboBox.setSelectedItem(trimmedCategory);
            } else {
                JOptionPane.showMessageDialog(this, "Category already exists!",
                                            "Duplicate Category", JOptionPane.WARNING_MESSAGE);
            }
        }
    }


    private void populateFields(Product product) {
        idField.setText(String.valueOf(product.getId()));
        idField.setEditable(false);
        nameField.setText(product.getName());
        descriptionArea.setText(product.getDescription());


        priceField.setText(String.valueOf(product.getPrice()));
        categoryComboBox.setSelectedItem(product.getCategory());
        quantityField.setText(String.valueOf(product.getQuantity()));
        minStockField.setText(String.valueOf(product.getMinStockLevel()));
        barcodeField.setText(product.getBarcode());
        activeCheckBox.setSelected(product.isActive());
    }



    private void saveProduct() {
        // Validate input
        if (idField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Product ID is required.",
                                        "Validation Error", JOptionPane.ERROR_MESSAGE);
            idField.requestFocus();
            return;
        }
        if (nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Product name is required.",
                                        "Validation Error", JOptionPane.ERROR_MESSAGE);
            nameField.requestFocus();
            return;
        }

        if (priceField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Price is required.",
                                        "Validation Error", JOptionPane.ERROR_MESSAGE);
            priceField.requestFocus();
            return;
        }




        String priceText = priceField.getText().trim().replace(".", "");
        if (priceText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Price is required.",
                                        "Validation Error", JOptionPane.ERROR_MESSAGE);
            priceField.requestFocus();
            return;
        }

        if (quantityField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Quantity is required.",
                                        "Validation Error", JOptionPane.ERROR_MESSAGE);
            quantityField.requestFocus();
            return;
        }

        try {
            int id = Integer.parseInt(idField.getText().trim());
            String name = nameField.getText().trim();
            String description = descriptionArea.getText().trim();


            int price = Integer.parseInt(priceText);
            String category = (String) categoryComboBox.getSelectedItem();
            int quantity = Integer.parseInt(quantityField.getText().trim());
            int minStock = minStockField.getText().trim().isEmpty() ? 5 :
                          Integer.parseInt(minStockField.getText().trim());
            String barcode = barcodeField.getText().trim();
            boolean active = activeCheckBox.isSelected();


            if (price < 0) {
                JOptionPane.showMessageDialog(this, "Price (in cents) cannot be negative.",
                                            "Validation Error", JOptionPane.ERROR_MESSAGE);
                priceField.requestFocus();
                return;
            }

            if (quantity < 0) {
                JOptionPane.showMessageDialog(this, "Quantity cannot be negative.",
                                            "Validation Error", JOptionPane.ERROR_MESSAGE);
                quantityField.requestFocus();
                return;
            }

            if (minStock < 0) {
                JOptionPane.showMessageDialog(this, "Minimum stock cannot be negative.",
                                            "Validation Error", JOptionPane.ERROR_MESSAGE);
                minStockField.requestFocus();
                return;
            }

            Product product;
            if (existingProduct != null) {
                // Update existing product
                existingProduct.setName(name);
                existingProduct.setDescription(description);
                existingProduct.setPrice(price);
                existingProduct.setCategory(category);
                existingProduct.setQuantity(quantity);
                existingProduct.setMinStockLevel(minStock);
                existingProduct.setBarcode(barcode.isEmpty() ? null : barcode);
                existingProduct.setActive(active);
                product = existingProduct;

                dataManager.updateProduct(product);
            } else {
                // Create new product
                if (dataManager.getProductById(id) != null) {
                    JOptionPane.showMessageDialog(this, "Product ID already exists.",
                                                "Validation Error", JOptionPane.ERROR_MESSAGE);
                    idField.requestFocus();
                    return;
                }
                product = new Product(name, price, category);
                product.setId(id);
                product.setDescription(description);
                product.setQuantity(quantity);
                product.setMinStockLevel(minStock);
                product.setBarcode(barcode.isEmpty() ? null : barcode);
                product.setActive(active);

                dataManager.addProduct(product);
            }

            dataManager.saveData();
            productSaved = true;

            JOptionPane.showMessageDialog(this, "Product saved successfully!",
                                        "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for ID and quantity.",
                                        "Invalid Input", JOptionPane.ERROR_MESSAGE);
        }
    }



















    public boolean isProductSaved() {
        return productSaved;
    }
}

