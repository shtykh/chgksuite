package shtykh.quedit.numerator;

import shtykh.util.CSV;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by shtykh on 01/10/15.
 */
public interface Numerator<T extends Numerable> {
	String getNumber(int index);
	int getIndex(String number);

	default CSV firstNumbers(int n) {
		Collection<String> string = new ArrayList<>();
		for (int i = 0; i < n; i++) {
			string.add(getNumber(i));
		}
		return new CSV(string);
	}
	
	default void number(Integer number, T item) {
		item.newIndex(number);
		item.setNumber(getNumber(number));
	}

	default void renumber(T item) {
		item.setNumber(getNumber(item.index()));
	}
}
