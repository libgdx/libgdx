package com.badlogic.gdx.graphics.g3d.newparticles.influencers;

import com.badlogic.gdx.graphics.g3d.newparticles.ModelInstanceParticle;
import com.badlogic.gdx.graphics.g3d.newparticles.Particle;
import com.badlogic.gdx.graphics.g3d.newparticles.ParticleController;
import com.badlogic.gdx.graphics.g3d.newparticles.ParticleControllerParticle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

public class FaceDirectionInfluencer<T> extends Influencer<T> {

	public static class ModelInstanceFaceDirectionInfluencer extends FaceDirectionInfluencer<ModelInstanceParticle>{
		@Override
		public void update () {
			for (int i = 0, count = controller.emitter.activeCount; i < count; ++i) {
				ModelInstanceParticle particle = controller.particles[i];
				Vector3 	axisZ = TMP_V1.set(particle.velocity).nor(),
							axisY = TMP_V2.set(TMP_V1).crs(Vector3.Y).nor().crs(TMP_V1).nor(),
							axisX = TMP_V3.set(axisY).crs(axisZ).nor();
				//Transposed
				particle.rotation.setFromAxes(false, 	axisX.x,  axisY.x, axisZ.x,
																	axisX.y,  axisY.y, axisZ.y,
																	axisX.z,  axisY.z, axisZ.z);
			}
		}
	}
	
	public static class ParticleControllerFaceDirectionInfluencer extends FaceDirectionInfluencer<ParticleControllerParticle>{
		@Override
		public void update () {
			for (int i = 0, count = controller.emitter.activeCount; i < count; ++i) {
				ParticleControllerParticle particle = controller.particles[i];
				Vector3 	axisZ = TMP_V1.set(particle.velocity).nor(),
							axisY = TMP_V2.set(TMP_V1).crs(Vector3.Y).nor().crs(TMP_V1).nor(),
							axisX = TMP_V3.set(axisY).crs(axisZ).nor();
				//Transposed
				particle.rotation.setFromAxes(false, 	axisX.x,  axisY.x, axisZ.x,
																	axisX.y,  axisY.y, axisZ.y,
																	axisX.z,  axisY.z, axisZ.z);
			}
		}
	}
	

	@Override
	public FaceDirectionInfluencer copy () {
		return new FaceDirectionInfluencer();
	}
	
}
