package aurelienribon.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * Basic class to manage templates. Enables replacements of special strings
 * embedded in text files. The manager also handles "ifdef" and "ifndef"
 * statements, for conditional replacements. Variables are looked for in the
 * format <code>@{variable}</code>.
 *
 * @author Aurelien Ribon | http://www.aurelienribon.com/
 */
public class TemplateManager {
	private final Map<String, String> replacements = new HashMap<String, String>();
	private final String varPattern = "[a-zA-Z_][a-zA-Z0-9_]*";

	/**
	 * Removes every defined variable.
	 */
	public void clear() {
		replacements.clear();
	}

	/**
	 * Registers a variable and its asociated replacement string.
	 */
	public void define(String variable, String replacement) {
		Matcher m = Pattern.compile(varPattern).matcher(variable);
		if (!m.matches()) throw new RuntimeException("Variable '" + variable + "' contains invalid characters");
		replacements.put(variable, replacement);
	}

	/**
	 * Registers a variable. Mainly used in "ifdef" tests, like in C
	 * preprocessor.
	 */
	public void define(String variable) {
		define(variable, "");
	}

	/**
	 * Opens the given file, processes its variables, and returns the result as
	 * a string.
	 * @throws IOException
	 */
	public String process(File file) throws IOException {
		String input = FileUtils.readFileToString(file);
		return process(input);
	}

	/**
	 * Opens the given file, processes its variables, and overwrites the file
	 * with the result.
	 * @throws IOException
	 */
	public void processOver(File file) throws IOException {
		String input = FileUtils.readFileToString(file);
		FileUtils.writeStringToFile(file, process(input));
	}

	/**
	 * Opens the given resource, processes its variables, and returns the result
	 * as a string.
	 */
	public String process(URL url) {
		try {
			String input = IOUtils.toString(url);
			return process(input);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Opens the given resource, processes its variables, and returns the result
	 * as a string.
	 */
	public String process(InputStream stream) {
		try {
			String input = IOUtils.toString(stream);
			return process(input);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Processes the variables over the given string, and returns the result.
	 */
	public String process(String input) {
		for (String var : replacements.keySet()) {
			input = input.replaceAll("@\\{" + var + "\\}", replacements.get(var));
		}

		{
			Pattern p = Pattern.compile("@\\{ifdef (" + varPattern + ")\\}(.*?)@\\{endif\\}", Pattern.DOTALL);
			Matcher m = p.matcher(input);
			StringBuffer sb = new StringBuffer();

			while (m.find()) {
				String var = m.group(1);
				String content = m.group(2);
				if (replacements.containsKey(var)) m.appendReplacement(sb, content);
				else m.appendReplacement(sb, "");
			}

			m.appendTail(sb);
			input = sb.toString();
		}

		{
			Pattern p = Pattern.compile("@\\{ifndef (" + varPattern + ")\\}(.*?)@\\{endif\\}", Pattern.DOTALL);
			Matcher m = p.matcher(input);
			StringBuffer sb = new StringBuffer();

			while (m.find()) {
				String var = m.group(1);
				String content = m.group(2);
				if (!replacements.containsKey(var)) m.appendReplacement(sb, content);
				else m.appendReplacement(sb, "");
			}

			m.appendTail(sb);
			input = sb.toString();
		}

		return input;
	}
}
