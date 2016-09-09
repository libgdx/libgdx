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

package com.badlogic.gdx.physics.box2d;

import com.badlogic.gdx.math.Vector2;

/** A circle shape.
 * @author mzechner */
public class CircleShape extends Shape {
	// @off
	/*JNI
#include <Box2D/Box2D.h>
	 */
	
	public CircleShape () {
		addr = newCircleShape();
	}

	private native long newCircleShape (); /*
		return (jlong)(new b2CircleShape( ));
	*/

	protected CircleShape (long addr) {
		this.addr = addr;
	}

	/** {@inheritDoc} */
	@Override
	public Type getType () {
		return Type.Circle;
	}

	/** Returns the position of the shape */
	private final float[] tmp = new float[2];
	private final Vector2 position = new Vector2();

	public Vector2 getPosition () {
		jniGetPosition(addr, tmp);
		position.x = tmp[0];
		position.y = tmp[1];
		return position;
	}

	private native void jniGetPosition (long addr, float[] position); /*
		b2CircleShape* circle = (b2CircleShape*)addr;
		position[0] = circle->m_p.x;
		position[1] = circle->m_p.y;
	*/

	/** Sets the position of the shape */
	public void setPosition (Vector2 position) {
		jniSetPosition(addr, position.x, position.y);
	}

	private native void jniSetPosition (long addr, float positionX, float positionY); /*
		b2CircleShape* circle = (b2CircleShape*)addr;
		circle->m_p.x = positionX;
		circle->m_p.y = positionY;
	*/
}
