
package com.pos.ui;

import com.pos.manager.DataManager;
import com.pos.model.Product;
import com.pos.model.Transaction;
import com.pos.ui.util.CurrencyAwareDocumentFilter;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Payment Dialog - For processing payment and completing transactions
 */
public class PaymentDialog extends JDialog {
    private Transaction transaction;
    private DataManager dataManager;
    private boolean paymentCompleted;
    
    // Payment components
    private JRadioButton cashRadioButton;
    private JRadioButton cardRadioButton;
    private JRadioButton digitalRadioButton;
    private ButtonGroup paymentMethodGroup;
    
    // Amount components
    private JLabel totalAmountLabel;
    private JTextField paidAmountField;
    private JLabel changeLabel;
    

    // Currency formatting - now handled by CurrencyAwareFormatter
    
    // Customer info
    private JTextField customerNameField;
    private JTextArea notesArea;
    
    // Buttons
    private JButton completePaymentButton;
    private JButton cancelButton;
    

    public PaymentDialog(Frame parent, Transaction transaction, DataManager dataManager) {
        super(parent, "Process Payment", true);
        this.transaction = transaction;
        this.dataManager = dataManager;
        this.paymentCompleted = false;
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        
        pack();
        if (parent != null) {
            setLocationRelativeTo(parent);
        }
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }
    
    private void initializeComponents() {
        // Payment method radio buttons
        cashRadioButton = new JRadioButton("Cash");
        cardRadioButton = new JRadioButton("Card");
        digitalRadioButton = new JRadioButton("Digital Wallet");
        
        paymentMethodGroup = new ButtonGroup();
        paymentMethodGroup.add(cashRadioButton);
        paymentMethodGroup.add(cardRadioButton);
        paymentMethodGroup.add(digitalRadioButton);
        cashRadioButton.setSelected(true); // Default selection
        

        // Amount labels and fields

        totalAmountLabel = new JLabel(formatWithDots(transaction.getTotal()));
        totalAmountLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        totalAmountLabel.setForeground(new Color(34, 139, 34));





        // Use text field with currency-aware formatting
        paidAmountField = new JTextField();
        paidAmountField.setColumns(15); // Set visible columns but allow limitless input
        paidAmountField.setHorizontalAlignment(JTextField.RIGHT);
        
        // Apply currency-aware document filter to enable autodecimal functionality
        javax.swing.text.Document document = paidAmountField.getDocument();
        if (document instanceof javax.swing.text.PlainDocument) {
            ((javax.swing.text.PlainDocument) document).setDocumentFilter(new CurrencyAwareDocumentFilter());
        }

        changeLabel = new JLabel("0");
        changeLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        
        // Customer info
        customerNameField = new JTextField(20);
        notesArea = new JTextArea(3, 20);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        
        // Buttons
        completePaymentButton = new JButton("Complete Payment");
        completePaymentButton.setMnemonic('C');
        completePaymentButton.setBackground(new Color(34, 139, 34));
        completePaymentButton.setForeground(Color.WHITE);
        completePaymentButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        
        cancelButton = new JButton("Cancel");
        cancelButton.setMnemonic('N');
    }


    // Currency formatting is now handled by CurrencyAwareFormatter and CurrencyUtil
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Main panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Payment method section
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(new JLabel("Payment Method:"), gbc);
        
        JPanel paymentMethodPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        paymentMethodPanel.add(cashRadioButton);
        paymentMethodPanel.add(cardRadioButton);
        paymentMethodPanel.add(digitalRadioButton);
        
        gbc.gridy = 1; gbc.gridwidth = 2;
        mainPanel.add(paymentMethodPanel, gbc);
        
        // Amount section
        gbc.gridy = 2; gbc.gridwidth = 1;
        mainPanel.add(new JLabel("Total Amount:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(totalAmountLabel, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        mainPanel.add(new JLabel("Amount Paid:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(paidAmountField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4;
        mainPanel.add(new JLabel("Change:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(changeLabel, gbc);
        
        // Customer info section
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        mainPanel.add(new JSeparator(), gbc);
        
        gbc.gridy = 6; gbc.gridwidth = 1;
        mainPanel.add(new JLabel("Customer Name (Optional):"), gbc);
        gbc.gridx = 1;
        mainPanel.add(customerNameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 1;
        mainPanel.add(new JLabel("Notes:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(new JScrollPane(notesArea), gbc);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(completePaymentButton);
        buttonPanel.add(cancelButton);
        
        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        // Payment amount field - calculate change
        paidAmountField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { calculateChange(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { calculateChange(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { calculateChange(); }
        });

        // Complete payment button
        completePaymentButton.addActionListener(e -> completePayment());

        // Cancel button
        cancelButton.addActionListener(e -> dispose());

        // Enter key on paid amount field completes payment
        paidAmountField.addActionListener(e -> completePayment());
    }
    



    private void calculateChange() {
        try {
            String paidText = paidAmountField.getText().trim().replace(".", "");
            if (paidText.isEmpty()) {
                changeLabel.setText("0");
                changeLabel.setForeground(Color.RED);
                return;
            }
            
            int paidAmount = Integer.parseInt(paidText);

            int change = paidAmount - transaction.getTotal();
            
            if (change < 0) {
                changeLabel.setForeground(Color.RED);
                changeLabel.setText("Insufficient");
            } else {
                changeLabel.setForeground(Color.BLACK);
                changeLabel.setText(formatWithDots(change));
            }
        } catch (Exception e) {
            changeLabel.setText("0");
            changeLabel.setForeground(Color.RED);
        }
    }
    

    private void completePayment() {
        // Validate stock before processing payment
        for (Transaction.TransactionItem item : transaction.getItems()) {
            Product product = dataManager.getProductById(item.getProduct().getId());
            if (product == null || product.getQuantity() < item.getQuantity()) {
                JOptionPane.showMessageDialog(this,
                    "The product '" + item.getProduct().getName() + "' is out of stock or has insufficient quantity.",
                    "Stock Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }


        // Validate payment amount
        try {
            String paidText = paidAmountField.getText().trim().replace(".", "");
            if (paidText.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Please enter the payment amount.",
                    "Payment Required", JOptionPane.WARNING_MESSAGE);
                paidAmountField.requestFocus();
                return;
            }

            int paidAmount = Integer.parseInt(paidText);



            if (paidAmount < transaction.getTotal()) {
                JOptionPane.showMessageDialog(this,
                    "Payment amount is less than the total. Please enter a sufficient amount.",
                    "Insufficient Payment", JOptionPane.ERROR_MESSAGE);
                paidAmountField.requestFocus();
                return;
            }

            // Set payment method
            String paymentMethod = "Cash"; // default
            if (cardRadioButton.isSelected()) {
                paymentMethod = "Card";
            } else if (digitalRadioButton.isSelected()) {
                paymentMethod = "Digital Wallet";
            }


            // Update transaction
            transaction.setPaymentMethod(paymentMethod);
            transaction.setCustomerName(customerNameField.getText().trim());
            transaction.setNotes(notesArea.getText().trim());
            transaction.setAmountPaid(paidAmount);

            paymentCompleted = true;


            JOptionPane.showMessageDialog(this,
                "Payment completed successfully!\nChange: " + formatWithDots(paidAmount - transaction.getTotal()),
                "Payment Successful", JOptionPane.INFORMATION_MESSAGE);

            dispose();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid numeric payment amount.",
                                        "Invalid Amount", JOptionPane.ERROR_MESSAGE);
            paidAmountField.requestFocus();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "An error occurred while processing payment. Please try again.",
                                        "Payment Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    public boolean isPaymentCompleted() {
        return paymentCompleted;
    }

    private String formatWithDots(int amount) {
        String cleanText = String.valueOf(amount);
        if (cleanText.isEmpty()) {
            return "";
        }

        String formattedText;
        if (cleanText.length() <= 2) {
            formattedText = cleanText;
        } else {
            String cents = cleanText.substring(cleanText.length() - 2);
            String whole = cleanText.substring(0, cleanText.length() - 2);
            
            StringBuilder formattedWhole = new StringBuilder();
            int wholeLen = whole.length();
            int firstGroupLen = wholeLen % 3;
            if (firstGroupLen == 0 && wholeLen > 0) {
                firstGroupLen = 3;
            }

            if (firstGroupLen > 0) {
                formattedWhole.append(whole.substring(0, firstGroupLen));
            }

            for (int i = firstGroupLen; i < wholeLen; i += 3) {
                formattedWhole.append('.').append(whole.substring(i, i + 3));
            }
            
            formattedText = formattedWhole.toString() + "." + cents;
        }
        return formattedText;
    }





    // New method to refresh display
    public void refreshData() {
        totalAmountLabel.setText(formatWithDots(transaction.getTotal()));
        calculateChange(); // Recalculate change
    }
}
