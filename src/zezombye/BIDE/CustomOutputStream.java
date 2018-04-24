package zezombye.BIDE;
import java.awt.TextArea;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextArea;
 
/**
 * This class extends from OutputStream to redirect output to a JTextArrea
 * @author www.codejava.net
 *
 */
public class CustomOutputStream extends OutputStream {
	private JTextArea output;
    private byte[] bytes = new byte[128000]; //not like 100kb of ram is a lot
    private int currentBytePos = 0;

    public CustomOutputStream(JTextArea ta) {
        this.output = ta;
    }

    @Override
    public void write(int i) throws IOException {
        currentBytePos++;
        bytes[i] = (byte)i;

        try {
            output.setText(new String(bytes, "UTF-8"));
            output.setCaretPosition(output.getText().length());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


    }

}