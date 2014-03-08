package com.badlogic.gdx.graphics.g3d.particles.renderers;

import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.particles.BillboardParticle;
import com.badlogic.gdx.graphics.g3d.particles.ModelInstanceParticle;
import com.badlogic.gdx.graphics.g3d.particles.Particle;
import com.badlogic.gdx.graphics.g3d.particles.ParticleController;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

public class ModelInstanceRenderer extends Renderer<ModelInstanceParticle>{
	@Override
	public void getRenderables (Array<Renderable> renderables, Pool<Renderable> pool) {
		ModelInstanceParticle[] particles = controller.particles;
		for (int i = 0, count = controller.emitter.activeCount; i < count; ++i) {
			ModelInstanceParticle particle = particles[i];
			particle.instance.transform.set(	particle.instance.transform.getTranslation(TMP_V1), 
														particle.rotation, 
														TMP_V2.set(particle.scale, particle.scale, particle.scale));
			particle.instance.getRenderables(renderables, pool);
		}
	}
	@Override
	public ModelInstanceRenderer copy () {
		return new ModelInstanceRenderer();
	}
}
