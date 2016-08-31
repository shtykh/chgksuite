package shtykh.util.args;

import org.springframework.boot.CommandLineRunner;

import java.io.FileReader;
import java.util.Properties;

/**
 * Created by shtykh on 10/04/15.
 */
public abstract class PropertyReader implements CommandLineRunner {

	private Properties properties;

	@Override
	public void run(String[] args) {
		if (args.length < 1) {
			throw new RuntimeException("getProperties() was not found");
		} else {
			String filename = args[0];
			readFromFile(filename);
		}
		afterRun();
	}

	protected void readFromFile(String filename) {
		try {
			setProperties(new Properties());
			properties.load(new FileReader(filename));
			System.out.println(filename + " was read");
		} catch (Exception e) {
			throw new RuntimeException(filename + " is bad propertyFile", e);
		}
	}

	public String getProperty(String key) {
		try {
			return properties.getProperty(key);
		} catch (NullPointerException npe) {
			throw new RuntimeException("Properties are null", npe);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected final void setProperties(Properties properties) {
		this.properties = properties;
	}

	public void afterRun() {}

	public Properties getProperties() {
		return properties;
	}
}
