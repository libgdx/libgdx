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

package com.badlogic.gdx.backends.android;

import android.util.Log;
import com.badlogic.gdx.ApplicationLogger;

/**
 * Default implementation of {@link ApplicationLogger} for android
 */
public class AndroidApplicationLogger implements ApplicationLogger {

	@Override
	public void log (String tag, String message) {
		Log.i(tag, message);
	}

	@Override
	public void log (String tag, String message, Throwable exception) {
		Log.i(tag, message, exception);
	}

	@Override
	public void error (String tag, String message) {
		Log.e(tag, message);
	}

	@Override
	public void error (String tag, String message, Throwable exception) {
		Log.e(tag, message, exception);
	}

	@Override
	public void debug (String tag, String message) {
		Log.d(tag, message);
	}

	@Override
	public void debug (String tag, String message, Throwable exception) {
		Log.d(tag, message, exception);
	}
}
