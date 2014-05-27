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

package com.badlogic.gdx.math.collision;

import com.badlogic.gdx.math.Shape3D;
import com.badlogic.gdx.math.Vector3;

/** Encapsulates a 3D sphere with a center and a radius
 *
 * @author badlogicgames@gmail.com */
public class Sphere extends Shape3D<Sphere> {

	public float radius;

	/** Constructs a new sphere with all values set to zero */
	public Sphere () {

	}

	public Sphere (float x, float y, float z, float radius) {
		set(x, y, z, radius);
	}

	public Sphere (Vector3 position, float radius) {
		super(position);
        setRadius(radius);
	}

	public Sphere (Sphere sphere) {
		super(sphere);
	}

    public void set(float x, float y, float z, float radius) {
        setPosition(x, y, z);
        setRadius(radius);
    }

    /** Sets a new location and radius for this circle.
	 * @param position Position {@link Vector3} for this sphere.
	 * @param radius sphere radius */
    public void set(Vector3 position, float radius) {
        set(position.x, position.y, position.z, radius);
    }

    /** Sets a new location and radius for this sphere, based upon another sphere.
	 * @param sphere The sphere to copy the position and radius of. */
     @Override
     public void set(Sphere sphere) {
        set(sphere.x, sphere.y, sphere.z, sphere.radius);
    }

    @Override
    public Sphere cpy() {
        return new Sphere(this);
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public float getRadius() {
        return radius;
    }

    @Override
    public boolean contains(float x, float y, float z) {
        float a = x - this.x;
        float b = y - this.y;
        float c = z - this.z;
        return a*a + b*b + c*c < radius*radius;
    }

	/** @param sphere the other sphere
	 * @return whether this and the other sphere overlap */
	public boolean overlaps (Sphere sphere) {
        float a = x - this.x;
        float b = y - this.y;
        float c = z - this.z;
        float radius = this.radius + sphere.radius;
		return a*a + b*b + c*c < radius*radius;
	}
}
