package shtykh.util.args;

import org.springframework.boot.CommandLineRunner;

import java.io.FileReader;
import java.util.Properties;

/**
 * Created by shtykh on 10/04/15.
 */
public interface PropertyReader extends CommandLineRunner {

	@Override
	default void run(String[] args) {
		if (args.length < 1) {
			throw new RuntimeException("getProperties() was not found");
		} else {
			String filename = args[0];
			try {
				setProperties(new Properties());
				getProperties().load(new FileReader(filename));
				System.out.println(filename + " was read");
				afterRun();
			} catch (Exception e) {
				throw new RuntimeException(filename + " is bad propertyFile", e);
			}
		}
	}

	default String getProperty(String key) {
		try {
			return getProperties().getProperty(key);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	void setProperties(Properties properties);

	Properties getProperties();

	void afterRun();
}
