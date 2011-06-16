package com.badlogic.gdx.graphics.g3d;

import com.badlogic.gdx.graphics.g3d.loaders.ModelLoaderRegistry;

/**
 * Hints passed to a loader which might ignore them. See {@link ModelLoaderRegistry}.
 * @author mzechner
 *
 */
public class ModelLoaderHints {
	/** whether to flip the v texture coordinate **/ 
	public final boolean flipV;
	
	public ModelLoaderHints(boolean flipV) {
		this.flipV = flipV;
	}
}
