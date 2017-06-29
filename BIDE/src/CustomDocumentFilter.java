import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

public class CustomDocumentFilter extends DocumentFilter {
    private StyledDocument styledDocument = null;

    private StyleContext styleContext = StyleContext.getDefaultStyleContext();
    private AttributeSet blackAttributeSet = styleContext.addAttribute(styleContext.getEmptySet(), StyleConstants.Foreground, Color.BLACK);
    public JTextPane textPane;
    public ColorationPattern[] regexes;
    
    public CustomDocumentFilter(JTextPane jtp, ColorationPattern[] regexes) {
    	this.textPane = jtp;
    	this.regexes = regexes;
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
        if (str.equals("=>"))
            super.replace(fb, offs, length, "t", a);
        else
            super.replace(fb, offs, length, str, a);
        
        handleTextChanged();
    }

    /**
     * Runs your updates later, not during the event notification.
     */
    private void handleTextChanged()
    {
    	long time = System.currentTimeMillis();
        updateTextStyles();
        System.out.println("Colored in " + (System.currentTimeMillis()-time) + "ms");
        /*SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
            	
            }
        });*/
    }


    private void updateTextStyles()
    {
        // Clear existing styles
        styledDocument.setCharacterAttributes(0, textPane.getText().length(), blackAttributeSet, true);

        // Look for tokens and highlight them
    	String textPaneText = textPane.getText();
    	SimpleAttributeSet sas = new SimpleAttributeSet();
        for (int i = 0; i < regexes.length; i++) {
        	StyleConstants.setForeground(sas, regexes[i].color);
        	StyleConstants.setBold(sas, regexes[i].isBold);
            Matcher matcher = regexes[i].pattern.matcher(textPaneText);
            while (matcher.find()) {
                // Change the color of recognized tokens
                styledDocument.setCharacterAttributes(matcher.start(), matcher.end() - matcher.start(), 
                		sas, false);
            }
        }
    }
}