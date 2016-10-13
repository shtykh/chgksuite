package shtykh.rest;

import org.apache.commons.lang.StringUtils;
import shtykh.util.args.PropertyReader;

import java.io.File;
import java.text.MessageFormat;

/**
 * Created by shtykh on 31/08/16.
 */
public class Locale extends PropertyReader {

	@Override
	public String toString() {
		return "Locale{" + name + "}";
	}

	private final String name;

	public Locale(File file) {
		this.name = StringUtils.substringBefore(file.getName(), ".");
		readFromFile(file.getAbsolutePath());
	}

	public String getString(String key, Object... objects) {
		return MessageFormat.format(getProperty(key), objects);
	}

	public String getName() {
		return name;
	}
}
