package shtykh.util.html.form.build;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.sun.research.ws.wadl.HTTPMethods;
import shtykh.util.html.form.material.FormMaterial;
import shtykh.util.html.form.material.FormParameterMaterial;
import shtykh.util.html.form.param.FormParameter;
import shtykh.util.html.form.param.FormParameterSignature;
import shtykh.util.html.form.param.FormParameterType;

import java.lang.reflect.Field;
import java.util.*;

import static shtykh.util.html.form.param.FormParameterType.comment;

/**
 * Created by shtykh on 12/07/15.
 */
public class ActionBuilder {
	private final String action;
	private final Multimap<Field, FormParameterSignature> signatureMap;
	private int signaturesNumber;
	private final HTTPMethods method;

	public ActionBuilder(String action) {
		this(action, HTTPMethods.GET);
	}

	public ActionBuilder(String action, HTTPMethods method) {
		this.action = action;
		this.method = method;
		this.signaturesNumber = 0;
		signatureMap = ArrayListMultimap.create();
	}

	public ActionBuilder addParam(Field field,
								  FormParameterSignature signature) {
		signature.setIndex(signaturesNumber++);
		signatureMap.put(field, signature);
		return this;
	}


	public ActionBuilder addParam(Class<? extends FormMaterial> clazz,
								  String fieldName,
								  String label,
								  FormParameterType type) throws NoSuchFieldException {
		return addParam(clazz, fieldName, label, fieldName, type);
	}

	public ActionBuilder addParam(Class<? extends FormMaterial> clazz,
								  String fieldName,
							  FormParameterType type) throws NoSuchFieldException {
		return addParam(clazz, fieldName, fieldName, fieldName, type);
	}

	public ActionBuilder addParam(Class<? extends FormMaterial> clazz,
								  String fieldName,
								  String label,
								  String parameterName,
								  FormParameterType type) throws NoSuchFieldException {
		Field field = clazz.getDeclaredField(fieldName);
		FormParameterSignature formParameterSignature = new FormParameterSignature(parameterName, label, type);
		return addParam(field, formParameterSignature);
	}

	public String buildForm(FormMaterial formMaterial) {
		synchronized (formMaterial) {
			Map<Integer, FormParameter> parameters = new TreeMap<>();
			addParameters(parameters, formMaterial, new ArrayList<>());
			FormBuilder builder = new FormBuilder(action);
			for (int i = 0; i < signaturesNumber; i++) {
				FormParameter formParameter = parameters.get(i);
				if (null != formParameter) {
					builder.addMember(formParameter);
				} else {
					//throw new RuntimeException(action + " error: " + i + "th parameter value wasn't found");
				}
			}
			return builder.build(this.method);
		}
	}

	private void addParameters(Map<Integer, FormParameter> parameters, 
							   FormMaterial formMaterial, 
							   List<FormMaterial> seen) {
		if (seen.contains(formMaterial) || formMaterial == null) {
			return;
		} else {
			seen.add(formMaterial);
		}
		Class clazz = formMaterial.getClass();
		while(FormMaterial.class.isAssignableFrom(clazz)) {
			addParameters(clazz, parameters, formMaterial, seen);
			clazz = clazz.getSuperclass();
		}
	}

	private void addParameters(Class<? extends FormMaterial> clazz, 
							   Map<Integer, FormParameter> parameters, 
							   FormMaterial formMaterial, 
							   List<FormMaterial> seen) {
		for (Field field : clazz.getDeclaredFields()) {
			if (FormMaterial.class.isAssignableFrom(field.getType())) {
				addParameters(parameters, get(field, formMaterial, FormMaterial.class), seen);
			}else if (FormParameterMaterial.class.isAssignableFrom(field.getType())) {
				for(FormParameterSignature sign : getSignFor(field)) {
					parameters.put(sign.getIndex(), getParameter(field, sign, formMaterial));
				}
			}
		}
	}

	private FormParameter getParameter(Field field,FormParameterSignature sign, FormMaterial formMaterial) {
		FormParameterMaterial material = get(field, formMaterial, FormParameterMaterial.class);
		return material.toParameter(sign);
	}

	private <T> T get(Field field, Object object, Class<T> fieldClazz) {
		try {
			field.setAccessible(true);
			T t = fieldClazz.cast(field.get(object));
			field.setAccessible(false);
			return t;
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private Collection<FormParameterSignature> getSignFor(Field field) {
		Collection<FormParameterSignature> signs = signatureMap.get(field);
		if (signs == null) {
			FormParameterSignature sign = new FormParameterSignature(field.getName(), comment);
			sign.setIndex(signaturesNumber++);
			return Arrays.asList(sign);
		}
		return signs;
	}
}
