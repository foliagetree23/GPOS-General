import com.pos.model.Product;
import com.pos.model.Transaction;
import com.pos.printer.ReceiptPrinter;
import java.time.LocalDateTime;

public class test_receipt {
    public static void main(String[] args) {
        // Create a test transaction
        Transaction transaction = new Transaction(123);
        transaction.setTimestamp(LocalDateTime.now());
        
        // Add some test items
        Product coffee = new Product(1, "Coffee", 250, 50); // $2.50, 50 in stock
        Product sandwich = new Product(2, "Sandwich", 899, 20); // $8.99, 20 in stock
        
        transaction.addItem(coffee, 2);
        transaction.addItem(sandwich, 1);
        
        // Set payment and customer info
        transaction.setCustomerName("John Doe");
        transaction.setNotes("Please handle with care");
        transaction.setPaymentMethod("Cash");
        transaction.setAmountPaid(2000); // $20.00
        
        // Test receipt formatting
        String receipt = ReceiptPrinter.formatReceiptText(
            transaction, 
            "GPOS-General Test Store", 
            "123 Test Street",
            2000 // $20.00 paid
        );
        
        System.out.println("=== TEST RECEIPT OUTPUT ===");
        System.out.println(receipt);
        
        // Test with empty customer name (should show "-")
        Transaction emptyCustomerTransaction = new Transaction(124);
        emptyCustomerTransaction.setTimestamp(LocalDateTime.now());
        emptyCustomerTransaction.addItem(coffee, 1);
        emptyCustomerTransaction.setPaymentMethod("Card");
        emptyCustomerTransaction.setAmountPaid(300); // $3.00
        
        String emptyCustomerReceipt = ReceiptPrinter.formatReceiptText(
            emptyCustomerTransaction,
            "GPOS-General Test Store",
            "123 Test Street", 
            300
        );
        
        System.out.println("=== TEST RECEIPT WITH EMPTY CUSTOMER ===");
        System.out.println(emptyCustomerReceipt);
    }
}

