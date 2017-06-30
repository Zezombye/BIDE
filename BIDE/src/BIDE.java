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
	
	public static String pathToG1M = System.getProperty("user.home");
	public static String pathToSavedG1M = "";
	public static UI ui = new UI();
	
	public final static int TYPE_PROG = 0;
	public final static int TYPE_PICT = 3;
	public final static int TYPE_CAPT = 4;
	
	public static void main(String[] args) {
		
		pathToG1M += "/desktop/clonelab.g1m";
		getOpcodes();
				
		ui.createAndDisplayUI();
		
		//System.out.println(readFromG1M("C:\\Users\\Catherine\\Desktop\\clonelab.g1m"));
		//readFromG1M("C:\\Users\\Catherine\\Desktop\\iceslide.g1m");
		/*} else {
			writeToG1M("C:\\Users\\Catherine\\Desktop\\prog.bide", "C:\\Users\\Catherine\\Desktop\\bundle2.g1m");
		}*/
		
		
		
		//Test stdout
		/*while(true) {
			System.out.println("test" + Math.random());
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}*/
		
		
		//createG1M("prog.txt", destPath);
		//B2C.main(new String[]{destPath, "TEST"});
	}
	
	public static ArrayList<String> readFromG1M(String g1mpath) throws IOException {
		ArrayList<String> progs = new ArrayList<String>();
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
				
				System.out.println("Found basic program");
				String progName = casioToAscii(g1mparser.getPartName(g1mparser.parts.get(h)));
				String progPw = casioToAscii(g1mparser.parts.get(h).substring(44, 52));

				if (progPw.isEmpty()) {
					progPw = "<no password>";
				}
				System.out.println("Name = " + progName + ", password = " + progPw);
				
				String progContent = casioToAscii(g1mparser.getPartContent(g1mparser.parts.get(h)).substring(10));
				
				if (progName == null || progPw == null || progContent == null) {
					return null;
				}
				
				progs.add("#Program name: "+progName+"\n#Password: " + progPw + "\n"+ progContent);
			}
		}
		System.out.println("Finished reading from g1m");
		return progs;
	}
	
	public static void writeToG1M(String destPath) throws IOException {
		
		System.out.println("Saving to \""+destPath+"\"...");
		
		G1MWrapper g1mwrapper = new G1MWrapper();
		
		List<AsciiProg> parts = new ArrayList<AsciiProg>();
		//Parse text file
		String line;
		AsciiProg currentPart = new AsciiProg("","","");
		/*try {
			BufferedReader br = new BufferedReader(new FileReader(new File(progPath)));
			
			while ((line = br.readLine()) != null) {*/
		for (int h = 0; h < ui.jtp.getTabCount(); h++) {
			
			String[] lines = ((Program)ui.jtp.getComponentAt(h)).textPane.getText().split("\\n|\\r|\\r\\n");
			currentPart = new AsciiProg("","","");
			
			for (int i = 0; i < lines.length; i++) {
				line = lines[i];
				
				if (i == 0 && line.startsWith("#Program name: ")) {
					currentPart.name = line.substring(15);
					
				} else if (i == 1 && line.startsWith("#Password: ")) {
					if (!line.substring(11).equals("<no password>")) {
						currentPart.password = line.substring(11);
					}
				} else {
					if (line.endsWith("Then") || line.endsWith("Else")) {
						line += " ";
					}
					currentPart.content += line + "\n";
				}
			}
			if (currentPart.name.isEmpty()) {
				error("Couldn't find the name of the program \""+ui.jtp.getTitleAt(h) + "\", make sure to include the directive \"#Program name: <name>\" at the beginning.");
				return;
			}
			parts.add(currentPart);
			
		}
		
			/*}
			br.close();
		    
		} catch (Exception e) {
			error(e.getMessage(), lineNb);
		}*/
		//System.out.println(asciiParts);
		
		if (parts.size() == 0) {
			error("No programs detected!");
			return;
		}
		//Add each part (program) of the ascii file
		byte[] padding = {0,0,0,0,0,0,0,0};
		for (int i = 0; i < parts.size(); i++) {
			CasioString password = new CasioString(asciiToCasio(parts.get(i).password, true, parts.get(i).name+".password"));
			if (password.length() > 8) {
				error("Program \""+parts.get(i).name+"\" has a password too long (8 characters max)!");
				return;
			}
			password.add(Arrays.copyOfRange(padding, 0, 8-password.length()));
			CasioString name = new CasioString(asciiToCasio(parts.get(i).name, true, parts.get(i).name+".name"));
			if (name.length() > 8) {
				error("Program \""+parts.get(i).name+"\" has a name too long (8 characters max)!");
				return;
			}
			name.add(Arrays.copyOfRange(padding, 0, 8-name.length()));
			CasioString part = new CasioString(password);
			part.add(new byte[]{0,0});
			part.add(asciiToCasio(parts.get(i).content, false, parts.get(i).name));
			part.add(Arrays.copyOfRange(padding, 0, 4-part.length()%4));
			g1mwrapper.addPart(part, name, BIDE.TYPE_PROG);
		}
		
		g1mwrapper.generateG1M(destPath);
		System.out.println("Finished writing to g1m");
		
	}
	
	public static CasioString asciiToCasio(String content, boolean allowUnknownOpcodes, String progName) {
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
				
				if (lines[h].charAt(i) == '\'') {
					currentPosIsComment = true;
				}
				
				//hex escape
				if (lines[h].startsWith("&#", i)) {
					String hexEsc = lines[h].substring(i+2, lines[h].indexOf(";", i));
					if (hexEsc.length() != 2 && hexEsc.length() != 4) {
						error("Hex escape must be 2 or 4 characters long! (escape: " + hexEsc + ")", h);
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
					if (lines[h].startsWith(opcodes.get(j).ascii, i)) {
						foundMatch = true;
						
						if (opcodes.get(j).hex.length() > 2) {
							result.add(Integer.parseInt(opcodes.get(j).hex.substring(0, 2), 16));
							result.add(Integer.parseInt(opcodes.get(j).hex.substring(2), 16));
						} else if (opcodes.get(j).hex.length() > 0) {
							result.add(Integer.parseInt(opcodes.get(j).hex, 16));
						}
						i += opcodes.get(j).ascii.length()-1;
						//System.out.println("Found opcode \"" + opcodes.get(j).ascii + "\"");
						break;
					}
				}
				
				if (!foundMatch) {
					if ((!allowUnknownOpcodes && !currentPosIsString && !currentPosIsComment) || lines[h].charAt(i) == '&') {
						if (lines[h].charAt(i) != ' ') {
							if (progName.endsWith("password")) {
								i += 11;
								h -= 1;
							} else if (progName.endsWith("name")) {
								i += 15;
								h -= 2;
							}
							error("program \"" + progName+"\", line " + (h+1+2) + ", col " + (i+1) + ": The char '" + lines[h].charAt(i) + 
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
	
	public static String casioToAscii(CasioString content) {
		String allowedCharacters = " !#$%;@^_abcdefghijklmnopqrstuvwxyz|";
		
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
		
		List<String> lines = new ArrayList<String>();
		String tabs = "";
		String currentLine = "";
		int indentLevel = 0;
		int currentIndentLevel = indentLevel;
		boolean unindentCurrentLineAndIndentNext = false;
		
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
				System.out.println("Found end of given string");
				break;
			}
			if (isMultiByte) {
				hex += (content.charAt(i+1) < 0x10 ? "0" : "") + Integer.toHexString(content.charAt(i+1));
			}
			
			
			//Indent
			if (indent.contains(hex)) {
				indentLevel++;
				tabs += "\t";
			}
			
			//Unindent
			if (unindent.contains(hex) && indentLevel > 0) {
				indentLevel--;
				tabs = tabs.substring(1);
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
				
				lines.add(currentLine + (hex.equalsIgnoreCase("C") ? "&disp;" : "") + "\n");
				
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
	/*
	public static void createG1M(String content, String progName) {
		
		//Convert ascii to casio
		for (int i = 0; i < convTable.length/2; i++) {
			//System.out.println(Integer.toHexString((char)convTable[2*i]));
			//Split the character if multi
			String casioChar = (char)(int)Integer.valueOf((String) convTable[2*i], 16) > 0xFF 
					? (String) new String(new char[]{(char)(Integer.valueOf((String)convTable[2*i], 16)/0x100), (char)(Integer.valueOf((String)convTable[2*i], 16) % 0x100)})
					: new String(new char[]{(char)(int)(Integer.valueOf((String)convTable[2*i], 16))});
			content = content.replace((String)convTable[2*i+1], casioChar);
			
		}
		//Padding
		String padding = new String(new char[]{0, 0, 0, 0, 0, 0, 0, 0});
		content += padding.substring(0, 4-content.length()%4);
		
		content = new String(new char[]{0,0,0,0,0,0,0,0,0,0}) + content;
		
		String sizeString = "";
		for (int i = 0; i < 4; i++) {
			sizeString += (char)(int)Integer.valueOf(new String("00000000".substring(
					0, 8-Integer.toHexString(content.length()).length())
					+Integer.toHexString(content.length())).substring(2*i, 2*i+2));
		}
		System.out.println(sizeString);
		//Subheader
		content = "PROGRAM" + new String(new char[]{0,0,0,0,0,0,0,0,0}) + new String(new char[]{0,0,0,1})
				+ "system" + new String(new char[]{0,0}) + progName + padding.substring(0, 8-progName.length())
				+ (char)1 + sizeString + new String(new char[]{0,0,0}) + content;
		
		//Header
		String sizeString2 = "";
		for (int i = 0; i < 4; i++) {
			sizeString2 += (char)(int)Integer.valueOf(new String("00000000".substring(
					0, 8-Integer.toHexString(content.length()+0x20).length())
					+Integer.toHexString(content.length()+0x20)).substring(2*i, 2*i+2));
		}
		String header = "USBPower" + new String(new char[]{0x31, 0x00, 0x10, 0x00, 0x10, 0x00})
				+(char)(sizeString2.charAt(3)+0x41) + (char)0x01 + sizeString2 + (char)((sizeString2.charAt(3)+0xB8)%0x100)
				+new String(new char[]{1, 0xBD, 0xB6, 0xBB, 0xBA, 0xDF, 0x8F, 0x8D, 0x90, 0x98, 0, 1});
		String header2 = new String();
		for (int i = 0; i < header.length(); i++) {
			System.out.println(Integer.toHexString(0xFF-header.charAt(i)));
			header2 += (char)(0xFF-header.charAt(i));
		}
		header2 += (char)(0x90);
		content = header2 + content;
		
		IO.writeToFile(new File("C:\\Users\\Catherine\\Desktop\\locate.g1m"), content, true);
	}*/
	
	public static void getOpcodes() {
		//String relativePath = IO.getRelativeFilePath("opcodes.txt");
		String line;
		int lineNb = 0;
		try {
			//BufferedReader br = new BufferedReader(new FileReader(new File(relativePath)));
			BufferedReader br = new BufferedReader(new InputStreamReader(BIDE.class.getClass().getResourceAsStream("/opcodes.txt")));
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
					error("Could not parse hex string \"" + hex + "\"", lineNb);
				}
				ascii = line.substring(line.indexOf(' ')+1, line.lastIndexOf(' '));
				if (line.substring(line.length()-2, line.length()).equals(" f")) {
					escape = false;
				} else if (line.substring(line.length()-2, line.length()).equals(" t")) {
					escape = true;
				} else {
					error("Unknown boolean \"" + line.substring(line.length()-2, line.length()) + "\"", lineNb);
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
			error(e.getMessage(), lineNb);
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
	
	public static void error(String error, int line) {
		error("line " + line + ": " + error);
	}

}
