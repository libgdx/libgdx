package com.badlogic.gdx.graphics.g3d.loaders.md5;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MD5Tokenizer {
	public static void tokenize(String line, List<String> tokens) {
		tokens.clear();
		String regex = "\"([^\"]*)\"|(\\S+)";
		Matcher m = Pattern.compile(regex).matcher(line);
		while (m.find()) {
			if (m.group(1) != null) {
				tokens.add(m.group(1));
			} else {
				tokens.add(m.group(2));
			}
		}		
	}
}