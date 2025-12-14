# Receipt Printer Enhancement Plan

## Objective
Modify the receipt printer to include customer name, notes, and change payment information. Display "-" for customer name when no name is provided.

## Current Analysis
- Transaction.java: Already has customerName and notes fields ✓
- PaymentDialog.java: Already has customerNameField and notesArea UI components ✓
- ReceiptPrinter.java: Partially implements customer name but needs enhancement

## Changes Required

### 1. ReceiptPrinter.java - formatReceiptText() Method
**Current Issues:**
- Customer name only shows if not null and not empty
- No change payment amount display
- No notes section

**Required Changes:**
1. Always display customer name (use "-" if empty)
2. Add change amount calculation and display in payment section
3. Add notes section after customer info
4. Improve payment information formatting

### 2. ReceiptPrinter.java - ReceiptPrintable Class
**Required Changes:**
1. Update the store address to be configurable
2. Ensure all new receipt fields print correctly

## Implementation Details

### formatReceiptText() Method Updates:
1. **Customer Name Section:**
   - Display customer name or "-" if empty
   - Format: "Customer: [name]"

2. **Payment Section Enhancement:**
   - Add amount paid
   - Add change amount calculation
   - Format: 
     ```
     Payment Method: [method]
     Amount Paid: $X.XX
     Change: $X.XX
     ```

3. **Notes Section:**
   - Add after customer name
   - Display notes or skip if empty
   - Format: "Notes: [notes]"

### Example Output Structure:
```
===========================================
               GPOS-General               
            123 Main Street               
===========================================

Transaction: #123
Date: 2024-01-15 14:30:25
Customer: John Doe
Notes: Please handle with care

-----------------------------------------
Item                  Qty     Price
-----------------------------------------
Coffee                2        $4.50
Sandwich              1        $8.99
-----------------------------------------
Subtotal:              $   13.49
Tax:                   $    1.08
-----------------------------------------
TOTAL:                 $   14.57
===========================================

Payment Method: Cash
Amount Paid: $  20.00
Change:       $   5.43

===========================================
           Thank you for your business!    
             Please come again!           
```

## Testing Steps
1. Test with customer name provided
2. Test with empty customer name (should show "-")
3. Test with notes provided
4. Test with empty notes (should skip section)
5. Test change calculation with different payment amounts
6. Test all payment methods (Cash, Card, Digital Wallet)

## Files to Modify
- `/home/ml1800sinkron/Java Agent/general-pos/src/com/pos/printer/ReceiptPrinter.java`
