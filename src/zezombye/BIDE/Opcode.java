package zezombye.BIDE;

public class Opcode {
	String hex;
	String text;
	String unicode;
	int relevance;
	String syntax;
	String description;
	String example;
	String compat;
	
	public Opcode(String hex, String text, String unicode, int relevance, String syntax, String example, String desc, String compatibility) {
		this.hex = hex;
		this.text = text;
		this.unicode = unicode;
		this.relevance = relevance;
		this.syntax = syntax;
		this.example = example;
		this.description = desc;
		this.compat = compatibility;
	}
	
	
	@Override public String toString() {
		return this.hex + " " + this.text + " " + this.relevance;
	}
}