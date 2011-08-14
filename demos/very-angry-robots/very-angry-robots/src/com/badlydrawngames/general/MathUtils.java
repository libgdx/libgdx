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

public class MathUtils {
	public static float abs (float n) {
		return (n >= 0.0f) ? n : -n;
	}

	public static float sgn (float n) {
		if (n > 0.0f)
			return 1.0f;
		else if (n < 0.0f)
			return -1.0f;
		else
			return 0.0f;
	}

	public static float min (float a, float b) {
		return (a < b) ? a : b;
	}

	public static float max (float a, float b) {
		return (a > b) ? a : b;
	}

	public static int min (int a, int b) {
		return (a < b) ? a : b;
	}

	public static int max (int a, int b) {
		return (a > b) ? a : b;
	}
}
