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
public class SpotLight extends BaseLight {
	public final Vector3 position = new Vector3();
	public final Vector3 direction = new Vector3();
	public float intensity;
	public float cutoffAngle;
	public float exponent;

	public static class Builder extends BaseLight.Builder {
		float positionX;
		float positionY;
		float positionZ;
		float directionX;
		float directionY;
		float directionZ;
		float intensity = 1;
		float cutoffAngle = 25;
		float exponent = 1;

		public Builder position(float positionX, float positionY, float positionZ) {
			this.positionX = positionX;
			this.positionY = positionY;
			this.positionZ = positionZ;
			return this;
		}

		public Builder position(Vector3 position) {
			positionX = position.x;
			positionY = position.y;
			positionZ = position.z;
			return this;
		}

		public Builder direction(float directionX, float directionY, float directionZ) {
			this.directionX = directionX;
			this.directionY = directionY;
			this.directionZ = directionZ;
			return this;
		}

		public Builder direction(Vector3 direction) {
			directionX = direction.x;
			directionY = direction.y;
			directionZ = direction.z;
			return this;
		}

		public Builder intensity(float intensity) {
			this.intensity = intensity;
			return this;
		}

		public Builder cutoffAngle(float cutoffAngle) {
			this.cutoffAngle = cutoffAngle;
			return this;
		}

		public Builder exponent(float exponent) {
			this.exponent = exponent;
			return this;
		}

		public SpotLight build() {
			return new SpotLight(this);
		}
	}

	public SpotLight(Builder builder) {
		this.color.set(builder.r, builder.g, builder.b, builder.a);
		this.position.set(builder.positionX, builder.positionY, builder.positionZ);
		this.direction.set(builder.directionX, builder.directionY, builder.directionZ);
		this.intensity = builder.intensity;
		this.cutoffAngle = builder.cutoffAngle;
		this.exponent = builder.exponent;
	}

	public SpotLight() {
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
