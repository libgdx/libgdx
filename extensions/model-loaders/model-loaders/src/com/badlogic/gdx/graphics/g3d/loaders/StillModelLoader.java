package com.badlogic.gdx.graphics.g3d.loaders;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.ModelLoaderHints;
import com.badlogic.gdx.graphics.g3d.model.still.StillModel;

/**
 * Interface for loaders loading {@link StillModel} instances.
 * @author mzechner
 *
 */
public interface StillModelLoader extends ModelLoader {	
	public StillModel load(FileHandle handle, ModelLoaderHints hints);
}
