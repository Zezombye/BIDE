package zezombye.BIDE;

import java.util.Arrays;

public class Macro {
	String text;
	String replacement;
	boolean isFunction;
	String[] args;
	
	public Macro(String text, String replacement) {
		this(text, replacement, null);
	}
	
	public Macro(String text, String replacement, String[] args) {
		if (args == null) {
			isFunction = false;
		} else {
			isFunction = true;
			this.args = args;
		}
		this.text = text;
		this.replacement = replacement;
	}
	
	@Override public String toString() {
		return "defined "+text+" as "+replacement+", args="+Arrays.toString(args);
	}
}
