package shtykh.quedit.author;

import org.apache.commons.lang.StringUtils;
import shtykh.util.Jsonable;
import shtykh.util.html.form.material.FormMaterial;
import shtykh.util.html.form.material.FormParameterMaterial;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Arrays;
import java.util.Objects;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Created by shtykh on 01/10/15.
 */
public class SinglePerson extends Person implements FormMaterial, Jsonable {
	private FormParameterMaterial<String> lastName = new FormParameterMaterial<>("Фамилия", String.class);
	private FormParameterMaterial<String> firstName = new FormParameterMaterial<>("Имя", String.class);
	private FormParameterMaterial<String> city = new FormParameterMaterial<>("Город", String.class);

	public SinglePerson() {
		this("", "", "");
	}

	public SinglePerson(String firstName, String lastName, String city) {
		this.firstName.set(firstName);
		this.lastName.set(lastName);
		this.city.set(city);
	}

	public String getLastName() {
		return lastName.get();
	}

	public void setLastName(String lastName) {
		this.lastName.set(lastName);
	}

	public String getFirstName() {
		return firstName.get();
	}

	public void setFirstName(String firstName) {
		this.firstName.set(firstName);
	}

	public String getCity() {
		return city.get();
	}

	public void setCity(String city) {
		this.city.set(city);
	}

	public static SinglePerson mock() {
		return new SinglePerson("Имя", "Фамилия", "Город");
	}

	@Override
	public String toString() {
		return firstName.get() + ' ' + lastName.get() + (StringUtils.isNotEmpty(city.get()) ? " (" + city.get() + ')' : "");
	}
	
	public int compareTo(SinglePerson o) {
		int lastNameComp = compareStrings(getLastName(), o.getLastName());
		if (lastNameComp != 0) { return lastNameComp; } else {
			int firstNameComp = compareStrings(getFirstName(), o.getFirstName());
			if (firstNameComp != 0) { return firstNameComp; } else {
				return compareStrings(getCity(), o.getCity());
			}	
		}
	}

	private int compareStrings(String obj1, String obj2) {
		if (Objects.equals(obj1, obj2)) {
			return 0;
		}
		if (obj1 == null) {
			return -1;
		}
		if (obj2 == null) {
			return 1;
		}
		return obj1.compareTo(obj2);
	}

	@Override
	public int compareTo(Person o) {
		if (o instanceof MultiPerson) return -1;
		if (o instanceof SinglePerson) {
			return compareTo((SinglePerson) o);
		} else {
			throw new NotImplementedException();
		}
	}

	public static SinglePerson fromString(String string) {
		SinglePerson person = new SinglePerson();
		String[] split = string.split("\\(");
		String[] names = split[0].split("\\s+", 2);
		person.setFirstName(StringUtils.trim(names[0]));
		person.setLastName(StringUtils.trim(names[1]));
		if (split.length > 1) {
			String city = split[1];
			city = city.substring(0, city.indexOf(")"));
			person.setCity(StringUtils.trim(city));
		}
		return person;
	}

	public static SinglePerson fromStrings(String name, String city) {
		SinglePerson person = new SinglePerson();
		String[] names = name.split("\\s");
		if (names.length > 0) {
			person.setLastName(names[names.length - 1]);
			names = Arrays.copyOfRange(names, 0, names.length - 1);
			person.setFirstName(StringUtils.join(names, " "));
		}
		person.setCity(city);
		return person;
	}

	public boolean empty() {
		return isBlank(firstName.get()) && isBlank(lastName.get()) && isBlank(city.get());
	}
}
