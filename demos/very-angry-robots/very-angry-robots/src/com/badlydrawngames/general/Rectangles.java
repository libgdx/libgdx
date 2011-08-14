/*
 * Copyright 2011 Rod Hyde (rod@badlydrawngames.com)
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

package com.badlydrawngames.general;

import com.badlogic.gdx.math.Rectangle;

import static com.badlydrawngames.general.MathUtils.*;

public class Rectangles {

	private Rectangles () {
	}

	public static void setRectangle (Rectangle r, float x, float y, float w, float h) {
		r.x = x;
		r.y = y;
		r.width = w;
		r.height = h;
	}

	public static Rectangle union (Rectangle a, Rectangle b, Rectangle result) {
		result.x = min(a.x, b.x);
		result.y = min(a.y, b.y);
		result.width = max(a.x + a.width, b.x + b.width) - result.x;
		result.height = max(a.y + a.height, b.y + b.height) - result.y;
		return result;
	}
}
