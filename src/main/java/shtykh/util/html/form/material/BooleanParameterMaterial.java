package shtykh.util.html.form.material;

import shtykh.util.html.form.param.FormParameterType;

/**
 * Created by shtykh on 14/07/15.
 */
public class BooleanParameterMaterial extends FormParameterMaterial<Boolean> {
	public BooleanParameterMaterial(Boolean value) {
		super(value, Boolean.class);
		allowTypes(FormParameterType.checkbox, FormParameterType.text, FormParameterType.hidden, FormParameterType.comment);
	}
}
