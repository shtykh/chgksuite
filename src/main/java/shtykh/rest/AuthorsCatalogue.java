package shtykh.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import shtykh.quedit.author.SinglePerson;
import shtykh.util.Util;
import shtykh.util.catalogue.MapCatalogue;
import shtykh.util.html.HtmlHelper;
import shtykh.util.html.TableBuilder;
import shtykh.util.html.UriGenerator;
import shtykh.util.html.form.build.ActionBuilder;
import shtykh.util.html.param.Parameter;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;

import static shtykh.util.html.HtmlHelper.href;
import static shtykh.util.html.HtmlHelper.htmlPage;
import static shtykh.util.html.form.param.FormParameterType.text;

/**
 * Created by shtykh on 01/10/15.
 */

@Component
@Path("/authors")
public class AuthorsCatalogue extends MapCatalogue<SinglePerson> implements UriGenerator {

	@Autowired
	private HtmlHelper htmlHelper;
	
	public AuthorsCatalogue() throws FileNotFoundException {
		super(SinglePerson.class, Util.readProperty("quedit.properties", "authors"));
		refresh();
	}

	@GET
	@Path("/list")
	public Response list() {
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
			return Response.status(500).entity(e.toString()).build();
		}
		String href = href(uriList, name);
		String body = folder.getAbsolutePath() + "<br>" + table.toString() + "<br>" + href(uriNew, "Добавить");
		return Response.status(Response.Status.OK).entity(htmlPage(name, href, body)).build();
	}

	private TableBuilder getPersonTable() throws URISyntaxException {
		TableBuilder table = new TableBuilder("Персонаж", "Редактировать", "Удалить");
		for (String name : keys()) {
			URI uriEdit = uri("editform", new Parameter<>("name", name));
			URI uriRemove = uri("remove", new Parameter<>("name", name));
			table.addRow(name, href(uriEdit, "Редактировать"), href(uriRemove, "Удалить"));
		}
		return table;
	}
	
	@GET
	@Path("/editform")
	public Response editForm(@QueryParam("name") String name) {
		SinglePerson person = get(name);
		if (person == null) {
			person = SinglePerson.mock();
		}
		return Response.status(Response.Status.OK).entity(htmlPage("Отредактируйте данные", editPersonAction.buildForm(person))).build();
	}

	@GET
	@Path("/remove")
	public Response removeMethod(@QueryParam("name") String name) {
		super.remove(name);
		return list();
	}

	@GET
	@Path("/edit")
	public Response edit(@QueryParam("firstName") String name, @QueryParam("lastName") String lastName, @QueryParam("city") String city) {
		SinglePerson p = new SinglePerson(name, lastName, city);
		add(p);
		return list();
	}

	private static ActionBuilder editPersonAction = new ActionBuilder("/quedit/rest/authors/edit");
	static {
		try {
			editPersonAction.addParam(SinglePerson.class, "firstName", "Имя", text)
					.addParam(SinglePerson.class, "lastName", "Фамилия", text)
					.addParam(SinglePerson.class, "city", "Город", text);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args) throws FileNotFoundException {
		AuthorsCatalogue ac = new AuthorsCatalogue();
		ac.htmlHelper = new HtmlHelper();
		System.out.println(ac.list().getEntity());
		System.out.println("ok");
	}

	@Override
	protected String getFileName(SinglePerson p) {
		return p.toString();
	}

	@Override
	public String base() {
		return "/quedit/rest/authors";
	}

	@Override
	public HtmlHelper htmlHelper() {
		return htmlHelper;
	}
}
