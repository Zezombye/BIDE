package zezombye.BIDE;

public class Opcode {
	String hex;
	String text;
	
	public Opcode(String hex, String text) {
		this.hex = hex;
		this.text = text;
	}
	
	@Override public String toString() {
		return this.hex + " " + this.text;
	}
}
