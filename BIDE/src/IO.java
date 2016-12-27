//Taken from B2C

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class IO {
	public static void writeToFile(File file, List<Byte> content, boolean deleteFile) {
		try {
			if (deleteFile) {
				file.delete();
			}
			file.createNewFile();
			FileOutputStream out = new FileOutputStream(file.getAbsolutePath());
			//Convert to byte[]
			byte[] result = new byte[content.size()];
			for(int i = 0; i < content.size(); i++) {
			    result[i] = content.get(i).byteValue();
			}
			out.write(result);
			out.close();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
	}
	
	public static List<Byte> readFromRelativeFile(String fileName) {
		byte[] encoded = null;
		try {
			//For some reason it appends a '/' to the beginning of the string, making the file path invalid
			String relativePath = BIDE.class.getClassLoader().getResource(fileName).getPath().substring(1);
			encoded = Files.readAllBytes(Paths.get(relativePath));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//Convert from byte[] to List<Byte>
		List<Byte> content = new ArrayList<Byte>();
		for (int i = 0; i < encoded.length; i++) {
			content.add(encoded[i]);
		}
		
		return content;
	}
	
	public static String readFromFile(String path) {
		/*String content = "";
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "Cp1252"))) {
		    content = br.
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}*/
		String content = "";
		byte[] encoded;
		try {
			encoded = Files.readAllBytes(Paths.get(path));
			content = new String(encoded, "Cp1252");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//due to unicode encoding, some characters get encoded as others

		content = content.replaceAll("\\u2020", new String(new char[]{0x86}));
		content = content.replaceAll("\\u2021", new String(new char[]{0x87}));
		content = content.replaceAll("\\u02C6", new String(new char[]{0x88}));
		content = content.replaceAll("\\u2030", new String(new char[]{0x89}));
		content = content.replaceAll("\\uFFFD", new String(new char[]{0x8F}));
		content = content.replaceAll("\\u2019", new String(new char[]{0x92}));
		content = content.replaceAll("\\u201D", new String(new char[]{0x94}));
		content = content.replaceAll("\\u2122", new String(new char[]{0x99}));
		content = content.replaceAll("\\u0161", new String(new char[]{0x9A}));
		content = content.replaceAll("\\u203A", new String(new char[]{0x9B}));
		
		return content;
	}
}
