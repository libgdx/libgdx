/*******************************************************************************
 * Copyright 2022 See AUTHORS file.
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

package com.badlogic.gdx.tests.gles32;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.charset.Charset;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL32;
import com.badlogic.gdx.graphics.GL32.DebugProc;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.tests.utils.GdxTestConfig;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.ScreenUtils;

/** see https://www.khronos.org/opengl/wiki/Debug_Output
 * 
 * @author mgsx */
@GdxTestConfig(requireGL32 = true)
public class GL32DebugControlTest extends GdxTest {
	/** Proto utility class for message debug control. */
	public static class GLDebug {

		/** can only be used when {@link #setCallback(DebugProc)} is set to null. */
		private static final DebugReader debugReader = new DebugReader();

		/** callback debug message version */
		public static final DebugProc loggingCallback = new DebugProc() {
			@Override
			public void onMessage (int source, int type, int id, int severity, String message) {
				GLDebug.log(source, type, id, severity, message);
			}
		};

		private static final int maxMessageLength;
		static {
			IntBuffer buf = BufferUtils.newIntBuffer(1);
			Gdx.gl.glGetIntegerv(GL32.GL_MAX_DEBUG_MESSAGE_LENGTH, buf);
			maxMessageLength = buf.get();

			// default options
			Gdx.gl.glEnable(GL32.GL_DEBUG_OUTPUT_SYNCHRONOUS);
			setCallback(loggingCallback);
		}

		/** polling debug message version */
		private static class DebugReader {

			int n = 1024;
			private IntBuffer sources;
			private IntBuffer types;
			private IntBuffer ids;
			private IntBuffer severities;
			private IntBuffer lengths;
			private ByteBuffer messageLog;

			public DebugReader () {
				sources = BufferUtils.newIntBuffer(n);
				types = BufferUtils.newIntBuffer(n);
				ids = BufferUtils.newIntBuffer(n);
				severities = BufferUtils.newIntBuffer(n);
				lengths = BufferUtils.newIntBuffer(n);
				messageLog = BufferUtils.newByteBuffer(maxMessageLength);
			}

			public void fetchAndLog () {
				for (;;) {
					int count = Gdx.gl32.glGetDebugMessageLog(n, sources, types, ids, severities, lengths, messageLog);
					if (count == 0) break;
					for (int i = 0; i < count; i++) {
						int source = sources.get();
						int type = types.get();
						int id = ids.get();
						int severity = severities.get();
						int length = lengths.get();
						byte[] bytes = new byte[length];
						messageLog.get(bytes);
						messageLog.rewind();
						String message = new String(bytes, Charset.forName("UTF8"));
						log(source, type, id, severity, message);
					}
					sources.rewind();
					types.rewind();
					ids.rewind();
					severities.rewind();
					lengths.rewind();
					if (count < n) break;
				}

			}
		}

		public static void enableOverall (boolean enabled) {
			if (enabled) {
				Gdx.gl.glEnable(GL32.GL_DEBUG_OUTPUT);
			} else {
				Gdx.gl.glDisable(GL32.GL_DEBUG_OUTPUT);
			}
		}

		/** set the debug messages callback.
		 * @param callback when null, messages can be logged using {@link #logPendingMessages()} but some messages may be lost. */
		public static void setCallback (DebugProc callback) {
			Gdx.gl32.glDebugMessageCallback(callback);
		}

		public static void insertApplicationMessage (int type, int id, int severity, String message) {
			insertMessage(GL32.GL_DEBUG_SOURCE_APPLICATION, type, id, severity, message);
		}

		public static void insertThirdPartyMessage (int type, int id, int severity, String message) {
			insertMessage(GL32.GL_DEBUG_SOURCE_THIRD_PARTY, type, id, severity, message);
		}

		private static void insertMessage (int source, int type, int id, int severity, String message) {
			if (message.length() + 1 > maxMessageLength) {
				Gdx.app.error("GLDebug", "user message too long, it will be truncated");
				message = message.substring(0, maxMessageLength - 1);
			}
			Gdx.gl32.glDebugMessageInsert(source, type, id, severity, message);
		}

		public static void enableAll (boolean enabled) {
			enable(enabled, GL32.GL_DONT_CARE, GL32.GL_DONT_CARE, GL32.GL_DONT_CARE);
		}

		public static void enable (boolean enabled, int source, int type, int severity) {
			Gdx.gl32.glDebugMessageControl(source, type, severity, null, enabled);
		}

		public static void enableIDs (boolean enabled, int source, int type, int... ids) {
			IntBuffer idsBuffer = BufferUtils.newIntBuffer(1);
			idsBuffer.put(ids);
			idsBuffer.flip();

			Gdx.gl32.glDebugMessageControl(source, type, GL32.GL_DONT_CARE, idsBuffer, enabled);
		}

		public static void log (int source, int type, int id, int severity, String message) {
			String strSource;
			if (source == GL32.GL_DEBUG_SOURCE_APPLICATION) {
				strSource = "APPLICATION";
			} else if (source == GL32.GL_DEBUG_SOURCE_THIRD_PARTY) {
				strSource = "THIRD_PARTY";
			} else if (source == GL32.GL_DEBUG_SOURCE_API) {
				strSource = "API";
			} else if (source == GL32.GL_DEBUG_SOURCE_SHADER_COMPILER) {
				strSource = "SHADER_COMPILER";
			} else if (source == GL32.GL_DEBUG_SOURCE_WINDOW_SYSTEM) {
				strSource = "WINDOW_SYSTEM";
			} else if (source == GL32.GL_DEBUG_SOURCE_OTHER) {
				strSource = "OTHER";
			} else {
				strSource = "UNKNOWN";
			}

			String strType;
			if (type == GL32.GL_DEBUG_TYPE_DEPRECATED_BEHAVIOR) {
				strType = "DEPRECATED_BEHAVIOR";
			} else if (type == GL32.GL_DEBUG_TYPE_ERROR) {
				strType = "ERROR";
			} else if (type == GL32.GL_DEBUG_TYPE_MARKER) {
				strType = "MARKER";
			} else if (type == GL32.GL_DEBUG_TYPE_OTHER) {
				strType = "OTHER";
			} else if (type == GL32.GL_DEBUG_TYPE_PERFORMANCE) {
				strType = "PERFORMANCE";
			} else if (type == GL32.GL_DEBUG_TYPE_POP_GROUP) {
				strType = "POP_GROUP";
			} else if (type == GL32.GL_DEBUG_TYPE_PORTABILITY) {
				strType = "PORTABILITY";
			} else if (type == GL32.GL_DEBUG_TYPE_PUSH_GROUP) {
				strType = "PUSH_GROUP";
			} else if (type == GL32.GL_DEBUG_TYPE_UNDEFINED_BEHAVIOR) {
				strType = "UNDEFINED_BEHAVIOR";
			} else {
				strType = "UNKNOWN";
			}

			String strSeverity;
			if (severity == GL32.GL_DEBUG_SEVERITY_HIGH) {
				strSeverity = "HIGH";
			} else if (severity == GL32.GL_DEBUG_SEVERITY_LOW) {
				strSeverity = "LOW";
			} else if (severity == GL32.GL_DEBUG_SEVERITY_MEDIUM) {
				strSeverity = "MEDIUM";
			} else if (severity == GL32.GL_DEBUG_SEVERITY_NOTIFICATION) {
				strSeverity = "NOTIFICATION";
			} else {
				strSeverity = "UNKNOWN";
			}

			Gdx.app.log("GLDebug",
				"source:" + strSource + " type:" + strType + " id:" + id + " severity:" + strSeverity + " message:" + message);
		}

		public static void logPendingMessages () {
			debugReader.fetchAndLog();
		}
	}

	private SpriteBatch batch;
	private Texture texture;
	private boolean useCallback = true;
	private boolean enableNotifications = false;

	public void create () {

		GLDebug.enableOverall(true);
		GLDebug.setCallback(useCallback ? GLDebug.loggingCallback : null);

		GLDebug.enableAll(true);

		// disable specific message
		GLDebug.enableIDs(false, GL32.GL_DEBUG_SOURCE_API, GL32.GL_DEBUG_TYPE_OTHER, 131185);

		// insert a user message
		GLDebug.insertApplicationMessage(GL32.GL_DEBUG_TYPE_OTHER, 1234, GL32.GL_DEBUG_SEVERITY_NOTIFICATION, "application start");

		// generate a fake error (once filtered, once reported)
		Gdx.app.log("GDX", "error report disabled");
		GLDebug.enableIDs(false, GL32.GL_DEBUG_SOURCE_API, GL32.GL_DEBUG_TYPE_ERROR, GL20.GL_INVALID_OPERATION);
		Gdx.gl.glUseProgram(0);
		Gdx.gl.glUniform1f(0, 0f);

		Gdx.app.log("GDX", "error report enabled");
		GLDebug.enableIDs(true, GL32.GL_DEBUG_SOURCE_API, GL32.GL_DEBUG_TYPE_ERROR, GL20.GL_INVALID_OPERATION);
		Gdx.gl.glUseProgram(0);
		Gdx.gl.glUniform1f(0, 0f);

		// reset (enable all but notifications)
		GLDebug.enableAll(true);
		GLDebug.enable(enableNotifications, GL32.GL_DONT_CARE, GL32.GL_DONT_CARE, GL32.GL_DEBUG_SEVERITY_NOTIFICATION);

		texture = new Texture(Gdx.files.internal("data/badlogic.jpg"));
		batch = new SpriteBatch();
		batch.getProjectionMatrix().setToOrtho2D(0, 0, 1, 1);

		// Labeling
		Gdx.gl32.glObjectLabel(GL20.GL_TEXTURE, texture.getTextureObjectHandle(), "myTexture");
		String label = Gdx.gl32.glGetObjectLabel(GL20.GL_TEXTURE, texture.getTextureObjectHandle());
		Gdx.app.log("Debug test", "texture handle " + texture.getTextureObjectHandle() + ": " + label);

		// generate fake error
		texture.bind();
		Gdx.gl.glTexParameteri(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_WRAP_S, -1);
		Gdx.gl.glTexParameteri(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_WRAP_S, GL20.GL_CLAMP_TO_EDGE);
	}

	@Override
	public void dispose () {
		GLDebug.enableOverall(false);
		texture.dispose();
		batch.dispose();
	}

	@Override
	public void render () {

		// example: enable/disable notifications
		if (Gdx.input.justTouched()) {
			enableNotifications = !enableNotifications;
			GLDebug.enable(enableNotifications, GL32.GL_DONT_CARE, GL32.GL_DONT_CARE, GL32.GL_DEBUG_SEVERITY_NOTIFICATION);
		}

		ScreenUtils.clear(Color.CLEAR);

		Gdx.gl32.glPushDebugGroup(GL32.GL_DEBUG_SOURCE_APPLICATION, 57, "sprite batch drawing 1");
		batch.begin();
		batch.draw(texture, 0, 0, 1, 1);
		batch.end();
		Gdx.gl32.glPopDebugGroup();

		Gdx.gl32.glPushDebugGroup(GL32.GL_DEBUG_SOURCE_APPLICATION, 57, "sprite batch drawing 2");
		batch.begin();
		batch.draw(texture, 0, 0, 1, 1);
		batch.end();
		Gdx.gl32.glPopDebugGroup();

		if (!useCallback) {
			GLDebug.logPendingMessages();
		}
	}
}
