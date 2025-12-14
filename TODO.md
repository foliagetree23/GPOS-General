# Price Amount Raw Number Implementation Plan

## Current State Analysis
- Product model uses `double` for price storage
- ProductDialog expects decimal format input (e.g., "12.99")
- PaymentDialog already treats amounts as integers
- Multiple files reference price calculations and displays

## Planned Changes

### 1. Product Model Updates (Product.java)
- Change `price` field from `double` to `int` (representing cents)
- Update constructor to accept price in cents
- Add helper methods for price conversion:
  - `getPriceAsDouble()` - returns price in decimal format
  - `setPriceFromDouble(double price)` - converts decimal to cents
  - `getDisplayPrice()` - formatted price string for UI

### 2. ProductDialog Updates (ProductDialog.java)
- Update price field validation to accept raw numbers only
- Remove decimal format validation (`\\d+\\.\\d+`)
- Update price parsing to work with integer input
- Update populateFields() to display formatted price
- Update saveProduct() to handle integer price storage

### 3. Transaction Model Updates (Transaction.java)
- Update price calculations to work with integer prices
- Ensure calculations maintain precision in cents

### 4. PaymentDialog Updates (PaymentDialog.java)
- Update amount display formatting to work with integer prices
- Ensure change calculations work correctly

### 5. DataManager Updates (DataManager.java)
- Update any price-related data operations
- Ensure data persistence works with integer prices

### 6. UI Display Updates (SalesPanel, ProductManagementPanel, etc.)
- Update price display formatting throughout UI
- Ensure all price displays show decimal format to users

### 7. Receipt Printer Updates (ReceiptPrinter.java)
- Update receipt formatting to work with integer prices
- Ensure proper decimal display in receipts

## Benefits
- Eliminates floating-point precision issues
- Ensures exact decimal calculations
- Prevents rounding errors in financial calculations
- Better performance for arithmetic operations

## Testing Requirements
- Test price input validation
- Test price display formatting
- Test transaction calculations
- Test receipt printing
- Test data persistence and loading
