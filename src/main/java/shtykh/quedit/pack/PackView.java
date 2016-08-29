package shtykh.quedit.pack;

/**
 * Created by shtykh on 26/08/16.
 */

import com.sun.research.ws.wadl.HTTPMethods;
import org.apache.log4j.Logger;
import shtykh.quedit.numerator.NaturalNumerator;
import shtykh.quedit.question.Question;
import shtykh.rest.AuthorsCatalogue;
import shtykh.rest.PacksController;
import shtykh.util.args.PropertyReader;
import shtykh.util.catalogue.Catalogue;
import shtykh.util.html.*;
import shtykh.util.html.form.build.ActionBuilder;
import shtykh.util.html.form.build.FormBuilder;
import shtykh.util.html.form.material.FormMaterial;
import shtykh.util.html.form.param.FormParameter;
import shtykh.util.html.form.param.FormParameterSignature;
import shtykh.util.html.param.Parameter;

import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static java.lang.Boolean.parseBoolean;
import static shtykh.util.Util.StringLogger;
import static shtykh.util.Util.timestamp;
import static shtykh.util.html.HtmlHelper.*;
import static shtykh.util.html.form.build.FormBuilder.buildUploadForm;
import static shtykh.util.html.form.param.FormParameterType.*;

/**
 * Created by shtykh on 01/10/15.
 */
public class PackView extends PropertyReader implements FormMaterial, UriGenerator {
	private static final Logger log = Logger.getLogger(PackView.class);
	private final String id;
	private final HtmlHelper html;
	private final PacksController packs;
	private PackInfo info;

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
	private Map<String, String> hrefs;
	private final AuthorsCatalogue authors;

	public PackView(String id, HtmlHelper html, PacksController packs, AuthorsCatalogue authors, Properties properties, PackInfo packInfo) throws FileNotFoundException, URISyntaxException {
		this.id = id;
		this.html = html;
		this.authors = authors;
		this.info = packInfo;
		this.packs = packs;
		setProperties(properties);
		afterRun();
		initActions();
		initHrefs();
	}

	private void initHrefs() throws URISyntaxException {
		hrefs = new HashMap<>();
		String outFormat = getProperty("outFormat");
		boolean debug = parseBoolean(getProperty("debug"));
		initHref("compose", "Сгенерировать пакет",
				new Parameter<>("outFormat", outFormat),
				new Parameter<>("debug", debug));
		initHref("uploadForm/4s", "Импорт пакета из 4s");
		initHref("uploadForm/docx", "Импорт пакета из docx");
		initHref("uploadFormTrello", "Импорт пакета из trello");
		initHref("uploadForm/pic", "Загрузить картинку");
		initHref("editCommonAuthorForm", "Автор всех вопросов");
		initHref("text", "Полный текст в 4s");
		initHref("split/getColor", "Разбить по цвету");
		initHref("info", "Редактировать преамбулу");
		initHref("", "К пакету " + info.getName());
		initHref("/authors/list", "Каталог персонажей");
	}

	public void initHref(String method, String name, Parameter... parameters) throws URISyntaxException {
		URI uri = uriBuilder(method, parameters).build();
		String href = href(uri, name);
		hrefs.put(method, href);
	}

	public void initHref(URI uri, String name) throws URISyntaxException {
		String href = href(uri, name);
		hrefs.put(uri.toString(), href);
	}

	public String getHref(String method) {
		return hrefs.get(method);
	}

	public String getHref(URI uri) {
		return hrefs.get(uri.toString());
	}

	public String home() throws Exception {
		info.refresh();
		ColoredTableBuilder questionsTable;
		URI uriNew;
		TableBuilder hrefs;
		try {
			questionsTable = getQuestionTable();
			Parameter<String> indexParameter = new Parameter<>("index", String.valueOf(info.size()));
			uriNew = uri("editForm", indexParameter);
			hrefs = new TableBuilder(
					getHref("uploadForm/4s"),
					getHref("uploadForm/docx"),
					getHref("uploadFormTrello"),
					getHref("uploadForm/pic"),
					getHref("editCommonAuthorForm")
			);
			hrefs.addRow(
					getHref("text"),
					getHref("compose"),
					getHref("split/getColor"));
		} catch (Exception e) {
			return error(e);
		}
		String hrefHome = getHref("");
		String body =
				href(packs.uri(""), "К списку пакетов") +
						hrefs.toString() + "<br>" +
						getHref("info") +
						questionsTable.toString() + "<br>" +
						href(uriNew, "Добавить вопрос №" + info.getNumerator().getNumber(info.size())) + "<br>" +
						"";
		return htmlPage(info.getName(), hrefHome, body);
	}

	public String info() {
		try {
			info.refresh();
		} catch (Exception e) {
			return error(e);
		}
		String body =
				info.to4s().replace("\n", "<br>") + "<br>" +
						editPackAction.buildForm(this) + "<br>" +
						addEditorAction.buildForm(authors) + "<br>" +
						removeEditorAction.buildForm(authors) + "<br>" +
						addTesterAction.buildForm(authors) + "<br>" +
						removeTesterAction.buildForm(authors) + "<br>" +
						getHref("/authors/list") + "<br>";
		return htmlPage(info.getName(), getHref(""), body);
	}

	private void initActions() {
		try {
			editQuestionAction = new ActionBuilder(address("edit"));
			editAuthorAction = new ActionBuilder(address("editAuthor"));
			removeAuthorAction = new ActionBuilder(address("removeAuthor"));
			editCommonAuthorAction = new ActionBuilder(address("editCommonAuthor"));
			removeCommonAuthorAction = new ActionBuilder(address("removeCommonAuthor"));
			editPackAction = new ActionBuilder(address("editPack"));
			addEditorAction = new ActionBuilder(address("addEditor"));
			addTesterAction = new ActionBuilder(address("addTester"));
			removeEditorAction = new ActionBuilder(address("removeEditor"));
			removeTesterAction = new ActionBuilder(address("removeTester"));
			replaceQuestionAction = new ActionBuilder(address("copyTo"));
			replaceQuestionAction
					.addParam(PacksController.class, "packNames", "Переместить в пакет", select)
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
		return htmlPage("Загрузите файл", buildUploadForm(address("upload" + "/" + what)));
	}

	public String upload_pic_page(File file) {
		try {
			FormBuilder formBuilder = new FormBuilder(address("addPicture"));
			formBuilder
					.addMember(new FormParameter<>(
							new FormParameterSignature("path", hidden), file.getAbsolutePath(), String.class))
					.addMember(new FormParameter<>(
							new FormParameterSignature("number", "Добавить раздаточный материал к вопросу номер", text), "", String.class));
			return html.listResponce(
					"Картинка загружена",
					formBuilder.build(HTTPMethods.GET),
					getQuestionTable(QuestionTableBuilder.ColumnName.NUMBER, QuestionTableBuilder.ColumnName.ANSWER).buildHtml());
		} catch (Exception e) {
			return error(e);
		}
	}

	public String editForm(int index) {
		try {
			Question question = info.get(index);
			if (question == null) {
				question = Question.mock();
				question.newIndex(info.size());
			} else {
				question.newIndex(index);
			}
			question.setNumber(info.getNumerator().getNumber(question.index()));
			question.setPacks(packs);
			Parameter<String> parameter = new Parameter<>("index", String.valueOf(index));
			String questionColor = question.getColor();
			ColoredTableBuilder navigation = new ColoredTableBuilder();
			URI uriColor = uri("nextColor", parameter, new Parameter<>("color", questionColor));
			navigation.addRow(
					href(uri("editForm", new Parameter<>("index", index - 1)), "Назад"),
					getHref(""),
					href(uriColor, "Сменить цвет"),
					href(uri("editForm", new Parameter<>("index", index + 1)), "Вперёд"));
			navigation.addColor(0, 2, questionColor);
			String body = navigation
					+ questionHtml(question)
					+ href(uri("editAuthorForm", new Parameter<>("index", index)), "Редактировать авторов")
					+ replaceQuestionAction.buildForm(question)
					+ editQuestionAction.buildForm(question);
			return htmlPage("Отредактируйте вопрос", body);
		} catch (Exception e) {
			return error(e);
		}
	}

	public String editAuthorForm(int index) throws Exception {
		Question question = info.get(index);
		if (question == null) {
			question = Question.mock();
			question.newIndex(info.size());
		} else {
			question.newIndex(index);
		}
		authors.refresh();
		question.setNumber(info.getNumerator().getNumber(question.index()));
		question.setAuthors(authors);
		String body = questionHtml(question) + "<br>"
				+ editAuthorAction.buildForm(question)
				+ removeAuthorAction.buildForm(question)
				+ getHref("/authors/list");
		return htmlPage("Добавить автора", body);
	}

	public String editCommonAuthorForm() throws Exception {
		authors.refresh();
		String body = "Редоктировать автора ко всем вопросам " + id + "<br>"
				+ editCommonAuthorAction.buildForm(authors)
				+ removeCommonAuthorAction.buildForm(authors)
				+ getHref("/authors/list");
		return htmlPage("Автор всех вопросов", body);
	}

	public String compose(int result, StringLogger logs, File timestampFolder, String outFormat) throws IOException {
		if (result == 0) {
			logs.info("Файл формата " + outFormat + " успешно создан в папке " + timestampFolder);
			if (outFormat.equals("docx")) {
				String path = getPath(timestampFolder, outFormat);
				try {
					URI downloadHref = uri("download/docx", new Parameter<>("path", path));
					logs.info(href(downloadHref, "Скачать"));
				} catch (Exception e) {
					logs.error(e.getMessage());
				}
			}
		}
		//template.delete();
		//logs.debug(template + " was deleted");
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

	private static File timestampFolder(File folderPath) {
		String timestamp = timestamp("yyyyMMdd_HHmmss");
		File timestampFolder = new File(folderPath.getAbsolutePath() + "/" + timestamp);
		timestampFolder.mkdirs();
		return timestampFolder;
	}

	public Response downloadDocFile(String path) {
		File file = new File(path);
		Response.ResponseBuilder responseBuilder = Response.ok(file);
		responseBuilder.header("Content-Disposition", "attachment; filename=\"" + id + ".docx\"");
		return responseBuilder.build();
	}

	@Override
	public String base(){
		return "/" + id;
	}

	@Override
	public HtmlHelper htmlHelper() {
		return html;
	}

	private ColumnTableBuilder<Question> getQuestionTable(QuestionTableBuilder.ColumnName... columnNames) throws URISyntaxException {
		ColumnTableBuilder<Question> table = initQuestionTable(columnNames);
		for (int i = 0; i < info.size(); i++) {
			table.addRow(info.get(i));
		}
		return table;
	}

	private QuestionTableBuilder initQuestionTable(QuestionTableBuilder.ColumnName... columnNames) {
		return new QuestionTableBuilder(this, columnNames);
	}

	private String questionHtml(Question question) {
		return question.toString().replace("\n", "<br>") + "<br>";
	}
}

