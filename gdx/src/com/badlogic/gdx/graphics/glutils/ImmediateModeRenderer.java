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

package com.badlogic.gdx.graphics.glutils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Matrix4;

public interface ImmediateModeRenderer {
	public void begin (Matrix4 projModelView, int primitiveType);

	public void flush ();
	
	public void color (Color color);

	public void color (float r, float g, float b, float a);
	
	public void color (float colorBits);

	public void texCoord (float u, float v);

	public void normal (float x, float y, float z);

	public void vertex (float x, float y, float z);

	public void end ();

	public int getNumVertices ();

	public int getMaxVertices ();

	public void dispose ();
}
