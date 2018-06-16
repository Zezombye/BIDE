package zezombye.BIDE;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.sun.xml.internal.ws.util.StringUtils;

/*
 * TODO:
 * add find and replace
 */

public class BIDE {
	
	public static List<Opcode> opcodes = new ArrayList<Opcode>();
	public static List<Macro> macros = new ArrayList<Macro>();
	public static List<Macro> defaultMacros = new ArrayList<Macro>();
	public static List<G1MPart> g1mparts = new ArrayList<G1MPart>();
	
	public static String pathToG1M = System.getProperty("user.home")+"/desktop/";
	public static String pathToSavedG1M = "";
	public static final String pathToOptions = System.getProperty("user.home")+"/BIDE/options.txt";
	//public static final String pathToTmp = System.getProperty("user.home")+"/BIDE/tmp.g1m";
	public static UI ui = new UI();
	
	public static String runOn = "none";
	
	public final static String VERSION = "4.2";
	
	public final static int TYPE_PROG = 0;
	public final static int TYPE_PICT = 3;
	public final static int TYPE_CAPT = 4;
	public final static int TYPE_OPCODE = 5;
	public final static int TYPE_COLORATION = 6;
	public final static int TYPE_CHARLIST = 7;
	public static boolean debug = false;
	//public static Font progFont = new Font("DejaVu Sans Mono", Font.PLAIN, 12);
	public static Font progFont, dispFont;
	//public static Font dispFont = new Font("DejaVu Sans Mono", Font.PLAIN, 13);
	 
	public final static String pictTutorial = 
			"\n'To edit the picture, use the characters ' , :\n"
			+ "'which make ▀ ▄ █ respectively.\n"
			+ "'Make sure not to edit the border!\n";
	public final static String pictWarning = 
			"\n'\n'DO NOT EDIT THE PICTURE BELOW, unless you are an advanced user!\n'\n";
	
	static Options options = new Options();
	public static boolean isCLI = false;
	
	public static EmulatorImport autoImport;
	public static String[] args;
	
	public static void main(String[] args2) {
		args = args2;
		
		
		if (args.length > 0 && args[0].equals("debug")) {
			debug = true;
			System.out.println("Debug activated");
			args = new String[0];
		}
		
		options.loadProperties();
		
		//progFont = progFont.deriveFont(30);
		//System.out.println(progFont.getSize());
		//System.setProperty("awt.useSystemAAFontSettings","none");
		//System.setProperty("swing.aatext", "false");
		
		//options.initProperties();
		getOpcodes();
		initMacros();
		
		//System.out.println(BIDE.class.getClass().getResource("/").toString());

		System.out.println("args : "+Arrays.toString(args));
		if (args.length > 0 && (args[0].equals("--to-g1m") || args[0].equals("--to-txt") || args[0].equals("--help") || args[0].equals("-help"))) {
			//CLI
			isCLI = true;
			if (args[0].equals("--help") || args[0].equals("-help")) {
				
				System.out.println("Syntax:\n"
						+ "--to-g1m <target> <file1> (<file2> <...>) : creates a g1m file out of the contents of the provided file(s)\n"
						+ "--to-txt <target> <file1> (<file2> <...>) : creates a txt file out of the contents of the provided file(s)\n"
						//+ "--run-on-emulator <file> : runs the provided file on the Manager PLUS emulator\n"
						//+ "--run-on-calculator <file> : runs the provided file on the calculator (requires the BIDE Add-in)\n"
						+ "<file1> (<file2> <...>) : opens the provided file(s) in GUI");
				
			} else if (args[0].equals("--to-g1m")) {
				String pathToG1M = args[1];
				for (int i = 2; i < args.length; i++) {
					try {
						G1MParser g1mparser = new G1MParser(args[i]);
						g1mparser.readG1M();
						
						if (!g1mparser.checkValidity()) {
							BIDE.readFromTxt(args[i]);
				    	} else {
				    		BIDE.readFromG1M(args[i]);
				    	}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				try {
					writeToG1M(pathToG1M);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if (args[0].equals("--to-txt")) {
				String pathToDest = args[1];
				/*if (!pathToDest.endsWith(System.getProperty("file.separator"))) {
					pathToDest += System.getProperty("file.separator");
				}*/
				try {
					for (int i = 2; i < args.length; i++) {
						G1MParser g1mparser = new G1MParser(args[i]);
						g1mparser.readG1M();
						
						if (!g1mparser.checkValidity()) {
							BIDE.readFromTxt(args[i]);
				    	} else {
				    		BIDE.readFromG1M(args[i]);
				    	}
					}
					
					/*for (int i = 0; i < g1mparts.size(); i++) {
						//Disallowed chars: <>:"/\|?*
						g1mparts.get(i).name = g1mparts.get(i).name.replaceAll("<|>|:|\"|\\/|\\\\|\\||\\?|\\*", "_");
					}*/
					writeToTxt(pathToDest);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			System.exit(0);
			
		} else {
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			try {
				ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, BIDE.class.getClass().getResourceAsStream("/Casio Graph.ttf")));
				ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, BIDE.class.getClass().getResourceAsStream("/DejaVuAvecCasio.ttf")));
			} catch (FontFormatException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			progFont = new Font(options.getProperty("progFontName"), Font.TRUETYPE_FONT, Integer.parseInt(options.getProperty("progFontSize")));
			dispFont = progFont;
			if (options.getProperty("runOn").equals("emulator")) {
				autoImport = new EmulatorImport();
			}
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					ui.createAndDisplayUI();
					
					ProgramTextPane.initAutoComplete();
					
					//ui.jtp.addTab("test", new Program("test1", "800", "testcontent", TYPE_PICT).comp);
					
					//ui.createNewTab(TYPE_COLORATION);
					//ui.createNewTab(TYPE_PICT);
					//ui.jtp.addTab("testPict", new Picture(BIDE.TYPE_PICT, "PICT10", 0x400, new Byte[] {(byte)0b10001011, 0b00100101, 0b01001010, 0b00010011}).jsp);
					//((ProgScrollPane)ui.jtp.getComponentAt(0)).textPane.setText("testcontent");
					
					//new AutoImport().autoImport("C:\\Users\\Catherine\\Desktop\\PUISS4.g1m");
					System.out.println("Finished initialization");
					if (!debug) checkForNewVersion();
					
					//Open eventual files provided as arguments
					
					if (args.length > 0) {
						File[] files = new File[args.length];
						for (int i = 0; i < args.length; i++) {
							files[i] = new File(args[i]);
						}
						ui.openFile(false, files);
					}
				}
			});
			
			
			
		}
		
		
		
	}
	
	public static void readFromTxt(String txtPath) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(txtPath));
		String fileContent = new String(encoded, "UTF-8");
		fileContent = fileContent.replaceAll("\r\n", "\n");
		String[] progsTxt = fileContent.split("\\n#End of part\\n");
		for (int i = 0; i < progsTxt.length; i++) {
			int type;
			if (progsTxt[i].startsWith("#Program")) {
				type = TYPE_PROG;
			} else if (progsTxt[i].startsWith("#Picture")) {
				type = TYPE_PICT;
			} else if (progsTxt[i].startsWith("#Capture")) {
				type = TYPE_CAPT;
			} else {
				BIDE.error("Could not find the type of part "+i+" at "+txtPath);
				return;
			}
			
			//By chance, "Program", "Picture" and "Capture" have the same number of letters.
			String name = progsTxt[i].substring(15, progsTxt[i].indexOf('\n'));
			
			System.out.println("Parsing part \""+name+"\"");
			//Get 2nd line, which is option
			String option = "";
			try {
				//System.out.println(progsTxt[i].indexOf('\n'));
				//System.out.println(progsTxt[i].substring(progsTxt[i].indexOf('\n')+1).indexOf('\n'));
				option = progsTxt[i].substring(progsTxt[i].indexOf('\n')+1, progsTxt[i].indexOf('\n')+1 + progsTxt[i].substring(progsTxt[i].indexOf('\n')+1).indexOf('\n'));
				//System.out.println("option = " + option);
			} catch (Exception e) {
				error("Invalid option in part "+name);
				e.printStackTrace();
				return;
			}
			
			Object content = "";
			if (type == BIDE.TYPE_PROG) {
				if (option.startsWith("#Password: ")) {
					if (!option.substring(11).equals("<no password>")) {
						option = option.substring(11);
					} else {
						option = "";
					}
					content = progsTxt[i];
				}
			} else {
				if (type == BIDE.TYPE_PICT) {
					if (option.startsWith("#Size: 0x")) {
						option = option.substring(9);
						try {
							Integer.parseInt(option, 16);
						} catch (NumberFormatException e) {
							error(name, "Invalid picture size!");
						}
					} else {
						error(name, "Picture size undefined!");
					}
				} else if (type == BIDE.TYPE_CAPT) {
					option = "400";
				}
				
				content = asciiToPict(progsTxt[i], name, 0, option).getContent().toArray(new Byte[0]);
			}
				
			g1mparts.add(new G1MPart(name, option, content, type));
		}
		
	}
	
	public static void readFromG1M(String g1mpath) throws IOException {
		System.out.println("Reading from g1m at " + g1mpath);
		G1MParser g1mparser = new G1MParser(g1mpath);
		g1mparser.readG1M();
		if (!g1mparser.checkValidity()) {
			error("Invalid g1m!");
			return;
		}
		g1mparser.divideG1MIntoParts();
		for (int h = 0; h < g1mparser.parts.size(); h++) {
			
			if (g1mparser.getPartType(g1mparser.parts.get(h)) == TYPE_PROG) {
				
				String progName = casioToAscii(g1mparser.getPartName(g1mparser.parts.get(h)), false);
				String progPw = casioToAscii(g1mparser.parts.get(h).substring(44, 52), false);

				if (progPw.isEmpty()) {
					progPw = "<no password>";
				}
				System.out.println("Found program \"" + progName + "\"");
				
				String progContent = casioToAscii(g1mparser.getPartContent(g1mparser.parts.get(h)).substring(10), true);
				
				if (progName == null || progPw == null || progContent == null) {
					return;
				}
				
				g1mparts.add(new G1MPart(progName, progPw, progContent, TYPE_PROG));
			} else if (g1mparser.getPartType(g1mparser.parts.get(h)) == TYPE_PICT || g1mparser.getPartType(g1mparser.parts.get(h)) == TYPE_CAPT) {
				String name = casioToAscii(g1mparser.getPartName(g1mparser.parts.get(h)), false);
				
				CasioString content = null;
				if (g1mparser.getPartType(g1mparser.parts.get(h)) == TYPE_PICT) {
					System.out.println("Found picture \""+name+"\"");
					content = g1mparser.getPartContent(g1mparser.parts.get(h));
				} else {
					System.out.println("Found capture \""+name+"\"");
					//Captures have a width and height attribute, skip it
					content = g1mparser.getPartContent(g1mparser.parts.get(h)).substring(4, 0x404);
				}
				
				Byte[] result = content.getContent().toArray(new Byte[0]);
				//System.out.println("byte content : "+Arrays.toString(result));
				if (g1mparser.getPartType(g1mparser.parts.get(h)) == TYPE_PICT) {
					g1mparts.add(new G1MPart(name, Integer.toHexString(content.length()), result, TYPE_PICT));
				} else {
					g1mparts.add(new G1MPart(name, "400", result, TYPE_CAPT));
				}
			}
		}
		System.out.println("Loading g1m...");
	}
	
	/*public static void writeToG1M(String destPath) throws IOException {
		//Parse programs from UI
		List<Program> parts = new ArrayList<Program>();
		//Parse text file
		for (int h = 0; h < ui.jtp.getTabCount(); h++) {
			
			parts.add((Program)ui.jtp.getComponentAt(h));
			
			int type = ((ProgScrollPane)ui.jtp.getComponentAt(h)).type;
			if (type != TYPE_PROG && type != TYPE_PICT && type != TYPE_CAPT) {
				continue;
			}
			
			String[] lines = ((ProgScrollPane)ui.jtp.getComponentAt(h)).textPane.getText().split("\\n|\\r|\\r\\n");

			if (type == TYPE_PICT || type == TYPE_CAPT) {
				parts.add(new Program("", "", ))
			} else {
				if (type == TYPE_PROG && !lines[0].startsWith("#Program name: ")) {
					error("Program "+ ui.jtp.getComponentAt(h).getName() + " must include the directive \"#Program name: \" at the beginning!");
				}
				parts.add(new Program("", "", ((ProgScrollPane)ui.jtp.getComponentAt(h)).textPane.getText(), type));
			}
			
			//System.out.println("Program text="+((Program)ui.jtp.getComponentAt(h)).textPane.getText());
		}
		writeToG1M(destPath, parts);
	}*/
	
	public static void writeToG1M(String destPath) throws IOException {
		
		long time = System.currentTimeMillis();
		if (!debug) {
			BIDE.ui.stdout.setText("");
		}
		
		System.out.println("Saving to \""+destPath+"\"...");
		
		G1MWrapper g1mwrapper = new G1MWrapper();
		//Parse text file
		G1MPart currentPart = null;
		clearMacros();
		
		//ArrayList<Macro> macros = new ArrayList<Macro>();
		for (int h = 0; h < g1mparts.size(); h++) {
			int type = g1mparts.get(h).type;
			if (type != TYPE_PICT && type != TYPE_CAPT && type != TYPE_PROG) {
				continue;
			}
			
			currentPart = g1mparts.get(h);
			//currentPart.content = "";
			if (type == BIDE.TYPE_PICT || type == BIDE.TYPE_CAPT) {
				
				Picture pict = ((Picture)((JScrollPane)(g1mparts.get(h).comp)).getViewport().getView());
				
				currentPart.name = pict.namejtf.getText();
				currentPart.content = Stream.concat(Arrays.stream(pict.pictPanel.pixels), Arrays.stream(pict.pictPanel2.pixels)).toArray(Byte[]::new);
				currentPart.option = pict.sizejtf.getText();
				
				try {
					Integer.parseInt(currentPart.option, 16);
				} catch (NumberFormatException e) {
					error(currentPart.name, "Invalid picture size! ("+currentPart.option+")");
					return;
				}
				
				currentPart.content = Arrays.copyOfRange((Byte[])currentPart.content, 0, Integer.parseInt(currentPart.option, 16));
				
			} else {
				String[] lines;
				if (!isCLI) {
					lines = ((ProgramTextPane)((ProgScrollPane)g1mparts.get(h).comp).getViewport().getView()).getText().split("\\n|\\r|\\r\\n");
				} else {
					lines = ((String)g1mparts.get(h).content).split("\\n|\\r|\\r\\n");
				}
				currentPart.content = "";
				currentPart.name = lines[0].substring(15);
				
				if (type == BIDE.TYPE_PROG && lines[1].startsWith("#Password: ")) {
					if (!lines[1].substring(11).equals("<no password>")) {
						currentPart.option = lines[1].substring(11);
					} else {
						currentPart.option = "";
					}
				}
				for (int i = 0; i < lines.length; i++) {
					/*if (lines[i].endsWith("Then") || lines[i].endsWith("Else")) {
						lines[i] += " ";
					}*/
					if (lines[i].startsWith("#define ")) {
						try {
							
							String macro = lines[i].substring(8);
							String text, replacement;
							String[] args = null;
							if (macro.indexOf(")") > 0 && macro.substring(0, macro.indexOf(")")+1).matches("\\w+\\([\\w, ]+\\)")) {
								//It's a function macro
								text = macro.substring(0, macro.indexOf(")")+1);
								System.out.println("Found function macro "+text);
								
								text = text.replaceAll(" ", "");
								args = text.substring(text.indexOf("(")+1, text.indexOf(")")).split(",");
								System.out.println("args : "+Arrays.toString(args));
							} else {
								try {
									text = macro.substring(0, macro.indexOf(" "));
								} catch (StringIndexOutOfBoundsException e) {
									text = macro;
								}
								System.out.println("Found macro "+text);
							}
							//Check for empty defines
							if (macro.length() == text.length()) {
								replacement = "";
							} else {
								replacement = macro.substring(text.length()+1);
								System.out.println("replacement = "+replacement);
								if (args != null) {
									if (replacement.indexOf("%") >= 0) {
										error(currentPart.name, i+1, "Function macros cannot contain percents! Use hex escape.");
										return;
									}
									for (int j = 0; j < args.length; j++) {
										String oldReplacement = replacement;
										replacement = replacement.replaceAll("(?<!\\w)"+args[j]+"(?!\\w)", "%"+args[j]+"%");
										if (oldReplacement.equals(replacement)) {
											error(currentPart.name, i+1, "Argument "+args[j]+" is not used in replacement!");
											return;
										}
									}
								}
							}
							System.out.println("replacement = "+replacement);
							macros.add(new Macro(text, replacement, args));
						} catch (StringIndexOutOfBoundsException e) {
							error(currentPart.name, i+1, "Invalid macro declaration!");
							return;
						}
					}
					currentPart.content += lines[i] + "\n";
				}
			}
			g1mparts.set(h, currentPart);
			
		}
		Collections.sort(macros, new Comparator<Macro>() {
			@Override
			public int compare(Macro o1, Macro o2) {
				return o2.text.compareTo(o1.text);
			}
		});
		if (g1mparts.size() == 0) {
			error("No programs detected!");
			return;
		}
		if (BIDE.debug) {
			System.out.println(macros.toString());
		}
		//Add each part (program) of the ascii file
		byte[] padding = {0,0,0,0,0,0,0,0};
		for (int i = 0; i < g1mparts.size(); i++) {
			
			CasioString name = new CasioString(asciiToCasio(g1mparts.get(i).name, true, g1mparts.get(i).name+".name", 1, macros));
			CasioString part = new CasioString("");
			
			if (!g1mparts.get(i).isEditedSinceLastSaveToG1M && g1mparts.get(i).type == BIDE.TYPE_PROG) {
				System.out.println("Already parsed "+g1mparts.get(i).name);
				part.add(g1mparts.get(i).binaryContent);
			} else {
			
				System.out.println("Parsing \""+g1mparts.get(i).name+"\"");
				
				if (name.length() > 8) {
					error("Program \""+g1mparts.get(i).name+"\" has a name too long (8 characters max)!");
					return;
				}
							
				if (g1mparts.get(i).type == BIDE.TYPE_CAPT && !g1mparts.get(i).name.startsWith("CAPT")) {
					error("Capture \""+g1mparts.get(i).name+"\"'s name should start with \"CAPT\"!");
					return;
				} else if (g1mparts.get(i).type == BIDE.TYPE_PICT && !g1mparts.get(i).name.startsWith("PICT")) {
					error("Picture \""+g1mparts.get(i).name+"\"'s name should start with \"PICT\"!");
					return;
				}
				if (g1mparts.get(i).type == BIDE.TYPE_CAPT || g1mparts.get(i).type == BIDE.TYPE_PICT) {
					try {
						int nb = Integer.parseInt(g1mparts.get(i).name.substring(4));
						if (nb < 1 || nb > 20) {
							error("Number of "+g1mparts.get(i).name+" should be 1-20!");
							return;
						}
					} catch (NumberFormatException e) {
						error(g1mparts.get(i).name, "Invalid picture/capture number!");
						return;
					}
				}
				
				name.add(Arrays.copyOfRange(padding, 0, 8-name.length()));
				
				if (g1mparts.get(i).type == BIDE.TYPE_PROG) {
					CasioString password = new CasioString(asciiToCasio(g1mparts.get(i).option, true, g1mparts.get(i).name+".password", 2, macros));
					if (password.length() > 8) {
						error("Program \""+g1mparts.get(i).name+"\" has a password too long (8 characters max)!");
						return;
					}
					password.add(Arrays.copyOfRange(padding, 0, 8-password.length()));
					part.add(password);
					part.add(new byte[]{0,0});
					part.add(asciiToCasio((String)g1mparts.get(i).content, false, g1mparts.get(i).name, 1, macros));
					part.add(Arrays.copyOfRange(padding, 0, 4-part.length()%4));
				} else if (g1mparts.get(i).type == BIDE.TYPE_PICT){
					part.add((Byte[])g1mparts.get(i).content);
				} else {
					part.add(new byte[]{0x00, (byte)0x80, 0x00, 0x40});
					part.add((Byte[])g1mparts.get(i).content);
				}
				g1mparts.get(i).binaryContent = new CasioString(part);
			}
			g1mwrapper.addPart(part, name, g1mparts.get(i).type);
		}
		g1mwrapper.generateG1M(destPath);
		
		for (int i = 0; i < BIDE.g1mparts.size(); i++) {
			BIDE.g1mparts.get(i).isEditedSinceLastSaveToG1M = false;
		}
		
		System.out.println("Finished writing to g1m in "+(System.currentTimeMillis()-time)+"ms");
		
	}
	
	public static void writeToTxt(String destPath) {
		System.out.println("Saving to "+destPath+"...");
		String result = "";
		for (int i = 0; i < g1mparts.size(); i++) {
			if (g1mparts.get(i).type == BIDE.TYPE_PROG) {
				if (!BIDE.isCLI) {
					result += ((ProgScrollPane)g1mparts.get(i).comp).textPane.getText();
				} else {
					result += g1mparts.get(i).content;
				}
				result += "\n#End of part\n";
			} else if (g1mparts.get(i).type == BIDE.TYPE_PICT || g1mparts.get(i).type == BIDE.TYPE_CAPT) {
				Picture pict = ((Picture)((JScrollPane)(g1mparts.get(i).comp)).getViewport().getView());
				if (g1mparts.get(i).type == BIDE.TYPE_PICT) {
					result += "#Picture name: ";
				} else {
					result += "#Capture name: ";
				}
				result += pict.namejtf.getText() + "\n"
						+ "#Size: 0x" + pict.sizejtf.getText() + "\n"
						+ pictToAscii(pict.pictPanel.pixels);
				if (g1mparts.get(i).type != BIDE.TYPE_CAPT) {
					result += pictWarning + pictToAscii(pict.pictPanel2.pixels);
				}
				result += "\n#End of part\n";
			}
		}
		try {
			IO.writeStrToFile(new File(destPath), result, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Done!");
	}
	
	/*public static String pictToAscii(CasioString content, int type) {
		//Convert from binary to string
		StringBuilder binary = new StringBuilder();
		for (int i = 0; i < content.length(); i++) {
			int mask = 0b10000000;
			for (int j = 0; j < 8; j++) {
				binary.append(((content.charAt(i)&mask) != 0 ? "1" : "0"));
				mask >>= 1;
			}
		}
		return pictToAscii(binary.toString(), type);
	}*/
	
	public static String pictToAscii(Byte[] content) {
		StringBuilder asciiResult = new StringBuilder();
		for (int i = 0; i < 130; i++) {
			asciiResult.append("▄");
		}
		asciiResult.append("\n");
		for (int i = 0; i < 64; i+=2) {
			asciiResult.append("█");
			for (int j = 0; j < 128; j++) {
				int pixel, pixel2;
				try {
					pixel = (content[j/8+16*i] & (0b10000000 >> j%8)) != 0 ? 1 : 0;
				} catch (IndexOutOfBoundsException e) {
					pixel = 0;
				}
				try {
					pixel2 = (content[j/8+16*(i+1)] & (0b10000000 >> j%8)) != 0 ? 1 : 0;
				} catch (IndexOutOfBoundsException e) {
					pixel2 = 0;
				}
				if (pixel == 0 && pixel2 == 0) {
					asciiResult.append(" ");
				} else if (pixel == 0 && pixel2 == 1) {
					asciiResult.append("▄");
				} else if (pixel == 1 && pixel2 == 0) {
					asciiResult.append("▀");
				} else if (pixel == 1 && pixel2 == 1) {
					asciiResult.append("█");
				} else {
					System.out.println("wtf ?");
					return null;
				}
			}
			asciiResult.append("█\n");
		}
		for (int i = 0; i < 130; i++) {
			asciiResult.append("▀");
		}
		return asciiResult.toString();
	}
	
	public static CasioString asciiToPict(String content, String progName, int startLine, String pictSize) {
		//Strip border
		//Split on lines
		String[] lines = content.split("\\n|\\r|\\r\\n");
		String binary = "";
		ArrayList<Byte> bytes = new ArrayList<Byte>();
		for (int i = 0; i < lines.length; i++) {
			if (lines[i].isEmpty() || lines[i].startsWith("'") || lines[i].startsWith("#") || lines[i].startsWith("▀") || lines[i].startsWith("▄")) {
				continue;
			}
			if (lines[i].length() != 130) {
				error(progName, i+startLine, "Line length isn't 130! ("+lines[i].length()+")");
				return null;
			}
			
			if (!lines[i].startsWith("█") || !lines[i].endsWith("█")) {
				error(progName, i+startLine, "Border error");
				return null;
			} else {
				lines[i] = lines[i].substring(1, 129);
			}

			
			//Iterate twice on the line to convert to ascii binary
			for (int j = 0; j < 128; j++) {
				if (lines[i].charAt(j) == '▀' || lines[i].charAt(j) == '█') {
					binary += '1';
				} else if (lines[i].charAt(j) == '▄' || lines[i].charAt(j) == ' ') {
					binary += '0';
				} else {
					error(progName, i+startLine, "Unallowed character '"+lines[i].charAt(j)+"' in picture!");
					return null;
				}
			}
			for (int j = 0; j < 128; j++) {
				if (lines[i].charAt(j) == '▄' || lines[i].charAt(j) == '█') {
					binary += '1';
				} else {
					binary += '0';
				}
			}
		}
		
		//Convert ascii binary to bytes
		for (int i = 0; i < Integer.parseInt(pictSize, 16); i++) {
			bytes.add(new Byte((byte)Integer.parseInt(binary.substring(8*i, 8*i+8), 2)));
		}
		
		return new CasioString(bytes);
		
	}
	
	public static CasioString asciiToCasio(String content, boolean allowUnknownOpcodes, String progName, int startLine, List<Macro> macros) {
		CasioString result = new CasioString();
		
		//Optimise for less wasted space
		
		//TODO: fix bug with Else messing up line count in errors
		//content = content.replaceAll("Else ?\\n", "Else ");
		content = content.replaceAll(":Then ?\\n", "\nThen ");
		content = content.replaceAll("Then ?\\n", "Then ");
		
		String[] lines = content.split("\\n|\\r|\\r\\n");
		
		//System.out.println(Arrays.toString(lines));
		
		for (int h = 0; h < lines.length; h++) {
			if (lines[h].startsWith("#nocheck")) {
				allowUnknownOpcodes = true;
			} else if (lines[h].startsWith("#yescheck")) {
				allowUnknownOpcodes = false;
			}
			if (lines[h].startsWith("#") || lines[h].trim().isEmpty()) {
				continue;
			}
			boolean currentPosIsString = false;
			boolean escapeNextChar = false;
			boolean currentPosIsComment = false;
			
			for (int i = 0; i < lines[h].length(); i++) {
				
				boolean foundMatch = false;
				
				if (lines[h].charAt(i) == '"' && !escapeNextChar) {
					currentPosIsString = !currentPosIsString;
				}
				
				if (lines[h].charAt(i) == '\\' && !escapeNextChar) {
					escapeNextChar = true;
				} else {
					escapeNextChar = false;
				}
				
				if (lines[h].charAt(i) == '\'' && !currentPosIsString) {
					currentPosIsComment = true;
				}
				
				if (lines[h].charAt(i) == ':' && !currentPosIsString && currentPosIsComment) {
					currentPosIsComment = false;
					continue;
				}
				
				if (currentPosIsComment && BIDE.options.getProperty("optimize").equals("true")) {
					continue;
				}
				
				//hex escape
				if (lines[h].startsWith("&#", i)) {
					String hexEsc = lines[h].substring(i+2, lines[h].indexOf(";", i));
					if (hexEsc.length() != 2 && hexEsc.length() != 4) {
						error(progName, h+startLine, i+1, "Hex escape must be 2 or 4 characters long! (escape: " + hexEsc + ")");
						return null;
					}
					result.add(Integer.parseInt(hexEsc.substring(0, 2), 16));
					if (hexEsc.length() > 2) {
						result.add(Integer.parseInt(hexEsc.substring(2, 4), 16));
					}
					i += 2 + hexEsc.length();
					continue;
				}
				
				//Test for macros
				if (!allowUnknownOpcodes && !currentPosIsString && !currentPosIsComment) {
					boolean foundMacro = false;
					for (int j = 0; j < macros.size(); j++) {
						if (macros.get(j).isFunction) {
							
							if (lines[h].startsWith(macros.get(j).text.substring(0, macros.get(j).text.indexOf("(")+1), i)) {
								
								System.out.println("Found match for function macro "+macros.get(j).text);
								//Get location of closing parenthesis
								int depth = 0;
								int beginParenthesisIndex = lines[h].indexOf("(", i);
								int k = 0;
								boolean currentPosIsString2 = false;
								boolean escapeNextChar2 = false;
								ArrayList<Integer> commaPos = new ArrayList<Integer>();
								commaPos.add(beginParenthesisIndex);
								try {
									for (k = beginParenthesisIndex+1;; k++) {
										if (lines[h].charAt(k) == '"' && !escapeNextChar2) {
											currentPosIsString2 = !currentPosIsString2;
										}
										if (lines[h].charAt(k) == '\\' && !escapeNextChar2) {
											escapeNextChar2 = true;
										} else {
											escapeNextChar2 = false;
										}
										if (lines[h].charAt(k) == ',' && !currentPosIsString2) {
											commaPos.add(k);
										}
										if (lines[h].charAt(k) == '(' && !currentPosIsString2) {
											depth++;
										}
										if (lines[h].charAt(k) == ')' && !currentPosIsString2) {
											depth--;
											if (depth == -1) {
												break;
											}
										}
									}
								} catch (StringIndexOutOfBoundsException e) {
									e.printStackTrace();
									return null;
								}
								
								commaPos.add(k);
								
								int nbArgs = commaPos.size()-1;
								if (nbArgs != macros.get(j).args.length) {
									continue;
								}
								
								String[] args = new String[nbArgs];
								
								for (int l = 0; l < nbArgs; l++) {
									args[l] = lines[h].substring(commaPos.get(l)+1, commaPos.get(l+1));
								}
								
								System.out.println("args = "+Arrays.toString(args));
								String replacement = macros.get(j).replacement;
								for (int l = 0; l < nbArgs; l++) {
									replacement = replacement.replaceAll("%"+macros.get(j).args[l]+"%", args[l].replaceAll("\\\\", "\\\\\\\\"));
								}
								System.out.println("Replacement : "+replacement);
								lines[h] = lines[h].substring(0, i) + replacement + lines[h].substring(k+1);
								foundMacro = true;
								break;
								
							}
							
						} else {

							if (lines[h].startsWith(macros.get(j).text, i)) {
								lines[h] = lines[h].substring(0, i) + macros.get(j).replacement + lines[h].substring(i+macros.get(j).text.length());
								foundMacro = true;
								//System.out.println(lines[h]);
								break;
							}
						}
							
					}
					/*if (foundMacro) {
						i--;
						continue;
					}*/
				}
				
				
				for (int j = 0; j < opcodes.size(); j++) {
					
					//If string, skip opcodes that are not entities/characters in order to avoid FA-124ing
					if (allowUnknownOpcodes || currentPosIsString || currentPosIsComment) {
						if (opcodes.get(j).text.length() != 1
								&& !opcodes.get(j).text.startsWith("&")
								&& !opcodes.get(j).text.equals("->")
								&& !opcodes.get(j).text.equals("=>")
								&& !opcodes.get(j).text.equals("!=")
								&& !opcodes.get(j).text.equals(">=")
								&& !opcodes.get(j).text.equals("<=")) {
							continue;
						}
					}
					
					if (lines[h].startsWith(opcodes.get(j).text, i) || (opcodes.get(j).unicode != null && lines[h].startsWith(opcodes.get(j).unicode, i))) {
						foundMatch = true;
						
						if (opcodes.get(j).hex.length() > 2) {
							result.add(Integer.parseInt(opcodes.get(j).hex.substring(0, 2), 16));
							result.add(Integer.parseInt(opcodes.get(j).hex.substring(2), 16));
						} else if (opcodes.get(j).hex.length() > 0) {
							result.add(Integer.parseInt(opcodes.get(j).hex, 16));
						}
						if (lines[h].startsWith(opcodes.get(j).text, i)) {
							i += opcodes.get(j).text.length()-1;
						} else { //lines[h].startsWith(opcodes.get(j).unicode, i) == true
							i += opcodes.get(j).unicode.length()-1;
						}
						break;
					}
				}
				
				if (!foundMatch) {
					if (!allowUnknownOpcodes && !currentPosIsString && !currentPosIsComment || lines[h].charAt(i) == '&') {
						if (lines[h].charAt(i) != ' ') {
							if (progName.endsWith("password")) {
								i += 11;
							} else if (progName.endsWith("name")) {
								i += 15;
							}
							String unknownOpcode = lines[h].substring(i, (lines[h].indexOf(' ', i) <= i ? lines[h].length() : lines[h] .indexOf(' ', i)));
							error(progName, h+startLine, i+1, "Unknown opcode \"" + unknownOpcode + "\". Check the spelling and case.\n"+lines[h]);
							return null;
						}
					} else {
						result.add(lines[h].charAt(i));
					}
					
				}
				
			}
			
			//add line feed
			if (h < lines.length-1 && !lines[h].trim().endsWith("◢") && !lines[h].trim().endsWith("&disp;")) {
				result.add(0x0D);
			}
			
		}
		
		
		return result;
	}
	
	public static String casioToAscii(CasioString content, boolean addSpaces) {
		String allowedCharacters = " !#$%;@_`abcdefghijklmnopqrstuvwxyz|";
		
		//Opcodes causing indentation
		List<String> indent = Arrays.asList(new String[]{
			"f700", //If
			"f702", //Else
			"f704", //For
			"f708", //While
			"f70a", //Do
		});
		//Opcodes causing unindentation
		List<String> unindent = Arrays.asList(new String[]{
			"f702", //Else
			"f703", //IfEnd
			"f707", //Next
			"f709", //WhileEnd
			"f70b", //LpWhile
		});
		
		//Opcodes with added spaces
		List<String> opcodesSpaces = Arrays.asList(new String[] {
			"E", options.getProperty("spacesFor->"), " -> ",
			"10", options.getProperty("spacesFor<="), " <= ",
			"11", options.getProperty("spacesFor!="), " != ",
			"12", options.getProperty("spacesFor>="), " >= ",
			"13", options.getProperty("spacesFor=>"), " => ",
			"2C", options.getProperty("spacesFor,"), ", ",
			"3A", options.getProperty("spacesFor:"), " : ",
			"3C", options.getProperty("spacesFor<"), " < ",
			"3D", options.getProperty("spacesFor="), " = ",
			"3E", options.getProperty("spacesFor>"), " > ",
			"89", options.getProperty("spacesFor+"), " + ",
			"99", options.getProperty("spacesFor-"), " - ",
			"A8", options.getProperty("spacesFor^"), " ^ ",
			"A9", options.getProperty("spacesFor*"), " * ",
			"B9", options.getProperty("spacesFor/"), " / ",
		});
		
		List<String> lines = new ArrayList<String>();
		String tabs = "";
		String currentLine = "";
		int indentLevel = 0;
		int currentIndentLevel = indentLevel;
		boolean unindentCurrentLineAndIndentNext = false;

		boolean currentPosIsString = false;
		boolean escapeNextChar = false;
		boolean currentPosIsComment = false;
		
		for (int i = 0; i < content.length(); i++) {
			boolean isMultiByte = isMultibytePrefix((byte)content.charAt(i));
			boolean foundMatch = false;
			
			//Allow characters that are not in the opcodes
			if (allowedCharacters.contains(""+(char)content.charAt(i))) {
				currentLine += (char)content.charAt(i);
				continue;
			}
			String hex = Integer.toHexString(content.charAt(i)&0xFF);
			if (hex.equals("0")) {
				break;
			}
			if (isMultiByte) {
				hex += (content.charAt(i+1) < 0x10 ? "0" : "") + Integer.toHexString(content.charAt(i+1));
			}
			
			if (content.charAt(i) == '"' && !escapeNextChar) {
				currentPosIsString = !currentPosIsString;
			}
			if (content.charAt(i) == '\\' && !escapeNextChar) {
				escapeNextChar = true;
			} else {
				escapeNextChar = false;
			}
			if (content.charAt(i) == '\'' && !currentPosIsString) {
				currentPosIsComment = true;
			}
			if ((content.charAt(i) == '\r' || content.charAt(i) == ':') && !currentPosIsString) {
				currentPosIsComment = false;
			}
			if (content.charAt(i) == '\r') {
				currentPosIsString = false;
			}
			
			if (addSpaces && !currentPosIsString && !currentPosIsComment) {
				boolean addedSpaces = false;
				for (int j = 0; j < opcodesSpaces.size()/3; j++) {
					if (opcodesSpaces.get(j*3+1).equals("true") && opcodesSpaces.get(j*3).equalsIgnoreCase(hex)) {
						
						//Check for unary operators, which are there if there is another operator (and space) before it
						if (currentLine.length() > 1 && currentLine.charAt(currentLine.length()-1) == ' ' && (opcodesSpaces.get(j*3+2).equals(" + ")||opcodesSpaces.get(j*3+2).equals(" - "))) {
							currentLine += opcodesSpaces.get(j*3+2).trim();
						} else {
							currentLine += opcodesSpaces.get(j*3+2);
						}
						addedSpaces = true;
						break;
					}
				}
				if (addedSpaces) {
					continue;
				}
			}
			
			//Indent
			if (indent.contains(hex)) {
				indentLevel++;
				tabs += "\t";
			}
			
			//Unindent
			if (unindent.contains(hex) && indentLevel > 0) {
				indentLevel--;
				try {
					tabs = tabs.substring(1);
				} catch (StringIndexOutOfBoundsException e) {}
			}
			
			//line feed
			if (hex.equalsIgnoreCase("D")||hex.equalsIgnoreCase("C")) {
				
				currentLine = tabs + currentLine;
				//remove tab if unindenting
				
				if (unindentCurrentLineAndIndentNext) {
					currentIndentLevel--;
				}
				
				if (currentIndentLevel < indentLevel) {
					currentLine = currentLine.substring((indentLevel-currentIndentLevel));
				}
				if (unindentCurrentLineAndIndentNext) {
					indentLevel++;
					unindentCurrentLineAndIndentNext = false;
				}
				
				//Replace "\nThen" by ":Then\n"
				if (i+3 < content.length() && content.substring(i, i+3).equals(new CasioString(new byte[]{0x0D, (byte)0xF7, 0x01}))) {
					//System.out.println("Replacing a then");
					currentLine += " :Then";
					i += 2;
				}
				
				lines.add(currentLine + (hex.equalsIgnoreCase("C") ? "◢" : "") + "\n");
				
				currentLine = "";
				currentIndentLevel = indentLevel;
				continue;
			}
			
			//Test for ascii opcodes such as ->, =>
			if ((hex.equalsIgnoreCase("3D") || hex.equalsIgnoreCase("99")) && i > 0) { //'=' or '-'
				short c = content.charAt(i-1);
				if (hex.equalsIgnoreCase("3D") && (c == '!' || c == '>' || c == '<')) { //!= >= <=
					currentLine += "&=;";
					continue;
				}
				if (i < content.length()-1) {
					c = content.charAt(i+1);
					if (hex.equalsIgnoreCase("99") && i < content.length()-1 && (c == '>' || c == 0x12)) { //-> ->=
						currentLine += "&-;";
						continue;
					}
					if (hex.equalsIgnoreCase("3D") && i < content.length()-1 && (c == '>' || c == 0x12)) { //=> =>=
						currentLine += "&=;";
						continue;
					}
				}
			}
			
			//System.out.println("Testing for opcode " + hex);
			for (int j = 0; j < opcodes.size(); j++) {
				if (hex.equalsIgnoreCase(opcodes.get(j).hex)) {
					if (opcodes.get(j).unicode == null || BIDE.options.getProperty("allowUnicode").equals("false")) {
						if (!currentPosIsString || opcodes.get(j).text.length() == 1 || opcodes.get(j).text.startsWith("&")
								|| "->=><=>=!=".contains(opcodes.get(j).text)) {
							currentLine += opcodes.get(j).text;
						} else {
							currentLine += "&#" + opcodes.get(j).hex + ";";
						}
					} else {
						if (!currentPosIsString || opcodes.get(j).unicode.length() == 1) {
							currentLine += opcodes.get(j).unicode;
						} else {
							currentLine += "&#" + opcodes.get(j).hex + ";";
						}
					}
					foundMatch = true;
					//System.out.println("Matches opcode " + opcodes.get(j).ascii);
					break;
				}
			}
			
			if (hex.equals("f702")) {
				unindentCurrentLineAndIndentNext = true;
				currentLine += "\n" + tabs;
			}
			
			if (!foundMatch) {
				System.out.println("WARNING: Unknown opcode 0x" + hex);
				currentLine += "&#"+hex+";";
			}

			
			
			if (isMultiByte) i++;
			//System.out.print(Integer.toHexString(prog1.charAt(i)) + " ");
		}
		
		lines.add(currentLine);
		
		
		StringBuilder strBuilder = new StringBuilder();
		for (int i = 0; i < lines.size(); i++) {
		   strBuilder.append(lines.get(i));
		}
		
		return strBuilder.toString();
	}
	
	public static void getOpcodes() {
		
		GsonBuilder gbuilder = new GsonBuilder();
		Gson gson = gbuilder.create();
		
		StringBuilder sb = new StringBuilder();
		
		try {
			String line;
			//BufferedReader br = new BufferedReader(new FileReader(new File(relativePath)));
			BufferedReader br = new BufferedReader(new InputStreamReader(BIDE.class.getClass().getResourceAsStream("/opcodes.txt"), "UTF-8"));
			while ((line = br.readLine()) != null) {
				sb.append(line);
				sb.append("\n");
			}
			br.close();
		    
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String json = sb.toString();
		Type listType = new TypeToken<List<Opcode>>(){}.getType();
		opcodes = gson.fromJson(json, listType);
		
		//Order opcodes by inverse alphabetical order of their ascii string
		Collections.sort(opcodes, new Comparator<Opcode>() {
			@Override
			public int compare(Opcode o1, Opcode o2) {
				return o2.text.compareTo(o1.text);
			}
		});
		
		//Check for duplicates
		for (int i = 0; i < opcodes.size(); i++) {
			for (int j = i+1; j < opcodes.size(); j++) {
				if (opcodes.get(i).text.equals(opcodes.get(j).text)) {
					error("opcodes.txt", "Opcode 0x"+opcodes.get(i).hex + " conflicts with opcode 0x"+opcodes.get(j).hex + "!");
				}
			}
		}
		
	}
	
	public static void clearMacros() {
		macros.clear();
		macros = new ArrayList<Macro>(defaultMacros);
	}
	
	public static void initMacros() {
		
		defaultMacros = new ArrayList<Macro>();
		
		defaultMacros.add(new Macro("!=", "!="));
		defaultMacros.add(new Macro("!", "Not "));
		defaultMacros.add(new Macro("&&", " And "));
		defaultMacros.add(new Macro("||", " Or "));
		defaultMacros.add(new Macro("%", " Rmdr "));
		
		defaultMacros.add(new Macro("→", "->"));
		defaultMacros.add(new Macro("⇒", "=>"));
		defaultMacros.add(new Macro("≥", ">="));
		defaultMacros.add(new Macro("≤", ">="));
		defaultMacros.add(new Macro("≠", "!="));
		
		defaultMacros.add(new Macro("&MOD(;","MOD("));
		defaultMacros.add(new Macro("&GCD(;","GCD("));
		defaultMacros.add(new Macro("&LCM(;","LCM("));
		
		defaultMacros.add(new Macro("beginBenchmark()", "ClrText:Locate 1,2,\"Begin\"&disp;"));
		defaultMacros.add(new Macro("endBenchmark()", "ClrText:Locate 1,2,\"End  \"&disp;"));
		defaultMacros.add(new Macro("graphxyVWin()", "ViewWindow 1,127,0,63,1,0,0,1,1"));
		defaultMacros.add(new Macro("topLeftVWin()", "ViewWindow 1,127,0,63,1,0"));
		defaultMacros.add(new Macro("bottomLeftVWin()", "ViewWindow 1,127,0,1,63,0"));
		defaultMacros.add(new Macro("initGraphScreen()", "AxesOff\nLabelOff\nGridOff\nBG-None\nClrGraph\nViewWindow 1,127,0,63,1,0,0,1,1"));
		
		defaultMacros.add(new Macro("clrLine(i)", "Locate 1,%i%,\"                     \"", new String[]{"i"}));
		
		Object[] keyCodes = {
			"key_f1", 79,
			"key_f2", 69,
			"key_f3", 59,
			"key_f4", 49,
			"key_f5", 39,
			"key_f6", 29,
			"key_shift", 78,
			"key_alpha", 77,
			"key_optn", 68,
			"key_square", 67,
			"key_vars", 58,
			"key_power", 57,
			"key_menu", 48,
			"key_exit", 47,
			"key_up", 28,
			"key_down", 37,
			"key_left", 38,
			"key_right", 27,
			"key_x", 76,
			"key_log", 66,
			"key_ln", 56,
			"key_sin", 46,
			"key_cos", 36,
			"key_tan", 26,
			"key_frac", 75,
			"key_fd", 65,
			"key_lparen", 55,
			"key_rparen", 45,
			"key_comma", 35,
			"key_arrow", 25,
			"key_7", 74,
			"key_8", 64,
			"key_9", 54,
			"key_del", 44,
			"key_4", 73,
			"key_5", 63,
			"key_6", 53,
			"key_mult", 43,
			"key_div", 33,
			"key_1", 72,
			"key_2", 62,
			"key_3", 52,
			"key_plus", 42,
			"key_minus", 32,
			"key_0", 71,
			"key_dot", 61,
			"key_exp", 51,
			"key_neg", 41,
			"key_exe", 31,
		};
		for (int i = 0; i < keyCodes.length; i += 2) {
			defaultMacros.add(new Macro((String)keyCodes[i], String.valueOf(keyCodes[i+1])));
		}

		macros = new ArrayList<Macro>(defaultMacros);
	}
	
	public static List<Byte> byteArrayToList(byte[] b) {
		List<Byte> result = new ArrayList<Byte>();
		for (int i = 0; i < b.length; i++) {
			result.add(b[i]);
		}
		return result;
	}
	
	public static boolean isMultibytePrefix(byte prefix) {
		if (prefix == (byte)0xF7 ||
				prefix == (byte)0x7F ||
				prefix == (byte)0xF9 ||
				prefix == (byte)0xE5 ||
				prefix == (byte)0xE6 ||
				prefix == (byte)0xE7)
			return true;
		return false;
	}
	
	public static void cleanupStrings() {
		
		String[] pairs = new String[] {
			"&n_stat;", "n",
			"&e_reg;", "e",
			"&#7FD4;", "se",
			"&fact;", "!",
			"&#7FE2;", "pa",
			"&#7FE3;", "pb",
			"&#7FE4;", "pab",
			"&#F91B;", "fn",
			"&#C5;", "sx",
			"&#C7;", "sy",
			"&#7FC6;", "sp",
			"&bitwiseor;", "or",
			"&bitwiseand;", "and",
			"&bitwisexor;", "xor",
			"&bitwisexnor;", "xnor",
			"&#F752;", "Hist",
			"&#7FBE;", "Fa",
			"&#F95B;", "Norm",
		};
		
		for (int g = 0; g < BIDE.g1mparts.size(); g++) {
			if (BIDE.g1mparts.get(g).comp instanceof ProgScrollPane) {
				String content = ((ProgScrollPane)BIDE.g1mparts.get(g).comp).textPane.getText();
				String[] lines = content.split("\\n|\\r|\\r\\n");
				
				for (int h = 0; h < lines.length; h++) {
					if (lines[h].startsWith("#") || lines[h].trim().isEmpty()) {
						continue;
					}
					boolean currentPosIsString = false;
					boolean escapeNextChar = false;
					boolean currentPosIsComment = false;
					
					for (int i = 0; i < lines[h].length(); i++) {
						
						if (lines[h].charAt(i) == '"' && !escapeNextChar) {
							currentPosIsString = !currentPosIsString;
						}
						
						if (lines[h].charAt(i) == '\\' && !escapeNextChar) {
							escapeNextChar = true;
						} else {
							escapeNextChar = false;
						}
						
						if (lines[h].charAt(i) == '\'' && !currentPosIsString) {
							currentPosIsComment = true;
						}
						
						if (lines[h].charAt(i) == ':' && !currentPosIsString && currentPosIsComment) {
							currentPosIsComment = false;
							continue;
						}
						
						if (currentPosIsComment) {
							continue;
						}
						if (!currentPosIsString) {
							continue;
						}
						
						for (int j = 0; j < pairs.length; j+=2) {
							if (lines[h].startsWith(pairs[j], i)) {
								lines[h] = lines[h].substring(0, i) + pairs[j+1] + lines[h].substring(pairs[j].length()+i);
								break;
							}
						}
						
					}
				}
				
				((ProgScrollPane)BIDE.g1mparts.get(g).comp).textPane.setText(Arrays.stream(lines).collect(Collectors.joining("\n")));
				
			}
		}
	}
	
	public static void checkForNewVersion() {
		TrustManager[] trustAllCerts = new TrustManager[]{
    	        new X509TrustManager() {
    	            public java.security.cert.X509Certificate[] getAcceptedIssuers()
    	            {
    	                return null;
    	            }
    	            public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType)
    	            {
    	                //No need to implement.
    	            }
    	            public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType)
    	            {
    	                //No need to implement.
    	            }
    	        }
    	};

    	// Install the all-trusting trust manager
    	try 
    	{
    	    SSLContext sc = SSLContext.getInstance("SSL");
    	    sc.init(null, trustAllCerts, new java.security.SecureRandom());
    	    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    	} 
    	catch (Exception e) 
    	{
    	    System.out.println(e);
    	}
    	
        URL oracle;
		try {
			oracle = new URL("https://www.planet-casio.com/Fr/logiciels/voir_un_logiciel_casio.php?cat=6");
			BufferedReader in = new BufferedReader(new InputStreamReader(oracle.openStream()));

	        String inputLine;
	        while ((inputLine = in.readLine()) != null) {
	            if (inputLine.contains("<a href=\"dl_logiciel.php?id=118&file=1\">")) {
	            	String currentVersion = inputLine.substring(inputLine.indexOf("BIDE")+5, inputLine.indexOf(".zip"));
	            	if (!currentVersion.equals(BIDE.VERSION)) {
	            		System.out.println("A new version of BIDE ("+currentVersion+") is available.");
	            	}
	            }
	        	//System.out.println(inputLine);
	            
	        }
	        in.close();
		} catch (Exception e) {
			//e.printStackTrace();
		}
	}
	
	public static void error(String error) {
		System.out.println("\nERROR: "+error+"\n");
		//System.exit(0);
	}
	public static void error(String progName, String error) {
		error(progName + ": " + error);
	}
	public static void error(String progName, int line, String error) {
		error(progName + ", line " + line + ": " + error);
	}
	public static void error(String progName, int line, int col, String error) {
		error(progName + ", line " + line + ", col " + col + ": " + error);
	}

}
