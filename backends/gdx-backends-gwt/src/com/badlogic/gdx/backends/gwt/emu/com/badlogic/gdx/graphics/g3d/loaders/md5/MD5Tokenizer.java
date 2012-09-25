package com.badlogic.gdx.graphics.g3d.loaders.md5;

import java.util.List;

import com.google.gwt.core.client.JsArrayString;

public class MD5Tokenizer {

	public static void tokenize (String line, List<String> tokens) {
		tokens.clear();
		JsArrayString nativeTokens = nativeTokenize(line);
		for (int i = 0, j = nativeTokens.length(); i < j; i++) {
			String string = nativeTokens.get(i);
			tokens.add(string);
		}
	}
	
	public static native JsArrayString nativeTokenize(String line) /*-{
		var tokens = [];
		var m = new RegExp(/\"([^\"]*)\"|(\S+)/g);
		while ((match = m.exec(line)) != null) {
			if (match[1] != null) {            
				tokens.push(match[1]);
			} else {
				tokens.push(match[2]);
			}
		}
		return tokens;
	}-*/;		
}
