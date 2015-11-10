package shtykh.util;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by shtykh on 01/04/15.
 */
public class Util {
	public static Random random = new Random();

	public static List<String> readLines(String filePath) {
		try(BufferedReader br = new BufferedReader(new FileReader(filePath))) {
			List<String> lines = new ArrayList<>();
			for (String line; (line = br.readLine()) != null; ) {
				lines.add(line);
			}
			return lines;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static String read(String filePath) {
		try(BufferedReader br = new BufferedReader(new FileReader(filePath))) {
			StringBuilder result = new StringBuilder();
			for (String line; (line = br.readLine()) != null; ) {
				result.append(line).append("\n");
			}
			return result.toString();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static String read(File file) {
		try(BufferedReader br = new BufferedReader(new FileReader(file))) {
			StringBuilder result = new StringBuilder();
			for (String line; (line = br.readLine()) != null; ) {
				result.append(line).append("\n");
			}
			return result.toString();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void write(File file, String s) {
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
			bw.write(s);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static File copyFileToDir(String sourcePath, File destFolder) throws IOException {
		File source = new File(sourcePath);
		File dest = new File(destFolder.getAbsolutePath() + "/" + source.getName());
		FileUtils.copyFile(source, dest);
		return dest;
	}

	public static String timestamp(String dateFormatString) {
		DateFormat df = new SimpleDateFormat(dateFormatString);
		Date today = Calendar.getInstance().getTime();
		return df.format(today);
	}

	public static int call(StringLogger logs, String[] cmd) throws IOException {
		logs.debug("calling " + org.apache.commons.lang.StringUtils.join(cmd, " "));
// create runtime to execute external command
		ProcessBuilder builder = new ProcessBuilder(cmd);
		builder.redirectErrorStream(true);
		Process pr = builder.start();
		logs.debug("process is " + (pr.isAlive() ? "" : "not ") + "alive");
// retrieve output from python script
		try {
			logs.logStream(pr.getInputStream());
			pr.waitFor();  // wait for process to complete
		} catch (InterruptedException e) {
			logs.error(e.getMessage());  // "Can'tHappen"
			return 1;
		}
		int status = pr.exitValue();
		logs.debug("Process done, exit status was " + status);
		return status;
	}

	public static class StringLogger {
		private final Logger log;
		private StringBuilder sb;
		private boolean debug = false;

		public StringLogger(Logger log, boolean debug) {
			this.debug = debug;
			sb = new StringBuilder();
			this.log = log;
		}

		public StringLogger info(String msg) {
			log.info(msg);
			sb.append(msg + "\n");
			return this;
		}

		public StringLogger debug(String msg) {
			if (debug) {
				log.debug(msg);
				sb.append("DEBUG: " + msg + "\n");
			}
			return this;
		}

		public StringLogger error(String msg) {
			log.error(msg);
			sb.append("ERROR: " + msg + "\n");
			return this;
		}

		public String toString() {
			return sb.toString();
		}

		public StringLogger logStream(InputStream stream) throws IOException {
			BufferedReader bfr = new BufferedReader(new InputStreamReader(stream));
			String line = "";
			while((line = bfr.readLine()) != null) {
				info(line);
			}
			return this;
		}
	}

	public static File saveFile(MultipartFile multipartFile,
								String serverLocation, String fileName) throws IOException {
		File directory = new File(serverLocation);
		directory.mkdirs();
		File file = new File(directory + "/" + fileName);
		int i = 0;
		while (file.exists()) {
			String[] split = fileName.split("\\.", 2);
			file = new File(directory + "/" + split[0] + "(" + (i++) + ")" + "." + split[1]);
		}
		try(OutputStream outpuStream = new FileOutputStream(file)){
			outpuStream.write(multipartFile.getBytes());
			outpuStream.flush();
			outpuStream.close();
			return file;
		}
	}
}
