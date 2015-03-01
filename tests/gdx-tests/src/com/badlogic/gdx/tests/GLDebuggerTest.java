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

package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.debugging.GLDebugger;
import com.badlogic.gdx.graphics.debugging.GLDebuggerErrorListener;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class GLDebuggerTest extends GdxTest {
	SpriteBatch batch;
	BitmapFont font;

	String message = "GLDebugger is currently disabled";
	boolean makeGlError = false;
	final GLDebuggerErrorListener customListener = new GLDebuggerErrorListener() {
		@Override
		public void onError (int error) {
			if (error == GL20.GL_INVALID_VALUE) {
				message = "Correctly raised GL_INVALID_VALUE";
			} else {
				message = "Raised error but something unexpected: " + error;
			}
		}
	};

	@Override
	public void create () {
		batch = new SpriteBatch();
		font = new BitmapFont();
		Gdx.input.setInputProcessor(this);
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		batch.begin();

		if (makeGlError) {
			makeGlError = false;
			try {
				Gdx.gl.glClear(42); // Random invalid value, will raise GL_INVALID_VALUE (0x501, 1281)
			} catch (GdxRuntimeException glError) {
				if ("GLDebugger: Got gl error GL_INVALID_VALUE".equals(glError.getMessage())) {
					message = "Got expected exception.";
				} else {
					message = "Got GdxRuntimeException (correct) but with unexpected message: " + glError.getMessage();
				}
				Gdx.app.log("GLDebuggerTest", "Caught exception: ", glError);
			}
		}

		int x = 10;
		int y = Gdx.graphics.getHeight() - 10;
		y -= font.draw(batch, "e - Enable debugging\n" + "d - Disable debugging\n" + "l - Test log error listener\n"
			+ "t - Test throw error listener\n" + "c - Test custom listener\n\n" + "Expected error: GL_INVALID_VALUE (0x501, 1281)",
			x, y).height;
		y -= 10;
		font.draw(batch, message, x, y);
		batch.end();
	}

	@Override
	public boolean keyTyped (char character) {
		String DEBUGGER_DISABLED_MESSAGE = "Error will be detected after enabling the debugger";
		switch (character) {
		case 'e':
			GLDebugger.enable();
			message = "GLDebugger enabled (isEnabled(): " + GLDebugger.isEnabled() + ")";
			break;
		case 'd':
			GLDebugger.disable();
			message = "GLDebugger disabled (isEnabled(): " + GLDebugger.isEnabled() + ")";
			break;
		case 'l':
			GLDebugger.listener = GLDebuggerErrorListener.LOGGING_LISTENER;
			makeGlError = true;
			if (GLDebugger.isEnabled()) {
				message = "Log should contain info about error, which happened in glClear.";
			} else {
				message = DEBUGGER_DISABLED_MESSAGE;
			}
			break;
		case 't':
			GLDebugger.listener = GLDebuggerErrorListener.THROWING_LISTENER;
			makeGlError = true;
			if (GLDebugger.isEnabled()) {
				message = "This should be soon replaced with info about caught exception.";
			} else {
				message = DEBUGGER_DISABLED_MESSAGE;
			}
			break;
		case 'c':
			GLDebugger.listener = customListener;
			makeGlError = true;
			if (GLDebugger.isEnabled()) {
				message = "This should be soon replaced about info about success.";
			} else {
				message = DEBUGGER_DISABLED_MESSAGE;
			}
			break;
		default:
			return false;
		}
		return true;
	}

	@Override
	public void dispose () {
		batch.dispose();
		font.dispose();
	}
}
