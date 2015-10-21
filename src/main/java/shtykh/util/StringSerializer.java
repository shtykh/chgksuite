package shtykh.util;

import org.apache.commons.lang3.StringEscapeUtils;

import java.awt.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by shtykh on 10/07/15.
 */
public abstract class StringSerializer<T> {
	private static Map<Class, StringSerializer> map = new ConcurrentHashMap<>();
	static {
		map.put(Integer.class, 	new StringSerializer<Integer>() {
			@Override
			protected Integer fromStringInternal(String string) {
				return Integer.decode(string);
			}

			@Override
			protected String toStringInternal(Integer stringSerializable) {
				return stringSerializable.toString();
			}
		});
		map.put(Long.class, new StringSerializer<Long>() {
			@Override
			protected Long fromStringInternal(String string) {
				return Long.decode(string);
			}

			@Override
			protected String toStringInternal(Long stringSerializable) {
				return stringSerializable.toString();
			}
		});
		
		map.put(Date.class, new StringSerializer<Date>() {
			private DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			@Override
			protected Date fromStringInternal(String string) {
				try {
					return df.parse(string);
				} catch (ParseException e) {
					throw new RuntimeException(e);
				}
			}
			@Override
			protected String toStringInternal(Date time) {
				return df.format(time);
			}
		});
		map.put(Boolean.class, 	new StringSerializer<Boolean>() {
			@Override
			protected Boolean fromStringInternal(String string) {
				return Boolean.valueOf(string);
			}

			@Override
			protected String toStringInternal(Boolean stringSerializable) {
				return stringSerializable.toString();
			}
		});
		
		map.put(CSV.class, new StringSerializer<CSV>() {
			@Override
			protected CSV fromStringInternal(String string) {
				return new CSV(string);
			}

			@Override
			protected String toStringInternal(CSV csv) {
				return csv.toString();
			}
		});
		
		map.put(String.class, new StringSerializer<String>() {

			@Override
			protected String fromStringInternal(String string) {
				return string;
			}

			@Override
			protected String toStringInternal(String string) {
				return string;
			}
		});

		map.put(Color.class, new StringSerializer<Color>() {

			@Override
			protected Color fromStringInternal(String string) {
				return Color.decode(string);
			}

			@Override
			protected String toStringInternal(Color color) {
				return String.format("#%06x", color.getRGB() & 0x00FFFFFF);
			}
		});
		
	}

	public T fromString(String string) {
		string = StringEscapeUtils.unescapeHtml4(string);
		return fromStringInternal(string);
	}


	public String toString(T stringSerializable) {
		String s = toStringInternal(stringSerializable);
		return StringEscapeUtils.escapeHtml4(s);
	}
	
	protected abstract T fromStringInternal(String string);
	protected abstract String toStringInternal(T stringSerializable);

	public static <T> StringSerializer<T> getForClass(Class<T> clazz) {
		return map.get(clazz);
	}
}
