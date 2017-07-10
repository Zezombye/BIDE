import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Options {
	
	Properties options = new Properties();
	
	public void loadProperties() {
		try {
			options.load(new FileInputStream(new File(System.getProperty("user.home")+"/BIDE/options.txt")));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void saveProperties() {
		try {
			options.store(new FileWriter(new File(System.getProperty("user.home")+"/BIDE/options.txt")), "");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getProperty(String prop) {
		if (options.getProperty(prop) == null) {
			//BIDE.error("Could not find property "+prop);
		}
		return options.getProperty(prop);
	}
	
	public void setProperty(String prop, String value) {
		options.setProperty(prop, value);
	}
	
	public void initProperties() {
		
		options.setProperty("spacesFor->", "true");
		options.setProperty("spacesFor<=", "true");
		options.setProperty("spacesFor!=", "true");
		options.setProperty("spacesFor>=", "true");
		options.setProperty("spacesFor=>", "true");
		options.setProperty("spacesFor,", "true");
		options.setProperty("spacesFor:", "true");
		options.setProperty("spacesFor<", "true");
		options.setProperty("spacesFor=", "true");
		options.setProperty("spacesFor>", "true");
		options.setProperty("spacesFor+", "true");
		options.setProperty("spacesFor-", "true");
		options.setProperty("spacesFor^", "true");
		options.setProperty("spacesFor*", "true");
		options.setProperty("spacesFor/", "true");
		saveProperties();
	}
}
