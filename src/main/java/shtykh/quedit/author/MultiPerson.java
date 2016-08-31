package shtykh.quedit.author;

import org.apache.commons.lang.StringUtils;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by shtykh on 01/10/15.
 */
public class MultiPerson extends Person {
	List<SinglePerson> personList = new ArrayList<>();
	
	public MultiPerson add(Person person) {
		if (person instanceof SinglePerson) {
			personList.add((SinglePerson)person);
		} else {
			for (SinglePerson singlePerson : ((MultiPerson) person).getPersonList()) {
				personList.add(singlePerson);
			}
		}
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
		Pattern pattern = Pattern.compile("([^\\(\\)]*)(\\(([^\\)]*)\\))*");
		Matcher matcher = pattern.matcher(value);
		while (matcher.find()) {
			SinglePerson person = SinglePerson.fromStrings(matcher.group(1), matcher.group(3));
			if (! person.empty()) {
				personList.add(person);
			}
		}
		return this;
	}
	
	public void sort() {
		Collections.sort(personList);
	}

	public MultiPerson copy() {
		return new MultiPerson().add(this);
	}
}
