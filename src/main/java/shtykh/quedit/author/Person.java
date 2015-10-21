package shtykh.quedit.author;

/**
 * Created by shtykh on 01/10/15.
 */
public abstract class Person implements Comparable<Person> {
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Person) {
			return this.compareTo((Person) obj) == 0;
		} else {
			return false;
		}
	}
}
