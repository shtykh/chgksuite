package shtykh.util.html.form.param;

import shtykh.util.html.form.material.FormParameterMaterial;
import shtykh.util.html.param.Parameter;

/**
 * Created by shtykh on 10/07/15.
 */
public class FormParameter<T> extends Parameter<T> {
	private FormParameterSignature sign;
	private FormParameterMaterial<T> material;

	public FormParameter(FormParameterSignature sign, FormParameterMaterial<T> material) {
		super(sign.getName(), material.get());
		this.sign = sign;
		this.material = material;
	}

	public FormParameter(FormParameterSignature sign, T obj, Class<T> clazz) {
		super(sign.getName(), obj);
		this.sign = sign;
		this.material = new FormParameterMaterial<>(obj, clazz);
	}

	@Override
	public String getValueString() {
		return material.getValueString();
	}
	
	public FormParameterType getType() {
		return sign.getType();
	}
	
	public FormParameterMaterial<T> getMaterial(){
		return material;
	}

	public String getLabel() {
		return sign.getLabel();
	}
}
