package shtykh.util.html;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by shtykh on 19/07/15.
 */
public class ColoredTable extends TableBuilder {
	private static final String DEFAULT_COLOR = "#FFFFFF";
	private Map<Integer, Map<Integer, String>> bgColors;

	public ColoredTable(String... hat) {
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
		rowColors.put(column, colorHex);
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
