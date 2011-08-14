/*
 * Copyright 2011 Rod Hyde (rod@badlydrawngames.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.badlydrawngames.general;

/** A <code>ScoreString</code> exists to prevent calls to {@link String#format(String, Object...)} as this seems to be rather
 * profligate at allocating memory, which in turns leads to garbage collection. Given that we can't manage that directly we do our
 * own thing.
 * 
 * @author Rod */
public class ScoreString implements CharSequence {

	public static final int DEFAULT_CHARS = 6;

	private final char[] score;
	private final int start;

	public ScoreString () {
		this(DEFAULT_CHARS);
	}

	public ScoreString (int numChars) {
		score = new char[numChars];
		int n = 1;
		for (int i = 0; i < numChars - 1; i++) {
			n *= 10;
		}
		start = n;
	}

	@Override
	public char charAt (int index) {
		return score[index];
	}

	@Override
	public int length () {
		return score.length;
	}

	@Override
	public CharSequence subSequence (int start, int end) {
		// Don't care. Call at your own risk.
		return null;
	}

	/** Sets this <code>ScoreString</code> to hold the given integer value.
	 * 
	 * @param v the value that the ScoreString will hold. */
	public void setScore (int v) {
		for (int n = start, i = 0; i < score.length; n /= 10, i++) {
			int j = (v / n) % 10;
			score[i] = (char)('0' + j);
		}
	}
}
