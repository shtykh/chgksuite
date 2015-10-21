package shtykh.util.html;

import shtykh.util.html.param.Parameter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static shtykh.util.html.TagBuilder.tag;

/**
 * Created by shtykh on 06/04/15.
 */
public class TableBuilder {
	private static final int BORDER = 1;
	protected List<String[]> contents;

	private String title = null;

	public TableBuilder(String... hat) {
		contents = new ArrayList<>();
		if (hat.length != 0) {
			addRow(hat);
		}
	}

	public TableBuilder addRow(String... strings) {
		contents.add(strings);
		return this;
	}

	public TableBuilder addRows(Collection<String[]> rows) {
		contents.addAll(rows);
		return this;
	}

	public TableBuilder setTitle(String title) {
		this.title = title;
		return this;
	}

	public String buildHtml() {
		StringBuilder sb = new StringBuilder();
		if (title != null) {
			sb.append(tag("h2")
					.build(title));
		}
		sb.append(tag("table")
				.params(new Parameter<>("border", BORDER))
				.build(contents(contents)));
		return sb.toString();
	}

	@Override
	public String toString() {
		return buildHtml();
	}

	private String contents(List<String[]> contents) {
		StringBuilder sb = new StringBuilder();
		int rowIndex = 0;
		for (String[] row : contents) {
			sb.append(row(row, rowIndex++));
		}
		return sb.toString();
	}

	private String row(String[] row, int rowIndex) {
		StringBuilder sb = new StringBuilder();
		int column = 0;
		for (String cell : row) {
			TagBuilder td = tag("td");
			String cellColor = getColor(rowIndex, column++);
			if (cellColor != null) {
				td.params(new Parameter<>("bgcolor", cellColor));
			}
			sb.append(td.build(cell));
		}
		TagBuilder tr = tag("tr");
		return tr.build(sb.toString());
	}

	protected String getColor(int row, int column) {
		return null;
	}
}
