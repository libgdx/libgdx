/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.jnigen.parsing;

import java.util.ArrayList;

public interface CMethodParser {
	public CMethodParserResult parse (String headerFile);

	public class CMethodParserResult {
		final ArrayList<CMethod> methods;

		public CMethodParserResult (ArrayList<CMethod> methods) {
			this.methods = methods;
		}

		public ArrayList<CMethod> getMethods () {
			return methods;
		}
	}

	public static class CMethod {
		final String returnType;
		final String head;
		final String[] argumentTypes;
		final int startIndex;
		final int endIndex;

		public CMethod (String returnType, String head, String[] argumentTypes, int startIndex, int endIndex) {
			this.returnType = returnType;
			this.head = head;
			this.argumentTypes = argumentTypes;
			this.startIndex = startIndex;
			this.endIndex = endIndex;

			for (int i = 0; i < argumentTypes.length; i++) {
				argumentTypes[i] = argumentTypes[i].trim();
			}
		}

		public String getReturnType () {
			return returnType;
		}

		public String getHead () {
			return head;
		}

		public String[] getArgumentTypes () {
			return argumentTypes;
		}

		public int getStartIndex () {
			return startIndex;
		}

		public int getEndIndex () {
			return endIndex;
		}
	}
}
