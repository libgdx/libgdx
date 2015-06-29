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

package com.badlogic.gdx.graphics.g3d.environment;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;

public class DirectionalLight extends BaseLight<DirectionalLight> {
	public final Vector3 direction = new Vector3();

	public DirectionalLight setDirection(float directionX, float directionY, float directionZ) {
		this.direction.set(directionX, directionY, directionZ);
		return this;
	}

	public DirectionalLight setDirection(Vector3 direction) {
		this.direction.set(direction);
		return this;
	}

	public DirectionalLight set (final DirectionalLight copyFrom) {
		return set(copyFrom.color, copyFrom.direction);
	}

	public DirectionalLight set (final Color color, final Vector3 direction) {
		if (color != null) this.color.set(color);
		if (direction != null) this.direction.set(direction).nor();
		return this;
	}

	public DirectionalLight set (final float r, final float g, final float b, final Vector3 direction) {
		this.color.set(r, g, b, 1f);
		if (direction != null) this.direction.set(direction).nor();
		return this;
	}

	public DirectionalLight set (final Color color, final float dirX, final float dirY, final float dirZ) {
		if (color != null) this.color.set(color);
		this.direction.set(dirX, dirY, dirZ).nor();
		return this;
	}

	public DirectionalLight set (final float r, final float g, final float b, final float dirX, final float dirY, final float dirZ) {
		this.color.set(r, g, b, 1f);
		this.direction.set(dirX, dirY, dirZ).nor();
		return this;
	}

	@Override
	public boolean equals (Object arg0) {
		return (arg0 instanceof DirectionalLight) ? equals((DirectionalLight)arg0) : false;
	}

	public boolean equals (final DirectionalLight other) {
		return (other != null) && ((other == this) || ((color.equals(other.color) && direction.equals(other.direction))));
	}
}
