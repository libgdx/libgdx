
package com.badlogic.gdx.backends.lwjgl3;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;

import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class Lwjgl3Cursor implements Cursor {
	static final Array<Lwjgl3Cursor> cursors = new Array<Lwjgl3Cursor>();
	static final Map<SystemCursor, Long> systemCursors = new HashMap<SystemCursor, Long>();

	final Lwjgl3Window window;
	Pixmap pixmapCopy;
	GLFWImage glfwImage;
	final long cursor;

	Lwjgl3Cursor(Lwjgl3Window window, Pixmap pixmap, int xHotspot, int yHotspot) {
		this.window = window;
		if (pixmap.getFormat() != Pixmap.Format.RGBA8888) {
			throw new GdxRuntimeException("Cursor image pixmap is not in RGBA8888 format.");
		}

		if ((pixmap.getWidth() & (pixmap.getWidth() - 1)) != 0) {
			throw new GdxRuntimeException(
					"Cursor image pixmap width of " + pixmap.getWidth() + " is not a power-of-two greater than zero.");
		}

		if ((pixmap.getHeight() & (pixmap.getHeight() - 1)) != 0) {
			throw new GdxRuntimeException("Cursor image pixmap height of " + pixmap.getHeight()
					+ " is not a power-of-two greater than zero.");
		}

		if (xHotspot < 0 || xHotspot >= pixmap.getWidth()) {
			throw new GdxRuntimeException("xHotspot coordinate of " + xHotspot
					+ " is not within image width bounds: [0, " + pixmap.getWidth() + ").");
		}

		if (yHotspot < 0 || yHotspot >= pixmap.getHeight()) {
			throw new GdxRuntimeException("yHotspot coordinate of " + yHotspot
					+ " is not within image height bounds: [0, " + pixmap.getHeight() + ").");
		}

		this.pixmapCopy = new Pixmap(pixmap.getWidth(), pixmap.getHeight(), Pixmap.Format.RGBA8888);
		this.pixmapCopy.drawPixmap(pixmap, 0, 0);

		glfwImage = GLFWImage.malloc();
		glfwImage.width(pixmapCopy.getWidth());
		glfwImage.height(pixmapCopy.getHeight());
		glfwImage.pixels(pixmapCopy.getPixels());
		cursor = GLFW.glfwCreateCursor(glfwImage, xHotspot, yHotspot);
		cursors.add(this);
	}

	@Override
	public void dispose() {
		if (pixmapCopy == null) {
			throw new GdxRuntimeException("Cursor already disposed");
		}
		cursors.removeValue(this, true);
		pixmapCopy.dispose();
		pixmapCopy = null;
		glfwImage.free();
	}

	static void disposeAll() {
		while (cursors.size > 0) {
			cursors.removeIndex(0).dispose();
		}
		for (long systemCursor : systemCursors.values()) {
			GLFW.glfwDestroyCursor(systemCursor);
		}
	}

	static void setSystemCursor(long windowHandle, SystemCursor systemCursor) {
		Long glfwCursor = systemCursors.get(systemCursor);
		if (glfwCursor == null) {
			long handle = 0;
			if (systemCursor == SystemCursor.Arrow) {
				handle = GLFW.glfwCreateStandardCursor(GLFW.GLFW_ARROW_CURSOR);
			} else if (systemCursor == SystemCursor.Crosshair) {
				handle = GLFW.glfwCreateStandardCursor(GLFW.GLFW_CROSSHAIR_CURSOR);
			} else if (systemCursor == SystemCursor.Hand) {
				handle = GLFW.glfwCreateStandardCursor(GLFW.GLFW_HAND_CURSOR);
			} else if (systemCursor == SystemCursor.HorizontalResize) {
				handle = GLFW.glfwCreateStandardCursor(GLFW.GLFW_HRESIZE_CURSOR);
			} else if (systemCursor == SystemCursor.VerticalResize) {
				handle = GLFW.glfwCreateStandardCursor(GLFW.GLFW_VRESIZE_CURSOR);
			} else {
				throw new GdxRuntimeException("Unknown system cursor " + systemCursor);
			}

			if (handle == 0) {
				return;
			}
			glfwCursor = handle;
			systemCursors.put(systemCursor, glfwCursor);
		}
		GLFW.glfwSetCursor(windowHandle, glfwCursor);
	}
}