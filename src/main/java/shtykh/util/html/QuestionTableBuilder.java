package shtykh.util.html;

import org.apache.commons.lang3.StringUtils;
import shtykh.quedit.author.Person;
import shtykh.quedit.question.Question;
import shtykh.util.html.param.Parameter;

import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import static shtykh.util.html.HtmlHelper.href;

/**
 * Created by shtykh on 08/11/15.
 */
public class QuestionTableBuilder extends ColumnTableBuilder<Question> {
	private Map<ColumnName, ColumnBuilder<Question>> columns;
	private final Collection<Question> questions;
	private final UriGenerator uri;
	
	public enum ColumnName {
		NUMBER("Номер"),
		COLOR("Цвет"),
		ANSWER("Ответ"),
		EDIT("Редактировать"), 
		AUTHORS("Авторы"), 
		REPLACE("В запас"), 
		REMOVE("Удалить"), 
		UP("Вверх"), 
		DOWN("Вниз")
		;
		
		private final String name;

		private ColumnName(String name) {
			this.name = name;
		}
		public String getName() {
			return name;
		}
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
			addColumn(columnName.getName(), columns.get(columnName));
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
				Parameter<String> parameter = new Parameter<>("index", String.valueOf(i));
				String questionColor = question.getColor();
				URI uriColor = uri.uri("nextColor", parameter, new Parameter<>("color", questionColor));
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
				int i = question.index();
				Parameter<String> parameter = new Parameter<>("index", String.valueOf(i));
				URI uriEdit = uri.uri("editForm", parameter);
				return href(uriEdit, "Редактировать");
			}
		});
		columns.put(ColumnName.AUTHORS, new ColumnBuilder<Question>() {
			@Override
			public String getCell(Question question) {
				int i = question.index();
				Parameter<String> parameter = new Parameter<>("index", String.valueOf(i));
				URI uriEditAuthor = uri.uri("editAuthorForm", parameter);
				Person author = question.getAuthor();
				String authorString = "Добавить автора";
				if (author != null && StringUtils.isNotBlank(author.toString())) {
					authorString = author.toString();
				}
				return href(uriEditAuthor, authorString);
			}
		});
		columns.put(ColumnName.REPLACE, new ColumnBuilder<Question>() {
			@Override
			public String getCell(Question question) {
				int i = question.index();
				Parameter<String> parameter = new Parameter<>("index", String.valueOf(i));
				URI uriReplace = uri.uri("copyTo", parameter);
				return href(uriReplace, "В запас");
			}
		});
		columns.put(ColumnName.REMOVE, new ColumnBuilder<Question>() {
			@Override
			public String getCell(Question question) {
				int i = question.index();
				Parameter<String> parameter = new Parameter<>("index", String.valueOf(i));
				URI uriRemove = uri.uri("remove", parameter);
				return href(uriRemove, "Удалить");
			}
		});
		columns.put(ColumnName.UP, new ColumnBuilder<Question>() {
			@Override
			public String getCell(Question question) {
				int i = question.index();
				Parameter<String> parameter = new Parameter<>("index", String.valueOf(i));
				URI home = uri.uri("");
				URI uriUp = i == 0 ? home : uri.uri("up", parameter);
				return href(uriUp, "^^");
			}
		});
		columns.put(ColumnName.DOWN, new ColumnBuilder<Question>() {
			@Override
			public String getCell(Question question) {
				int i = question.index();
				Parameter<String> parameter = new Parameter<>("index", String.valueOf(i));
				URI home = uri.uri("");
				URI uriDown = i == questions.size() - 1 ? home : uri.uri("down", parameter);
				return href(uriDown, "vv");
			}
		});
	}
}
