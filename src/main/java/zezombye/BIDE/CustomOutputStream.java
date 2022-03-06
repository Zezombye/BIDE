package zezombye.BIDE;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import javax.swing.JTextArea;

public class CustomOutputStream extends OutputStream {
	private JTextArea output;
    private byte[] bytes = new byte[1024];
    private int currentBytePos = 0;

    public CustomOutputStream(JTextArea ta) {
        this.output = ta;
    }

    @Override
    public void write(int i) throws IOException {
        bytes[currentBytePos] = (byte)i;
        currentBytePos++;
        if (i == '\n') {
             output.append(new String(bytes, 0, currentBytePos, "UTF-8"));
			 output.setCaretPosition(output.getText().length());
             bytes = new byte[1024];
             currentBytePos = 0;
             
        }
       


    }

}
