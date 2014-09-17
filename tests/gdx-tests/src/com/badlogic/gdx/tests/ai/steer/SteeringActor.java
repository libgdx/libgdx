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

package com.badlogic.gdx.tests.ai.steer;

import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;

/** A SteeringActor is a scene2d {@link Actor} implementing the {@link Steerable} interface.
 * 
 * @autor davebaol */
public class SteeringActor extends Actor implements Steerable<Vector2> {

	private static final SteeringAcceleration<Vector2> steeringOutput = new SteeringAcceleration<Vector2>(new Vector2());

	TextureRegion region;

	Vector2 position;  // like scene2d centerX and centerY, but we need a vector to implement Steerable
	Vector2 linearVelocity;
	float angularVelocity;
	float boundingRadius;
	boolean tagged;

	float maxLinearSpeed = 100;
	float maxLinearAcceleration = 200;
	float maxAngularSpeed = 5;
	float maxAngularAcceleration = 10;

	boolean independentFacing;

	SteeringBehavior<Vector2> steeringBehavior;

	public SteeringActor (TextureRegion region) {
		this(region, false);
	}

	public SteeringActor (TextureRegion region, boolean independentFacing) {
		this.independentFacing = independentFacing;
		this.region = region;
		this.position = new Vector2();
		this.linearVelocity = new Vector2();
		this.setBounds(0, 0, region.getRegionWidth(), region.getRegionHeight());
		this.boundingRadius = (region.getRegionWidth() + region.getRegionHeight()) / 4f;
		this.setOrigin(region.getRegionWidth() * .5f, region.getRegionHeight() * .5f);
	}

	public TextureRegion getRegion () {
		return region;
	}

	public void setRegion (TextureRegion region) {
		this.region = region;
	}

	@Override
	public Vector2 getPosition () {
		return position;
	}

	@Override
	public float getOrientation () {
		return getRotation() * MathUtils.degreesToRadians;
	}

	@Override
	public Vector2 getLinearVelocity () {
		return linearVelocity;
	}

	@Override
	public float getAngularVelocity () {
		return angularVelocity;
	}

	@Override
	public float getBoundingRadius () {
		return boundingRadius;
	}

	@Override
	public boolean isTagged () {
		return tagged;
	}

	@Override
	public void setTagged (boolean tagged) {
		this.tagged = tagged;
	}

	@Override
	public Vector2 newVector () {
		return new Vector2();
	}

	@Override
	public float vectorToAngle (Vector2 vector) {
		return (float)Math.atan2(-vector.x, vector.y);
	}

	@Override
	public Vector2 angleToVector (Vector2 outVector, float angle) {
		outVector.x = -(float)Math.sin(angle);
		outVector.y = (float)Math.cos(angle);
		return outVector;
	}

	@Override
	public float getMaxLinearSpeed () {
		return maxLinearSpeed;
	}

	@Override
	public void setMaxLinearSpeed (float maxLinearSpeed) {
		this.maxLinearSpeed = maxLinearSpeed;
	}

	@Override
	public float getMaxLinearAcceleration () {
		return maxLinearAcceleration;
	}

	@Override
	public void setMaxLinearAcceleration (float maxLinearAcceleration) {
		this.maxLinearAcceleration = maxLinearAcceleration;
	}

	@Override
	public float getMaxAngularSpeed () {
		return maxAngularSpeed;
	}

	@Override
	public void setMaxAngularSpeed (float maxAngularSpeed) {
		this.maxAngularSpeed = maxAngularSpeed;
	}

	@Override
	public float getMaxAngularAcceleration () {
		return maxAngularAcceleration;
	}

	@Override
	public void setMaxAngularAcceleration (float maxAngularAcceleration) {
		this.maxAngularAcceleration = maxAngularAcceleration;
	}

	public boolean isIndependentFacing () {
		return independentFacing;
	}

	public void setIndependentFacing (boolean independentFacing) {
		this.independentFacing = independentFacing;
	}

	public SteeringBehavior<Vector2> getSteeringBehavior () {
		return steeringBehavior;
	}

	public void setSteeringBehavior (SteeringBehavior<Vector2> steeringBehavior) {
		this.steeringBehavior = steeringBehavior;
	}

	@Override
	public void act (float delta) {
		position.set(getCenterX(), getCenterY());
		if (steeringBehavior != null) {
			steeringBehavior.steer(steeringOutput);
			applySteering(steeringOutput, delta);
			wrapAround(position, getParent().getWidth(), getParent().getHeight());
			setCenterPosition(position.x, position.y);
		}
		super.act(delta);
	}

	// the display area is considered to wrap around from top to bottom
	// and from left to right
	public static void wrapAround (Vector2 pos, float maxX, float maxY) {
		if (pos.x > maxX) pos.x = 0.0f;

		if (pos.x < 0) pos.x = maxX;

		if (pos.y < 0) pos.y = maxY;

		if (pos.y > maxY) pos.y = 0.0f;
	}

	private void applySteering (SteeringAcceleration<Vector2> steering, float time) {
		// Update position and linear velocity. Velocity is trimmed to maximum speed
		position.mulAdd(linearVelocity, time);
		linearVelocity.mulAdd(steering.linear, time).limit(getMaxLinearSpeed());

		// Update orientation and angular velocity
		if (independentFacing) {
			setRotation(getRotation() + (angularVelocity * time) * MathUtils.radiansToDegrees);
			angularVelocity += steering.angular * time;
		} else {
			// If we haven't got any velocity, then we can do nothing.
			if (!linearVelocity.isZero(MathUtils.FLOAT_ROUNDING_ERROR)) {
				float newOrientation = vectorToAngle(linearVelocity);
				angularVelocity = (newOrientation - getRotation() * MathUtils.degreesToRadians) * time; // this is superfluous if independentFacing is always true
				setRotation(newOrientation * MathUtils.radiansToDegrees);
			}
		}
	}

	@Override
	public void draw (Batch batch, float parentAlpha) {
		Color color = getColor();
		batch.setColor(color.r, color.g, color.b, parentAlpha);
		batch.draw(region, getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), getScaleX(), getScaleY(),
			getRotation());
	}

}
