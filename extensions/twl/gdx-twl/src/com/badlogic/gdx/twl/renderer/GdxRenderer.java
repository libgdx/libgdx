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
import java.nio.FloatBuffer;
import java.util.Collection;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.BufferUtils;

import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Rect;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.renderer.CacheContext;
import de.matthiasmann.twl.renderer.DynamicImage;
import de.matthiasmann.twl.renderer.Font;
import de.matthiasmann.twl.renderer.FontParameter;
import de.matthiasmann.twl.renderer.LineRenderer;
import de.matthiasmann.twl.renderer.MouseCursor;
import de.matthiasmann.twl.renderer.OffscreenRenderer;
import de.matthiasmann.twl.renderer.Renderer;
import de.matthiasmann.twl.renderer.Texture;
import de.matthiasmann.twl.utils.ClipStack;

// BOZO - Add cursors.

/**
 * @author Nathan Sweet
 * @author Matthias Mann
 * @author Kurtis Kopf
 */
public class GdxRenderer implements Renderer, LineRenderer {
	private int mouseX, mouseY;
	private GdxCacheContext cacheContext;
	private boolean hasScissor;
	private final TintStack tintStateRoot = new TintStack();
	private TintStack tintStack = tintStateRoot;
	private final Color tempColor = new Color(1, 1, 1, 1);
	private boolean rendering;
	private int width, height;
	final SpriteBatch batch;
	
	private final ClipStack clipStack;

	public GdxRenderer (SpriteBatch batch) {
		this.batch = batch;
		clipStack = new ClipStack();
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

	public boolean startRenderering () {
		tintStack = tintStateRoot;
		batch.begin();
		rendering = true;
		return true;
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
		if (fileName == null) throw new IllegalArgumentException("Font filename parameter is required.");
		if (!fileName.startsWith("/")) fileName = "/" + fileName;
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
		return this;
	}

	public DynamicImage createDynamicImage (int width, int height) {
		return null; // Unsupported.
	}

	public void setCursor (MouseCursor cursor) {
		// Unsupported
	}

	public void setMouseButton (int arg0, boolean arg1) {
		// Unsupported
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

	@Override
	public void clipEnter(Rect rect)
	{
		clipStack.push(rect);
		// using null might not be correct here.
		setClipRect(null);
	}

	@Override
	public void clipEnter(int x, int y, int w, int h)
	{
		clipStack.push(x, y, w, h);
		setClipRect(null);
	}
	
	@Override
	public void clipLeave()
	{
		clipStack.pop();
		setClipRect(null);
	}

	@Override
	public boolean clipIsEmpty()
	{
		return clipStack.isClipEmpty();
	}
	
	@Override
	public OffscreenRenderer getOffscreenRenderer()
	{
		// this is the same as in LWJGLRenderer in the main TWL project
		return null;
	}

	@Override
	public void drawLine(float[] pts, int numPts, float width, de.matthiasmann.twl.Color color, boolean drawAsLoop)
	{
		if(numPts*2 > pts.length) 
		{
            throw new ArrayIndexOutOfBoundsException(numPts*2);
        }
		if(numPts >= 2) 
		{
            if (Gdx.gl11 != null)
            {
            	//tintStack.push(color.getRedFloat(), color.getGreenFloat(), color.getBlueFloat(), color.getAlphaFloat());
            	Gdx.gl.glDisable(GL10.GL_TEXTURE_2D);
                Gdx.gl.glLineWidth(width);
            	FloatBuffer fb = BufferUtils.newFloatBuffer(pts.length);
            	fb.put(pts);
            	fb.position(0);
            	Gdx.gl11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
            	Gdx.gl11.glVertexPointer(2, GL11.GL_FLOAT, 0, fb);
            	Gdx.gl11.glColor4f(color.getRedFloat(), color.getGreenFloat(), color.getBlueFloat(), color.getAlphaFloat());
            	Gdx.gl11.glDrawArrays((drawAsLoop ? GL11.GL_LINE_LOOP : GL11.GL_LINE_STRIP), 0, numPts);
            	Gdx.gl11.glColor4f(tintStack.r, tintStack.g, tintStack.b, tintStack.a);
            	Gdx.gl11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
            	Gdx.gl.glEnable(GL10.GL_TEXTURE_2D);
            }
        }
	}
}
