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

package com.badlogic.gdx.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** CompositeTexture wraps two textures as a single Texture, binding them on separate texture units. The textures may be
 * processed using a custom shader for various blending and lighting effects.
 *
 * @see com.badlogic.gdx.graphics.g2d.MaskShader MaskShader
 * @author Valentin Milea */
public class CompositeTexture extends Texture {

	private final Texture texture0;
	private final Texture texture1;

	/** Wraps a pair of textures */
	public CompositeTexture (Texture texture0, Texture texture1) {
		this.texture0 = texture0;
		this.texture1 = texture1;
	}

	/** @return texture0 */
	public Texture getTexture0 () {
		return texture0;
	}

	/** @return texture1 */
	public Texture getTexture1 () {
		return texture1;
	}

	@Override
	public void draw (Pixmap pixmap, int x, int y) {
		texture0.draw(pixmap, x, y);
	}

	/** @return the width of texture0 in pixels */
	@Override
	public int getWidth () {
		return texture0.getWidth();
	}

	/** @return the height of texture0 in pixels */
	@Override
	public int getHeight () {
		return texture0.getHeight();
	}

	/** The CompositingTexture is an unmanaged wrapper.
	 * @return false */
	@Override
	public boolean isManaged () {
		return false;
	}

	/** Disposes associated textures */
	@Override
	public void dispose () {
		texture1.dispose();
		texture0.dispose();
	}

	/** Binds texture0 to the texture unit 0, and texture1 to texture unit 1. On exit, the active texture unit will be 0.
	 * @see GL20#glActiveTexture(int)
	 * @see GL20#glBindTexture(int, int) */
	@Override
	public void bind () {
		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE1);
		texture1.bind();
		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
		texture0.bind();
	}

	/** Binds texture0 to the given texture unit, and texture1 to the following texture unit. On exit, the active texture unit will
	 * be {@code unit}.
	 * @param unit the unit (0 to MAX_TEXTURE_UNITS-1).
	 * @see GL20#glActiveTexture(int)
	 * @see GL20#glBindTexture(int, int) */
	@Override
	public void bind (int unit) {
		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE1 + unit);
		texture1.bind();
		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0 + unit);
		texture0.bind();
	}

	@Override
	public TextureFilter getMinFilter () {
		return texture0.getMinFilter();
	}

	@Override
	public TextureFilter getMagFilter () {
		return texture0.getMagFilter();
	}

	@Override
	public TextureWrap getUWrap () {
		return texture0.getUWrap();
	}

	@Override
	public TextureWrap getVWrap () {
		return texture0.getVWrap();
	}

	/** Sets the {@link Texture.TextureWrap} for the composited textures. Assumes the textures are already bound to texture units 0
	 * and 1. On exit, the active texture unit will be 0.
	 * @param u the u wrap
	 * @param v the v wrap
	 * @param force True to always set the values, even if they are the same as the current values. */
	public void unsafeSetWrap (TextureWrap u, TextureWrap v, boolean force) {
		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE1);
		texture1.unsafeSetWrap(u, v, force);
		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
		texture0.unsafeSetWrap(u, v, force);
	}

	/** Sets the {@link Texture.TextureWrap} for the composited textures. This will bind the textures. On exit, the active texture
	 * unit will have texture0 bound to it.
	 * @param u the u wrap
	 * @param v the v wrap */
	public void setWrap (TextureWrap u, TextureWrap v) {
		texture1.setWrap(u, v);
		texture0.setWrap(u, v);
	}

	/** Sets the {@link Texture.TextureFilter} for the composited textures. Assumes the textures are already bound to texture units
	 * 0 and 1. On exit, the active texture unit will be 0.
	 * @param minFilter the minification filter
	 * @param magFilter the magnification filter
	 * @param force True to always set the values, even if they are the same as the current values. */
	@Override
	public void unsafeSetFilter (TextureFilter minFilter, TextureFilter magFilter, boolean force) {
		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE1);
		texture1.unsafeSetFilter(minFilter, magFilter, force);
		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
		texture0.unsafeSetFilter(minFilter, magFilter, force);
	}

	/** Sets the {@link Texture.TextureFilter} for the composited textures. This will bind the textures. On exit, the active texture
	 * unit will have texture0 bound to it.
	 * @param minFilter the minification filter
	 * @param magFilter the magnification filter */
	@Override
	public void setFilter (TextureFilter minFilter, TextureFilter magFilter) {
		texture1.setFilter(minFilter, magFilter);
		texture0.setFilter(minFilter, magFilter);
	}

	/** Unsupported. */
	@Override
	public TextureData getTextureData () {
		throw new GdxRuntimeException("CompositeTexture doesn't own texure data, use texture0 or texture1 instead");
	}

	/** Unsupported. */
	@Override
	public void load (TextureData data) {
		throw new GdxRuntimeException("CompositeTexture doesn't own texure data, use texture0 or texture1 instead");
	}

	/** Unsupported. */
	@Override
	public int getTextureObjectHandle () {
		throw new GdxRuntimeException("CompositeTexture doesn't own texure data, use texture0 or texture1 instead");
	}
}
