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

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Graphics.Monitor;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics.Lwjgl3Monitor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;

public class Lwjgl3ApplicationConfiguration {
	/**
	 * If true, OpenAL will not be used. This means
	 * {@link Application#getAudio()} returns null and the gdx-openal.jar and
	 * OpenAL natives are not needed.
	 */
	static public boolean disableAudio;

	/** whether to attempt use OpenGL ES 3.0. **/
	public boolean useGL30 = false;
	/**
	 * The OpenGL context major version (the part in front of the decimal point)
	 * used to emulate OpenGL ES 3.0, when the version is not supported it will
	 * fall back to OpenGL ES 2.0 emulation. Defaults to 3.2 (major=3, minor=2).
	 * Only used when {@link #useGL30} is true. OpenGL is fully compatible with
	 * OpenGL ES 3.0 since version 4.3, setting the context version to a lower
	 * value might cause some features not to function properly. OSX requires
	 * 3.2 though.
	 * 
	 * @see <a href=
	 *      "http://legacy.lwjgl.org/javadoc/org/lwjgl/opengl/ContextAttribs.html">
	 *      LWJGL OSX ContextAttribs note</a>
	 */
	public int gles30ContextMajorVersion = 3;
	/**
	 * The OpenGL context major version (the part after the decimal point) used
	 * to emulate OpenGL ES 3.0, when the version is not supported it will fall
	 * back to OpenGL ES 2.0 emulation. Defaults to 3.2 (major=3, minor=2). Only
	 * used when {@link #useGL30} is true. OpenGL is fully compatible with
	 * OpenGL ES 3.0 since version 4.3, setting the context version to a lower
	 * value might cause some features not to function properly. OSX requires
	 * 3.2 though.
	 * 
	 * @see <a href=
	 *      "http://legacy.lwjgl.org/javadoc/org/lwjgl/opengl/ContextAttribs.html">
	 *      LWJGL OSX ContextAttribs note</a>
	 */
	public int gles30ContextMinorVersion = 2;

	/** number of bits per color channel **/
	public int r = 8, g = 8, b = 8, a = 8;
	/** number of bits for depth and stencil buffer **/
	public int depth = 16, stencil = 0;
	/** number of samples for MSAA **/
	public int samples = 0;
	/** width & height of application window **/
	public int width = 640, height = 480;
	/** x & y of application window, -1 for center **/
	public int x = -1, y = -1;
	/** fullscreen **/
	public boolean fullscreen = false;
	/**
	 * whether to enable vsync, can be changed at runtime via
	 * {@link Graphics#setVSync(boolean)}
	 **/
	public boolean vSyncEnabled = true;
	/** title of application **/
	public String title;
	/** whether the window is resizable **/
	public boolean resizable = true;
	/** the maximum number of sources that can be played simultaneously */
	public int audioDeviceSimultaneousSources = 16;
	/** the audio device buffer size in samples **/
	public int audioDeviceBufferSize = 512;
	/** the audio device buffer count **/
	public int audioDeviceBufferCount = 9;
	public Color initialBackgroundColor = Color.BLACK;
	/** Preferences directory on the desktop. Default is ".prefs/". */
	public String preferencesDirectory = ".prefs/";
	/** Preferences file type on the desktop. Default is FileType.External */
	public Files.FileType preferencesFileType = FileType.External;
	/**
	 * Callback used when trying to create a display, can handle failures,
	 * default value is null (disabled)
	 */
	// public LwjglGraphics.SetDisplayModeCallback setDisplayModeCallback;
	/**
	 * Enable HDPI mode. {@link Graphics#getWidth()} and
	 * {@link Graphics#getHeight()} will report the physical pixel size instead
	 * of the logical pixel size. Mouse coordinates will also be given in
	 * physical pixels instead of logical pixels.
	 */
	public boolean useHDPI = false;
	Array<String> iconPaths = new Array<String>();
	Array<FileType> iconFileTypes = new Array<FileType>();

	/**
	 * Adds a window icon. Icons are tried in the order added, the first one
	 * that works is used. Typically three icons should be provided: 128x128
	 * (for Mac), 32x32 (for Windows and Linux), and 16x16 (for Windows).
	 */
	public void addIcon(String path, FileType fileType) {
		iconPaths.add(path);
		iconFileTypes.add(fileType);
	}

	/**
	 * Sets the r, g, b and a bits per channel based on the given
	 * {@link DisplayMode} and sets the fullscreen flag to true.
	 * 
	 * @param mode
	 */
	public void setFromDisplayMode(DisplayMode mode) {
		this.width = mode.width;
		this.height = mode.height;
		if (mode.bitsPerPixel == 16) {
			this.r = 5;
			this.g = 6;
			this.b = 5;
			this.a = 0;
		}
		if (mode.bitsPerPixel == 24) {
			this.r = 8;
			this.g = 8;
			this.b = 8;
			this.a = 8;
		}
		if (mode.bitsPerPixel == 32) {
			this.r = 8;
			this.g = 8;
			this.b = 8;
			this.a = 8;
		}
		this.fullscreen = true;
	}

	public static DisplayMode getsDisplayMode() {
		Lwjgl3Application.initializeGlfw();
		GLFWVidMode videoMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
		return new Lwjgl3Graphics.Lwjgl3DisplayMode(videoMode.width(), videoMode.height(), videoMode.refreshRate(),
				videoMode.redBits() + videoMode.greenBits() + videoMode.blueBits());
	}

	public static DisplayMode[] getDisplayModes() {
		Lwjgl3Application.initializeGlfw();
		Buffer videoModes = GLFW.glfwGetVideoModes(GLFW.glfwGetPrimaryMonitor());
		DisplayMode[] result = new DisplayMode[videoModes.limit()];
		for (int i = 0; i < result.length; i++) {
			GLFWVidMode videoMode = videoModes.get(i);
			result[i] = new Lwjgl3Graphics.Lwjgl3DisplayMode(videoMode.width(), videoMode.height(),
					videoMode.refreshRate(), videoMode.redBits() + videoMode.greenBits() + videoMode.blueBits());
		}
		return result;
	}
	
	public static DisplayMode[] getDisplayModes(Monitor monitor) {
		Lwjgl3Application.initializeGlfw();
		Buffer videoModes = GLFW.glfwGetVideoModes(GLFW.glfwGetPrimaryMonitor());
		DisplayMode[] result = new DisplayMode[videoModes.limit()];
		for (int i = 0; i < result.length; i++) {
			GLFWVidMode videoMode = videoModes.get(i);
			result[i] = new Lwjgl3Graphics.Lwjgl3DisplayMode(videoMode.width(), videoMode.height(),
					videoMode.refreshRate(), videoMode.redBits() + videoMode.greenBits() + videoMode.blueBits());
		}
		return result;
	}
	
	public static Monitor getPrimaryMonitor() {
		Lwjgl3Application.initializeGlfw();
		return toLwjgl3Monitor(GLFW.glfwGetPrimaryMonitor());
	}
	
	public static Monitor[] getMonitors() {
		PointerBuffer glfwMonitors = GLFW.glfwGetMonitors();
		Monitor[] monitors = new Monitor[glfwMonitors.limit()];
		for(int i = 0; i < glfwMonitors.limit(); i++) {
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
		return new Lwjgl3Monitor(virtualX, virtualY, name);
	}
}
