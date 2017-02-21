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

/**
 * Breaks a string into tokens; new code should probably use {@link String#split}.
 *
 * <blockquote>
 * <pre>
 * // Legacy code:
 * StringTokenizer st = new StringTokenizer("a:b:c", ":");
 * while (st.hasMoreTokens()) {
 *     System.err.println(st.nextToken());
 * }
 *
 * // New code:
 * for (String token : "a:b:c".split(":")) {
 *     System.err.println(token);
 * }
 * </pre>
 * </blockquote>
 *
 * @since 1.0
 */
public class StringTokenizer implements Enumeration<Object> {

    private String string;

    private String delimiters;

    private boolean returnDelimiters;

    private int position;

    /**
     * Constructs a new {@code StringTokenizer} for the parameter string using
     * whitespace as the delimiter. The {@code returnDelimiters} flag is set to
     * {@code false}.
     *
     * @param string
     *            the string to be tokenized.
     */
    public StringTokenizer(String string) {
        this(string, " \t\n\r\f", false);
    }

    /**
     * Constructs a new {@code StringTokenizer} for the parameter string using
     * the specified delimiters. The {@code returnDelimiters} flag is set to
     * {@code false}. If {@code delimiters} is {@code null}, this constructor
     * doesn't throw an {@code Exception}, but later calls to some methods might
     * throw a {@code NullPointerException}.
     *
     * @param string
     *            the string to be tokenized.
     * @param delimiters
     *            the delimiters to use.
     */
    public StringTokenizer(String string, String delimiters) {
        this(string, delimiters, false);
    }

    /**
     * Constructs a new {@code StringTokenizer} for the parameter string using
     * the specified delimiters, returning the delimiters as tokens if the
     * parameter {@code returnDelimiters} is {@code true}. If {@code delimiters}
     * is null this constructor doesn't throw an {@code Exception}, but later
     * calls to some methods might throw a {@code NullPointerException}.
     *
     * @param string
     *            the string to be tokenized.
     * @param delimiters
     *            the delimiters to use.
     * @param returnDelimiters
     *            {@code true} to return each delimiter as a token.
     */
    public StringTokenizer(String string, String delimiters,
            boolean returnDelimiters) {
        if (string == null) {
            throw new NullPointerException("string == null");
        }
        this.string = string;
        this.delimiters = delimiters;
        this.returnDelimiters = returnDelimiters;
        this.position = 0;
    }

    /**
     * Returns the number of unprocessed tokens remaining in the string.
     *
     * @return number of tokens that can be retreived before an {@code
     *         Exception} will result from a call to {@code nextToken()}.
     */
    public int countTokens() {
        int count = 0;
        boolean inToken = false;
        for (int i = position, length = string.length(); i < length; i++) {
            if (delimiters.indexOf(string.charAt(i), 0) >= 0) {
                if (returnDelimiters)
                    count++;
                if (inToken) {
                    count++;
                    inToken = false;
                }
            } else {
                inToken = true;
            }
        }
        if (inToken)
            count++;
        return count;
    }

    /**
     * Returns {@code true} if unprocessed tokens remain. This method is
     * implemented in order to satisfy the {@code Enumeration} interface.
     *
     * @return {@code true} if unprocessed tokens remain.
     */
    public boolean hasMoreElements() {
        return hasMoreTokens();
    }

    /**
     * Returns {@code true} if unprocessed tokens remain.
     *
     * @return {@code true} if unprocessed tokens remain.
     */
    public boolean hasMoreTokens() {
        if (delimiters == null) {
            throw new NullPointerException("delimiters == null");
        }
        int length = string.length();
        if (position < length) {
            if (returnDelimiters)
                return true; // there is at least one character and even if
            // it is a delimiter it is a token

            // otherwise find a character which is not a delimiter
            for (int i = position; i < length; i++)
                if (delimiters.indexOf(string.charAt(i), 0) == -1)
                    return true;
        }
        return false;
    }

    /**
     * Returns the next token in the string as an {@code Object}. This method is
     * implemented in order to satisfy the {@code Enumeration} interface.
     *
     * @return next token in the string as an {@code Object}
     * @throws NoSuchElementException
     *                if no tokens remain.
     */
    public Object nextElement() {
        return nextToken();
    }

    /**
     * Returns the next token in the string as a {@code String}.
     *
     * @return next token in the string as a {@code String}.
     * @throws NoSuchElementException
     *                if no tokens remain.
     */
    public String nextToken() {
        if (delimiters == null) {
            throw new NullPointerException("delimiters == null");
        }
        int i = position;
        int length = string.length();

        if (i < length) {
            if (returnDelimiters) {
                if (delimiters.indexOf(string.charAt(position), 0) >= 0)
                    return String.valueOf(string.charAt(position++));
                for (position++; position < length; position++)
                    if (delimiters.indexOf(string.charAt(position), 0) >= 0)
                        return string.substring(i, position);
                return string.substring(i);
            }

            while (i < length && delimiters.indexOf(string.charAt(i), 0) >= 0)
                i++;
            position = i;
            if (i < length) {
                for (position++; position < length; position++)
                    if (delimiters.indexOf(string.charAt(position), 0) >= 0)
                        return string.substring(i, position);
                return string.substring(i);
            }
        }
        throw new NoSuchElementException();
    }

    /**
     * Returns the next token in the string as a {@code String}. The delimiters
     * used are changed to the specified delimiters.
     *
     * @param delims
     *            the new delimiters to use.
     * @return next token in the string as a {@code String}.
     * @throws NoSuchElementException
     *                if no tokens remain.
     */
    public String nextToken(String delims) {
        this.delimiters = delims;
        return nextToken();
    }
}
