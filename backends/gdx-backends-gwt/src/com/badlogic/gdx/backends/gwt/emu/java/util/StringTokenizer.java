/*
 * Copyright 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package java.util;

import java.util.NoSuchElementException;

public class StringTokenizer {
	private final String deli;
	private final String s;
	private final int len;

	private int pos;
	private String next;
	
	public StringTokenizer(String s, String deli) {
		this.s = s;
		this.deli = deli;
		len = s.length();
	}
	
	public StringTokenizer(String s) {
		this(s, " \t\n\r\f");
		
	}
	
	public String nextToken() {
		if(!hasMoreTokens()) {
			throw new NoSuchElementException();
		}
		String result = next;
		next = null;
		return result;
	}
	
	public boolean hasMoreTokens() {
		if (next != null) {
			return true;
		}
		// skip leading delimiters
		while (pos < len && deli.indexOf(s.charAt(pos)) != -1) {
			pos++;
		}
		
		if (pos >= len) {
			return false;
		}
		
		int p0 = pos++;
		while (pos < len && deli.indexOf(s.charAt(pos)) == -1) {
			pos++;
		}
		
		next = s.substring(p0, pos++);
		return true;
	}

}
