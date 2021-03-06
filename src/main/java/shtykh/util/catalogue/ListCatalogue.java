package shtykh.util.catalogue;

import shtykh.quedit.numerator.Numerable;
import shtykh.quedit.numerator.Numerator;
import shtykh.util.CSV;
import shtykh.util.Jsonable;
import shtykh.util.Util;
import shtykh.util.html.form.material.FormParameterMaterial;

import java.io.File;
import java.util.*;

/**
 * Created by shtykh on 05/10/15.
 */
public abstract class ListCatalogue<T extends Jsonable & Numerable> extends Catalogue<Integer, T> {
	private List<T> list;

	public ListCatalogue(Class<T> clazz) {
		super(clazz);
	}

	protected void refreshKeys() {
		keys.set(getNumerator().firstNumbers(size()));
	}

	protected abstract Numerator<T> getNumerator();

	protected void swap(int key, int key2) {
		Collections.swap(list, key, key2);
		File buffer = file(-1);
		file(key).renameTo(buffer);
		file(key2).renameTo(file(key));
		buffer.renameTo(file(key2));
	}

	@Override
	protected void clearCash() {
		list.clear();
	}

	@Override
	protected void initFields() {
		list = new ArrayList<>();
		keys = new FormParameterMaterial<>(new CSV(""), CSV.class);
	}

	@Override
	public void add(Integer index, T item) {
		if (index >= size()) {
			item.newIndex(index);
			list.add(item);
		}
		Util.write(file(index), item.toJson());
	}

	@Override
	public T get(Integer key) {
		try{
			return list.get(key);
		} catch (Exception e) {
			return null;
		}
	}

	public void up(Integer key) {
		swap(key, key - 1);
	}

	public void down(Integer key) {
		swap(key, key + 1);
	}

	@Override
	public int size() {
		return list.size();
	}

	@Override
	protected Integer getFileName(T p) {
		return list.indexOf(p);
	}

	@Override
	public List<T> getAll() {
		return list;
	}

	@Override
	protected void add(T p) {
		add(size(), p);
	}

	@Override
	public void addAll(Collection<T> objects) {
		for (T object : objects) {
			add(object);
		}
	}

	@Override
	public void remove(Integer name) throws Exception {
		list.remove(name);
		super.remove(name);
	}

	@Override
	public void copyTo(Integer name, String folder) throws Exception {
		super.copyTo(name, folder);
	}

	@Override
	protected String nextName(String name) {
		return String.valueOf(Integer.decode(name) + 1);
	}

	@Override
	public Comparator<? super File> getFilesComparator() {
		return new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				Boolean isGood1 = isGood(o1);
				Boolean isGood2 = isGood(o2);
				if(isGood1 && isGood2) {
					return Integer.decode(o1.getName()).compareTo(Integer.decode(o2.getName()));
				} else {
					return isGood1.compareTo(isGood2);
				}
			}
		};
	}
}
