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

public class PointLight extends BaseLight<PointLight> {
	public final Vector3 position = new Vector3();
	public float intensity;

	public PointLight setPosition(float positionX, float positionY, float positionZ) {
		this.position.set(positionX, positionY, positionZ);
		return this;
	}

	public PointLight setPosition(Vector3 position) {
		this.position.set(position);
		return this;
	}

	public PointLight setIntensity(float intensity) {
		this.intensity = intensity;
		return this;
	}

	public PointLight set (final PointLight copyFrom) {
		return set(copyFrom.color, copyFrom.position, copyFrom.intensity);
	}

	public PointLight set (final Color color, final Vector3 position, final float intensity) {
		if (color != null) this.color.set(color);
		if (position != null) this.position.set(position);
		this.intensity = intensity;
		return this;
	}

	public PointLight set (final float r, final float g, final float b, final Vector3 position, final float intensity) {
		this.color.set(r, g, b, 1f);
		if (position != null) this.position.set(position);
		this.intensity = intensity;
		return this;
	}

	public PointLight set (final Color color, final float x, final float y, final float z, final float intensity) {
		if (color != null) this.color.set(color);
		this.position.set(x, y, z);
		this.intensity = intensity;
		return this;
	}

	public PointLight set (final float r, final float g, final float b, final float x, final float y, final float z,
		final float intensity) {
		this.color.set(r, g, b, 1f);
		this.position.set(x, y, z);
		this.intensity = intensity;
		return this;
	}

	@Override
	public boolean equals (Object obj) {
		return (obj instanceof PointLight) && equals((PointLight) obj);
	}

	public boolean equals (PointLight other) {
		return (other != null && (other == this || (color.equals(other.color) && position.equals(other.position) && intensity == other.intensity)));
	}
}
