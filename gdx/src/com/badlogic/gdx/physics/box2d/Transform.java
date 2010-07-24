/*******************************************************************************
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com)
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
package com.badlogic.gdx.physics.box2d;

import com.badlogic.gdx.math.Vector2;

/**
 * Encodes a Box2D transform. We are lazy so we only store a 6 float wide
 * array. First two floats are the position of the b2Transform struct. Next
 * two floats are the b2Transform.R.col1 x and y coordinates. Final 2 floats
 * are the b2Transform.R.col2 x and y coordinates;
 * @author mzechner
 *
 */
public class Transform 
{
	public static final int POS_X = 0;
	public static final int POS_Y = 1;
	public static final int COL1_X = 2;
	public static final int COL1_Y = 3;
	public static final int COL2_X = 4;
	public static final int COL2_Y = 5;
	
	public float[] vals = new float[6];

	/**
	 * Transforms the given vector by this transform
	 * @param v the vector
	 */
	public void mul(Vector2 v) 
	{	
		float x = vals[POS_X] + vals[COL1_X] * v.x + vals[COL2_X] * v.y;
		float y = vals[POS_Y] + vals[COL1_Y] * v.x + vals[COL2_Y] * v.y;
		
		v.x = x;
		v.y = y;
	}
}
