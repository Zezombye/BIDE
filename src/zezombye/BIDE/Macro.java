package zezombye.BIDE;

public class Macro {
	String text;
	String replacement;
	
	public Macro(String text, String replacement) {
		this.text = text;
		this.replacement = replacement;
	}
	
	@Override public String toString() {
		return "defined "+text+" as "+replacement;
	}
}
