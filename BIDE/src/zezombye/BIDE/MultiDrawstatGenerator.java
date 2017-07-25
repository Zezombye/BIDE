package zezombye.BIDE;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import javax.swing.*;

public class MultiDrawstatGenerator extends JFrame {
	
	public MultiDrawstatGenerator() {
		JFrame multiDrawstat = new JFrame("drawstat");
		multiDrawstat.setSize(128*6+20, 500);
		multiDrawstat.setLocationRelativeTo(null);
		multiDrawstat.setAlwaysOnTop(true);
		multiDrawstat.setResizable(false);
		multiDrawstat.setLayout(new FlowLayout());
		DrawstatPanel dp = new DrawstatPanel();
		JButton reset = new JButton("Reset all");
		reset.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent arg0) {
				dp.clearScreen();
				dp.repaint();
			}
		});
		multiDrawstat.add(reset);
		JButton undo = new JButton("Undo");
		undo.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				if (dp.lines.size() > 0) {
					dp.lines.remove(dp.lines.size()-1);
					dp.repaint();
				}
			}
   		});
		multiDrawstat.add(dp, BorderLayout.CENTER);
		multiDrawstat.add(dp.result, BorderLayout.SOUTH);
		multiDrawstat.setVisible(true);
	}
}

class DrawstatPanel extends JPanel {
	
	JTextField result = new JTextField();
	
	int zoom = 6;
	Dimension size = new Dimension(128*zoom+1, 64*zoom+1);
	
	int[][] pixels = new int[128][64];
	ArrayList<Line> lines = new ArrayList<Line>();
	boolean drawLineOnHover = false;
	
	int xClick=-1, yClick=-1;
	int xMouse=-1, yMouse=-1;
	
	public DrawstatPanel() {
		super();
		this.setPreferredSize(size);
		this.setMaximumSize(size);
		this.setSize(size);
		this.setBackground(new Color(0xFFFFFF));
		clearScreen();
		//lines.add(new Line(0, 0, 1, 2));
		lines.add(new Line(127, 63, 126, 61));
		result.setText("test");
		result.setEditable(false);
		result.setPreferredSize(new Dimension(128*zoom, 30));
		result.addMouseListener(new MouseListener() {
			@Override public void mouseClicked(MouseEvent arg0) {
				result.selectAll();
			}
			@Override public void mouseEntered(MouseEvent arg0) { }
			@Override public void mouseExited(MouseEvent arg0) {}
			@Override public void mousePressed(MouseEvent arg0) {}
			@Override public void mouseReleased(MouseEvent arg0) {}
		});
		
		this.addMouseListener(new MouseListener() {
			@Override public void mouseClicked(MouseEvent arg0) {
				if (!drawLineOnHover) {
					xClick = arg0.getX()/zoom;
					yClick = arg0.getY()/zoom;
				} else {
					lines.add(new Line(xClick, yClick, arg0.getX()/zoom, arg0.getY()/zoom));
					xClick = -1;
					yClick = -1;
				}
				drawScreen();
				drawLineOnHover = !drawLineOnHover;
				repaint();
			}
			@Override public void mouseEntered(MouseEvent arg0) {}
			@Override public void mouseExited(MouseEvent arg0) {}
			@Override public void mousePressed(MouseEvent arg0) {}
			@Override public void mouseReleased(MouseEvent arg0) {}
		});
		
		this.addMouseMotionListener(new MouseMotionListener() {
			@Override public void mouseDragged(MouseEvent arg0) {}
			@Override public void mouseMoved(MouseEvent arg0) {
				xMouse = arg0.getX()/zoom;
				yMouse = arg0.getY()/zoom;
				repaint();
			}
		});
	}
	
	public void clearScreen() {
		lines.clear();
		drawScreen();
	}
	
	public void drawScreen() {
		for (int i = 0; i < 128; i++) {
			for (int j = 0; j < 64; j++) {
				pixels[i][j] = 0;
			}
		}
		if (xClick >= 0 && yClick >= 0) {
			pixels[xClick][yClick] = 1;
		}
		for (int i = 0; i < lines.size(); i++) {
			drawLine(lines.get(i).x0, lines.get(i).y0, lines.get(i).x1, lines.get(i).y1);
		}
		try {
			drawLine(xClick, yClick, xMouse, yMouse);
		} catch (ArrayIndexOutOfBoundsException e) {}
	}
	
	public void updateResult() {
		String list1 = "";
		String list2 = "";
		
		//get minimum x and y
		int minX = 127, minY = 63;
		for (int i = 0; i < lines.size(); i++) {
			if (lines.get(i).x0 < minX) minX = lines.get(i).x0;
			if (lines.get(i).x1 < minX) minX = lines.get(i).x1;
			if (lines.get(i).y0 < minY) minY = lines.get(i).y0;
			if (lines.get(i).y1 < minY) minY = lines.get(i).y1;
		}
		
		for (int i = 0; i < lines.size(); i++) {
			list1 += getOptimizedCoord(lines.get(i).x0-minX, lines.get(i).x1-minX);
			list2 += getOptimizedCoord(lines.get(i).y0-minY, lines.get(i).y1-minY);
			if (i < lines.size()-1) {
				list1 += ", ";
				list2 += ", ";
			}
		}
		result.setText("Graph(X,Y)=(xSprite+{"+list1+"}, ySprite+{"+list2+"})");
		result.setCaretPosition(0);
	}
	
	//Returns an optimized string. For example, "2+0T"->"2", "3-1T"->"3-T".
	public String getOptimizedCoord(int start, int end) {
		String result = "";
		if (start != 0) {
			result += start;
		}
		int delta = end-start;
		if (delta != 0) {
			if (delta > 0 && start != 0) {
				result += "+";
			}
			if (delta < 0) {
				result += "-";
			}
			if (delta != 1 && delta != -1) {
				result += Math.abs(delta);
			}
			result += "T";
		}
		if (result.isEmpty()) {
			result = "0";
		}
		return result;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		drawScreen();
		updateResult();
		super.paintComponent(g);
		g.setColor(Color.GRAY);
		for (int i = 0; i <= 128; i++) {
			g.drawLine(i*zoom, 0, i*zoom, 64*zoom);
		}
		for (int i = 0; i <= 64; i++) {
			g.drawLine(0, i*zoom, 128*zoom, i*zoom);
		}
		for (int i = 0; i < 128; i++) {
			for (int j = 0; j < 64; j++) {
				if (pixels[i][j] == 0) {
					g.setColor(Color.WHITE);
				} else {
					g.setColor(Color.BLACK);
				}
				g.fillRect(i*zoom+1, j*zoom+1, zoom-1, zoom-1);
			}
		}
	}
	
	public void drawLine(int x1, int y1, int x2, int y2) {
		try {
			// delta of exact value and rounded value of the dependant variable
	        int d = 0;
	        int dy = Math.abs(y2 - y1);
	        int dx = Math.abs(x2 - x1);

	        int dy2 = (dy << 1); // slope scaling factors to avoid floating
	        int dx2 = (dx << 1); // point
	 
	        int ix = x1 < x2 ? 1 : -1; // increment direction
	        int iy = y1 < y2 ? 1 : -1;
	 
	        if (dy <= dx) {
	            for (;;) {
	                pixels[x1][y1] = 1;
	                if (x1 == x2)
	                    break;
	                x1 += ix;
	                d += dy2;
	                if (d > dx) {
	                    y1 += iy;
	                    d -= dx2;
	                }
	            }
	        } else {
	            for (;;) {
	            	pixels[x1][y1] = 1;
	                if (y1 == y2)
	                    break;
	                y1 += iy;
	                d += dx2;
	                if (d > dy) {
	                    x1 += ix;
	                    d -= dy2;
	                }
	            }
	        }
		} catch (ArrayIndexOutOfBoundsException e) {
			
		}
        
	}
}

class Line {
	int x0, y0, x1, y1;
	
	public Line(int x0, int y0, int x1, int y1) {
		this.x0 = x0;
		this.y0 = y0;
		this.x1 = x1;
		this.y1 = y1;
	}
}