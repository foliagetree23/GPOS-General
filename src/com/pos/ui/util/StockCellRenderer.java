package com.pos.ui.util;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class StockCellRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        // The "Stock" column is at index 4 in SalesPanel and 5 in ProductManagementPanel.
        // A more robust solution would be to pass the column index to the constructor.
        int stockColumnIndex = -1;
        for (int i = 0; i < table.getColumnCount(); i++) {
            if ("Stock".equals(table.getColumnName(i))) {
                stockColumnIndex = i;
                break;
            }
        }

        if (stockColumnIndex != -1) {
            try {
                Object stockValue = table.getValueAt(row, stockColumnIndex);
                int stock = 0;
                if (stockValue instanceof Integer) {
                    stock = (Integer) stockValue;
                } else if (stockValue instanceof String) {
                    stock = Integer.parseInt((String) stockValue);
                }

                if (stock <= 0) {
                    c.setBackground(new Color(220, 220, 220)); // Light gray for out of stock
                    c.setForeground(Color.GRAY);
                } else {
                    c.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                    c.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
                }
            } catch (Exception e) {
                // Could be a parsing error if the column format changes.
                // Reset to default colors in case of an error.
                c.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                c.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
            }
        }

        return c;
    }
}
