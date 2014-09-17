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

package com.badlogic.gdx.tests.ai.steer.bullet;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.tests.bullet.BulletEntity;

/** @author Daniel Holderbaum */
public class SteeringBulletEntity extends BulletEntity implements Steerable<Vector3> {

	public btRigidBody body;

	float maxLinearSpeed;
	float maxLinearAcceleration;
	float maxAngularSpeed;
	float maxAngularAcceleration;
	float boundingRadius;
	boolean tagged;
	protected SteeringBehavior<Vector3> steeringBehavior;
	private static final SteeringAcceleration<Vector3> steeringOutput = new SteeringAcceleration<Vector3>(new Vector3());

	private static final Quaternion tmpQuaternion = new Quaternion();
	private static final Matrix4 tmpMatrix4 = new Matrix4();
	private static final Vector3 tmpVector3 = new Vector3();

	private static final Vector3 ANGULAR_LOCK = new Vector3(0, 1, 0);

	public SteeringBulletEntity (BulletEntity copyEntity) {
		super(copyEntity.modelInstance, copyEntity.body);

		if (!(copyEntity.body instanceof btRigidBody)) {
			throw new IllegalArgumentException("Body must be a rigid body.");
		}
// if (copyEntity.body.isStaticOrKinematicObject()) {
// throw new IllegalArgumentException("Body must be a dynamic body.");
// }

		this.body = (btRigidBody)copyEntity.body;
		body.setAngularFactor(ANGULAR_LOCK);
	}

	public SteeringBehavior<Vector3> getSteeringBehavior () {
		return steeringBehavior;
	}

	public void setSteeringBehavior (SteeringBehavior<Vector3> steeringBehavior) {
		this.steeringBehavior = steeringBehavior;
	}

	public void update () {
		if (steeringBehavior != null) {
			// Calculate steering acceleration
			steeringBehavior.steer(steeringOutput);

			// Apply steering accelerations (if any)
			boolean anyAccelerations = false;
			if (!steeringOutput.linear.isZero()) {
				body.applyCentralForce(steeringOutput.linear.scl(Gdx.graphics.getDeltaTime()));
				anyAccelerations = true;
			}
			if (steeringOutput.angular != 0) {
				body.applyTorque(tmpVector3.set(0, steeringOutput.angular * Gdx.graphics.getDeltaTime(), 0));
				anyAccelerations = true;
			}
			if (anyAccelerations) {
				body.activate();

				// TODO:
				// Looks like truncating speeds here after applying forces doesn't work as expected.
				// We should likely cap speeds form inside an InternalTickCallback, see
				// http://www.bulletphysics.org/mediawiki-1.5.8/index.php/Simulation_Tick_Callbacks

				// Cap the linear speed
				Vector3 velocity = body.getLinearVelocity();
				float currentSpeedSquare = velocity.len2();
				float maxLinearSpeed = getMaxLinearSpeed();
				if (currentSpeedSquare > maxLinearSpeed * maxLinearSpeed) {
					body.setLinearVelocity(velocity.scl(maxLinearSpeed / (float)Math.sqrt(currentSpeedSquare)));
				}

				// Cap the angular speed
				Vector3 angVelocity = body.getAngularVelocity();
				if (angVelocity.y > getMaxAngularSpeed()) {
					angVelocity.y = getMaxAngularSpeed();
					body.setAngularVelocity(angVelocity);
				}
			}
		}
	}

	@Override
	public float getOrientation () {
		transform.getRotation(tmpQuaternion, true);
		return tmpQuaternion.getYawRad();
	}

	@Override
	public Vector3 getLinearVelocity () {
		return body.getLinearVelocity();
	}

	@Override
	public float getAngularVelocity () {
		Vector3 angularVelocity = body.getAngularVelocity();
		return angularVelocity.y;
	}

	@Override
	public float getBoundingRadius () {
		// TODO: this should be calculated via the actual btShape
		return 1;
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
	public Vector3 newVector () {
		return new Vector3();
	}

	@Override
	public float vectorToAngle (Vector3 vector) {
		return (float)Math.atan2(vector.z, vector.x);
	}

	@Override
	public Vector3 angleToVector (Vector3 outVector, float angle) {
		outVector.set(MathUtils.cos(angle), 0f, MathUtils.sin(angle));
		return outVector;
	}

	@Override
	public Vector3 getPosition () {
		body.getMotionState().getWorldTransform(tmpMatrix4);
		return tmpMatrix4.getTranslation(tmpVector3);
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

}
