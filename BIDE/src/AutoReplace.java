import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public class AutoReplace extends DocumentFilter {
	
	@Override
	public void replace(final FilterBypass fb, final int offs, final int length, final String str, final AttributeSet a) throws BadLocationException {
	    if (str.equals("\\"))
	        super.replace(fb, offs, length, "&#92", a);
	    else
	        super.replace(fb, offs, length, str, a);
	}
	
}
