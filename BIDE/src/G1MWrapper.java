import java.util.ArrayList;
import java.util.List;

public class G1MWrapper {
	
	public final int TYPE_PROG = 0;
	public final int TYPE_LIST = 1;
	public final int TYPE_MAT = 2;
	public final int TYPE_PICT = 3;
	public final int TYPE_CAPT = 4;
	
	public List<String> parts = new ArrayList<String>();
	
	public void addPart(String part, int type) {
		
	}
	
	public int getPartID(int part) {
		switch (part) {
			case TYPE_PROG:
				return 0x01;
			case TYPE_LIST:
				return 0x05;
			case TYPE_MAT:
				return 0x06;
			case TYPE_PICT:
				return 0x07;
			case TYPE_CAPT:
				return 0x0A;
			default:
				BIDE.error("Unknown part "+ part);
				return -1;
		}
	}
	
}
