package com.pos.ui.util;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.Toolkit;

public class CurrencyAwareDocumentFilter extends DocumentFilter {
    @Override
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
        if (string == null) {
            return;
        }

        StringBuilder sb = new StringBuilder(fb.getDocument().getText(0, fb.getDocument().getLength()));
        sb.insert(offset, string);

        if (handleFormatting(fb, sb.toString(), attr)) {
            return;
        }

        super.insertString(fb, offset, string, attr);
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
        if (text == null) {
            text = "";
        }

        StringBuilder sb = new StringBuilder(fb.getDocument().getText(0, fb.getDocument().getLength()));
        sb.replace(offset, offset + length, text);

        if (handleFormatting(fb, sb.toString(), attrs)) {
            return;
        }

        super.replace(fb, offset, length, text, attrs);
    }

    @Override
    public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
        StringBuilder sb = new StringBuilder(fb.getDocument().getText(0, fb.getDocument().getLength()));
        sb.delete(offset, offset + length);

        if (handleFormatting(fb, sb.toString(), null)) {
            return;
        }

        super.remove(fb, offset, length);
    }

    private boolean handleFormatting(FilterBypass fb, String text, AttributeSet attr) throws BadLocationException {
        String cleanText = text.replaceAll("[^\\d]", "");
        if (cleanText.isEmpty()) {
            fb.replace(0, fb.getDocument().getLength(), "", attr);
            return true;
        }

        try {
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

            fb.replace(0, fb.getDocument().getLength(), formattedText, attr);
            return true;
        } catch (Exception e) {
            Toolkit.getDefaultToolkit().beep();
            return false;
        }
    }
}