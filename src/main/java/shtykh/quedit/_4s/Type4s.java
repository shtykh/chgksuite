package shtykh.quedit._4s;

/**
 * Created by shtykh on 02/10/15.
 */
public enum Type4s {
	TITLE("###"),
	TITLE_LJ("###LJ"),
	EDITOR("#EDITOR"),
	DATE("#DATE"),
	META("#"),
	QUESTION("?"),
	NUMBER("№"),
	ANSWER("!"),
	EQUAL_ANSWER("="),
	NOT_EQUAL_ANSWER("!="),
	COMMENT("/"),
	SOURCES("^"),
	AUTHORS("@"),
	LIST_ELEM("-"),
	NONE("");
	
	private final String symbol;
	
	Type4s(String symbol) {
		this.symbol = symbol;
	}

	public String getSymbol() {
		return symbol;
	}

	public static Type4s fromString(String s) {
		for (Type4s type4s : values()) {
			if (s.equals(type4s.getSymbol())) {
				return type4s;
			}
		}
		return NONE;
	}
}
