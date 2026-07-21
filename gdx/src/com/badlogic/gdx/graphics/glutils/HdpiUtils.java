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

package com.badlogic.gdx.graphics.glutils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.GL20;

/** To deal with HDPI monitors properly, use the glViewport and glScissor functions of this class instead of directly calling
 * OpenGL yourself. The logical coordinate system provided by the operating system may not have the same resolution as the actual
 * drawing surface to which OpenGL draws, also known as the backbuffer. This class will ensure, that you pass the correct values
 * to OpenGL for any function that expects backbuffer coordinates instead of logical coordinates.
 * 
 * @author badlogic */
public class HdpiUtils {
	private static HdpiMode mode = HdpiMode.Logical;

	/** Allows applications to override HDPI coordinate conversion for glViewport and glScissor calls.
	 *
	 * This function can be used to ignore the default behavior, for example when rendering a UI stage to an off-screen
	 * framebuffer:
	 *
	 * <pre>
	 * HdpiUtils.setMode(HdpiMode.Pixels);
	 * fb.begin();
	 * stage.draw();
	 * fb.end();
	 * HdpiUtils.setMode(HdpiMode.Logical);
	 * </pre>
	 *
	 * @param mode set to HdpiMode.Pixels to ignore HDPI conversion for glViewport and glScissor functions */
	public static void setMode (HdpiMode mode) {
		HdpiUtils.mode = mode;
	}

	/** Calls {@link GL20#glScissor(int, int, int, int)}, expecting the coordinates and sizes given in logical coordinates and
	 * automatically converts them to backbuffer coordinates, which may be bigger on HDPI screens. */
	public static void glScissor (int x, int y, int width, int height) {
		glScissor(Gdx.graphics, x, y, width, height);
	}

	public static void glScissor (Graphics graphics, int x, int y, int width, int height) {
		if (mode == HdpiMode.Logical && (graphics.getWidth() != graphics.getBackBufferWidth()
			|| graphics.getHeight() != graphics.getBackBufferHeight())) {
			graphics.getGL20().glScissor(toBackBufferX(graphics, x), toBackBufferY(graphics, y), toBackBufferX(graphics, width),
				toBackBufferY(graphics, height));
		} else {
			graphics.getGL20().glScissor(x, y, width, height);
		}
	}

	/** Calls {@link GL20#glViewport(int, int, int, int)}, expecting the coordinates and sizes given in logical coordinates and
	 * automatically converts them to backbuffer coordinates, which may be bigger on HDPI screens. */
	public static void glViewport (int x, int y, int width, int height) {
		glViewport(Gdx.graphics, x, y, width, height);
	}

	public static void glViewport (Graphics graphics, int x, int y, int width, int height) {
		if (mode == HdpiMode.Logical && (graphics.getWidth() != graphics.getBackBufferWidth()
			|| graphics.getHeight() != graphics.getBackBufferHeight())) {
			graphics.getGL20().glViewport(toBackBufferX(graphics, x), toBackBufferY(graphics, y), toBackBufferX(graphics, width),
				toBackBufferY(graphics, height));
		} else {
			graphics.getGL20().glViewport(x, y, width, height);
		}
	}

	/** Converts an x-coordinate given in backbuffer coordinates to logical screen coordinates. */
	public static int toLogicalX (int backBufferX) {
		return toLogicalX(Gdx.graphics, backBufferX);
	}

	public static int toLogicalX (Graphics graphics, int backBufferX) {
		return (int)(backBufferX * graphics.getWidth() / (float)graphics.getBackBufferWidth());
	}

	/** Converts an y-coordinate given in backbuffer coordinates to logical screen coordinates. */
	public static int toLogicalY (int backBufferY) {
		return toLogicalY(Gdx.graphics, backBufferY);
	}

	public static int toLogicalY (Graphics graphics, int backBufferY) {
		return (int)(backBufferY * graphics.getHeight() / (float)graphics.getBackBufferHeight());
	}

	/** Converts an x-coordinate given in logical screen coordinates to backbuffer coordinates. */
	public static int toBackBufferX (int logicalX) {
		return toBackBufferX(Gdx.graphics, logicalX);
	}

	public static int toBackBufferX (Graphics graphics, int logicalX) {
		return (int)(logicalX * graphics.getBackBufferWidth() / (float)graphics.getWidth());
	}

	/** Converts an y-coordinate given in logical screen coordinates to backbuffer coordinates. */
	public static int toBackBufferY (int logicalY) {
		return toBackBufferY(Gdx.graphics, logicalY);
	}

	public static int toBackBufferY (Graphics graphics, int logicalY) {
		return (int)(logicalY * graphics.getBackBufferHeight() / (float)graphics.getHeight());
	}
}
