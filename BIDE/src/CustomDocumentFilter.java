import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

public class CustomDocumentFilter extends DocumentFilter {
    private StyledDocument styledDocument = null;

    public JTextPane textPane;
    public ArrayList<ColorationPattern> regexes = null;
    
    public boolean isTooLaggy = false;
    public int type = 0;
    
    public CustomDocumentFilter(JTextPane jtp, ColorationPattern[] regexes, int type) {
    	this.textPane = jtp;
    	this.regexes = new ArrayList<ColorationPattern>(Arrays.asList(regexes));
    	this.type = type;
    	styledDocument = textPane.getStyledDocument();
    }
    
    @Override
    public void insertString(FilterBypass fb, int offset, String text, AttributeSet attributeSet) throws BadLocationException {
        super.insertString(fb, offset, text, attributeSet);

        handleTextChanged();
    }

    @Override
    public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
        super.remove(fb, offset, length);

        handleTextChanged();
    }
    
    @Override
    public void replace(final FilterBypass fb, final int offs, final int length, final String str, final AttributeSet a) throws BadLocationException {
    	if (type == BIDE.TYPE_PICT || type == BIDE.TYPE_CAPT) {
            if (str.equals("'")) {
                super.replace(fb, offs, length, "▀", a);
            } else if (str.equals(",")) {
                super.replace(fb, offs, length, "▄", a);
            } else if (str.equals(":")) {
                super.replace(fb, offs, length, "█", a);
            } else {
                super.replace(fb, offs, length, str, a);
            }
    	} else {
            super.replace(fb, offs, length, str, a);
    	}
        
        handleTextChanged();
    }

    /**
     * Runs your updates later, not during the event notification.
     */
    private void handleTextChanged()
    {
    	if (!isTooLaggy) {
    		updateTextStyles();
    	}
    }
    
    public void testForLag() {
    	long time = System.currentTimeMillis();
        updateTextStyles();
        time = System.currentTimeMillis()-time;

    	if (type == BIDE.TYPE_OPCODE) {
    		return;
    	}
        if (time > 100) {
        	this.isTooLaggy = true;
        	SimpleAttributeSet sas = new SimpleAttributeSet();
        	StyleConstants.setForeground(sas, Color.BLACK);
        	styledDocument.setCharacterAttributes(0, textPane.getText().length(), sas, false);
        	System.out.println("Disabled coloration on program \""+textPane.getText().substring(15, textPane.getText().indexOf("\n"))+"\", too laggy ("+time+"ms)");
        }
        //System.out.println("Test lag in " + time + "ms");
    }


    private void updateTextStyles() {

        // Look for tokens and highlight them
    	String textPaneText = textPane.getText();
    	MutableAttributeSet sas = new SimpleAttributeSet();
    	StyleConstants.setForeground(sas, Color.BLACK);
    	
        // Clear existing styles
        styledDocument.setCharacterAttributes(0, textPane.getText().length(), sas, true);
    	
        for (int i = 0; i < regexes.size(); i++) {
        	StyleConstants.setForeground(sas, regexes.get(i).color);
        	//StyleConstants.setBold(sas, regexes[i].isBold);
            Matcher matcher = regexes.get(i).pattern.matcher(textPaneText);
            while (matcher.find()) {
                // Change the color of recognized tokens
                styledDocument.setCharacterAttributes(matcher.start(), matcher.end() - matcher.start(), sas, false);
            }
        }
    }
}