/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.io;

import java.io.Serializable;

/**
 * Enumeration of IO case sensitivity.
 * <p>
 * Different filing systems have different rules for case-sensitivity.
 * Windows is case-insensitive, Unix is case-sensitive.
 * <p>
 * This class captures that difference, providing an enumeration to
 * control how filename comparisons should be performed. It also provides
 * methods that use the enumeration to perform comparisons.
 * <p>
 * Wherever possible, you should use the <code>check</code> methods in this
 * class to compare filenames.
 *
 * @author Stephen Colebourne
 * @version $Id: IOCase.java 1003647 2010-10-01 20:53:59Z niallp $
 * @since Commons IO 1.3
 */
public final class IOCase implements Serializable {

    /**
     * The constant for case sensitive regardless of operating system.
     */
    public static final IOCase SENSITIVE = new IOCase("Sensitive", true);

    /**
     * The constant for case insensitive regardless of operating system.
     */
    public static final IOCase INSENSITIVE = new IOCase("Insensitive", false);

    /**
     * The constant for case sensitivity determined by the current operating system.
     * Windows is case-insensitive when comparing filenames, Unix is case-sensitive.
     * <p>
     * <strong>Note:</strong> This only caters for Windows and Unix. Other operating
     * systems (e.g. OSX and OpenVMS) are treated as case sensitive if they use the
     * Unix file separator and case-insensitive if they use the Windows file separator
     * (see {@link java.io.File#separatorChar}).
     * <p>
     * If you derialize this constant of Windows, and deserialize on Unix, or vice
     * versa, then the value of the case-sensitivity flag will change.
     */
    public static final IOCase SYSTEM = new IOCase("System", !FilenameUtils.isSystemWindows());

    /** Serialization version. */
    private static final long serialVersionUID = -6343169151696340687L;

    /** The enumeration name. */
    private final String name;

    /** The sensitivity flag. */
    private final transient boolean sensitive;

    //-----------------------------------------------------------------------
    /**
     * Factory method to create an IOCase from a name.
     *
     * @param name  the name to find
     * @return the IOCase object
     * @throws IllegalArgumentException if the name is invalid
     */
    public static IOCase forName(String name) {
        if (IOCase.SENSITIVE.name.equals(name)){
            return IOCase.SENSITIVE;
        }
        if (IOCase.INSENSITIVE.name.equals(name)){
            return IOCase.INSENSITIVE;
        }
        if (IOCase.SYSTEM.name.equals(name)){
            return IOCase.SYSTEM;
        }
        throw new IllegalArgumentException("Invalid IOCase name: " + name);
    }

    //-----------------------------------------------------------------------
    /**
     * Private constructor.
     *
     * @param name  the name
     * @param sensitive  the sensitivity
     */
    private IOCase(String name, boolean sensitive) {
        this.name = name;
        this.sensitive = sensitive;
    }

    /**
     * Replaces the enumeration from the stream with a real one.
     * This ensures that the correct flag is set for SYSTEM.
     *
     * @return the resolved object
     */
    private Object readResolve() {
        return forName(name);
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the name of the constant.
     *
     * @return the name of the constant
     */
    public String getName() {
        return name;
    }

    /**
     * Does the object represent case sensitive comparison.
     *
     * @return true if case sensitive
     */
    public boolean isCaseSensitive() {
        return sensitive;
    }

    //-----------------------------------------------------------------------
    /**
     * Compares two strings using the case-sensitivity rule.
     * <p>
     * This method mimics {@link String#compareTo} but takes case-sensitivity
     * into account.
     *
     * @param str1  the first string to compare, not null
     * @param str2  the second string to compare, not null
     * @return true if equal using the case rules
     * @throws NullPointerException if either string is null
     */
    public int checkCompareTo(String str1, String str2) {
        if (str1 == null || str2 == null) {
            throw new NullPointerException("The strings must not be null");
        }
        return sensitive ? str1.compareTo(str2) : str1.compareToIgnoreCase(str2);
    }

    /**
     * Compares two strings using the case-sensitivity rule.
     * <p>
     * This method mimics {@link String#equals} but takes case-sensitivity
     * into account.
     *
     * @param str1  the first string to compare, not null
     * @param str2  the second string to compare, not null
     * @return true if equal using the case rules
     * @throws NullPointerException if either string is null
     */
    public boolean checkEquals(String str1, String str2) {
        if (str1 == null || str2 == null) {
            throw new NullPointerException("The strings must not be null");
        }
        return sensitive ? str1.equals(str2) : str1.equalsIgnoreCase(str2);
    }

    /**
     * Checks if one string starts with another using the case-sensitivity rule.
     * <p>
     * This method mimics {@link String#startsWith(String)} but takes case-sensitivity
     * into account.
     *
     * @param str  the string to check, not null
     * @param start  the start to compare against, not null
     * @return true if equal using the case rules
     * @throws NullPointerException if either string is null
     */
    public boolean checkStartsWith(String str, String start) {
        return str.regionMatches(!sensitive, 0, start, 0, start.length());
    }

    /**
     * Checks if one string ends with another using the case-sensitivity rule.
     * <p>
     * This method mimics {@link String#endsWith} but takes case-sensitivity
     * into account.
     *
     * @param str  the string to check, not null
     * @param end  the end to compare against, not null
     * @return true if equal using the case rules
     * @throws NullPointerException if either string is null
     */
    public boolean checkEndsWith(String str, String end) {
        int endLen = end.length();
        return str.regionMatches(!sensitive, str.length() - endLen, end, 0, endLen);
    }

    /**
     * Checks if one string contains another starting at a specific index using the
     * case-sensitivity rule.
     * <p>
     * This method mimics parts of {@link String#indexOf(String, int)}
     * but takes case-sensitivity into account.
     *
     * @param str  the string to check, not null
     * @param strStartIndex  the index to start at in str
     * @param search  the start to search for, not null
     * @return the first index of the search String,
     *  -1 if no match or <code>null</code> string input
     * @throws NullPointerException if either string is null
     * @since Commons IO 2.0
     */
    public int checkIndexOf(String str, int strStartIndex, String search) {
        int endIndex = str.length() - search.length();
        if (endIndex >= strStartIndex) {
            for (int i = strStartIndex; i <= endIndex; i++) {
                if (checkRegionMatches(str, i, search)) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Checks if one string contains another at a specific index using the case-sensitivity rule.
     * <p>
     * This method mimics parts of {@link String#regionMatches(boolean, int, String, int, int)}
     * but takes case-sensitivity into account.
     *
     * @param str  the string to check, not null
     * @param strStartIndex  the index to start at in str
     * @param search  the start to search for, not null
     * @return true if equal using the case rules
     * @throws NullPointerException if either string is null
     */
    public boolean checkRegionMatches(String str, int strStartIndex, String search) {
        return str.regionMatches(!sensitive, strStartIndex, search, 0, search.length());
    }

    //-----------------------------------------------------------------------
    /**
     * Gets a string describing the sensitivity.
     *
     * @return a string describing the sensitivity
     */
    @Override
    public String toString() {
        return name;
    }

}
