import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
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
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;

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
				return "Basic Casio files (.g[12][mr], .bide)";
			}
		});
		//window.setUndecorated(true);
		
		JMenuBar menuBar = new JMenuBar();
		//menuBar.setMargin(new Insets(5, 10, 5, 10));
		//menuBar.setFloatable(false);
		JButton open = null;
		try {
			open = new JButton(new ImageIcon(ImageIO.read(BIDE.class.getClass().getResourceAsStream("/images/openFile.png"))));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		open.setBorder(BorderFactory.createEmptyBorder(2, 5, 0, 5));
		open.setContentAreaFilled(false);
		open.setFocusPainted(false);
		/*open.addMouseListener(new MouseListener() {
			
			@Override
			public void mousePressed(MouseEvent arg0) {

				
			}
			@Override
			public void mouseEntered(MouseEvent arg0) {
			}
			@Override
			public void mouseExited(MouseEvent arg0) {
			}
			@Override
			public void mouseClicked(MouseEvent arg0) {
			}
			@Override
			public void mouseReleased(MouseEvent arg0) {
			}
		});*/
		
		open.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				jfc.setSelectedFile(new File(BIDE.pathToG1M));
				
				File input = null; 
				if (jfc.showOpenDialog(window) == JFileChooser.APPROVE_OPTION) {
					input = jfc.getSelectedFile();
				}
				//Object input = JOptionPane.showInputDialog(window, "Entrez le chemin du fichier .g1m : ", "BIDE", JOptionPane.PLAIN_MESSAGE, null, null, BIDE.pathToG1M);
				
			    if (input != null) {
			    	BIDE.pathToG1M = input.getPath();
			    	new Thread(new Runnable() {
				    	public void run() {

						    ArrayList<String> progs = new ArrayList<String>();
						    try {
						    	progs = BIDE.readFromG1M(BIDE.pathToG1M);
						    	BIDE.pathToSavedG1M = "";
						    } catch (NullPointerException e) {
						    	e.printStackTrace();
						    } catch (NoSuchFileException e) {
						    	BIDE.error("The file at \"" + BIDE.pathToG1M + "\" does not exist.");
						    } catch (AccessDeniedException e) {
						    	BIDE.error("BIDE is denied access to the file at \"" + BIDE.pathToG1M + "\"");
						    } catch (IOException e) {
								e.printStackTrace();
							}
						    jtp.removeAll();
				    		for (int i = 0; i < progs.size(); i++) {
				    			jtp.addTab(progs.get(i).substring(15, progs.get(i).indexOf("\n")), new Program(progs.get(i)));
				    			//Update textpanes for the syntax coloration to be correct
				    			((Program)jtp.getComponentAt(i)).textPane.setText(((Program)jtp.getComponentAt(i)).textPane.getText());
				    		}
				    	}
				    }).start();
				    getTextPane().setCaretPosition(0);
			    }
			}
		});
		
		JButton save = null;
		try {
			save = new JButton(new ImageIcon(ImageIO.read(BIDE.class.getClass().getResourceAsStream("/images/saveFile.png"))));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		save.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
		save.setContentAreaFilled(false);
		save.setFocusPainted(false);
		save.addActionListener(new ActionListener() {
			/*@Override public void menuCanceled(MenuEvent arg0) {}
			@Override public void menuDeselected(MenuEvent arg0) {}

			@Override
			public void menuSelected(MenuEvent arg0) {
				
			}*/
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (BIDE.pathToSavedG1M.isEmpty()) {
					BIDE.pathToSavedG1M = BIDE.pathToG1M.substring(0, BIDE.pathToG1M.length()-3) + "bide";
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
		
		menuBar.add(open);
		menuBar.add(save);
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
	
}