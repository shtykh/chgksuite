package shtykh.util.html.param;

/**
 * Created by shtykh on 25/06/15.
 */
public class Parameter<T> {
	protected String name;
	protected T value;

	public Parameter(String name, T value) {
		this.name = name;
		this.value = value;
	}

	public final String getName() {
		return name;
	}

	@Override
	public final String toString() {
		return getName() + "=\"" + getValueString() + "\"" ;
	}

	public String getValueString() {
		return value.toString();
	}
}
