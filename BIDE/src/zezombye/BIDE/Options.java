package zezombye.BIDE;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.stream.Collectors;

public class Options {
	
	Properties options = new Properties();
	
	public void loadProperties() {
		try {
			options.load(new FileInputStream(new File(BIDE.pathToOptions)));
		} catch (FileNotFoundException e) {
			System.out.println("No options.txt file found, creating it at "+BIDE.pathToOptions);
			initProperties();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//Not needed
	/*public void saveProperties() {
		try {
			options.store(new FileWriter(new File(BIDE.pathToOptions)), "");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}*/
	
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
		IO.writeStrToFile(new File(BIDE.pathToOptions),
				new BufferedReader(new InputStreamReader(BIDE.class.getClass().getResourceAsStream("/options.txt"))).lines().collect(Collectors.joining("\n")), true);
		loadProperties();
	}
}
