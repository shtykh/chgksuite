package shtykh.util.html;

import java.util.Collection;
import java.util.LinkedHashMap;

/**
 * Created by shtykh on 07/11/15.
 */
public class ColumnTableBuilder<T extends TableRowMaterial> extends ColoredTableBuilder {
	private final LinkedHashMap<String, ColumnBuilder<T>> columns = new LinkedHashMap<>();
	private boolean columnsAreFilled = false;

	public boolean addColumn(String name, ColumnBuilder<T> column) {
		if (columnsAreFilled) {
			return false;
		} else {
			this.columns.put(name, column);
			return true;
		}
	}

	@Override
	public TableBuilder addRow(String... strings) {
		throw new UnsupportedOperationException();
	}

	@Override
	public TableBuilder addRows(Collection<String[]> rows) {
		throw new UnsupportedOperationException();
	}
	
	public ColumnTableBuilder addRow(T source) {
		if (!columnsAreFilled) {
			String[] hat = new String[columns.size()];
			columns.keySet().toArray(hat);
			super.addRow(hat);
			columnsAreFilled = true;
		}
		String[] row = new String[columns.size()];
		int i = 0;
		for (ColumnBuilder<T> column : columns.values()) {
			row[i] = column.getCell(source);
			i++;
		}
		super.addRow(row);
		return this;
	}
}
