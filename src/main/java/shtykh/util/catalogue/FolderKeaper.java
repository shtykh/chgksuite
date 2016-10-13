package shtykh.util.catalogue;

import shtykh.util.CSV;
import shtykh.util.args.PropertyReader;
import shtykh.util.html.form.material.FormMaterial;
import shtykh.util.html.form.material.FormParameterMaterial;

import java.io.File;
import java.util.*;

/**
 * Created by shtykh on 08/10/15.
 */
public abstract class FolderKeaper extends PropertyReader implements FormMaterial{
	protected File folder;
	protected FormParameterMaterial<CSV> keys = new FormParameterMaterial<>(new CSV(""), CSV.class);
	
	public String[] keys() throws Exception {
		refresh();
		return keys.get().asArray();
	}

	private void initFolder(String filename) {
		try {
			folder = new File(filename);
			if (!folder.exists()) {
				folder.mkdirs();
			} else if (!folder.isDirectory()) {
				throw new Exception(filename + " must be a directory!");
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void afterRun() {
		initFolder(folderName());
	}

	public String folderName() {
		return getProperty(folderNameKey());
	}

	protected abstract String folderNameKey();

	public void refresh() throws Exception{
		clearCash();
		List<File> files = Arrays.asList(folder.listFiles());
		Collections.sort(files, getFilesComparator());
		for (File file : files) {
			if (isGood(file)) {
				refreshFile(file);
			}
		}
	}

	public List<String> listFileNames() throws Exception{
		clearCash();
		List<File> files = Arrays.asList(folder.listFiles());
		Collections.sort(files, getFilesComparator());
		List<String> goodFiles = new ArrayList<>();
		for (File file : files) {
			if (isGood(file)) {
				goodFiles.add(file.getName());
			}
		}
		return goodFiles;
	}

	public void clearFolder() {
		clearCash();
		for(File file: folder.listFiles()) {
			if (isGood(file)) {
				file.delete();
			}
		}
	}

	protected abstract void clearCash();

	public abstract void refreshFile(File file) throws Exception;

	public abstract boolean isGood(File file);

	public Comparator<? super File> getFilesComparator() {
		return new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				return o1.compareTo(o2);
			}
		};
	}
}
