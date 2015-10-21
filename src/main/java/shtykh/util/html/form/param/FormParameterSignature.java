package shtykh.util.html.form.param;

/**
 * Created by shtykh on 12/07/15.
 */
public class FormParameterSignature implements Comparable<FormParameterSignature>{
	private final String name;
	private final String label;
	private final FormParameterType type;
	private int index;

	public FormParameterSignature(String name, FormParameterType type) {
		this(name, name, type);
	}

	public FormParameterSignature(String name, String label, FormParameterType type) {
		this.name = name;
		this.label = label;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public FormParameterType getType() {
		return type;
	}

	@Override
	public int compareTo(FormParameterSignature o) {
		if (o == null) {
			return -1;
		} else {
			int namesCompared = name.compareTo(o.getName());
			if (namesCompared != 0) {
				return namesCompared;
			} else {
				return type.compareTo(o.getType());
			}
		}
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getLabel() {
		return label;
	}
}
