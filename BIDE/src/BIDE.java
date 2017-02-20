import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import b2c.B2C;

public class BIDE {
	
	/* Left: casio characters, right: ascii characters
	 * Example: 0x89 => "+"
	 * 
	 * Order by numerical value of the casio character when possible.
	 * For example, WhileEnd/LpWhile have to be before While, else they are not properly replaced.
	 */     
	public static final String[] convTable = {
			"0C", "&disp;",
			"0D", "\n",
			"0E", "->",
			"0F", "x10^",
			"10", "<=",
			"11", "!=",
			"12", ">=",
			"13", "=>",
			"26", "&amp;",
			"27", "//",
			"86", "&sqrt;",
			"87", "&neg;",
			"89", "+",
			"8B", "^2",
			"99", "-",
			"9B", "^-1",
			"A5", "e^",
			"A8", "^",
			"A9", "*",
			"AB", "&fact;",
			"B9", "/",
			"C0", "Ans",
			"C1", "Ran#",
			"CD", "&radius;",
			"CE", "&theta;",
			"D0", "&pi;",
			"7F40", "Mat ",
			"7F51", "List ",
			"7F87", "RanInt#(",
			"7F8F", "GetKey",
			"7FB0", " And ",
			"7FB3", "Not ",
			"F700", "If ",
			"F701", "Then ",
			"F702", "Else ",
			"F703", "IfEnd",
			"F704", "For ",
			"F705", " To ",
			"F706", " Step ",
			"F707", "Next",
			"F70B", "LpWhile ",
			"F709", "WhileEnd",
			"F708", "While ",
			"F70A", "Do",
			"F70C", "Return",
			"F710", "Locate ",
			
	};
	
	public static void main(String[] args) {
		String destPath = "C:\\Users\\Catherine\\Desktop\\test.g1m";
		/*CasioString test = new CasioString(new byte[]{(byte) 0xFF, 0x00, 0x30, (byte) 0x90});
		test.replace(new byte[]{0x00, 0x30}, new byte[]{'A', 'B', 'C'});
		IO.writeToFile(new File("C:\\Users\\Catherine\\Desktop\\locate.g1m"), test.getContent(), true);*/
		createG1M("prog.txt", destPath);
		B2C.main(new String[]{destPath, "TEST"});
	}
	
	public static void createG1M(String path, String destPath) {
		
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
	
	public static List<Byte> byteArrayToList(byte[] b) {
		List<Byte> result = new ArrayList<Byte>();
		for (int i = 0; i < b.length; i++) {
			result.add(b[i]);
		}
		return result;
	}

}
