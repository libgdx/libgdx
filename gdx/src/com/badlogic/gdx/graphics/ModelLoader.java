package com.badlogic.gdx.graphics;

import java.io.InputStream;

import com.badlogic.gdx.graphics.loaders.ObjLoader;

/**
 * A class for loading various model formats such as 
 * Wavefront OBJ or the Quake II MD2 format. Ties in
 * all the loaders from the loaders package.
 * 
 * @author mzechner
 *
 */
public class ModelLoader 
{
	/**
	 * Loads a Wavefront OBJ file from the given
	 * InputStream. The OBJ file must only contain
	 * triangulated meshes. Materials are ignored.
	 * 
	 * @param in the InputStream
	 * @param useFloats whether to return a {@link FloatMesh} or a {@link FixedPointMesh}
	 * @return a Mesh holding the OBJ data or null in case something went wrong.
	 */
	public static Mesh loadObj( InputStream in, boolean useFloats )
	{
		return ObjLoader.loadObj( in, useFloats);
	}
}
