package zezombye.BIDE;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.text.AbstractDocument;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;

import org.fife.ui.autocomplete.AutoCompleteDescWindow;

import javax.swing.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.*;
public class UI {
	
	JFrame window = new JFrame();
	JPanel sidebar = new JPanel();
	JTabbedPane jtp;
	JFileChooser jfc;
	JTextArea stdout = new JTextArea();
	PrintStream printStream;
	
	public void createAndDisplayUI() {
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//Set default font
		/*java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
		FontUIResource fontUI = new FontUIResource(BIDE.progFont);
	    while (keys.hasMoreElements())
	    {
	        Object key = keys.nextElement();
	        Object value = UIManager.get(key);
	        if (value instanceof FontUIResource)
	        {
	        	System.out.println(key);
	            UIManager.put(key, BIDE.progFont);
	        }
	    }*/

	    //UIManager.put("Label.font", new Font(BIDE.options.getProperty("progFontName"), Font.PLAIN, 12));
	    //UIManager.put("List.font", new Font(BIDE.options.getProperty("progFontName"), Font.PLAIN, 12));
	    
		jtp = new JTabbedPane() {
			@Override public void addTab(String name, Component comp) {
				super.addTab(name, comp);
				this.setTabComponentAt(jtp.getTabCount()-1, new ButtonTabComponent(jtp));
				//this.getTabComponentAt(jtp.getTabCount()-1).setFont(BIDE.progFont); //doesn't work
			}
		};
		//Is overridden by the label font at ButtonTabComponent
		//jtp.setFont(new Font(BIDE.options.getProperty("progFontName"), Font.PLAIN, 12));
		jfc = new JFileChooser();
		
		window = new JFrame();
		window.setTitle("BIDE v"+BIDE.VERSION+" by Zezombye");
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setSize(1200, 800);
		window.setLocationRelativeTo(null);
		try {
			window.setIconImage(ImageIO.read(BIDE.class.getClass().getResourceAsStream("/images/BIDEicon.png")));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		//window.add(jsp);
		window.add(jtp);
		//jtp.setBorder(BorderFactory.createEmptyBorder());
		
		//Because writing sidebar.getWidth() causes bugs...
		int sidebarWidth = 350;	
		sidebar.setPreferredSize(new Dimension(sidebarWidth, window.getHeight()));
		//sidebar.setSize(new Dimension(200, window.getHeight()));
		window.add(sidebar, BorderLayout.EAST);
		stdout.setWrapStyleWord(true);
		printStream = new PrintStream(new CustomOutputStream(stdout));
		if (!BIDE.debug) {
			System.setOut(printStream);
			System.setErr(printStream);
		}
		stdout.setBackground(Color.ORANGE);
		stdout.setFont(BIDE.dispFont);
		stdout.setLineWrap(true);
		JScrollPane jsp2 = new JScrollPane(stdout);
		jsp2.setPreferredSize(new Dimension(sidebarWidth, 200));
		//sidebar.setLayout(new BorderLayout());
		sidebar.add(new JLabel("Console output"));
		sidebar.add(jsp2);
		if (!BIDE.debug) window.setExtendedState(JFrame.MAXIMIZED_BOTH);
		
		jfc.setFileFilter(new FileFilter() {
			@Override
			public boolean accept(File file) {
				if (file.isDirectory()) {
					return true;
				}
				try {
					String extension = file.getPath().substring(file.getPath().lastIndexOf('.')).toLowerCase();
					if (extension.equals(".bide") || extension.matches("\\.g[123][mr]")) {
						return true;
					}
				} catch (Exception e) {}
				return false;
			}

			@Override
			public String getDescription() {
				return "Basic Casio files (.g1m, .g2m, .g1r, .g2r, .g3m, .bide)";
			}
		});
		//window.setUndecorated(true);
		
		JMenuBar menuBar = new JMenuBar();
		//menuBar.setMargin(new Insets(5, 10, 5, 10));
		//menuBar.setFloatable(false);
		ToolbarButton open = new ToolbarButton("openFile.png", "Open file");
		open.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				openFile();
			}
		});
		ToolbarButton save = new ToolbarButton("saveFile.png", "Save file");
		save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				saveFile();
			}
		});
		
		ToolbarButton newProg = new ToolbarButton("newProg.png", "New Basic Casio program");
		newProg.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent arg0) {
				createNewTab(BIDE.TYPE_PROG);
			}
		});
		
		/*newProg.addMouseListener(new java.awt.event.MouseAdapter() {
		    public void mouseEntered(java.awt.event.MouseEvent evt) {
		        System.out.println(Arrays.toString(Window.getWindows()));
		    }

		    public void mouseExited(java.awt.event.MouseEvent evt) {
		    }
		});*/
		
		ToolbarButton newPict = new ToolbarButton("newPict.png", "New Picture");
		newPict.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent arg0) {
				createNewTab(BIDE.TYPE_PICT);
			}
		});
		
		ToolbarButton newCapt = new ToolbarButton("newCapt.png", "New Capture");
		newCapt.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent arg0) {
				createNewTab(BIDE.TYPE_CAPT);
			}
		});
		
		/*ToolbarButton dispOpcodes = new ToolbarButton("opcodes.png", "Show opcodes");
		dispOpcodes.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent arg0) {
				createNewTab(BIDE.TYPE_OPCODE);
			}
		});*/
		
		menuBar.add(open);
		menuBar.add(save);
		menuBar.add(newProg);
		menuBar.add(newPict);
		menuBar.add(newCapt);
		//menuBar.add(dispOpcodes);
		
		menuBar.setPreferredSize(new Dimension(100, 25));
		//menuBar.add(save);
		window.add(menuBar, BorderLayout.NORTH);
		
		JMenuBar menuBar2 = new JMenuBar();
		JMenu openMenu = new JMenu("Open");
		menuBar2.add(openMenu);
		JMenuItem importFile = new JMenuItem("Open Basic Casio file");
		importFile.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				openFile();
			}
		});
		openMenu.add(importFile);
		JMenu saveMenu = new JMenu("Save");
		menuBar2.add(saveMenu);
		JMenuItem saveg1m = new JMenuItem("Save to g1m");
		saveg1m.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				try {
					BIDE.pathToSavedG1M = BIDE.pathToSavedG1M.substring(0, BIDE.pathToSavedG1M.lastIndexOf("."))+".g1m";
				} catch (Exception e1) {}
				saveFile();
			}
		});
		saveMenu.add(saveg1m);
		JMenuItem saveTxt = new JMenuItem("Save to .bide file");
		saveTxt.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				try {
					BIDE.pathToSavedG1M = BIDE.pathToSavedG1M.substring(0, BIDE.pathToSavedG1M.lastIndexOf("."))+".bide";
				} catch (Exception e1) {}
				saveFile();
			}
		});
		saveMenu.add(saveTxt);
		JMenu toolsMenu = new JMenu("Tools");
		menuBar2.add(toolsMenu);
		JMenuItem multiDrawstat = new JMenuItem("Multi Drawstat Generator");
		multiDrawstat.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent arg0) {
				new MultiDrawstatGenerator();
			}
		});
		toolsMenu.add(multiDrawstat);
		JMenuItem imgToPict = new JMenuItem("Convert image to picture");
		imgToPict.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent arg0) {
				importPict();
			}
		});
		toolsMenu.add(imgToPict);
		JMenuItem showOptions = new JMenuItem("Show/Edit options");
		showOptions.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent arg0) {
				try {
					Desktop.getDesktop().open(new File(BIDE.pathToOptions));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		toolsMenu.add(showOptions);
		JMenuItem showOpcodes = new JMenuItem("Show list of opcodes");
		showOpcodes.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent arg0) {
				createNewTab(BIDE.TYPE_OPCODE);
			}
		});
		toolsMenu.add(showOpcodes);
		JMenuItem showChars = new JMenuItem("Show characters list");
		showChars.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent arg0) {
				createNewTab(BIDE.TYPE_CHARLIST);
			}
		});
		toolsMenu.add(showChars);
		JMenuItem showColoration = new JMenuItem("Show syntax coloration test");
		showColoration.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent arg0) {
				createNewTab(BIDE.TYPE_COLORATION);
			}
		});
		toolsMenu.add(showColoration);
		JMenuItem takeEmuScreenshot = new JMenuItem("Take emulator screenshot");
		takeEmuScreenshot.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent arg0) {
				BIDE.autoImport.storeEmuScreenshot();
			}
		});
		toolsMenu.add(takeEmuScreenshot);
		JMenuItem takeEmuScreenScreenshot = new JMenuItem("Take emulator screen screenshot");
		takeEmuScreenScreenshot.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent arg0) {
				BIDE.autoImport.storeEmuScreen();
			}
		});
		toolsMenu.add(takeEmuScreenScreenshot);
		JMenuItem benchmark = new JMenuItem("Run benchmark");
		benchmark.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent arg0) {
				new Thread(new Runnable() {
					public void run() {
						BIDE.autoImport.benchmark();
					}
				}).start();
			}
		});
		toolsMenu.add(benchmark);
		window.setJMenuBar(menuBar2);

		window.setVisible(true);
		//Because window.repaint() does not work...
		//Java pls
		//window.setSize(window.getWidth()+1, window.getHeight()+1);
		//window.setSize(window.getWidth()-1, window.getHeight()-1);
		
	}
	public ProgramTextPane getTextPane() {
		try {
			return ((ProgScrollPane)this.jtp.getSelectedComponent()).textPane;
		} catch (ClassCastException e) {
			return null;
		}
	}
	
	public void createNewTab(int type) {
		
		if (type == BIDE.TYPE_CAPT || type == BIDE.TYPE_PICT) {
			String name;
			String size;
			if (type == BIDE.TYPE_PICT) {
				name = "PICT"+JOptionPane.showInputDialog(BIDE.ui.window, "Picture number:", "New picture", JOptionPane.QUESTION_MESSAGE);
				size = "800";
			} else {
				name = "CAPT"+JOptionPane.showInputDialog(BIDE.ui.window, "Capture number:", "New capture", JOptionPane.QUESTION_MESSAGE);
				size = "400";
			}
			if (name.endsWith("null")) return;
			
			BIDE.g1mparts.add(new G1MPart(name, size, new Byte[0], type));
			jtp.addTab(name, BIDE.g1mparts.get(BIDE.g1mparts.size()-1).comp);
			
		} else {
			String content = "";
			String option = "";
			String name = "";
			if (type == BIDE.TYPE_PROG) {
				name = JOptionPane.showInputDialog(BIDE.ui.window, "Program name:", "New program", JOptionPane.QUESTION_MESSAGE);
				option = "<no password>";
			} else if (type == BIDE.TYPE_OPCODE) {
				name = "Opcodes List";
				content = "#\n#DO NOT EDIT THIS TAB, changes won't be saved!\n#\n";
				try {
					BufferedReader reader = new BufferedReader(new InputStreamReader(BIDE.class.getClass().getResourceAsStream("/opcodes.txt"), "UTF-8"));
					String line = null;
				    StringBuilder stringBuilder = new StringBuilder();

				    try {
				        while((line = reader.readLine()) != null) {
				            stringBuilder.append(line);
				            stringBuilder.append("\n");
				        }

				        content += stringBuilder.toString();
				    } catch (IOException e) {
						e.printStackTrace();
					} finally {
				        try {
							reader.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
				    }
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			
			} else if (type == BIDE.TYPE_CHARLIST) {
				name = "All characters";
				try {
					BufferedReader reader = new BufferedReader(new InputStreamReader(BIDE.class.getClass().getResourceAsStream("/characters.txt"), "UTF-8"));
					String line = null;
				    StringBuilder stringBuilder = new StringBuilder();

				    try {
				        while((line = reader.readLine()) != null) {
				            stringBuilder.append(line);
				            stringBuilder.append("\n");
				        }

				        content += stringBuilder.toString();
				    } catch (IOException e) {
						e.printStackTrace();
					} finally {
				        try {
							reader.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
				    }
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			
			} else if (type == BIDE.TYPE_COLORATION) {
				name = "Syntax coloration test";
				try {
					BufferedReader reader = new BufferedReader(new InputStreamReader(BIDE.class.getClass().getResourceAsStream("/testColoration.txt"), "UTF-8"));
					String line = null;
				    StringBuilder stringBuilder = new StringBuilder();

				    try {
				        while((line = reader.readLine()) != null) {
				            stringBuilder.append(line);
				            stringBuilder.append("\n");
				        }

				        content += stringBuilder.toString();
				    } catch (IOException e) {
						e.printStackTrace();
					} finally {
				        try {
							reader.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
				    }
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
				}
			
			} else {
				BIDE.error("Unknown type "+type);
			}
			if (name == null || name.endsWith("null")) {
				return;
			}
			BIDE.g1mparts.add(new G1MPart(name, option, content, type));
			jtp.addTab(name, BIDE.g1mparts.get(BIDE.g1mparts.size()-1).comp);
		}
		
		
		//jtp.setTabComponentAt(jtp.getTabCount()-1, new ButtonTabComponent(jtp));
		//((CustomDocumentFilter)((AbstractDocument)((Program)jtp.getComponentAt(jtp.getTabCount()-1)).textPane.getDocument()).getDocumentFilter()).testForLag();
		jtp.setSelectedIndex(jtp.getTabCount()-1);
	    try {
	    	getTextPane().setCaretPosition(0);
	    } catch (NullPointerException e) {
	    	//if (BIDE.debug) e.printStackTrace();
	    }
	}
	
	public void openFile() {
    	jfc.setCurrentDirectory(new File(BIDE.pathToG1M));
		
		File input = null; 
		if (jfc.showOpenDialog(window) == JFileChooser.APPROVE_OPTION) {
			input = jfc.getSelectedFile();
		}
		
	    if (input != null) {
	    	BIDE.pathToG1M = input.getPath();
	    	BIDE.pathToSavedG1M = "";
	    	new Thread(new Runnable() {
		    	public void run() {
		    		BIDE.g1mparts = new ArrayList<G1MPart>();
				    try {
				    	G1MParser g1mparser = new G1MParser(BIDE.pathToG1M);
						g1mparser.readG1M();
						
						if (!g1mparser.checkValidity()) {
							BIDE.readFromTxt(BIDE.pathToG1M);
				    	} else {
				    		BIDE.readFromG1M(BIDE.pathToG1M);
				    	}
				    	BIDE.pathToSavedG1M = "";
				    	jtp.removeAll();
			    		for (int i = 0; i < BIDE.g1mparts.size(); i++) {
			    			jtp.addTab(BIDE.g1mparts.get(i).name, BIDE.g1mparts.get(i).comp);
			    			//jtp.setTabComponentAt(i, new ButtonTabComponent(jtp));
			    			try {
								Thread.sleep(100);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
			    		}
				    } catch (NullPointerException e) {
				    	if (BIDE.debug) e.printStackTrace();
				    } catch (NoSuchFileException e) {
				    	BIDE.error("The file at \"" + BIDE.pathToG1M + "\" does not exist.");
				    } catch (AccessDeniedException e) {
				    	BIDE.error("BIDE is denied access to the file at \"" + BIDE.pathToG1M + "\"");
				    } catch (IOException e) {
						e.printStackTrace();
					}
				    if (BIDE.g1mparts.size() != 0) {
					    System.out.println("Finished loading g1m");
				    	
				    }
				    
		    	}
		    }).start();
	    	
		    try {
		    	getTextPane().setCaretPosition(0);
		    } catch (NullPointerException e) {
		    	if (BIDE.debug) e.printStackTrace();
		    }
	    }
    }
	
	public void saveFile() {
		
		if (BIDE.pathToSavedG1M.isEmpty()) {
			BIDE.pathToSavedG1M = BIDE.pathToG1M;
		}
		new Thread(new Runnable() {
	    	public void run() {
	    		
	    		jfc.setSelectedFile(new File(BIDE.pathToSavedG1M));
	    		File input = null;
				if (jfc.showSaveDialog(window) == JFileChooser.APPROVE_OPTION) {
					input = jfc.getSelectedFile();
				}
				
				if (input != null) {
					BIDE.pathToSavedG1M = input.getAbsolutePath();
					try {
						if (BIDE.pathToSavedG1M.endsWith(".bide") || BIDE.pathToSavedG1M.endsWith(".txt")) {
							BIDE.writeToTxt(input.getPath());
						} else {
							BIDE.writeToG1M(input.getPath());
						}
					} catch (NullPointerException e) {
						if (BIDE.debug) e.printStackTrace();
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
	
	public void importPict() {
		JFileChooser jfc = new JFileChooser();
		jfc.setFileFilter(new FileFilter() {
			@Override
			public boolean accept(File file) {
				if (file.isDirectory()) {
					return true;
				}
				try {
					String extension = file.getPath().substring(file.getPath().lastIndexOf('.')).toLowerCase();
					if (extension.equals(".png") || extension.equals(".bmp")) {
						return true;
					}
				} catch (Exception e) {}
				return false;
			}

			@Override
			public String getDescription() {
				return "Image files (.png, .bmp)";
			}
		});
		
		File input = null; 
		if (jfc.showOpenDialog(window) == JFileChooser.APPROVE_OPTION) {
			input = jfc.getSelectedFile();
		}
				
	    if (input != null) {
	    	try {
				BufferedImage img = ImageIO.read(input);
				if (img.getHeight() != 64 || img.getWidth() != 128) {
					BIDE.error("Image must be 128*64!");
					return;
				}
				Byte[] binary = new Byte[0x400];
				Arrays.fill(binary, (byte)0);
				for (int j = 0; j < 64; j++) {
					for (int i = 0; i < 128; i++) {
						if (img.getRGB(i, j) == Color.BLACK.getRGB()) {
							binary[i/8+16*j] = (byte)(binary[i/8+16*j] | (0b10000000 >> (i%8)));
						} /*else {
							binary += "1";
							//System.out.println(img.getRGB(i, j));
						}*/
					}
				}
				//System.out.println(binary);
				String imgName = input.getName().substring(0, input.getName().lastIndexOf('.'));
				this.jtp.addTab(imgName, new G1MPart(imgName, "800", binary, BIDE.TYPE_PICT).comp);
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
	}
		
}

class ToolbarButton extends JButton {
	public ToolbarButton(String iconName, String toolTip) {
		super();
		try {
			this.setIcon(new ImageIcon(ImageIO.read(BIDE.class.getClass().getResourceAsStream("/images/"+iconName))));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		this.setBorder(BorderFactory.createEmptyBorder(2, 5, 0, 0));
		this.setContentAreaFilled(false);
		this.setFocusPainted(false);
		this.setToolTipText(toolTip);
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
        label.setFont(new Font(BIDE.options.getProperty("progFontName"), Font.PLAIN, 12));

        add(label);
        //add more space between the label and the button
        //label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        //tab button
        add(Box.createRigidArea(new Dimension(8,17)));
        JButton button = new TabButton();
        add(button);
        //add more space to the top of the component
        //setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
    }

    private class TabButton extends JButton implements ActionListener {
        public TabButton() {
            //int size = 17;
            setPreferredSize(new Dimension(9, 17));
            //setToolTipText("close this tab");
            //Make the button looks the same for all Laf's
            setUI(new BasicButtonUI());
            //Make it transparent
            setContentAreaFilled(false);
            //No need to be focusable
            setFocusable(false);
            //setBorder(BorderFactory.createEtchedBorder());
            setBorderPainted(true);
            setRolloverEnabled(true);
            //Close the proper tab by clicking the button
            addActionListener(this);
            //this.setBorder(BorderFactory.createBevelBorder(1, Color.RED, Color.GREEN));
        }

        public void actionPerformed(ActionEvent e) {
            int i = pane.indexOfTabComponent(ButtonTabComponent.this);
            if (i != -1) {
            	int option = JOptionPane.showConfirmDialog(BIDE.ui.window, "Are you sure you want to close this tab?", "BIDE", JOptionPane.YES_NO_OPTION);
                if (option == JOptionPane.YES_OPTION) {
                	pane.remove(i);
                	BIDE.g1mparts.remove(i);
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
            g2.setStroke(new BasicStroke(1));
            
          
            g2.setColor(Color.GRAY);
            if (getModel().isRollover()) {
                g2.setColor(Color.BLACK);
            }
            int lmargin = 0;
            int rmargin = 2;
            int umargin = 4;
            int dmargin = 6;
            
            g2.drawLine(lmargin, umargin, getWidth()-rmargin+1, getHeight()-dmargin+1);
            g2.drawLine(lmargin+1, umargin, getWidth()-rmargin+1, getHeight()-dmargin);
            g2.drawLine(lmargin, umargin+1, getWidth()-rmargin, getHeight()-dmargin+1);

            g2.drawLine(lmargin, getHeight()-dmargin+1, getWidth()-rmargin+1, umargin);
            g2.drawLine(lmargin+1, getHeight()-dmargin+1, getWidth()-rmargin+1, umargin+1);
            g2.drawLine(lmargin, getHeight()-dmargin, getWidth()-rmargin, umargin);
            g2.dispose();
        }
    }
    
    
}