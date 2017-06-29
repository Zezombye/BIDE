import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class FixedInputDialog extends JDialog {
	
	public String showInputDialog(Window parent, String message, String title, String defaultInputValue) {
		
		JDialog dialog = new JDialog(parent, title, Dialog.DEFAULT_MODALITY_TYPE);
		dialog.setLocationRelativeTo(parent);
		dialog.setLayout(new FlowLayout());
		JLabel msg = new JLabel(message);
		dialog.add(msg, FlowLayout.LEFT);
		JTextField jtf = new JTextField(defaultInputValue);
		jtf.setSize(100, 25);
		dialog.add(jtf);
		dialog.setTitle(title);
		dialog.pack();
		dialog.setVisible(true);
		return "";
	}
	
}
