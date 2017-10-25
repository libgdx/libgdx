
package com.badlogic.gdx.graphics.g3d.particles.influencers;

import com.badlogic.gdx.graphics.g3d.particles.ParallelArray.FloatChannel;
import com.badlogic.gdx.graphics.g3d.particles.ParticleChannels;
import com.badlogic.gdx.math.MathUtils;

/** @author ryanastout */
public class CircularMotion extends DynamicsModifier.Angular {
	FloatChannel positionChannel;
	FloatChannel previousPositionChannel;
	FloatChannel accelerationChannel;

	public CircularMotion () {

	}

	public CircularMotion (CircularMotion motion) {
		super(motion);
	}

	@Override
	public void allocateChannels () {
		super.allocateChannels();
		positionChannel = controller.particles.addChannel(ParticleChannels.Position);
		previousPositionChannel = controller.particles.addChannel(ParticleChannels.PreviousPosition);
		accelerationChannel = controller.particles.addChannel(ParticleChannels.Acceleration);
	}

	@Override
	public void activateParticles (int startIndex, int count) {
		super.activateParticles(startIndex, count);
		for (int i = startIndex; i < startIndex + count; ++i) {
			int s = i * strengthChannel.strideSize;
			int l = i * lifeChannel.strideSize;
			int a = i * angularChannel.strideSize;
			int posI = positionChannel.strideSize * i;
			int prevPosI = previousPositionChannel.strideSize * i;

			float lifePercent = lifeChannel.data[ParticleChannels.LifePercentOffset + l];
			float speed = strengthChannel.data[s + ParticleChannels.VelocityStrengthStartOffset]
				+ strengthChannel.data[s + ParticleChannels.VelocityStrengthDiffOffset] * strengthValue.getScale(lifePercent);
			float phi = angularChannel.data[a + ParticleChannels.VelocityPhiStartOffset]
				+ angularChannel.data[a + ParticleChannels.VelocityPhiDiffOffset] * phiValue.getScale(lifePercent);
			float theta = angularChannel.data[a + ParticleChannels.VelocityThetaStartOffset]
				+ angularChannel.data[a + ParticleChannels.VelocityThetaDiffOffset] * thetaValue.getScale(lifePercent);
			float cosTheta = MathUtils.cosDeg(theta);
			float sinTheta = MathUtils.sinDeg(theta);
			float cosPhi = MathUtils.cosDeg(phi);
			float sinPhi = MathUtils.sinDeg(phi);

			TMP_V1.set(positionChannel.data[posI + ParticleChannels.XOffset], positionChannel.data[posI + ParticleChannels.YOffset],
				positionChannel.data[posI + ParticleChannels.ZOffset]);
			TMP_V3.set(cosTheta * sinPhi, cosPhi, sinTheta * sinPhi);

			if (!isGlobal) {
				TMP_V3.mul(controller.transform.getRotation(TMP_Q, true));
				TMP_V1.sub(controller.transform.getTranslation(TMP_V5));
			}
			// TMP_V1 is position
			// TMP_V3 is axis of rotation
			TMP_V4.set(TMP_V3).crs(TMP_V1); // TMP_V4 is now the direction of velocity (axis cross position)
			TMP_V5.set(TMP_V3).scl(TMP_V1.dot(TMP_V3)); // TMP_V5 is now the projection of position onto the axis
			TMP_V6.set(TMP_V5).sub(TMP_V1); // TMP_V6 is now the vector from the particle to the closest point on the axis

			float radius = TMP_V6.len();
			if (radius > 0) {
				// for uniform circular motion, the magnitude of the force is toward the center of the circle and with this
				// magnitude
				float forceMag = speed * speed / radius;

				// initialization of Verlet integration says:
				// previous_position = pos -(initial_velocity * time_delta + .5 * time_delta_sq * force)
				// TMP_V6 is the vector from the position to the axis, so TMP_V6.nor() is the direction in which force is applied
				// TMP_V4 is the direction of the particle's initial velocity
				TMP_V4.nor().scl(speed * controller.deltaTime);
				TMP_V6.nor().scl((float)(forceMag * .5 * controller.deltaTimeSqr));

				previousPositionChannel.data[prevPosI + ParticleChannels.XOffset] -= (TMP_V4.x + TMP_V6.x);
				previousPositionChannel.data[prevPosI + ParticleChannels.YOffset] -= (TMP_V4.y + TMP_V6.y);
				previousPositionChannel.data[prevPosI + ParticleChannels.ZOffset] -= (TMP_V4.z + TMP_V6.z);
			}
		}

	}

	@Override
	public void update () {
		for (int i = 0, l = ParticleChannels.LifePercentOffset, s = 0, a = 0, positionOffset = 0, c = i + controller.particles.size
			* accelerationChannel.strideSize; i < c; s += strengthChannel.strideSize, i += accelerationChannel.strideSize, a += angularChannel.strideSize, l += lifeChannel.strideSize, positionOffset += positionChannel.strideSize) {

			float lifePercent = lifeChannel.data[l], strength = strengthChannel.data[s
				+ ParticleChannels.VelocityStrengthStartOffset]
				+ strengthChannel.data[s + ParticleChannels.VelocityStrengthDiffOffset] * strengthValue.getScale(lifePercent), phi = angularChannel.data[a
				+ ParticleChannels.VelocityPhiStartOffset]
				+ angularChannel.data[a + ParticleChannels.VelocityPhiDiffOffset] * phiValue.getScale(lifePercent), theta = angularChannel.data[a
				+ ParticleChannels.VelocityThetaStartOffset]
				+ angularChannel.data[a + ParticleChannels.VelocityThetaDiffOffset] * thetaValue.getScale(lifePercent);

			float cosTheta = MathUtils.cosDeg(theta), sinTheta = MathUtils.sinDeg(theta), cosPhi = MathUtils.cosDeg(phi), sinPhi = MathUtils
				.sinDeg(phi);

			TMP_V3.set(cosTheta * sinPhi, cosPhi, sinTheta * sinPhi);

			TMP_V1.set(positionChannel.data[positionOffset + ParticleChannels.XOffset], positionChannel.data[positionOffset
				+ ParticleChannels.YOffset], positionChannel.data[positionOffset + ParticleChannels.ZOffset]);
			if (!isGlobal) {
				TMP_V1.sub(controller.transform.getTranslation(TMP_V2));
				TMP_V3.mul(controller.transform.getRotation(TMP_Q, true));
			}
			// TMP_V1 is the position of the particle (relative to its center if isGlobal is false)
			// TMP_V3 is the axis of rotation (rotated by the same rotation applied to the controller if isGlobal is false)

			TMP_V4.set(TMP_V3).crs(TMP_V1); // TMP_V4 is now the direction of velocity (axis cross position)
			TMP_V5.set(TMP_V3).scl(TMP_V1.dot(TMP_V3)); // TMP_V5 is now the projection of position onto the axis
			TMP_V6.set(TMP_V5).sub(TMP_V1); // TMP_V6 is now the vector from the particle to the closest point on the axis

			float radius = TMP_V6.len();
			if (radius > 0) {
				TMP_V6.nor().scl(strength * strength / radius);
				accelerationChannel.data[i + ParticleChannels.XOffset] += TMP_V6.x;
				accelerationChannel.data[i + ParticleChannels.YOffset] += TMP_V6.y;
				accelerationChannel.data[i + ParticleChannels.ZOffset] += TMP_V6.z;
			}
		}
	}

	@Override
	public CircularMotion copy () {
		return new CircularMotion(this);
	}
}
