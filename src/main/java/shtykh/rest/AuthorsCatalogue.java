package shtykh.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import shtykh.quedit.author.SinglePerson;
import shtykh.util.catalogue.MapCatalogue;
import shtykh.util.html.HtmlHelper;
import shtykh.util.html.table.TableBuilder;
import shtykh.util.html.UriGenerator;
import shtykh.util.html.form.build.ActionBuilder;
import shtykh.util.html.param.Parameter;

import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;

import static shtykh.rest.Locales.getString;
import static shtykh.util.html.HtmlHelper.*;
import static shtykh.util.html.form.param.FormParameterType.text;

/**
 * Created by shtykh on 01/10/15.
 */

@Controller
@RequestMapping("/authors")
public class AuthorsCatalogue extends MapCatalogue<SinglePerson> implements UriGenerator {

	@Autowired
	private HtmlHelper htmlHelper;

	private ActionBuilder editPersonAction() {
		ActionBuilder editPersonAction = new ActionBuilder("/authors/edit");
		try {
			editPersonAction.addParam(SinglePerson.class, "firstName", getString("FIRST_NAME"), text)
					.addParam(SinglePerson.class, "lastName", getString("LAST_NAME"), text)
					.addParam(SinglePerson.class, "city", getString("CITY"), text);
			return editPersonAction;
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	public AuthorsCatalogue() throws FileNotFoundException {
		super(SinglePerson.class);
	}

	@Override
	protected String folderNameKey() {
		return "authors";
	}

	@ResponseBody
	@RequestMapping("list")
	public String list() throws Exception {
		refresh();
		String name = "Персонажи";
		TableBuilder table;
		URI uriList;
		URI uriNew;
		try {
			table = getPersonTable();
			uriList = uri("list");
			uriNew = uri("editform");
		} catch (URISyntaxException e) {
			return error(e);
		}
		String href = href(uriList, name);
		String body = folder.getAbsolutePath() + "<br>" + table.toString() + "<br>" + href(uriNew, getString("ADD"));
		return htmlPage(name, href, body);
	}

	private TableBuilder getPersonTable() throws Exception {
		TableBuilder table = new TableBuilder(getString("PERSON"), getString("EDIT"), getString("REMOVE"));
		for (String name : keys()) {
			URI uriEdit = uri("editform", new Parameter<>("name", name));
			URI uriRemove = uri("remove", new Parameter<>("name", name));
			table.addRow(name, href(uriEdit, getString("EDIT")), href(uriRemove, getString("REMOVE")));
		}
		return table;
	}

	@ResponseBody
	@RequestMapping("editform")
	public String editForm(@RequestParam(value = "name", required = false) String name) {
		SinglePerson person = get(name);
		if (person == null) {
			person = SinglePerson.mock();
		}
		return htmlPage(getString("PLEASE_EDIT"), editPersonAction().buildForm(person));
	}

	@ResponseBody
	@RequestMapping("remove")
	public String removeMethod(@RequestParam("name") String name) throws Exception {
		super.remove(name);
		return list();
	}

	@ResponseBody
	@RequestMapping("/edit")
	public String edit(@RequestParam("firstName") String name, @RequestParam("lastName") String lastName, @RequestParam("city") String city) throws Exception {
		SinglePerson p = new SinglePerson(name, lastName, city);
		add(p);
		return list();
	}

	@Override
	protected String getFileName(SinglePerson p) {
		return p.toString();
	}

	@Override
	public String base() {
		return "/authors";
	}

	@Override
	public HtmlHelper htmlHelper() {
		return htmlHelper;
	}
}
