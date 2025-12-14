package com.pos.printer;

import com.pos.model.Transaction;
import com.pos.model.Product;
import javax.swing.*;
import java.awt.*;
import java.awt.print.*;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Receipt Printer for generating and printing transaction receipts
 */
public class ReceiptPrinter {
    
    /**
     * Print a transaction receipt
     */
    public static void printReceipt(Transaction transaction) {
        if (transaction == null || transaction.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No transaction to print", 
                                        "Print Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Create a print dialog
        PrinterJob printerJob = PrinterJob.getPrinterJob();
        printerJob.setPrintable(new ReceiptPrintable(transaction));
        
        if (printerJob.printDialog()) {
            try {
                printerJob.print();
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(null, "Error printing receipt: " + ex.getMessage(),
                                            "Print Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Display receipt in a dialog window
     */
    public static void displayReceiptDialog(Transaction transaction, String storeName, String storeAddress) {
        if (transaction == null || transaction.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No transaction to display", 
                                        "Display Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        JFrame frame = new JFrame("Receipt - Transaction #" + transaction.getTransactionId());
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(400, 600);
        
        JTextArea receiptArea = new JTextArea();
        receiptArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        receiptArea.setEditable(false);
        receiptArea.setText(formatReceiptText(transaction, storeName, storeAddress));
        
        JScrollPane scrollPane = new JScrollPane(receiptArea);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton printButton = new JButton("Print");
        JButton closeButton = new JButton("Close");
        
        printButton.addActionListener(e -> printReceipt(transaction));
        closeButton.addActionListener(e -> frame.dispose());
        
        buttonPanel.add(printButton);
        buttonPanel.add(closeButton);
        
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    

    /**
     * Format receipt as text for display or printing
     */
    public static String formatReceiptText(Transaction transaction, String storeName, String storeAddress) {
        StringBuilder receipt = new StringBuilder();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        // Header
        receipt.append("===========================================\n");
        receipt.append(String.format("%-25s\n", centerText(storeName, 43)));
        receipt.append(String.format("%-25s\n", centerText(storeAddress, 43)));
        receipt.append("===========================================\n\n");
        
        // Transaction info
        receipt.append(String.format("Transaction: #%d\n", transaction.getTransactionId()));
        receipt.append(String.format("Date: %s\n", dateFormat.format(java.sql.Timestamp.valueOf(transaction.getTimestamp()))));
        if (transaction.getCustomerName() != null && !transaction.getCustomerName().isEmpty()) {
            receipt.append(String.format("Customer: %s\n", transaction.getCustomerName()));
        }
        receipt.append("\n");
        
        // Items header
        receipt.append("-----------------------------------------\n");
        receipt.append(String.format("%-20s %4s %10s\n", "Item", "Qty", "Price"));
        receipt.append("-----------------------------------------\n");
        
        // Items
        for (Transaction.TransactionItem item : transaction.getItems()) {
            Product product = item.getProduct();
            String itemName = truncateString(product.getName(), 20);
            String quantity = String.valueOf(item.getQuantity());

            String price = String.format("$%.2f", item.getTotalPrice() / 100.0);
            
            receipt.append(String.format("%-20s %4s %10s\n", itemName, quantity, price));
        }
        
        receipt.append("-----------------------------------------\n");
        

        // Totals
        receipt.append(String.format("%-24s $%8.2f\n", "Subtotal:", transaction.getSubtotal() / 100.0));
        receipt.append(String.format("%-24s $%8.2f\n", "Tax:", transaction.getTax() / 100.0));
        receipt.append("-----------------------------------------\n");
        receipt.append(String.format("%-24s $%8.2f\n", "TOTAL:", transaction.getTotal() / 100.0));
        receipt.append("===========================================\n\n");
        
        // Payment info
        receipt.append(String.format("Payment Method: %s\n", transaction.getPaymentMethod() != null ? 
                                  transaction.getPaymentMethod() : "Cash"));
        receipt.append("\n");
        
        // Footer
        receipt.append(String.format("%-25s\n", centerText("Thank you for your business!", 43)));
        receipt.append(String.format("%-25s\n", centerText("Please come again!", 43)));
        
        return receipt.toString();
    }
    
    /**
     * Print a text report
     */
    public static void printTextReport(String title, String content) {
        PrinterJob printerJob = PrinterJob.getPrinterJob();
        printerJob.setPrintable(new TextReportPrintable(title, content));
        
        if (printerJob.printDialog()) {
            try {
                printerJob.print();
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(null, "Error printing report: " + ex.getMessage(),
                                            "Print Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Utility methods
     */
    private static String centerText(String text, int width) {
        if (text == null) text = "";
        if (text.length() >= width) return text.substring(0, width);
        
        int padding = (width - text.length()) / 2;
        return " ".repeat(padding) + text + " ".repeat(width - padding - text.length());
    }
    
    private static String truncateString(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
}

/**
 * Printable interface implementation for receipts
 */
class ReceiptPrintable implements Printable {
    private final Transaction transaction;

    private final String storeName = "GPOS-General";
    private final String storeAddress = "123 Main Street";
    
    public ReceiptPrintable(Transaction transaction) {
        this.transaction = transaction;
    }
    
    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        if (pageIndex > 0) {
            return NO_SUCH_PAGE;
        }
        
        Graphics2D g2d = (Graphics2D) graphics;
        g2d.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
        
        // Set up page format
        double x = pageFormat.getImageableX();
        double y = pageFormat.getImageableY();
        double width = pageFormat.getImageableWidth();
        double height = pageFormat.getImageableHeight();
        
        g2d.translate((int) x, (int) y);
        

        // Print receipt content
        String receiptText = ReceiptPrinter.formatReceiptText(transaction, storeName, storeAddress);
        String[] lines = receiptText.split("\n");
        
        int lineHeight = 12;
        int currentY = 0;
        
        for (String line : lines) {
            if (currentY + lineHeight > height) {
                // Page overflow - create new page
                return PAGE_EXISTS;
            }
            g2d.drawString(line, 0, currentY);
            currentY += lineHeight;
        }
        
        return PAGE_EXISTS;
    }
}

/**
 * Printable interface implementation for text reports
 */
class TextReportPrintable implements Printable {
    private final String title;
    private final String content;
    
    public TextReportPrintable(String title, String content) {
        this.title = title;
        this.content = content;
    }
    
    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        Graphics2D g2d = (Graphics2D) graphics;
        g2d.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
        
        double x = pageFormat.getImageableX();
        double y = pageFormat.getImageableY();
        double width = pageFormat.getImageableWidth();
        double height = pageFormat.getImageableHeight();
        
        g2d.translate((int) x, (int) y);
        
        int lineHeight = 12;
        int currentY = 0;
        
        // Print title
        g2d.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));
        g2d.drawString(title, 0, currentY);
        currentY += 20;
        
        // Print content
        g2d.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
        String[] lines = content.split("\n");
        
        for (String line : lines) {
            if (currentY + lineHeight > height) {
                return PAGE_EXISTS;
            }
            g2d.drawString(line, 0, currentY);
            currentY += lineHeight;
        }
        
        return PAGE_EXISTS;
    }
}
