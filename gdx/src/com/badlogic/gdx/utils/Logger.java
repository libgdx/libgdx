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

package com.badlogic.gdx.utils;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;

/** Simple logger that uses the {@link Application} logging facilities to output messages. The log level set with
 * {@link Application#setLogLevel(int)} overrides the log level set here.
 * @author mzechner
 * @author Nathan Sweet */
public class Logger {
	static public final int NONE = 0;
	static public final int ERROR = 1;
	static public final int INFO = 2;
	static public final int DEBUG = 3;

	private final String tag;
	private int level;

	public Logger (String tag) {
		this(tag, ERROR);
	}

	public Logger (String tag, int level) {
		this.tag = tag;
		this.level = level;
	}

	public void debug (String message) {
		if (level >= DEBUG) Gdx.app.debug(tag, message);
	}

	public void debug (String message, Exception exception) {
		if (level >= DEBUG) Gdx.app.debug(tag, message, exception);
	}

	public void info (String message) {
		if (level >= INFO) Gdx.app.log(tag, message);
	}

	public void info (String message, Exception exception) {
		if (level >= INFO) Gdx.app.log(tag, message, exception);
	}

	public void error (String message) {
		if (level >= ERROR) Gdx.app.error(tag, message);
	}

	public void error (String message, Throwable exception) {
		if (level >= ERROR) Gdx.app.error(tag, message, exception);
	}

	/** Sets the log level. {@link #NONE} will mute all log output. {@link #ERROR} will only let error messages through.
	 * {@link #INFO} will let all non-debug messages through, and {@link #DEBUG} will let all messages through.
	 * @param level {@link #NONE}, {@link #ERROR}, {@link #INFO}, {@link #DEBUG}. */
	public void setLevel (int level) {
		this.level = level;
	}

	public int getLevel () {
		return level;
	}
}
