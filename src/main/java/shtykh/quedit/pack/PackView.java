package shtykh.quedit.pack;

/**
 * Created by shtykh on 26/08/16.
 */

import com.sun.research.ws.wadl.HTTPMethods;
import shtykh.quedit.numerator.NaturalNumerator;
import shtykh.quedit.question.Question;
import shtykh.rest.AuthorsCatalogue;
import shtykh.rest.PackController;
import shtykh.util.args.PropertyReader;
import shtykh.util.catalogue.FolderKeaper;
import shtykh.util.html.*;
import shtykh.util.html.form.build.ActionBuilder;
import shtykh.util.html.form.build.ActionBuilderParameter;
import shtykh.util.html.form.build.FormBuilder;
import shtykh.util.html.form.material.FormMaterial;
import shtykh.util.html.form.param.FormParameter;
import shtykh.util.html.form.param.FormParameterSignature;
import shtykh.util.html.param.Parameter;
import shtykh.util.html.table.ColoredTableBuilder;
import shtykh.util.html.table.ColumnTableBuilder;
import shtykh.util.html.table.QuestionTableBuilder;
import shtykh.util.html.table.TableBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;

import static java.lang.Boolean.parseBoolean;
import static shtykh.rest.Locales.getString;
import static shtykh.util.Util.StringLogger;
import static shtykh.util.html.HtmlHelper.*;
import static shtykh.util.html.form.build.FormBuilder.buildUploadForm;
import static shtykh.util.html.form.param.FormParameterType.*;

/**
 * Created by shtykh on 01/10/15.
 */
public class PackView extends PropertyReader implements FormMaterial {
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
		hrefs.put("compose", getString("COMPOSE"),
				new Parameter<>("outFormat", outFormat),
				new Parameter<>("debug", debug));
		hrefs.put("uploadForm/4s", getString("IMPORT_4s"));
		hrefs.put("uploadForm/docx", getString("IMPORT_DOCX"));
		hrefs.put("uploadFormTrello", getString("IMPORT_TRELLO"));
		hrefs.put("uploadForm/pic", getString("IMPORT_PIC"));
		hrefs.put("editCommonAuthorForm", getString("AUTHOR_ALL"));
		hrefs.put("text", getString("TEXT_4S"));
		hrefs.put("split/getColor", getString("SPLIT_COLOUR"));
		hrefs.put("info", getString("EDIT_MEMO"));
		hrefs.put("", getString("2PACK", packName));
		hrefs.put(personUri, getString("2AUTHORS"));
		hrefs.put(backUri, getString("2PACKS"));
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
						href(uriNew, getString("ADD_QUEST", nextNumber)) + "<br>" +
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
			ActionBuilderParameter hiddenIndex = new ActionBuilderParameter(
					Question.class, "index", getString("NUMBER"), "index", hidden);
			replaceQuestionAction
					.addParam(PackController.class, "packNames", getString("REPLACE_TO"), select)
					.addParam(hiddenIndex);
			editQuestionAction
					.addParam(Question.class, "number", getString("NUMBER"), comment)
					.addParam(hiddenIndex)
					.addParam(Question.class, "unaudible", getString("UNAUDIBLE"), textarea)
					.addParam(Question.class, "text", getString("TEXT"), textarea)
					.addParam(Question.class, "answer", getString("ANSWER"), textarea)
					.addParam(Question.class, "possibleAnswers", getString("EQUAL_ANSWER"), textarea)
					.addParam(Question.class, "impossibleAnswers", getString("NOT_EQUAL_ANSWER"), textarea)
					.addParam(Question.class, "comment", getString("COMMENT"), textarea)
					.addParam(Question.class, "sources", getString("SOURCES"), textarea)
					.addParam(Question.class, "color", getString("COLOUR"), color)
			;
			editAuthorAction
					.addParam(FolderKeaper.class, "keys", getString("ADD_AUTH"), select)
					.addParam(hiddenIndex)
			;
			removeAuthorAction
					.addParam(FolderKeaper.class, "keys", getString("REMOVE_AUTH"), select)
					.addParam(hiddenIndex)
			;editCommonAuthorAction
					.addParam(FolderKeaper.class, "keys", getString("ADD_AUTH"), select)
			;
			removeCommonAuthorAction
					.addParam(FolderKeaper.class, "keys", getString("REMOVE_AUTH"), select)
			;
			addEditorAction
					.addParam(FolderKeaper.class, "keys", getString("ADD_EDITOR"), select)
			;
			addTesterAction
					.addParam(FolderKeaper.class, "keys", getString("ADD_TESTER"), select)
			;
			removeEditorAction
					.addParam(FolderKeaper.class, "keys", getString("REMOVE_EDITOR"), select)
			;
			removeTesterAction
					.addParam(FolderKeaper.class, "keys", getString("REMOVE_TESTER"), select)
			;
			editPackAction
					.addParam(PackInfo.class, "name", getString("PACK_NAME"), text)
					.addParam(PackInfo.class, "editor", getString("EDITOR"), comment)
					.addParam(PackInfo.class, "date", getString("DATE"), text)
					.addParam(PackInfo.class, "metaInfo", getString("MEMO"), textarea)
					.addParam(NaturalNumerator.class, "zeroNumbers", getString("NUMERATION_ZEROS"), textarea)
					.addParam(NaturalNumerator.class, "first", getString("NUMERATIONS_INIT_NUMBER"), number)
					.addParam(PackInfo.class, "nameLJ", getString("PACK_NAME_LJ"), text)
			;
		} catch (NoSuchFieldException | URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}


	public String uploadForm(String what) throws URISyntaxException {
		return htmlPage(getString("LOAD_FILE"), buildUploadForm(hrefs.address("upload" + "/" + what)));
	}

	public String upload_pic_page(File file, Collection<Question> questions) {
		try {
			FormBuilder formBuilder = new FormBuilder(hrefs.address("addPicture"));
			formBuilder
					.addMember(new FormParameter<>(
							new FormParameterSignature("path", hidden), file.getAbsolutePath(), String.class))
					.addMember(new FormParameter<>(
							new FormParameterSignature("number", getString("ADD_IMG_TO"), text), "", String.class));
			return hrefs.listResponce(
					getString("IMG_LOADED"),
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
					href(hrefs.uri("editForm", new Parameter<>("index", index - 1)), getString("BACK")),
					hrefs.get(""),
					href(uriColor, getString("CHANGE_COLOUR")),
					href(hrefs.uri("editForm", new Parameter<>("index", index + 1)), getString("FORV")));
			navigation.addColor(0, 2, questionColor);
			String body = navigation
					+ questionHtml(question)
					+ href(hrefs.uri("editAuthorForm", new Parameter<>("index", index)), getString("EDIT_AUTH"))
					+ replaceQuestionAction.buildForm(question, packs)
					+ editQuestionAction.buildForm(question, authors);
			return htmlPage(getString("EDIT_QUEST"), body);
		} catch (Exception e) {
			return error(e);
		}
	}

	public String editAuthorForm(Question question, AuthorsCatalogue authors) throws Exception {
		String body = questionHtml(question) + "<br>"
				+ editAuthorAction.buildForm(question, authors)
				+ removeAuthorAction.buildForm(question, authors)
				+ hrefs.get(personUri);
		return htmlPage(getString("ADD_AUTH"), body);
	}

	public String editCommonAuthorForm(AuthorsCatalogue authors, String id) throws Exception {
		String body = getString("EDIT_AUTH_ALL", id) + "<br>"
				+ editCommonAuthorAction.buildForm(authors)
				+ removeCommonAuthorAction.buildForm(authors)
				+ hrefs.get(personUri);
		return htmlPage(getString("AUTHOR_ALL"), body);
	}

	public String compose_result_page(int result, StringLogger logs, File timestampFolder, String outFormat) throws IOException {
		if (result == 0) {
			logs.info(getString("FILE_SAVED", outFormat, timestampFolder));
			if (outFormat.equals("docx")) {
				String path = getPath(timestampFolder, outFormat);
				try {
					URI downloadHref = hrefs.uri("download/docx", new Parameter<>("path", path));
					logs.info(href(downloadHref, getString("DOWNLOAD")));
				} catch (Exception e) {
					logs.error(e.getMessage());
				}
			}
		}
		return htmlPage(getString("COMPOSITION"), logs.toString().replace("\n", "<br>"));
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

