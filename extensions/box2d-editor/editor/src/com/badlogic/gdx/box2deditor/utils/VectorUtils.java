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
package com.badlogic.gdx.box2deditor.utils;

import com.badlogic.gdx.math.Vector2;

/**
 *
 * @author Aurelien Ribon (aurelien.ribon@gmail.com)
 */
public class VectorUtils {
	public static Vector2 mul(Vector2 v, float coeff) {
		return new Vector2(v).mul(coeff);
	}

	public static Vector2[] mul(Vector2[] vs, float coeff) {
		Vector2[] ret = new Vector2[vs.length];
		for (int i=0; i<ret.length; i++)
			ret[i] = mul(vs[i], coeff);
		return ret;
	}

	public static Vector2[][] mul(Vector2[][] vss, float coeff) {
		Vector2[][] ret = new Vector2[vss.length][];
		for (int i=0; i<ret.length; i++)
			ret[i] = mul(vss[i], coeff);
		return ret;
	}

	// -------------------------------------------------------------------------

	public static Vector2 getCopy(Vector2 v) {
		if (v == null)
			return null;
		return v.cpy();
	}

	public static Vector2[] getCopy(Vector2[] vs) {
		if (vs == null)
			return null;

		Vector2[] ret = new Vector2[vs.length];
		for (int i=0; i<ret.length; i++)
			ret[i] = getCopy(vs[i]);
		return ret;
	}

	public static Vector2[][] getCopy(Vector2[][] vss) {
		if (vss == null)
			return null;

		Vector2[][] ret = new Vector2[vss.length][];
		for (int i=0; i<ret.length; i++)
			ret[i] = getCopy(vss[i]);
		return ret;
	}
}