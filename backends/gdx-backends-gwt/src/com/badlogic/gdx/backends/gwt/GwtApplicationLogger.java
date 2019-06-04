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

package com.badlogic.gdx.backends.gwt;

import com.badlogic.gdx.ApplicationLogger;
import com.badlogic.gdx.Gdx;
import com.google.gwt.user.client.ui.TextArea;

/**
 * Default implementation of {@link ApplicationLogger} for gwt
 */
public class GwtApplicationLogger implements ApplicationLogger {

	private TextArea log;

	public GwtApplicationLogger (TextArea log) {
		this.log = log;
	}

	@Override
	public void log (String tag, String message) {
		checkLogLabel();
		log.setText(log.getText() + "\n" + tag + ": " + message);
		log.setCursorPos(log.getText().length() - 1);
		System.out.println(tag + ": " + message);
	}

	@Override
	public void log (String tag, String message, Throwable exception) {
		checkLogLabel();
		log.setText(log.getText() + "\n" + tag + ": " + message + "\n" + getMessages(exception) + "\n");
		log.setCursorPos(log.getText().length() - 1);
		System.out.println(tag + ": " + message + "\n" + exception.getMessage());
		System.out.println(getStackTrace(exception));
	}

	@Override
	public void error (String tag, String message) {
		checkLogLabel();
		log.setText(log.getText() + "\n" + tag + ": " + message + "\n");
		log.setCursorPos(log.getText().length() - 1);
		System.err.println(tag + ": " + message);
	}

	@Override
	public void error (String tag, String message, Throwable exception) {
		checkLogLabel();
		log.setText(log.getText() + "\n" + tag + ": " + message + "\n" + getMessages(exception) + "\n");
		log.setCursorPos(log.getText().length() - 1);
		System.err.println(tag + ": " + message + "\n" + exception.getMessage() + "\n");
		System.out.println(getStackTrace(exception));
	}

	@Override
	public void debug (String tag, String message) {
		checkLogLabel();
		log.setText(log.getText() + "\n" + tag + ": " + message + "\n");
		log.setCursorPos(log.getText().length() - 1);
		System.out.println(tag + ": " + message + "\n");
	}

	@Override
	public void debug (String tag, String message, Throwable exception) {
		checkLogLabel();
		log.setText(log.getText() + "\n" + tag + ": " + message + "\n" + getMessages(exception) + "\n");
		log.setCursorPos(log.getText().length() - 1);
		System.out.println(tag + ": " + message + "\n" + exception.getMessage());
		System.out.println(getStackTrace(exception));
	}

	private void checkLogLabel () {
		if (log == null) {
			((GwtApplication)Gdx.app).log = log = new TextArea();

			// It's possible that log functions are called
			// before the app is initialized. E.g. SoundManager can call log functions before the app is initialized.
			// Since graphics is null, we're getting errors. The log size will be updated later, in case graphics was null
			if (Gdx.graphics != null) {
				log.setSize(Gdx.graphics.getWidth() + "px", "200px");
			} else {
				log.setSize("400px", "200px"); // Dummy value
			}

			log.setReadOnly(true);
			((GwtApplication)Gdx.app).getRootPanel().add(log);
		}
	}

	private String getMessages (Throwable e) {
		StringBuilder sb = new StringBuilder();
		while (e != null) {
			sb.append(e.getMessage() + "\n");
			e = e.getCause();
		}
		return sb.toString();
	}

	private String getStackTrace (Throwable e) {
		StringBuilder sb = new StringBuilder();
		for (StackTraceElement trace : e.getStackTrace()) {
			sb.append(trace.toString() + "\n");
		}
		return sb.toString();
	}

}
