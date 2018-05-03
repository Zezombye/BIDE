package zezombye.BIDE;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

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
             try {
                 output.append(new String(bytes, 0, currentBytePos, "UTF-8"));
                 output.setCaretPosition(output.getText().length());
             } catch (UnsupportedEncodingException e) {
                 e.printStackTrace();
             }
             bytes = new byte[1024];
             currentBytePos = 0;
             
        }
       


    }

}
