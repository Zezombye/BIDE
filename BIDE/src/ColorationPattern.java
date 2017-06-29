import java.awt.Color;
import java.util.regex.Pattern;

public class ColorationPattern {
	public Pattern pattern;
	public Color color;
	public boolean isBold;
	
	public ColorationPattern(String regex, Color color, boolean isBold) {
		this.pattern = Pattern.compile(regex);
		this.color = color;
		this.isBold = isBold;
	}
	
	public ColorationPattern(String[] words, boolean needWordBoundaries, Color color, boolean isBold) {
		String regex = "";
		for (int i = 0; i < words.length; i++) {
			if (needWordBoundaries) 
				regex += "(?<=\\W)";
			else 
				regex += "\\Q";
			regex += words[i];
			if (needWordBoundaries)
				regex += "(?=\\W)";
			else
				regex += "\\E";
			if (i < words.length - 1) {
				regex += "|";
			}
		}
		this.pattern = Pattern.compile(regex);
		this.color = color;
		this.isBold = isBold;
	}
	
}
