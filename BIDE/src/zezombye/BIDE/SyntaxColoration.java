package zezombye.BIDE;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.text.Segment;

import org.fife.ui.rsyntaxtextarea.AbstractTokenMaker;
import org.fife.ui.rsyntaxtextarea.RSyntaxUtilities;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenMap;

public class SyntaxColoration extends AbstractTokenMaker {
	
	/*//These keywords need word boundaries
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
			cps.add(new ColorationPattern(operators, false, operatorColor, false));
			cps.add(new ColorationPattern(keywords, true, keywordColor, true));
			cps.add(new ColorationPattern(keywords2, false, keywordColor, true));
			cps.add(new ColorationPattern(variables, true, variableColor, false));
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
	}*/
	
	@Override
	public Token getTokenList(Segment text, int startTokenType, int startOffset) {
		   resetTokenList();

		   char[] array = text.array;
		   int offset = text.offset;
		   int count = text.count;
		   int end = offset + count;

		   // Token starting offsets are always of the form:
		   // 'startOffset + (currentTokenStart-offset)', but since startOffset and
		   // offset are constant, tokens' starting positions become:
		   // 'newStartOffset+currentTokenStart'.
		   int newStartOffset = startOffset - offset;

		   int currentTokenStart = offset;
		   int currentTokenType = startTokenType;

		   for (int i=offset; i<end; i++) {

		      char c = array[i];

		      switch (currentTokenType) {

		         case Token.NULL:

		            currentTokenStart = i;   // Starting a new token here.

		            switch (c) {

		               case ' ':
		               case '\t':
		                  currentTokenType = Token.WHITESPACE;
		                  break;

		               case '"':
		                  currentTokenType = Token.LITERAL_STRING_DOUBLE_QUOTE;
		                  break;

		               case '#':
		                  currentTokenType = Token.COMMENT_EOL;
		                  break;

		               default:
		                  if (RSyntaxUtilities.isDigit(c)) {
		                     currentTokenType = Token.LITERAL_NUMBER_DECIMAL_INT;
		                     break;
		                  }
		                  else if (RSyntaxUtilities.isLetter(c) || c=='/' || c=='_') {
		                     currentTokenType = Token.IDENTIFIER;
		                     break;
		                  }
		                  
		                  // Anything not currently handled - mark as an identifier
		                  currentTokenType = Token.IDENTIFIER;
		                  break;

		            } // End of switch (c).

		            break;

		         case Token.WHITESPACE:

		            switch (c) {

		               case ' ':
		               case '\t':
		                  break;   // Still whitespace.

		               case '"':
		                  addToken(text, currentTokenStart,i-1, Token.WHITESPACE, newStartOffset+currentTokenStart);
		                  currentTokenStart = i;
		                  currentTokenType = Token.LITERAL_STRING_DOUBLE_QUOTE;
		                  break;

		               case '#':
		                  addToken(text, currentTokenStart,i-1, Token.WHITESPACE, newStartOffset+currentTokenStart);
		                  currentTokenStart = i;
		                  currentTokenType = Token.COMMENT_EOL;
		                  break;

		               default:   // Add the whitespace token and start anew.

		                  addToken(text, currentTokenStart,i-1, Token.WHITESPACE, newStartOffset+currentTokenStart);
		                  currentTokenStart = i;

		                  if (RSyntaxUtilities.isDigit(c)) {
		                     currentTokenType = Token.LITERAL_NUMBER_DECIMAL_INT;
		                     break;
		                  }
		                  else if (RSyntaxUtilities.isLetter(c) || c=='/' || c=='_') {
		                     currentTokenType = Token.IDENTIFIER;
		                     break;
		                  }

		                  // Anything not currently handled - mark as identifier
		                  currentTokenType = Token.IDENTIFIER;

		            } // End of switch (c).

		            break;

		         default: // Should never happen
		         case Token.IDENTIFIER:

		            switch (c) {

		               case ' ':
		               case '\t':
		                  addToken(text, currentTokenStart,i-1, Token.IDENTIFIER, newStartOffset+currentTokenStart);
		                  currentTokenStart = i;
		                  currentTokenType = Token.WHITESPACE;
		                  break;

		               case '"':
		                  addToken(text, currentTokenStart,i-1, Token.IDENTIFIER, newStartOffset+currentTokenStart);
		                  currentTokenStart = i;
		                  currentTokenType = Token.LITERAL_STRING_DOUBLE_QUOTE;
		                  break;

		               default:
		                  if (RSyntaxUtilities.isLetterOrDigit(c) || c=='/' || c=='_') {
		                     break;   // Still an identifier of some type.
		                  }
		                  // Otherwise, we're still an identifier (?).

		            } // End of switch (c).

		            break;

		         case Token.LITERAL_NUMBER_DECIMAL_INT:

		            switch (c) {

		               case ' ':
		               case '\t':
		                  addToken(text, currentTokenStart,i-1, Token.LITERAL_NUMBER_DECIMAL_INT, newStartOffset+currentTokenStart);
		                  currentTokenStart = i;
		                  currentTokenType = Token.WHITESPACE;
		                  break;

		               case '"':
		                  addToken(text, currentTokenStart,i-1, Token.LITERAL_NUMBER_DECIMAL_INT, newStartOffset+currentTokenStart);
		                  currentTokenStart = i;
		                  currentTokenType = Token.LITERAL_STRING_DOUBLE_QUOTE;
		                  break;

		               default:

		                  if (RSyntaxUtilities.isDigit(c)) {
		                     break;   // Still a literal number.
		                  }

		                  // Otherwise, remember this was a number and start over.
		                  addToken(text, currentTokenStart,i-1, Token.LITERAL_NUMBER_DECIMAL_INT, newStartOffset+currentTokenStart);
		                  i--;
		                  currentTokenType = Token.NULL;

		            } // End of switch (c).

		            break;

		         case Token.COMMENT_EOL:
		            i = end - 1;
		            addToken(text, currentTokenStart,i, currentTokenType, newStartOffset+currentTokenStart);
		            // We need to set token type to null so at the bottom we don't add one more token.
		            currentTokenType = Token.NULL;
		            break;

		         case Token.LITERAL_STRING_DOUBLE_QUOTE:
		            if (c=='"') {
		               addToken(text, currentTokenStart,i, Token.LITERAL_STRING_DOUBLE_QUOTE, newStartOffset+currentTokenStart);
		               currentTokenType = Token.NULL;
		            }
		            break;

		      } // End of switch (currentTokenType).

		   } // End of for (int i=offset; i<end; i++).

		   switch (currentTokenType) {

		      // Remember what token type to begin the next line with.
		      case Token.LITERAL_STRING_DOUBLE_QUOTE:
		         addToken(text, currentTokenStart,end-1, currentTokenType, newStartOffset+currentTokenStart);
		         break;

		      // Do nothing if everything was okay.
		      case Token.NULL:
		         addNullToken();
		         break;

		      // All other token types don't continue to the next line...
		      default:
		         addToken(text, currentTokenStart,end-1, currentTokenType, newStartOffset+currentTokenStart);
		         addNullToken();

		   }

		   // Return the first token in our linked list.
		   return firstToken;

	}

	@Override
	public TokenMap getWordsToHighlight() {
		   TokenMap tokenMap = new TokenMap();
		   
		   tokenMap.put("case",  Token.RESERVED_WORD);
		   tokenMap.put("for",   Token.RESERVED_WORD);
		   tokenMap.put("if",    Token.RESERVED_WORD);
		   tokenMap.put("while", Token.RESERVED_WORD);
		  
		   tokenMap.put("printf", Token.FUNCTION);
		   tokenMap.put("scanf",  Token.FUNCTION);
		   tokenMap.put("fopen",  Token.FUNCTION);
		   
		   return tokenMap;
	}
	
}
