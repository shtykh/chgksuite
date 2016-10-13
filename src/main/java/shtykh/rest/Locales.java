package shtykh.rest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import shtykh.util.catalogue.FolderKeaper;
import shtykh.util.html.HtmlHelper;
import shtykh.util.html.table.TableBuilder;
import shtykh.util.html.UriGenerator;

import java.io.File;
import java.text.MessageFormat;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;

import static shtykh.util.html.HtmlHelper.*;

/**
 * Created by shtykh on 07/10/16.
 */
@Controller
public class Locales extends FolderKeaper implements UriGenerator {
	private static final AtomicReference<Locale> instance = new AtomicReference<>();
	private Map<String, Locale> map = new TreeMap<>();
	
	@Autowired
	private HtmlHelper htmlHelper;

	public Locales() {}

	@Override
	protected String folderNameKey() {
		return "strings";
	}

	@Override
	public void afterRun() {
		super.afterRun();
		try {
			refresh();
			change(getProperty("DEFAULT_LOCALE"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@ResponseBody
	@RequestMapping("locales")
	public String all() {
		try {
			TableBuilder table = new TableBuilder();
			map.keySet().forEach(name -> table.addRow(href(uri(name), name)));
			return htmlPage(getString("PACKS"), table.buildHtml());
		} catch (Exception e) {
			return error(e);
		}
	}

	@ResponseBody
	@RequestMapping("locales/{name}")
	public String change(@PathVariable("name") String name) {
		try{
			instance.set(map.get(name));
			return getString("HELLO_LANG");
		} catch (Exception e) {
			return error(e);
		}
	}

	@Override
	protected void clearCash() {
		map.clear();
	}

	@Override
	public void refreshFile(File file) throws Exception {
		Locale locale = new Locale(file);
		map.put(locale.getName(), locale);
	}

	@Override
	public boolean isGood(File file) {
		return StringUtils.endsWith(file.getName(), "properties");
	}

	public static String getString(String key, Object... objects) {
		return MessageFormat.format(instance.get().getProperty(key), objects);
	}

	@Override
	public String base() {
		return "/locales";
	}

	@Override
	public HtmlHelper htmlHelper() {
		return htmlHelper;
	}
}
