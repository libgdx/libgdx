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
 * @author Aurelien Ribon | http://www.aurelienribon.com/
 */
public class TemplateManager {
	private final Map<String, String> replacements = new HashMap<String, String>();
	private final String varPattern = "[a-zA-Z_][a-zA-Z0-9_]*";

	public void define(String variable, String replacement) {
		Matcher m = Pattern.compile(varPattern).matcher(variable);
		if (!m.matches()) throw new RuntimeException("Variable '" + variable + "' contains invalid characters");
		replacements.put(variable, replacement);
	}

	public void define(String variable) {
		define(variable, "");
	}

	public String process(File file) throws IOException {
		String input = FileUtils.readFileToString(file);
		return process(input);
	}

	public void processOver(File file) throws IOException {
		String input = FileUtils.readFileToString(file);
		FileUtils.writeStringToFile(file, process(input));
	}

	public String process(URL url) {
		try {
			String input = IOUtils.toString(url);
			return process(input);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public String process(InputStream stream) {
		try {
			String input = IOUtils.toString(stream);
			return process(input);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public String process(String input) {
		for (String var : replacements.keySet()) {
			input = input.replaceAll("@\\{" + var + "\\}", replacements.get(var));
		}

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
		return input;
	}
}
