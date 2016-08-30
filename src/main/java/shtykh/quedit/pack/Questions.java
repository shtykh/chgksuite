package shtykh.quedit.pack;

import org.apache.commons.lang3.StringUtils;
import shtykh.quedit.numerator.Numerator;
import shtykh.quedit.numerator.QuestionNaturalNumerator;
import shtykh.quedit.question.Question;
import shtykh.util.Jsonable;
import shtykh.util.Util;
import shtykh.util.catalogue.ListCatalogue;

import java.util.Properties;

/**
 * Created by shtykh on 29/08/16.
 */
public class Questions extends ListCatalogue<Question> {
	private final String id;
	private PackInfo info;

	public Questions(Properties properties, String id) {
		super(Question.class);
		this.id = id;
		setProperties(properties);
		initInfo();
		afterRun();
	}

	private void initInfo() {
		try {
			this.info = Jsonable.fromJson(Util.read(infoPath()), PackInfo.class);
		} catch (Exception e) {
			info = new PackInfo();
			info.save(infoPath());
		}
		if (StringUtils.isBlank(info.getName())) {
			info.setName(id);
		}
	}

	private String infoPath() {
		return folderName() + ".info";
	}

	protected String folderNameKey() {
		return "packs";
	}

	public String folderName() {
		return getProperty("packs") + "/" + id;
	}

	@Override
	public Numerator<Question> getNumerator() {
		return info.getNumerator();
	}

	@Override
	protected void swap(int key, int key2) {
		super.swap(key, key2);
		getNumerator().number(key, get(key2));
		getNumerator().number(key2, get(key));
	}

	@Override
	public void refresh() throws Exception {
		super.refresh();
		getAll().stream().forEach(getNumerator()::renumber);
		this.info = Jsonable.fromJson(Util.read(infoPath()), PackInfo.class);
	}

	public void setInfo(PackInfo info) {
		this.info = info;
	}

	public void saveInfo() {
		info.save(infoPath());
	}

	public PackInfo getInfo() {
		return info;
	}

	public void renumber(Question question) {
		getNumerator().renumber(question);
	}

	public String getName() {
		return info.getName();
	}

	public int getIndex(String number) {
		return getNumerator().getIndex(number);
	}

	public void setName(String name) {
		info.setName(name);
	}

	public void setNameLJ(String nameLJ) {
		info.setNameLJ(nameLJ);	
	}

	public void setDate(String date) {
		info.setDate(date);
	}

	public void setMetaInfo(String metaInfo) {
		info.setMetaInfo(metaInfo);
	}

	public String getMetaInfo() {
		return info.getMetaInfo();
	}

	public void setNumerator(QuestionNaturalNumerator numerator) {
		info.setNumerator(numerator);
	}

	public String getNumber(int index) {
		return getNumerator().getNumber(index);
	}

	public void number(Integer index, Question item) {
		getNumerator().number(index, item);
	}
}
