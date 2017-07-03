import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class SyntaxColoration {
	
	//These keywords need word boundaries
	public String[] keywords = {
			"If",
			"Else",
			"Then",
			"IfEnd",
			"For",
			"To",
			"Step",
			"Next",
			"Do",
			"LpWhile",
			"While",
			"WhileEnd",
			"Return",
			"Stop",
			"Break",
			"Goto",
			"Lbl",
	};
	
	//These don't
	public String[] keywords2 = {
		"=>",
		":",
	};
	
	//colored after entities
	public String[] keywords3 = {
		"&disp;",
	};
	
	//Operators are colored before the keywords because they have no restrictions
	public String[] operators = {
		"~",
		"+", "-", "*", "/", "^",
		//"[", "]",
		//"{", "}",
		//"(", ")",
		"<", ">", "=", "!=",
		//",",
		" And ", " Or ", " Xor ", "Not ",
	};
	
	//colored after entities
	public String[] operators2 = {
		"&neg;",
		"&x10^;",
		"&_10;",
		"&sqrt;",
		"&nth_root;",
	};
	
	public String[] variables = {
		"List",
		"Mat",
		"Ans",
		"Str",
		"[A-Z]",
	};
	
	//colored after entities
	public String[] variables2 = {
		"&theta;",
		"&r;",
		"&pi;",
		"&i;",
	};
	
	public final Color keywordColor = new Color(0,0,255); //blue
	public final Color operatorColor = new Color(0, 128, 255); //light blue
	public final Color variableColor = new Color(128, 0, 255); //purple
	public final Color borderColor = Color.GRAY;
	public final Color strColor = new Color(128, 128, 128); //gray
	public final Color entityColor = new Color(255, 128, 0); //orange
	public final Color commentColor = new Color(0, 128, 0); //dark green
	public final Color preprocessorColor = new Color(128, 64, 0);
	
	public ColorationPattern[] getColorationPatterns(int type) {
		List<ColorationPattern> cps = new ArrayList<ColorationPattern>();
	
		if (type == BIDE.TYPE_PROG) {
			//cps.add(new ColorationPattern(operators, false, operatorColor, false));
			//cps.add(new ColorationPattern(keywords, true, keywordColor, true));
			//cps.add(new ColorationPattern(keywords2, false, keywordColor, true));
			//cps.add(new ColorationPattern(variables, true, variableColor, false));
		}
		
		if (type == BIDE.TYPE_PROG || type == BIDE.TYPE_CAPT || type == BIDE.TYPE_PICT) {
			//Comments
			cps.add(new ColorationPattern("'.*?\\n", commentColor, false));
		}
		
		if (type == BIDE.TYPE_PROG) {
			//String
			cps.add(new ColorationPattern("\"(\\\\.|[^\"\\n])*\"", strColor, false));
		}
		
		if (type == BIDE.TYPE_PROG || type == BIDE.TYPE_OPCODE) {
			//Entities
			cps.add(new ColorationPattern("&.+?;", entityColor, false));
			cps.add(new ColorationPattern(keywords3, false, entityColor, false));
			cps.add(new ColorationPattern(operators2, false, entityColor, false));
			cps.add(new ColorationPattern(variables2, false, entityColor, false));
		}
		
		if (type == BIDE.TYPE_PICT || type == BIDE.TYPE_CAPT) {
			cps.add(new ColorationPattern("(▀{130})|(▄{130})|(█(?=\\n))|((?<=\\n)█)", borderColor, false));
		}
		
		if (type == BIDE.TYPE_OPCODE) {
			//cps.add(new ColorationPattern("[^ -~]", commentColor, true));
			//cps.add(new ColorationPattern("(?<=\\n)\\w+", variableColor, false));
			//cps.add(new ColorationPattern("[tf](?=\\n)", keywordColor, false));
		}
		
		
		//Preprocessor
		cps.add(new ColorationPattern("(^|\\n)#.*?(?=\\n)", preprocessorColor, false));

		return cps.toArray(new ColorationPattern[cps.size()]);
	}
	
}
