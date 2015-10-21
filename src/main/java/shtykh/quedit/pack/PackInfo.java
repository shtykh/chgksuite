package shtykh.quedit.pack;

import org.apache.commons.lang3.StringUtils;
import shtykh.quedit._4s.FormParameterMaterial4s;
import shtykh.quedit._4s.Type4s;
import shtykh.quedit._4s._4Sable;
import shtykh.quedit.author.Authored;
import shtykh.quedit.author.MultiPerson;
import shtykh.quedit.author.Person;
import shtykh.quedit.author.SinglePerson;
import shtykh.quedit.numerator.QuestionNaturalNumerator;
import shtykh.util.Jsonable;
import shtykh.util.Util;
import shtykh.util.html.form.material.FormMaterial;

import java.io.File;

/**
 * Created by shtykh on 08/10/15.
 */
public class PackInfo implements FormMaterial, Authored, Jsonable, _4Sable {
	private FormParameterMaterial4s metaInfo;
	private FormParameterMaterial4s name;
	private FormParameterMaterial4s nameLJ;
	private FormParameterMaterial4s date;
	private MultiPerson author;
	private QuestionNaturalNumerator numerator;

	public void setTester(MultiPerson tester) {
		this.tester = tester;
	}

	private MultiPerson tester;
	private FormParameterMaterial4s editor;

	public PackInfo() {
		metaInfo = new FormParameterMaterial4s(Type4s.META, "");
		name = new FormParameterMaterial4s(Type4s.TITLE, "");
		nameLJ = new FormParameterMaterial4s(Type4s.TITLE_LJ, "");
		date = new FormParameterMaterial4s(Type4s.DATE, "");
		editor = new FormParameterMaterial4s(Type4s.EDITOR, "");
		author = new MultiPerson();
		tester = new MultiPerson();
		numerator = new QuestionNaturalNumerator(1);
	}
	
	public void addAuthor(SinglePerson name) {
		if (author == null) {
			author = new MultiPerson();
		}
		author.add(name);
		editor.setValueString(this.author.toString());
	}

	public void addTester(SinglePerson name) {
		if (tester == null) {
			tester = new MultiPerson();
		}
		tester.add(name);
	}
	
	@Override
	public Person getAuthor() {
		return author;
	}
	@Override
	public void setAuthor(MultiPerson author) {
		this.author = author;
		editor.set(author.toString());
	}

	public String getMetaInfo() {
		return metaInfo.get();
	}

	public void setMetaInfo(String metaInfo) {
		this.metaInfo.set(metaInfo);
	}

	public void setName(String name) {
		this.name.set(name);
	}

	public String getNameLJ() {
		return nameLJ.get();
	}

	public void setNameLJ(String nameLJ) {
		this.nameLJ.set(nameLJ);
	}

	public String getDate() {
		return date.get();
	}

	public void setDate(String date) {
		this.date.set(date);
	}

	public String getName() {
		return name.get();
	}

	public FormParameterMaterial4s _4sName() {
		return name;
	}

	public FormParameterMaterial4s _4sNameLJ() {
		return nameLJ;
	}

	public FormParameterMaterial4s _4sDate() {
		return date;
	}

	public FormParameterMaterial4s _4sAuthor() {
		return editor;
	}

	public FormParameterMaterial4s _4sMetaInfo() {
		return metaInfo;
	}

	public static void main(String[] args) {
		PackInfo pi = new PackInfo();
		String json = pi.toJson();
		pi = Jsonable.fromJson(json, PackInfo.class);
		System.out.println(pi);
		System.out.println(json);
	}

	public void save(String pathname) {
		Util.write(new File(pathname), toJson());
	}

	@Override
	public String toString() {
		return "PackInfo{" +
				"date=" + getDate() +
				", nameLJ=" + getNameLJ() +
				", name=" + getName() +
				", metaInfo=" + getMetaInfo() +
				", editor=" + getAuthor() +
				'}';
	}

	public MultiPerson getTester() {
		return tester;
	}

	public String thankToTesters() {
		if (tester.getPersonList().isEmpty()) {
			return "";
		} else {
			tester.sort();
			String firstSymbol = (StringUtils.isBlank(metaInfo.get())?"# ":"");
			String who = author.getPersonList().size() > 1 ? "Редакторы выражают":"Редактор выражает";
			return firstSymbol + who + " благодарность " +
					"за помощь и ценные замечания в подготовке пакета следующим людям:\n" +
					tester;
		}
	}

	public String to4s() {
		StringBuilder sb = new StringBuilder();
		append(sb, _4sName());
		append(sb, _4sNameLJ());
		append(sb, _4sDate());
		append(sb, _4sAuthor());
		append(sb, _4sMetaInfo());
		sb.append(thankToTesters());
		return sb.toString();
	}

	public void removeAuthor(SinglePerson singlePerson) {
		author.getPersonList().remove(singlePerson);
		editor.set(author.toString());
	}

	public void removeTester(SinglePerson singlePerson) {
		tester.getPersonList().remove(singlePerson);
	}

	public QuestionNaturalNumerator getNumerator() {
		return numerator;
	}

	public void setNumerator(QuestionNaturalNumerator numerator) {
		this.numerator = numerator;
	}

}
