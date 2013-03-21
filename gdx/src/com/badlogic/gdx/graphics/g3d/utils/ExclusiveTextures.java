package com.badlogic.gdx.graphics.g3d.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** Class that you assign a range of texture units and binds textures for you within that range.
 * It does some basic usage tracking to avoid unnessecary bind calls. 
 * @author xoppa */
public final class ExclusiveTextures {
	public final static int ROUNDROBIN = 0;
	public final static int WEIGHTED = 1;
	/** GLES only supports up to 32 textures */
	public final static int MAX_GLES_UNITS = 32;
	/** The index of the first exclusive texture unit */
	private final int offset;
	/** The amount of exclusive textures that may be used */
	private final int count;
	/** The weight added to a texture when its reused */
	private final int reuseWeight;
	/** The textures currently exclusive bound */
	private final TextureDescription[] textures;
	/** The weight (reuseWeight * reused - discarded) of the textures */
	private final int[] weights;
	/** The method of binding to use */
	private final int method;
	/** Flag to indicate the current texture is reused */
	private boolean reused;
	
	private int reuseCount = 0; // TODO remove debug code
	private int bindCount = 0; // TODO remove debug code
	
	/** Uses all available texture units and reuse weight of 3 */
	public ExclusiveTextures(final int method) {
		this(method, 0);
	}
	
	/** Uses all remaining texture units and reuse weight of 3 */
	public ExclusiveTextures(final int method, final int offset) {
		this(method, offset, GL10.GL_MAX_TEXTURE_UNITS - offset);
	}
	
	/** Uses reuse weight of 10 */
	public ExclusiveTextures(final int method, final int offset, final int count) {
		this(method, offset, count, 10);
	}
	
	public ExclusiveTextures(final int method, final int offset, final int count, final int reuseWeight) {
		int max = Math.max(GL10.GL_MAX_TEXTURE_UNITS, MAX_GLES_UNITS - offset);
		if (offset < 0 || count < 0 || (offset + count) > max || reuseWeight < 1)
			throw new GdxRuntimeException("Illegal arguments");
		this.method = method;
		this.offset = offset;
		this.count = count;
		this.textures = new TextureDescription[count];
		for (int i = 0; i < count; i++)
			this.textures[i] = new TextureDescription();
		this.reuseWeight = reuseWeight;
		this.weights = (method == WEIGHTED) ? new int[count] : null;
	}

	private final TextureDescription tempDesc = new TextureDescription();
	/** @deprecated
	 * Binds the texture if needed and sets it active, returns the unit */
	public final int bindTexture(final Texture texture) {
		tempDesc.reset();
		tempDesc.texture = texture;
		return bindTexture(tempDesc, false);
	}
	
	/** Binds the texture if needed and sets it active, returns the unit */
	public final int bindTexture(final TextureDescription textureDesc) {
		return bindTexture(textureDesc, false);
	}

	/** @deprecated 
	 * Force binds the texture and sets it active, returns the unit */
	public final int rebindTexture(final Texture texture) {
		tempDesc.reset();
		tempDesc.texture = texture;
		return bindTexture(tempDesc, true);
	}
	
	/** Force binds the texture and sets it active, returns the unit */
	public final int rebindTexture(final TextureDescription textureDesc) {
		return bindTexture(textureDesc, true);
	}
	
	/** Removes the reference to the texture, to assure that it will be rebound at the next bind call */
	public final void unbindTexture(final Texture texture) {
		for (int i = 0; i < count; i++) {
			if (textures[i].texture == texture) {
				textures[i] = null;
				weights[i] = 0;
				return;
			}
		}
	}
	
	private final int bindTexture(final TextureDescription textureDesc, final boolean rebind) {
		int idx, result;
		reused = false;
		
		switch (method) {
		case ROUNDROBIN: result = offset + (idx = bindTextureRoundRobin(textureDesc.texture)); break;
		case WEIGHTED: result = offset + (idx = bindTextureWeighted(textureDesc.texture)); break;
		default: return -1; 
		}
		
		if (reused) {
			reuseCount++;
			if (rebind)
				textureDesc.texture.bind(result);
			else
				Gdx.gl.glActiveTexture(GL10.GL_TEXTURE0 + result);
		} else
			bindCount++;
		if (textureDesc.minFilter != GL10.GL_INVALID_VALUE && textureDesc.minFilter != textures[idx].minFilter)
			Gdx.gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, textures[idx].minFilter = textureDesc.minFilter);
		if (textureDesc.magFilter != GL10.GL_INVALID_VALUE && textureDesc.magFilter != textures[idx].magFilter)
			Gdx.gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, textures[idx].magFilter = textureDesc.magFilter);
		if (textureDesc.uWrap != GL10.GL_INVALID_VALUE && textureDesc.uWrap != textures[idx].uWrap)
			Gdx.gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, textures[idx].uWrap = textureDesc.uWrap);
		if (textureDesc.vWrap != GL10.GL_INVALID_VALUE && textureDesc.vWrap != textures[idx].vWrap)
			Gdx.gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, textures[idx].vWrap = textureDesc.vWrap);
		return result;
	}

	private int currentTexture = 0;
	private final int bindTextureRoundRobin(final Texture texture) {
		for (int i = 0; i < count; i++) {
			final int idx = (currentTexture + i) % count;
			if (textures[idx].texture == texture) {
				reused = true;
				return idx;
			}
		}
		currentTexture = (currentTexture + 1) % count;
		textures[currentTexture].texture = texture;
		texture.bind(offset + currentTexture);
		return currentTexture;
	}
	
	private final int bindTextureWeighted(final Texture texture) {
		int result = -1;
		int weight = weights[0];
		int windex = 0;
		for (int i = 0; i < count; i++) {
			if (textures[i].texture == texture) {
				result = i;
				weights[i]+=reuseWeight;
			} else if (weights[i] < 0 || --weights[i] < weight) {
				weight = weights[i];
				windex = i;
			}
		}
		if (result < 0) {
			textures[windex].texture = texture;
			weights[windex] = 100;
			texture.bind(offset + (result = windex));
		} else 
			reused = true;
		return result;
	}
	
	public final int getBindCount() { return bindCount; }
	public final int getReuseCount() { return reuseCount; }
	public final void resetCounter() { bindCount = reuseCount = 0; }
}
