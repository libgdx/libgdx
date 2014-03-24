package com.badlogic.gdx.graphics.g3d.particles.batches;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.particles.ModelInstanceParticle;
import com.badlogic.gdx.graphics.g3d.particles.ParticleController;
import com.badlogic.gdx.graphics.g3d.particles.ResourceData;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pool;

/** @author Inferno */
public class ModelInstanceParticleBatch implements ParticleBatch<ModelInstanceParticle> {
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

	@Override
	public void save (AssetManager manager, ResourceData assetDependencyData) {}

	@Override
	public void load (AssetManager manager, ResourceData assetDependencyData) {}
}
