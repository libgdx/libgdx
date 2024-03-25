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

package com.badlogic.gdx.backends.lwjgl3;

import java.io.PrintStream;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWVidMode.Buffer;

import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Graphics.Monitor;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics.Lwjgl3Monitor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.HdpiMode;
import com.badlogic.gdx.graphics.glutils.HdpiUtils;
import com.badlogic.gdx.math.GridPoint2;

public class Lwjgl3ApplicationConfiguration extends Lwjgl3WindowConfiguration {
	public static PrintStream errorStream = System.err;

	boolean disableAudio = false;

	/** The maximum number of threads to use for network requests. Default is {@link Integer#MAX_VALUE}. */
	int maxNetThreads = Integer.MAX_VALUE;

	int audioDeviceSimultaneousSources = 16;
	int audioDeviceBufferSize = 512;
	int audioDeviceBufferCount = 9;

	public enum GLEmulation {
		ANGLE_GLES20, GL20, GL30, GL31, GL32
	}

	GLEmulation glEmulation = GLEmulation.GL20;
	int gles30ContextMajorVersion = 3;
	int gles30ContextMinorVersion = 2;

	int r = 8, g = 8, b = 8, a = 8;
	int depth = 16, stencil = 0;
	int samples = 0;
	boolean transparentFramebuffer;

	int idleFPS = 60;
	int foregroundFPS = 0;

	String preferencesDirectory = ".prefs/";
	Files.FileType preferencesFileType = FileType.External;

	HdpiMode hdpiMode = HdpiMode.Logical;

	boolean debug = false;
	PrintStream debugStream = System.err;

	static Lwjgl3ApplicationConfiguration copy (Lwjgl3ApplicationConfiguration config) {
		Lwjgl3ApplicationConfiguration copy = new Lwjgl3ApplicationConfiguration();
		copy.set(config);
		return copy;
	}

	void set (Lwjgl3ApplicationConfiguration config) {
		super.setWindowConfiguration(config);
		disableAudio = config.disableAudio;
		audioDeviceSimultaneousSources = config.audioDeviceSimultaneousSources;
		audioDeviceBufferSize = config.audioDeviceBufferSize;
		audioDeviceBufferCount = config.audioDeviceBufferCount;
		glEmulation = config.glEmulation;
		gles30ContextMajorVersion = config.gles30ContextMajorVersion;
		gles30ContextMinorVersion = config.gles30ContextMinorVersion;
		r = config.r;
		g = config.g;
		b = config.b;
		a = config.a;
		depth = config.depth;
		stencil = config.stencil;
		samples = config.samples;
		transparentFramebuffer = config.transparentFramebuffer;
		idleFPS = config.idleFPS;
		foregroundFPS = config.foregroundFPS;
		preferencesDirectory = config.preferencesDirectory;
		preferencesFileType = config.preferencesFileType;
		hdpiMode = config.hdpiMode;
		debug = config.debug;
		debugStream = config.debugStream;
	}

	/** @param visibility whether the window will be visible on creation. (default true) */
	public void setInitialVisible (boolean visibility) {
		this.initialVisible = visibility;
	}

	/** Whether to disable audio or not. If set to true, the returned audio class instances like {@link Audio} or {@link Music}
	 * will be mock implementations. */
	public void disableAudio (boolean disableAudio) {
		this.disableAudio = disableAudio;
	}

	/** Sets the maximum number of threads to use for network requests. */
	public void setMaxNetThreads (int maxNetThreads) {
		this.maxNetThreads = maxNetThreads;
	}

	/** Sets the audio device configuration.
	 * 
	 * @param simultaneousSources the maximum number of sources that can be played simultaniously (default 16)
	 * @param bufferSize the audio device buffer size in samples (default 512)
	 * @param bufferCount the audio device buffer count (default 9) */
	public void setAudioConfig (int simultaneousSources, int bufferSize, int bufferCount) {
		this.audioDeviceSimultaneousSources = simultaneousSources;
		this.audioDeviceBufferSize = bufferSize;
		this.audioDeviceBufferCount = bufferCount;
	}

	/** Sets which OpenGL version to use to emulate OpenGL ES. If the given major/minor version is not supported, the backend falls
	 * back to OpenGL ES 2.0 emulation through OpenGL 2.0. The default parameters for major and minor should be 3 and 2
	 * respectively to be compatible with Mac OS X. Specifying major version 4 and minor version 2 will ensure that all OpenGL ES
	 * 3.0 features are supported. Note however that Mac OS X does only support 3.2.
	 * 
	 * @see <a href= "http://legacy.lwjgl.org/javadoc/org/lwjgl/opengl/ContextAttribs.html"> LWJGL OSX ContextAttribs note</a>
	 * 
	 * @param glVersion which OpenGL ES emulation version to use
	 * @param gles3MajorVersion OpenGL ES major version, use 3 as default
	 * @param gles3MinorVersion OpenGL ES minor version, use 2 as default */
	public void setOpenGLEmulation (GLEmulation glVersion, int gles3MajorVersion, int gles3MinorVersion) {
		this.glEmulation = glVersion;
		this.gles30ContextMajorVersion = gles3MajorVersion;
		this.gles30ContextMinorVersion = gles3MinorVersion;
	}

	/** Sets the bit depth of the color, depth and stencil buffer as well as multi-sampling.
	 * 
	 * @param r red bits (default 8)
	 * @param g green bits (default 8)
	 * @param b blue bits (default 8)
	 * @param a alpha bits (default 8)
	 * @param depth depth bits (default 16)
	 * @param stencil stencil bits (default 0)
	 * @param samples MSAA samples (default 0) */
	public void setBackBufferConfig (int r, int g, int b, int a, int depth, int stencil, int samples) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
		this.depth = depth;
		this.stencil = stencil;
		this.samples = samples;
	}

	/** Set transparent window hint. Results may vary on different OS and GPUs. Usage with the ANGLE backend is less consistent.
	 * @param transparentFramebuffer */
	public void setTransparentFramebuffer (boolean transparentFramebuffer) {
		this.transparentFramebuffer = transparentFramebuffer;
	}

	/** Sets the polling rate during idle time in non-continuous rendering mode. Must be positive. Default is 60. */
	public void setIdleFPS (int fps) {
		this.idleFPS = fps;
	}

	/** Sets the target framerate for the application. The CPU sleeps as needed. Must be positive. Use 0 to never sleep. Default is
	 * 0. */
	public void setForegroundFPS (int fps) {
		this.foregroundFPS = fps;
	}

	/** Sets the directory where {@link Preferences} will be stored, as well as the file type to be used to store them. Defaults to
	 * "$USER_HOME/.prefs/" and {@link FileType#External}. */
	public void setPreferencesConfig (String preferencesDirectory, Files.FileType preferencesFileType) {
		this.preferencesDirectory = preferencesDirectory;
		this.preferencesFileType = preferencesFileType;
	}

	/** Defines how HDPI monitors are handled. Operating systems may have a per-monitor HDPI scale setting. The operating system
	 * may report window width/height and mouse coordinates in a logical coordinate system at a lower resolution than the actual
	 * physical resolution. This setting allows you to specify whether you want to work in logical or raw pixel units. See
	 * {@link HdpiMode} for more information. Note that some OpenGL functions like {@link GL20#glViewport(int, int, int, int)} and
	 * {@link GL20#glScissor(int, int, int, int)} require raw pixel units. Use {@link HdpiUtils} to help with the conversion if
	 * HdpiMode is set to {@link HdpiMode#Logical}. Defaults to {@link HdpiMode#Logical}. */
	public void setHdpiMode (HdpiMode mode) {
		this.hdpiMode = mode;
	}

	/** Enables use of OpenGL debug message callbacks. If not supported by the core GL driver (since GL 4.3), this uses the
	 * KHR_debug, ARB_debug_output or AMD_debug_output extension if available. By default, debug messages with NOTIFICATION
	 * severity are disabled to avoid log spam.
	 *
	 * You can call with {@link System#err} to output to the "standard" error output stream.
	 *
	 * Use {@link Lwjgl3Application#setGLDebugMessageControl(Lwjgl3Application.GLDebugMessageSeverity, boolean)} to enable or
	 * disable other severity debug levels. */
	public void enableGLDebugOutput (boolean enable, PrintStream debugOutputStream) {
		debug = enable;
		debugStream = debugOutputStream;
	}

	/** @return the currently active {@link DisplayMode} of the primary monitor */
	public static DisplayMode getDisplayMode () {
		Lwjgl3Application.initializeGlfw();
		GLFWVidMode videoMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
		return new Lwjgl3Graphics.Lwjgl3DisplayMode(GLFW.glfwGetPrimaryMonitor(), videoMode.width(), videoMode.height(),
			videoMode.refreshRate(), videoMode.redBits() + videoMode.greenBits() + videoMode.blueBits());
	}

	/** @return the currently active {@link DisplayMode} of the given monitor */
	public static DisplayMode getDisplayMode (Monitor monitor) {
		Lwjgl3Application.initializeGlfw();
		GLFWVidMode videoMode = GLFW.glfwGetVideoMode(((Lwjgl3Monitor)monitor).monitorHandle);
		return new Lwjgl3Graphics.Lwjgl3DisplayMode(((Lwjgl3Monitor)monitor).monitorHandle, videoMode.width(), videoMode.height(),
			videoMode.refreshRate(), videoMode.redBits() + videoMode.greenBits() + videoMode.blueBits());
	}

	/** @return the available {@link DisplayMode}s of the primary monitor */
	public static DisplayMode[] getDisplayModes () {
		Lwjgl3Application.initializeGlfw();
		Buffer videoModes = GLFW.glfwGetVideoModes(GLFW.glfwGetPrimaryMonitor());
		DisplayMode[] result = new DisplayMode[videoModes.limit()];
		for (int i = 0; i < result.length; i++) {
			GLFWVidMode videoMode = videoModes.get(i);
			result[i] = new Lwjgl3Graphics.Lwjgl3DisplayMode(GLFW.glfwGetPrimaryMonitor(), videoMode.width(), videoMode.height(),
				videoMode.refreshRate(), videoMode.redBits() + videoMode.greenBits() + videoMode.blueBits());
		}
		return result;
	}

	/** @return the available {@link DisplayMode}s of the given {@link Monitor} */
	public static DisplayMode[] getDisplayModes (Monitor monitor) {
		Lwjgl3Application.initializeGlfw();
		Buffer videoModes = GLFW.glfwGetVideoModes(((Lwjgl3Monitor)monitor).monitorHandle);
		DisplayMode[] result = new DisplayMode[videoModes.limit()];
		for (int i = 0; i < result.length; i++) {
			GLFWVidMode videoMode = videoModes.get(i);
			result[i] = new Lwjgl3Graphics.Lwjgl3DisplayMode(((Lwjgl3Monitor)monitor).monitorHandle, videoMode.width(),
				videoMode.height(), videoMode.refreshRate(), videoMode.redBits() + videoMode.greenBits() + videoMode.blueBits());
		}
		return result;
	}

	/** @return the primary {@link Monitor} */
	public static Monitor getPrimaryMonitor () {
		Lwjgl3Application.initializeGlfw();
		return toLwjgl3Monitor(GLFW.glfwGetPrimaryMonitor());
	}

	/** @return the connected {@link Monitor}s */
	public static Monitor[] getMonitors () {
		Lwjgl3Application.initializeGlfw();
		PointerBuffer glfwMonitors = GLFW.glfwGetMonitors();
		Monitor[] monitors = new Monitor[glfwMonitors.limit()];
		for (int i = 0; i < glfwMonitors.limit(); i++) {
			monitors[i] = toLwjgl3Monitor(glfwMonitors.get(i));
		}
		return monitors;
	}

	static Lwjgl3Monitor toLwjgl3Monitor (long glfwMonitor) {
		IntBuffer tmp = BufferUtils.createIntBuffer(1);
		IntBuffer tmp2 = BufferUtils.createIntBuffer(1);
		GLFW.glfwGetMonitorPos(glfwMonitor, tmp, tmp2);
		int virtualX = tmp.get(0);
		int virtualY = tmp2.get(0);
		String name = GLFW.glfwGetMonitorName(glfwMonitor);
		return new Lwjgl3Monitor(glfwMonitor, virtualX, virtualY, name);
	}

	static GridPoint2 calculateCenteredWindowPosition (Lwjgl3Monitor monitor, int newWidth, int newHeight) {
		IntBuffer tmp = BufferUtils.createIntBuffer(1);
		IntBuffer tmp2 = BufferUtils.createIntBuffer(1);
		IntBuffer tmp3 = BufferUtils.createIntBuffer(1);
		IntBuffer tmp4 = BufferUtils.createIntBuffer(1);

		DisplayMode displayMode = getDisplayMode(monitor);

		GLFW.glfwGetMonitorWorkarea(monitor.monitorHandle, tmp, tmp2, tmp3, tmp4);
		int workareaWidth = tmp3.get(0);
		int workareaHeight = tmp4.get(0);

		int minX, minY, maxX, maxY;

		// If the new width is greater than the working area, we have to ignore stuff like the taskbar for centering and use the
		// whole monitor's size
		if (newWidth > workareaWidth) {
			minX = monitor.virtualX;
			maxX = displayMode.width;
		} else {
			minX = tmp.get(0);
			maxX = workareaWidth;
		}
		// The same is true for height
		if (newHeight > workareaHeight) {
			minY = monitor.virtualY;
			maxY = displayMode.height;
		} else {
			minY = tmp2.get(0);
			maxY = workareaHeight;
		}

		return new GridPoint2(Math.max(minX, minX + (maxX - newWidth) / 2), Math.max(minY, minY + (maxY - newHeight) / 2));
	}
}
