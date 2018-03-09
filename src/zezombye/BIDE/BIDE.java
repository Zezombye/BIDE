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
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/*
 * TODO:
 * add find and replace
 */

public class BIDE {
	
	public static List<Opcode> opcodes = new ArrayList<Opcode>();
	public static List<Macro> macros = new ArrayList<Macro>();
	public static List<Macro> defaultMacros = new ArrayList<Macro>();
	
	public static String pathToG1M = System.getProperty("user.home")+"/desktop/";
	public static String pathToSavedG1M = "";
	public static final String pathToOptions = System.getProperty("user.home")+"/BIDE/options.txt";
	public static UI ui = new UI();
	
	public final static String VERSION = "4.0";
	
	public final static int TYPE_PROG = 0;
	public final static int TYPE_PICT = 3;
	public final static int TYPE_CAPT = 4;
	public final static int TYPE_OPCODE = 5;
	public final static int TYPE_COLORATION = 6;
	public final static int TYPE_CHARLIST = 7;
	public static boolean debug = false;
	//public static Font progFont = new Font("DejaVu Sans Mono", Font.PLAIN, 12);
	public static Font progFont, pictFont, dispFont;
	//public static Font dispFont = new Font("DejaVu Sans Mono", Font.PLAIN, 13);
	 
	public final static String pictTutorial = 
			"\n'To edit the picture, use the characters ' , :\n"
			+ "'which make ▀ ▄ █ respectively.\n"
			+ "'Make sure not to edit the border!\n";
	public final static String pictWarning = 
			"\n'\n'DO NOT EDIT THE PICTURE BELOW, unless you are an advanced user!\n'\n";
	
	static Options options = new Options();
	public static boolean isCLI = false;
	
	public static AutoImport autoImport;
	
	public static void main(String[] args) {
		
		if (args.length > 0 && args[0].equals("debug")) {
			//TODO: change that
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
		
		System.out.println(Arrays.toString(args));
		if (args.length > 0) {
			//CLI
			isCLI = true;
			if (args[0].equals("--compile")) {
				String pathToG1M = args[1];
				ArrayList<Program> progs = new ArrayList<Program>();
				for (int i = 2; i < args.length; i++) {
					try {
						progs.addAll(readFromTxt(args[i]));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				try {
					writeToG1M(pathToG1M, progs);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if (args[0].equals("--decompile")) {
				String pathToG1M = args[1];
				String pathToDest = args[2];
				if (!pathToDest.endsWith(System.getProperty("file.separator"))) {
					pathToDest += System.getProperty("file.separator");
				}
				try {
					ArrayList<Program> progs = readFromG1M(pathToG1M);
					
					for (int i = 0; i < progs.size(); i++) {
						//Disallowed chars: <>:"/\|?*
						String name = progs.get(i).name.replaceAll("<|>|:|\"|\\/|\\\\|\\||\\?|\\*", "_");
						IO.writeStrToFile(new File(pathToDest+name+".bide"), progs.get(i).content, true);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
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
			if (options.getProperty("useEmulator").equals("true")) {
				autoImport = new AutoImport();
			}
			pictFont = new Font("DejaVu Avec Casio", Font.TRUETYPE_FONT, Integer.parseInt(options.getProperty("pictFontSize")));
			ui.createAndDisplayUI();
			ProgramTextPane.initAutoComplete();
			
			//ui.jtp.addTab("test", new Program("test1", "", "testcontent", TYPE_PICT).comp);
			//ui.createNewTab(TYPE_COLORATION);
			ui.createNewTab(TYPE_CHARLIST);
			//((ProgScrollPane)ui.jtp.getComponentAt(0)).textPane.setText("testcontent");
			
			//new AutoImport().autoImport("C:\\Users\\Catherine\\Desktop\\PUISS4.g1m");
			System.out.println("Finished initialization");
		}
		
		
		
	}
	
	public static ArrayList<Program> readFromTxt(String txtPath) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(txtPath));
		String content = new String(encoded, "UTF-8");
		
		ArrayList<Program> progs = new ArrayList<Program>();
		
		String[] progsTxt = content.split("\\n#End of part\\n");
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
				return null;
			}
			
			String name = progsTxt[i].substring(15, progsTxt[i].indexOf('\n'));
			
			//Get 2nd line, which is option
			/*String option = progsTxt[i].substring(progsTxt[i].indexOf('\n'), progsTxt[i].substring(progsTxt[i].indexOf('\n')).indexOf('\n'));
			if (type == BIDE.TYPE_PROG && option.startsWith("#Password: ")) {
				if (!option.substring(11).equals("<no password>")) {
					option = option.substring(11);
				} else {
					option = "";
				}
			}
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
			}*/
			progs.add(new Program(name, "", progsTxt[i], type));
		}
		return progs;
		
	}
	
	public static ArrayList<Program> readFromG1M(String g1mpath) throws IOException {
		ArrayList<Program> progs = new ArrayList<Program>();
		System.out.println("Reading from g1m at " + g1mpath);
		G1MParser g1mparser = new G1MParser(g1mpath);
		g1mparser.readG1M();
		if (!g1mparser.checkValidity()) {
			error("Invalid g1m!");
			return null;
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
					return null;
				}
				
				progs.add(new Program(progName, progPw, progContent, TYPE_PROG));
			} else if (g1mparser.getPartType(g1mparser.parts.get(h)) == TYPE_PICT || g1mparser.getPartType(g1mparser.parts.get(h)) == TYPE_CAPT) {
				String name = casioToAscii(g1mparser.getPartName(g1mparser.parts.get(h)), false);
				//TODO: see if the second part of pictures is important or not
				CasioString content = null;
				if (g1mparser.getPartType(g1mparser.parts.get(h)) == TYPE_PICT) {
					System.out.println("Found picture \""+name+"\"");
					content = g1mparser.getPartContent(g1mparser.parts.get(h));
				} else {
					System.out.println("Found capture \""+name+"\"");
					//Captures have a width and height attribute, skip it
					content = g1mparser.getPartContent(g1mparser.parts.get(h)).substring(4, 0x404);
				}
				String asciiResult = pictToAscii(content, g1mparser.getPartType(g1mparser.parts.get(h)));
				
				if (g1mparser.getPartType(g1mparser.parts.get(h)) == TYPE_PICT) {
					progs.add(new Program(name, Integer.toHexString(content.length()), asciiResult, TYPE_PICT));
				} else {
					progs.add(new Program(name, "", asciiResult, TYPE_CAPT));
				}
			}
		}
		System.out.println("Loading g1m...");
		return progs;
	}
	
	public static void writeToG1M(String destPath) throws IOException {
		//Parse programs from UI
		List<Program> parts = new ArrayList<Program>();
		//Parse text file
		for (int h = 0; h < ui.jtp.getTabCount(); h++) {
			int type = ((ProgScrollPane)ui.jtp.getComponentAt(h)).type;
			if (type != TYPE_PROG && type != TYPE_PICT && type != TYPE_CAPT) {
				continue;
			}
			
			String[] lines = ((ProgScrollPane)ui.jtp.getComponentAt(h)).textPane.getText().split("\\n|\\r|\\r\\n");

			
			if (type == TYPE_PROG && !lines[0].startsWith("#Program name: ")) {
				error("Program "+ ui.jtp.getComponentAt(h).getName() + " must include the directive \"#Program name: \" at the beginning!");
			}
			if (type == TYPE_PICT && !lines[0].startsWith("#Picture name: ")) {
				error("Picture "+ ui.jtp.getComponentAt(h).getName() + " must include the directive \"#Picture name: \" at the beginning!");
			}
			if (type == TYPE_CAPT && !lines[0].startsWith("#Capture name: ")) {
				error("Capture "+ ui.jtp.getComponentAt(h).getName() + " must include the directive \"#Capture name: \" at the beginning!");
			}
			parts.add(new Program("", "", ((ProgScrollPane)ui.jtp.getComponentAt(h)).textPane.getText(), type));
			//System.out.println("Program text="+((Program)ui.jtp.getComponentAt(h)).textPane.getText());
		}
		writeToG1M(destPath, parts);
	}
	
	public static void writeToG1M(String destPath, List<Program> parts) throws IOException {
		
		long time = System.currentTimeMillis();
		System.setOut(System.out);
		System.setOut(new PrintStream(new CustomOutputStream(BIDE.ui.stdout)));
		BIDE.ui.stdout.setText("");
		System.out.println("Saving to \""+destPath+"\"...");
		
		G1MWrapper g1mwrapper = new G1MWrapper();
		//Parse text file
		Program currentPart = null;
		clearMacros();
		
		//ArrayList<Macro> macros = new ArrayList<Macro>();
		for (int h = 0; h < parts.size(); h++) {
			int type = parts.get(h).type;
			if (type == TYPE_OPCODE || type == TYPE_COLORATION) {
				continue;
			}
			
			currentPart = new Program("", "", "", parts.get(h).type);
			currentPart.content = "";
			String[] lines = parts.get(h).content.split("\\n|\\r|\\r\\n");
			//System.out.println(lines[0]);
			
			currentPart.name = lines[0].substring(15);
						
			if (type == BIDE.TYPE_PROG && lines[1].startsWith("#Password: ")) {
				if (!lines[1].substring(11).equals("<no password>")) {
					currentPart.option = lines[1].substring(11);
				}
			}
			if (type == BIDE.TYPE_PICT) {
				if (lines[1].startsWith("#Size: 0x")) {
					currentPart.option = lines[1].substring(9);
					try {
						Integer.parseInt(currentPart.option, 16);
					} catch (NumberFormatException e) {
						error(currentPart.name, "Invalid picture size!");
						return;
					}
				} else {
					error(currentPart.name, "Picture size undefined!");
					return;
				}
			}
			for (int i = 0; i < lines.length; i++) {
				if (lines[i].endsWith("Then") || lines[i].endsWith("Else")) {
					lines[i] += " ";
				}
				if (lines[i].startsWith("#define ")) {
					try {
						macros.add(new Macro(lines[i].substring(8, lines[i].indexOf(' ', 8)), lines[i].substring(lines[i].indexOf(' ', 8)+1)));
					} catch (StringIndexOutOfBoundsException e) {
						error(currentPart.name, i+1, "Invalid macro declaration!");
						return;
					}
				}
				currentPart.content += lines[i] + "\n";
			}
			parts.set(h, currentPart);
			
		}
		Collections.sort(macros, new Comparator<Macro>() {
			@Override
			public int compare(Macro o1, Macro o2) {
				return o2.text.compareTo(o1.text);
			}
		});
		if (parts.size() == 0) {
			error("No programs detected!");
			return;
		}
		if (BIDE.debug) {
			System.out.println(macros.toString());
		}
		//Add each part (program) of the ascii file
		byte[] padding = {0,0,0,0,0,0,0,0};
		for (int i = 0; i < parts.size(); i++) {
			
			CasioString name = new CasioString(asciiToCasio(parts.get(i).name, true, parts.get(i).name+".name", 1, macros));

			System.out.println("Parsing \""+parts.get(i).name+"\"");
			
			if (name.length() > 8) {
				error("Program \""+parts.get(i).name+"\" has a name too long (8 characters max)!");
				return;
			}
						
			if (parts.get(i).type == BIDE.TYPE_CAPT && !parts.get(i).name.startsWith("CAPT")) {
				error("Capture \""+parts.get(i).name+"\"'s name should start with \"CAPT\"!");
				return;
			} else if (parts.get(i).type == BIDE.TYPE_PICT && !parts.get(i).name.startsWith("PICT")) {
				error("Picture \""+parts.get(i).name+"\"'s name should start with \"PICT\"!");
				return;
			}
			if (parts.get(i).type == BIDE.TYPE_CAPT || parts.get(i).type == BIDE.TYPE_PICT) {
				try {
					int nb = Integer.parseInt(parts.get(i).name.substring(4));
					if (nb < 1 || nb > 20) {
						error("Number of "+parts.get(i).name+" should be 1-20!");
						return;
					}
				} catch (NumberFormatException e) {
					error(parts.get(i).name, "Invalid picture/capture number!");
					return;
				}
			}
			
			name.add(Arrays.copyOfRange(padding, 0, 8-name.length()));
			CasioString part = new CasioString("");
			
			if (parts.get(i).type == BIDE.TYPE_PROG) {
				CasioString password = new CasioString(asciiToCasio(parts.get(i).option, true, parts.get(i).name+".password", 2, macros));
				if (password.length() > 8) {
					error("Program \""+parts.get(i).name+"\" has a password too long (8 characters max)!");
					return;
				}
				password.add(Arrays.copyOfRange(padding, 0, 8-password.length()));
				part.add(password);
				part.add(new byte[]{0,0});
				part.add(asciiToCasio(parts.get(i).content, false, parts.get(i).name, 1, macros));
				part.add(Arrays.copyOfRange(padding, 0, 4-part.length()%4));
			} else if (parts.get(i).type == BIDE.TYPE_PICT){
				part.add(asciiToPict(parts.get(i).content, parts.get(i).name, 3, parts.get(i).option));
			} else {
				part.add(new byte[]{0x00, (byte)0x80, 0x00, 0x40});
				part.add(asciiToPict(parts.get(i).content, parts.get(i).name, 2, "400"));
			}
			
			g1mwrapper.addPart(part, name, parts.get(i).type);
		}
		g1mwrapper.generateG1M(destPath);
		System.out.println("Finished writing to g1m in "+(System.currentTimeMillis()-time)+"ms");
		
	}
	
	public static void writeToTxt(String destPath) {
		System.out.println("Saving to "+destPath+"...");
		String result = "";
		for (int h = 0; h < ui.jtp.getTabCount(); h++) {
			result += ((ProgScrollPane)ui.jtp.getComponentAt(h)).textPane.getText() + "\n#End of part\n";
		}
		try {
			IO.writeStrToFile(new File(destPath), result, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Done!");
	}
	
	public static String pictToAscii(CasioString content, int type) {
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
	}
	
	public static String pictToAscii(String content, int type) {
		String asciiResult = "";
		for (int g = 0; g < (type == TYPE_PICT ? 2 : 1); g++) {
			if (g == 1) {
				asciiResult += pictWarning;
			}
			for (int i = 0; i < 130; i++) {
				asciiResult += "▄";
			}
			asciiResult += "\n";
			for (int i = 0; i < 32; i++) {
				asciiResult += "█";
				for (int j = 0; j < 128; j++) {
					char pixel, pixel2;
					try {
						pixel = content.charAt(i*2*128 + j%128 + g*0x400*8);
					} catch (IndexOutOfBoundsException e) {
						pixel = '0';
					}
					try {
						pixel2 = content.charAt((i*2+1)*128 + j%128 + g*0x400*8);
					} catch (IndexOutOfBoundsException e) {
						pixel2 = '0';
					}
					if (pixel == '0' && pixel2 == '0') {
						asciiResult += " ";
					} else if (pixel == '0' && pixel2 == '1') {
						asciiResult += "▄";
					} else if (pixel == '1' && pixel2 == '0') {
						asciiResult += "▀";
					} else if (pixel == '1' && pixel2 == '1') {
						asciiResult += "█";
					}
				}
				asciiResult += "█\n";
			}
			for (int i = 0; i < 130; i++) {
				asciiResult += "▀";
			}
		}
		return asciiResult;
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
		content = content.replaceAll("Else \\n", "Else ");
		content = content.replaceAll(":Then \\n", "\nThen ");
		content = content.replaceAll("Then \\n", "Then ");
		
		String[] lines = content.split("\\n|\\r|\\r\\n");
				
		for (int h = 0; h < lines.length; h++) {
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
				
				if (currentPosIsComment) {
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
						if (lines[h].startsWith(macros.get(j).text, i)) {
							lines[h] = lines[h].substring(0, i) + macros.get(j).replacement + lines[h].substring(i+macros.get(j).text.length());
							foundMacro = true;
							//System.out.println(lines[h]);
							break;
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
							error(progName, h+startLine, i+1, "Unknown opcode \"" + unknownOpcode + "\". Check the spelling and case.");
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
			if (content.charAt(i) == '\r' && !currentPosIsString) {
				currentPosIsComment = false;
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
						currentLine += opcodes.get(j).text;
					} else {
						currentLine += opcodes.get(j).unicode;
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
		
		defaultMacros.add(new Macro("!=", "!="));
		defaultMacros.add(new Macro("!", "Not "));
		defaultMacros.add(new Macro("&&", " And "));
		defaultMacros.add(new Macro("||", " Or "));
		defaultMacros.add(new Macro("%", " Rmdr "));
		defaultMacros.add(new Macro("beginBenchmark", "ClrText:Locate 1,2,\"Begin\"&disp;"));
		defaultMacros.add(new Macro("endBenchmark", "ClrText:Locate 1,2,\"End  \"&disp;"));
		
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
