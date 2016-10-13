package shtykh.quedit.question;

import org.apache.commons.lang.StringUtils;
import shtykh.quedit._4s.FormParameterMaterial4s;
import shtykh.quedit._4s.Type4s;
import shtykh.quedit._4s._4Sable;
import shtykh.quedit.author.Authored;
import shtykh.quedit.author.MultiPerson;
import shtykh.quedit.author.Person;
import shtykh.quedit.numerator.Numerable;
import shtykh.util.CSV;
import shtykh.util.Jsonable;
import shtykh.util.html.table.TableRowMaterial;
import shtykh.util.html.form.material.FormMaterial;
import shtykh.util.html.form.material.FormParameterMaterial;

import java.awt.*;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Created by shtykh on 01/10/15.
 */
public class Question implements Authored, FormMaterial, Jsonable, _4Sable, Numerable, TableRowMaterial {
	private FormParameterMaterial<String> unaudible;
	private FormParameterMaterial<Integer> index;
	private FormParameterMaterial4s number;
	private FormParameterMaterial4s text;
	private FormParameterMaterial4s answer;
	private FormParameterMaterial4s possibleAnswers;
	private FormParameterMaterial4s impossibleAnswers;
	private FormParameterMaterial4s comment;
	private FormParameterMaterial<CSV> sources;
	private FormParameterMaterial<Color> color;
	private MultiPerson author;

	public Question() {
		index = new FormParameterMaterial<>(0, Integer.class);
		number = new FormParameterMaterial4s(Type4s.NUMBER, index.get().toString());
		unaudible = new FormParameterMaterial<>("", String.class);
		text = new FormParameterMaterial4s(Type4s.QUESTION, "");
		answer = new FormParameterMaterial4s(Type4s.ANSWER, "");
		possibleAnswers = new FormParameterMaterial4s(Type4s.EQUAL_ANSWER, "");
		impossibleAnswers = new FormParameterMaterial4s(Type4s.NOT_EQUAL_ANSWER, "");
		comment = new FormParameterMaterial4s(Type4s.COMMENT, "");
		sources = new FormParameterMaterial<>(new CSV(), CSV.class);
		color = new FormParameterMaterial<>(Color.WHITE, Color.class);
		author = new MultiPerson();
	}

	public String getNumber() {
		return number.get();
	}

	public String getUnaudible() {
		return unaudible.get();
	}

	public String getText() {
		return text.get();
	}

	public String getAnswer() {
		return answer.get();
	}

	public String getPossibleAnswers() {
		return possibleAnswers.get();
	}

	public String getComment() {
		return comment.get();
	}

	public String getSources() {
		return sources.getValueString();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		append(sb, number);
		sb.append(Type4s.QUESTION.getSymbol() + " ");
		appendUnaudible(sb);
		sb.append(text.get() + "\n");
		append(sb, answer);
		append(sb, possibleAnswers);
		append(sb, impossibleAnswers);
		append(sb, comment);
		appendSources(sb);
		appendAuthor(sb);
		String result = sb.toString();
		result = result.replaceAll("\n\n*", "\n");
		if (result.endsWith("\n")) {
			result = result.substring(0, result.length());
		}
		return result;
	}

	private void appendAuthor(StringBuilder sb) {
		if (author != null && !author.getPersonList().isEmpty()) {
			sb.append(Type4s.AUTHORS.getSymbol() + " ").append(author.toString()).append("\n");
		}
	}

	private void appendUnaudible(StringBuilder sb) {
		if (isNotBlank(unaudible.get())) {
			sb.append("[").append(unaudible.get()).append("]\n");
		}
	}

	private void appendSources(StringBuilder sb) {
		if (sources != null && !sources.get().isEmpty())
			{sb.append(Type4s.SOURCES.getSymbol() + " ");
				String[] sourcesArray = sources.get().asArray();
				for (String source : sourcesArray) {
					if (sourcesArray.length > 1) {
						sb.append("\n- ");
					}
					sb.append(source);
				}
				sb.append("\n");
			}
	}

	public static Question mock() {
		return new Question();
	}

	@Override
	public Person getAuthor() {
		return author;
	}

	public void setAuthor(MultiPerson author) {
		this.author = author;
	}
	
	public void setText(String question) {
		this.text.setValueString(question);
	}

	public void setComment(String comment) {
		this.comment.setValueString(comment);
	}

	public void setUnaudible(String unaudible) {
		this.unaudible.setValueString(unaudible);
	}

	public void setAnswer(String answer) {
		this.answer.setValueString(answer);
	}

	public void setSources(String source) {
		this.sources.setValueString(source);
	}

	public void setPossibleAnswers(String possibleAnswers) {
		this.possibleAnswers.setValueString(possibleAnswers);
	}

	public void setNumber(String number) {
		this.number.setValueString(number);
	}

	public String getImpossibleAnswers() {
		return impossibleAnswers.get();
	}

	public void setImpossibleAnswers(String impossibleAnswers) {
		this.impossibleAnswers.setValueString(impossibleAnswers);
	}

	public int index() {
		return index.get();
	}

	@Override
	public void newIndex(int index) {
		this.index.set(index);
	}

	public void setIndex(Integer index) {
		this.index.set(index);
	}

	public void addAuthor(Person author) {
		this.author.add(author);
	}

	public void setColor(String colorhex) {
		color.setValueString(colorhex);
	}

	public String getColor() {
		return color.getValueString();
	}
	@Override
	public String to4s() {
		return toString();
	}

	public boolean empty() {
		return StringUtils.isEmpty(text.get()) && StringUtils.isEmpty(answer.get());
	}

	public void appendUnaudible(String delta) {
		setUnaudible(getUnaudible() + delta);
	}
}
