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

package java.lang;

import com.google.gwt.core.client.GWT;

public class Thread {
	public static void sleep (long millis) throws InterruptedException {
		// noop emu
	}
	
	public static void setDefaultUncaughtExceptionHandler(final Thread.UncaughtExceptionHandler javaHandler) {
		GWT.setUncaughtExceptionHandler(new GWT.UncaughtExceptionHandler() {
			@Override
			public void onUncaughtException (Throwable e) {
				final Thread th = new Thread() {
					@Override
					public String toString() {
						return "The only thread";
					}
				};
				javaHandler.uncaughtException(th, e);
			}
		});
	}
	
	public static interface UncaughtExceptionHandler {
		void uncaughtException(Thread t, Throwable e);
	}
}
