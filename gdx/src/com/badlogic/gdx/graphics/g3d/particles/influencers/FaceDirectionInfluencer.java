package com.badlogic.gdx.graphics.g3d.particles.influencers;

import com.badlogic.gdx.graphics.g3d.particles.ModelInstanceParticle;
import com.badlogic.gdx.graphics.g3d.particles.ParticleControllerParticle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

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


	@Override
	public void write (Json json) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void read (Json json, JsonValue jsonData) {
		// TODO Auto-generated method stub
		
	}
	
}
