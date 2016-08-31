package shtykh.rest;

import org.springframework.stereotype.Component;
import shtykh.util.args.PropertyReader;

import java.text.MessageFormat;

/**
 * Created by shtykh on 31/08/16.
 */
@Component
public class StringConstants extends PropertyReader {
	private static StringConstants instance;
	
	private StringConstants() {
		instance = this;
	}
	
	@Override
	public void afterRun() {
		readFromFile(getProperty("strings"));
	}

	public static String getString(String key, Object... objects) {
		return MessageFormat.format(instance.getProperty(key), objects);
	}
}
