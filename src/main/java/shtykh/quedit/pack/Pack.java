package shtykh.quedit.pack;

import com.sun.research.ws.wadl.HTTPMethods;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.web.multipart.MultipartFile;
import shtykh.quedit._4s.Parser4s;
import shtykh.quedit._4s._4Sable;
import shtykh.quedit.author.Authored;
import shtykh.quedit.author.MultiPerson;
import shtykh.quedit.author.Person;
import shtykh.quedit.numerator.NaturalNumerator;
import shtykh.quedit.numerator.QuestionNaturalNumerator;
import shtykh.quedit.numerator.QuestionNumerator;
import shtykh.quedit.question.Question;
import shtykh.rest.AuthorsCatalogue;
import shtykh.rest.PackController;
import shtykh.util.Jsonable;
import shtykh.util.StringSerializer;
import shtykh.util.Util;
import shtykh.util.catalogue.Catalogue;
import shtykh.util.catalogue.ListCatalogue;
import shtykh.util.html.*;
import shtykh.util.html.form.build.ActionBuilder;
import shtykh.util.html.form.build.FormBuilder;
import shtykh.util.html.form.material.FormMaterial;
import shtykh.util.html.form.param.FormParameter;
import shtykh.util.html.form.param.FormParameterSignature;
import shtykh.util.html.param.Parameter;

import javax.ws.rs.core.Response;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import static java.lang.Boolean.parseBoolean;
import static shtykh.util.Util.*;
import static shtykh.util.html.HtmlHelper.*;
import static shtykh.util.html.form.build.FormBuilder.buildUploadForm;
import static shtykh.util.html.form.param.FormParameterType.*;

/**
 * Created by shtykh on 01/10/15.
 */
public class Pack extends ListCatalogue<Question> implements FormMaterial, _4Sable, Authored, UriGenerator {
	private static final Logger log = Logger.getLogger(Pack.class);
	private final String id;
	private final HtmlHelper htmlHelper;
	private final AuthorsCatalogue authors;
	private final PackController packs;
	private PackInfo info;

	private ActionBuilder editQuestionAction;
	private ActionBuilder editAuthorAction;
	private ActionBuilder removeAuthorAction;
	private ActionBuilder editPackAction;
	private ActionBuilder addEditorAction;
	private ActionBuilder addTesterAction;
	private ActionBuilder removeEditorAction;
	private ActionBuilder removeTesterAction;
	private ActionBuilder replaceQuestionAction;
	private Boolean debug = false;

	public Pack(String id, HtmlHelper htmlHelper, AuthorsCatalogue authors, PackController packs, Properties properties) throws FileNotFoundException {
		super(Question.class);
		this.id = id;
		this.htmlHelper = htmlHelper;
		this.authors = authors;
		this.packs = packs;
		setProperties(properties);
		afterRun();
		initActions();
		initInfo();
	}

	@Override
	protected String folderName() {
		return getProperty("packs") + "/" + id;
	}

	public String home() throws Exception {
		refresh();
		ColoredTableBuilder questionsTable;
		URI uriHome;
		URI uriNew;
		URI uriText;
		URI uriBuild;
		URI uriPreambula;
		URI uriPacks;
		TableBuilder hrefs;
		try {
			questionsTable = getQuestionTable();
			uriHome = uri("");
			Parameter<String> parameter = new Parameter<>("index", String.valueOf(size()));
			uriNew = uri("editForm", parameter);
			uriText = uri("text");
			String outFormat = getProperty("outFormat");
			debug = parseBoolean(getProperty("debug"));
			uriPacks = packs.uri("");
			uriBuild = uri("compose",
					new Parameter<>("outFormat", outFormat),
					new Parameter<>("debug", debug.toString()));
			uriPreambula = uri("info");
			hrefs = new TableBuilder("Загрузить",
					href(uriBuilder("uploadForm/4s").build(), "Импорт пакета из 4s"),
					href(uriBuilder("uploadForm/docx").build(), "Импорт пакета из docx"),
					href(uriBuilder("uploadForm/pic").build(), "Загрузить картинку")
			);
		} catch (Exception e) {
			return error(e);
		}

		String hrefHome = href(uriHome, getName());
		hrefs.addRow("Выгрузить",
				href(uriText, "Полный текст в 4s"),
				href(uriBuild, "Сгенерировать пакет"));
		String body = 
				href(uriPacks, "К списку пакетов") + 
				hrefs.toString() + "<br>" +
						href(uriPreambula, "Редактировать преамбулу") +
						questionsTable.toString() + "<br>" +
						href(uriNew, "Добавить вопрос №" + numerator().getNumber(size())) + "<br>" +
						"";
		return htmlPage(getName(), hrefHome, body);
	}


	public String info() throws Exception {
		refresh();
		URI uriHome;
		URI uriAuthors;
		try {
			uriHome = uri("");
			uriAuthors = htmlHelper.uriBuilder("/authors/list").build();
		} catch (Exception e) {
			return error(e);
		}
		String hrefHome = href(uriHome, getName());
		String body =
				info.to4s().replace("\n", "<br>") + "<br>" +
				editPackAction.buildForm(this) + "<br>" +
				addEditorAction.buildForm(authors) + "<br>" +
				removeEditorAction.buildForm(authors) + "<br>" +
				addTesterAction.buildForm(authors) + "<br>" +
				removeTesterAction.buildForm(authors) + "<br>" +
				href(uriAuthors, "Каталог персонажей") + "<br>" +
				"";
		return htmlPage(getName(), hrefHome, body);
	}
	
	private void initActions() {
		editQuestionAction = new ActionBuilder(address("edit"));
		editAuthorAction = new ActionBuilder(address("editAuthor"));
		removeAuthorAction = new ActionBuilder(address("removeAuthor"));
		editPackAction = new ActionBuilder(address("editPack"));
		addEditorAction = new ActionBuilder(address("addEditor"));
		addTesterAction = new ActionBuilder(address("addTester"));
		removeEditorAction = new ActionBuilder(address("removeEditor"));
		removeTesterAction = new ActionBuilder(address("removeTester"));
		replaceQuestionAction = new ActionBuilder(address("copyTo"));
		try {
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
					.addParam(Question.class, "color", "Цвет(для удобства редактора)", color)
			;
			editAuthorAction
					.addParam(Catalogue.class, "keys", "Добавить автора", select)
					.addParam(Question.class, "index", "Номер", hidden)
			;
			removeAuthorAction
					.addParam(Catalogue.class, "keys", "Удалить автора", select)
					.addParam(Question.class, "index", "Номер", hidden)
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
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected String folderNameKey() {
		return "packs";
	}

	@Override
	public void refresh() throws Exception {
		super.refresh();
		this.info = Jsonable.fromJson(Util.read(infoPath()), PackInfo.class);
	}

	private String infoPath() {
		return folderPath() + ".info";
	}

	public String editPack(
			 String name,
			 String nameLJ,
			 String date,
			 String metaInfo,
			 int first,
			 String zeroNumbers
	) throws Exception {
		setName(name);
		setNameLJ(nameLJ);
		setDate(date);
		setMetaInfo(metaInfo);
		setNumerator(new QuestionNaturalNumerator(first, zeroNumbers));
		return info();
	}

	public String uploadForm(String what) {
		return htmlPage("Загрузите файл", buildUploadForm(address("upload" + "/" + what)));
	}

	public String upload_file(MultipartFile multipartFile) {
		try{
			String folderPath = folder + "/uploads";
			File file = saveFile(multipartFile, folderPath, "file");
			String output = "File saved to server location : " + file;
			return output;
		} catch (Exception e) {
			return error(e);
		}
	}

	public String upload_4s(MultipartFile multipartFile) {
		String folderPath = folder + "/import";
		try {
			File timestampFolder = timestampFolder(new File(folderPath));
			clearFolder();
			File file = saveFile(multipartFile, timestampFolder.getAbsolutePath(), id + ".docx");
			fron4sFile(file);
			return home();
		} catch (Exception e) {
			return error(e);
		}
	}

	private void fron4sFile(File file) {
		Parser4s parser4s = new Parser4s(file.getAbsolutePath());
		this.fromParser(parser4s);
		file.delete();
	}

	public String upload_docx(MultipartFile multipartFile) {
		String folderPath = folder + "/import";
		try {
			File timestampFolder = timestampFolder(new File(folderPath));
			clearFolder();
			File fileDocx = saveFile(multipartFile, timestampFolder.getAbsolutePath(), id + ".docx");
			StringLogger logs = new StringLogger(log, debug);
			logs.info("File saved to server location : " + file);
			String[] cmd = chgkSuiteCmd("parse", fileDocx.getAbsolutePath());
			if (call(logs, cmd) == 0) {
				File file4s = new File(timestampFolder.getAbsoluteFile() + "/" + id + ".4s");
				fron4sFile(file4s);
				return home();
			} else {
				return htmlPage("Загрузка из " + multipartFile.getOriginalFilename(), logs.toString().replace("\n", "<br>"));
			}
		} catch (Exception e) {
			return error(e);
		}
	}

	public String upload_pic(MultipartFile multipartFile) {
		String folderPath = folder + "/pics";
		try {
			File timestampFolder = timestampFolder(new File(folderPath));
			File file = saveFile(multipartFile, timestampFolder.getAbsolutePath(), multipartFile.getName());
			FormBuilder formBuilder = new FormBuilder(address("addPicture"));
			formBuilder
					.addMember(new FormParameter<>(
							new FormParameterSignature("path", hidden), file.getAbsolutePath(), String.class))
					.addMember(new FormParameter<>(
							new FormParameterSignature("number", "Добавить раздаточный материал к вопросу номер", text), "", String.class));
			return htmlHelper.listResponce(
					"Картинка загружена", 
					formBuilder.build(HTTPMethods.GET), 
					getQuestionTable(QuestionTableBuilder.ColumnName.NUMBER, QuestionTableBuilder.ColumnName.ANSWER).buildHtml());
		} catch (Exception e) {
			return error(e);
		}
	}

	public void fromParser(Parser4s parser4s) {
		info = parser4s.getInfo();
		info.save(infoPath());
		for (Question question : parser4s.getQuestions()) {
			add(question);
		}
	}

	public String editForm(int index) {
		try {
			Question question = get(index);
			if (question == null) {
				question = Question.mock();
				question.newIndex(size());
			} else {
				question.newIndex(index);
			}
			question.setNumber(numerator().getNumber(question.index()));
			question.setPacks(packs);
			TableBuilder navigation = new TableBuilder()
					.addRow(href(uri("editForm", new Parameter<>("index", index - 1)), "Назад"),
							href(uri(""), "К пакету"),
							href(uri("editForm", new Parameter<>("index", index + 1)), "Вперёд"));
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
		Question question = get(index);
		if (question == null) {
			question = Question.mock();
			question.newIndex(size());
		} else {
			question.newIndex(index);
		}
		authors.refresh();
		question.setNumber(numerator().getNumber(question.index()));
		question.setAuthors(authors);
		String body = questionHtml(question) + "<br>"
				+ editAuthorAction.buildForm(question)
				+ removeAuthorAction.buildForm(question)
				+ href(htmlHelper.uriBuilder("/authors/list").build(), "Каталог персонажей");
		return htmlPage("Добавить автора", body);
	}

	public String removeMethod(int index) throws Exception {
		super.remove(index);
		return home();
	}

	public String addPicture(String number, String path) {
		try{
			int index = numerator().getIndex(number);
			Question q = get(index);
			q.setUnaudible(q.getUnaudible() + "\nРаздаточный материал: (img " + path + ")");
			return editForm(index);
		} catch (Exception e) {
			return error(e);
		}
	}

	public String copyTo(int index, String packNames) throws Exception {
		if (packNames == null) {
			packNames = "zapas";
		}
		super.copyTo(index, packNames);
		return editForm(index);
	}

	public String upMethod( int index) throws Exception {
		super.up(index);
		return home();
	}

	public String downMethod( int index) throws Exception {
		super.down(index);
		return home();
	}

  	public String edit(
			 int index,
			 String unaudible,
			 String color,
			 String text,
			 String answer,
			 String possibleAnswers,
			 String impossibleAnswers,
			 String comment,
			 String sources
	) throws Exception {
		Question question = get(index);
		if (question == null) {
			question = new Question();
		}
		question.setUnaudible(unaudible);
		question.setText(text);
		question.setAnswer(answer);
		question.setComment(comment);
		question.setImpossibleAnswers(impossibleAnswers);
		question.setPossibleAnswers(possibleAnswers);
		question.setSources(sources);
		question.setNumber(numerator().getNumber(index));
		question.setColor(color);
		add(index, question);
		return home();
	}

	public String editAuthor(
			 int index,
			 String author
	) {
		try {
			Question question = get(index);
			question.setAuthors(authors);
			question.addAuthor(author);
			add(index, question);
			return editForm(index);
		} catch (Exception e) {
			return error(e);
		}
	}

	public String removeAuthor(
			int index,
			String author
	) {
		Question question = get(index);
		((MultiPerson)question.getAuthor()).getPersonList().remove(authors.get(author));
		add(index, question);
		return editForm(index);
	}

	public String nextColor(
			 int index,
			 String colorHex
	) throws Exception {
		StringSerializer<Color> serializer = StringSerializer.getForClass(Color.class);
		Question question = get(index);
		Color color = Color.decode(colorHex);
		if (color.equals(Color.white)) {
			color = Color.red;
		} else if (color.equals(Color.red)) {
			color = Color.yellow;
		} else if (color.equals(Color.yellow)) {
			color = Color.green;
		} else {
			color = Color.white;
		}
		colorHex = serializer.toString(color);
		question.setColor(colorHex);
		add(index, question);
		return home();
	}

	public String text() throws IOException {
		String text4s = to4s();
		return htmlPage(getName(), "", text4s.replace("\n", "<br>"));
	}

	public String compose(String outFormat, boolean debug) throws IOException {
		StringLogger logs = new StringLogger(log, debug);
		String text4s = to4s();
		logs.debug("text generated in 4s");
		File timestampFolder = timestampFolder(folder);
		logs.debug(timestampFolder + " is created");
		String name4sFile = timestampFolder.getAbsoluteFile() + "/" + id + ".4s";
		write(new File(name4sFile), text4s);
		logs.debug("4s was written to " + name4sFile);
		//File template = copyFileToDir(propertyReader.get(("quedit.properties", "templatedocx"), timestampFolder);
		//logs.debug(template + " was created");
		String[] cmd = chgkSuiteCmd("compose", name4sFile, outFormat, "--nospoilers");
		if (call(logs, cmd) == 0) {
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
	
	private static File timestampFolder(File folder) {
		String timestamp = timestamp("yyyyMMdd_HHmmss");
		File timestampFolder = new File(folder.getAbsolutePath() + "/" + timestamp);
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
		return htmlHelper;
	}

	private ColumnTableBuilder<Question> getQuestionTable(QuestionTableBuilder.ColumnName... columnNames) throws URISyntaxException {
		ColumnTableBuilder<Question> table = initQuestionTable(columnNames);
		for (int i = 0; i < size(); i++) {
			table.addRow(get(i));
		}
		return table;
	}

	private QuestionTableBuilder initQuestionTable(QuestionTableBuilder.ColumnName... columnNames) {
		return new QuestionTableBuilder(this, columnNames);
	}

	@Override
	protected void initFields() {
		super.initFields();
	}

	private void initInfo() {
		try {
			this.info = Jsonable.fromJson(Util.read(infoPath()), PackInfo.class);
		} catch (Exception e) {
			info = new PackInfo();
			info.save(infoPath());
		}
		if (StringUtils.isBlank(getName())) {
			setName(id);
		}
	}

	private String questionHtml(Question question) {
		return question.toString().replace("\n", "<br>") + "<br>";
	}

	@Override
	public void add(Integer index, Question item) {
		super.add(index, item);
		number(index, item);
	}

	@Override
	public QuestionNumerator numerator() {
		return info.getNumerator();
	}

	@Override
	protected void swap(int key, int key2) {
		super.swap(key, key2);
		number(key, get(key2));
		number(key2, get(key));
	}

	private void number(Integer number, Question item) {
		item.setIndex(number);
		item.setNumber(numerator().getNumber(number));
	}

	public String addEditor(String name) throws Exception {
		if (authors == null) {
			throw new RuntimeException("Authors were null");
		}
		info.addAuthor(authors.get(name));
		info.save(infoPath());
		return info();
	}

	public String removeEditor(String name) throws Exception {
		if (authors == null) {
			throw new RuntimeException("Authors were null");
		}
		info.removeAuthor(authors.get(name));
		info.save(infoPath());
		return info();
	}

	public String addTester(String name) throws Exception {
		if (authors == null) {
			throw new RuntimeException("Authors were null");
		}
		info.addTester(authors.get(name));
		info.save(infoPath());
		return info();
	}
	
	public String removeTester(String name) throws Exception {
		if (authors == null) {
			throw new RuntimeException("Authors were null");
		}
		info.removeTester(authors.get(name));
		info.save(infoPath());
		return info();
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

	private String[] chgkSuiteCmd(String... parameters) throws FileNotFoundException {
		String[] cmd = new String[parameters.length + 2];
		cmd[0] = getProperty("python");
		cmd[1] = getProperty("chgksuite");
		for (int i = 0; i < parameters.length; i++) {
			cmd[i + 2] = parameters[i];
		}
		return cmd;
	}

	@Override
	public String to4s() {
		StringBuilder sb = new StringBuilder();
		sb.append(info.to4s()).append('\n');
		for (Question question : super.getAll()) {
			numerator().renumber(question);
			sb.append(question.to4s()).append('\n');
		}
		return sb.toString();
	}

	@Override
	public Person getAuthor() {
		return info.getAuthor();
	}
	
	@Override
	public void setAuthor(MultiPerson author) {
		info.setAuthor(author);
		info.save(infoPath());
	}

	public String getMetaInfo() {
		return info.getMetaInfo();
	}

	public void setMetaInfo(String metaInfo) {
		info.setMetaInfo(metaInfo);
		info.save(infoPath());
	}

	public void setName(String name) {
		info.setName(name);
		info.save(infoPath());
	}

	public String getNameLJ() {
		return info.getNameLJ();
	}

	public void setNameLJ(String nameLJ) {
		info.setNameLJ(nameLJ);
		info.save(infoPath());
	}

	public String getDate() {
		return info.getDate();
	}

	public void setDate(String date) {
		info.setDate(date);
		info.save(infoPath());
	}

	public String getName() {
		return info.getName();
	}

	public void setNumerator(QuestionNaturalNumerator numerator) {
		info.setNumerator(numerator);
		info.save(infoPath());
	}
}
