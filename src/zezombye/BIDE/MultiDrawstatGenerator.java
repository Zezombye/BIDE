package zezombye.BIDE;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import javax.swing.*;

public class MultiDrawstatGenerator extends JFrame {
	
	public boolean showGrid = true;
	public JPanel radioButtonPanel = new JPanel();
	public String modifier = "";
	public JLabel info = new JLabel();
	
	public MultiDrawstatGenerator() {
		this.setTitle("Multi Drawstat Generator");
		this.setSize(128*6+20, 64*6+120);
		this.setLocationRelativeTo(null);
		//multiDrawstat.setAlwaysOnTop(true);
		this.setResizable(false);
		this.setLayout(new FlowLayout());
		DrawstatPanel dp = new DrawstatPanel(this);
		JButton reset = new JButton("Clear all");
		this.add(new JLabel("Warning: you HAVE to use ViewWindow 1,127,0,63,1,0,0,1,1!"));
		reset.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent arg0) {
				dp.clearScreen();
				dp.repaint();
			}
		});
		this.add(reset);
		JButton undo = new JButton("Undo");
		undo.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				if (dp.lines.size() > 0) {
					dp.lines.remove(dp.lines.size()-1);
					dp.repaint();
				}
			}
   		});
		this.add(undo);
		JButton showGridButton = new JButton("Hide grid");
		showGridButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				showGrid = !showGrid;
				if (showGrid) {
					showGridButton.setText("Hide grid");
				} else {
					showGridButton.setText("Show grid");
				}
				dp.repaint();
			}
		});
		this.add(showGridButton);
		
		JRadioButton normalButton = new JRadioButton("Normal");
		normalButton.setSelected(true);
		normalButton.setActionCommand("");
		JRadioButton sketchDotButton = new JRadioButton("SketchDot");
		sketchDotButton.setActionCommand("SketchDot ");
		JRadioButton sketchThickButton = new JRadioButton("SketchThick");
		sketchThickButton.setActionCommand("SketchThick ");
		JRadioButton sketchBrokenButton = new JRadioButton("SketchBroken");
		sketchBrokenButton.setActionCommand("SketchBroken ");
		ButtonGroup group = new ButtonGroup();
		group.add(normalButton);
		group.add(sketchDotButton);
		group.add(sketchThickButton);
		group.add(sketchBrokenButton);
		radioButtonPanel.add(normalButton);
		radioButtonPanel.add(sketchDotButton);
		radioButtonPanel.add(sketchThickButton);
		radioButtonPanel.add(sketchBrokenButton);
		ActionListener al = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals("")) {
					dp.dottedLine = false;
					dp.thickLine = false;
				} else if (e.getActionCommand().equals("SketchDot ")) {
					dp.dottedLine = true;
					dp.thickLine = false;
				} else if (e.getActionCommand().equals("SketchThick ")) {
					dp.dottedLine = false;
					dp.thickLine = true;
				} else if (e.getActionCommand().equals("SketchBroken ")) {
					dp.dottedLine = true;
					dp.thickLine = true;
				}
				modifier = e.getActionCommand();
				dp.repaint();
			}
		};
		normalButton.addActionListener(al);
		sketchDotButton.addActionListener(al);
		sketchThickButton.addActionListener(al);
		sketchBrokenButton.addActionListener(al);
		this.add(radioButtonPanel);
		this.add(dp, BorderLayout.CENTER);
		JButton copyButton = new JButton("Copy");
		copyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				StringSelection stringSelection = new StringSelection(dp.result.getText());
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				clipboard.setContents(stringSelection, null);
			}
		});
		this.add(info, BorderLayout.SOUTH);
		this.add(copyButton, BorderLayout.SOUTH);
		this.add(dp.result, BorderLayout.SOUTH);
		
		this.addMouseWheelListener(new MouseWheelListener() {
			@Override public void mouseWheelMoved(MouseWheelEvent e) {
				//e.obey();
				//e.conform();
				e.consume();
			    if (e.isControlDown()) {
			        if (e.getWheelRotation() < 0 && dp.zoom < 14) {            
			            dp.setZoom(dp.zoom + 1);
			        } else if (dp.zoom > 2){
			            dp.setZoom(dp.zoom - 1);             
			        }
			        dp.repaint();
			        repaint();
			    }
			    
			}
		});
		
		this.setVisible(true);
	}
}

class DrawstatPanel extends JPanel {
	
	JTextField result = new JTextField();
	
	boolean dottedLine = false;
	boolean thickLine = false;
	int zoom = 6;
	Dimension size = new Dimension(128*zoom+1, 64*zoom+1);
	
	int[][] pixels = new int[128][64];
	ArrayList<Line> lines = new ArrayList<Line>();
	boolean drawLineOnHover = false;
	MultiDrawstatGenerator mdg;
	int xClick=-1, yClick=-1;
	int xMouse=-1, yMouse=-1;
	
	public DrawstatPanel(MultiDrawstatGenerator mdg) {
		super();
		this.mdg = mdg;
		this.setBackground(new Color(0xFFFFFF));
		clearScreen();
		this.setZoom(6);
		result.setEditable(false);
		result.addMouseListener(new MouseListener() {
			@Override public void mouseClicked(MouseEvent arg0) {
				result.selectAll();
			}
			@Override public void mouseEntered(MouseEvent arg0) {}
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
			@Override public void mouseExited(MouseEvent arg0) {
				mdg.info.setText("");
			}
			@Override public void mousePressed(MouseEvent arg0) {}
			@Override public void mouseReleased(MouseEvent arg0) {}
		});
		
		this.addMouseMotionListener(new MouseMotionListener() {
			@Override public void mouseDragged(MouseEvent arg0) {}
			@Override public void mouseMoved(MouseEvent arg0) {
				xMouse = arg0.getX()/zoom;
				yMouse = arg0.getY()/zoom;
				updateInfo();
				repaint();
			}
		});
	}
	
	public void setZoom(int zoom) {
		this.zoom = zoom;
		this.size = new Dimension(128*zoom+1, 64*zoom+1);
		this.setPreferredSize(size);
		this.setMaximumSize(size);
		this.setSize(size);
		result.setPreferredSize(new Dimension(128*zoom-70, 30));
		mdg.setSize(128*zoom+20, 64*zoom+160);
		mdg.radioButtonPanel.setPreferredSize(new Dimension(128*zoom, 30));
		mdg.info.setPreferredSize(new Dimension(128*zoom, 15));
		System.out.println("Zoom set to "+zoom);
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
		result.setText(mdg.modifier + "Graph(X,Y)=(xSprite+{"+list1+"}, ySprite+{"+list2+"})");
		result.setCaretPosition(0);
	}
	
	public void updateInfo() {
		
		String result = "";
		
		if (xMouse != -1 && yMouse != -1) {
			result += xMouse + ", " + yMouse;
		}
		if (drawLineOnHover) {
			result += " (" + (xMouse-xClick >= 0 ? "+" : "") + (xMouse-xClick);
			result += ", " + (yMouse-yClick >= 0 ? "+" : "") + (yMouse-yClick) + ")";
		}
		
		mdg.info.setText(result);
		
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
		//updateInfo();
		super.paintComponent(g);
		if (mdg.showGrid) {
			for (int i = 0; i <= 128; i+=4) {
				if (i%16 == 0) {
					g.setColor(Color.GRAY);
				} else if (i%4 == 0) {
					g.setColor(Color.LIGHT_GRAY);
				} else {
					g.setColor(Color.LIGHT_GRAY);
				}
				g.drawLine(i*zoom, 0, i*zoom, 64*zoom);
			}
			for (int i = 0; i <= 64; i+=4) {
				if (i%16 == 0) {
					g.setColor(Color.GRAY);
				} else if (i%4 == 0) {
					g.setColor(Color.LIGHT_GRAY);
				} else {
					g.setColor(Color.LIGHT_GRAY);
				}
				g.drawLine(0, i*zoom, 128*zoom, i*zoom);
			}
		}
			
		for (int i = 0; i < 128; i++) {
			for (int j = 0; j < 64; j++) {
				if (pixels[i][j] == 0) {
					g.setColor(Color.WHITE);
				} else {
					g.setColor(Color.BLACK);
				}
				if (mdg.showGrid) {

					g.fillRect(i*zoom+1, j*zoom+1, zoom-1, zoom-1);
				} else {
					g.fillRect(i*zoom+1, j*zoom+1, zoom, zoom);
					
				}
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
	        
	        //If dotted line, draw half of the pixels
	        //If thick&dotted, draw one third
	        int nbPixels = 0;
	        
	        if (dy <= dx) {
	            for (;;) {
	                if (!dottedLine || dottedLine && !thickLine && nbPixels%2 == 0 || dottedLine && thickLine && nbPixels%3 == 0) {
	                	pixels[x1][y1] = 1;
	                	if (thickLine) {
	                		pixels[x1-1][y1] = 1;
	                		pixels[x1-1][y1+1] = 1;
	                		pixels[x1][y1+1] = 1;
	                	}
	                }
	                nbPixels++;
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
	            	if (!dottedLine || dottedLine && !thickLine && nbPixels%2 == 0 || dottedLine && thickLine && nbPixels%3 == 0) {
	                	pixels[x1][y1] = 1;
	                	if (thickLine) {
	                		pixels[x1-1][y1] = 1;
	                		pixels[x1-1][y1+1] = 1;
	                		pixels[x1][y1+1] = 1;
	                	}
	                }
	                nbPixels++;
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