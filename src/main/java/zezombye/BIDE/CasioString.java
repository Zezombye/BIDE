package zezombye.BIDE;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class CasioString {
	private List<Byte> content = new ArrayList<Byte>();
	
	public CasioString() {}
	
	public CasioString(String str) {
		setContent(byteArrayToList(str.getBytes()));
	}
	
	public CasioString(CasioString str) {
		setContent(str.getContent());
	}
	
	public CasioString(byte[] b) {
		setContent(byteArrayToList(b));
	}
	
	public CasioString(List<Byte> b) {
		setContent(b);
	}
	
	public int length() {
		return this.content.size();
	}
	
	public short charAt(int index) {
		return (short)(this.content.get(index)&0xFF);
	}
	
	public int indexOf(byte char_) {
		for (int i = 0; i < this.length(); i++) {
			if (this.content.get(i) == char_) {
				return i;
			}
		}
		return -1;
	}
	public int indexOf(char c) {return indexOf((byte)c);}
	
	public CasioString substring(int fromIndex, int toIndex) {
		return new CasioString(new ArrayList<Byte>(this.content.subList(fromIndex, toIndex)));
	}
	public CasioString substring(int fromIndex) {return substring(fromIndex, this.length());}
	
	public void setCharAt(int index, byte char_) {
		content.set(index, char_);
	}
	
	public boolean endsWith(CasioString str) {
		if (this.length() < str.length()) return false;
		return this.substring(this.length()-str.length()).equals(str);
	}
	
	public void add(int i) {this.content.add((byte)i);}
	public void add(byte b) {this.content.add(b);}
	
	public void add(List<Byte> lb) {
		this.content.addAll(lb);
	}
	public void add(byte[] byteArray) {add(byteArrayToList(byteArray));}
	public void add(CasioString str) {add(str.getContent());}
	public void add(String str) {add(byteArrayToList(str.getBytes()));}
	public void add(Byte[] byteArray) {
		add(Arrays.asList(byteArray));
	}
	
	public void add(int index, List<Byte> lb) {
		this.content.addAll(index, lb);
	}
	public void add(int index, byte[] byteArray) {add(index, byteArrayToList(byteArray));}
	public void add(int index, CasioString str) {add(index, str.getContent());}
	
	/**
	 * Replace the contents of the CasioString that matches target with the replacement.
	 * Unlike String's replace method, it does not need to be assigned to the string.
	 * 
	 * @param target The string to replace.
	 * @param replacement The string used as replacement.
	 */
	public void replace(List<Byte> target, List<Byte> replacement) {
		for (int i = 0; i < this.content.size()-target.size()+1; i++) {
			if (this.content.subList(i, i+target.size()).equals(target)) {
				this.content.subList(i, i+target.size()).clear();
				this.content.addAll(i, replacement);
				i += replacement.size()-1;
			}
		}
	}
	
	public void replace(byte[] target, byte[] replacement) {replace(byteArrayToList(target), byteArrayToList(replacement));}
	public void replace(List<Byte> target, byte[] replacement) {replace(target, byteArrayToList(replacement));}
	public void replace(byte[] target, List<Byte> replacement) {replace(byteArrayToList(target), replacement);}
	
	public List<Byte> byteArrayToList(byte[] b) {
		List<Byte> result = new ArrayList<Byte>();
		for (int i = 0; i < b.length; i++) {
			result.add(b[i]);
		}
		return result;
	}
	
	public byte[] listToByteArray(List<Byte> list) {
	    byte[] result = new byte[list.size()];
	    for (int i = 0; i < result.length; i++) {
	        result[i] = list.get(i);
	    }
	    return result;
	}

	public List<Byte> getContent() {
		return this.content;
	}

	public void setContent(List<Byte> content) {
		this.content = new ArrayList<Byte>(content);
	}
	
	@Override
	public String toString() {
		return new String(listToByteArray(this.getContent()));
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj.getClass() != CasioString.class) {
			return false;
		}
		CasioString cs = (CasioString)obj;
		return cs.getContent().equals(this.getContent());
	}


	
}
