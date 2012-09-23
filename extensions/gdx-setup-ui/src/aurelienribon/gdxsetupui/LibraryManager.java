package aurelienribon.gdxsetupui;

import aurelienribon.utils.HttpUtils;
import aurelienribon.utils.HttpUtils.DownloadListener;
import aurelienribon.utils.HttpUtils.DownloadTask;
import aurelienribon.utils.ParseUtils;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;

/**
 * The library manager job is to retrieve the master configuration file,
 * and to download each library definition file. It maintains a collection of
 * definition files and urls.
 *
 * @author Aurelien Ribon | http://www.aurelienribon.com/
 */
public class LibraryManager {
	private final String configUrl;
	private final List<String> libraries = new ArrayList<String>();
	private final Map<String, String> librariesUrls = new HashMap<String, String>();
	private final Map<String, LibraryDef> librariesDefs = new HashMap<String, LibraryDef>();

	public LibraryManager(String configUrl) {
		this.configUrl = configUrl;
	}

	// -------------------------------------------------------------------------
	// Public API
	// -------------------------------------------------------------------------

	/**
	 * Asynchronously downloads the master config file and parses it to build
	 * the list of available libraries.
	 */
	public DownloadTask downloadConfigFile() {
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		DownloadTask task =  HttpUtils.downloadAsync(configUrl, output, "Master config file");

		task.addListener(new DownloadListener() {
			@Override public void onComplete() {
				parseLibraries(output.toString());
			}
		});

		return task;
	}

	/**
	 * Asynchronously downloads the library definition file corresponding
	 * to the given name.
	 */
	public DownloadTask downloadDef(final String name) {
		if (!librariesUrls.containsKey(name)) return null;

		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		DownloadTask task =  HttpUtils.downloadAsync(librariesUrls.get(name), output, "Def '" + name + "'");

		task.addListener(new DownloadListener() {
			@Override public void onComplete() {
				registerLibraryDef(name, output);
			}
		});

		return task;
	}

	/**
	 * Manually adds a library definition file url. Used mostly for testing
	 * a library.
	 */
	public void addUrl(String name, String url) {
		if (!libraries.contains(name)) libraries.add(name);
		librariesUrls.put(name, url);
	}

	/**
	 * Manually adds a library definition file. Used mostly for testing a
	 * library.
	 */
	public void addDef(String name, LibraryDef def) {
		if (!libraries.contains(name)) libraries.add(name);
		librariesDefs.put(name, def);
	}

	/**
	 * Deletes every incomplete downloaded file (all the ".zip.tmp").
	 */
	public void cleanUpDownloads() {
		for (File file : new File(".").listFiles()) {
			if (file.isFile() && file.getName().endsWith(".zip.tmp")) {
				FileUtils.deleteQuietly(file);
			}
		}
	}

	public String getConfigUrl() {return configUrl;}
	public List<String> getNames() {return Collections.unmodifiableList(libraries);}
	public String getUrl(String name) {return librariesUrls.get(name);}
	public LibraryDef getDef(String name) {return librariesDefs.get(name);}

	// -------------------------------------------------------------------------
	// Helpers
	// -------------------------------------------------------------------------

	private void parseLibraries(String str) {
		List<String> lines = ParseUtils.parseBlockAsList(str, "libraries");

		for (String line : lines) {
			String[] parts = line.split("=", 2);
			if (parts.length != 2) continue;

			String name = parts[0].trim();
			if (!libraries.contains(name)) libraries.add(name);
			librariesUrls.put(name, parts[1].trim());
		}
	}

	private synchronized void registerLibraryDef(String name, ByteArrayOutputStream output) {
		librariesDefs.put(name, new LibraryDef(output.toString()));
	}
}
