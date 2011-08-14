/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.badlogic.gdx.graphics.g3d.loaders.md2;

public class MD2Header {
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