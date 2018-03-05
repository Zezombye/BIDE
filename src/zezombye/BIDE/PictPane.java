package zezombye.BIDE;

import java.awt.BorderLayout;
import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class PictPane extends JPanel {
	
	int type, size;
	
	JPanel namePanel = new JPanel();
	JTextField namejtf = new JTextField();
	JPanel sizePanel = new JPanel();
	JTextField sizejtf = new JTextField();
	
	public PictPane(int type, int size) {
		this.setLayout(null); //Layouts are pure evil, absolute is at least consistent
		
		this.add(namePanel);
		namePanel.add(new JLabel("Name: "));
		namePanel.add(new JTextField("PICT10"));
		JPanel sizePanel = new JPanel();
		this.add(sizePanel, BorderLayout.WEST);
		sizePanel.add(new JLabel("Size: 0x"));
		sizePanel.add(new JTextField("800"));
		this.type = type;
		this.size = size;
	}
	
	@Override protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		namePanel.setBounds(40, 100, namePanel.getPreferredSize().width, namePanel.getPreferredSize().height);
	}
	
}
