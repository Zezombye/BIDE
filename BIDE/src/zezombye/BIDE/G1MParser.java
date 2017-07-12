package zezombye.BIDE;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class G1MParser {
		
	
	String path;
	CasioString fileContent;
	List<CasioString> parts = new ArrayList<CasioString>();
	
	public G1MParser(String path) {
		this.path = path;
		
	}
	
	public void readG1M() throws IOException {
		this.fileContent = IO.readFromFile(path);
	}
	
	public boolean checkValidity() {
		return fileContent.substring(0, 8).equals(new CasioString(new byte[]{(byte)0xAA, (byte)0xAC, (byte)0xBD, (byte)0xAF, (byte)0x90, (byte)0x88, (byte)0x9A, (byte)0x8D}));
	}
	
	public void divideG1MIntoParts() {
		int fileIndex = 32;
		while (fileIndex < fileContent.length()) {
			//Seek the size of the subpart and add size of header which is 44 bytes long
			//Lots of magic numbers in there
			int partSize = 44;
			for (int i = 0; i < 4; i++) {
				partSize += ((fileContent.charAt(fileIndex+37+i)&0xFF)<<(8*(3-i)));
			}
			//fileContent.charAt(fileIndex+37)*16777216 + fileContent.charAt(fileIndex+38)*65536 + fileContent.charAt(fileIndex+39)*256 + fileContent.charAt(fileIndex+40) + 44;
			parts.add(fileContent.substring(fileIndex, fileIndex+partSize));
			fileIndex += partSize;
		}
		//System.out.println(parts);
	}
	
	public CasioString getPartContent(CasioString part) {
		return part.substring(44);
	}
	
	public CasioString getPartName(CasioString part) {
		return part.substring(28, 36);
	}
	
	public int getPartType(CasioString part) {
		int type = part.charAt(36);
		switch (type) {
			case 0x01:
				return BIDE.TYPE_PROG;
			case 0x07:
				return BIDE.TYPE_PICT;
			case 0x0A:
				return BIDE.TYPE_CAPT;
			default:
				System.out.println("Ignoring unknown type 0x0"+Integer.toHexString(type));
				return -1;
		}
	}
}
