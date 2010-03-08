/**
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
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.badlogic.gdx.models;

public class MD2Header 
{
	public int ident;
	public int version;
	public int skinWidth;
	public int skinHeight;
	public int frameSize;
	public int numSkins;
	public int numVertices;
	public int numTexCoords;
	public int numTriangles;
	public int numGLCommands;
	public int numFrames;
	public int offsetSkin;
	public int offsetTexCoords;
	public int offsetTriangles;
	public int offsetFrames;
	public int offsetGLCommands;
	public int offsetEnd;
}
