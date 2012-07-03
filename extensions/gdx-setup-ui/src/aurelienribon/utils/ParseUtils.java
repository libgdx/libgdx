package aurelienribon.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Aurelien Ribon | http://www.aurelienribon.com/
 */
public class ParseUtils {
	public static String parseBlock(String input, String name, String defaultStr) {
		Matcher m = Pattern.compile("\\[" + name + "\\](.*?)(\\[|$)", Pattern.DOTALL).matcher(input);
		if (m.find()) return m.group(1).trim();
		return defaultStr;
	}

	public static List<String> parseBlockAsList(String input, String name) {
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
		return Collections.unmodifiableList(new ArrayList<String>());
	}
}
