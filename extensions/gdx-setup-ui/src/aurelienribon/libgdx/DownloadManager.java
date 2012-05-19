package aurelienribon.libgdx;

import aurelienribon.utils.HttpUtils;
import aurelienribon.utils.ParseUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author Aurelien Ribon | http://www.aurelienribon.com/
 */
public class DownloadManager {
	private final URL configUrl;
	private final List<String> libraries = new ArrayList<String>();
	private final Map<String, URL> librariesUrls = new HashMap<String, URL>();
	private final Map<String, LibraryDef> librariesDefs = new HashMap<String, LibraryDef>();

	public DownloadManager(String configUrl) throws MalformedURLException {
		this.configUrl = new URL(configUrl);
	}

	public static class Callback {
		public void completed() {}
		public void error() {}
	}

	// -------------------------------------------------------------------------
	// Public API
	// -------------------------------------------------------------------------

	public void downloadConfigFile(final Callback callback) {
		libraries.clear();
		librariesUrls.clear();
		librariesDefs.clear();

		final ByteArrayOutputStream output = new ByteArrayOutputStream();

		HttpUtils.downloadAsync(configUrl, output, new HttpUtils.Callback() {
			@Override public void completed() {parseLibraries(output.toString()); callback.completed();}
			@Override public void error(IOException ex) {callback.error();}
		});
	}

	public void downloadLibraryDef(final String name, final Callback callback) {
		if (!librariesUrls.containsKey(name)) return;

		final ByteArrayOutputStream output = new ByteArrayOutputStream();

		HttpUtils.downloadAsync(librariesUrls.get(name), output, new HttpUtils.Callback() {
			@Override public void completed() {librariesDefs.put(name, new LibraryDef(output.toString())); callback.completed();}
			@Override public void error(IOException ex) {callback.error();}
		});
	}

	public void addTestLibraryUrl(String name, URL url) {
		libraries.add(name);
		librariesUrls.put(name, url);
	}

	public void addTestLibraryDef(String name, LibraryDef def) {
		libraries.add(name);
		librariesDefs.put(name, def);
	}

	public URL getConfigUrl() {
		return configUrl;
	}

	public List<String> getLibrariesNames() {
		return Collections.unmodifiableList(libraries);
	}

	public URL getLibraryUrl(String name) {
		return librariesUrls.get(name);
	}

	public LibraryDef getLibraryDef(String name) {
		return librariesDefs.get(name);
	}

	// -------------------------------------------------------------------------
	// Helpers
	// -------------------------------------------------------------------------

	private void parseLibraries(String str) {
		List<String> lines = ParseUtils.parseBlockAsList(str, "libraries");

		for (String line : lines) {
			String[] parts = line.split("=", 2);
			if (parts.length != 2) continue;

			String name = parts[0].trim();
			libraries.add(name);

			try {
				URL url = new URL(parts[1].trim());
				librariesUrls.put(name, url);
			} catch (MalformedURLException ex) {
			}
		}
	}
}
