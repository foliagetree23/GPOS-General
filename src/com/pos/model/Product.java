package com.pos.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Product entity class representing items in the POS system
 */
public class Product implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int id;
    private String name;
    private String description;

    private int price; // Price in cents
    private String category;
    private int quantity;
    private int minStockLevel;
    private String barcode;
    private boolean active;
    
    // Default constructor
    public Product() {
        this.active = true;
        this.minStockLevel = 5;
    }
    

    // Constructor with essential parameters
    public Product(String name, int price, String category) {
        this();
        this.name = name;
        this.price = price;
        this.category = category;
    }
    

    // Full constructor
    public Product(int id, String name, String description, int price, 
                   String category, int quantity, int minStockLevel, String barcode) {
        this(name, price, category);
        this.id = id;
        this.description = description;
        this.quantity = quantity;
        this.minStockLevel = minStockLevel;
        this.barcode = barcode;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    

    // Price in cents (raw number)
    public int getPrice() {
        return price;
    }
    
    public void setPrice(int price) {
        this.price = price;
    }
    
    // Helper method to get price as double for legacy compatibility
    public double getPriceAsDouble() {
        return price / 100.0;
    }
    
    // Helper method to set price from double
    public void setPriceFromDouble(double price) {
        this.price = (int) Math.round(price * 100);
    }
    
    // Helper method to get formatted price string for display
    public String getDisplayPrice() {
        return String.format("$%.2f", price / 100.0);
    }
    
    // Helper method to get price in cents from string input
    public static int parsePriceFromString(String priceStr) {
        try {
            // Remove currency symbols and whitespace
            String cleanStr = priceStr.replaceAll("[^\\d.]", "");
            if (cleanStr.isEmpty()) {
                return 0;
            }
            double price = Double.parseDouble(cleanStr);
            return (int) Math.round(price * 100);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
    public int getMinStockLevel() {
        return minStockLevel;
    }
    
    public void setMinStockLevel(int minStockLevel) {
        this.minStockLevel = minStockLevel;
    }
    
    public String getBarcode() {
        return barcode;
    }
    
    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    // Helper methods
    public boolean isLowStock() {
        return quantity <= minStockLevel;
    }
    

    public double getTotalValue() {
        return (price * quantity) / 100.0;
    }
    
    // Get total value in cents
    public int getTotalValueInCents() {
        return price * quantity;
    }
    
    public boolean hasBarcode() {
        return barcode != null && !barcode.trim().isEmpty();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Product product = (Product) obj;
        return id == product.id;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", category='" + category + '\'' +
                ", quantity=" + quantity +
                '}';
    }
}
