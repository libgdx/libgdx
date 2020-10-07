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

package com.badlogic.gdx.backends.headless;

import com.badlogic.gdx.ApplicationLogger;

/**
 * Default implementation of {@link ApplicationLogger} for headless
 */
public class HeadlessApplicationLogger implements ApplicationLogger {

	@Override
	public void log (String tag, String message) {
		System.out.println("[" + tag + "] " + message);
	}

	@Override
	public void log (String tag, String message, Throwable exception) {
		System.out.println("[" + tag + "] " + message);
		exception.printStackTrace(System.out);
	}

	@Override
	public void error (String tag, String message) {
		System.err.println("[" + tag + "] " + message);
	}

	@Override
	public void error (String tag, String message, Throwable exception) {
		System.err.println("[" + tag + "] " + message);
		exception.printStackTrace(System.err);
	}

	@Override
	public void debug (String tag, String message) {
		System.out.println("[" + tag + "] " + message);
	}

	@Override
	public void debug (String tag, String message, Throwable exception) {
		System.out.println("[" + tag + "] " + message);
		exception.printStackTrace(System.out);
	}
}
