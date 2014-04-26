/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.badlogic.gdx.i18n;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** <code>Control</code> defines a set of callback methods that are invoked by the
 * {@link ResourceBundle#getBundle(String, Locale, Control) ResourceBundle.getBundle} factory methods during the bundle loading
 * process. In other words, a <code>Control</code> collaborates with the factory methods for loading resource bundles.
 * 
 * <p>
 * In addition to the callback methods, the {@link #toBundleName(String, Locale) toBundleName} and
 * {@link #toResourceName(String, String) toResourceName} methods are defined primarily for convenience in implementing the
 * callback methods. However, these 2 methods could be overridden to provide different conventions in the organization and
 * packaging of localized resources.
 * 
 * <p>
 * The formats returned by the {@link Control#getFormats(String) getFormats} method and candidate locales returned by the
 * {@link Control#getCandidateLocales(String, Locale) getCandidateLocales} method must be consistent in all
 * <code>ResourceBundle.getBundle</code> invocations for the same base bundle. Otherwise, the
 * <code>ResourceBundle.getBundle</code> methods may return unintended bundles. For example, assuming the existence of the 2
 * formats <code>F1</code> and <code>F2</code>, if only <code>F1</code> is returned by the <code>getFormats</code> method for the
 * first call to <code>ResourceBundle.getBundle</code> and only <code>F2</code> for the second call, then the second call will
 * return the <code>F1</code>-based one that has been cached during the first call.
 * 
 * <p>
 * A <code>Control</code> instance must be thread-safe if it's simultaneously used by multiple threads.
 * <code>ResourceBundle.getBundle</code> does not synchronize to call the <code>Control</code> methods. The default
 * implementations of the methods are thread-safe.
 * 
 * <p>
 * Applications can specify <code>Control</code> instances created from a subclass of <code>Control</code> to customize the bundle
 * loading process. The following are examples of changing the default bundle loading process. */
public abstract class Control {

	/** a constant that indicates cache will not be used. */
	public static final long TTL_DONT_CACHE = -1L;

	/** a constant that indicates cache will not be expired. */
	public static final long TTL_NO_EXPIRATION_CONTROL = -2L;

	/** Returns the auxiliary id of the bundle. This method is called by the <code>ResourceBundle.getBundle</code> factory method to
	 * create a unique cache key. This allows different <code>Control</code> implementations to support bundles who despite having
	 * the same name are considered different due to their different auxiliary ids.
	 * 
	 * <p>
	 * The default implementation returns <code>null</code>, meaning that there's no auxiliary id.
	 * @param baseName baseName the base name of the resource bundle
	 * @return the auxiliary id of the bundle. */
	public String getAuxId (String baseName) {
		return null;
	}

	/** Returns a <code>List</code> of <code>Locale</code>s as candidate locales for <code>baseName</code> and <code>locale</code>.
	 * This method is called by the <code>ResourceBundle.getBundle</code> factory method each time the factory method tries finding
	 * a resource bundle for a target <code>Locale</code>.
	 * 
	 * <p>
	 * The sequence of the candidate locales also corresponds to the runtime resource lookup path (also known as the <I>parent
	 * chain</I>), if the corresponding resource bundles for the candidate locales exist and their parents are not defined by
	 * loaded resource bundles themselves. The last element of the list must be a {@linkplain Locale#ROOT root locale} if it is
	 * desired to have the base bundle as the terminal of the parent chain.
	 * 
	 * <p>
	 * If the given locale is equal to <code>Locale.ROOT</code> (the root locale), a <code>List</code> containing only the root
	 * <code>Locale</code> must be returned. In this case, the <code>ResourceBundle.getBundle</code> factory method loads only the
	 * base bundle as the resulting resource bundle.
	 * 
	 * <p>
	 * It is not a requirement to return an immutable (unmodifiable) <code>List</code>. However, the returned <code>List</code>
	 * must not be mutated after it has been returned by <code>getCandidateLocales</code>.
	 * 
	 * <p>
	 * The default implementation returns a <code>List</code> containing <code>Locale</code>s in the following sequence:
	 * 
	 * <pre>
	 *     Locale(language, country, variant)
	 *     Locale(language, country)
	 *     Locale(language)
	 *     Locale.ROOT
	 * </pre>
	 * 
	 * where <code>language</code>, <code>country</code> and <code>variant</code> are the language, country and variant values of
	 * the given <code>locale</code>, respectively. Locales where the final component values are empty strings are omitted.
	 * 
	 * <p>
	 * The default implementation uses an {@link ArrayList} that overriding implementations may modify before returning it to the
	 * caller. However, a subclass must not modify it after it has been returned by <code>getCandidateLocales</code>.
	 * 
	 * <p>
	 * For example, if the given <code>baseName</code> is "Messages" and the given <code>locale</code> is
	 * <code>Locale("ja",&nbsp;"",&nbsp;"XX")</code>, then a <code>List</code> of <code>Locale</code>s:
	 * 
	 * <pre>
	 *     Locale("ja", "", "XX")
	 *     Locale("ja")
	 *     Locale.ROOT
	 * </pre>
	 * 
	 * is returned. And if the resource bundles for the "ja" and "" <code>Locale</code>s are found, then the runtime resource
	 * lookup path (parent chain) is:
	 * 
	 * <pre>
	 *     Messages_ja -> Messages
	 * </pre>
	 * 
	 * @param baseName the base name of the resource bundle
	 * @param locale the locale for which a resource bundle is desired
	 * @return a <code>List</code> of candidate <code>Locale</code>s for the given <code>locale</code>
	 * @exception NullPointerException if <code>baseName</code> or <code>locale</code> is <code>null</code> */
	public List<Locale> getCandidateLocales (String baseName, Locale locale) {
		if (baseName == null) {
			throw new NullPointerException();
		}
		String language = locale.getLanguage();
		String country = locale.getCountry();
		String variant = locale.getVariant();

		List<Locale> locales = new ArrayList<Locale>(4);
		if (variant.length() > 0) {
			locales.add(locale);
		}
		if (country.length() > 0) {
			locales.add((locales.size() == 0) ? locale : new Locale(language, country));
		}
		if (language.length() > 0) {
			locales.add((locales.size() == 0) ? locale : new Locale(language));
		}
		locales.add(Locale.ROOT);
		return locales;
	}

	/** Returns a <code>List</code> of <code>String</code>s containing formats to be used to load resource bundles for the given
	 * <code>baseName</code>. The <code>ResourceBundle.getBundle</code> factory method tries to load resource bundles with formats
	 * in the order specified by the list. The list returned by this method must have at least one <code>String</code>.
	 * 
	 * <p>
	 * It is not a requirement to return an immutable (unmodifiable) <code>List</code>. However, the returned <code>List</code>
	 * must not be mutated after it has been returned by <code>getFormats</code>.
	 * 
	 * @param baseName the base name of the resource bundle.
	 * @return a <code>List</code> of <code>String</code>s containing formats for loading resource bundles.
	 * @exception NullPointerException if <code>baseName</code> is null */
	public abstract List<String> getFormats (String baseName);

	/** Returns a <code>Locale</code> to be used as a fallback locale for further resource bundle searches by the
	 * <code>ResourceBundle.getBundle</code> factory method. This method is called from the factory method every time when no
	 * resulting resource bundle has been found for <code>baseName</code> and <code>locale</code>, where locale is either the
	 * parameter for <code>ResourceBundle.getBundle</code> or the previous fallback locale returned by this method.
	 * 
	 * <p>
	 * The method returns <code>null</code> if no further fallback search is desired.
	 * 
	 * <p>
	 * The default implementation returns the {@linkplain Locale#getDefault() default <code>Locale</code>} if the given
	 * <code>locale</code> isn't the default one. Otherwise, <code>null</code> is returned.
	 * 
	 * @param baseName the base name of the resource bundle for which <code>ResourceBundle.getBundle</code> has been unable to find
	 *           any resource bundles (except for the base bundle)
	 * @param locale the <code>Locale</code> for which <code>ResourceBundle.getBundle</code> has been unable to find any resource
	 *           bundles (except for the base bundle)
	 * @return a <code>Locale</code> for the fallback search, or <code>null</code> if no further fallback search is desired.
	 * @exception NullPointerException if <code>baseName</code> or <code>locale</code> is <code>null</code> */
	public Locale getFallbackLocale (String baseName, Locale locale) {
		if (baseName == null) {
			throw new NullPointerException();
		}
		Locale defaultLocale = Locale.getDefault();
		return locale.equals(defaultLocale) ? null : defaultLocale;
	}

	/** Returns a new ResourceBundle.
	 * 
	 * @param baseName the base name to use
	 * @param locale the given locale
	 * @param format the format
	 * @param reload whether to reload the resource
	 * @return a new ResourceBundle according to the give parameters */
	public abstract ResourceBundle newBundle (String baseName, Locale locale, String format, boolean reload) throws Exception;

	/** Returns true if the ResourceBundle needs to reload.
	 * 
	 * @param baseName the base name of the ResourceBundle
	 * @param locale the locale of the ResourceBundle
	 * @param format the format to load
	 * @param loadTime the expired time
	 * @return if the ResourceBundle needs to reload */
	public abstract boolean needsReload (String baseName, Locale locale, String format, ResourceBundle bundle, long loadTime);

	/** Returns the time-to-live (TTL) value for resource bundles that are loaded under this <code>Control</code>. Positive
	 * time-to-live values specify the number of milliseconds a bundle can remain in the cache without being validated against the
	 * source data from which it was constructed. The value 0 indicates that a bundle must be validated each time it is retrieved
	 * from the cache. {@link #TTL_DONT_CACHE} specifies that loaded resource bundles are not put in the cache.
	 * {@link #TTL_NO_EXPIRATION_CONTROL} specifies that loaded resource bundles are put in the cache with no expiration control.
	 * 
	 * <p>
	 * The expiration affects only the bundle loading process by the <code>ResourceBundle.getBundle</code> factory method. That is,
	 * if the factory method finds a resource bundle in the cache that has expired, the factory method calls the
	 * {@link #needsReload(String, Locale, String, ResourceBundle, long) needsReload} method to determine whether the resource
	 * bundle needs to be reloaded. If <code>needsReload</code> returns <code>true</code>, the cached resource bundle instance is
	 * removed from the cache. Otherwise, the instance stays in the cache, updated with the new TTL value returned by this method.
	 * 
	 * <p>
	 * All cached resource bundles are subject to removal from the cache due to memory constraints of the runtime environment.
	 * Returning a large positive value doesn't mean to lock loaded resource bundles in the cache.
	 * 
	 * <p>
	 * The default implementation returns {@link #TTL_NO_EXPIRATION_CONTROL}.
	 * 
	 * @param baseName the base name of the resource bundle for which the expiration value is specified.
	 * @param locale the locale of the resource bundle for which the expiration value is specified.
	 * @return the time (0 or a positive millisecond offset from the cached time) to get loaded bundles expired in the cache,
	 *         {@link #TTL_NO_EXPIRATION_CONTROL} to disable the expiration control, or {@link #TTL_DONT_CACHE} to disable caching.
	 * @exception NullPointerException if <code>baseName</code> or <code>locale</code> is <code>null</code> */
	public long getTimeToLive (String baseName, Locale locale) {
		if (baseName == null || locale == null) {
			throw new NullPointerException();
		}
		return TTL_NO_EXPIRATION_CONTROL;
	}

	/** Converts the given <code>baseName</code> and <code>locale</code> to the bundle name.
	 * 
	 * <p>
	 * This implementation returns the following value:
	 * 
	 * <pre>
	 * baseName + &quot;_&quot; + language + &quot;_&quot; + country + &quot;_&quot; + variant
	 * </pre>
	 * 
	 * where <code>language</code>, <code>country</code> and <code>variant</code> are the language, country and variant values of
	 * <code>locale</code>, respectively. Final component values that are empty Strings are omitted along with the preceding '_'.
	 * If all of the values are empty strings, then <code>baseName</code> is returned.
	 * 
	 * <p>
	 * For example, if <code>baseName</code> is <code>"baseName"</code> and <code>locale</code> is
	 * <code>Locale("ja",&nbsp;"",&nbsp;"XX")</code>, then <code>"baseName_ja_&thinsp;_XX"</code> is returned. If the given locale
	 * is <code>Locale("en")</code>, then <code>"baseName_en"</code> is returned.
	 * 
	 * <p>
	 * Overriding this method allows applications to use different conventions in the organization and packaging of localized
	 * resources.
	 * 
	 * @param baseName the base name of the resource bundle
	 * @param locale the locale for which a resource bundle should be loaded
	 * @return the bundle name for the resource bundle
	 * @exception NullPointerException if <code>baseName</code> or <code>locale</code> is <code>null</code> */
	public String toBundleName (String baseName, Locale locale) {
		if (locale.equals(Locale.ROOT)) {
			return baseName;
		}

		String language = locale.getLanguage();
		String country = locale.getCountry();
		String variant = locale.getVariant();
		boolean emptyLanguage = "".equals(language);
		boolean emptyCountry = "".equals(country);
		boolean emptyVariant = "".equals(variant);

		if (emptyLanguage && emptyCountry && emptyVariant) {
			return baseName;
		}

		StringBuilder sb = new StringBuilder(baseName);
		sb.append('_');
		if (!emptyVariant) {
			sb.append(language).append('_').append(country).append('_').append(variant);
		} else if (!emptyCountry) {
			sb.append(language).append('_').append(country);
		} else {
			sb.append(language);
		}
		return sb.toString();

	}

	/** An utility method to get the name of a resource based on the given bundleName and suffix. This implementation appends a
	 * <code>'.'</code> and the given file <code>suffix</code> to the <code>bundleName</code>. For example, if
	 * <code>bundleName</code> is <code>"foo/bar/MyResources_ja_JP"</code> and <code>suffix</code> is <code>"properties"</code>,
	 * then <code>"foo/bar/MyResources_ja_JP.properties"</code> is returned.
	 * 
	 * @param bundleName the bundle name
	 * @param suffix the file type suffix
	 * @return the name of the resource based on its bundleName and suffix
	 * @exception NullPointerException if <code>bundleName</code> or <code>suffix</code> is <code>null</code> */
	public String toResourceName (String bundleName, String suffix) {
		StringBuilder sb = new StringBuilder(bundleName.length() + 1 + suffix.length());
		sb.append(bundleName).append('.').append(suffix);
		return sb.toString();
	}

}
