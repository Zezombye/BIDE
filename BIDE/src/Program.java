import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.Element;
import javax.swing.text.IconView;
import javax.swing.text.LabelView;
import javax.swing.text.ParagraphView;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

public class Program extends JScrollPane {
	
	public FixedJTextPane textPane = new FixedJTextPane();
	TextLineNumber tln = new TextLineNumber(textPane);
	
	public Program(String content) {
		this.setViewportView(textPane);
		this.getVerticalScrollBar().setUnitIncrement(30);
		this.setBorder(BorderFactory.createEmptyBorder());
		this.setRowHeaderView(tln);
		textPane.setText(content);
		tln.setBorderGap(10);
		tln.setCurrentLineForeground(null);
	}
}

class FixedJTextPane extends JTextPane {

	Font font = new Font("Courier new", Font.PLAIN, 13);
	
	public FixedJTextPane() {
        this.setEditorKit(new WrapEditorKit());
        //JScrollPane jsp = new JScrollPane(this);
        //jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        

		//this.setMaximumSize(this.getParent().getSize());
		this.setFont(font);
		this.setBackground(Color.WHITE);
		this.setForeground(Color.BLACK);
		this.setTabs(this, 4);
		//Syntax coloration
		((AbstractDocument)this.getDocument()).setDocumentFilter(new CustomDocumentFilter(this, new SyntaxColoration().getColorationPatterns()));
    }
	
	//Of course java couldn't have a setTabSize() for JTextPane, it would be too easy.
	public void setTabs(final JTextPane textPane, int charactersPerTab) {
        FontMetrics fm = textPane.getFontMetrics( textPane.getFont() );
//	          int charWidth = fm.charWidth( 'w' );
        int charWidth = fm.charWidth( ' ' );
        int tabWidth = charWidth * charactersPerTab;
//	      int tabWidth = 100;

        TabStop[] tabs = new TabStop[5];

        for (int j = 0; j < tabs.length; j++)
        {
            int tab = j + 1;
            tabs[j] = new TabStop(tab * tabWidth);
        }

        TabSet tabSet = new TabSet(tabs);
        SimpleAttributeSet attributes = new SimpleAttributeSet();
        StyleConstants.setTabSet(attributes, tabSet);
        int length = textPane.getDocument().getLength();
        textPane.getStyledDocument().setParagraphAttributes(0, length, attributes, false);
    }

 
    class WrapEditorKit extends StyledEditorKit {
        ViewFactory defaultFactory=new WrapColumnFactory();
        public ViewFactory getViewFactory() {
            return defaultFactory;
        }
 
    }
 
    class WrapColumnFactory implements ViewFactory {
        public View create(Element elem) {
            String kind = elem.getName();
            if (kind != null) {
                if (kind.equals(AbstractDocument.ContentElementName)) {
                    return new WrapLabelView(elem);
                } else if (kind.equals(AbstractDocument.ParagraphElementName)) {
                    return new ParagraphView(elem);
                } else if (kind.equals(AbstractDocument.SectionElementName)) {
                    return new BoxView(elem, View.Y_AXIS);
                } else if (kind.equals(StyleConstants.ComponentElementName)) {
                    return new ComponentView(elem);
                } else if (kind.equals(StyleConstants.IconElementName)) {
                    return new IconView(elem);
                }
            }
 
            // default to text display
            return new LabelView(elem);
        }
    }
 
    class WrapLabelView extends LabelView {
        public WrapLabelView(Element elem) {
            super(elem);
        }
 
        public float getMinimumSpan(int axis) {
            switch (axis) {
                case View.X_AXIS:
                    return 0;
                case View.Y_AXIS:
                    return super.getMinimumSpan(axis);
                default:
                    throw new IllegalArgumentException("Invalid axis: " + axis);
            }
        }
 
    }
}
