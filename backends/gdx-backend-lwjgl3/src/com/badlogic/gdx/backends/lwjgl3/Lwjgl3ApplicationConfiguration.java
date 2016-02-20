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

import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWVidMode.Buffer;
import org.lwjgl.opengl.GL;

import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Graphics.Monitor;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics.Lwjgl3DisplayMode;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics.Lwjgl3Monitor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.HdpiUtils;

public class Lwjgl3ApplicationConfiguration {
	boolean disableAudio = false;
	int audioDeviceSimultaneousSources = 16;
	int audioDeviceBufferSize = 512;
	int audioDeviceBufferCount = 9;

	boolean useGL30 = false;
	int gles30ContextMajorVersion = 3;
	int gles30ContextMinorVersion = 2;

	int r = 8, g = 8, b = 8, a = 8;
	int depth = 16, stencil = 0;
	int samples = 0;

	int windowX = -1;
	int windowY = -1;
	int windowWidth = 640;
	int windowHeight = 480;
	boolean windowResizable = true;
	boolean windowDecorated = true;
	Lwjgl3WindowListener windowListener;
	Lwjgl3DisplayMode fullscreenMode;

	boolean vSyncEnabled = true;
	String title = "";
	Color initialBackgroundColor = Color.BLACK;

	String preferencesDirectory = ".prefs/";
	Files.FileType preferencesFileType = FileType.External;

	HdpiMode hdpiMode = HdpiMode.Logical;
	
	static Lwjgl3ApplicationConfiguration copy(Lwjgl3ApplicationConfiguration config) {
		Lwjgl3ApplicationConfiguration copy = new Lwjgl3ApplicationConfiguration();
		copy.disableAudio = config.disableAudio;
		copy.audioDeviceSimultaneousSources = config.audioDeviceSimultaneousSources;
		copy.audioDeviceBufferSize = config.audioDeviceBufferSize;
		copy.audioDeviceBufferCount = config.audioDeviceBufferCount;
		copy.useGL30 = config.useGL30;
		copy.gles30ContextMajorVersion = config.gles30ContextMajorVersion;
		copy.gles30ContextMinorVersion = config.gles30ContextMinorVersion;
		copy.r = config.r;
		copy.g = config.g;
		copy.b = config.b;
		copy.a = config.a;
		copy.depth = config.depth;
		copy.stencil = config.stencil;
		copy.samples = config.samples;
		copy.windowX = config.windowX;
		copy.windowY = config.windowY;
		copy.windowWidth = config.windowWidth;
		copy.windowHeight = config.windowHeight;
		copy.windowResizable = config.windowResizable;
		copy.windowDecorated = config.windowDecorated;
		copy.windowListener = config.windowListener;
		copy.fullscreenMode = config.fullscreenMode;
		copy.vSyncEnabled = config.vSyncEnabled;
		copy.title = config.title;
		copy.initialBackgroundColor = config.initialBackgroundColor;
		copy.preferencesDirectory = config.preferencesDirectory;
		copy.preferencesFileType = config.preferencesFileType;
		copy.hdpiMode = config.hdpiMode;
		return copy;
	}
	

	/**
	 * Whether to disable audio or not. If set to false, the returned audio
	 * class instances like {@link Audio} or {@link Music} will be mock
	 * implementations.
	 */
	public void disableAudio(boolean disableAudio) {
		this.disableAudio = disableAudio;
	}

	/**
	 * Sets the audio device configuration.
	 * 
	 * @param simultaniousSources
	 *            the maximum number of sources that can be played
	 *            simultaniously (default 16)
	 * @param bufferSize
	 *            the audio device buffer size in samples (default 512)
	 * @param bufferCount
	 *            the audio device buffer count (default 9)
	 */
	public void setAudioConfig(int simultaniousSources, int bufferSize, int bufferCount) {
		this.audioDeviceSimultaneousSources = simultaniousSources;
		this.audioDeviceBufferSize = bufferSize;
		this.audioDeviceBufferCount = bufferCount;
	}

	/**
	 * Sets whether to use OpenGL ES 3.0 emulation. If the given major/minor
	 * version is not supported, the backend falls back to OpenGL ES 2.0
	 * emulation. The default parameters for major and minor should be 3 and 2
	 * respectively to be compatible with Mac OS X. Specifying major version 4
	 * and minor version 2 will ensure that all OpenGL ES 3.0 features are
	 * supported. Note however that Mac OS X does only support 3.2.
	 * 
	 * @see <a href=
	 *      "http://legacy.lwjgl.org/javadoc/org/lwjgl/opengl/ContextAttribs.html">
	 *      LWJGL OSX ContextAttribs note
	 * 
	 * @param useGL30
	 *            whether to use OpenGL ES 3.0
	 * @param gles3MajorVersion
	 *            OpenGL ES major version, use 3 as default
	 * @param gles3MinorVersion
	 *            OpenGL ES minor version, use 2 as default
	 */
	public void useOpenGL3(boolean useGL30, int gles3MajorVersion, int gles3MinorVersion) {
		this.useGL30 = useGL30;
		this.gles30ContextMajorVersion = gles3MajorVersion;
		this.gles30ContextMinorVersion = gles3MinorVersion;
	}

	/**
	 * Sets the bit depth of the color, depth and stencil buffer as well as
	 * multi-sampling.
	 * 
	 * @param r
	 *            red bits (default 8)
	 * @param g
	 *            green bits (default 8)
	 * @param b
	 *            blue bits (default 8)
	 * @param a
	 *            alpha bits (default 8)
	 * @param depth
	 *            depth bits (default 16)
	 * @param stencil
	 *            stencil bits (default 0)
	 * @param samples
	 *            MSAA samples (default 0)
	 */
	public void setBackBufferConfig(int r, int g, int b, int a, int depth, int stencil, int samples) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
		this.depth = depth;
		this.stencil = stencil;
		this.samples = samples;
	}

	/**
	 * Sets the app to use windowed mode.
	 * 
	 * @param width
	 *            the width of the window (default 640)
	 * @param height
	 *            the height of the window (default 480)
	 */
	public void setWindowedMode(int width, int height) {
		this.windowWidth = width;
		this.windowHeight = height;		
	}
	
	/** 
	 * @param resizable whether the windowed mode window is resizable (default true)
	 */
	public void setResizable(boolean resizable) {
		this.windowResizable = resizable;
	}
	
	/**
	 * @param decorated whether the windowed mode window is decorated, i.e. displaying the title bars (default true)
	 */
	public void setDecorated(boolean decorated) {
		this.windowDecorated = decorated;
	}
	
	/**
	 * Sets the position of the window in windowed mode on the
	 * primary monitor. Default -1 for booth coordinates for centered.
	 */
	public void setWindowPosition(int x, int y) {
		windowX = x;
		windowY = y;
	}
	
	/**
	 * Sets the {@link Lwjgl3WindowListener} which will be informed about
	 * iconficiation, focus loss and window close events.
	 */
	public void setWindowListener(Lwjgl3WindowListener windowListener) {
		this.windowListener = windowListener;
	}

	/**
	 * Sets the app to use fullscreen mode. Use the static methods like
	 * {@link #getDisplayMode()} on this class to enumerate connected monitors
	 * and their fullscreen display modes.
	 */
	public void setFullscreenMode(DisplayMode mode) {
		this.fullscreenMode = (Lwjgl3DisplayMode)mode;
	}

	/**
	 * Sets whether to use vsync. This setting can be changed anytime at runtime
	 * via {@link Graphics#setVSync(boolean)}.
	 */
	public void useVsync(boolean vsync) {
		this.vSyncEnabled = vsync;
	}

	/**
	 * Sets the window title. Defaults to empty string.
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Sets the initial background color. Defaults to black.
	 */
	public void setInitialBackgroundColor(Color color) {
		initialBackgroundColor = color;
	}

	/**
	 * Sets the directory where {@link Preferences} will be stored, as well as
	 * the file type to be used to store them. Defaults to "$USER_HOME/.prefs/"
	 * and {@link FileType#External}.
	 */
	public void setPreferencesConfig(String preferencesDirectory, Files.FileType preferencesFileType) {
		this.preferencesDirectory = preferencesDirectory;
		this.preferencesFileType = preferencesFileType;
	}

	/**
	 * Defines how HDPI monitors are handled. Operating systems may have a
	 * per-monitor HDPI scale setting. The operating system may report window
	 * width/height and mouse coordinates in a logical coordinate system at a
	 * lower resolution than the actual physical resolution. This setting allows
	 * you to specify whether you want to work in logical or raw pixel units.
	 * See {@link HdpiMode} for more information. Note that some OpenGL
	 * functions like {@link GL#glViewport()} and {@link GL#glScissor()} require
	 * raw pixel units. Use {@link HdpiUtils} to help with the conversion if
	 * HdpiMode is set to {@link HdpiMode#Logical}. Defaults to {@link HdpiMode#Logical}.
	 */
	public void setHdpiMode(HdpiMode mode) {
		this.hdpiMode = mode;
	}	

	/**
	 * @return the currently active {@link DisplayMode} of the primary monitor
	 */
	public static DisplayMode getDisplayMode() {
		Lwjgl3Application.initializeGlfw();
		GLFWVidMode videoMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
		return new Lwjgl3Graphics.Lwjgl3DisplayMode(GLFW.glfwGetPrimaryMonitor(), videoMode.width(), videoMode.height(), videoMode.refreshRate(),
				videoMode.redBits() + videoMode.greenBits() + videoMode.blueBits());
	}
	
	/**
	 * @return the currently active {@link DisplayMode} of the given monitor
	 */
	public static DisplayMode getDisplayMode(Monitor monitor) {
		Lwjgl3Application.initializeGlfw();
		GLFWVidMode videoMode = GLFW.glfwGetVideoMode(((Lwjgl3Monitor)monitor).monitorHandle);
		return new Lwjgl3Graphics.Lwjgl3DisplayMode(((Lwjgl3Monitor)monitor).monitorHandle, videoMode.width(), videoMode.height(), videoMode.refreshRate(),
				videoMode.redBits() + videoMode.greenBits() + videoMode.blueBits());
	}

	/**
	 * @return the available {@link DisplayMode}s of the primary monitor
	 */
	public static DisplayMode[] getDisplayModes() {
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

	/**
	 * @return the available {@link DisplayMode}s of the given {@link Monitor}
	 */
	public static DisplayMode[] getDisplayModes(Monitor monitor) {
		Lwjgl3Application.initializeGlfw();
		Buffer videoModes = GLFW.glfwGetVideoModes(((Lwjgl3Monitor)monitor).monitorHandle);
		DisplayMode[] result = new DisplayMode[videoModes.limit()];
		for (int i = 0; i < result.length; i++) {
			GLFWVidMode videoMode = videoModes.get(i);
			result[i] = new Lwjgl3Graphics.Lwjgl3DisplayMode(((Lwjgl3Monitor)monitor).monitorHandle, videoMode.width(), videoMode.height(),
					videoMode.refreshRate(), videoMode.redBits() + videoMode.greenBits() + videoMode.blueBits());
		}
		return result;
	}

	/**
	 * @return the primary {@link Monitor}
	 */
	public static Monitor getPrimaryMonitor() {
		Lwjgl3Application.initializeGlfw();
		return toLwjgl3Monitor(GLFW.glfwGetPrimaryMonitor());
	}

	/**
	 * @return the conntected {@link Monitor}s
	 */
	public static Monitor[] getMonitors() {
		Lwjgl3Application.initializeGlfw();
		PointerBuffer glfwMonitors = GLFW.glfwGetMonitors();
		Monitor[] monitors = new Monitor[glfwMonitors.limit()];
		for (int i = 0; i < glfwMonitors.limit(); i++) {
			monitors[i] = toLwjgl3Monitor(glfwMonitors.get(i));
		}
		return monitors;
	}

	static Lwjgl3Monitor toLwjgl3Monitor(long glfwMonitor) {
		IntBuffer tmp = BufferUtils.createIntBuffer(1);
		IntBuffer tmp2 = BufferUtils.createIntBuffer(1);
		GLFW.glfwGetMonitorPos(glfwMonitor, tmp, tmp2);
		int virtualX = tmp.get(0);
		int virtualY = tmp2.get(0);
		String name = GLFW.glfwGetMonitorName(glfwMonitor);
		return new Lwjgl3Monitor(glfwMonitor, virtualX, virtualY, name);
	}

	public static enum HdpiMode {
		/**
		 * mouse coordinates, {@link Graphics#getWidth()} and
		 * {@link Graphics#getHeight()} will return logical coordinates
		 * according to the system defined HDPI scaling. Rendering will be
		 * performed to a backbuffer at raw resolution. Use {@link HdpiUtils}
		 * when calling {@link GL20#glScissor} or {@link GL20#glViewport} which
		 * expect raw coordinates.
		 */
		Logical,

		/**
		 * Mouse coordinates, {@link Graphics#getWidth()} and
		 * {@link Graphics#getHeight()} will return raw pixel coordinates
		 * irrespective of the system defined HDPI scaling.
		 */
		Pixels
	}		
}
