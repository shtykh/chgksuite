package shtykh.util.synth;

import java.io.File;

/**
 * Created by shtykh on 05/09/16.
 */
public interface Synthesator {
	void say(String text);
	File record(String text, String path);
}
