package shtykh.quedit.author;

import org.apache.commons.lang.StringUtils;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by shtykh on 01/10/15.
 */
public class MultiPerson extends Person {
	List<SinglePerson> personList = new ArrayList<>();
	
	public MultiPerson add(SinglePerson person) {
		personList.add(person);
		return this;
	}

	@Override
	public String toString() {
		return StringUtils.join(personList, ", ");
	}

	@Override
	public int compareTo(Person o) {
		if (o instanceof SinglePerson) return 1;
		if (o instanceof MultiPerson) {
			return compareTo((MultiPerson) o);
		} else {
			throw new NotImplementedException();
		}
	}

	public int compareTo(MultiPerson o) {
		int compare = Integer.signum(getSize() - o.getSize());
		int index = 0;
		while (compare == 0 && index < getSize()) {
			compare = get(index).compareTo(o.get(index));
		}
		return compare;
	}

	private SinglePerson get(int index) {
		return personList.get(index);
	}
	
	public List<SinglePerson> getPersonList() {
		return this.personList;
	}

	private int getSize() {
		return personList.size();
	}

	public MultiPerson fromString(String value) {
		personList = new ArrayList<>();
		String[] words = value.split("\\s+");
		SinglePerson person = new SinglePerson();
		for (String word : words) {
			if (word.matches("\\([^\\)]*\\)\\,?")) {
				person.setCity(word.substring(1, word.indexOf(")")));
				personList.add(person);
				person = new SinglePerson();
			} else {
				if (StringUtils.isBlank(person.getFirstName())) {
					person.setFirstName(word);
				} else {
					person.setLastName(word);
				}
			}
		}
		return this;
	}
	
	public void sort() {
		Collections.sort(personList);
	}
}
