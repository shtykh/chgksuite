package shtykh.util.catalogue;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by shtykh on 08/10/15.
 */
public abstract class FolderKeaper {
	protected File folder;
	public FolderKeaper(String folderName) {
		initFolder(folderName);
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

	public void refresh() {
		clearCash();
		try {
			List<File> files = Arrays.asList(folder.listFiles());
			Collections.sort(files, getFilesComparator());
			for (File file : files) {
				if (isGood(file)) {
					refreshFile(file);
				}
			}
		} catch (Exception ignored) {
			ignored.printStackTrace();
		}
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

	public abstract void refreshFile(File file);

	public abstract boolean isGood(File file);

	public String folderPath() {
		return folder.getAbsolutePath();
	}

	public Comparator<? super File> getFilesComparator() {
		return new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				return o1.compareTo(o2);
			}
		};
	}
}
