package shtykh.quedit._4s;

import shtykh.quedit.author.MultiPerson;
import shtykh.quedit.author.SinglePerson;
import shtykh.quedit.pack.PackInfo;
import shtykh.quedit.question.Question;
import shtykh.util.Util;

import java.util.*;

/**
 * Created by shtykh on 09/10/15.
 */
public class Parser4s {
	private List<Question> questions;

	private PackInfo info;
	private Set<SinglePerson> persons;

	public Parser4s(String filePath, String picsFolderPath) throws Exception {
		String _4s = Util.read(filePath);
		_4s = _4s.replaceAll("(\\(img\\s+)", "$1" + picsFolderPath + "\\/");
		parse(_4s);
	}

	private void parse(String string) throws Exception {
		questions = new ArrayList<>();
		info = new PackInfo();
		persons = new TreeSet<>();
		String[] questionStrings = string.split("\n\n+");
		for (String qs : questionStrings) {
			parseQuestion(qs);
		}
	}

	private void parseQuestion(String qs) throws Exception {
		Question q = new Question();
		String[] lines = qs.split("\n+");
		Type4s currentType = Type4s.NONE;
		StringBuilder sb = new StringBuilder();
		for (String line : lines) {
			String[] firstAndTail = line.split("\\s", 2);
			Type4s newType =Type4s.fromString(firstAndTail[0]);
			if (! newType.equals(Type4s.NONE)) {
				if (newType.equals(Type4s.LIST_ELEM)) {
					sb.append(firstAndTail[1]);
					sb.append("\n");
				} else {
					set4s(currentType, sb.toString(), q);
					currentType = newType;
					sb = new StringBuilder(firstAndTail[1]);
				}
			} else {
				sb.append('\n').append(line);
			}
		}
		set4s(currentType, sb.toString(), q);
		if (! q.empty()) {
			questions.add(q);
		}
	}

	private void set4s(Type4s type4s, String value, Question q) throws Exception {
		try {
			switch (type4s) {
				case TITLE:
					info.setName(value);
					break;
				case TITLE_LJ:
					info.setNameLJ(value);
					break;
				case EDITOR:
					info.addAuthor(initMultiPerson(value));
					break;
				case DATE:
					info.setDate(value);
					break;
				case META:
					info.addMetaInfo(value);
					break;
				case QUESTION:
					q.setText(value);
					break;
				case NUMBER:
					q.setNumber(value);
					break;
				case ANSWER:
					q.setAnswer(value);
					break;
				case EQUAL_ANSWER:
					q.setPossibleAnswers(value);
					break;
				case NOT_EQUAL_ANSWER:
					q.setImpossibleAnswers(value);
					break;
				case COMMENT:
					q.setComment(value);
					break;
				case SOURCES:
					q.setSources(value);
					break;
				case AUTHORS:
					q.setAuthor(initMultiPerson(value));
					break;
				case LIST_ELEM:
					break;
				case NONE:
					break;
			}
		} catch (Exception e) {
			throw new Exception("Could not properly read \"" + value + "\" as " + type4s, e);
		}
	}

	private MultiPerson initMultiPerson(String value) {
		MultiPerson person = new MultiPerson().fromString(value);
		for (SinglePerson singlePerson : person.getPersonList()) {
			persons.add(singlePerson);
		}
		return person;
	}

	public List<Question> getQuestions() {
		return questions;
	}

	public PackInfo getInfo() {
		return info;
	}

	public static Parser4s parseMock() throws Exception {
		String path = "/Users/shtykh/local/codebrag/repos/chgksuiteweb/packs/rudn_2/20160829_123251/rudn_2.4s";
		String pics = "/Users/shtykh/local/codebrag/repos/chgksuiteweb/packs/rudn_2/pics";
		return new Parser4s(path, pics);
	}

	public Collection<SinglePerson> getPersons() {
		return persons;
	}
}
