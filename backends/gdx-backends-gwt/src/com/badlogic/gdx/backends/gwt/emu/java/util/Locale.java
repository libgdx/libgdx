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

package java.util;

import java.io.Serializable;

/** {@code Locale} represents a language/country/variant combination. Locales are used to alter the presentation of information
 * such as numbers or dates to suit the conventions in the region they describe.
 *
 * <p>
 * The language codes are two-letter lowercase ISO language codes (such as "en") as defined by
 * <a href="http://en.wikipedia.org/wiki/ISO_639-1">ISO 639-1</a>. The country codes are two-letter uppercase ISO country codes
 * (such as "US") as defined by <a href="http://en.wikipedia.org/wiki/ISO_3166-1_alpha-3">ISO 3166-1</a>. The variant codes are
 * unspecified.
 *
 * <p>
 * Note that Java uses several deprecated two-letter codes. The Hebrew ("he") language code is rewritten as "iw", Indonesian
 * ("id") as "in", and Yiddish ("yi") as "ji". This rewriting happens even if you construct your own {@code Locale} object, not
 * just for instances returned by the various lookup methods.
 *
 * <a name="available_locales">
 * <h3>Available locales</h3></a>
 * <p>
 * This class' constructors do no error checking. You can create a {@code Locale} for languages and countries that don't exist,
 * and you can create instances for combinations that don't exist (such as "de_US" for "German as spoken in the US").
 *
 * <p>
 * Note that locale data is not necessarily available for any of the locales pre-defined as constants in this class except for
 * en_US, which is the only locale Java guarantees is always available.
 *
 * <p>
 * It is also a mistake to assume that all devices have the same locales available. A device sold in the US will almost certainly
 * support en_US and es_US, but not necessarily any locales with the same language but different countries (such as en_GB or
 * es_ES), nor any locales for other languages (such as de_DE). The opposite may well be true for a device sold in Europe.
 *
 * <p>
 * You can use {@link Locale#getDefault} to get an appropriate locale for the <i>user</i> of the device you're running on, or
 * {@link Locale#getAvailableLocales} to get a list of all the locales available on the device you're running on.
 *
 * <a name="locale_data">
 * <h3>Locale data</h3></a>
 * <p>
 * Note that locale data comes solely from ICU. User-supplied locale service providers (using the {@code java.text.spi} or
 * {@code java.util.spi} mechanisms) are not supported.
 *
 * <p>
 * See <a href="https://developer.android.com/guide/topics/resources/internationalization">Unicode and internationalization
 * support</a> for the versions of ICU (and the corresponding CLDR and Unicode versions) used in various Android releases.
 *
 * <a name="default_locale">
 * <h3>Be wary of the default locale</h3></a>
 * <p>
 * Note that there are many convenience methods that automatically use the default locale, but using them may lead to subtle bugs.
 *
 * <p>
 * The default locale is appropriate for tasks that involve presenting data to the user. In this case, you want to use the user's
 * date/time formats, number formats, rules for conversion to lowercase, and so on. In this case, it's safe to use the convenience
 * methods.
 *
 * <p>
 * The default locale is <i>not</i> appropriate for machine-readable output. The best choice there is usually
 * {@code Locale.US}&nbsp;&ndash; this locale is guaranteed to be available on all devices, and the fact that it has no surprising
 * special cases and is frequently used (especially for computer-computer communication) means that it tends to be the most
 * efficient choice too.
 *
 * <p>
 * A common mistake is to implicitly use the default locale when producing output meant to be machine-readable. This tends to work
 * on the developer's test devices (especially because so many developers use en_US), but fails when run on a device whose user is
 * in a more complex locale.
 *
 * <p>
 * For example, if you're formatting integers some locales will use non-ASCII decimal digits. As another example, if you're
 * formatting floating-point numbers some locales will use {@code ','} as the decimal point and {@code '.'} for digit grouping.
 * That's correct for human-readable output, but likely to cause problems if presented to another computer
 * ({@link Double#parseDouble} can't parse such a number, for example). You should also be wary of the {@link String#toLowerCase}
 * and {@link String#toUpperCase} overloads that don't take a {@code Locale}: in Turkey, for example, the characters {@code 'i'}
 * and {@code 'I'} won't be converted to {@code 'I'} and {@code 'i'}. This is the correct behavior for Turkish text (such as user
 * input), but inappropriate for, say, HTTP headers. */
public final class Locale implements Cloneable, Serializable {

	private static final long serialVersionUID = 9149081749638150636L;

	/** Locale constant for en_CA. */
	public static final Locale CANADA = new Locale(true, "en", "CA");

	/** Locale constant for fr_CA. */
	public static final Locale CANADA_FRENCH = new Locale(true, "fr", "CA");

	/** Locale constant for zh_CN. */
	public static final Locale CHINA = new Locale(true, "zh", "CN");

	/** Locale constant for zh. */
	public static final Locale CHINESE = new Locale(true, "zh", "");

	/** Locale constant for en. */
	public static final Locale ENGLISH = new Locale(true, "en", "");

	/** Locale constant for fr_FR. */
	public static final Locale FRANCE = new Locale(true, "fr", "FR");

	/** Locale constant for fr. */
	public static final Locale FRENCH = new Locale(true, "fr", "");

	/** Locale constant for de. */
	public static final Locale GERMAN = new Locale(true, "de", "");

	/** Locale constant for de_DE. */
	public static final Locale GERMANY = new Locale(true, "de", "DE");

	/** Locale constant for it. */
	public static final Locale ITALIAN = new Locale(true, "it", "");

	/** Locale constant for it_IT. */
	public static final Locale ITALY = new Locale(true, "it", "IT");

	/** Locale constant for ja_JP. */
	public static final Locale JAPAN = new Locale(true, "ja", "JP");

	/** Locale constant for ja. */
	public static final Locale JAPANESE = new Locale(true, "ja", "");

	/** Locale constant for ko_KR. */
	public static final Locale KOREA = new Locale(true, "ko", "KR");

	/** Locale constant for ko. */
	public static final Locale KOREAN = new Locale(true, "ko", "");

	/** Locale constant for zh_CN. */
	public static final Locale PRC = new Locale(true, "zh", "CN");

	/** Locale constant for the root locale. The root locale has an empty language, country, and variant.
	 *
	 * @since 1.6 */
	public static final Locale ROOT = new Locale(true, "", "");

	/** Locale constant for zh_CN. */
	public static final Locale SIMPLIFIED_CHINESE = new Locale(true, "zh", "CN");

	/** Locale constant for zh_TW. */
	public static final Locale TAIWAN = new Locale(true, "zh", "TW");

	/** Locale constant for zh_TW. */
	public static final Locale TRADITIONAL_CHINESE = new Locale(true, "zh", "TW");

	/** Locale constant for en_GB. */
	public static final Locale UK = new Locale(true, "en", "GB");

	/** Locale constant for en_US. */
	public static final Locale US = new Locale(true, "en", "US");

	private static Locale defaultLocale = initDefault();

	private transient String countryCode;
	private transient String languageCode;
	private transient String variantCode;
	private transient String cachedToStringResult;

	/** There's a circular dependency between toLowerCase/toUpperCase and Locale.US. Work around this by avoiding these methods
	 * when constructing the built-in locales.
	 *
	 * @param unused required for this constructor to have a unique signature */
	private Locale (boolean unused, String lowerCaseLanguageCode, String upperCaseCountryCode) {
		this.languageCode = lowerCaseLanguageCode;
		this.countryCode = upperCaseCountryCode;
		this.variantCode = "";
	}

	/** Constructs a new {@code Locale} using the specified language. */
	public Locale (String language) {
		this(language, "", "");
	}

	/** Constructs a new {@code Locale} using the specified language and country codes. */
	public Locale (String language, String country) {
		this(language, country, "");
	}

	/** Constructs a new {@code Locale} using the specified language, country, and variant codes. */
	public Locale (String language, String country, String variant) {
		if (language == null || country == null || variant == null) {
			throw new NullPointerException("language=" + language + ",country=" + country + ",variant=" + variant);
		}
		if (language.isEmpty() && country.isEmpty()) {
			languageCode = "";
			countryCode = "";
			variantCode = variant;
			return;
		}

		languageCode = language.toLowerCase();
		// Map new language codes to the obsolete language
		// codes so the correct resource bundles will be used.
		if (languageCode.equals("he")) {
			languageCode = "iw";
		} else if (languageCode.equals("id")) {
			languageCode = "in";
		} else if (languageCode.equals("yi")) {
			languageCode = "ji";
		}

		countryCode = country.toUpperCase();

		// Work around for be compatible with RI
		variantCode = variant;
	}

	/** Returns true if {@code object} is a locale with the same language, country and variant. */
	@Override
	public boolean equals (Object object) {
		if (object == this) {
			return true;
		}
		if (object instanceof Locale) {
			Locale o = (Locale)object;
			return languageCode.equals(o.languageCode) && countryCode.equals(o.countryCode) && variantCode.equals(o.variantCode);
		}
		return false;
	}

	/** Returns the country code for this locale, or {@code ""} if this locale doesn't correspond to a specific country. */
	public String getCountry () {
		return countryCode;
	}

	/** Returns the user's preferred locale. This may have been overridden for this process with {@link #setDefault}.
	 *
	 * <p>
	 * Since the user's locale changes dynamically, avoid caching this value. Instead, use this method to look it up for each
	 * use. */
	public static Locale getDefault () {
		return defaultLocale;
	}

	private static Locale initDefault () {
		Locale defaultLoc = US;

		String browserLanguage = getBrowserLanguage();

		if (browserLanguage != null && browserLanguage.length() > 0) {
			String[] locale = browserLanguage.split("-");

			defaultLoc = new Locale(true, locale[0].toLowerCase(), locale.length > 1 ? locale[1].toUpperCase() : "");
		}

		return defaultLoc;
	}

	/** @return browser language in format "de", "en-US" */
	private native static String getBrowserLanguage () /*-{
       return $wnd.navigator.languages ? $wnd.navigator.languages[0] : $wnd.navigator.userLanguage || $wnd.navigator.language;
    }-*/;

	/** Returns the language code for this {@code Locale} or the empty string if no language was set. */
	public String getLanguage () {
		return languageCode;
	}

	/** Returns the variant code for this {@code Locale} or an empty {@code String} if no variant was set. */
	public String getVariant () {
		return variantCode;
	}

	@Override
	public synchronized int hashCode () {
		return countryCode.hashCode() + languageCode.hashCode() + variantCode.hashCode();
	}

	/** Overrides the default locale. This does not affect system configuration, and attempts to override the system-provided
	 * default locale may themselves be overridden by actual changes to the system configuration. Code that calls this method is
	 * usually incorrect, and should be fixed by passing the appropriate locale to each locale-sensitive method that's called. */
	public synchronized static void setDefault (Locale locale) {
		if (locale == null) {
			throw new NullPointerException("locale == null");
		}
		defaultLocale = locale;
	}

	/** Returns the string representation of this {@code Locale}. It consists of the language code, country code and variant
	 * separated by underscores. If the language is missing the string begins with an underscore. If the country is missing there
	 * are 2 underscores between the language and the variant. The variant cannot stand alone without a language and/or country
	 * code: in this case this method would return the empty string.
	 *
	 * <p>
	 * Examples: "en", "en_US", "_US", "en__POSIX", "en_US_POSIX" */
	@Override
	public final String toString () {
		String result = cachedToStringResult;
		if (result == null) {
			result = cachedToStringResult = toNewString(languageCode, countryCode, variantCode);
		}
		return result;
	}

	private static String toNewString (String languageCode, String countryCode, String variantCode) {
		// The string form of a locale that only has a variant is the empty string.
		if (languageCode.length() == 0 && countryCode.length() == 0) {
			return "";
		}
		// Otherwise, the output format is "ll_cc_variant", where language and country are always
		// two letters, but the variant is an arbitrary length. A size of 11 characters has room
		// for "en_US_POSIX", the largest "common" value. (In practice, the string form is almost
		// always 5 characters: "ll_cc".)
		StringBuilder result = new StringBuilder(11);
		result.append(languageCode);
		if (countryCode.length() > 0 || variantCode.length() > 0) {
			result.append('_');
		}
		result.append(countryCode);
		if (variantCode.length() > 0) {
			result.append('_');
		}
		result.append(variantCode);
		return result.toString();
	}

}
