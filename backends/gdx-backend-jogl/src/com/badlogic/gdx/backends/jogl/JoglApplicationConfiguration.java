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

package com.badlogic.gdx.backends.jogl;

import java.util.ArrayList;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.backends.jogl.JoglGraphics.JoglDisplayMode;

public class JoglApplicationConfiguration {
	/** If true, OpenAL will not be used. This means {@link Application#getAudio()} returns null and the gdx-joal.jar and OpenAL
	 * natives are not needed. */
	//FIXME not correctly handled
	public static boolean disableAudio;
	/** whether to use OpenGL ES 2.0 or not. default: false **/
	public boolean useGL20 = false;
	/** number of bits per color channel **/
	public int r = 8, g = 8, b = 8, a = 8;
	/** number of bits for depth and stencil buffer **/
	public int depth = 16, stencil = 0;
	/** number of samples for MSAA **/
	public int samples = 0;
	/** width & height of application **/
	public int width = 480, height = 320;
	/** fullscreen **/
	public boolean fullscreen = false;
	/** whether to enable vsync, can be changed at runtime via {@link Graphics#setVSync(boolean)} **/
	public boolean vSyncEnabled = true;
	/** title of application **/
	public String title = "Jogl Application";
	/** the audio device buffer size in samples **/
	public int audioDeviceBufferSize = 512;
	/** the audio device buffer count **/
	public int audioDeviceBufferCount = 9;

	/** Sets the r, g, b and a bits per channel based on the given {@link DisplayMode} and sets the fullscreen flag to true.
	 * @param mode */
	public void setFromDisplayMode (DisplayMode mode) {
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
			this.a = 0;
		}
		if (mode.bitsPerPixel == 32) {
			this.r = 8;
			this.g = 8;
			this.b = 8;
			this.a = 8;
		}
		this.fullscreen = true;
	}
}
