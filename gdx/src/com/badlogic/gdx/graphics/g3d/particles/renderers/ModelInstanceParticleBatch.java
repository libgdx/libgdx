package com.badlogic.gdx.graphics.g3d.particles.renderers;

import java.util.Comparator;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.particles.ModelInstanceParticle;
import com.badlogic.gdx.graphics.g3d.particles.ParticleController;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

public class ModelInstanceParticleBatch implements IParticleBatch<ModelInstanceParticle> {
	Array<ParticleController<ModelInstanceParticle>> controllers;
	public ModelInstanceParticleBatch () {
		controllers = new Array<ParticleController<ModelInstanceParticle>>(false, 5);
	}

	@Override
	public void getRenderables (Array<Renderable> renderables, Pool<Renderable> pool) {
		for(ParticleController<ModelInstanceParticle> controller : controllers){
			for(int i=0, count = controller.emitter.activeCount; i < count; ++i){
				controller.particles[i].instance.getRenderables(renderables, pool);
			}
		}
	}

	@Override
	public void begin () {
		controllers.clear();
	}
	
	@Override
	public void end () {}

	@Override
	public <K extends ParticleController<ModelInstanceParticle>> void draw (K controller) {
		controllers.add(controller);
	}
}
