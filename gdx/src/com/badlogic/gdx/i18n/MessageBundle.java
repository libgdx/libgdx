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

package com.badlogic.gdx.i18n;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.StringBuilder;

/** A {@code MessageBundle} is a bundle of strings containing typed arguments. The replaced arguments are properly localized
 * according to the bundle's locale.
 * <p>
 * {@code MessageBundle}s are not supported by the GWT backend.
 * 
 * @see MessageFormat
 * @author davebaol */
public class MessageBundle extends I18NBundle {

	/** The MessageFormat for this bundle used to format parametric strings with the bundle locale. */
	private MessageFormat messageFormat;

	/** Creates a new {@code MessageBundle} using the specified <code>baseFileHandle</code>, the default locale and the default
	 * encoding "ISO-8859-1".
	 * 
	 * <p>
	 * This method is a shortcut for {@link I18NBundle#createBundle(Class, FileHandle) I18NBundle.createBundle(MessageBundle.class,
	 * baseFileHandle)}
	 * 
	 * @param baseFileHandle the file handle to the base of the bundle
	 * @exception NullPointerException if <code>baseFileHandle</code> is <code>null</code>
	 * @exception MissingResourceException if no bundle for the specified base file handle can be found
	 * @return a {@code MessageBundle} for the given base file handle and the default locale */
	public static MessageBundle createBundle (FileHandle baseFileHandle) {
		return I18NBundle.createBundle(MessageBundle.class, baseFileHandle);
	}

	/** Creates a new {@code MessageBundle} using the specified <code>baseFileHandle</code> and <code>locale</code>. Also, the
	 * default encoding "ISO-8859-1" is used.
	 * 
	 * <p>
	 * This method is a shortcut for {@link I18NBundle#createBundle(Class, FileHandle, Locale)
	 * I18NBundle.createBundle(MessageBundle.class, baseFileHandle, locale)}
	 * 
	 * @param baseFileHandle the file handle to the base of the bundle
	 * @param locale the locale for which a bundle is desired
	 * @return a {@code MessageBundle} for the given base file handle and locale
	 * @exception NullPointerException if <code>baseFileHandle</code> or <code>locale</code> is <code>null</code>
	 * @exception MissingResourceException if no bundle for the specified base file handle can be found */
	public static MessageBundle createBundle (FileHandle baseFileHandle, Locale locale) {
		return I18NBundle.createBundle(MessageBundle.class, baseFileHandle, locale);
	}

	/** Creates a new {@code MessageBundle} using the specified <code>baseFileHandle</code> and <code>encoding</code>; the default
	 * locale is used.
	 * 
	 * <p>
	 * This method is a shortcut for {@link I18NBundle#createBundle(Class, FileHandle, String)
	 * I18NBundle.createBundle(MessageBundle.class, baseFileHandle, encoding)}
	 * 
	 * @param baseFileHandle the file handle to the base of the bundle
	 * @param encoding the charter encoding
	 * @return a {@code MessageBundle} for the given base file handle and locale
	 * @exception NullPointerException if <code>baseFileHandle</code> or <code>encoding</code> is <code>null</code>
	 * @exception MissingResourceException if no bundle for the specified base file handle can be found */
	public static MessageBundle createBundle (FileHandle baseFileHandle, String encoding) {
		return I18NBundle.createBundle(MessageBundle.class, baseFileHandle, encoding);
	}

	/** Creates a new {@code MessageBundle} using the specified <code>baseFileHandle</code>, <code>locale</code> and
	 * <code>encoding</code>.
	 * 
	 * <p>
	 * This method is a shortcut for {@link I18NBundle#createBundle(Class, FileHandle, Locale, String)
	 * I18NBundle.createBundle(MessageBundle.class, baseFileHandle, locale, encoding)}
	 * 
	 * @param baseFileHandle the file handle to the base of the bundle
	 * @param locale the locale for which a bundle is desired
	 * @param encoding the charter encoding
	 * @return a {@code MessageBundle} for the given base file handle and locale
	 * @exception NullPointerException if <code>baseFileHandle</code>, <code>locale</code> or <code>encoding</code> is
	 *               <code>null</code>
	 * @exception MissingResourceException if no bundle for the specified base file handle can be found */
	public static MessageBundle createBundle (FileHandle baseFileHandle, Locale locale, String encoding) {
		return I18NBundle.createBundle(MessageBundle.class, baseFileHandle, locale, encoding);
	}

	@Override
	protected void setLocale (Locale locale) {
		super.setLocale(locale);

		// Create a MessageFormat with the specified locale
		this.messageFormat = new MessageFormat("", locale);
	}

	/** Formats the given {@code pattern} replacing any placeholder with the corresponding object from {@code args} converted to a
	 * string properly localized for the bundle's locale.
	 * <p>
	 * This implementation uses {@code java.text.MessageFormat}.
	 * 
	 * @param pattern the pattern
	 * @param args the arguments
	 * @return the formatted pattern
	 * @exception IllegalArgumentException if the pattern is invalid */
	@Override
	protected String formatPattern (String pattern, Object[] args) {
		messageFormat.applyPattern(pattern);
		return messageFormat.format(args);
	}
}
