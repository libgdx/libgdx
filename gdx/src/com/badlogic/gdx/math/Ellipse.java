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

package com.badlogic.gdx.math;

/** A convenient 2D ellipse class, based on the circle class
 * @author tonyp7 */
public class Ellipse extends RectangleBased<Ellipse> {

	/** Construct a new ellipse with all values set to zero */
	public Ellipse () {

	}

	public Ellipse (Ellipse ellipse) {
		super(ellipse);
	}

	public Ellipse (float x, float y, float width, float height) {
		super(x, y, width, height);
	}

	public Ellipse (Vector2 position, float width, float height) {
		super(position, width, height);
	}

	@Override
	public boolean contains (float x, float y) {
		x = x - this.x;
		y = y - this.y;
		return (x * x) / (width * 0.5f * width * 0.5f) + (y * y) / (height * 0.5f * height * 0.5f) <= 1.0f;
	}

	/** Sets a new position and size for this ellipse based upon another ellipse.
	 * @param ellipse The ellipse to copy the position and size of. */
	@Override
	public void set (Ellipse ellipse) {
		set(ellipse.x, ellipse.y, ellipse.width, ellipse.height);
	}

    @Override
    public Ellipse cpy() {
        return new Ellipse(this);
    }

}
