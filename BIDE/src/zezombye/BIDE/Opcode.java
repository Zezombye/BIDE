package zezombye.BIDE;

public class Opcode {
	String hex;
	String text;
	int relevance;
	
	public Opcode(String hex, String text, int relevance) {
		this.hex = hex;
		this.text = text;
		this.relevance = relevance;
	}
	
	@Override public String toString() {
		return this.hex + " " + this.text + " " + this.relevance;
	}
}