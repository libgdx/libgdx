package aurelienribon.libgdx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Aurelien Ribon | http://www.aurelienribon.com/
 */
public class LibraryDef {
	public final String name;
	public final String description;
	public final String homepage;
	public final String stableVersion;
	public final String stableUrl;
	public final String latestUrl;
	public final List<String> libsCommon;
	public final List<String> libsDesktop;
	public final List<String> libsAndroid;
	public final List<String> libsHtml;
	public boolean isUsed = false;

	public LibraryDef(String input) {
		this.name = parseBlock(input, "name");
		this.description = parseBlock(input, "description").replaceAll("\\s+", " ");
		this.homepage = parseBlock(input, "homepage");
		this.stableVersion = parseBlock(input, "stable-version");
		this.stableUrl = parseBlock(input, "stable-url");
		this.latestUrl = parseBlock(input, "latest-url");
		this.libsCommon = parseBlockAsList(input, "libs-common");
		this.libsDesktop = parseBlockAsList(input, "libs-desktop");
		this.libsAndroid = parseBlockAsList(input, "libs-android");
		this.libsHtml = parseBlockAsList(input, "libs-html");
	}

	private String parseBlock(String input, String name) {
		Matcher m = Pattern.compile("\\[" + name + "\\](.*?)(\\[|$)", Pattern.DOTALL).matcher(input);
		if (m.find()) return m.group(1).trim();
		throw new RuntimeException("block " + name + " not found");
	}

	private List<String> parseBlockAsList(String input, String name) {
		Matcher m = Pattern.compile("\\[" + name + "\\](.*?)(\\[|$)", Pattern.DOTALL).matcher(input);
		if (m.find()) {
			String str = m.group(1).trim();
			List<String> lines = new ArrayList<String>(Arrays.asList(str.split("\n")));
			for (int i=lines.size()-1; i>=0; i--) {
				String line = lines.get(i).trim();
				lines.set(i, line);
				if (line.equals("")) lines.remove(i);
			}
			return Collections.unmodifiableList(lines);
		}
		throw new RuntimeException("block " + name + " not found");
	}
}
