package shtykh.util.html.form.param;

/**
 * Created by shtykh on 10/07/15.
 */
public enum FormParameterType {
	text,
	textarea,
	submit,
	button,
	checkbox,
	file,
	hidden,
	image,
	password,
	radio,
	reset,
	color,
	date,
	datetime,
	email,
	number,
	range,
	search,
	tel,
	time,
	url,
	month,
	week,
	datetime_local,
	comment,
	select;

	@Override
	public String toString() {
		return name().replace("_", "-");
	}

	public boolean isComment() {
		return comment.equals(this);
	}

}
