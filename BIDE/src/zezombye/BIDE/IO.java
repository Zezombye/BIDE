package zezombye.BIDE;
//Taken from B2C

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * ONLY EVER USE CASIOSTRINGS WHEN HANDLING CASIO ENCODING!
 * The reason is that Strings use UTF-16, and invalid encodings are replaced by '?'
 * Try to create the string with byte[]{0xAA, 0xAC, 0xBD, 0xAF, 0x90, 0x88, 0x9A, 0x8D}.
 * You'll see that some characters are replaced by '?'.
 */

public class IO {
	public static void writeToFile(File file, List<Byte> content, boolean deleteFile) throws IOException {
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
	
	//Only use this for non-casio strings (ascii text)!
	public static void writeStrToFile(File file, String content, boolean deleteFile) {
		try {
			if (deleteFile) {
				file.delete();
			}
			Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "utf-8"));
			writer.write(content);
			writer.close();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
	}
	
	/*public static CasioString readFromRelativeFile(String fileName) {
		byte[] encoded = null;
		try {
			//For some reason it appends a '/' to the beginning of the string, making the file path invalid
			String relativePath = getRelativeFilePath(fileName);
			encoded = new BufferedReader(new InputStreamReader(new InputStream(getClass().getResourceAsStream("/opcodes.txt"))));
		} catch (IOException e) {
			e.printStackTrace();
		}
				
		return new CasioString(encoded);
	}
	
	public static String getRelativeFilePath(String fileName) {
		//return BIDE.class.getClass().getResource(fileName).getPath().substring(0);
		return "./" + fileName;
	}*/
	
	public static CasioString readFromFile(String path) throws IOException {
		/*String content = "";
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "Cp1252"))) {
		    content = br.
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}*/
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		
		
		//due to unicode encoding, some characters get encoded as others
		/*
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
		*/
		return new CasioString(encoded);
	}
}
