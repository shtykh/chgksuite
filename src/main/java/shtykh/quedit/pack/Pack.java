package shtykh.quedit.pack;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.sun.research.ws.wadl.HTTPMethods;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.web.multipart.MultipartFile;
import shtykh.quedit._4s.Parser4s;
import shtykh.quedit._4s._4Sable;
import shtykh.quedit.author.Authored;
import shtykh.quedit.author.MultiPerson;
import shtykh.quedit.author.Person;
import shtykh.quedit.numerator.Numerator;
import shtykh.quedit.numerator.QuestionNaturalNumerator;
import shtykh.quedit.question.Question;
import shtykh.rest.AuthorsCatalogue;
import shtykh.rest.PackController;
import shtykh.util.Jsonable;
import shtykh.util.StringSerializer;
import shtykh.util.Util;
import shtykh.util.catalogue.ListCatalogue;
import shtykh.util.html.ColumnTableBuilder;
import shtykh.util.html.HtmlHelper;
import shtykh.util.html.QuestionTableBuilder;
import shtykh.util.html.UriGenerator;
import shtykh.util.html.form.build.FormBuilder;
import shtykh.util.html.form.material.FormMaterial;
import shtykh.util.html.form.param.FormParameter;
import shtykh.util.html.form.param.FormParameterSignature;

import javax.ws.rs.core.Response;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;

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
	private final HtmlHelper html;
	private final AuthorsCatalogue authors;
	private final PackController packs;
	private PackInfo info;
	private PackView view;

	public Pack(String id, HtmlHelper html, AuthorsCatalogue authors, PackController packs) throws FileNotFoundException, URISyntaxException {
		super(Question.class);
		this.id = id;
		this.html = html;
		this.authors = authors;
		this.packs = packs;
		setProperties(packs.getProperties());
		afterRun();
		initInfo();
		view = new PackView(id, html, packs, authors, this);
	}

	@Override
	protected String folderName() {
		return getProperty("packs") + "/" + id;
	}

	public String home() throws Exception {
		return view.home();
	}
	
	public String info() {
		return view.info();
	}

	@Override
	protected String folderNameKey() {
		return "packs";
	}

	@Override
	public void refresh() {
		try {
			super.refresh();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
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

	public String uploadForm(String what) throws URISyntaxException {
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
			File timestampFolder = timestampFolder(folderPath);
			clearFolder();
			File file = saveFile(multipartFile, timestampFolder.getAbsolutePath(), id + ".docx");
			from4sFile(file);
			return home();
		} catch (Exception e) {
			return error(e);
		}
	}

	private void from4sFile(File file) throws Exception {
		Parser4s parser4s = new Parser4s(file.getAbsolutePath(), folderPath() + "/pics");
		this.fromParser(parser4s);
		file.delete();
	}

	public String upload_docx(MultipartFile multipartFile) {
		String folderPath = folder + "/import";
		try {
			File timestampFolder = timestampFolder(folderPath);
			clearFolder();
			File fileDocx = saveFile(multipartFile, timestampFolder.getAbsolutePath(), id + ".docx");
			StringLogger logs = new StringLogger(log, parseBoolean(getProperty("debug")));
			logs.info("File saved to server location : " + file);
			String[] cmd = chgkSuiteCmd("parse", fileDocx.getAbsolutePath());
			if (call(logs, cmd) == 0) {
				File file4s = new File(timestampFolder.getAbsoluteFile() + "/" + id + ".4s");
				copyFilesToDir(timestampFolder, folder(folder.getAbsolutePath(), "/pics"), new SuffixFileFilter(new String[]{"png", "jpg", "jpeg"}));
				from4sFile(file4s);
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
			File folder = new File(folderPath);
			File file = saveFile(multipartFile, folder.getAbsolutePath(), multipartFile.getName());
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

	public void fromParser(Parser4s parser4s) {
		info = parser4s.getInfo();
		if (StringUtils.isBlank(info.getName())) {
			info.setName(id);
		}
		info.save(infoPath());
		for (Question question : parser4s.getQuestions()) {
			add(question);
		}
		authors.addAll(parser4s.getPersons());
	}

	public String editForm(int index) {
		return view.editForm(index);
	}

	public String editAuthorForm(int index) throws Exception {
		return view.editAuthorForm(index);
	}

	public String editCommonAuthorForm() throws Exception {
		return view.editCommonAuthorForm();
	}

	public String removeMethod(int index) throws Exception {
		super.remove(index);
		return home();
	}

	public String addPicture(String number, String path) {
		try{
			int index = getNumerator().getIndex(number);
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
		question.setNumber(getNumerator().getNumber(index));
		question.setColor(color);
		add(index, question);
		return editForm(index);
	}

	public String editAuthor(
			 Integer index,
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

	public String editCommonAuthor(
			String author
	) {
		try {
			for (int i = 0; i < getAll().size(); i++) {
				editAuthor(i, author);
			}
			return home();
		} catch (Exception e) {
			return error(e);
		}
	}

	public String removeAuthor(
			Integer index,
			String author
	) {
		try {

			if (index == null) {
				for (int i = 0; i < getAll().size(); i++) {
					removeAuthor(i, author);
				}
				return home();
			}
			Question question = get(index);
			((MultiPerson)question.getAuthor()).getPersonList().remove(authors.get(author));
			add(index, question);
			return editForm(index);
		} catch (Exception e) {
			return error(e);
		}
	}

	public String removeCommonAuthor(
			String author
	) {
		try {
			for (int i = 0; i < getAll().size(); i++) {
				removeAuthor(i, author);
			}
			return home();
		} catch (Exception e) {
			return error(e);
		}
	}

	public String nextColor(
			 int index,
			 String colorHex
	) throws Exception {
		Color color = Color.decode(colorHex);
		if (color.equals(Color.white)) {
			color = Color.red;
		} else if (color.equals(Color.red)) {
			color = Color.green;
		} else {
			color = Color.white;
		}
		StringSerializer<Color> serializer = StringSerializer.getForClass(Color.class);
		colorHex = serializer.toString(color);
		Question question = get(index);
		question.setColor(colorHex);
		add(index, question);
		return home();
	}
	
	public String split(String methodName) throws Exception {
		try{
			Multimap<String, Question> map = ArrayListMultimap.create();
			Method method = findMethodByName(Question.class, methodName);
			for (Question question : getAll()) {
				Object result = method.invoke(question);
				String key = result == null ? "null" : result.toString();
				map.put(key, question);
			}
			for (String key : map.keySet()) {
				packs.addPack(id + "_" + key, map.get(key));
			}
			return home();
		} catch (Exception e) {
			return errorPage("Не удалось разбить " + id + " на подпакеты по признаку " + methodName);
		}
	}

	public String text() throws IOException {
		String text4s = to4s();
		return htmlPage(getName(), "", text4s.replace("\n", "<br>"));
	}

	public String compose(String outFormat, boolean debug) throws IOException {
		StringLogger logs = new StringLogger(log, debug);
		String text4s = to4s();
		logs.debug("text generated in 4s");
		File timestampFolder = timestampFolder(folder.getAbsolutePath());
		logs.debug(timestampFolder + " is created");
		String name4sFile = timestampFolder.getAbsoluteFile() + "/" + id + ".4s";
		write(new File(name4sFile), text4s);
		logs.debug("4s was written to " + name4sFile);
		//File template = copyFileToDir(propertyReader.get(("quedit.properties", "templatedocx"), timestampFolder);
		//logs.debug(template + " was created");
		String[] cmd = chgkSuiteCmd("compose", outFormat, name4sFile, "--nospoilers");
		int result = call(logs, cmd);
		return view.compose(result, logs, timestampFolder, outFormat);
	}
	
	private static File timestampFolder(String folder) {
		String timestamp = timestamp("yyyyMMdd_HHmmss");
		return folder(folder, timestamp);
	}

	private static File folder(String folder, String name) {
		File newFolder = new File(folder + "/" + name);
		newFolder.mkdirs();
		return newFolder;
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
		ColumnTableBuilder<Question> table = new QuestionTableBuilder(this, columnNames);
		for (int i = 0; i < size(); i++) {
			table.addRow(get(i));
		}
		return table;
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

	@Override
	public void add(Integer index, Question item) {
		super.add(index, item);
		getNumerator().number(index, item);
	}

	@Override
	public Numerator<Question> getNumerator() {
		return info.getNumerator();
	}

	@Override
	protected void swap(int key, int key2) {
		super.swap(key, key2);
		getNumerator().number(key, get(key2));
		getNumerator().number(key2, get(key));
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
			getNumerator().renumber(question);
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
