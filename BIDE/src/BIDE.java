import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public class BIDE {
	
	public static List<Opcode> opcodes = new ArrayList<Opcode>();
	
	public static String pathToG1M = System.getProperty("user.home")+"/desktop/";
	public static String pathToSavedG1M = "";
	public static UI ui = new UI();
	
	public final static int TYPE_PROG = 0;
	public final static int TYPE_PICT = 3;
	public final static int TYPE_CAPT = 4;
	public final static int TYPE_OPCODE = 5;

	public static Font progFont = new Font("DejaVu Sans Mono", Font.PLAIN, 12);
	public static Font pictFont = new Font("Courier New", Font.PLAIN, 13);
	
	public final static String pictTutorial = 
			"\n'To edit the picture, use the characters ' , :\n"
			+ "'which make ▀ ▄ █ respectively.\n"
			+ "'Make sure not to edit the border!\n";
	public final static String pictWarning = 
			"\n'\n'DO NOT EDIT THE PICTURE BELOW, unless you are an advanced user!\n'\n";
	public static void main(String[] args) {
		
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		try {
			ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, BIDE.class.getClass().getResourceAsStream("/droid-sans-mono.ttf")));
		} catch (FontFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		
		ui.createAndDisplayUI();
		getOpcodes();
		System.out.println("Finished initialization");
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
				//Convert from binary to bitmap
				String binary = "";
				for (int i = 0; i < content.length(); i++) {
					int mask = 0b10000000;
					for (int j = 0; j < 8; j++) {
						binary += ((content.charAt(i)&mask) != 0 ? "1" : "0");
						mask >>= 1;
					}
				}
				String asciiResult = "";
				for (int g = 0; g < (g1mparser.getPartType(g1mparser.parts.get(h)) == TYPE_PICT ? 2 : 1); g++) {
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
								pixel = binary.charAt(i*2*128 + j%128 + g*0x400*8);
							} catch (IndexOutOfBoundsException e) {
								pixel = '0';
							}
							try {
								pixel2 = binary.charAt((i*2+1)*128 + j%128 + g*0x400*8);
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
		
		System.out.println("Saving to \""+destPath+"\"...");
		
		G1MWrapper g1mwrapper = new G1MWrapper();
		
		List<Program> parts = new ArrayList<Program>();
		//Parse text file
		Program currentPart = null;
		/*try {
			BufferedReader br = new BufferedReader(new FileReader(new File(progPath)));
			
			while ((line = br.readLine()) != null) {*/
		for (int h = 0; h < ui.jtp.getTabCount(); h++) {
			int type = ((Program)ui.jtp.getComponentAt(h)).type;
			if (type == TYPE_OPCODE) {
				continue;
			}
			
			String[] lines = ((Program)ui.jtp.getComponentAt(h)).textPane.getText().split("\\n|\\r|\\r\\n");

			if (type == TYPE_PROG && !lines[0].startsWith("#Program name: ")) {
				error("Program "+((Program)ui.jtp.getComponentAt(h)).name + " must include the directive \"#Program name: \" at the beginning!");
			}
			if (type == TYPE_PICT && !lines[0].startsWith("#Picture name: ")) {
				error("Picture "+((Program)ui.jtp.getComponentAt(h)).name + " must include the directive \"#Picture name: \" at the beginning!");
			}
			if (type == TYPE_CAPT && !lines[0].startsWith("#Capture name: ")) {
				error("Capture "+((Program)ui.jtp.getComponentAt(h)).name + " must include the directive \"#Capture name: \" at the beginning!");
			}
			
			currentPart = new Program("","","", type);
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
					}
				} else {
					error(currentPart.name, "Picture size undefined!");
				}
			}
			
			for (int i = 0; i < lines.length; i++) {
				if (lines[i].endsWith("Then") || lines[i].endsWith("Else")) {
					lines[i] += " ";
				}
				if (!lines[i].startsWith("#")) {
					currentPart.content += lines[i] + "\n";
				}
			}
			parts.add(currentPart);
			
		}
		
		if (parts.size() == 0) {
			error("No programs detected!");
			return;
		}
		//Add each part (program) of the ascii file
		byte[] padding = {0,0,0,0,0,0,0,0};
		for (int i = 0; i < parts.size(); i++) {
			
			CasioString name = new CasioString(asciiToCasio(parts.get(i).name, true, parts.get(i).name+".name", 1));
			if (name.length() > 8) {
				error("Program \""+parts.get(i).name+"\" has a name too long (8 characters max)!");
				return;
			}
			System.out.println("Parsing \""+parts.get(i).name+"\"");
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
				CasioString password = new CasioString(asciiToCasio(parts.get(i).option, true, parts.get(i).name+".password", 2));
				if (password.length() > 8) {
					error("Program \""+parts.get(i).name+"\" has a password too long (8 characters max)!");
					return;
				}
				password.add(Arrays.copyOfRange(padding, 0, 8-password.length()));
				part.add(password);
				part.add(new byte[]{0,0});
				part.add(asciiToCasio(parts.get(i).content, false, parts.get(i).name, 3));
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
		System.out.println("Finished writing to g1m");
		
	}
	
	public static CasioString asciiToPict(String content, String progName, int startLine, String pictSize) {
		//Strip border
		//Split on lines
		String[] lines = content.split("\\n|\\r|\\r\\n");
		String binary = "";
		ArrayList<Byte> bytes = new ArrayList<Byte>();
		for (int i = 0; i < lines.length; i++) {
			if (lines[i].isEmpty() || lines[i].startsWith("'") || lines[i].startsWith("▀") || lines[i].startsWith("▄")) {
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
	
	public static CasioString asciiToCasio(String content, boolean allowUnknownOpcodes, String progName, int startLine) {
		CasioString result = new CasioString();
		
		//Optimise for less wasted space
		content = content.replaceAll("Else \\n", "Else ");
		content = content.replaceAll(":Then \\n", "\nThen ");
		content = content.replaceAll("Then \\n", "Then ");
		
		String[] lines = content.split("\\n|\\r|\\r\\n");
				
		for (int h = 0; h < lines.length; h++) {

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
				
				for (int j = 0; j < opcodes.size(); j++) {
					
					//If string, skip opcodes that are not entities/characters in order to avoid FA-124ing
					if (allowUnknownOpcodes || currentPosIsString || currentPosIsComment) {
						if (opcodes.get(j).ascii.length() != 1 && !opcodes.get(j).ascii.startsWith("&")
								&& !opcodes.get(j).ascii.equals("->")
								&& !opcodes.get(j).ascii.equals("=>")
								&& !opcodes.get(j).ascii.equals("!=")
								&& !opcodes.get(j).ascii.equals(">=")
								&& !opcodes.get(j).ascii.equals("<=")) {
							continue;
						}
					}
					if (lines[h].startsWith(opcodes.get(j).ascii, i)) {
						foundMatch = true;
						
						if (opcodes.get(j).hex.length() > 2) {
							result.add(Integer.parseInt(opcodes.get(j).hex.substring(0, 2), 16));
							result.add(Integer.parseInt(opcodes.get(j).hex.substring(2), 16));
						} else if (opcodes.get(j).hex.length() > 0) {
							result.add(Integer.parseInt(opcodes.get(j).hex, 16));
						}
						i += opcodes.get(j).ascii.length()-1;
						break;
					}
				}
				
				if (!foundMatch) {
					if ((!allowUnknownOpcodes && !currentPosIsString && !currentPosIsComment) || lines[h].charAt(i) == '&') {
						if (lines[h].charAt(i) != ' ') {
							if (progName.endsWith("password")) {
								i += 11;
							} else if (progName.endsWith("name")) {
								i += 15;
							}
							error(progName, h+startLine, i+1, "The char '" + lines[h].charAt(i) + 
									"' shouldn't be here, BIDE did not recognize that opcode. Check the spelling and case.");
							return null;
						}
					} else {
						result.add(lines[h].charAt(i));
					}
					
				}
				
			}
			
			//add line feed
			if (h < lines.length-1) {
				result.add(0x0D);
			}
			
		}
		
		
		return result;
	}
	
	public static String casioToAscii(CasioString content, boolean addSpaces) {
		String allowedCharacters = " !#$%;@_abcdefghijklmnopqrstuvwxyz|";
		
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
			"E", " -> ",
			"10", " <= ",
			"11", " != ",
			"12", " >= ",
			"13", " => ",
			"2C", ", ",
			"3A", " : ",
			"3C", " < ",
			"3D", " = ",
			"3E", " > ",
			"89", " + ",
			"99", " - ",
			"A8", " ^ ",
			"A9", " * ",
			"B9", " / ",
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
				for (int j = 0; j < opcodesSpaces.size()/2; j++) {
					if (opcodesSpaces.get(j*2).equalsIgnoreCase(hex)) {
						currentLine += opcodesSpaces.get(j*2+1);
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
			if ((hex.equalsIgnoreCase("3D") || hex.equalsIgnoreCase("3E")) && i > 0) { //'=' or '>'
				short c = content.charAt(i-1);
				if (hex.equalsIgnoreCase("3D") && (c == '!' || c == '>' || c == '<')) { //!= >= <=
					currentLine += "&=;";
					continue;
				} else if (hex.equalsIgnoreCase("3E") && (c == 0x99 || c == '=')) { //-> =>
					currentLine += "&>;";
					continue;
				}
				
			}
			
			//System.out.println("Testing for opcode " + hex);
			for (int j = 0; j < opcodes.size(); j++) {
				if (hex.equalsIgnoreCase(opcodes.get(j).hex)) {
					currentLine += opcodes.get(j).ascii;
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
				error("Unknown opcode 0x" + hex + "\nTell Zezombye to add it, or add it yourself in opcodes.txt");
				return null;
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
		//String relativePath = IO.getRelativeFilePath("opcodes.txt");
		String line;
		int lineNb = 0;
		try {
			//BufferedReader br = new BufferedReader(new FileReader(new File(relativePath)));
			BufferedReader br = new BufferedReader(new InputStreamReader(BIDE.class.getClass().getResourceAsStream("/opcodes.txt"), "UTF-8"));
			String hex, ascii;
			boolean escape = false;
			
			while ((line = br.readLine()) != null) {
				lineNb++;
				if (line.startsWith("#") || line.isEmpty()) {
					continue;
				}
				hex = line.substring(0, line.indexOf(' '));
				try {
					Integer.parseInt(hex, 16);
				} catch (NumberFormatException e) {
					error("opcodes.txt", lineNb, "Could not parse hex string \"" + hex + "\"");
				}
				ascii = line.substring(line.indexOf(' ')+1, line.lastIndexOf(' '));
				if (line.substring(line.length()-2, line.length()).equals(" f")) {
					escape = false;
				} else if (line.substring(line.length()-2, line.length()).equals(" t")) {
					escape = true;
				} else {
					error("opcodes.txt", lineNb, "Unknown boolean \"" + line.substring(line.length()-2, line.length()) + "\"");
				}
				if (escape) {
					ascii = "&" + ascii + ";";
				}
				opcodes.add(new Opcode(hex, ascii));
				
				//This is to allow removal of trailing whitespace when converting ascii->casio
				//opcodes.add(new Opcode(hex, ascii.replaceAll(" +?$", "")));
		       
		       
			}
			br.close();
		    
		} catch (Exception e) {
			error("opcodes.txt", lineNb, e.getMessage());
		}
		
		//Add opcodes for variables and operators that are ascii and one character
		String additionalOpcodes = "\"'(),.0123456789:<=>?ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]{}~";
		for (int i = 0; i < additionalOpcodes.length(); i++) {
			opcodes.add(new Opcode(Integer.toHexString(additionalOpcodes.charAt(i)), additionalOpcodes.charAt(i)+""));
		}
		
		//Add whitespace
		opcodes.add(new Opcode("", "\t"));
		//opcodes.add(new Opcode("", " "));
		
		//Add newline
		opcodes.add(new Opcode("D", "\n"));
		
		//Add disp
		//opcodes.add(new Opcode("C", "&disp;\n"));
		
		//Order opcodes by inverse alphabetical order of their ascii string
		Collections.sort(opcodes, new Comparator<Opcode>() {
			@Override
			public int compare(Opcode o1, Opcode o2) {
				return o2.ascii.compareTo(o1.ascii);
			}
		});
		
		//Check for duplicates
		for (int i = 0; i < opcodes.size(); i++) {
			for (int j = i+1; j < opcodes.size(); j++) {
				if (opcodes.get(i).ascii.equals(opcodes.get(j).ascii)) {
					error("opcodes.txt", "Opcode 0x"+opcodes.get(i).hex + " conflicts with opcode 0x"+opcodes.get(j).hex + "!");
				}
			}
		}
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
