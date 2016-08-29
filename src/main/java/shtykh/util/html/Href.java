package shtykh.util.html;

import shtykh.util.html.param.Parameter;

import java.net.URI;

import static shtykh.util.html.TagBuilder.tag;

/**
 * Created by shtykh on 26/08/16.
 */
public class Href {
	private URI uri;
	private String name;

	public Href(URI uri, String name) {
		this.uri = uri;
		this.name = name;
	}

	public Href(URI uri) {
		this(uri, null);
	}

	@Override
	public String toString() {
		return tag("a")
				.params(new Parameter<>("href", uri.toString()))
				.build(name);
	}
}
