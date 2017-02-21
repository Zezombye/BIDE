import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import b2c.B2C;

public class BIDE {
	
	public static List<Opcode> opcodes = new ArrayList<Opcode>();
	
	public static void main(String[] args) {
		
		boolean casioToAscii = false;
		
		getOpcodes();
		
		if (casioToAscii) {
			readFromG1M("C:\\Users\\Catherine\\Desktop\\demnr.g1m", "C:\\Users\\Catherine\\Desktop\\prog.txt");
		} else {
			writeToG1M("C:\\Users\\Catherine\\Desktop\\prog.txt", "C:\\Users\\Catherine\\Desktop\\demnr.g1m");
		}
		
		
		//createG1M("prog.txt", destPath);
		//B2C.main(new String[]{destPath, "TEST"});
	}
	
	public static void readFromG1M(String g1mpath, String destPath) {
		String result = "";
		G1MParser g1mparser = new G1MParser(g1mpath);
		g1mparser.divideG1MIntoParts();
		for (int h = 0; h < g1mparser.parts.size(); h++) {
			
			if (g1mparser.getPartType(g1mparser.parts.get(h)) == g1mparser.TYPE_PROG) {
				
				String progName = casioToAscii(g1mparser.getPartName(g1mparser.parts.get(h)));
				String progPw = casioToAscii(g1mparser.parts.get(h).substring(44, 52));
				if (progPw.isEmpty()) {
					progPw = "<no password>";
				}
				if (h > 0) {
					result += "\n";
				}
				result += "#\n#Program name: "+progName+"\n#Password: " + progPw + "\n#\n";
				
				result += casioToAscii(g1mparser.getPartContent(g1mparser.parts.get(h)).substring(10));
				result += "\n#End of program";
			}
		}
				
		IO.writeToFile(new File(destPath), byteArrayToList(result.getBytes()), true);
	}
	
	public static void writeToG1M(String progpath, String destPath) {
		
		G1MWrapper g1mwrapper = new G1MWrapper();
		
		
		
		CasioString content = new CasioString(IO.readFromRelativeFile("prog.txt"));
		CasioString progName = content.substring(14, content.indexOf('\n'));
		//System.out.println(progName);
		content = content.substring(content.indexOf('\n')+1);
		
		//Convert ascii to casio
		for (int i = 0; i < convTable.length/2; i++) {
			//System.out.println(Integer.toHexString((char)convTable[2*i]));
			//Split the character if multi
			List<Byte> casioChar = (int)Integer.valueOf(convTable[2*i], 16) > 0xFF 
					? byteArrayToList(new byte[]{(byte)(Integer.valueOf((String)convTable[2*i], 16)/0x100), (byte)(Integer.valueOf((String)convTable[2*i], 16) % 0x100)})
					: byteArrayToList(new byte[]{(byte)(int)(Integer.valueOf((String)convTable[2*i], 16))});
			content.replace(((String)convTable[2*i+1]).getBytes(), casioChar);
			
		}
		//Padding
		byte[] padding = {0, 0, 0, 0, 0, 0, 0, 0};
		content.add(Arrays.copyOfRange(padding, 0, 4-(content.length()+0x56)%4));
		
		content.add(0, new byte[]{0,0,0,0,0,0,0,0,0,0});
		
		CasioString sizeString = new CasioString(new byte[]{0,0,0,0});
		for (int i = 0; i < 4; i++) {
			sizeString.setCharAt(i, (byte)(content.length()>>(8*(3-i))));
		}
		//System.out.println(sizeString);
		//Subheader
		CasioString subheader = new CasioString(new byte[]{'P','R','O','G','R','A','M',0,0,0,0,0,0,0,0,0, 0,0,0,1, 's','y','s','t','e','m', 0,0});
		subheader.add(progName.getContent());
		subheader.add(Arrays.copyOfRange(padding, 0, 8-progName.length()));
		subheader.add(1);
		subheader.add(sizeString);
		subheader.add(new byte[]{0,0,0});
		content.add(0, subheader);
		
		//Header
		CasioString sizeString2 = new CasioString(new byte[]{0,0,0,0});
		
		for (int i = 0; i < 4; i++) {
			sizeString2.setCharAt(i, (byte)((content.length()+0x20)>>(8*(3-i))));
		}
		CasioString header = new CasioString(new byte[]{'U','S','B','P','o','w','e','r', 0x31, 0,0x10,0,0x10,0, (byte)(sizeString2.charAt(3)+0x41), 1});
		header.add(sizeString2);
		header.add((sizeString2.charAt(3)+0xB8)%0x100);
		header.add(new byte[]{(byte)0xBD, (byte)0xB6, (byte)0xBB, (byte)0xBA, (byte)0xDF, (byte)0x8F, (byte)0x8D, (byte)0x90, (byte)0x98, 0, 1});
		//header.add(new byte[]{(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0x00,1});
		CasioString header2 = new CasioString();
		for (int i = 0; i < header.length(); i++) {
			//System.out.println(Integer.toHexString(0xFF-header.charAt(i)));
			header2.add(0xFF-header.charAt(i));
		}
		content.add(0, header2);
		IO.writeToFile(new File(destPath), content.getContent(), true);
		
	}
	
	public static String casioToAscii(String content) {
		String allowedCharacters = " !#$%&;@_abcdefghijklmnopqrstuvwxyz|";
		
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
			boolean isMultiByte = isMultibytePrefix(content.charAt(i));
			boolean foundMatch = false;
			
			//Allow characters that are not in the opcodes
			if (allowedCharacters.contains(""+content.charAt(i))) {
				currentLine += content.charAt(i);
				continue;
			}
			String hex = Integer.toHexString(content.charAt(i));
			if (hex.equals("0")) {
				System.out.println("Found end of program");
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
			if (hex.equalsIgnoreCase("D")) {
				
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
				if (content.substring(i, i+3).equals(new String(new char[]{0x0D, 0xF7, 0x01}))) {
					currentLine += " :Then";
					i += 2;
				}
					
				lines.add(currentLine + "\n");
				
				currentLine = "";
				currentIndentLevel = indentLevel;
				continue;
			}
			
			System.out.println("Testing for opcode " + hex);
			for (int j = 0; j < opcodes.size(); j++) {
				if (hex.equalsIgnoreCase(opcodes.get(j).hex)) {
					currentLine += opcodes.get(j).ascii;
					foundMatch = true;
					System.out.println("Matches opcode " + opcodes.get(j).ascii);
					break;
				}
			}
			
			if (hex.equals("f702")) {
				unindentCurrentLineAndIndentNext = true;
				currentLine += "\n" + tabs;
			}
			
			if (!foundMatch) {
				error("Unknown opcode " + hex);
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
		String relativePath = IO.getRelativeFilePath("opcodes.txt");
		String line;
		int lineNb = 0;
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(relativePath)));
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
				opcodes.add(new Opcode(hex, ascii.replaceAll(" +?$", "")));
		       
		       
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
		opcodes.add(new Opcode("", " "));
		
		//Add newline
		opcodes.add(new Opcode("D", "\n"));
		
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
	
	public static boolean isMultibytePrefix(char prefix) {
		if (prefix == (char)0xF7 ||
				prefix == (char)0x7F ||
				prefix == (char)0xF9 ||
				prefix == (char)0xE5 ||
				prefix == (char)0xE6 ||
				prefix == (char)0xE7)
			return true;
		return false;
	}
	
	public static void error(String error) {
		System.out.println("\nERROR: "+error+"\n");
		System.exit(0);
	}
	
	public static void error(String error, int line) {
		error("line " + line + ": " + error);
	}

}
