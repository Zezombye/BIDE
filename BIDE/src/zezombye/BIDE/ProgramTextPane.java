package zezombye.BIDE;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.JEditorPane;
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

import org.fife.ui.autocomplete.AbstractCompletionProvider;
import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.CompletionCellRenderer;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.autocomplete.ShorthandCompletion;
import org.fife.ui.autocomplete.ParameterizedCompletion;
import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Style;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.TokenTypes;
import org.fife.ui.rtextarea.CaretStyle;
import org.fife.ui.rtextarea.ConfigurableCaret;
import org.fife.ui.rtextarea.RTextArea;

public class ProgramTextPane extends RSyntaxTextArea {
	
	public static CompletionProvider cp;
	
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
		
		
		this.setLineWrap(true);
		this.setBackground(new Color(Integer.parseInt(BIDE.options.getProperty("bgColor"), 16)));
		this.setForeground(new Color(Integer.parseInt(BIDE.options.getProperty("textColor"), 16)));
		this.setCurrentLineHighlightColor(new Color(Integer.parseInt(BIDE.options.getProperty("hlColor"), 16)));
		//this.getDocument().setParagraphAttributes(0, this.getDocument().getLength(), 1, true);
		/*AbstractTokenMakerFactory atmf = (AbstractTokenMakerFactory)TokenMakerFactory.getDefaultInstance();
		atmf.putMapping("test/BasicCasio", "zezombye.BIDE.SyntaxColoration");
		this.setSyntaxEditingStyle("text/BasicCasio");*/
		this.setSyntaxEditingStyle(SYNTAX_STYLE_JAVA);
		
		//Set colors
		SyntaxScheme ss = (SyntaxScheme)this.getSyntaxScheme().clone();

		final Color keywordColor = new Color(Integer.parseInt(BIDE.options.getProperty("keywordColor"), 16));
		final Color operatorColor = new Color(Integer.parseInt(BIDE.options.getProperty("operatorColor"), 16));
		final Color variableColor = new Color(Integer.parseInt(BIDE.options.getProperty("variableColor"), 16));
		final Color borderColor = new Color(Integer.parseInt(BIDE.options.getProperty("borderColor"), 16));
		final Color strColor = new Color(Integer.parseInt(BIDE.options.getProperty("strColor"), 16));
		final Color entityColor = new Color(Integer.parseInt(BIDE.options.getProperty("entityColor"), 16));
		final Color commentColor = new Color(Integer.parseInt(BIDE.options.getProperty("commentColor"), 16));
		final Color preprocessorColor = new Color(Integer.parseInt(BIDE.options.getProperty("preprocessorColor"), 16));
		
		ss.setStyle(Token.RESERVED_WORD, new Style(keywordColor, this.getFont()));
		ss.setStyle(Token.RESERVED_WORD_2, new Style(operatorColor));
		ss.setStyle(Token.OPERATOR, new Style(operatorColor));
		ss.setStyle(Token.VARIABLE, new Style(variableColor));
		ss.setStyle(Token.LITERAL_STRING_DOUBLE_QUOTE, new Style(strColor));
		ss.setStyle(Token.ERROR_STRING_DOUBLE, new Style(strColor));
		ss.setStyle(Token.COMMENT_EOL, new Style(commentColor, this.getFont().deriveFont(Font.ITALIC)));
		ss.setStyle(Token.MARKUP_ENTITY_REFERENCE, new Style(entityColor));
		ss.setStyle(Token.PREPROCESSOR, new Style(preprocessorColor));
		ss.setStyle(Token.FUNCTION, new Style(new Color(Integer.parseInt(BIDE.options.getProperty("textColor"), 16))));
		ss.setStyle(Token.DATA_TYPE, new Style(borderColor));
		
		
		
		this.setSyntaxScheme(ss);
		
		if (type == BIDE.TYPE_PICT || type == BIDE.TYPE_CAPT) {
			//this.setCaretStyle(RSyntaxTextArea.OVERWRITE_MODE, CaretStyle.BLOCK_STYLE);
			this.setCaretStyle(OVERWRITE_MODE, CaretStyle.BLOCK_BORDER_STYLE);
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
			
		} else if (type == BIDE.TYPE_PROG) {
			//((AbstractDocument)this.getDocument()).setDocumentFilter(new CustomDocumentFilter(this, new SyntaxColoration().getColorationPatterns(BIDE.TYPE_PROG), BIDE.TYPE_PROG));
		}
		
		if (BIDE.options.getProperty("autocomplete").equals("true")) {
			AutoCompletion ac = new AutoCompletion(cp);
			ac.setAutoActivationEnabled(true);
			ac.setAutoCompleteSingleChoices(false);
			ac.setAutoActivationDelay(0);
		    ac.install(this);
		}
	}
	
	public static void initAutoComplete() {
	    DefaultCompletionProvider provider = new DefaultCompletionProvider() {
	    	@Override protected boolean isValidChar(char ch) {
	    		///return (ch >= 'A' && ch <= 'z') || "&^_".contains(""+ch);
	    		return ch >= '!' && ch <= '~';
	    	}
	    };
	    CompletionCellRenderer ccr = new CompletionCellRenderer();
	    ccr.setFont(new Font("Courier New", Font.PLAIN, 12));
	    provider.setListCellRenderer(ccr);
	    provider.setAutoActivationRules(false, "!#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~");
	    ArrayList<Opcode> opcodes2 = new ArrayList<Opcode>(BIDE.opcodes);
	    Collections.sort(opcodes2, new Comparator<Opcode>() {
			@Override
			public int compare(Opcode o1, Opcode o2) {
				return o1.hex.compareTo(o2.hex);
			}
		});
	    
	    //System.out.println(opcodes2.toString());
	    
	    for (int i = 0; i < opcodes2.size(); i++) {
	    	if (opcodes2.get(i).text.length() > 1 && opcodes2.get(i).text.matches("([ -~])+")) {
	    		String txt = opcodes2.get(i).text.replaceAll("^ +", "");
	    		//Check if there is another opcode with the same hex value, but with unicode text
	    		//Note that the unicode opcode is always before the ascii one
	    		if (BIDE.options.getProperty("allowUnicode").equals("true") && opcodes2.get(i-1).hex.equals(opcodes2.get(i).hex)) {
	    			if (opcodes2.get(i-1).text.length() == 1) {
				    	provider.addCompletion(new ShorthandCompletion(provider, txt, opcodes2.get(i-1).text, opcodes2.get(i-1).text, opcodes2.get(i).relevance));
	    			} else {
				    	provider.addCompletion(new ShorthandCompletion(provider, txt, opcodes2.get(i-1).text, opcodes2.get(i).relevance));
	    			}
			    	
	    		} else {
			    	provider.addCompletion(new BasicCompletion(provider, txt, opcodes2.get(i).relevance));
	    		}
	    	}
	    }
	    for (int i = 0; i < BIDE.macros.size(); i++) {
	    	if (BIDE.macros.get(i).text.length() > 1 && BIDE.macros.get(i).text.matches("\\w+")) {
	    		provider.addCompletion(new BasicCompletion(provider, BIDE.macros.get(i).text, 2));
	    	}
	    	
	    }
		cp = provider;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		//((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
		super.paintComponent(g);
	}
	
}
