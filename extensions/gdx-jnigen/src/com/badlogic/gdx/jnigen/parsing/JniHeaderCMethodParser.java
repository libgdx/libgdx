package com.badlogic.gdx.jnigen.parsing;

import java.util.ArrayList;

public class JniHeaderCMethodParser implements CMethodParser {
	private static final String C_METHOD_MARKER = "JNIEXPORT";
	
	public CMethodParserResult parse(String headerFile) {
		ArrayList<CMethod> methods = new ArrayList<CMethod>();
		
		int index = headerFile.indexOf(C_METHOD_MARKER);
		if (index == -1)
			return null;
		while (index >= 0) {
			CMethod method = parseCMethod(headerFile, index);
			if (method == null)
				throw new RuntimeException("Couldn't parse method");
			methods.add(method);
			index = headerFile.indexOf(C_METHOD_MARKER, method.endIndex);
		}
		return new CMethodParserResult(methods);
	}
	
	private CMethod parseCMethod(String headerFile, int start) {
		int headEnd = headerFile.indexOf('(', start);
		String head = headerFile.substring(start, headEnd).trim();
		
		String returnType = head.split(" ")[1].trim();
		
		int argsStart = headEnd + 1;
		int argsEnd = headerFile.indexOf(')', argsStart);
		String[] args = headerFile.substring(argsStart, argsEnd).split(",");
		
		return new CMethod(returnType, head, args, start, argsEnd + 1);
	}
}
