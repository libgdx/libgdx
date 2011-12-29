package com.badlogic.gdx.jnigen.parsing;

import java.util.ArrayList;

public interface CMethodParser {
	public CMethodParserResult parse(String headerFile);
	
	public class CMethodParserResult {
		final ArrayList<CMethod> methods;
		
		public CMethodParserResult(ArrayList<CMethod> methods) {
			this.methods = methods;
		}

		public ArrayList<CMethod> getMethods() {
			return methods;
		}
	}
	
	public static class CMethod {
		final String returnType;
		final String head;
		final String[] argumentTypes;
		final int startIndex;
		final int endIndex;
		
		public CMethod(String returnType, String head, String[] argumentTypes, int startIndex, int endIndex) {
			this.returnType = returnType;
			this.head = head;
			this.argumentTypes = argumentTypes;
			this.startIndex = startIndex;
			this.endIndex = endIndex;
			
			for(int i = 0; i < argumentTypes.length; i++) {
				argumentTypes[i] = argumentTypes[i].trim();
			}
		}
		
		public String getReturnType() {
			return returnType;
		}

		public String getHead() {
			return head;
		}

		public String[] getArgumentTypes() {
			return argumentTypes;
		}

		public int getStartIndex() {
			return startIndex;
		}

		public int getEndIndex() {
			return endIndex;
		}
	}
}
