/*
 * Copyright 2010 Dave Clayton (contact@redskyforge.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package com.badlogic.gdx.graphics.glutils;

/**
 * A simple Utility class to do some basic OpenGL legwork for us.
 * @author Dave Clayton <contact@redskyforge.com>
 *
 */
public class Utils {
	
	static public float[] lparam = new float[4];
	/**
	 * Helper method to store 4 floats in a scratch float array. Useful for some OpenGL calls.
	 * @param f0
	 * @param f1
	 * @param f2
	 * @param f3
	 */
	static public void setLparam(float f0, float f1, float f2, float f3)
	{
		lparam[0] = f0; lparam[1] = f1; lparam[2] = f2; lparam[3] = f3;
	}
}
