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

import java.util.Locale;
import java.util.MissingResourceException;

import com.badlogic.gdx.files.FileHandle;

/** A {@ConstantBundle} is a bundle of constant strings.
 * 
 * @author davebaol */
public class ConstantBundle extends I18NBundle {

	/** Creates a new {@code ConstantBundle} using the specified <code>baseFileHandle</code>, the default locale and the default
	 * encoding "ISO-8859-1".
	 * 
	 * <p>
	 * This method is a shortcut for {@link I18NBundle#createBundle(Class, FileHandle)
	 * I18NBundle.createBundle(ConstantBundle.class, baseFileHandle)}
	 * 
	 * @param baseFileHandle the file handle to the base of the bundle
	 * @exception NullPointerException if <code>baseFileHandle</code> is <code>null</code>
	 * @exception MissingResourceException if no bundle for the specified base file handle can be found
	 * @return a {@code ConstantBundle} for the given base file handle and the default locale */
	public static ConstantBundle createBundle (FileHandle baseFileHandle) {
		return I18NBundle.createBundle(ConstantBundle.class, baseFileHandle);
	}

	/** Creates a new {@code ConstantBundle} using the specified <code>baseFileHandle</code> and <code>locale</code>. Also, the
	 * default encoding "ISO-8859-1" is used.
	 * 
	 * <p>
	 * This method is a shortcut for {@link I18NBundle#createBundle(Class, FileHandle, Locale)
	 * I18NBundle.createBundle(ConstantBundle.class, baseFileHandle, locale)}
	 * 
	 * @param baseFileHandle the file handle to the base of the bundle
	 * @param locale the locale for which a bundle is desired
	 * @return a {@code ConstantBundle} for the given base file handle and locale
	 * @exception NullPointerException if <code>baseFileHandle</code> or <code>locale</code> is <code>null</code>
	 * @exception MissingResourceException if no bundle for the specified base file handle can be found */
	public static ConstantBundle createBundle (FileHandle baseFileHandle, Locale locale) {
		return I18NBundle.createBundle(ConstantBundle.class, baseFileHandle, locale);
	}

	/** Creates a new {@code ConstantBundle} using the specified <code>baseFileHandle</code> and <code>encoding</code>; the default
	 * locale is used.
	 * 
	 * <p>
	 * This method is a shortcut for {@link I18NBundle#createBundle(Class, FileHandle, String)
	 * I18NBundle.createBundle(ConstantBundle.class, baseFileHandle, encoding)}
	 * 
	 * @param baseFileHandle the file handle to the base of the bundle
	 * @param encoding the charter encoding
	 * @return a {@code ConstantBundle} for the given base file handle and locale
	 * @exception NullPointerException if <code>baseFileHandle</code> or <code>encoding</code> is <code>null</code>
	 * @exception MissingResourceException if no bundle for the specified base file handle can be found */
	public static ConstantBundle createBundle (FileHandle baseFileHandle, String encoding) {
		return I18NBundle.createBundle(ConstantBundle.class, baseFileHandle, encoding);
	}

	/** Creates a new {@code ConstantBundle} using the specified <code>baseFileHandle</code>, <code>locale</code> and
	 * <code>encoding</code>.
	 * 
	 * <p>
	 * This method is a shortcut for {@link I18NBundle#createBundle(Class, FileHandle, Locale, String)
	 * I18NBundle.createBundle(ConstantBundle.class, baseFileHandle, locale, encoding)}
	 * 
	 * @param baseFileHandle the file handle to the base of the bundle
	 * @param locale the locale for which a bundle is desired
	 * @param encoding the charter encoding
	 * @return a {@code ConstantBundle} for the given base file handle and locale
	 * @exception NullPointerException if <code>baseFileHandle</code>, <code>locale</code> or <code>encoding</code> is
	 *               <code>null</code>
	 * @exception MissingResourceException if no bundle for the specified base file handle can be found */
	public static ConstantBundle createBundle (FileHandle baseFileHandle, Locale locale, String encoding) {
		return I18NBundle.createBundle(ConstantBundle.class, baseFileHandle, locale, encoding);
	}

	/** Returns {@code pattern} without formatting since this is a {@code ConstantMessage}.
	 * 
	 * @param pattern the pattern
	 * @param args the arguments
	 * @return the pattern itself
	 * @exception IllegalArgumentException if the pattern is invalid */
	@Override
	protected String formatPattern (String pattern, Object[] args) {
		return pattern;
	}
}
