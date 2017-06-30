import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.AccessDeniedException;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.text.AbstractDocument;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;


import javax.swing.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.*;
public class UI {
	
	JFrame window = new JFrame();
	JPanel sidebar = new JPanel();
	JTabbedPane jtp = new JTabbedPane();
	Font font = new Font("Courier new", Font.PLAIN, 13);
	
	public void createAndDisplayUI() {
		
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		window = new JFrame();
		window.setTitle("BIDE");
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setSize(800, 600);
		window.setLocationRelativeTo(null);
		
		//window.add(jsp);
		window.add(jtp);
		jtp.setBorder(BorderFactory.createEmptyBorder());
		
		//Because writing sidebar.getWidth() causes bugs...
		int sidebarWidth = 350;	
		sidebar.setPreferredSize(new Dimension(sidebarWidth, window.getHeight()));
		//sidebar.setSize(new Dimension(200, window.getHeight()));
		window.add(sidebar, BorderLayout.EAST);
		JTextArea stdout = new JTextArea();
		stdout.setWrapStyleWord(true);
		PrintStream printStream = new PrintStream(new CustomOutputStream(stdout));
		System.setOut(printStream);
		System.setErr(printStream);
		stdout.setBackground(Color.ORANGE);
		stdout.setFont(font);
		stdout.setLineWrap(true);
		JScrollPane jsp2 = new JScrollPane(stdout);
		jsp2.setPreferredSize(new Dimension(sidebarWidth, 200));
		//sidebar.setLayout(new BorderLayout());
		sidebar.add(new JLabel("Console output"));
		sidebar.add(jsp2);
		window.setExtendedState(JFrame.MAXIMIZED_BOTH);
		
		JFileChooser jfc = new JFileChooser();
		jfc.setFileFilter(new FileFilter() {
			@Override
			public boolean accept(File file) {
				if (file.isDirectory()) {
					return true;
				}
				try {
					String extension = file.getPath().substring(file.getPath().lastIndexOf('.')).toLowerCase();
					if (extension.equals(".bide") || extension.matches("\\.g[12][mr]")) {
						return true;
					}
				} catch (Exception e) {
					
				}
				return false;
			}

			@Override
			public String getDescription() {
				return "Basic Casio files (.g1m, .g2m, .g1r, .g2r, .bide)";
			}
		});
		//window.setUndecorated(true);
		
		JMenuBar menuBar = new JMenuBar();
		//menuBar.setMargin(new Insets(5, 10, 5, 10));
		//menuBar.setFloatable(false);
		ToolbarButton open = new ToolbarButton("openFile.png");
		open.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				jfc.setSelectedFile(new File(BIDE.pathToG1M));
				
				File input = null; 
				if (jfc.showOpenDialog(window) == JFileChooser.APPROVE_OPTION) {
					input = jfc.getSelectedFile();
				}
				
			    if (input != null) {
			    	BIDE.pathToG1M = input.getPath();
			    	new Thread(new Runnable() {
				    	public void run() {

						    ArrayList<String> progs = new ArrayList<String>();
						    try {
						    	progs = BIDE.readFromG1M(BIDE.pathToG1M);
						    	BIDE.pathToSavedG1M = "";
						    	jtp.removeAll();
					    		for (int i = 0; i < progs.size(); i++) {
					    			jtp.addTab(progs.get(i).substring(15, progs.get(i).indexOf("\n")), new Program(progs.get(i)));
					    			jtp.setTabComponentAt(i, new ButtonTabComponent(jtp));
					    		}
					    		for (int i = 0; i < jtp.getTabCount(); i++) {
					    			((CustomDocumentFilter)((AbstractDocument)((Program)jtp.getComponentAt(i)).textPane.getDocument()).getDocumentFilter()).testForLag();
							    }
						    } catch (NullPointerException e) {
						    } catch (NoSuchFileException e) {
						    	BIDE.error("The file at \"" + BIDE.pathToG1M + "\" does not exist.");
						    } catch (AccessDeniedException e) {
						    	BIDE.error("BIDE is denied access to the file at \"" + BIDE.pathToG1M + "\"");
						    } catch (IOException e) {
								e.printStackTrace();
							}
						    
				    	}
				    }).start();
				    try {
				    	getTextPane().setCaretPosition(0);
				    } catch (NullPointerException e) {}
			    }
			}
		});
		ToolbarButton save = new ToolbarButton("saveFile.png");
		save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (BIDE.pathToSavedG1M.isEmpty()) {
					BIDE.pathToSavedG1M = BIDE.pathToG1M.substring(0, BIDE.pathToG1M.lastIndexOf(".")) + ".bide";
				}
				new Thread(new Runnable() {
			    	public void run() {
			    		
			    		jfc.setSelectedFile(new File(BIDE.pathToSavedG1M));
			    		File input = null;
						if (jfc.showSaveDialog(window) == JFileChooser.APPROVE_OPTION) {
							input = jfc.getSelectedFile();
						}
						
						if (input != null) {
							try {
								BIDE.writeToG1M(input.getPath());
							} catch (NullPointerException e) {
								
							} catch (NoSuchFileException e) {
						    	BIDE.error("The file at \"" + BIDE.pathToSavedG1M + "\" does not exist.");
						    } catch (AccessDeniedException e) {
						    	BIDE.error("BIDE is denied access to the file at \"" + BIDE.pathToSavedG1M + "\"");
						    } catch (IOException e) {
								e.printStackTrace();
							}
						}
			    	}
				}).start();
			}
		});
		
		ToolbarButton newProg = new ToolbarButton("newProg.png");
		newProg.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent arg0) {
				createNewTab(BIDE.TYPE_PROG);
			}
		});
		
		ToolbarButton newPict = new ToolbarButton("newPict.png");
		newPict.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent arg0) {
				createNewTab(BIDE.TYPE_PICT);
			}
		});
		
		ToolbarButton newCapt = new ToolbarButton("newCapt.png");
		newCapt.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent arg0) {
				createNewTab(BIDE.TYPE_CAPT);
			}
		});
		
		menuBar.add(open);
		menuBar.add(save);
		menuBar.add(newProg);
		menuBar.add(newPict);
		menuBar.add(newCapt);
		menuBar.setPreferredSize(new Dimension(100, 25));
		//menuBar.add(save);
		window.add(menuBar, BorderLayout.NORTH);
		

		window.setVisible(true);
		//Because window.repaint() does not work...
		//Java pls
		//window.setSize(window.getWidth()+1, window.getHeight()+1);
		//window.setSize(window.getWidth()-1, window.getHeight()-1);
		
		System.out.println("Finished initializing UI");
	}
	public FixedJTextPane getTextPane() {
		return ((Program)this.jtp.getSelectedComponent()).textPane;
	}
	
	public void createNewTab(int type) {
		String content = "";
		String name = "";
		if (type == BIDE.TYPE_PROG) {
			name = JOptionPane.showInputDialog(BIDE.ui.window, "Program name:", "New program", JOptionPane.QUESTION_MESSAGE);
			content = "#Program name: "+name+"\n#Password: <no password>\n";
		} else if (type == BIDE.TYPE_PICT) {
			name = "PICT"+JOptionPane.showInputDialog(BIDE.ui.window, "Picture number:", "New picture", JOptionPane.QUESTION_MESSAGE);
			content = "#Picture name: "+name+"\n";
		} else if (type == BIDE.TYPE_CAPT) {
			name = "CAPT"+JOptionPane.showInputDialog(BIDE.ui.window, "Capture number:", "New capture", JOptionPane.QUESTION_MESSAGE);
			content = "#Capture name: "+name+"\n";
		}
		jtp.addTab(name, new Program(content));
		jtp.setTabComponentAt(jtp.getTabCount()-1, new ButtonTabComponent(jtp));
		((CustomDocumentFilter)((AbstractDocument)((Program)jtp.getComponentAt(jtp.getTabCount()-1)).textPane.getDocument()).getDocumentFilter()).testForLag();
	}
	
}

class ToolbarButton extends JButton {
	public ToolbarButton(String iconName) {
		super();
		try {
			this.setIcon(new ImageIcon(ImageIO.read(BIDE.class.getClass().getResourceAsStream("/images/"+iconName))));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		this.setBorder(BorderFactory.createEmptyBorder(2, 5, 0, 0));
		this.setContentAreaFilled(false);
		this.setFocusPainted(false);
	}
}

class ButtonTabComponent extends JPanel {
    private final JTabbedPane pane;

    public ButtonTabComponent(final JTabbedPane pane) {
        //unset default FlowLayout' gaps
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));
        if (pane == null) {
            throw new NullPointerException("TabbedPane is null");
        }
        this.pane = pane;
        setOpaque(false);
        
        //make JLabel read titles from JTabbedPane
        JLabel label = new JLabel() {
            public String getText() {
                int i = pane.indexOfTabComponent(ButtonTabComponent.this);
                if (i != -1) {
                    return pane.getTitleAt(i);
                }
                return null;
            }
        };

        add(label);
        //add more space between the label and the button
        //label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        //tab button
        JButton button = new TabButton();
        add(button);
        //add more space to the top of the component
        //setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
    }

    private class TabButton extends JButton implements ActionListener {
        public TabButton() {
            int size = 17;
            setPreferredSize(new Dimension(size, size));
            //setToolTipText("close this tab");
            //Make the button looks the same for all Laf's
            setUI(new BasicButtonUI());
            //Make it transparent
            setContentAreaFilled(false);
            //No need to be focusable
            setFocusable(false);
            //setBorder(BorderFactory.createEtchedBorder());
            setBorderPainted(false);
            setRolloverEnabled(true);
            //Close the proper tab by clicking the button
            addActionListener(this);
        }

        public void actionPerformed(ActionEvent e) {
            int i = pane.indexOfTabComponent(ButtonTabComponent.this);
            if (i != -1) {
            	int option = JOptionPane.showConfirmDialog(BIDE.ui.window, "Are you sure to close this tab?", "BIDE", JOptionPane.YES_NO_OPTION);
                if (option == JOptionPane.YES_OPTION) {
                	pane.remove(i);
                }
            }
        }

        //we don't want to update UI for this button
        public void updateUI() {
        }

        //paint the cross
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            //shift the image for pressed buttons
            /*if (getModel().isPressed()) {
                g2.translate(1, 1);
            }*/
            g2.setStroke(new BasicStroke(2));
            g2.setColor(Color.GRAY);
            if (getModel().isRollover()) {
                g2.setColor(Color.BLACK);
            }
            int delta = 5;
            g2.drawLine(delta, delta, getWidth() - delta - 1, getHeight() - delta - 1);
            g2.drawLine(getWidth() - delta - 1, delta, delta, getHeight() - delta - 1);
            g2.dispose();
        }
    }
}