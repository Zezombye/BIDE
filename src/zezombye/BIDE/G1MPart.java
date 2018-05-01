package zezombye.BIDE;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

public class G1MPart {
	
	public CasioString binaryContent = null;
	public boolean isEditedSinceLastSave = true;
	public JComponent comp;
	public String name = "";
	public String option = "";
	public Object content = "";
	public int type = 0;
	
	//Warning: do not use name, option or content to retrieve informations because they may have changed!
	//For example, if the user modifies the content to modify the name, that change will not be reflected in name (nor in content).
	public G1MPart(String name, String option, Object content, int type) {
		this.name = name;
		this.option = option;
		this.type = type;
		//this.setViewportView(textPane);
		//this.setViewportView(new RSyntaxTextArea());
		//Check if content is missing headers
		if (type != BIDE.TYPE_PICT && type != BIDE.TYPE_CAPT && !((String)content).startsWith("#")) {
			if (type == BIDE.TYPE_PROG) {
				content = "#Program name: "+name+"\n#Password: "+option+"\n"+content;
			} /*else if (type == BIDE.TYPE_PICT) {
				content = "#Picture name: "+name+"\n#Size: 0x"+option+BIDE.pictTutorial+content;
			} else if (type == BIDE.TYPE_CAPT) {
				content = "#Capture name: "+name+"\n"+BIDE.pictTutorial+content;
			}*/
		}
		this.content = content;
		
		if (!BIDE.isCLI) {
			
			if (type == BIDE.TYPE_PICT || type == BIDE.TYPE_CAPT) {
				this.comp = new Picture(type, name, Integer.valueOf(option, 16), (Byte[])content).jsp;
			} else {
				ProgramTextPane textPane = new ProgramTextPane(type);
				textPane.setText((String)content);
				textPane.getDocument().addDocumentListener(new DocumentListener() {

					@Override
					public void changedUpdate(DocumentEvent arg0) {
						isEditedSinceLastSave = true;
					}

					@Override
					public void insertUpdate(DocumentEvent arg0) {
						isEditedSinceLastSave = true;
					}

					@Override
					public void removeUpdate(DocumentEvent arg0) {
						isEditedSinceLastSave = true;
					}
					
				});
				this.comp = new ProgScrollPane(textPane, type);
				((JScrollPane)comp).getVerticalScrollBar().setUnitIncrement(30);
				comp.setBorder(BorderFactory.createEmptyBorder());
			}
		}
	}
}

class ProgScrollPane extends RTextScrollPane {
	
	ProgramTextPane textPane;
	int type;
	
	public ProgScrollPane(ProgramTextPane textPane, int type) {
		super(textPane);
		this.textPane = textPane;
		this.type = type;
		/*if (type == BIDE.TYPE_CAPT || type == BIDE.TYPE_PICT) {
			this.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		}*/
	}
}
