package shtykh.quedit.numerator;

import shtykh.util.CSV;
import shtykh.util.Jsonable;
import shtykh.util.html.form.material.FormMaterial;
import shtykh.util.html.form.material.FormParameterMaterial;

/**
 * Created by shtykh on 01/10/15.
 */
public class NaturalNumerator implements Numerator, Jsonable, FormMaterial {
	private FormParameterMaterial<Integer> first;
	private FormParameterMaterial<CSV> zeroNumbers;

	public NaturalNumerator() {
		this(1, "0", "00", "000");
	}

	public NaturalNumerator(int first, String... zeroNumbers) {
		this.first = new FormParameterMaterial<>(first, Integer.class);
		this.zeroNumbers = new FormParameterMaterial<>(new CSV(zeroNumbers), CSV.class);
	}

	@Override
	public String getNumber(int index) {
		if (index < zeroNumbers.get().size()) {
			return zeroNumbers.get().get(index);
		} else {
			return String.valueOf(index - zeroNumbers.get().size() + first.get());
		}
	}

	@Override
	public int getIndex(String number) {
		int indexOf = zeroNumbers.get().asList().indexOf(number);
		if (indexOf > 0) {
			return indexOf;
		} else {
			return Integer.decode(number) - first.get() + zeroNumbers.get().size();
		}
	}

	public int getFirst() {
		return first.get();
	}

	public String getZeroNumbers() {
		return zeroNumbers.getValueString();
	}

	public void setZeroNumbers(String zeroNumbers) {
		this.zeroNumbers.setValueString(zeroNumbers);
	}

	public void setFirst(Integer first) {
		this.first.set(first);
	}

	public static void main(String[] args) {
		NaturalNumerator n = new NaturalNumerator(1, "0", "00", "000");
		String json = n.toJson();
		System.out.println(json);
		n = Jsonable.fromJson(json, NaturalNumerator.class);
		System.out.println(n);
		for (int i = 0; i < 10; i++) {
			System.out.println(n.getNumber(i));
		}
		System.out.println();
		System.out.println(n.getNumber(5));
		System.out.println(n.getIndex("5"));
		System.out.println(n.getIndex("000"));

	}

	@Override
	public String toString() {
		return "NaturalNumerator{" +
				"first=" + first.get() +
				", zeroNumbers=" + zeroNumbers.getValueString() +
				'}';
	}
}
