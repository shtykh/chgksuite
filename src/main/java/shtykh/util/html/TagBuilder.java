package shtykh.util.html;

import shtykh.util.html.param.Parameter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by shtykh on 10/07/15.
 */
public class TagBuilder {
	private final String tag;
	private Collection<Parameter> params;

	public TagBuilder(String tag) {
		this.tag = tag;
		this.params = new ArrayList<>();
	}
	
	public TagBuilder params(Parameter... parameters) {
		Collections.addAll(params, parameters);
		return this;
	}
	
	public static TagBuilder tag(String tag) {
		return new TagBuilder(tag);
	}
	
	public String build(Object obj) {
		return buildInternal(obj, tag, params);
	}

	private static String buildInternal(Object value, String tag, Collection<Parameter> parameters) {
		StringBuilder sb = new StringBuilder("<" + tag + " ");
		for (Parameter parameter : parameters) {
			sb.append(parameter).append(" ");
		}
		if(value != null) {
			sb.append(">")
				.append(value)
			.append("</")
				.append(tag)
			.append(">");
		} else {
			sb.append("/>");
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		return buildInternal(null, tag, params);
	}
}
