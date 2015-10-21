package shtykh.util.catalogue;

import shtykh.util.CSV;
import shtykh.util.Jsonable;
import shtykh.util.Util;
import shtykh.util.html.form.material.FormMaterial;
import shtykh.util.html.form.material.FormParameterMaterial;

import java.io.File;
import java.io.IOException;

import static shtykh.util.Jsonable.fromJson;
import static shtykh.util.Util.read;

/**
 * Created by shtykh on 02/10/15.
 */
public abstract class Catalogue<K,T extends Jsonable> extends FolderKeaper implements FormMaterial {
	private final Class<T> clazz;
	protected FormParameterMaterial<CSV> keys = new FormParameterMaterial<>(new CSV(""), CSV.class);
	

	public Catalogue(Class<T> clazz, String fileName) {
		super(fileName);
		this.clazz = clazz;
		initFields();
		refresh();
	}

	protected abstract void initFields();

	@Override
	public void refresh() {
		super.refresh();
		refreshKeys();
	}

	@Override
	public void refreshFile(File file) {
		T p = fromJson(read(file), clazz);
		file.delete();
		add(p);
	}

	@Override
	public boolean isGood(File file) {
		return !file.isDirectory() && ! file.getName().startsWith(".");
	}

	public String[] keys() {
		refresh();
		return keys.get().asArray();
	}

	protected abstract void add(T p);

	protected File file(K name) {
		return new File(folder.getAbsolutePath() + "/" + name);
	}

	protected abstract void clearCash();
	
	public void remove(K name) {
		file(name).delete();
		refresh();
	}

	public void replace(K name, String folder) throws IOException {
		File destFolder = new File(this.folder.getAbsolutePath().replace(this.folder.getName(), folder));
		File fileToReplace = file(name);
		Util.copyFileToDir(fileToReplace.getAbsolutePath(), destFolder);
		fileToReplace.delete();
		refresh();
	}

	protected abstract void refreshKeys();
	protected abstract K getFileName(T p);
	public abstract void add(K number, T item);
	public abstract T get(K key);

	protected abstract int size();

	public abstract Iterable<T> getAll();
}
