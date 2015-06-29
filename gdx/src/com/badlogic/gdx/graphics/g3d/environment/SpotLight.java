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
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

/** Note that the default shader doesn't support spot lights, you'll have to supply your own shader to use this class.
 * @author realitix */
public class SpotLight extends BaseLight<SpotLight> {
	public final Vector3 position = new Vector3();
	public final Vector3 direction = new Vector3();
	public float intensity;
	public float cutoffAngle;
	public float exponent;

	public SpotLight setPosition(float positionX, float positionY, float positionZ) {
		this.position.set(positionX, positionY, positionZ);
		return this;
	}

	public SpotLight setPosition(Vector3 position) {
		this.position.set(position);
		return this;
	}

	public SpotLight setDirection(float directionX, float directionY, float directionZ) {
		this.direction.set(directionX, directionY, directionZ);
		return this;
	}

	public SpotLight setDirection(Vector3 direction) {
		this.direction.set(direction);
		return this;
	}

	public SpotLight setIntensity(float intensity) {
		this.intensity = intensity;
		return this;
	}

	public SpotLight setCutoffAngle(float cutoffAngle) {
		this.cutoffAngle = cutoffAngle;
		return this;
	}

	public SpotLight setExponent(float exponent) {
		this.exponent = exponent;
		return this;
	}

	public SpotLight set (final SpotLight copyFrom) {
		return set(copyFrom.color, copyFrom.position, copyFrom.direction, copyFrom.intensity, copyFrom.cutoffAngle, copyFrom.exponent);
	}

	public SpotLight set (final Color color, final Vector3 position, final Vector3 direction, final float intensity,
		final float cutoffAngle, final float exponent) {
		if (color != null) this.color.set(color);
		if (position != null) this.position.set(position);
		if (direction != null) this.direction.set(direction).nor();
		this.intensity = intensity;
		this.cutoffAngle = cutoffAngle;
		this.exponent = exponent;
		return this;
	}

	public SpotLight set (final float r, final float g, final float b, final Vector3 position, final Vector3 direction,
		final float intensity, final float cutoffAngle, final float exponent) {
		this.color.set(r, g, b, 1f);
		if (position != null) this.position.set(position);
		if (direction != null) this.direction.set(direction).nor();
		this.intensity = intensity;
		this.cutoffAngle = cutoffAngle;
		this.exponent = exponent;
		return this;
	}

	public SpotLight set (final Color color, final float posX, final float posY, final float posZ, final float dirX,
		final float dirY, final float dirZ, final float intensity, final float cutoffAngle, final float exponent) {
		if (color != null) this.color.set(color);
		this.position.set(posX, posY, posZ);
		this.direction.set(dirX, dirY, dirZ).nor();
		this.intensity = intensity;
		this.cutoffAngle = cutoffAngle;
		this.exponent = exponent;
		return this;
	}

	public SpotLight set (final float r, final float g, final float b, final float posX, final float posY, final float posZ,
		final float dirX, final float dirY, final float dirZ, final float intensity, final float cutoffAngle, final float exponent) {
		this.color.set(r, g, b, 1f);
		this.position.set(posX, posY, posZ);
		this.direction.set(dirX, dirY, dirZ).nor();
		this.intensity = intensity;
		this.cutoffAngle = cutoffAngle;
		this.exponent = exponent;
		return this;
	}

	public SpotLight setTarget (final Vector3 target) {
		direction.set(target).sub(position).nor();
		return this;
	}

	@Override
	public boolean equals (Object obj) {
		return (obj instanceof SpotLight) ? equals((SpotLight)obj) : false;
	}

	public boolean equals (SpotLight other) {
		return (other != null && (other == this || (color.equals(other.color) && position.equals(other.position)
			&& direction.equals(other.direction) && MathUtils.isEqual(intensity, other.intensity) && MathUtils.isEqual(cutoffAngle,
				other.cutoffAngle) && MathUtils.isEqual(exponent, other.exponent) )));
	}
}
