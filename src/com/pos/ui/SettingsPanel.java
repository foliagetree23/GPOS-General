
package com.pos.ui;

import com.pos.manager.DataManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Settings Panel - Interface for application settings and configuration
 */
public class SettingsPanel extends JPanel {
    private DataManager dataManager;
    private MainPOS mainPOS;

    // Store settings components
    private JTextField storeNameField;
    private JTextArea storeAddressArea;


    // Tax settings
    private JTextField taxRateField;

    // UI scaling settings
    private JSlider uiScaleSlider;
    private JLabel uiScaleLabel;

    // Action buttons
    private JButton saveButton;
    private JButton resetSettingsButton;

    // Status label
    private JLabel statusLabel;

    public SettingsPanel(DataManager dataManager, MainPOS mainPOS) {
        this.dataManager = dataManager;
        this.mainPOS = mainPOS;

        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadSettings();
    }

    private void initializeComponents() {
        // Store settings
        storeNameField = new JTextField(20);
        storeAddressArea = new JTextArea(3, 20);
        storeAddressArea.setLineWrap(true);
        storeAddressArea.setWrapStyleWord(true);


        // Tax settings
        taxRateField = new JTextField(10);



        // UI scaling settings

        uiScaleSlider = new JSlider(JSlider.HORIZONTAL, 80, 200, 100);
        uiScaleSlider.setMajorTickSpacing(20);
        uiScaleSlider.setMinorTickSpacing(10);
        uiScaleSlider.setPaintTicks(true);
        uiScaleSlider.setPaintLabels(true);
        uiScaleSlider.setToolTipText("Adjust UI scaling from 80% to 200%");
        
        uiScaleLabel = new JLabel("100%");
        uiScaleLabel.setFont(new Font(uiScaleLabel.getFont().getName(), Font.BOLD, 14));



        // Action buttons
        saveButton = new JButton("Save");
        saveButton.setMnemonic('S');

        resetSettingsButton = new JButton("Reset to Defaults");
        resetSettingsButton.setMnemonic('R');

        // Status label
        statusLabel = new JLabel();
        statusLabel.setBorder(BorderFactory.createLoweredBevelBorder());
    }





    private void setupLayout() {
        setLayout(new BorderLayout());

        // Top panel - Navigation
        JPanel navigationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        navigationPanel.setBorder(BorderFactory.createTitledBorder("Navigation"));

        JButton salesBtn = new JButton("🛒 Sales");
        salesBtn.addActionListener(e -> mainPOS.showPanel("Sales"));

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

        // Main settings panel
        JPanel settingsPanel = new JPanel(new GridBagLayout());
        settingsPanel.setBorder(BorderFactory.createTitledBorder("Application Settings"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Store Information section
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        settingsPanel.add(new JLabel("Store Information"), gbc);
        gbc.gridy = 1; gbc.gridwidth = 1;
        settingsPanel.add(new JSeparator(), gbc);

        gbc.gridy = 2;
        settingsPanel.add(new JLabel("Store Name:"), gbc);
        gbc.gridx = 1;
        settingsPanel.add(storeNameField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        settingsPanel.add(new JLabel("Store Address:"), gbc);
        gbc.gridx = 1;
        settingsPanel.add(new JScrollPane(storeAddressArea), gbc);

        // Tax section
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        settingsPanel.add(new JLabel("Tax Settings"), gbc);
        gbc.gridy = 5; gbc.gridwidth = 1;
        settingsPanel.add(new JSeparator(), gbc);


        gbc.gridy = 6;
        settingsPanel.add(new JLabel("Tax Rate (%):"), gbc);
        gbc.gridx = 1;
        settingsPanel.add(taxRateField, gbc);

        // UI Scaling section
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2;
        settingsPanel.add(new JLabel("UI Scaling"), gbc);
        gbc.gridy = 8; gbc.gridwidth = 1;
        settingsPanel.add(new JSeparator(), gbc);

        gbc.gridy = 9;
        settingsPanel.add(new JLabel("Scale:"), gbc);
        gbc.gridx = 1;
        JPanel scalePanel = new JPanel(new BorderLayout());
        scalePanel.add(uiScaleSlider, BorderLayout.CENTER);
        scalePanel.add(uiScaleLabel, BorderLayout.EAST);
        settingsPanel.add(scalePanel, gbc);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(saveButton);
        buttonPanel.add(resetSettingsButton);

        // Status panel
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.add(statusLabel, BorderLayout.WEST);

        // Add all panels to main panel
        add(navigationPanel, BorderLayout.NORTH);
        add(settingsPanel, BorderLayout.CENTER);

        // South panel for buttons and status
        JPanel southPanel = new JPanel();
        southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
        southPanel.add(buttonPanel);
        southPanel.add(statusPanel);

        add(southPanel, BorderLayout.SOUTH);
    }


    private void setupEventHandlers() {
        saveButton.addActionListener(e -> saveSettings());
        resetSettingsButton.addActionListener(e -> resetSettings());
        


        // UI scaling slider change listener for real-time preview
        uiScaleSlider.addChangeListener(e -> {
            int scaleValue = uiScaleSlider.getValue();
            uiScaleLabel.setText(scaleValue + "%");
            
            // Only apply scaling when user stops dragging (more stable)
            if (!uiScaleSlider.getValueIsAdjusting()) {
                applyScalingPreview(scaleValue / 100.0);
            }
        });
    }




    private void loadSettings() {
        storeNameField.setText((String) dataManager.getSetting("storeName"));
        storeAddressArea.setText((String) dataManager.getSetting("storeAddress"));
        taxRateField.setText(String.valueOf((Double) dataManager.getSetting("taxRate") * 100));
        
        // Load UI scale setting
        double uiScale = (Double) dataManager.getSetting("uiScale");
        int scalePercentage = (int) (uiScale * 100);
        uiScaleSlider.setValue(scalePercentage);
        uiScaleLabel.setText(scalePercentage + "%");
    }
    
    private void applyScalingPreview(double scaleFactor) {
        // Apply scaling preview to main window
        mainPOS.applyPreviewUIScaling(scaleFactor);
    }


    private void saveSettings() {
        // Validate input
        if (storeNameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Store name cannot be empty.",
                                        "Validation Error", JOptionPane.ERROR_MESSAGE);
            storeNameField.requestFocus();
            return;
        }

        if (storeAddressArea.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Store address cannot be empty.",
                                        "Validation Error", JOptionPane.ERROR_MESSAGE);
            storeAddressArea.requestFocus();
            return;
        }

        try {
            double taxRate = Double.parseDouble(taxRateField.getText().trim());
            if (taxRate < 0 || taxRate > 100) {
                JOptionPane.showMessageDialog(this, "Tax rate must be between 0 and 100.",
                                            "Validation Error", JOptionPane.ERROR_MESSAGE);
                taxRateField.requestFocus();
                return;
            }


            // Save settings
            dataManager.setSetting("storeName", storeNameField.getText().trim());
            dataManager.setSetting("storeAddress", storeAddressArea.getText().trim());
            dataManager.setSetting("taxRate", taxRate / 100.0); // Convert percentage to decimal
            dataManager.setSetting("uiScale", uiScaleSlider.getValue() / 100.0); // Convert percentage to decimal

            dataManager.saveData();

            // Apply UI scaling immediately
            mainPOS.applyUIScaling();




        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid tax rate.",
                                        "Invalid Input", JOptionPane.ERROR_MESSAGE);
            taxRateField.requestFocus();
        }
    }




    private void resetSettings() {
        int result = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to reset all settings to default values?",
            "Confirm Reset", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);



        if (result == JOptionPane.YES_OPTION) {
            // Reset to defaults
            dataManager.setSetting("storeName", "GPOS-General");
            dataManager.setSetting("storeAddress", "123 Main Street");
            dataManager.setSetting("taxRate", 0.08);
            dataManager.setSetting("uiScale", 1.0);

            dataManager.saveData();

            // Reload settings in UI
            loadSettings();

            // Apply UI scaling immediately
            mainPOS.applyUIScaling();

            statusLabel.setText("Settings reset to defaults");
            JOptionPane.showMessageDialog(this, "Settings reset to default values.",
                                        "Reset Complete", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // Public methods for external access
    public void refreshData() {
        loadSettings();
    }
}
