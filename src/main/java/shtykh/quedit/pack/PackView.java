package shtykh.quedit.pack;

/**
 * Created by shtykh on 26/08/16.
 */

import com.sun.research.ws.wadl.HTTPMethods;
import org.apache.log4j.Logger;
import shtykh.quedit.numerator.NaturalNumerator;
import shtykh.quedit.question.Question;
import shtykh.rest.AuthorsCatalogue;
import shtykh.rest.PackController;
import shtykh.util.args.PropertyReader;
import shtykh.util.catalogue.Catalogue;
import shtykh.util.html.*;
import shtykh.util.html.form.build.ActionBuilder;
import shtykh.util.html.form.build.FormBuilder;
import shtykh.util.html.form.material.FormMaterial;
import shtykh.util.html.form.param.FormParameter;
import shtykh.util.html.form.param.FormParameterSignature;
import shtykh.util.html.param.Parameter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;

import static java.lang.Boolean.parseBoolean;
import static shtykh.util.Util.StringLogger;
import static shtykh.util.html.HtmlHelper.*;
import static shtykh.util.html.form.build.FormBuilder.buildUploadForm;
import static shtykh.util.html.form.param.FormParameterType.*;

/**
 * Created by shtykh on 01/10/15.
 */
public class PackView extends PropertyReader implements FormMaterial {
	private static final Logger log = Logger.getLogger(PackView.class);
	
	private HrefHelper hrefs;
	private final URI personUri;
	private final URI backUri;

	private ActionBuilder editQuestionAction;
	private ActionBuilder editAuthorAction;
	private ActionBuilder removeAuthorAction;
	private ActionBuilder editCommonAuthorAction;
	private ActionBuilder removeCommonAuthorAction;
	private ActionBuilder editPackAction;
	private ActionBuilder addEditorAction;
	private ActionBuilder addTesterAction;
	private ActionBuilder removeEditorAction;
	private ActionBuilder removeTesterAction;
	private ActionBuilder replaceQuestionAction;

	public PackView(String id, HtmlHelper html, PackController packs, Pack pack) throws FileNotFoundException, URISyntaxException {
		setProperties(packs.getProperties());
		afterRun();
		hrefs = new HrefHelper("/" + id, html);
		this.personUri = hrefs.uriAbsolute("/authors/list");
		this.backUri = packs.uri("");
		initActions();
		initHrefs(pack.getName());
	}

	private void initHrefs(String packName) throws URISyntaxException {
		String outFormat = getProperty("outFormat");
		boolean debug = parseBoolean(getProperty("debug"));
		hrefs.put("compose", "Сгенерировать пакет",
				new Parameter<>("outFormat", outFormat),
				new Parameter<>("debug", debug));
		hrefs.put("uploadForm/4s", "Импорт пакета из 4s");
		hrefs.put("uploadForm/docx", "Импорт пакета из docx");
		hrefs.put("uploadFormTrello", "Импорт пакета из trello");
		hrefs.put("uploadForm/pic", "Загрузить картинку");
		hrefs.put("editCommonAuthorForm", "Автор всех вопросов");
		hrefs.put("text", "Полный текст в 4s");
		hrefs.put("split/getColor", "Разбить по цвету");
		hrefs.put("info", "Редактировать преамбулу");
		hrefs.put("", "К пакету " + packName);
		hrefs.put(personUri, "Каталог персонажей");
		hrefs.put(backUri, "К списку пакетов");
	}

	public String home(String name, int size, String nextNumber, Collection<Question> questions) throws Exception {
		ColoredTableBuilder questionsTable;
		URI uriNew;
		TableBuilder hrefTable;
		try {
			questionsTable = getQuestionTable(questions);
			Parameter<String> indexParameter = new Parameter<>("index", String.valueOf(size));
			uriNew = hrefs.uri("editForm", indexParameter);
			hrefTable = new TableBuilder(
					hrefs.get("uploadForm/4s"),
					hrefs.get("uploadForm/docx"),
					hrefs.get("uploadFormTrello"),
					hrefs.get("uploadForm/pic"),
					hrefs.get("editCommonAuthorForm")
			);
			hrefTable.addRow(
					hrefs.get("text"),
					hrefs.get("compose"),
					hrefs.get("split/getColor"));
		} catch (Exception e) {
			return error(e);
		}
		String hrefHome = hrefs.get("");
		String body =
						hrefs.get(backUri) +
						hrefTable.toString() + "<br>" +
						hrefs.get("info") +
						questionsTable.toString() + "<br>" +
						href(uriNew, "Добавить вопрос №" + nextNumber) + "<br>" +
						"";
		return htmlPage(name, hrefHome, body);
	}

	public String infoPage(PackInfo info, AuthorsCatalogue authors) {
		String body =
				info.to4s().replace("\n", "<br>") + "<br>" +
						editPackAction.buildForm(info) + "<br>" +
						addEditorAction.buildForm(authors) + "<br>" +
						removeEditorAction.buildForm(authors) + "<br>" +
						addTesterAction.buildForm(authors) + "<br>" +
						removeTesterAction.buildForm(authors) + "<br>" +
						hrefs.get(personUri) + "<br>";
		return htmlPage(info.getName(), hrefs.get(""), body);
	}

	private void initActions() {
		try {
			editQuestionAction = new ActionBuilder(hrefs.address("edit"));
			editAuthorAction = new ActionBuilder(hrefs.address("editAuthor"));
			removeAuthorAction = new ActionBuilder(hrefs.address("removeAuthor"));
			editCommonAuthorAction = new ActionBuilder(hrefs.address("editCommonAuthor"));
			removeCommonAuthorAction = new ActionBuilder(hrefs.address("removeCommonAuthor"));
			editPackAction = new ActionBuilder(hrefs.address("editPack"));
			addEditorAction = new ActionBuilder(hrefs.address("addEditor"));
			addTesterAction = new ActionBuilder(hrefs.address("addTester"));
			removeEditorAction = new ActionBuilder(hrefs.address("removeEditor"));
			removeTesterAction = new ActionBuilder(hrefs.address("removeTester"));
			replaceQuestionAction = new ActionBuilder(hrefs.address("copyTo"));
			replaceQuestionAction
					.addParam(PackController.class, "packNames", "Переместить в пакет", select)
					.addParam(Question.class, "index", "Номер", hidden);
			editQuestionAction
					.addParam(Question.class, "number", "Номер", comment)
					.addParam(Question.class, "index", "Номер", hidden)
					.addParam(Question.class, "unaudible", "Примечания чтецу", textarea)
					.addParam(Question.class, "text", "Текст вопроса", textarea)
					.addParam(Question.class, "answer", "Ответ", textarea)
					.addParam(Question.class, "possibleAnswers", "Зачёт", textarea)
					.addParam(Question.class, "impossibleAnswers", "Незачёт", textarea)
					.addParam(Question.class, "comment", "Комментарий", textarea)
					.addParam(Question.class, "sources", "Источники (каждый с новой строки)", textarea)
					.addParam(Question.class, "color", "Цвет(Если красно-бело-зелёных не хватает)", color)
			;
			editAuthorAction
					.addParam(Catalogue.class, "keys", "Добавить автора", select)
					.addParam(Question.class, "index", "Номер", hidden)
			;
			removeAuthorAction
					.addParam(Catalogue.class, "keys", "Удалить автора", select)
					.addParam(Question.class, "index", "Номер", hidden)
			;editCommonAuthorAction
					.addParam(Catalogue.class, "keys", "Добавить автора", select)
			;
			removeCommonAuthorAction
					.addParam(Catalogue.class, "keys", "Удалить автора", select)
			;
			addEditorAction
					.addParam(Catalogue.class, "keys", "Добавить редактора", select)
			;
			addTesterAction
					.addParam(Catalogue.class, "keys", "Добавить тестера", select)
			;
			removeEditorAction
					.addParam(Catalogue.class, "keys", "Удалить редактора", select)
			;
			removeTesterAction
					.addParam(Catalogue.class, "keys", "Удалить тестера", select)
			;
			editPackAction
					.addParam(PackInfo.class, "name", "Название пакета", text)
					.addParam(PackInfo.class, "editor", "Редакторы", comment)
					.addParam(PackInfo.class, "date", "Дата", text)
					.addParam(PackInfo.class, "metaInfo", "Слово редактора", textarea)
					.addParam(NaturalNumerator.class, "zeroNumbers", "Нумерация: номера \"нулевых\" вопросов (каждый с новой строки)", textarea)
					.addParam(NaturalNumerator.class, "first", "Нумерация: номер первого вопроса", number)
					.addParam(PackInfo.class, "nameLJ", "Название пакета (для ЖЖ)", text)
			;
		} catch (NoSuchFieldException | URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}


	public String uploadForm(String what) throws URISyntaxException {
		return htmlPage("Загрузите файл", buildUploadForm(hrefs.address("upload" + "/" + what)));
	}

	public String upload_pic_page(File file, Collection<Question> questions) {
		try {
			FormBuilder formBuilder = new FormBuilder(hrefs.address("addPicture"));
			formBuilder
					.addMember(new FormParameter<>(
							new FormParameterSignature("path", hidden), file.getAbsolutePath(), String.class))
					.addMember(new FormParameter<>(
							new FormParameterSignature("number", "Добавить раздаточный материал к вопросу номер", text), "", String.class));
			return hrefs.listResponce(
					"Картинка загружена",
					formBuilder.build(HTTPMethods.GET),
					getQuestionTable(questions, QuestionTableBuilder.ColumnName.NUMBER, QuestionTableBuilder.ColumnName.ANSWER).buildHtml());
		} catch (Exception e) {
			return error(e);
		}
	}

	public String editForm(Question question, PackController packs, AuthorsCatalogue authors) {
		try {
			int index = question.index();
			Parameter<String> parameter = new Parameter<>("index", String.valueOf(index));
			String questionColor = question.getColor();
			ColoredTableBuilder navigation = new ColoredTableBuilder();
			URI uriColor = hrefs.uri("nextColor", parameter, new Parameter<>("color", questionColor));
			navigation.addRow(
					href(hrefs.uri("editForm", new Parameter<>("index", index - 1)), "Назад"),
					hrefs.get(""),
					href(uriColor, "Сменить цвет"),
					href(hrefs.uri("editForm", new Parameter<>("index", index + 1)), "Вперёд"));
			navigation.addColor(0, 2, questionColor);
			String body = navigation
					+ questionHtml(question)
					+ href(hrefs.uri("editAuthorForm", new Parameter<>("index", index)), "Редактировать авторов")
					+ replaceQuestionAction.buildForm(question, packs)
					+ editQuestionAction.buildForm(question, authors);
			return htmlPage("Отредактируйте вопрос", body);
		} catch (Exception e) {
			return error(e);
		}
	}

	public String editAuthorForm(Question question, AuthorsCatalogue authors) throws Exception {
		String body = questionHtml(question) + "<br>"
				+ editAuthorAction.buildForm(question, authors)
				+ removeAuthorAction.buildForm(question, authors)
				+ hrefs.get(personUri);
		return htmlPage("Добавить автора", body);
	}

	public String editCommonAuthorForm(AuthorsCatalogue authors, String id) throws Exception {
		String body = "Редактировать автора ко всем вопросам " + id + "<br>"
				+ editCommonAuthorAction.buildForm(authors)
				+ removeCommonAuthorAction.buildForm(authors)
				+ hrefs.get(personUri);
		return htmlPage("Автор всех вопросов", body);
	}

	public String compose_result_page(int result, StringLogger logs, File timestampFolder, String outFormat) throws IOException {
		if (result == 0) {
			logs.info("Файл формата " + outFormat + " успешно создан в папке " + timestampFolder);
			if (outFormat.equals("docx")) {
				String path = getPath(timestampFolder, outFormat);
				try {
					URI downloadHref = hrefs.uri("download/docx", new Parameter<>("path", path));
					logs.info(href(downloadHref, "Скачать"));
				} catch (Exception e) {
					logs.error(e.getMessage());
				}
			}
		}
		return htmlPage("Выгрузка", logs.toString().replace("\n", "<br>"));
	}

	private String getPath(File folder, String extension) {
		if (folder == null || !folder.isDirectory()) {
			return null;
		} else {
			for (File file : folder.listFiles()) {
				if (file.getName().endsWith(extension)) {
					return file.getAbsolutePath();
				}
			}
			return null;
		}
	}

	private ColumnTableBuilder<Question> getQuestionTable(Collection<Question> questions, QuestionTableBuilder.ColumnName... columnNames) throws URISyntaxException {
		return new QuestionTableBuilder(questions, hrefs, columnNames);
	}

	private String questionHtml(Question question) {
		return question.toString().replace("\n", "<br>") + "<br>";
	}
}

