package zezombye.BIDE;
import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.RECT;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.WinUser.WNDENUMPROC;
import com.sun.jna.win32.StdCallLibrary;


public class AutoImport {
	
	public interface User32 extends StdCallLibrary {
	    User32 INSTANCE = (User32) Native.loadLibrary("user32", User32.class);

	    boolean EnumWindows(WinUser.WNDENUMPROC lpEnumFunc, Pointer arg);

	    WinDef.HWND SetFocus(WinDef.HWND hWnd);

	    int GetWindowTextA(HWND hWnd, byte[] lpString, int nMaxCount);
	    int GetWindowRect(HWND handle, RECT rekt);
	    boolean SetForegroundWindow(WinDef.HWND hWnd);
	}
	

    final User32 user32 = User32.INSTANCE;
    HWND emulatorHWND = null;
    int emuSleep = 50; //optimal sleep time between keypresses for the emulator
    Robot robot;
    BufferedImage confirmation, memMenu, complete;
    int screenX, screenY, screenWidth, screenHeight;
    
    public AutoImport() {
    	try {
			confirmation = ImageIO.read(BIDE.class.getClass().getResourceAsStream("/images/confirmation.png"));
			complete = ImageIO.read(BIDE.class.getClass().getResourceAsStream("/images/complete.png"));
			memMenu = ImageIO.read(BIDE.class.getClass().getResourceAsStream("/images/memMenu.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			robot = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}
		screenX = Integer.parseInt(BIDE.options.getProperty("screenX"));
		screenY = Integer.parseInt(BIDE.options.getProperty("screenY"));
		screenWidth = Integer.parseInt(BIDE.options.getProperty("screenWidth"));
		screenHeight = Integer.parseInt(BIDE.options.getProperty("screenHeight"));
    }
    
	public void autoImport(String path) {
		emulatorHWND = null;
		//menu = ImageIO.read(new File("C:\\Users\\Catherine\\Desktop\\menu.png"));
		//long time = System.currentTimeMillis();
		ArrayList<String> titles = enumWindows();
		if (titles.contains("Ouvrir")) {
			BIDE.error("There is another window titled \"Ouvrir\"");
			return;
		}
		if (emulatorHWND == null) {
			BIDE.error("Could not find emulator");
			return;
		}
		user32.SetForegroundWindow(emulatorHWND);
    	
    	//storeEmuScreen();
    	//The user is supposed to be on the menu
    	inputKey(KeyEvent.VK_PAGE_DOWN, emuSleep);
    	
    	//Go to memory
    	inputKey('F', emuSleep);
    	
    	long end = System.currentTimeMillis()+1000;
    	boolean foundMemMenu = false;
    	while (System.currentTimeMillis() < end) {
    		if (testImgEquality(getEmuScreen(), memMenu)) {
    			foundMemMenu = true;
    			break;
    		}
    	}
    	if (!foundMemMenu) {
    		BIDE.error("Could not find memory menu, check the offset of the emulator screen");
    		return;
    	}
    	inputKey(KeyEvent.VK_F3, emuSleep);
    	end = System.currentTimeMillis()+3000;
    	while (!enumWindows().contains("Ouvrir")) {
    		 if (System.currentTimeMillis() < end) {
    			 BIDE.error("Couldn't open file system");
    			 return;
    		 }
    	}
    	try {
			Thread.sleep(20);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	inputString(path, 0);
    	inputKey(KeyEvent.VK_ENTER, 0);
    	
    	//Wait for confirmation screen (copy to main mem/storage mem)
    	end = System.currentTimeMillis()+5000;
    	while (!testImgEquality(getEmuScreen(), confirmation) && System.currentTimeMillis() < end) {}
    	if (enumWindows().contains("Ouvrir")) {
    		BIDE.error("Aborting, unknown error");
    		return;
    	}
    	inputKey('1', emuSleep);
    	
    	//There can be warning screens if the file already exists. In doubt, spam F1
    	end = System.currentTimeMillis()+4000;
    	while (!testImgEquality(getEmuScreen(), complete) && System.currentTimeMillis() < end) {
    		inputKey(KeyEvent.VK_F1, emuSleep);
    	}
    	//Now the transfer is completed
    	inputKey(KeyEvent.VK_PAGE_DOWN, emuSleep);
    	inputKey('B', emuSleep);
		//System.out.println(System.currentTimeMillis()-time);
		
	}
	
	public boolean testImgEquality(BufferedImage image1, BufferedImage image2) {
		int width;
		int height;

		if (image1.getWidth() == (width = image2.getWidth()) && 
		    image1.getHeight() == (height = image2.getHeight())) {

		    for (int x = 0; x < width; x++){
		        for (int y = 0; y < height; y++){
		            if (image1.getRGB(x, y) != image2.getRGB(x, y)){
		                return false;
		            }
		        }
		    }
		} else {
		    return false;
		}
		return true;
	}
	
	public BufferedImage getEmuScreen() {
		RECT dimensionsOfWindow = new RECT();
		user32.GetWindowRect(emulatorHWND, dimensionsOfWindow);
		Rectangle screen = dimensionsOfWindow.toRectangle();
		screen.x += screenX;
		screen.y += screenY;
		screen.width = screenWidth;
		screen.height = screenHeight;
        return robot.createScreenCapture(screen);
	}
	
	public BufferedImage getEmuScreenshot() {
		RECT dimensionsOfWindow = new RECT();
		user32.GetWindowRect(emulatorHWND, dimensionsOfWindow);
		Rectangle screen = dimensionsOfWindow.toRectangle();
        return robot.createScreenCapture(screen);
	}
	
	//storeEmuScreenshot() takes a screenshot of the whole emulator, while storeEmuScreen() only takes a screenshot of the calculator screen.
	public void storeEmuScreenshot() {
		try {
			ImageIO.write(getEmuScreenshot(), "png", new File(System.getProperty("user.home")+"/emulator.png"));
			System.out.println("Saved emulator screenshot at "+System.getProperty("user.home")+"/emulator.png");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void storeEmuScreen() {
		try {
			ImageIO.write(getEmuScreen(), "png", new File(System.getProperty("user.home")+"/open.png"));
			System.out.println("Saved calculator screenshot at " + System.getProperty("user.home")+"/open.png");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void inputString(String str, int sleep){
		for (int i = 0; i < str.length(); i++) {
			inputKey(str.charAt(i), sleep);
		}
	}
	
	public void inputKey(char key, int sleep) {
		
		//Check for caps
		if (key >= 'A' && key <= 'Z') {
			inputKeyWithShift(key, sleep);
		} else if (key >= 'a' && key <= 'z') {
			inputKey('A'+(key-'a'), sleep);
		} else if (key >= '0' && key <= '9') {
			inputKeyWithShift(key, sleep);
		} else if (key == '.') {
			inputKeyWithShift(KeyEvent.VK_SEMICOLON, sleep);
		} else if (key == '_') {
			inputKey(KeyEvent.VK_8, sleep);
		} else if (key == ' ') {
			inputKey(KeyEvent.VK_SPACE, sleep);
		} else if (key == '/') {
			inputKeyWithShift(KeyEvent.VK_COLON, sleep);
		} else if (key == ':') {
			inputKey(KeyEvent.VK_COLON, sleep);
		} else {
			//user32.SetForegroundWindow(emulatorHWND);
			//Input via alt codes
			robot.keyPress(KeyEvent.VK_ALT);
		    for (int i = 3; i >= 0; --i) {
		        // extracts a single decade of the key-code and adds
		        // an offset to get the required VK_NUMPAD key-code
		        int numpad_kc = key / (int) (Math.pow(10, i)) % 10 + KeyEvent.VK_NUMPAD0;

		        robot.keyPress(numpad_kc);
		        try {
					Thread.sleep(sleep);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		        robot.keyRelease(numpad_kc);
		        try {
					Thread.sleep(sleep);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		    }

		    robot.keyRelease(KeyEvent.VK_ALT);
		}
		
		
	}
	
	public void inputKeyWithShift(int key, int sleep) {
		//user32.SetForegroundWindow(emulatorHWND);
		robot.keyPress(KeyEvent.VK_SHIFT);
		robot.keyPress(key);
		try {
			Thread.sleep(sleep);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		robot.keyRelease(KeyEvent.VK_SHIFT);
		robot.keyRelease(key);
		try {
			Thread.sleep(sleep);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void inputKey(int key, int sleep) {
		//user32.SetForegroundWindow(emulatorHWND);
		robot.keyPress(key);
		try {
			Thread.sleep(sleep);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		robot.keyRelease(key);
		try {
			Thread.sleep(sleep);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public ArrayList<String> enumWindows() {
		ArrayList<String> titles = new ArrayList<String>();
		user32.EnumWindows(new WNDENUMPROC() {
			
	        public boolean callback(HWND hWnd, Pointer arg1) {
	            byte[] windowText = new byte[512];
	            user32.GetWindowTextA(hWnd, windowText, 512);
	            String wText = Native.toString(windowText);

	            if (wText.isEmpty()) {
	                return true;
	            }
	            titles.add(wText);
	            //System.out.println(wText);
	            //System.out.println("Found window with text " + hWnd + ", total " + ++count + " Text: " + wText);
	            /*if (wText.startsWith("fx-9860")) {
	                
	                return true;
	            }*/
	            if (wText.startsWith("fx-9860")) {
	            	emulatorHWND = hWnd;
	            }
	            return true;
	        }
	    }, null);
		return titles;
	}
}
