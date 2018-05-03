package org.fife.ui.rsyntaxtextarea.modes;

import java.io.*;   
import javax.swing.text.Segment;   
   
import org.fife.ui.rsyntaxtextarea.*;   
   
%%   
   
%public   
%class JavaTokenMaker   
%extends AbstractJFlexCTokenMaker   
%unicode   
%type org.fife.ui.rsyntaxtextarea.Token   
   
/**   
 * A simple TokenMaker example.   
 */   
%{   
   
   /**   
    * Constructor.  This must be here because JFlex does not generate a   
    * no-parameter constructor.   
    */   
   public JavaTokenMaker() {   
   }   
   
   /**   
    * Adds the token specified to the current linked list of tokens.   
    *   
    * @param tokenType The token's type.   
    * @see #addToken(int, int, int)   
    */   
   private void addHyperlinkToken(int start, int end, int tokenType) {   
      int so = start + offsetShift;   
      addToken(zzBuffer, start,end, tokenType, so, true);   
   }   
   
   /**   
    * Adds the token specified to the current linked list of tokens.   
    *   
    * @param tokenType The token's type.   
    */   
   private void addToken(int tokenType) {   
      addToken(zzStartRead, zzMarkedPos-1, tokenType);   
   }   
   
   /**   
    * Adds the token specified to the current linked list of tokens.   
    *   
    * @param tokenType The token's type.   
    * @see #addHyperlinkToken(int, int, int)   
    */   
   private void addToken(int start, int end, int tokenType) {   
      int so = start + offsetShift;   
      addToken(zzBuffer, start,end, tokenType, so, false);   
   }   
   
   /**   
    * Adds the token specified to the current linked list of tokens.   
    *   
    * @param array The character array.   
    * @param start The starting offset in the array.   
    * @param end The ending offset in the array.   
    * @param tokenType The token's type.   
    * @param startOffset The offset in the document at which this token   
    *        occurs.   
    * @param hyperlink Whether this token is a hyperlink.   
    */   
   public void addToken(char[] array, int start, int end, int tokenType,   
                  int startOffset, boolean hyperlink) {   
      super.addToken(array, start,end, tokenType, startOffset, hyperlink);   
      zzStartRead = zzMarkedPos;   
   }   
   
   /**   
    * Returns the text to place at the beginning and end of a   
    * line to "comment" it in a this programming language.   
    *   
    * @return The start and end strings to add to a line to "comment"   
    *         it out.   
    */   
   public String[] getLineCommentStartAndEnd() {   
      return new String[] { "//", null };   
   }   
   
   /**   
    * Returns the first token in the linked list of tokens generated   
    * from <code>text</code>.  This method must be implemented by   
    * subclasses so they can correctly implement syntax highlighting.   
    *   
    * @param text The text from which to get tokens.   
    * @param initialTokenType The token type we should start with.   
    * @param startOffset The offset into the document at which   
    *        <code>text</code> starts.   
    * @return The first <code>Token</code> in a linked list representing   
    *         the syntax highlighted text.   
    */   
   public Token getTokenList(Segment text, int initialTokenType, int startOffset) {   
   
      resetTokenList();   
      this.offsetShift = -text.offset + startOffset;   
   
      // Start off in the proper state.   
      int state = Token.NULL;   
      switch (initialTokenType) {   
                  case Token.COMMENT_MULTILINE:   
            state = MLC;   
            start = text.offset;   
            break;   
   
         /* No documentation comments */   
         default:   
            state = Token.NULL;   
      }   
   
      s = text;   
      try {   
         yyreset(zzReader);   
         yybegin(state);   
         return yylex();   
      } catch (IOException ioe) {   
         ioe.printStackTrace();   
         return new TokenImpl();   
      }   
   
   }   
   
   /**   
    * DELETE THE OTHER ONE (down)
    */   
   private boolean zzRefill() {   
      return zzCurrentPos>=s.offset+s.count;   
   }   
   
   /**   
    * DELETE THE OTHER ONE (down)
    */   
   public final void yyreset(Reader reader) {   
      // 's' has been updated.   
      zzBuffer = s.array;   
      /*   
       * We replaced the line below with the two below it because zzRefill   
       * no longer "refills" the buffer (since the way we do it, it's always   
       * "full" the first time through, since it points to the segment's   
       * array).  So, we assign zzEndRead here.   
       */   
      //zzStartRead = zzEndRead = s.offset;   
      zzStartRead = s.offset;   
      zzEndRead = zzStartRead + s.count - 1;   
      zzCurrentPos = zzMarkedPos = zzPushbackPos = s.offset;   
      zzLexicalState = YYINITIAL;   
      zzReader = reader;   
      zzAtBOL  = true;   
      zzAtEOF  = false;   
   }   
   
%}   
   
Letter                     = [A-Za-z]   
Digit                     = ([0-9])   
AnyCharacterButApostropheOrBackSlash   = ([^\\'])   
AnyCharacterButDoubleQuote   = ([^\"\n])   
NonSeparator                  = ([^\t\f\r\n\ \(\)\{\}\[\]\;\,\.\=\>\<\!\~\?\:\+\-\*\/\&\|\^\%\"\']|"#"|"\\")   
IdentifierStart               = ({Letter}|"_")   
IdentifierPart                  = ({IdentifierStart}|{Digit})   
WhiteSpace            = ([ \t\f]+)   
EntityReference					= ([&][^; \t]*[;]?)
LetterVar = ([A-Z\uE015\u03B8])
CharLiteral               = ([\'][^\:]*[\:\u000A])   
UnclosedCharLiteral         = ([\'][^\:\n]*)   
ErrorCharLiteral         = ({UnclosedCharLiteral}[\'])   


Escape					= ([\\]([abfnrtv\'\"\?\\0e]))
AnyStrChr					= ([^\"\n\\])
StringLiteral			= ([\"]((((("?")*)({Escape}))|{AnyStrChr})*)(("?")*)[\"])
//StringLiteral            = ([\"]({AnyCharacterButDoubleQuote})*[\"\n])   
//StringLiteral            = ([\"](\\\\.|[^\"\\n])*[\"])   
UnclosedStringLiteral      = ([\"]([\\].|[^\\\"])*[^\"]?)   
ErrorStringLiteral         = ({UnclosedStringLiteral}[\"])   
//PictureBorder = (\u2580{130}\n\u2588)|(\u2588\n\u2584{130})|(\u2588\n\u2588)
   
MLCBegin               = "/*"   
MLCEnd               = "*/"   
LineCommentBegin         = "'"   

recurVar = ([abc](0|1|2|\uE027|\uE028|\uE029|nStart))
   
IntegerLiteral         = ({Digit}+)   
ErrorNumberFormat         = (({IntegerLiteral}){NonSeparator}+)   
   
Separator               = ([\(\)\{\}\[\]])   
Separator2            = ([\;,.])   
   
Identifier            = ({IdentifierStart}{IdentifierPart}*)   
   
%state MLC   
   
%%   
   
<YYINITIAL> {   
   
   
   /* Keywords */   
   "=>" |   
   ":" |   
   "&disp;" |   
   "\u25E2" |   
   "If" |   
   "Else" | 
   "Then" | 
   "IfEnd" | 
   "For" | 
   "To" | 
   "Step" | 
   "Next" | 
   "Do" | 
   "LpWhile" | 
   "While" | 
   "WhileEnd" | 
   "Prog" |
   "Return" | 
   "Stop" | 
   "Break" | 
   "Goto" | 
   "Lbl"    { addToken(Token.RESERVED_WORD); }  

   /*Operator keywords*/
	"And" | "Or" | "Xor" | "Not" | "Rmdr" |
	"&neg;" | "&E;" | "&_10;" | "&sqrt;" | "&cbrt;" | "&nth_root;" |
	"&nCr;" | "&nPr;" |
	"&femto;" | "&pico;" | "&nano;" | "&micro;" | "&milli;" |
	"&kilo;" | "&mega;" | "&giga;" | "&tera;" | "&peta;" | "&exa;" |
	"&frac;" | "&^-1;" | "&^2;" | "&angle;"
	{addToken(Token.RESERVED_WORD_2);}
   
   /* Variables */   
   "Mat" |    
   "List" |   
   "Str"  |
   "Vct" |
	"Ans" |
	"&r;" |
	"&theta;" |
	{recurVar} | {LetterVar} { addToken(Token.VARIABLE); }   
   
   /*Functions that could be colored wrong, such as F-line*/
   //There are more opcodes but I'm too lazy to put them all
   "S-L-Normal" | "S-L-Thick" | "S-L-Broken" | "S-L-Dot" |
   "S-Gph1" | "S-Gph2" | "S-Gph3" |
   "S-WindAuto" | "S-WindMan" |
   "G-Connect" | "G-Plot" |
   "F-Line" |
   "S-L-Thin" |
   "List->Mat(" | "Mat->List(" |
   /*"->Simp" |
   "[s-yr]" | "[t-yr]" | "[l-atm]" |
   "DrawFTG-Con" | "DrawFTG-Plt" |
   "DrawR-Con" | "DrawR-Plt" |
   "DrawRSum-Con" | "DrawRSum-Plt*/
   "1-Variable" | "2-Variable" |
   "S-Gph1" | "S-Gph2" | "S-Gph3" |
   "BG-None" | "BG-Pict" |
   "StoV-Win" | "RclV-Win" |
   "Exp->Str"
   {addToken(Token.FUNCTION);}
   
   {Identifier}            { addToken(Token.IDENTIFIER); }   
   
   //Not actually a data type
	//{PictureBorder} {addToken(Token.DATA_TYPE);}
   
   {WhiteSpace}            { addToken(Token.WHITESPACE); }   
   
   /* String/Character literals. */   
   {CharLiteral}            { addToken(Token.LITERAL_CHAR); }   
   {UnclosedCharLiteral}      { addToken(Token.ERROR_CHAR); addNullToken(); return firstToken; }   
   //{ErrorCharLiteral}         { addToken(Token.ERROR_CHAR); }   
   {StringLiteral}            { addToken(Token.LITERAL_STRING_DOUBLE_QUOTE); }   
   {EntityReference}			{ addToken(Token.MARKUP_ENTITY_REFERENCE); }
   {UnclosedStringLiteral}      { addToken(Token.ERROR_STRING_DOUBLE); addNullToken(); return firstToken; }   
   {ErrorStringLiteral}      { addToken(Token.ERROR_STRING_DOUBLE); }   
   
   /* Comment literals. */   
   //{MLCBegin}               { start = zzMarkedPos-2; yybegin(MLC); }   
   //{LineCommentBegin}.*    { addToken(Token.COMMENT_EOL); addNullToken(); return firstToken; }   
   /* Preprocessor directives */
   "#"[^( ].*	{ addToken(Token.PREPROCESSOR); addNullToken(); return firstToken; }
   
   /* Separators. */   
   //{Separator}               { addToken(Token.SEPARATOR); }   
   //{Separator2}            { addToken(Token.IDENTIFIER); }   
   
   /* Operators. */   
   "!" | "*" | "+" | "-" | "!=" |
   "/" | ":" | "<" | "=" | ">" | "?" | "^" | "~" | "%" |
	"\uE064" | "\uE00D" | "\uE00F" | "\uE000" | "\uE001" | "\uE002" | "\uE003" | "\uE004" | "\uE005" | 
	"\u00B2" | "\uFE63" | "\u221A" | "\u2220" | "\uE02D" | "\uE008" | "\uE01B" | "\u231F" | "\uE02B" |
	"\u00B5"
	{ addToken(Token.OPERATOR); }
   
   /*Operator keywords*/
   /*"And" | 
   "Or" | 
   "Xor" | 
   "Not" | 
   "&neg;" | 
   "&*10^;" | 
   "&_10;" | 
   "&sqrt;" | 
   "&cbrt;" | 
   "&nth_root;" { addToken(Token.RESERVED_WORD); } */  
   
   
   /* Numbers */   
   //{IntegerLiteral}         { addToken(Token.LITERAL_NUMBER_DECIMAL_INT); }   
   //{ErrorNumberFormat}         { addToken(Token.ERROR_NUMBER_FORMAT); }   
   
   /* Ended with a line not in a string or comment. */   
   \n |   
   <<EOF>>                  { addNullToken(); return firstToken; }   
   
   /* Catch any other (unhandled) characters. */   
   .                     { addToken(Token.IDENTIFIER); }   
   
}   
   
<MLC> {   
   [^\n*]+            {}   
   {MLCEnd}         { yybegin(YYINITIAL); addToken(start,zzStartRead+2-1, Token.COMMENT_MULTILINE); }   
   "*"               {}   
   \n |   
   <<EOF>>            { addToken(start,zzStartRead-1, Token.COMMENT_MULTILINE); return firstToken; }   
}   