package shtykh.util;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * Created by shtykh on 05/09/16.
 */
public class CMD {
	private final List<String> parameters;

	public CMD(String command) {
		parameters = new ArrayList<>();
		parameters.add(command);
	}
	
	public CMD with(String key, String value) {
		parameters.add(key + " " + value);
		return this;
	}

	public CMD with(String parameter) {
		parameters.add(parameter);
		return this;
	}

	public CMD with(String... parameters) {
		this.parameters.addAll(Arrays.asList(parameters));
		return this;
	}

	public<T> T call(Util.StringLogger log, Supplier<T> onSuccess, Supplier<T> onFailure) throws IOException {
		if (call(log, asArray()) == 0) {
			return onSuccess.get();
		} else {
			return onFailure.get();
		}
	}
	
	public int call(Util.StringLogger log) throws IOException {
		return call(log, asArray());
	}

	public int call() throws IOException {
		return call(asArray());
	}

	public String[] asArray() {
		return parameters.toArray(new String[parameters.size()]);
	}

	public static int call(Util.StringLogger logs, String... cmd) throws IOException {
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

	public static int call(String[] cmd) throws IOException {
		return call(new Util.StringLogger(Logger.getLogger(Util.class), false), cmd);
	}
}
