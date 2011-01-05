/*
 * Copyright (c) 2008-2010, Matthias Mann
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution. * Neither the name of Matthias Mann nor
 * the names of its contributors may be used to endorse or promote products derived from this software without specific prior
 * written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.badlogic.gdx.twl.renderer;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Rect;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.renderer.CacheContext;
import de.matthiasmann.twl.renderer.DynamicImage;
import de.matthiasmann.twl.renderer.Font;
import de.matthiasmann.twl.renderer.FontParameter;
import de.matthiasmann.twl.renderer.LineRenderer;
import de.matthiasmann.twl.renderer.MouseCursor;
import de.matthiasmann.twl.renderer.Renderer;
import de.matthiasmann.twl.renderer.Texture;

/**
 * @author Nathan Sweet
 * @author Matthias Mann
 */
public class GdxRenderer implements Renderer {
	private int mouseX, mouseY;
	private GdxCacheContext cacheContext;
	private boolean hasScissor;
	private final TintStack tintStateRoot = new TintStack();
	private TintStack tintStack = tintStateRoot;
	private final Color tempColor = new Color(1, 1, 1, 1);
	private boolean rendering;
	private int width, height;
	final SpriteBatch batch;

	public GdxRenderer (SpriteBatch batch) {
		this.batch = batch;

		Widget root = new Widget() {
			protected void layout () {
				layoutChildrenFullInnerArea();
			}
		};
		root.setTheme("");
		new GUI(root, this, null);
	}

	public GdxCacheContext createNewCacheContext () {
		return new GdxCacheContext(this);
	}

	public GdxCacheContext getActiveCacheContext () {
		if (cacheContext == null) setActiveCacheContext(createNewCacheContext());
		return cacheContext;
	}

	public void setActiveCacheContext (CacheContext cacheContext) throws IllegalStateException {
		if (cacheContext == null) throw new IllegalArgumentException("cacheContext cannot be null.");
		if (!cacheContext.isValid()) throw new IllegalStateException("cacheContext is invalid.");
		if (!(cacheContext instanceof GdxCacheContext))
			throw new IllegalArgumentException("cacheContext is not from this renderer.");
		if (((GdxCacheContext)cacheContext).renderer != this)
			throw new IllegalArgumentException("cacheContext is not from this renderer.");
		this.cacheContext = (GdxCacheContext)cacheContext;
	}

	public long getTimeMillis () {
		return System.nanoTime() / 1000000;
	}

	public void startRenderering () {
		tintStack = tintStateRoot;
		batch.begin();
		rendering = true;
	}

	public void endRendering () {
		rendering = false;
		batch.end();
		if (hasScissor) {
			Gdx.gl.glDisable(GL10.GL_SCISSOR_TEST);
			hasScissor = false;
		}
	}

	public void setClipRect (Rect rect) {
		if (rendering) batch.flush();
		if (rect == null) {
			Gdx.gl.glDisable(GL10.GL_SCISSOR_TEST);
			hasScissor = false;
		} else {
			Gdx.gl.glScissor(rect.getX(), Gdx.graphics.getHeight() - rect.getBottom(), rect.getWidth(), rect.getHeight());
			if (!hasScissor) {
				Gdx.gl.glEnable(GL10.GL_SCISSOR_TEST);
				hasScissor = true;
			}
		}
	}

	public Font loadFont (URL baseUrl, Map<String, String> parameter, Collection<FontParameter> conditionalParameter)
		throws IOException {
		String fileName = parameter.get("filename");
		if (fileName == null) {
			throw new IllegalArgumentException("filename parameter required");
		}
		BitmapFont bitmapFont = getActiveCacheContext().loadBitmapFont(new URL(baseUrl, fileName));
		return new GdxFont(this, bitmapFont, parameter, conditionalParameter);
	}

	public Texture loadTexture (URL url, String formatStr, String filterStr) throws IOException {
		if (url == null) throw new IllegalArgumentException("url cannot be null.");
		return getActiveCacheContext().loadTexture(url);
	}

	public int getWidth () {
		return width;
	}

	public int getHeight () {
		return height;
	}

	public void setSize (int width, int height) {
		this.width = width;
		this.height = height;
		batch.getProjectionMatrix().setToOrtho(0, width, height, 0, 0, 1);
	}

	public LineRenderer getLineRenderer () {
		return null; // Unsupported.
	}

	public DynamicImage createDynamicImage (int width, int height) {
		return null; // Unsupported.
	}

	public void setCursor (MouseCursor cursor) {
	}

	public void setMousePosition (int mouseX, int mouseY) {
		this.mouseX = mouseX;
		this.mouseY = mouseY;
	}

	public void pushGlobalTintColor (float r, float g, float b, float a) {
		tintStack = tintStack.push(r, g, b, a);
	}

	public void popGlobalTintColor () {
		tintStack = tintStack.previous;
	}

	public Color getColor (de.matthiasmann.twl.Color color) {
		Color tempColor = this.tempColor;
		TintStack tintStack = this.tintStack;
		tempColor.r = tintStack.r * (color.getR() & 255);
		tempColor.g = tintStack.g * (color.getG() & 255);
		tempColor.b = tintStack.b * (color.getB() & 255);
		tempColor.a = tintStack.a * (color.getA() & 255);
		return tempColor;
	}

	public void dispose () {
		if (cacheContext != null) {
			cacheContext.destroy();
			cacheContext = null;
		}
		batch.dispose();
	}

	static private class TintStack extends Color {
		final TintStack previous;

		public TintStack () {
			super(1 / 255f, 1 / 255f, 1 / 255f, 1 / 255f);
			this.previous = this;
		}

		private TintStack (TintStack prev) {
			super(prev.r, prev.g, prev.b, prev.a);
			this.previous = prev;
		}

		public TintStack push (float r, float g, float b, float a) {
			TintStack next = new TintStack(this);
			next.r = this.r * r;
			next.g = this.g * g;
			next.b = this.b * b;
			next.a = this.a * a;
			return next;
		}
	}
}
