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

package com.badlogic.gdx.utils;

import java.util.Locale;

/** {@code TextFormatter} is used by {@link I18NBundle} to perform argument replacement.
 * <p>
 * This class partially emulates its counterpart in the core package. See {@link #format(String, Object...)}
 * 
 * @author davebaol */
public class TextFormatter {

	private StringBuilder buffer = new StringBuilder();

	public TextFormatter (Locale locale, boolean useMessageFormat) {
		// both arguments are meaningless for GWT
	}

	/** Formats the given {@code pattern} replacing any placeholder of the form {0}, {1}, {2} and so on with the corresponding
	 * object from {@code args} converted to a string with {@code toString()}, so without taking into account the locale.
	 * <p>
	 * This method only implements a small subset of the grammar supported by {@link java.text.MessageFormat}. Especially,
	 * placeholder are only made up of an index; neither the type nor the style are supported.
	 * <p>
	 * If nothing has been replaced this implementation returns the pattern itself.
	 * 
	 * @param pattern the pattern
	 * @param args the arguments
	 * @return the formatted pattern
	 * @exception IllegalArgumentException if the pattern is invalid */
	public String format (String pattern, Object... args) {
		buffer.setLength(0);
		boolean changed = false;
		int placeholder = -1;
		int patternLength = pattern.length();
		for (int i = 0; i < patternLength; ++i) {
			char ch = pattern.charAt(i);
			if (placeholder < 0) { // processing constant part
				if (ch == '{') {
					changed = true;
					if (i + 1 < patternLength && pattern.charAt(i + 1) == '{') {
						buffer.append(ch); // handle escaped '{'
						++i;
					} else {
						placeholder = 0; // switch to placeholder part
					}
				} else {
					buffer.append(ch);
				}
			} else { // processing placeholder part
				if (ch == '}') {
					if (placeholder >= args.length)
						throw new IllegalArgumentException("Argument index out of bounds: " + placeholder);
					if (pattern.charAt(i - 1) == '{')
						throw new IllegalArgumentException("Missing argument index after a left curly brace");
					if (args[placeholder] == null)
						buffer.append("null"); // append null argument
					else
						buffer.append(args[placeholder].toString()); // append actual argument
					placeholder = -1; // switch to constant part
				} else {
					if (ch < '0' || ch > '9')
						throw new IllegalArgumentException("Unexpected '" + ch + "' while parsing argument index");
					placeholder = placeholder * 10 + (ch - '0');
				}
			}
		}
		if (placeholder >= 0) throw new IllegalArgumentException("Unmatched braces in the pattern.");

		return changed ? buffer.toString() : pattern;
	}
}
