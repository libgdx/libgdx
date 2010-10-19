
package com.badlogic.gdx.twl.renderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;

import de.matthiasmann.twl.renderer.AnimationState;
import de.matthiasmann.twl.renderer.FontCache;

class GdxFontCache implements FontCache {
	private final GdxFont font;
	Mesh mesh;
	int width, height;

	public GdxFontCache (GdxFont font, int capacity) {
		ensureCapacity(capacity);
		this.font = font;
	}

	public int getWidth () {
		return width;
	}

	public int getHeight () {
		return height;
	}

	public void draw (AnimationState as, int x, int y) {
		GdxFont.FontState fontState = font.evalFontState(as);
		font.renderer.tintStack.setColor(fontState.color);
		font.bitmapFont.texture.bind();
		GL10 gl = Gdx.graphics.getGL10();
		gl.glPushMatrix();
		gl.glTranslatef(x + fontState.offsetX, y + fontState.offsetY, 0);
		mesh.render(GL11.GL_TRIANGLE_STRIP);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glPopMatrix();
	}

	public void destroy () {
		mesh.dispose();
	}

	public void ensureCapacity (int capacity) {
		if (mesh == null || mesh.getNumVertices() < capacity) {
			if (mesh != null) mesh.dispose();
			mesh = new Mesh(true, false, capacity, 0, new VertexAttribute(VertexAttributes.Usage.Position, 2, null),
				new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, null));
		}
	}
}
