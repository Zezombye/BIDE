package zezombye.BIDE;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.plaf.TextUI;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.DocumentFilter.FilterBypass;
import javax.swing.text.Segment;

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
        } else {
        	this.setFont(BIDE.progFont);
        }
		this.setBackground(Color.WHITE);
		this.setForeground(Color.BLACK);
		this.setTabSize(4);
		if (type == BIDE.TYPE_PICT || type == BIDE.TYPE_CAPT) {
			//this.setCaretStyle(RSyntaxTextArea.OVERWRITE_MODE, CaretStyle.BLOCK_STYLE);
			this.setCaretStyle(OVERWRITE_MODE, CaretStyle.BLOCK_BORDER_STYLE);
			this.setTextMode(RSyntaxTextArea.OVERWRITE_MODE);
			//this.setCaretColor(new Color(0, 128, 255));
			this.setCaretColor(Color.RED);
			
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
		AbstractTokenMakerFactory atmf = (AbstractTokenMakerFactory)TokenMakerFactory.getDefaultInstance();
		atmf.putMapping("test/BasicCasio", "zezombye.BIDE.SyntaxColoration");
		this.setSyntaxEditingStyle("text/BasicCasio");
		//this.setSyntaxEditingStyle(SYNTAX_STYLE_JAVA);
	}
	
}
