package zezombye.BIDE;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class CharPicker extends JPanel {

	public JTabbedPane jtp = new JTabbedPane();
	public final int nbCats = 7;
	public BufferedImage[] cats = new BufferedImage[nbCats];
	public String[] catTitles = new String[] {
			"Math", "Symbols", "ABΓ", "αβγ", "abc", "Graph 90+E", "Others"
	};
	public static String[][] catSymbols = new String[][] {
		{
			"89",  "99",  "A9",  "B9",  "A8",  "2A",  "2F",  "3D",  "11",  "<",   ">",   "10",  "12",  "87",  "E5BE","E5BF","E6B0","E6B1","E5A3",
			"E5A4","7F50","E5B0","F",   "D0",  "7F53","E542","86",  "E551","E54F","E6BB","E6B7","E6B8","E6B9","E5C0","E5C1","8B",  "E5C3","E5C4",
			"E5C5","E5C6","E5C7","E5C8","E5C9","9B",  "E5CB","E5CC","E5CD","E5CE","E5CF","E5D0","E5D1","E5D2","E5D3","E5D4","E5D5","E5D6","E5D7",
			"E5D8","E5D9","E5DA","E5DB","E5DC","E5DD","E5DE","E5DF","C2",  "C3",  "CB",  "CC",  "7FC7","7F54","8C",  "9C",  "AC",  "BC","E6BD",
			"E6BE","E6BF","E6C0","E6C1","E6C2","E6C3","E6C4","E6C5","E6C6","E6C7","E6C8","E6C9","E6CA","E6CB","E6D6","E6CC","E6CD","E6CE","E6CF",
			"E6D0","E6D1","E6D2","E6D3","E6D4","E6D5","E6B2","E6B3","E6B4","E6B5","E6BC","E6B6","E6D7","E6D8","E6D9","E6DA","E6DB","E6DC","E6DD",
			"E6DE"
		},{
			"!",   "22",  "#",   "$",   "%",   "26",  "27",  "28",  "29",  "2C",  "2E",  "3A",  ";",   "3F",  "@",   "5B",  "5C",  "5D",  "_",
			"`",   "7B",  "|",   "7D",  "7E",  "13",  "B5",  "BB",  "E594","E595","E596","E597","E598","E590","E591","E592","E593","E599","E59A",
			"E59B","E59C","E59D","E59E","E5A1","E59F","E5A2","E5A0","E5A5","E5A6","E5A7","E5B5","E5B6","E5B8","E5B9","E5BA","E5BB","E5BC","E690",
			"E",   "E692","E693","E694","E695","E696","E697","E698","E699","E69A","E69B","E69C","E69D","E69E","E69F","E6A0","E6A1","E6A2","E6A3",
			"E6A4","E6A5","E6A6","E6A7","E6A8","E6A9","E6AA"
		},{
			"E540","E541","E542","E543","E544","E545","E546","E547","E548","E549","E54A","E54B","E54C","E54D","E54E","E54F","E550","E551","E553",
			"E554","E555","E556","E557","E558","E501","E502","E503","E504","E505","E506","E507","E508","E509","E50A","E50B","E50C","E50D","E50E",
			"E50F","E510","E511","E512","E513","E514","E515","E516","E517","E518","E519","E51A","E51B","E51C","E51D","E51E","E520","E521","E522",
			"E523","E524","E525","E526","E527","E528","E529","E52A","E52B","E52C","E52D","E52E","E52F","E530","E531","E532","E533","E534","E535",
			"E560","E561","E562","E563","E564","E565","E566","E567","E568","E569","E56A","E56B","E56C","E56D","E56E","E56F","E570","E571","E572",
			"E573","E574","E575","E576","E577","E578","E579","E57A","E57B","E57C","E57D","E57E","E580","E581","E582"
		},{
			"E640","E641","E642","E643","E644","E645","E646","E647","E648","E649","E64A","E64B","E64C","E64D","E64E","E64F","E650","E651","E652",
			"E653","E654","E655","E656","E657","E658","E601","E602","E603","E604","E605","E606","E607","E608","E609","E60A","E60B","E60C","E60D",
			"E60E","E60F","E610","E611","E612","E613","E614","E615","E616","E617","E618","E619","E61A","E61B","E61C","E61D","E61E","E61F","E620",
			"E621","E622","E623","E624","E625","E626","E627","E628","E629","E62A","E62B","E62C","E62D","E62E","E62F","E630","E631","E632","E633",
			"E634","E635","E660","E661","E662","E663","E664","E665","E666","E667","E668","E669","E66A","E66B","E66C","E66D","E66E","E66F","E670",
			"E671","E672","E673","E674","E675","E676","E677","E678","E679","E67A","E67B","E67C","E67D","E67E","E680","E681","E682"
		},{
			"E741","E742","E743","E744","E745","E746","E747","E748","E749","E74A","E74B","E74C","E74D","E74E","E74F","E750","E751","E752","E753",
			"E754","E755","E756","E757","E758","E759","E75A","E761","E762","E763","E764","E765","E766","E767","E768","E769","E76A","E76B","E76C",
			"E76D","E76E","E76F","E770","E771","E772","E773","E774","E775","E776","E777","E778","E779","E77A"
		},{
			"E5AC","E5AB","E5E8","E5E9","E5EA","E5EB","E5EC","E5ED","E5EE","E5EF","E5AF","E5AE","E5AD","E5E0","E5E1","E5E2","E5E3","E5F0","E5F1",
			"E5F2","E5F3","E5F4","E5F5","E5FB","E5FC","E5FD","E6AE","90"
		},{
			"3",   "CD",  "7",   "8",   "9",   "A",   "B",   null,  null,  null,  null,  null,  null,  null,  null,  null,  null,  null,  null,
			"1A",  "1B",  "98",  "1D",  "1E",  "1F",  "88",  "7FF4","7FF0","7FF1",null,  null,  null,  null,  null,  null,  null,  null,  null,
			"D8",  "E5BD","E5B7","C"
		}
	};
	
	public CharPicker() {
		
		try {
			for (int i = 0; i < nbCats; i++) {
				cats[i] = ImageIO.read(BIDE.class.getClass().getResourceAsStream("/images/charPicker/cat"+(i+1)+".png"));
				jtp.addTab(catTitles[i], new CharPanel(cats[i], i));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		this.add(jtp);
		
	}
	
}

class CharPanel extends JPanel {
	
	int nb;
	BufferedImage image;
	int xMouse = -1, yMouse = -1;
	
	public CharPanel(BufferedImage image, int nb) {
		super();
		this.image = image;
		this.nb = nb;
		this.setPreferredSize(new Dimension(345, 169));
		//this.setBorder(BorderFactory.createBevelBorder(1, Color.RED, Color.RED));
		
		this.addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseDragged(MouseEvent arg0) {
			}

			@Override
			public void mouseMoved(MouseEvent arg0) {
				xMouse = arg0.getX();
				yMouse = arg0.getY();
				repaint();
			}
		});
		
		this.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent arg0) {
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
			}

			@Override
			public void mouseExited(MouseEvent arg0) {
				xMouse = -1;
				yMouse = -1;
				repaint();
			}

			@Override
			public void mousePressed(MouseEvent arg0) {
				//System.out.println(arg0.getX() + " " + arg0.getY());
				int gridX = arg0.getX()/18;
				int gridY = arg0.getY()/24;
				//System.out.println(CharPicker.catSymbols[nb][gridX+19*gridY]);
				try {
					String hex = CharPicker.catSymbols[nb][gridX+19*gridY];
					String insert = "";
					try {
						Integer.parseInt(hex, 16);
						for (int i = 0; i < BIDE.opcodes.size(); i++) {
							if (BIDE.opcodes.get(i).hex.equalsIgnoreCase(hex)) {
								
								if (BIDE.options.getProperty("allowUnicode").equals("false") || BIDE.opcodes.get(i).unicode == null) {
									insert = BIDE.opcodes.get(i).text;
								} else {
									insert = BIDE.opcodes.get(i).unicode;
								}
								break;
							}
						}
					} catch (NumberFormatException e) {
						insert = hex;
					}
					
					if (insert.isEmpty()) {
						System.out.println("No opcode found!");
					} else {
						ProgramTextPane comp = ((ProgramTextPane)((ProgScrollPane)BIDE.ui.jtp.getSelectedComponent()).getViewport().getView());
						comp.insert(insert, comp.getCaretPosition());
					}
				} catch (ArrayIndexOutOfBoundsException e) {
					
				} catch (NullPointerException e) {
					
				} catch (ClassCastException e) {
					
				}
				//find opcode with that hex
				
				
			}

			@Override
			public void mouseReleased(MouseEvent arg0) {
			}
			
		});
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g.create();
        g2d.drawImage(image, 0, 1, this);
        if (xMouse != -1 && yMouse != -1) {
            int gridX = xMouse/18;
    		int gridY = yMouse/24;
    		try {
    			if (CharPicker.catSymbols[nb][gridX+19*gridY] != null) {
    				g2d.setColor(Color.RED);
    				g2d.drawRect(gridX*18, gridY*24, 18, 24);
    			}
    		} catch (ArrayIndexOutOfBoundsException e) {}
	            
        }
        
        g2d.dispose();
	}
	
	
}