package shtykh.util.catalogue;

import shtykh.util.CSV;
import shtykh.util.Jsonable;
import shtykh.util.Util;

import java.util.Map;
import java.util.TreeMap;

/**
 * Created by shtykh on 05/10/15.
 */
public abstract class MapCatalogue<T extends Jsonable> extends Catalogue<String, T> {
	private Map<String, T> map;

	public MapCatalogue(Class<T> clazz, String propertyName) {
		super(clazz, propertyName);
	}

	@Override
	protected void initFields() {
		map = new TreeMap<>();
	}

	@Override
	public T get(String name) {
		return name == null?null:map.get(name);
	}

	protected void refreshKeys() {
		keys.set(new CSV(map.keySet()));
	}

	@Override
	protected void clearCash() {
		map.clear();
	}

	@Override
	public void add(String key, T p) {
		if (!map.containsKey(key)) {
			Util.write(file(key), p.toJson());
		}
		map.put(key, p);
	}

	@Override
	protected void add(T p) {
		add(p.toString(), p);
	}

	@Override
	protected int size() {
		return map.size();
	}

	@Override
	public Iterable<T> getAll() {
		return map.values();
	}
}
