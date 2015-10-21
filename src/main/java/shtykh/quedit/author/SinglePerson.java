package shtykh.quedit.author;

import shtykh.util.Jsonable;
import shtykh.util.html.form.material.FormMaterial;
import shtykh.util.html.form.material.FormParameterMaterial;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

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
		return firstName.get() + ' ' + lastName.get() + " (" + city.get() + ')';
	}

	public static void main(String[] args) {
		SinglePerson p = mock();
		String string = p.toJson();
		System.out.println(string);
		p = Jsonable.fromJson(string, SinglePerson.class);
		System.out.println(p);
	}
	
	public int compareTo(SinglePerson o) {
		int lastNameComp = getLastName().compareTo(o.getLastName());
		if (lastNameComp != 0) { return lastNameComp; } else {
			int firstNameComp = getFirstName().compareTo(o.getFirstName());
			if (firstNameComp != 0) { return firstNameComp; } else {
				return getCity().compareTo(o.getCity());
			}	
		}
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
}
