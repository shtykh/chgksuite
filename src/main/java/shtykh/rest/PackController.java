package shtykh.rest;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import shtykh.quedit.pack.Pack;
import shtykh.util.Util;
import shtykh.util.catalogue.FolderKeaper;
import shtykh.util.html.HtmlHelper;
import shtykh.util.html.TableBuilder;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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

@Component
@Path("/pack")
public class PackController extends FolderKeaper {
	private Map<String, Pack> packs = new TreeMap<>();

	@Autowired
	private HtmlHelper htmlHelper;
	@Autowired
	private AuthorsCatalogue authors;

	public PackController() throws FileNotFoundException {
		super(Util.readProperty("quedit.properties", "packs"));
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

	private Response getOr404(String id, String methodName, Object... args) {
		Pack pack = packs.get(id);
		if (pack == null) {
			return Response.status(404).build();
		} else {
			try {
				return (Response) findMethodByName(Pack.class, methodName).invoke(pack, args);
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

	@GET
	@Path("/")
	public Response all() throws IOException, URISyntaxException {
		refresh();
		TableBuilder table = new TableBuilder();
		for (String id : packs.keySet()) {
			URI editUri = htmlHelper.uriBuilder("/quedit/rest/pack/" + id).build();
			table.addRow(href(editUri, packs.get(id).getName()));
		}
		URI addUri = htmlHelper.uriBuilder("/quedit/rest/pack/new").build();
		table.addRow(href(addUri, "Новый пакет"));
		return Response.ok(htmlPage("Пакеты", table.buildHtml())).build();
	}
	
	@GET
	@Path("{id}")
	public Response getPack(@PathParam("id") String id) throws IOException {
		if (id.equals("new")) {
			return Response.ok(htmlPage("Новый пакет", "Впишите id пакета в адресную строку вместо \"new\"")).build();
		}
		Pack pack = packs.get(id);
		if (pack == null) {
			String namePattern = Util.readProperty("quedit.properties", "namePattern");
			if (!id.matches(namePattern)) {
				return Response.status(500).entity(htmlPage("Придумайте id попроще!", namePattern)).build();
			}
			pack = addPack(id);
		}
		return pack.home();
	}

	@GET
	@Path("{id}/info")
	public Response info(@PathParam("id") String id) throws IOException {
		return getOr404(id, "info");
	}

	private Pack addPack(String id) throws FileNotFoundException {
		Pack pack = new Pack(id, htmlHelper, authors);
		packs.put(id, pack);
		return pack;
	}

	@GET
	@Path("{id}/editPack")
	public Response editPack(
			@PathParam("id") String id,
			@QueryParam("name") String name,
			@QueryParam("nameLJ") String nameLJ,
			@QueryParam("date") String date,			
			@QueryParam("first") int first,
			@QueryParam("zeroNumbers") String zeroNumbers,
			@QueryParam("metaInfo") String metaInfo
	) {
		return getOr404(id, "editPack", name, nameLJ, date, metaInfo, first, zeroNumbers);
	}

	@GET
	@Path("{id}/addPicture")
	public Response addPicture(
			@PathParam("id") String id,
			@QueryParam("number") String number,
			@QueryParam("path") String path
			) {
		return getOr404(id, "addPicture", number, path);
	}

	@GET
	@Path("{id}/uploadForm/{what}")
	public Response uploadForm(@PathParam("id") String id, @PathParam("what") String what) {
		return getOr404(id, "uploadForm", what);
	}

	@POST
	@Path("{id}/upload/{what}")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response uploadFile(
			@PathParam("id") String id,
			@PathParam("what") String what,
			@FormDataParam("file") InputStream fileInputStream,
			@FormDataParam("file") FormDataContentDisposition contentDispositionHeader) {
		if (what == null) {
			what = "file";
		}
		return getOr404(id, "upload_" + what, fileInputStream, contentDispositionHeader);
	}
	
	@GET
	@Path("{id}/editForm")
	public Response editForm(@PathParam("id") String id, @QueryParam("index") int index) {
		return getOr404(id, "editForm", index);
	}

	@GET
	@Path("{id}/editAuthorForm")
	public Response editAuthorForm(@PathParam("id") String id, @QueryParam("index") int index) throws URISyntaxException {
		return getOr404(id, "editAuthorForm", index);
	}

	@GET
	@Path("{id}/remove")
	public Response removeMethod(@PathParam("id") String id, @QueryParam("index") int index) {
		return getOr404(id, "removeMethod", index);
	}

	@GET
	@Path("{id}/replace")
	public Response replace(@PathParam("id") String id, @QueryParam("index") int index) {
		return getOr404(id, "replace", index);
	}

	@GET
	@Path("{id}/up")
	public Response upMethod(@PathParam("id") String id, @QueryParam("index") int index) {
		return getOr404(id, "upMethod", index);
	}

	@GET
	@Path("{id}/down")
	public Response downMethod(@PathParam("id") String id, @QueryParam("index") int index) {
		return getOr404(id, "downMethod", index);
	}

	@GET
	@Path("{id}/edit")
	public Response edit(
			@PathParam("id") String id,
			@QueryParam("index") int index,
			@QueryParam("unaudible") String unaudible,
			@QueryParam("color") String color,
			@QueryParam("text") String text,
			@QueryParam("answer") String answer,
			@QueryParam("possibleAnswers") String possibleAnswers,
			@QueryParam("impossibleAnswers") String impossibleAnswers,
			@QueryParam("comment") String comment,
			@QueryParam("sources") String sources
	) {
		return getOr404(id, "edit", index, unaudible, color, text, answer, possibleAnswers, impossibleAnswers, comment, sources);
	}
	
	@GET
	@Path("{id}/editAuthor")
	public Response editAuthor(
			@PathParam("id") String id,
			@QueryParam("index") int index,
			@QueryParam("keys") String author
	) {
		return getOr404(id, "editAuthor", index, author);
	}

	@GET
	@Path("{id}/removeAuthor")
	public Response removeAuthor(
			@PathParam("id") String id,
			@QueryParam("index") int index,
			@QueryParam("keys") String author
	) {
		return getOr404(id, "removeAuthor", index, author);
	}

	@GET
	@Path("{id}/addEditor")
	public Response addEditor(
			@PathParam("id") String id,
			@QueryParam("keys") String author
	) {
		return getOr404(id, "addEditor", author);
	}

	@GET
	@Path("{id}/addTester")
	public Response addTester(
			@PathParam("id") String id,
			@QueryParam("keys") String testerName
	) {
		return getOr404(id, "addTester", testerName);
	}

	@GET
	@Path("{id}/removeEditor")
	public Response removeEditor(
			@PathParam("id") String id,
			@QueryParam("keys") String author
	) {
		return getOr404(id, "removeEditor", author);
	}

	@GET
	@Path("{id}/removeTester")
	public Response removeTester(
			@PathParam("id") String id,
			@QueryParam("keys") String testerName
	) {
		return getOr404(id, "removeTester", testerName);
	}

	@GET
	@Path("{id}/nextColor")
	public Response nextColor(
			@PathParam("id") String id,
			@QueryParam("index") int index,
			@QueryParam("color") String colorHex
	) {
		return getOr404(id, "nextColor", index, colorHex);
	}

	@GET
	@Path("{id}/text")
	public Response text(@PathParam("id") String id) throws IOException {
		return getOr404(id, "text");
	}

	@GET
	@Path("{id}/compose")
	public Response compose(@PathParam("id") String id, 
							@QueryParam("outFormat") String outFormat, 
							@QueryParam("debug") boolean debug) throws IOException {
		return getOr404(id, "compose", outFormat, debug);
	}

	@GET
	@Path("{id}/download/docx")
	@Produces("application/msword")
	public Response downloadDocFile(@PathParam("id") String id, @QueryParam("path") String path) {
		File file = new File(path);
		Response.ResponseBuilder responseBuilder = Response.ok(file);
		responseBuilder.header("Content-Disposition", "attachment; filename=\""+ id + ".docx\"");
		return responseBuilder.build();
	}

	public static void main(String[] args) throws IOException, URISyntaxException {
		PackController packController = new PackController();
		packController.htmlHelper = new HtmlHelper();
		packController.authors = new AuthorsCatalogue();
		packController.refresh();
		Pack pack = packController.packs.get("rudn_cup");
		pack.editPack("Кубок РУДН", "Кубок РУДН", "20 октября 2015", "", 1, "0\n00\n000");
		for (String s : ((String) pack.info().getEntity()).split("<br>")) {
			System.out.println(s + "<br>");
		}
	}
}
