package shtykh.util.html;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by shtykh on 19/07/15.
 */
public class ColoredTableBuilder extends TableBuilder {
	private static final String COLOR_PATTERN_STRING = "#[0-9a-f]{6}";
	private static final Pattern COLOR_PATTERN = Pattern.compile(COLOR_PATTERN_STRING);
	private Map<Integer, Map<Integer, String>> bgColors;

	public ColoredTableBuilder(String... hat) {
		super(hat);
	}

	private void initBgColors() {
		if (bgColors == null) {
			bgColors = new TreeMap<>();
		}
	}
	
	public void addColor(int row, int column, String colorHex) {
		initBgColors();
		if (!bgColors.containsKey(row)) {
			bgColors.put(row, new TreeMap<>());
		}
		Map<Integer, String> rowColors = bgColors.get(row);
		if (colorHex.matches(COLOR_PATTERN_STRING)) {
			rowColors.put(column, colorHex);
		} else {
			Matcher m = COLOR_PATTERN.matcher(colorHex);
			if(m.find()) {
				rowColors.put(column, m.group(0));
			}
		}
	}

	@Override
	public TableBuilder addRows(Collection<String[]> rows) {
		for (String[] row : rows) {
			addRow(row);
		}
		return this;
	}

	@Override
	public String getColor(int row, int column) {
		Map<Integer, String> rowColors = bgColors == null ? null : bgColors.get(row);
		return rowColors == null ? null : rowColors.get(column);
	}
}
