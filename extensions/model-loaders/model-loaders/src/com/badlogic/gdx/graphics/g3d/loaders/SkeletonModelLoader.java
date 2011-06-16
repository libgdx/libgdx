package com.badlogic.gdx.graphics.g3d.loaders;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.ModelLoaderHints;
import com.badlogic.gdx.graphics.g3d.model.skeleton.SkeletonModel;

/**
 * Interface for classes loading {@link SkeletonModel} instances.
 * @author mzechner
 *
 */
public interface SkeletonModelLoader extends ModelLoader {
	public SkeletonModel load(FileHandle file, ModelLoaderHints hints);
}
