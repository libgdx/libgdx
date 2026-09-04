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

package com.badlogic.gdx;

import java.io.IOException;

/** The outcome of a verified preferences persist through {@link Preferences#flush(PreferencesSaveCallback)}.
 * <p>
 * Backends map their native results onto this enum on a best-effort basis because not every backend reports detailed failure
 * reasons. Classification relies on exception types and other non-localized identifiers only, never on OS-provided error
 * messages. The documented mapping is:
 * </p>
 * <ul>
 * <li>Android: {@code SharedPreferences.Editor#commit()} only returns a boolean, so failures are reported as
 * {@link #IO_ERROR}.</li>
 * <li>LWJGL3/LWJGL/Headless: writes go through {@code java.nio.file}, whose exception subtypes carry the OS error;
 * {@code AccessDeniedException} maps to {@link #ACCESS_DENIED}, everything else to {@link #IO_ERROR}.</li>
 * <li>iOS: {@code NSDictionary#writeToFile:atomically:} only returns a boolean, so failures are reported as
 * {@link #IO_ERROR}.</li>
 * <li>GWT: Local Storage quota errors ({@code QuotaExceededError}) map to {@link #DISK_FULL}, everything else to
 * {@link #IO_ERROR}. There is no structured disk-full signal on the desktop JVMs, so desktop backends never produce
 * {@link #DISK_FULL}.</li>
 * </ul>
 */
public enum PreferencesSaveResult {
	/** The preferences were persisted. */
	SUCCESS,
	/** The write failed because storage ran out of space or a storage quota was exceeded. */
	DISK_FULL,
	/** The write failed because access to the target location was denied. */
	ACCESS_DENIED,
	/** The write failed with an I/O error that was not classified more specifically. */
	IO_ERROR,
	/** The backend did not report a failure reason. */
	UNKNOWN;

	/** Best-effort classification of an exception thrown while persisting, walking the exception cause chain because backends
	 * commonly wrap the originating I/O exception.
	 *
	 * This base implementation uses only exception types available on all platforms including GWT. Backends with richer native
	 * information classify locally before invoking the callback.
	 *
	 * @param t the exception to classify, may be null
	 * @return {@link #ACCESS_DENIED} for a {@link SecurityException}, {@link #IO_ERROR} for any other {@link IOException},
	 *         {@link #UNKNOWN} otherwise */
	public static PreferencesSaveResult from (Throwable t) {
		while (t != null && t.getCause() != t) {
			if (t instanceof SecurityException) return ACCESS_DENIED;
			if (t instanceof IOException) return IO_ERROR;
			t = t.getCause();
		}
		return UNKNOWN;
	}
}
