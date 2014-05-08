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

import java.text.MessageFormat;
import java.util.Locale;

/** {@code TextFormatter} is used by {@link I18NBundle} to support argument replacement with a syntax similar to
 * {@link MessageFormat}. The only difference lies in escape sequences since the rules for using single quotes within message
 * format patterns have shown to be somewhat confusing. In particular, it isn't always obvious to localizers whether single quotes
 * need to be doubled or not. So we decided to provide a simpler escape sequence without limiting the expressive power of message
 * format patterns:
 * <ul>
 * <li>a single quote never needs to be escaped</li>
 * <li>a left curly brace must be doubled if you want it to be part of your string</li>
 * </ul>
 * 
 * @author davebaol */
class TextFormatter {

	private MessageFormat messageFormat;
	private StringBuilder buffer;

	public TextFormatter (Locale locale) {
		messageFormat = new MessageFormat("", locale);
		buffer = new StringBuilder();
	}

	/** Formats the given {@code pattern} replacing any placeholder with the corresponding object from {@code args} converted to a
	 * string properly localized for the bundle's locale.
	 * <p>
	 * This implementation uses {@code java.text.MessageFormat#format(Object)} after restoring the escape sequences occurring in the
	 * pattern.
	 * 
	 * @param pattern the pattern
	 * @param args the arguments
	 * @return the formatted pattern
	 * @exception IllegalArgumentException if the pattern is invalid */
	public String format (String pattern, Object... args) {
		messageFormat.applyPattern(replaceEscapeChars(pattern));
		return messageFormat.format(args);
	}

	// This code is needed because a simple replacement like
	// pattern.replace("'", "''").replace("{{", "'{'");
	// can't properly manage some special cases.
	// For example, the expected output for {{{{ is {{ but you get {'{ instead.
	// Also this code is optimized since a new string is returned only if something has been replaced.
	private String replaceEscapeChars (String pattern) {
		buffer.setLength(0);
		boolean changed = false;
		int len = pattern.length();
		for (int i = 0; i < len; i++) {
			char ch = pattern.charAt(i);
			if (ch == '\'') {
				changed = true;
				buffer.append("''");
			} else if (ch == '{') {
				int j = i + 1;
				while (j < len && pattern.charAt(j) == '{')
					j++;
				int escaped = (j - i) / 2;
				if (escaped > 0) {
					changed = true;
					buffer.append('\'');
					do {
						buffer.append('{');
					} while ((--escaped) > 0);
					buffer.append('\'');
				}
				if ((j - i) % 2 != 0) buffer.append('{');
				i = j - 1;
			} else {
				buffer.append(ch);
			}
		}
		return changed ? buffer.toString() : pattern;
	}
}
