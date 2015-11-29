package shtykh.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import shtykh.quedit.pack.Pack;
import shtykh.quedit.question.Question;
import shtykh.util.CSV;
import shtykh.util.catalogue.FolderKeaper;
import shtykh.util.html.HtmlHelper;
import shtykh.util.html.TableBuilder;
import shtykh.util.html.UriGenerator;
import shtykh.util.html.form.material.FormMaterial;
import shtykh.util.html.form.material.FormParameterMaterial;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import static shtykh.util.Util.findMethodByName;
import static shtykh.util.html.HtmlHelper.*;

/**
 * Created by shtykh on 01/10/15.
 */

@Controller
public class PackController extends FolderKeaper implements FormMaterial, UriGenerator {
	private static final Logger log = LoggerFactory.getLogger(PackController.class);
	protected FormParameterMaterial<CSV> packNames = new FormParameterMaterial<>(new CSV(""), CSV.class);
	private Map<String, Pack> packs = new TreeMap<>();

	private HtmlHelper htmlHelper;
	@Autowired
	public void setHtmlHelper(HtmlHelper htmlHelper) {
		this.htmlHelper = htmlHelper;
		log.info("Check me out at " + uri(""));
	}
	
	@Autowired
	private AuthorsCatalogue authors;

	public PackController() throws FileNotFoundException {
		super();
	}

	@Override
	protected String folderNameKey() {
		return "packs";
	}

	@Override
	protected void clearCash() {
		packs.clear();
	}

	@Override
	public void refreshFile(File file) {
		addPack(file.getName());
	}

	@Override
	public void refresh() throws Exception {
		super.refresh();
		packNames.set(new CSV(packs.keySet()));
	}

	@Override
	public boolean isGood(File file) {
		return file.isDirectory();
	}

	private String getOr404(String id, String methodName, Object... args) {
		Pack pack = packs.get(id);
		if (pack == null) {
			try {
				refresh();
				pack = packs.get(id);
			} catch (Exception e) {
				return error(e);
			}
			if (pack == null) {
				return errorPage("Пакет " + id + " не был найден в папке " + folderPath());
			}
		}
		try {
			return (String) findMethodByName(Pack.class, methodName).invoke(pack, args);
		} catch (Exception e) {
			return error(e);
		}
	}

	@ResponseBody
	@RequestMapping("packs")
	public String all() throws Exception {
		refresh();
		TableBuilder table = new TableBuilder();
		for (String id : packs.keySet()) {
			URI editUri = htmlHelper.uriBuilder("/" + id).build();
			table.addRow(href(editUri, packs.get(id).getName()));
		}
		URI addUri = htmlHelper.uriBuilder("/new").build();
		table.addRow(href(addUri, "Новый пакет"));
		return htmlPage("Пакеты", table.buildHtml());
	}

	@ResponseBody
	@RequestMapping("{id}")
	public String getPack(@PathVariable("id") String id) throws Exception {
		String namePattern = getProperty("namePattern");
		if (id.equals("new")) {
			return htmlPage("Новый пакет", "Впишите id пакета в адресную строку вместо \"new\"<br>Формат имён : " + namePattern);
		}
		Pack pack = packs.get(id);
		if (pack == null) {
			if (!id.matches(namePattern)) {
				return htmlPage("Придумайте id попроще!", namePattern);
			}
			pack = addPack(id);
		}
		return pack.home();
	}

	@ResponseBody
	@RequestMapping("{id}/split/{method}")
	public String splitColor(@PathVariable("id") String id, @PathVariable("method") String method) throws IOException {
		return getOr404(id, "split", method);
	}

	@ResponseBody
	@RequestMapping("{id}/info")
	public String info(@PathVariable("id") String id) throws IOException {
		return getOr404(id, "info");
	}

	public void addPack(String key, Collection<Question> questions) {
		Pack pack = addPack(key);
		pack.addAll(questions);
	}

	private Pack addPack(String id) {
		try{
			Pack pack = packs.get(id);
			if (pack == null) {
				pack = new Pack(id, htmlHelper, authors, this, getProperties());
				pack.refresh();
				packs.put(id, pack);
			}
			return pack;
		} catch (Exception e) {
			return null;
		}
	}

	@ResponseBody
	@RequestMapping("{id}/editPack")
	public String editPack(
			@PathVariable("id") String id,
			@RequestParam("name") String name,
			@RequestParam("nameLJ") String nameLJ,
			@RequestParam("date") String date,
			@RequestParam("first") int first,
			@RequestParam("zeroNumbers") String zeroNumbers,
			@RequestParam("metaInfo") String metaInfo
	) {
		return getOr404(id, "editPack", name, nameLJ, date, metaInfo, first, zeroNumbers);
	}

	@ResponseBody
	@RequestMapping("{id}/addPicture")
	public String addPicture(
			@PathVariable("id") String id,
			@RequestParam("number") String number,
			@RequestParam("path") String path
			) {
		return getOr404(id, "addPicture", number, path);
	}

	@ResponseBody
	@RequestMapping("{id}/uploadForm/{what}")
	public String uploadForm(@PathVariable("id") String id, @PathVariable("what") String what) {
		return getOr404(id, "uploadForm", what);
	}

	@RequestMapping(value = "{id}/upload/{what}", method = RequestMethod.POST)
	@ResponseBody
	public String uploadFile(
			@PathVariable("id") String id,
			@PathVariable("what") String what,
			@RequestParam("file") MultipartFile file) {
		if (what == null) {
			what = "file";
		}
		return getOr404(id, "upload_" + what, file);
	}

	@ResponseBody
	@RequestMapping("{id}/editForm")
	public String editForm(@PathVariable("id") String id, @RequestParam("index") int index) {
		return getOr404(id, "editForm", index);
	}

	@ResponseBody
	@RequestMapping("{id}/editAuthorForm")
	public String editAuthorForm(@PathVariable("id") String id, @RequestParam("index") int index) throws URISyntaxException {
		return getOr404(id, "editAuthorForm", index);
	}

	@ResponseBody
	@RequestMapping("{id}/editCommonAuthorForm")
	public String editCommonAuthorForm(@PathVariable("id") String id) throws URISyntaxException {
		return getOr404(id, "editCommonAuthorForm");
	}

	@ResponseBody
	@RequestMapping("{id}/remove")
	public String removeMethod(@PathVariable("id") String id, @RequestParam("index") int index) {
		return getOr404(id, "removeMethod", index);
	}

	@ResponseBody
	@RequestMapping("{id}/copyTo")
	public String replace(@PathVariable("id") String id, 
						  @RequestParam("index") int index, 
						  @RequestParam("packNames") String packNames) {
		return getOr404(id, "copyTo", index, packNames);
	}

	@ResponseBody
	@RequestMapping("{id}/up")
	public String upMethod(@PathVariable("id") String id, @RequestParam("index") int index) {
		return getOr404(id, "upMethod", index);
	}

	@ResponseBody
	@RequestMapping("{id}/down")
	public String downMethod(@PathVariable("id") String id, @RequestParam("index") int index) {
		return getOr404(id, "downMethod", index);
	}

	@ResponseBody
	@RequestMapping("{id}/edit")
	public String edit(
			@PathVariable("id") String id,
			@RequestParam("index") int index,
			@RequestParam("unaudible") String unaudible,
			@RequestParam("color") String color,
			@RequestParam("text") String text,
			@RequestParam("answer") String answer,
			@RequestParam("possibleAnswers") String possibleAnswers,
			@RequestParam("impossibleAnswers") String impossibleAnswers,
			@RequestParam("comment") String comment,
			@RequestParam("sources") String sources
	) {
		return getOr404(id, "edit", index, unaudible, color, text, answer, possibleAnswers, impossibleAnswers, comment, sources);
	}

	@ResponseBody
	@RequestMapping("{id}/editAuthor")
	public String editAuthor(
			@PathVariable("id") String id,
			@RequestParam("index") Integer index,
			@RequestParam("keys") String author
	) {
		return getOr404(id, "editAuthor", index, author);
	}

	@ResponseBody
	@RequestMapping("{id}/editCommonAuthor")
	public String editCommonAuthor(
			@PathVariable("id") String id,
			@RequestParam("keys") String author
	) {
		return getOr404(id, "editCommonAuthor", author);
	}

	@ResponseBody
	@RequestMapping("{id}/removeAuthor")
	public String removeAuthor(
			@PathVariable("id") String id,
			@RequestParam("index") Integer index,
			@RequestParam("keys") String author
	) {
		return getOr404(id, "removeAuthor", index, author);
	}

	@ResponseBody
	@RequestMapping("{id}/removeCommonAuthor")
	public String removeCommonAuthor(
			@PathVariable("id") String id,
			@RequestParam("keys") String author
	) {
		return getOr404(id, "removeCommonAuthor", author);
	}

	@ResponseBody
	@RequestMapping("{id}/addEditor")
	public String addEditor(
			@PathVariable("id") String id,
			@RequestParam("keys") String author
	) {
		return getOr404(id, "addEditor", author);
	}

	@ResponseBody
	@RequestMapping("{id}/addTester")
	public String addTester(
			@PathVariable("id") String id,
			@RequestParam("keys") String testerName
	) {
		return getOr404(id, "addTester", testerName);
	}

	@ResponseBody
	@RequestMapping("{id}/removeEditor")
	public String removeEditor(
			@PathVariable("id") String id,
			@RequestParam("keys") String author
	) {
		return getOr404(id, "removeEditor", author);
	}

	@ResponseBody
	@RequestMapping("{id}/removeTester")
	public String removeTester(
			@PathVariable("id") String id,
			@RequestParam("keys") String testerName
	) {
		return getOr404(id, "removeTester", testerName);
	}

	@ResponseBody
	@RequestMapping("{id}/nextColor")
	public String nextColor(
			@PathVariable("id") String id,
			@RequestParam("index") int index,
			@RequestParam("color") String colorHex
	) {
		return getOr404(id, "nextColor", index, colorHex);
	}

	@ResponseBody
	@RequestMapping("{id}/text")
	public String text(@PathVariable("id") String id) throws IOException {
		return getOr404(id, "text");
	}

	@ResponseBody
	@RequestMapping("{id}/compose")
	public String compose(@PathVariable("id") String id,
							@RequestParam("outFormat") String outFormat,
							@RequestParam("debug") boolean debug) throws IOException {
		return getOr404(id, "compose", outFormat, debug);
	}

	@ResponseBody
	@RequestMapping(value = "{id}/download/docx", method = RequestMethod.GET, produces = "application/msword")
	public FileSystemResource downloadDocFile(@PathVariable("id") String id, @RequestParam("path") String path, HttpServletResponse response) {
		response.setHeader("Content-Disposition", "attachment; filename=\"" + id + ".docx\"");
		return new FileSystemResource(path);
	}

	@Override
	public String base() {
		return "/packs";
	}

	@Override
	public HtmlHelper htmlHelper() {
		return htmlHelper;
	}

	public void refreshNames() throws Exception {
		packNames.set(new CSV(listFileNames()));
	}

//	public static void main(String[] args) throws IOException, URISyntaxException {
//		PackController packController = new PackController();
//		packController.htmlHelper = new HtmlHelper();
//		packController.authors = new AuthorsCatalogue();
//		packController.refresh();
//		Pack pack = packController.packs.get("rudn_cup");
//		pack.editPack("Кубок РУДН", "Кубок РУДН", "20 октября 2015", "", 1, "0\n00\n000");
//		for (String s : pack.info().split("<br>")) {
//			System.out.println(s + "<br>");
//		}
//	}
}
