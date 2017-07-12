package zezombye.BIDE;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.util.Arrays;

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

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

public class Program extends RTextScrollPane {
	
	public ProgramTextPane textPane = null;
	public String name = "";
	public String option = "";
	public String content = "";
	public int type = 0;
	
	//Warning: do not use name, option or content to retrieve informations because they may have changed!
	//For example, if the user modifies the content to modify the name, that change will not be reflected in name (nor in content).
	
	
	
	public Program(String name, String option, String content, int type) {
		this(name, option, content, type, new ProgramTextPane(type));
	}
	
	public Program(String name, String option, String content, int type, ProgramTextPane ptp) {
		super(ptp);
		this.name = name;
		this.option = option;
		this.textPane = ptp;
		this.type = type;
		//this.setViewportView(textPane);
		//this.setViewportView(new RSyntaxTextArea());
		this.getVerticalScrollBar().setUnitIncrement(30);
		this.setBorder(BorderFactory.createEmptyBorder());
		//Check if content is missing headers
		if (!content.startsWith("#")) {
			if (type == BIDE.TYPE_PROG) {
				content = "#Program name: "+name+"\n#Password: "+option+"\n"+content;
			} else if (type == BIDE.TYPE_PICT) {
				content = "#Picture name: "+name+"\n#Size: 0x"+option+BIDE.pictTutorial+content;
			} else if (type == BIDE.TYPE_CAPT) {
				content = "#Capture name: "+name+"\n"+BIDE.pictTutorial+content;
			}
		}
		this.content = content;
		
		textPane.setText(content);
	}
}
