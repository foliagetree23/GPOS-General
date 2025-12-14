package com.pos.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Transaction entity class representing sales transactions in the POS system
 */
public class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int transactionId;
    private LocalDateTime timestamp;

    private List<TransactionItem> items;
    private int subtotal; // in cents
    private int tax; // in cents
    private double taxRate;
    private int total; // in cents

    private String paymentMethod;
    private String customerName;
    private String notes;
    private int amountPaid; // in cents
    private boolean completed;
    
    // Default constructor
    public Transaction() {
        this.items = new ArrayList<>();
        this.timestamp = LocalDateTime.now();
        this.taxRate = 0.08; // 8% default tax rate
        this.completed = false;
    }
    
    // Constructor with basic parameters
    public Transaction(int transactionId) {
        this();
        this.transactionId = transactionId;
    }
    
    // Inner class for transaction items
    public static class TransactionItem implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private Product product;

        private int quantity;
        private int unitPrice; // in cents
        private int totalPrice; // in cents
        
        public TransactionItem() {}
        


        public TransactionItem(Product product, int quantity) {
            this.product = product;
            this.quantity = quantity;
            this.unitPrice = product.getPrice(); // Now returns int (cents)
            this.totalPrice = unitPrice * quantity;
        }
        
        // Getters and Setters
        public Product getProduct() {
            return product;
        }
        
        public void setProduct(Product product) {
            this.product = product;
        }
        
        public int getQuantity() {
            return quantity;
        }
        
        public void setQuantity(int quantity) {
            this.quantity = quantity;
            if (product != null) {
                this.totalPrice = unitPrice * quantity;
            }
        }
        

        public int getUnitPrice() {
            return unitPrice;
        }
        
        public void setUnitPrice(int unitPrice) {
            this.unitPrice = unitPrice;
            if (product != null) {
                this.totalPrice = unitPrice * quantity;
            }
        }
        
        public int getTotalPrice() {
            return totalPrice;
        }
        
        public void setTotalPrice(int totalPrice) {
            this.totalPrice = totalPrice;
        }
        

        @Override
        public String toString() {
            return String.format("%s x%d - $%.2f", 
                product != null ? product.getName() : "Unknown", 
                quantity, totalPrice / 100.0);
        }
    }
    
    // Getters and Setters
    public int getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public List<TransactionItem> getItems() {
        return new ArrayList<>(items);
    }
    
    public void setItems(List<TransactionItem> items) {
        this.items = items != null ? items : new ArrayList<>();
        recalculateTotals();
    }
    

    public int getSubtotal() {
        return subtotal;
    }
    
    public void setSubtotal(int subtotal) {
        this.subtotal = subtotal;
        recalculateTotal();
    }
    
    public int getTax() {
        return tax;
    }
    
    public void setTax(int tax) {
        this.tax = tax;
        recalculateTotal();
    }
    
    public double getTaxRate() {
        return taxRate;
    }
    
    public void setTaxRate(double taxRate) {
        this.taxRate = taxRate;
        recalculateTotals();
    }
    

    public int getTotal() {
        return total;
    }
    
    public void setTotal(int total) {
        this.total = total;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public String getCustomerName() {
        return customerName;
    }
    
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    

    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public int getAmountPaid() {
        return amountPaid;
    }
    
    public void setAmountPaid(int amountPaid) {
        this.amountPaid = amountPaid;
    }
    
    public boolean isCompleted() {
        return completed;
    }
    
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
    
    // Business logic methods
    public void addItem(Product product, int quantity) {
        if (product == null || quantity <= 0) return;
        
        // Check if product already exists in transaction
        for (TransactionItem item : items) {
            if (item.getProduct().getId() == product.getId()) {
                item.setQuantity(item.getQuantity() + quantity);
                recalculateTotals();
                return;
            }
        }
        
        // Add new item
        items.add(new TransactionItem(product, quantity));
        recalculateTotals();
    }
    
    public void removeItem(int productId) {
        items.removeIf(item -> item.getProduct().getId() == productId);
        recalculateTotals();
    }
    
    public void updateItemQuantity(int productId, int newQuantity) {
        for (TransactionItem item : items) {
            if (item.getProduct().getId() == productId) {
                if (newQuantity <= 0) {
                    removeItem(productId);
                } else {
                    item.setQuantity(newQuantity);
                }
                recalculateTotals();
                break;
            }
        }
    }
    
    public int getItemCount() {
        return items.stream().mapToInt(TransactionItem::getQuantity).sum();
    }
    
    public boolean isEmpty() {
        return items.isEmpty();
    }

    public void clear() {
        items.clear();
        recalculateTotals();
    }
    

    private void recalculateTotals() {
        subtotal = items.stream()
                .mapToInt(TransactionItem::getTotalPrice)
                .sum();
        tax = (int) Math.round(subtotal * taxRate);
        total = subtotal + tax;
    }
    
    private void recalculateTotal() {
        total = subtotal + tax;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Transaction that = (Transaction) obj;
        return transactionId == that.transactionId;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(transactionId);
    }
    
    @Override
    public String toString() {
        return String.format("Transaction #%d - $%.2f (%d items) - %s", 
                transactionId, total, getItemCount(), timestamp);
    }
}
