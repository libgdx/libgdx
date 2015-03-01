/*******************************************************************************
 * Copyright 2015 See AUTHORS file.
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

package com.badlogic.gdx.graphics.debugging;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** @see GLDebugger
 * @author Jan Polák */
public interface GLDebuggerErrorListener {

	/** Put your error logging code here.
	 * @see GLDebugger#resolveErrorNumber(int) */
	public void onError (int error);

	// Basic implementations

	/** Listener that will log using Gdx.app.error GL error number and GL function. */
	public static final GLDebuggerErrorListener LOGGING_LISTENER = new GLDebuggerErrorListener() {
		@Override
		public void onError (int error) {
			String place = null;
			try {
				final StackTraceElement[] stack = Thread.currentThread().getStackTrace();
				for (int i = 0; i < stack.length; i++) {
					if ("check".equals(stack[i].getMethodName())) {
						if (i + 1 < stack.length) {
							final StackTraceElement glMethod = stack[i + 1];
							place = glMethod.getMethodName();
						}
						break;
					}
				}
			} catch (Exception ignored) {
			}

			if (place != null) {
				Gdx.app.error("GLDebugger", "Error " + GLDebugger.resolveErrorNumber(error) + " from " + place);
			} else {
				Gdx.app.error("GLDebugger", "Error " + GLDebugger.resolveErrorNumber(error) + " at: ", new Exception());
				// This will capture current stack trace for logging, if possible
			}
		}
	};

	/** Listener that will throw a GdxRuntimeException with error number. */
	public static final GLDebuggerErrorListener THROWING_LISTENER = new GLDebuggerErrorListener() {
		@Override
		public void onError (int error) {
			throw new GdxRuntimeException("GLDebugger: Got gl error " + GLDebugger.resolveErrorNumber(error));
		}
	};
}
