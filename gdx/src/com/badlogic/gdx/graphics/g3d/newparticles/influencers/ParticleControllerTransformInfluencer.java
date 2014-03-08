package com.badlogic.gdx.graphics.g3d.newparticles.influencers;

import com.badlogic.gdx.graphics.g3d.newparticles.ModelInstanceParticle;
import com.badlogic.gdx.graphics.g3d.newparticles.Particle;
import com.badlogic.gdx.graphics.g3d.newparticles.ParticleController;
import com.badlogic.gdx.graphics.g3d.newparticles.ParticleControllerParticle;
import com.badlogic.gdx.graphics.g3d.newparticles.attributes.VelocityAttribute;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

public class ParticleControllerTransformInfluencer extends Influencer<ParticleControllerTransformInfluencer> {
	private static final Quaternion TMP_Q = new Quaternion();
	int particleControllerIndex, velIndex;
	
	@Override
	public void bind (ParticleController particleController) {
		super.bind(particleController);
		particleControllerIndex = particleController.particleAttributes.getAttributePosition(ParticleControllerParticle.class);
		velIndex = particleController.particleAttributes.getAttributePosition(VelocityAttribute.class);
	}

	@Override
	public void update () {
		for (int i = 0, count = controller.emitter.activeCount; i < count; ++i) {
			Particle particle = controller.particles[i];
			ParticleControllerParticle particleControllerAttribute = (ParticleControllerParticle)particle.data[particleControllerIndex];
			/*
			particleControllerAttribute.controller.setTransform(TMP_M4.set(TMP_V1.set(particleControllerAttribute.x, particleControllerAttribute.y, particleControllerAttribute.z), 
																							TMP_Q, 
																							TMP_V2.set(particleControllerAttribute.scale, particleControllerAttribute.scale, particleControllerAttribute.scale )));
			*/
			particleControllerAttribute.controller.update(controller.deltaTime);
		}
	}
	
	@Override
	public ParticleControllerTransformInfluencer copy () {
		return new ParticleControllerTransformInfluencer();
	}

}
