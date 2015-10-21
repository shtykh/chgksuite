package shtykh.quedit._4s;

import org.apache.commons.lang.StringUtils;
import shtykh.util.html.form.material.FormParameterMaterial;

/**
 * Created by shtykh on 03/10/15.
 */
public class FormParameterMaterial4s extends FormParameterMaterial<String> {
	private final Type4s type4s;

	public FormParameterMaterial4s(Type4s type4s, String value) {
		super(value, String.class);
		this.type4s = type4s;
	}

	public String to4s() {
		return StringUtils.isBlank(get()) ? "" : type4s.getSymbol() + " " + get() + "\n";
	}

}
