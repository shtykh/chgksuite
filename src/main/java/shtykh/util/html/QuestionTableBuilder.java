package shtykh.util.html;

import org.apache.commons.lang3.StringUtils;
import shtykh.quedit.author.Person;
import shtykh.quedit.pack.Pack;
import shtykh.quedit.question.Question;
import shtykh.util.html.param.Parameter;

import java.net.URI;
import java.util.Map;
import java.util.TreeMap;

import static shtykh.util.html.HtmlHelper.href;

/**
 * Created by shtykh on 08/11/15.
 */
public class QuestionTableBuilder extends ColumnTableBuilder<Question> {
	private static Map<ColumnName, ColumnBuilder<Question>> columns;
	private final Pack pack;
	
	public enum ColumnName {
		NUMBER("Номер"), 
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

	public QuestionTableBuilder(Pack pack, ColumnName... columnNames) {
		super();
		this.pack = pack;
		initMap();
		if (columnNames.length == 0) {
			columnNames = ColumnName.values();
		}
		for (ColumnName columnName : columnNames) {
			addColumn(columnName.getName(), columns.get(columnName));
		}
	}
	
	private void initMap() {
		columns = new TreeMap<>();
		columns.put(ColumnName.NUMBER, new ColumnBuilder<Question>() {
			@Override
			public String getCell(Question question) {
				int i = question.index();
				Parameter<String> parameter = new Parameter<>("index", String.valueOf(i));
				String questionColor = question.getColor();
				URI uriColor = pack.uri("nextColor", parameter, new Parameter<>("color", questionColor));
				addColor(i + 1, 0, questionColor);
				return href(uriColor, pack.numerator().getNumber(i));
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
				URI uriEdit = pack.uri("editForm", parameter);
				return href(uriEdit, "Редактировать");
			}
		});
		columns.put(ColumnName.AUTHORS, new ColumnBuilder<Question>() {
			@Override
			public String getCell(Question question) {
				int i = question.index();
				Parameter<String> parameter = new Parameter<>("index", String.valueOf(i));
				URI uriEditAuthor = pack.uri("editAuthorForm", parameter);
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
				URI uriReplace = pack.uri("replace", parameter);
				return href(uriReplace, "В запас");
			}
		});
		columns.put(ColumnName.REMOVE, new ColumnBuilder<Question>() {
			@Override
			public String getCell(Question question) {
				int i = question.index();
				Parameter<String> parameter = new Parameter<>("index", String.valueOf(i));
				URI uriRemove = pack.uri("remove", parameter);
				return href(uriRemove, "Удалить");
			}
		});
		columns.put(ColumnName.UP, new ColumnBuilder<Question>() {
			@Override
			public String getCell(Question question) {
				int i = question.index();
				Parameter<String> parameter = new Parameter<>("index", String.valueOf(i));
				URI home = pack.uri("");
				URI uriUp = i == 0 ? home : pack.uri("up", parameter);
				return href(uriUp, "^^");
			}
		});
		columns.put(ColumnName.DOWN, new ColumnBuilder<Question>() {
			@Override
			public String getCell(Question question) {
				int i = question.index();
				Parameter<String> parameter = new Parameter<>("index", String.valueOf(i));
				URI home = pack.uri("");
				URI uriDown = i == pack.size() - 1 ? home : pack.uri("down", parameter);
				return href(uriDown, "vv");
			}
		});
	}
}
