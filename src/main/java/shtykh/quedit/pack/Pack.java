package shtykh.quedit.pack;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.log4j.Logger;
import org.springframework.web.multipart.MultipartFile;
import shtykh.quedit._4s.Parser4s;
import shtykh.quedit._4s._4Sable;
import shtykh.quedit.author.Authored;
import shtykh.quedit.author.MultiPerson;
import shtykh.quedit.author.Person;
import shtykh.quedit.numerator.QuestionNaturalNumerator;
import shtykh.quedit.question.Question;
import shtykh.rest.AuthorsCatalogue;
import shtykh.rest.PackController;
import shtykh.util.CMD;
import shtykh.util.StringSerializer;
import shtykh.util.html.HtmlHelper;
import shtykh.util.html.form.material.FormMaterial;
import shtykh.util.synth.Synthesator;

import javax.ws.rs.core.Response;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.Collection;

import static java.lang.Boolean.parseBoolean;
import static shtykh.rest.StringConstants.getString;
import static shtykh.util.Util.*;
import static shtykh.util.html.HtmlHelper.*;
import static shtykh.util.html.form.param.FormParameterType.file;

/**
 * Created by shtykh on 01/10/15.
 */
public class Pack implements FormMaterial, _4Sable, Authored {
	private static final Logger log = Logger.getLogger(Pack.class);
	private final String id;
	private final AuthorsCatalogue authors;
	private final PackController packs;
	private Questions questions;
	private PackView view;
	
	public Pack(String id, HtmlHelper html, AuthorsCatalogue authors, PackController packs) throws FileNotFoundException, URISyntaxException {
		this.id = id;
		this.authors = authors;
		if (authors == null) {
			throw new RuntimeException("Authors were null");
		}
		this.packs = packs;
		this.questions = new Questions(packs.getProperties(), id);
		view = new PackView(id, html, packs, this);
	}

	protected String folderName() {
		return questions.folderName();
	}

	public String home() throws Exception {
		refresh();
		return view.home(getName(), size(), questions.getNumber(size()), getAll());
	}

	private Collection<Question> getAll() {
		return questions.getAll();
	}

	private int size() {
		return questions.size();
	}

	public String info() throws Exception {
		refresh();
		return view.infoPage(getInfo(), authors);
	}
	
	public void refresh() throws Exception {
		authors.refresh();
		questions.refresh();
	}

	public String editPack(
			 String name,
			 String nameLJ,
			 String date,
			 String metaInfo,
			 int first,
			 String zeroNumbers
	) throws Exception {
		questions.setName(name);
		questions.setNameLJ(nameLJ);
		questions.setDate(date);
		questions.setMetaInfo(metaInfo);
		questions.setNumerator(new QuestionNaturalNumerator(first, zeroNumbers));
		questions.saveInfo();
		return info();
	}

	public String uploadForm(String what) throws URISyntaxException {
		return view.uploadForm(what);
	}

	public String upload_file(MultipartFile multipartFile) {
		try{
			String folderName = folderName() + "/uploads";
			File file = saveFile(multipartFile, folderName, "file");
			return "File saved to server location : " + file;
		} catch (Exception e) {
			return error(e);
		}
	}

	public String upload_4s(MultipartFile multipartFile) {
		String folderName = folderName() + "/import";
		try {
			File timestampFolder = timestampFolder(folderName);
			questions.clearFolder();
			File file = saveFile(multipartFile, timestampFolder.getAbsolutePath(), id + ".docx");
			from4sFile(file);
			return home();
		} catch (Exception e) {
			return error(e);
		}
	}

	private void from4sFile(File file) throws Exception {
		Parser4s parser4s = new Parser4s(file.getAbsolutePath(), folderName() + "/pics");
		this.fromParser(parser4s);
		file.delete();
	}

	private void fromParser(Parser4s parser4s) {
		setInfo(parser4s.getInfo());
		addAll(parser4s.getQuestions());
		authors.addAll(parser4s.getPersons());
	}

	public void setInfo(PackInfo info) {
		questions.setInfo(info);
		questions.saveInfo();
	}

	public String upload_docx(MultipartFile multipartFile) {
		String folderName = folderName() + "/import";
		try {
			File timestampFolder = timestampFolder(folderName);
			questions.clearFolder();
			File fileDocx = saveFile(multipartFile, timestampFolder.getAbsolutePath(), id + ".docx");
			StringLogger logs = new StringLogger(log, parseBoolean(questions.getProperty("debug")));
			logs.info("File saved to server location : " + file);
			CMD cmd = chgkSuiteCmd("parse", fileDocx.getAbsolutePath());
			if (cmd.call(logs) == 0) {
				File file4s = new File(timestampFolder.getAbsoluteFile() + "/" + id + ".4s");
				copyFilesToDir(
						timestampFolder,
						folder(folderName(), "/pics"),
						new SuffixFileFilter(new String[]{"png", "jpg", "jpeg"})); // todo put in a property
				from4sFile(file4s);
				return home();
			} else {
				return htmlPage(
						"Загрузка из " + multipartFile.getOriginalFilename(),
						logs.toString().replace("\n", "<br>"));
			}
		} catch (Exception e) {
			return error(e);
		}
	}

	public String upload_pic(MultipartFile multipartFile) {
		String folderName = folderName() + "/pics";
		try {
			File folder = new File(folderName);
			File file = saveFile(multipartFile, folder.getAbsolutePath(), multipartFile.getName());
			return view.upload_pic_page(file, getAll());
		} catch (Exception e) {
			return error(e);
		}
	}

	public void addAll(Collection<Question> questions) {
		this.questions.addAll(questions);
	}

	public String editForm(int index) {
		return view.editForm(getRefreshed(index), packs, authors);
	}

	public String editAuthorForm(int index) throws Exception {
		Question question = getRefreshed(index);
		return view.editAuthorForm(question, authors);
	}
	
	private Question getRefreshed(int index) {
		try{
			Question question = questions.get(index);
			if (question == null) {
				question = Question.mock();
				question.newIndex(size());
			} else {
				question.newIndex(index);
			}
			questions.renumber(question);
			return question;
		} catch (Exception e) {
			return null;
		}
	}

	public String editCommonAuthorForm() throws Exception {
		authors.refresh();
		return view.editCommonAuthorForm(authors, id);
	}

	public String removeMethod(int index) throws Exception {
		questions.remove(index);
		return home();
	}

	public String addPicture(String number, String path) {
		try{
			int index = questions.getIndex(number);
			Question q = questions.get(index);
			q.appendUnaudible(getString("IMG_COMMENT", path));
			return editForm(index);
		} catch (Exception e) {
			return error(e);
		}
	}

	public String copyTo(int index, String packNames) throws Exception {
		if (packNames == null) {
			packNames = "zapas";
		}
		questions.copyTo(index, packNames);
		return editForm(index);
	}

	public String upMethod( int index) throws Exception {
		questions.up(index);
		return home();
	}

	public String downMethod( int index) throws Exception {
		questions.down(index);
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
		authors.refresh();
		Question question = questions.get(index);
		if (question == null) {
			question = new Question();
			question.setIndex(index);
		}
		question.setUnaudible(unaudible);
		question.setText(text);
		question.setAnswer(answer);
		question.setComment(comment);
		question.setImpossibleAnswers(impossibleAnswers);
		question.setPossibleAnswers(possibleAnswers);
		question.setSources(sources);
		question.setColor(color);
		add(index, question);
		return editForm(index);
	}

	public String editAuthor(
			 Integer index,
			 String author
	) {
		try {
			editAuthorInternal(index, author);
			return editForm(index);
		} catch (Exception e) {
			return error(e);
		}
	}

	private void editAuthorInternal(Integer index, String author) throws Exception{
		Question question = getRefreshed(index);
		question.addAuthor(authors.get(author));
		add(index, question);
	}


	public String editCommonAuthor(
			String author
	) {
		try {
			for (int i = 0; i < getAll().size(); i++) {
				editAuthorInternal(i, author);
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
			Question question = getRefreshed(index);
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
		Question question = getRefreshed(index);
		question.setColor(colorHex);
		add(index, question);
		return home();
	}
	
	public String split(String methodName) {
		try{
			Multimap<String, Question> map = ArrayListMultimap.create();
			Method method = findMethodByName(Question.class, methodName);
			for (Question question : getAll()) {
				Object result = method.invoke(question);
				String key = String.valueOf(result);
				map.put(key, question);
			}
			for (String key : map.keySet()) {
				packs.addPack(id + "_" + key, map.get(key), getInfo().copy().appendName("_" + key));
			}
			return packs.all();
		} catch (Exception e) {
			return errorPage(getString("SPLIT_FAILED", id, methodName));
		}
	}

	public String read(Synthesator reader, int index) {
		try{
			Question q = getRefreshed(index);
			reader.say(q.to4s());
			return home();
		} catch (Exception e) {
			return error(e);
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
		File timestampFolder = timestampFolder(folderName());
		logs.debug(timestampFolder + " is created");
		String name4sFile = timestampFolder.getAbsoluteFile() + "/" + id + ".4s";
		write(new File(name4sFile), text4s);
		logs.debug("4s was written to " + name4sFile);
		CMD cmd = chgkSuiteCmd("compose", outFormat, name4sFile, "--nospoilers");
		int result = cmd.call(logs);
		return view.compose_result_page(result, logs, timestampFolder, outFormat);
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

	public void add(Integer index, Question item) {
		questions.add(index, item);
		questions.number(index, item);
	}

	public String addEditor(String name) throws Exception {
		getInfo().addAuthor(authors.get(name));
		questions.saveInfo();
		return info();
	}

	public String removeEditor(String name) throws Exception {
		getInfo().removeAuthor(authors.get(name));
		questions.saveInfo();
		return info();
	}

	public String addTester(String name) throws Exception {
		getInfo().addTester(authors.get(name));
		questions.saveInfo();
		return info();
	}
	
	public String removeTester(String name) throws Exception {
		getInfo().removeTester(authors.get(name));
		questions.saveInfo();
		return info();
	}

	private CMD chgkSuiteCmd(String... parameters) throws FileNotFoundException {
		return new CMD(questions.getProperty("python"))
				.with(questions.getProperty("chgksuite"))
				.with(parameters);
	}

	@Override
	public String to4s() {
		StringBuilder sb = new StringBuilder();
		sb.append(getInfo().to4s()).append('\n');
		for (Question question : questions.getAll()) {
			questions.renumber(question);
			sb.append(question.to4s()).append('\n');
		}
		return sb.toString();
	}

	@Override
	public Person getAuthor() {
		return getInfo().getAuthor();
	}
	
	@Override
	public void setAuthor(MultiPerson author) {
		getInfo().setAuthor(author);
		questions.saveInfo();
	}

	public PackInfo getInfo() {
		return questions.getInfo();
	}

	public String getName() {
		return questions.getName();
	}
}
