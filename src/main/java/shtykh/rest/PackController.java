package shtykh.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import shtykh.quedit.pack.Pack;
import shtykh.util.catalogue.FolderKeaper;
import shtykh.util.html.HtmlHelper;
import shtykh.util.html.TableBuilder;

import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.TreeMap;

import static shtykh.util.html.HtmlHelper.href;
import static shtykh.util.html.HtmlHelper.htmlPage;

/**
 * Created by shtykh on 01/10/15.
 */

@Controller
public class PackController extends FolderKeaper {
	private static final Logger log = LoggerFactory.getLogger(PackController.class);
	private Map<String, Pack> packs = new TreeMap<>();

	private HtmlHelper htmlHelper;
	@Autowired
	public void setHtmlHelper(HtmlHelper htmlHelper) {
		this.htmlHelper = htmlHelper;
		try {
			log.info("Check me out at " + htmlHelper.uriBuilder("/packs").build());
		} catch (URISyntaxException ignored) {
		}
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
		try {
			addPack(file.getName());
		}
		catch (FileNotFoundException ignored) {}
	}

	@Override
	public boolean isGood(File file) {
		return file.isDirectory();
	}

	private String getOr404(String id, String methodName, Object... args) {
		Pack pack = packs.get(id);
		if (pack == null) {
			return (String) Response.status(404).build().getEntity();
		} else {
			try {
				return (String) findMethodByName(Pack.class, methodName).invoke(pack, args);
			} catch (NoSuchMethodException | InvocationTargetException | ClassCastException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private Method findMethodByName(Class<?> clazz, String methodName) throws NoSuchMethodException{
		for (Method method : clazz.getMethods()) {
			if (method.getName().equals(methodName)) {
				return method;
			}
		}
		throw new NoSuchMethodException(clazz.toString() + "::" + methodName);
	}

	@ResponseBody
	@RequestMapping("packs")
	public String all() throws IOException, URISyntaxException {
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
	public String getPack(@PathVariable("id") String id) throws IOException {
		if (id.equals("new")) {
			return htmlPage("Новый пакет", "Впишите id пакета в адресную строку вместо \"new\"");
		}
		Pack pack = packs.get(id);
		if (pack == null) {
			String namePattern = getProperty("namePattern");
			if (!id.matches(namePattern)) {
				return htmlPage("Придумайте id попроще!", namePattern);
			}
			pack = addPack(id);
		}
		return pack.home();
	}

	@ResponseBody
	@RequestMapping("{id}/info")
	public String info(@PathVariable("id") String id) throws IOException {
		return getOr404(id, "info");
	}

	private Pack addPack(String id) throws FileNotFoundException {
		Pack pack = new Pack(id, htmlHelper, authors, getProperties());
		packs.put(id, pack);
		return pack;
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
	@RequestMapping("{id}/remove")
	public String removeMethod(@PathVariable("id") String id, @RequestParam("index") int index) {
		return getOr404(id, "removeMethod", index);
	}

	@ResponseBody
	@RequestMapping("{id}/replace")
	public String replace(@PathVariable("id") String id, @RequestParam("index") int index) {
		return getOr404(id, "replace", index);
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
			@RequestParam("index") int index,
			@RequestParam("keys") String author
	) {
		return getOr404(id, "editAuthor", index, author);
	}

	@ResponseBody
	@RequestMapping("{id}/removeAuthor")
	public String removeAuthor(
			@PathVariable("id") String id,
			@RequestParam("index") int index,
			@RequestParam("keys") String author
	) {
		return getOr404(id, "removeAuthor", index, author);
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
	public FileSystemResource downloadDocFile(@PathVariable("id") String id, @RequestParam("path") String path) {
		return new FileSystemResource(path);
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
