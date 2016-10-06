package shtykh.util.synth;

import org.springframework.stereotype.Component;
import shtykh.util.CMD;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by shtykh on 05/09/16.
 */
@Component
public class MacOSVoice implements Synthesator {
	private static final int THREAD_NUMBER = 5;
	private ExecutorService executorService = Executors.newFixedThreadPool(THREAD_NUMBER);
	
	@Override
	public void say(String text) {
		executorService.submit(() -> {
			try {
				new CMD("say").with(text).call();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}

	@Override
	public File record(String text, String path) {
		try {
			new CMD("say").with("-o", path).call();
		} catch (IOException e) {
				throw new RuntimeException(e);
		}
		return new File(path);
	}
}
