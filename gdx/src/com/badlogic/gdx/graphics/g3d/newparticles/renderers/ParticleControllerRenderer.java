package com.badlogic.gdx.graphics.g3d.newparticles.renderers;

import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.newparticles.BillboardParticle;
import com.badlogic.gdx.graphics.g3d.newparticles.ModelInstanceParticle;
import com.badlogic.gdx.graphics.g3d.newparticles.Particle;
import com.badlogic.gdx.graphics.g3d.newparticles.ParticleController;
import com.badlogic.gdx.graphics.g3d.newparticles.ParticleControllerParticle;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

public class ParticleControllerRenderer extends Renderer<ParticleControllerParticle>{
	@Override
	public void getRenderables (Array<Renderable> renderables, Pool<Renderable> pool) {
		ParticleControllerParticle[] particles = controller.particles;
		for (int i = 0, count = controller.emitter.activeCount; i < count; ++i) {
			ParticleControllerParticle particle = particles[i];
			particle.controller.setTransform( 	TMP_M4.set(	TMP_V1.set(particle.x, particle.y, particle.z), 
																			particle.rotation, 
																			TMP_V2.set(particle.scale, particle.scale, particle.scale)) );
			particle.controller.update(controller.deltaTime);
			particle.controller.getRenderables(renderables, pool);
		}
	}

	@Override
	public ParticleControllerRenderer copy () {
		return new ParticleControllerRenderer();
	}
}
