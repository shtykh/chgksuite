package shtykh.util.html.form.build;

import shtykh.util.html.form.material.FormMaterial;
import shtykh.util.html.form.param.FormParameterType;

/**
 * Created by shtykh on 07/10/16.
 */
public class ActionBuilderParameter {
	
	private final Class<? extends FormMaterial> clazz;
	private final String fieldName;
	private final String label;
	private final String parameterName;
	private final FormParameterType type;
	
	public ActionBuilderParameter(Class<? extends FormMaterial> clazz,
								  String fieldName,
								  String label,
								  String parameterName,
								  FormParameterType type) {
		this.clazz = clazz;
		this.fieldName = fieldName;
		this.label = label;
		this.parameterName = parameterName;
		this.type = type;
	}

	public Class<? extends FormMaterial> getClazz() {
		return clazz;
	}

	public String getFieldName() {
		return fieldName;
	}

	public String getLabel() {
		return label;
	}

	public String getParameterName() {
		return parameterName;
	}

	public FormParameterType getType() {
		return type;
	}
}
