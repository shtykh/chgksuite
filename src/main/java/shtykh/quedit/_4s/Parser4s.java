package shtykh.quedit._4s;

import shtykh.quedit.author.MultiPerson;
import shtykh.quedit.pack.PackInfo;
import shtykh.quedit.question.Question;
import shtykh.util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shtykh on 09/10/15.
 */
public class Parser4s {
	private List<Question> questions;

	private PackInfo info;

	public Parser4s(String filePath) {
		String _4s = Util.read(filePath);
		parse(_4s);
	}

	private void parse(String string) {
		questions = new ArrayList<>();
		info = new PackInfo();
		String[] questionStrings = string.split("\n\n+");
		for (String qs : questionStrings) {
			parseQuestion(qs);
		}
	}

	private void parseQuestion(String qs) {
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
		questions.add(q);
	}

	private void set4s(Type4s type4s, String value, Question q) {
		switch (type4s) {
			case TITLE:
				info.setName(value);
				break;
			case TITLE_LJ:
				info.setNameLJ(value);
				break;
			case EDITOR:
				info.setAuthor(new MultiPerson().fromString(value));
				break;
			case DATE:
				info.setDate(value);
				break;
			case META:
				info.setMetaInfo(value);
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
				q.setAuthor(new MultiPerson().fromString(value));
				break;
			case LIST_ELEM:
				break;
			case NONE:
				break;
		}
	}

	public List<Question> getQuestions() {
		return questions;
	}

	public PackInfo getInfo() {
		return info;
	}

	public static void main(String[] args) {
		Parser4s p = parseMock();
		System.out.println("ok");
	}

	public static Parser4s parseMock() {
		String path = "/Users/shtykh/bot/target/4s.4s";
		return new Parser4s(path);
	}
}
