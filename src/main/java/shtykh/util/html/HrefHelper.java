package shtykh.util.html;

import shtykh.util.html.param.Parameter;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static shtykh.util.html.HtmlHelper.href;

/**
 * Created by shtykh on 29/08/16.
 */
public class HrefHelper implements UriGenerator {
	private final String base;
	private final HtmlHelper html;
	private Map<String, String> hrefs;

	public HrefHelper(String base, HtmlHelper html) {
		this.base = base;
		this.html = html;
		hrefs = new HashMap<>();
	}

	public void put(String method, String name, Parameter... parameters) throws URISyntaxException {
		URI uri = uriBuilder(method, parameters).build();
		String href = href(uri, name);
		hrefs.put(method, href);
	}

	public void put(URI uri, String name) throws URISyntaxException {
		String href = href(uri, name);
		hrefs.put(uri.toString(), href);
	}

	public String get(String method) {
		return hrefs.get(method);
	}

	public String get(URI uri) {
		return hrefs.get(uri.toString());
	}

	@Override
	public String base() {
		return base;
	}

	@Override
	public HtmlHelper htmlHelper() {
		return html;
	}

	public String listResponce(String title, String... s) {
		return html.listResponce(title, s);
	}
}
