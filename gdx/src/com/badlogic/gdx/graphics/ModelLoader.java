/*
 *  This file is part of Libgdx by Mario Zechner (badlogicgames@gmail.com)
 *
 *  Libgdx is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Libgdx is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with libgdx.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.badlogic.gdx.graphics;

import java.io.InputStream;

import com.badlogic.gdx.graphics.loaders.ObjLoader;
import com.badlogic.gdx.graphics.loaders.OctLoader;
import com.badlogic.gdx.math.Vector;

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
	
	/**
	 * Loads an OCT file as can be found in many of Paul Nettle's
	 * demo programs. See the source at http://www.paulnettle.com/pub/FluidStudios/CollisionDetection/Fluid_Studios_Collision_Detection_Demo_and_Source.zip
	 * for more information.
	 * 
	 * @param in the InputStream
	 * @param useFloats whether to return a {@link FloatMesh} or a {@link FixedPointMesh}
	 * @param start the start position as defined in the map
	 * @return a Mesh holding the OCT data or null in case something went wrong.
	 */
	public static Mesh loadOct( InputStream in, boolean useFloats, Vector start )
	{
		return OctLoader.loadOct( in, useFloats, start );
	}
}
