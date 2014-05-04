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
import java.util.MissingResourceException;

import com.badlogic.gdx.files.FileHandle;

/** A {@code SimpleMessageBundle} is a bundle of string containing arguments. The replaced arguments are not localized.
 * 
 * @author davebaol */
public class SimpleMessageBundle extends I18NBundle {

	StringBuilder buffer = new StringBuilder();

	/** Creates a new {@code SimpleMessageBundle} using the specified <code>baseFileHandle</code>, the default locale and the
	 * default encoding "ISO-8859-1".
	 * 
	 * <p>
	 * This method is a shortcut for {@link I18NBundle#createBundle(Class, FileHandle)
	 * I18NBundle.createBundle(SimpleMessageBundle.class, baseFileHandle)}
	 * 
	 * @param baseFileHandle the file handle to the base of the bundle
	 * @exception NullPointerException if <code>baseFileHandle</code> is <code>null</code>
	 * @exception MissingResourceException if no bundle for the specified base file handle can be found
	 * @return a {@code SimpleMessageBundle} for the given base file handle and the default locale */
	public static SimpleMessageBundle createBundle (FileHandle baseFileHandle) {
		return I18NBundle.createBundle(SimpleMessageBundle.class, baseFileHandle);
	}

	/** Creates a new {@code SimpleMessageBundle} using the specified <code>baseFileHandle</code> and <code>locale</code>. Also, the
	 * default encoding "ISO-8859-1" is used.
	 * 
	 * <p>
	 * This method is a shortcut for {@link I18NBundle#createBundle(Class, FileHandle, Locale)
	 * I18NBundle.createBundle(SimpleMessageBundle.class, baseFileHandle, locale)}
	 * 
	 * @param baseFileHandle the file handle to the base of the bundle
	 * @param locale the locale for which a bundle is desired
	 * @return a {@code SimpleMessageBundle} for the given base file handle and locale
	 * @exception NullPointerException if <code>baseFileHandle</code> or <code>locale</code> is <code>null</code>
	 * @exception MissingResourceException if no bundle for the specified base file handle can be found */
	public static SimpleMessageBundle createBundle (FileHandle baseFileHandle, Locale locale) {
		return I18NBundle.createBundle(SimpleMessageBundle.class, baseFileHandle, locale);
	}

	/** Creates a new {@code SimpleMessageBundle} using the specified <code>baseFileHandle</code> and <code>encoding</code>; the
	 * default locale is used.
	 * 
	 * <p>
	 * This method is a shortcut for {@link I18NBundle#createBundle(Class, FileHandle, String)
	 * I18NBundle.createBundle(SimpleMessageBundle.class, baseFileHandle, encoding)}
	 * 
	 * @param baseFileHandle the file handle to the base of the bundle
	 * @param encoding the charter encoding
	 * @return a {@code SimpleMessageBundle} for the given base file handle and locale
	 * @exception NullPointerException if <code>baseFileHandle</code> or <code>encoding</code> is <code>null</code>
	 * @exception MissingResourceException if no bundle for the specified base file handle can be found */
	public static SimpleMessageBundle createBundle (FileHandle baseFileHandle, String encoding) {
		return I18NBundle.createBundle(SimpleMessageBundle.class, baseFileHandle, encoding);
	}

	/** Creates a new {@code SimpleMessageBundle} using the specified <code>baseFileHandle</code>, <code>locale</code> and
	 * <code>encoding</code>.
	 * 
	 * <p>
	 * This method is a shortcut for {@link I18NBundle#createBundle(Class, FileHandle, Locale, String)
	 * I18NBundle.createBundle(SimpleMessageBundle.class, baseFileHandle, locale, encoding)}
	 * 
	 * @param baseFileHandle the file handle to the base of the bundle
	 * @param locale the locale for which a bundle is desired
	 * @param encoding the charter encoding
	 * @return a {@code SimpleMessageBundle} for the given base file handle and locale
	 * @exception NullPointerException if <code>baseFileHandle</code>, <code>locale</code> or <code>encoding</code> is
	 *               <code>null</code>
	 * @exception MissingResourceException if no bundle for the specified base file handle can be found */
	public static SimpleMessageBundle createBundle (FileHandle baseFileHandle, Locale locale, String encoding) {
		return I18NBundle.createBundle(SimpleMessageBundle.class, baseFileHandle, locale, encoding);
	}

	/** Formats the given {@code pattern} replacing any placeholder with the corresponding object from {@code args} converted to a
	 * string with {@code String.toString()}.
	 * <p>
	 * If nothing has been replaced this implementation returns the pattern itself.
	 * 
	 * @param pattern the pattern
	 * @param args the arguments
	 * @return the formatted pattern
	 * @exception IllegalArgumentException if the pattern is invalid */
	@Override
	protected String formatPattern (String pattern, Object[] args) {
		buffer.setLength(0);
		boolean hasPlaceholders = false;
		boolean hasQuotes = false;
		int placeholder = -1;
		boolean inQuote = false;
		int patternLength = pattern.length();
		for (int i = 0; i < patternLength; ++i) {
			char ch = pattern.charAt(i);
			if (placeholder < 0) { // processing constant part
				if (ch == '\'') {
					hasQuotes = true;
					if (i + 1 < patternLength && pattern.charAt(i + 1) == '\'') {
						buffer.append(ch); // handle doubles
						++i;
					} else {
						inQuote = !inQuote;
					}
				} else if (ch == '{' && !inQuote) {
					hasPlaceholders = true;
					placeholder = 0;
				} else {
					buffer.append(ch);
				}
			} else { // processing placeholder
				if (ch == '}') {
					if (placeholder >= args.length)
						throw new IllegalArgumentException("Out of bounds argument number:" + placeholder);
					if (args[placeholder] == null)
						buffer.append("null"); // append null argument
					else
						buffer.append(args[placeholder].toString()); // append actual argument
					placeholder = -1; // reset placeholder
				} else {
					if (ch < '0' || ch > '9')
						throw new IllegalArgumentException("Unexpected '" + ch + "' while parsing argument number");
					placeholder = placeholder * 10 + (ch - '0');
				}
			}
		}
		if (placeholder >= 0) throw new IllegalArgumentException("Unmatched braces in the pattern.");

		return hasPlaceholders || hasQuotes ? buffer.toString() : pattern;
	}
}
