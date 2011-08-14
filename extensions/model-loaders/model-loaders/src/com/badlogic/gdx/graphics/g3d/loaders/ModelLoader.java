
package com.badlogic.gdx.graphics.g3d.loaders;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.ModelLoaderHints;
import com.badlogic.gdx.graphics.g3d.model.Model;

/** Interface for all loaders. Loaders that need more files need to derrive the other file names by the given file. A bit of a
 * hack, but most formats are self contained.
 * 
 * @author mzechner */
public interface ModelLoader {
	public Model load (FileHandle file, ModelLoaderHints hints);
}
