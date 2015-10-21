package shtykh.util.html.form.material;

import shtykh.util.StringSerializer;
import shtykh.util.html.form.param.FormParameter;
import shtykh.util.html.form.param.FormParameterSignature;
import shtykh.util.html.form.param.FormParameterType;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by shtykh on 18/07/15.
 */
public class FormParameterMaterial<T> {
	protected T value;
	private final StringSerializer<T> serializer;
	private Set<FormParameterType> allowedTypes;

	public FormParameterMaterial(T value, Class<T> clazz) {
		this.value = value;
		this.serializer = getSerializer(clazz);
	}

	protected StringSerializer<T> getSerializer(Class<T> clazz) {
		StringSerializer<T> serializer = StringSerializer.getForClass(clazz);
		if (serializer == null) {
			throw new NullPointerException("serializer for " + clazz.getCanonicalName());
		}
		return serializer;
	}

	public T get() {
		return value;
	}

	public void set(T value) {
		this.value = value;
	}

	public void setValueString(String s) {
		value = serializer.fromString(s);
	}

	public String getValueString() {
		return serializer.toString(value);
	}

	protected void allowTypes(FormParameterType... types) {
		if (allowedTypes == null) {
			allowedTypes = new TreeSet<>();
		}
		Collections.addAll(allowedTypes, types);
	}

	private boolean isAllowed(FormParameterType type) {
		return allowedTypes == null
				|| allowedTypes.isEmpty()
				|| allowedTypes.contains(type);
	}


	public FormParameter toParameter(FormParameterSignature sign) {
		FormParameterType type = sign.getType();
		if (! isAllowed(type)) {
			throw new RuntimeException(type + " isn't allowed for " + toString());
		}
		return new FormParameter<T>(sign, this);
	}

}
