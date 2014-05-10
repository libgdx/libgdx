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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;
//import libcore.icu.ICU;


/**
 * {@code Locale} represents a language/country/variant combination. Locales are used to
 * alter the presentation of information such as numbers or dates to suit the conventions
 * in the region they describe.
 *
 * <p>The language codes are two-letter lowercase ISO language codes (such as "en") as defined by
 * <a href="http://en.wikipedia.org/wiki/ISO_639-1">ISO 639-1</a>.
 * The country codes are two-letter uppercase ISO country codes (such as "US") as defined by
 * <a href="http://en.wikipedia.org/wiki/ISO_3166-1_alpha-3">ISO 3166-1</a>.
 * The variant codes are unspecified.
 *
 * <p>Note that Java uses several deprecated two-letter codes. The Hebrew ("he") language
 * code is rewritten as "iw", Indonesian ("id") as "in", and Yiddish ("yi") as "ji". This
 * rewriting happens even if you construct your own {@code Locale} object, not just for
 * instances returned by the various lookup methods.
 *
 * <a name="available_locales"><h3>Available locales</h3></a>
 * <p>This class' constructors do no error checking. You can create a {@code Locale} for languages
 * and countries that don't exist, and you can create instances for combinations that don't
 * exist (such as "de_US" for "German as spoken in the US").
 *
 * <p>Note that locale data is not necessarily available for any of the locales pre-defined as
 * constants in this class except for en_US, which is the only locale Java guarantees is always
 * available.
 *
 * <p>It is also a mistake to assume that all devices have the same locales available.
 * A device sold in the US will almost certainly support en_US and es_US, but not necessarily
 * any locales with the same language but different countries (such as en_GB or es_ES),
 * nor any locales for other languages (such as de_DE). The opposite may well be true for a device
 * sold in Europe.
 *
 * <p>You can use {@link Locale#getDefault} to get an appropriate locale for the <i>user</i> of the
 * device you're running on, or {@link Locale#getAvailableLocales} to get a list of all the locales
 * available on the device you're running on.
 *
 * <a name="locale_data"><h3>Locale data</h3></a>
 * <p>Note that locale data comes solely from ICU. User-supplied locale service providers (using
 * the {@code java.text.spi} or {@code java.util.spi} mechanisms) are not supported.
 *
 * <p>Here are the versions of ICU (and the corresponding CLDR and Unicode versions) used in
 * various Android releases:
 * <table BORDER="1" WIDTH="100%" CELLPADDING="3" CELLSPACING="0" SUMMARY="">
 * <tr><td>Cupcake/Donut/Eclair</td> <td>ICU 3.8</td> <td><a href="http://cldr.unicode.org/index/downloads/cldr-1-5">CLDR 1.5</a></td>   <td><a href="http://www.unicode.org/versions/Unicode5.0.0/">Unicode 5.0</a></td></tr>
 * <tr><td>Froyo</td>                <td>ICU 4.2</td> <td><a href="http://cldr.unicode.org/index/downloads/cldr-1-7">CLDR 1.7</a></td>   <td><a href="http://www.unicode.org/versions/Unicode5.1.0/">Unicode 5.1</a></td></tr>
 * <tr><td>Gingerbread/Honeycomb</td><td>ICU 4.4</td> <td><a href="http://cldr.unicode.org/index/downloads/cldr-1-8">CLDR 1.8</a></td>   <td><a href="http://www.unicode.org/versions/Unicode5.2.0/">Unicode 5.2</a></td></tr>
 * <tr><td>Ice Cream Sandwich</td>   <td>ICU 4.6</td> <td><a href="http://cldr.unicode.org/index/downloads/cldr-1-9">CLDR 1.9</a></td>   <td><a href="http://www.unicode.org/versions/Unicode6.0.0/">Unicode 6.0</a></td></tr>
 * <tr><td>Jelly Bean</td>           <td>ICU 4.8</td> <td><a href="http://cldr.unicode.org/index/downloads/cldr-2-0">CLDR 2.0</a></td>   <td><a href="http://www.unicode.org/versions/Unicode6.0.0/">Unicode 6.0</a></td></tr>
 * <tr><td>Jelly Bean MR2</td>       <td>ICU 50</td>  <td><a href="http://cldr.unicode.org/index/downloads/cldr-21-1">CLDR 22.1</a></td> <td><a href="http://www.unicode.org/versions/Unicode6.2.0/">Unicode 6.2</a></td></tr>
 * </table>
 *
 * <a name="default_locale"><h3>Be wary of the default locale</h3></a>
 * <p>Note that there are many convenience methods that automatically use the default locale, but
 * using them may lead to subtle bugs.
 *
 * <p>The default locale is appropriate for tasks that involve presenting data to the user. In
 * this case, you want to use the user's date/time formats, number
 * formats, rules for conversion to lowercase, and so on. In this case, it's safe to use the
 * convenience methods.
 *
 * <p>The default locale is <i>not</i> appropriate for machine-readable output. The best choice
 * there is usually {@code Locale.US}&nbsp;&ndash; this locale is guaranteed to be available on all
 * devices, and the fact that it has no surprising special cases and is frequently used (especially
 * for computer-computer communication) means that it tends to be the most efficient choice too.
 *
 * <p>A common mistake is to implicitly use the default locale when producing output meant to be
 * machine-readable. This tends to work on the developer's test devices (especially because so many
 * developers use en_US), but fails when run on a device whose user is in a more complex locale.
 *
 * <p>For example, if you're formatting integers some locales will use non-ASCII decimal
 * digits. As another example, if you're formatting floating-point numbers some locales will use
 * {@code ','} as the decimal point and {@code '.'} for digit grouping. That's correct for
 * human-readable output, but likely to cause problems if presented to another
 * computer ({@link Double#parseDouble} can't parse such a number, for example).
 * You should also be wary of the {@link String#toLowerCase} and
 * {@link String#toUpperCase} overloads that don't take a {@code Locale}: in Turkey, for example,
 * the characters {@code 'i'} and {@code 'I'} won't be converted to {@code 'I'} and {@code 'i'}.
 * This is the correct behavior for Turkish text (such as user input), but inappropriate for, say,
 * HTTP headers.
 */
public final class Locale implements Cloneable, Serializable {

    private static final long serialVersionUID = 9149081749638150636L;

    /**
     * Locale constant for en_CA.
     */
    public static final Locale CANADA = new Locale(true, "en", "CA");

    /**
     * Locale constant for fr_CA.
     */
    public static final Locale CANADA_FRENCH = new Locale(true, "fr", "CA");

    /**
     * Locale constant for zh_CN.
     */
    public static final Locale CHINA = new Locale(true, "zh", "CN");

    /**
     * Locale constant for zh.
     */
    public static final Locale CHINESE = new Locale(true, "zh", "");

    /**
     * Locale constant for en.
     */
    public static final Locale ENGLISH = new Locale(true, "en", "");

    /**
     * Locale constant for fr_FR.
     */
    public static final Locale FRANCE = new Locale(true, "fr", "FR");

    /**
     * Locale constant for fr.
     */
    public static final Locale FRENCH = new Locale(true, "fr", "");

    /**
     * Locale constant for de.
     */
    public static final Locale GERMAN = new Locale(true, "de", "");

    /**
     * Locale constant for de_DE.
     */
    public static final Locale GERMANY = new Locale(true, "de", "DE");

    /**
     * Locale constant for it.
     */
    public static final Locale ITALIAN = new Locale(true, "it", "");

    /**
     * Locale constant for it_IT.
     */
    public static final Locale ITALY = new Locale(true, "it", "IT");

    /**
     * Locale constant for ja_JP.
     */
    public static final Locale JAPAN = new Locale(true, "ja", "JP");

    /**
     * Locale constant for ja.
     */
    public static final Locale JAPANESE = new Locale(true, "ja", "");

    /**
     * Locale constant for ko_KR.
     */
    public static final Locale KOREA = new Locale(true, "ko", "KR");

    /**
     * Locale constant for ko.
     */
    public static final Locale KOREAN = new Locale(true, "ko", "");

    /**
     * Locale constant for zh_CN.
     */
    public static final Locale PRC = new Locale(true, "zh", "CN");

    /**
     * Locale constant for the root locale. The root locale has an empty language,
     * country, and variant.
     *
     * @since 1.6
     */
    public static final Locale ROOT = new Locale(true, "", "");

    /**
     * Locale constant for zh_CN.
     */
    public static final Locale SIMPLIFIED_CHINESE = new Locale(true, "zh", "CN");

    /**
     * Locale constant for zh_TW.
     */
    public static final Locale TAIWAN = new Locale(true, "zh", "TW");

    /**
     * Locale constant for zh_TW.
     */
    public static final Locale TRADITIONAL_CHINESE = new Locale(true, "zh", "TW");

    /**
     * Locale constant for en_GB.
     */
    public static final Locale UK = new Locale(true, "en", "GB");

    /**
     * Locale constant for en_US.
     */
    public static final Locale US = new Locale(true, "en", "US");

//    /**
//     * The current default locale. It is temporarily assigned to US because we
//     * need a default locale to lookup the real default locale.
//     */
    private static Locale defaultLocale = US;

//    static {
//        String language = System.getProperty("user.language", "en");
//        String region = System.getProperty("user.region", "US");
//        String variant = System.getProperty("user.variant", "");
//        defaultLocale = new Locale(language, region, variant);
//    }

    private transient String countryCode;
    private transient String languageCode;
    private transient String variantCode;
    private transient String cachedToStringResult;

    /**
     * There's a circular dependency between toLowerCase/toUpperCase and
     * Locale.US. Work around this by avoiding these methods when constructing
     * the built-in locales.
     *
     * @param unused required for this constructor to have a unique signature
     */
    private Locale(boolean unused, String lowerCaseLanguageCode, String upperCaseCountryCode) {
        this.languageCode = lowerCaseLanguageCode;
        this.countryCode = upperCaseCountryCode;
        this.variantCode = "";
    }

    /**
     * Constructs a new {@code Locale} using the specified language.
     */
    public Locale(String language) {
        this(language, "", "");
    }

    /**
     * Constructs a new {@code Locale} using the specified language and country codes.
     */
    public Locale(String language, String country) {
        this(language, country, "");
    }

    /**
     * Constructs a new {@code Locale} using the specified language, country,
     * and variant codes.
     */
    public Locale(String language, String country, String variant) {
        if (language == null || country == null || variant == null) {
            throw new NullPointerException("language=" + language +
                                           ",country=" + country +
                                           ",variant=" + variant);
        }
        if (language.isEmpty() && country.isEmpty()) {
            languageCode = "";
            countryCode = "";
            variantCode = variant;
            return;
        }

//        languageCode = language.toLowerCase(Locale.US);      // not supported by GWT
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

//        countryCode = country.toUpperCase(Locale.US);      // not supported by GWT
        countryCode = country.toUpperCase();

        // Work around for be compatible with RI
        variantCode = variant;
    }

//    @Override public Object clone() {
//        try {
//            return super.clone();
//        } catch (CloneNotSupportedException e) {
//            throw new AssertionError(e);
//        }
//    }

    /**
     * Returns true if {@code object} is a locale with the same language,
     * country and variant.
     */
    @Override public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof Locale) {
            Locale o = (Locale) object;
            return languageCode.equals(o.languageCode)
                    && countryCode.equals(o.countryCode)
                    && variantCode.equals(o.variantCode);
        }
        return false;
    }

//    /**
//     * Returns the system's installed locales. This array always includes {@code
//     * Locale.US}, and usually several others. Most locale-sensitive classes
//     * offer their own {@code getAvailableLocales} method, which should be
//     * preferred over this general purpose method.
//     *
//     * @see java.text.BreakIterator#getAvailableLocales()
//     * @see java.text.Collator#getAvailableLocales()
//     * @see java.text.DateFormat#getAvailableLocales()
//     * @see java.text.DateFormatSymbols#getAvailableLocales()
//     * @see java.text.DecimalFormatSymbols#getAvailableLocales()
//     * @see java.text.NumberFormat#getAvailableLocales()
//     * @see java.util.Calendar#getAvailableLocales()
//     */
//    public static Locale[] getAvailableLocales() {
//        return ICU.getAvailableLocales();
//    }

    /**
     * Returns the country code for this locale, or {@code ""} if this locale
     * doesn't correspond to a specific country.
     */
    public String getCountry() {
        return countryCode;
    }

    /**
     * Returns the user's preferred locale. This may have been overridden for
     * this process with {@link #setDefault}.
     *
     * <p>Since the user's locale changes dynamically, avoid caching this value.
     * Instead, use this method to look it up for each use.
     */
    public static Locale getDefault() {
        return defaultLocale;
    }

//    /**
//     * Equivalent to {@code getDisplayCountry(Locale.getDefault())}.
//     */
//    public final String getDisplayCountry() {
//        return getDisplayCountry(getDefault());
//    }
//
//    /**
//     * Returns the name of this locale's country, localized to {@code locale}.
//     * Returns the empty string if this locale does not correspond to a specific
//     * country.
//     */
//    public String getDisplayCountry(Locale locale) {
//        if (countryCode.isEmpty()) {
//            return "";
//        }
//        String result = ICU.getDisplayCountryNative(toString(), locale.toString());
//        if (result == null) { // TODO: do we need to do this, or does ICU do it for us?
//            result = ICU.getDisplayCountryNative(toString(), Locale.getDefault().toString());
//        }
//        return result;
//    }
//
//    /**
//     * Equivalent to {@code getDisplayLanguage(Locale.getDefault())}.
//     */
//    public final String getDisplayLanguage() {
//        return getDisplayLanguage(getDefault());
//    }
//
//    /**
//     * Returns the name of this locale's language, localized to {@code locale}.
//     * If the language name is unknown, the language code is returned.
//     */
//    public String getDisplayLanguage(Locale locale) {
//        if (languageCode.isEmpty()) {
//            return "";
//        }
//
//        // http://b/8049507 --- frameworks/base should use fil_PH instead of tl_PH.
//        // Until then, we're stuck covering their tracks, making it look like they're
//        // using "fil" when they're not.
//        String localeString = toString();
//        if (languageCode.equals("tl")) {
//            localeString = toNewString("fil", countryCode, variantCode);
//        }
//
//        String result = ICU.getDisplayLanguageNative(localeString, locale.toString());
//        if (result == null) { // TODO: do we need to do this, or does ICU do it for us?
//            result = ICU.getDisplayLanguageNative(localeString, Locale.getDefault().toString());
//        }
//        return result;
//    }
//
//    /**
//     * Equivalent to {@code getDisplayName(Locale.getDefault())}.
//     */
//    public final String getDisplayName() {
//        return getDisplayName(getDefault());
//    }
//
//    /**
//     * Returns this locale's language name, country name, and variant, localized
//     * to {@code locale}. The exact output form depends on whether this locale
//     * corresponds to a specific language, country and variant.
//     *
//     * <p>For example:
//     * <ul>
//     * <li>{@code new Locale("en").getDisplayName(Locale.US)} -> {@code English}
//     * <li>{@code new Locale("en", "US").getDisplayName(Locale.US)} -> {@code English (United States)}
//     * <li>{@code new Locale("en", "US", "POSIX").getDisplayName(Locale.US)} -> {@code English (United States,Computer)}
//     * <li>{@code new Locale("en").getDisplayName(Locale.FRANCE)} -> {@code anglais}
//     * <li>{@code new Locale("en", "US").getDisplayName(Locale.FRANCE)} -> {@code anglais (tats-Unis)}
//     * <li>{@code new Locale("en", "US", "POSIX").getDisplayName(Locale.FRANCE)} -> {@code anglais (tats-Unis,informatique)}.
//     * </ul>
//     */
//    public String getDisplayName(Locale locale) {
//        int count = 0;
//        StringBuilder buffer = new StringBuilder();
//        if (!languageCode.isEmpty()) {
//            String displayLanguage = getDisplayLanguage(locale);
//            buffer.append(displayLanguage.isEmpty() ? languageCode : displayLanguage);
//            ++count;
//        }
//        if (!countryCode.isEmpty()) {
//            if (count == 1) {
//                buffer.append(" (");
//            }
//            String displayCountry = getDisplayCountry(locale);
//            buffer.append(displayCountry.isEmpty() ? countryCode : displayCountry);
//            ++count;
//        }
//        if (!variantCode.isEmpty()) {
//            if (count == 1) {
//                buffer.append(" (");
//            } else if (count == 2) {
//                buffer.append(",");
//            }
//            String displayVariant = getDisplayVariant(locale);
//            buffer.append(displayVariant.isEmpty() ? variantCode : displayVariant);
//            ++count;
//        }
//        if (count > 1) {
//            buffer.append(")");
//        }
//        return buffer.toString();
//    }
//
//    /**
//     * Returns the full variant name in the default {@code Locale} for the variant code of
//     * this {@code Locale}. If there is no matching variant name, the variant code is
//     * returned.
//     */
//    public final String getDisplayVariant() {
//        return getDisplayVariant(getDefault());
//    }
//
//    /**
//     * Returns the full variant name in the specified {@code Locale} for the variant code
//     * of this {@code Locale}. If there is no matching variant name, the variant code is
//     * returned.
//     */
//    public String getDisplayVariant(Locale locale) {
//        if (variantCode.length() == 0) {
//            return variantCode;
//        }
//        String result = ICU.getDisplayVariantNative(toString(), locale.toString());
//        if (result == null) { // TODO: do we need to do this, or does ICU do it for us?
//            result = ICU.getDisplayVariantNative(toString(), Locale.getDefault().toString());
//        }
//        return result;
//    }
//
//    /**
//     * Returns the three-letter ISO 3166 country code which corresponds to the country
//     * code for this {@code Locale}.
//     * @throws MissingResourceException if there's no 3-letter country code for this locale.
//     */
//    public String getISO3Country() {
//        String code = ICU.getISO3CountryNative(toString());
//        if (!countryCode.isEmpty() && code.isEmpty()) {
//            throw new MissingResourceException("No 3-letter country code for locale: " + this, "FormatData_" + this, "ShortCountry");
//        }
//        return code;
//    }
//
//    /**
//     * Returns the three-letter ISO 639-2/T language code which corresponds to the language
//     * code for this {@code Locale}.
//     * @throws MissingResourceException if there's no 3-letter language code for this locale.
//     */
//    public String getISO3Language() {
//        String code = ICU.getISO3LanguageNative(toString());
//        if (!languageCode.isEmpty() && code.isEmpty()) {
//            throw new MissingResourceException("No 3-letter language code for locale: " + this, "FormatData_" + this, "ShortLanguage");
//        }
//        return code;
//    }
//
//    /**
//     * Returns an array of strings containing all the two-letter ISO 3166 country codes that can be
//     * used as the country code when constructing a {@code Locale}.
//     */
//    public static String[] getISOCountries() {
//        return ICU.getISOCountries();
//    }
//
//    /**
//     * Returns an array of strings containing all the two-letter ISO 639-1 language codes that can be
//     * used as the language code when constructing a {@code Locale}.
//     */
//    public static String[] getISOLanguages() {
//        return ICU.getISOLanguages();
//    }

    /**
     * Returns the language code for this {@code Locale} or the empty string if no language
     * was set.
     */
    public String getLanguage() {
        return languageCode;
    }

    /**
     * Returns the variant code for this {@code Locale} or an empty {@code String} if no variant
     * was set.
     */
    public String getVariant() {
        return variantCode;
    }

    @Override
    public synchronized int hashCode() {
        return countryCode.hashCode() + languageCode.hashCode()
                + variantCode.hashCode();
    }

    /**
     * Overrides the default locale. This does not affect system configuration,
     * and attempts to override the system-provided default locale may
     * themselves be overridden by actual changes to the system configuration.
     * Code that calls this method is usually incorrect, and should be fixed by
     * passing the appropriate locale to each locale-sensitive method that's
     * called.
     */
    public synchronized static void setDefault(Locale locale) {
        if (locale == null) {
            throw new NullPointerException("locale == null");
        }
        defaultLocale = locale;
    }

    /**
     * Returns the string representation of this {@code Locale}. It consists of the
     * language code, country code and variant separated by underscores.
     * If the language is missing the string begins
     * with an underscore. If the country is missing there are 2 underscores
     * between the language and the variant. The variant cannot stand alone
     * without a language and/or country code: in this case this method would
     * return the empty string.
     *
     * <p>Examples: "en", "en_US", "_US", "en__POSIX", "en_US_POSIX"
     */
    @Override
    public final String toString() {
        String result = cachedToStringResult;
        if (result == null) {
            result = cachedToStringResult = toNewString(languageCode, countryCode, variantCode);
        }
        return result;
    }

    private static String toNewString(String languageCode, String countryCode, String variantCode) {
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

//    private static final ObjectStreamField[] serialPersistentFields = {
//        new ObjectStreamField("country", String.class),
//        new ObjectStreamField("hashcode", int.class),
//        new ObjectStreamField("language", String.class),
//        new ObjectStreamField("variant", String.class),
//    };
//
//    private void writeObject(ObjectOutputStream stream) throws IOException {
//        ObjectOutputStream.PutField fields = stream.putFields();
//        fields.put("country", countryCode);
//        fields.put("hashcode", -1);
//        fields.put("language", languageCode);
//        fields.put("variant", variantCode);
//        stream.writeFields();
//    }
//
//    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
//        ObjectInputStream.GetField fields = stream.readFields();
//        countryCode = (String) fields.get("country", "");
//        languageCode = (String) fields.get("language", "");
//        variantCode = (String) fields.get("variant", "");
//    }
}
