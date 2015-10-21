package shtykh.util.html;

import org.apache.http.client.utils.URIBuilder;
import shtykh.util.html.param.Parameter;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by shtykh on 09/10/15.
 */
public interface UriGenerator {
	default URI uri(String method, Parameter... parameters) {
		try {
			return htmlHelper().uriBuilder(base() + "/" + method, parameters).build();
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	default URIBuilder uriBuilder(String method, Parameter... parameters) {
		try {
			return htmlHelper().uriBuilder(base() + "/" + method, parameters);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	default String address(String method, Parameter... parameters) {
		return uri(method, parameters).toString();
	}

	public String base();

	public HtmlHelper htmlHelper();
}
