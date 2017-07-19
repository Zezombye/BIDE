package zezombye.BIDE;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import javax.swing.plaf.TextUI;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.DocumentFilter.FilterBypass;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.Segment;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.autocomplete.ShorthandCompletion;
import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.fife.ui.rtextarea.CaretStyle;
import org.fife.ui.rtextarea.ConfigurableCaret;
import org.fife.ui.rtextarea.RTextArea;

public class ProgramTextPane extends RSyntaxTextArea {
	
	
	public ProgramTextPane(int type) {
		super();
		if (type == BIDE.TYPE_CAPT || type == BIDE.TYPE_PICT) {
        	this.setFont(BIDE.pictFont);
        } else if (type == BIDE.TYPE_PROG || type == BIDE.TYPE_OPCODE || type == BIDE.TYPE_CHARLIST){
        	this.setFont(BIDE.progFont);
        } else {
        	this.setFont(BIDE.dispFont);
        }
		this.setBackground(Color.WHITE);
		this.setForeground(Color.BLACK);
		this.setCaretStyle(OVERWRITE_MODE, CaretStyle.UNDERLINE_STYLE);
		this.setTabSize(4);
		if (type == BIDE.TYPE_PICT || type == BIDE.TYPE_CAPT) {
			//this.setCaretStyle(RSyntaxTextArea.OVERWRITE_MODE, CaretStyle.BLOCK_STYLE);
			this.setCaretStyle(OVERWRITE_MODE, CaretStyle.UNDERLINE_STYLE);
			this.setTextMode(RSyntaxTextArea.OVERWRITE_MODE);
			//this.setCaretColor(new Color(0, 128, 255));
			this.setCaretColor(Color.RED);
			this.setLineWrap(false);
			((AbstractDocument)this.getDocument()).setDocumentFilter(new DocumentFilter() {
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
			    }
			});
			
		}
		
		this.setLineWrap(true);
		this.setBackground(new Color(Integer.parseInt(BIDE.options.getProperty("bgColor"), 16)));
		this.setForeground(new Color(Integer.parseInt(BIDE.options.getProperty("textColor"), 16)));
		this.setCurrentLineHighlightColor(new Color(Integer.parseInt(BIDE.options.getProperty("hlColor"), 16)));
		//this.getDocument().setParagraphAttributes(0, this.getDocument().getLength(), 1, true);
		AbstractTokenMakerFactory atmf = (AbstractTokenMakerFactory)TokenMakerFactory.getDefaultInstance();
		atmf.putMapping("test/BasicCasio", "zezombye.BIDE.SyntaxColoration");
		this.setSyntaxEditingStyle("text/BasicCasio");
		//this.setSyntaxEditingStyle(SYNTAX_STYLE_JAVA);
		
		if (BIDE.options.getProperty("autocomplete").equals("true")) {
			AutoCompletion ac = new AutoCompletion(getAutoComplete());
			ac.setAutoActivationEnabled(true);
			ac.setAutoCompleteSingleChoices(false);
			ac.setAutoActivationDelay(0);
		    ac.install(this);
		}
	}
	
	public CompletionProvider getAutoComplete() {

	    DefaultCompletionProvider provider = new DefaultCompletionProvider() {
	    	@Override protected boolean isValidChar(char ch) {
	    		return Character.isLetterOrDigit(ch) || ch=='_' || ch=='&';
	    	}
	    };
	    for (int i = 0; i < BIDE.opcodes.size(); i++) {
	    	provider.addCompletion(new BasicCompletion(provider, BIDE.opcodes.get(i).text));
	    }
		provider.setAutoActivationRules(true, null);
		return provider;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		//((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
		super.paintComponent(g);
	}
	
}
