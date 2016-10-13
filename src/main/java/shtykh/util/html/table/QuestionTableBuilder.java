package shtykh.util.html.table;

import org.apache.commons.lang3.StringUtils;
import shtykh.quedit.author.Person;
import shtykh.quedit.question.Question;
import shtykh.util.html.UriGenerator;
import shtykh.util.html.param.Parameter;

import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import static shtykh.rest.Locales.getString;
import static shtykh.util.html.HtmlHelper.href;

/**
 * Created by shtykh on 08/11/15.
 */
public class QuestionTableBuilder extends ColumnTableBuilder<Question> {
	private Map<ColumnName, ColumnBuilder<Question>> columns;
	private final Collection<Question> questions;
	private final UriGenerator uri;
	
	public enum ColumnName {
		NUMBER,
		COLOR,
		ANSWER,
		EDIT,
		AUTHORS,
		SAY,
		REPLACE,
		REMOVE,
		UP,
		DOWN
		;
	}

	public QuestionTableBuilder(Collection<Question> questions, UriGenerator uri, ColumnName... columnNames) {
		super();
		this.questions = questions;
		this.uri = uri;
		initMap();
		if (columnNames.length == 0) {
			columnNames = ColumnName.values();
		}
		for (ColumnName columnName : columnNames) {
			addColumn(getString(columnName.name()), columns.get(columnName));
		}
		for (Question question : questions) {
			addRow(question);
		}

	}
	
	private void initMap() {
		columns = new TreeMap<>();
		columns.put(ColumnName.NUMBER, new ColumnBuilder<Question>() {
			@Override
			public String getCell(Question question) {
				return question.getNumber();
			}
		});
		columns.put(ColumnName.COLOR, new ColumnBuilder<Question>() {
			@Override
			public String getCell(Question question) {
				int i = question.index();
				String questionColor = question.getColor();
				URI uriColor = uri.uri("nextColor", indexParam(question), new Parameter<>("color", questionColor));
				addColor(i + 1, 1, questionColor);
				return href(uriColor, questionColor);
			}
		});
		columns.put(ColumnName.ANSWER, new ColumnBuilder<Question>() {
			@Override
			public String getCell(Question question) {
				return question.getAnswer();
			}
		});
		columns.put(ColumnName.EDIT, new ColumnBuilder<Question>() {
			@Override
			public String getCell(Question question) {
				URI uriEdit = uri.uri("editForm", indexParam(question));
				return href(uriEdit, getString("EDIT"));
			}
		});
		columns.put(ColumnName.SAY, new ColumnBuilder<Question>() {
			@Override
			public String getCell(Question question) {
				URI uriEdit = uri.uri("read", indexParam(question));
				return href(uriEdit, getString("SAY"));
			}
		});
		columns.put(ColumnName.AUTHORS, new ColumnBuilder<Question>() {
			@Override
			public String getCell(Question question) {
				URI uriEditAuthor = uri.uri("editAuthorForm", indexParam(question));
				Person author = question.getAuthor();
				String authorString = getString("ADD_AUTH");
				if (author != null && StringUtils.isNotBlank(author.toString())) {
					authorString = author.toString();
				}
				return href(uriEditAuthor, authorString);
			}
		});
		columns.put(ColumnName.REPLACE, new ColumnBuilder<Question>() {
			@Override
			public String getCell(Question question) {
				URI uriReplace = uri.uri("copyTo", indexParam(question));
				return href(uriReplace, getString("REPLACE"));
			}
		});
		columns.put(ColumnName.REMOVE, new ColumnBuilder<Question>() {
			@Override
			public String getCell(Question question) {
				URI uriRemove = uri.uri("remove", indexParam(question));
				return href(uriRemove, getString("REMOVE"));
			}
		});
		columns.put(ColumnName.UP, new ColumnBuilder<Question>() {
			@Override
			public String getCell(Question question) {
				int i = question.index();
				URI home = uri.uri("");
				URI uriUp = i == 0 ? home : uri.uri("up", indexParam(question));
				return href(uriUp, "^^");
			}
		});
		columns.put(ColumnName.DOWN, new ColumnBuilder<Question>() {
			@Override
			public String getCell(Question question) {
				int i = question.index();
				URI home = uri.uri("");
				URI uriDown = i == questions.size() - 1 ? home : uri.uri("down", indexParam(question));
				return href(uriDown, "vv");
			}
		});
	}
	
	private static Parameter<String> indexParam(Question q) {
		int i = q.index();
		return new Parameter<>("index", String.valueOf(i));
	}
}
