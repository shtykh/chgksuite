package shtykh.quedit.numerator;

import shtykh.util.CSV;
import shtykh.util.Jsonable;
import shtykh.util.html.form.material.FormMaterial;
import shtykh.util.html.form.material.FormParameterMaterial;

/**
 * Created by shtykh on 01/10/15.
 */
public class NaturalNumerator<T extends Numerable> implements Numerator<T>, Jsonable, FormMaterial {
	private FormParameterMaterial<Integer> first;
	protected FormParameterMaterial<CSV> zeroNumbers;

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

	@Override
	public String toString() {
		return "NaturalNumerator{" +
				"first=" + first.get() +
				", zeroNumbers=" + zeroNumbers.getValueString() +
				'}';
	}
}
