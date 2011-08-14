
package com.badlogic.gdx.graphics.g3d.loaders;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.ModelLoaderHints;
import com.badlogic.gdx.graphics.g3d.model.keyframe.KeyframedModel;

/** Interface for classes loading {@link KeyframedModel} instances.
 * @author mzechner */
public interface KeyframedModelLoader extends ModelLoader {
	public KeyframedModel load (FileHandle handle, ModelLoaderHints hints);
}
