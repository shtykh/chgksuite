package shtykh.quedit.pack;

import shtykh.quedit.numerator.Numerator;
import shtykh.quedit.question.Question;
import shtykh.util.catalogue.ListCatalogue;

import java.util.Properties;

/**
 * Created by shtykh on 29/08/16.
 */
public class Questions extends ListCatalogue<Question> {
	private final String id;
	private final PackInfo info;

	public Questions(Properties properties, String id, PackInfo info) {
		super(Question.class);
		this.id = id;
		this.info = info;
		setProperties(properties);
		afterRun();
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

}
