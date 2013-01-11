package com.badlogic.gdx.graphics.g3d.xoppa.utils;

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
	private final Texture[] textures;
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
		this.textures = new Texture[count];
		this.reuseWeight = reuseWeight;
		this.weights = (method == WEIGHTED) ? new int[count] : null;
	}
	
	/** Binds the texture if needed and sets it active, returns the unit */
	public final int bindTexture(final Texture texture) {
		return bindTexture(texture, false);
	}
	
	/** Force binds the texture and sets it active, returns the unit */
	public final int rebindTexture(final Texture texture) {
		return bindTexture(texture, false);
	}
	
	/** Removes the reference to the texture, to assure that it will be rebound at the next bind call */
	public final void unbindTexture(final Texture texture) {
		for (int i = 0; i < count; i++) {
			if (textures[i] == texture) {
				textures[i] = null;
				weights[i] = 0;
				return;
			}
		}
	}
	
	private final int bindTexture(final Texture texture, final boolean rebind) {
		int result;
		reused = false;
		
		switch (method) {
		case ROUNDROBIN: result = bindTextureRoundRobin(texture); break;
		case WEIGHTED: result = bindTextureWeighted(texture); break;
		default: return -1; 
		}
		
		if (reused) {
			reuseCount++;
			if (rebind)
				texture.bind(result);
			else
				Gdx.gl.glActiveTexture(GL10.GL_TEXTURE0 + result);
		} else
			bindCount++;
		return result;
	}

	private int currentTexture = 0;
	private final int bindTextureRoundRobin(final Texture texture) {
		for (int i = 0; i < count; i++) {
			final int idx = (currentTexture + i) % count;
			if (textures[idx] == texture) {
				reused = true;
				return offset + idx;
			}
		}
		currentTexture = (currentTexture + 1) % count;
		textures[currentTexture] = texture;
		texture.bind(offset + currentTexture);
		return offset + currentTexture;
	}
	
	private final int bindTextureWeighted(final Texture texture) {
		int result = -1;
		int weight = weights[0];
		int windex = 0;
		for (int i = 0; i < count; i++) {
			if (textures[i] == texture) {
				result = offset + i;
				weights[i]+=reuseWeight;
			} else if (weights[i] < 0 || --weights[i] < weight) {
				weight = weights[i];
				windex = i;
			}
		}
		if (result < 0) {
			textures[windex] = texture;
			weights[windex] = 100;
			texture.bind(result = offset + windex);
		} else 
			reused = true;
		return result;
	}
	
	public final int getBindCount() { return bindCount; }
	public final int getReuseCount() { return reuseCount; }
	public final void resetCounter() { bindCount = reuseCount = 0; }
}
