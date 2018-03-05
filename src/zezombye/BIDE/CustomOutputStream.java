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
    private ArrayList<Byte> bytes = new ArrayList<>();

    public CustomOutputStream(JTextArea ta) {
        this.output = ta;
    }

    @Override
    public void write(int i) throws IOException {
        bytes.add((byte)i);

        byte[] array = new byte[bytes.size()];
        int q = 0;
        for (Byte current : bytes) {
            array[q] = current;
            q++;
        }
        try {
            output.setText(new String(array, "UTF-8"));
            output.setCaretPosition(output.getText().length());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


    }

}