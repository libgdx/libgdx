package com.badlogic.gdx.graphics.g3d.particles.renderers;

import com.badlogic.gdx.graphics.g3d.particles.ParticleController;
import com.badlogic.gdx.graphics.g3d.particles.ParticleControllerComponent;
import com.badlogic.gdx.graphics.g3d.particles.batches.ParticleBatch;

/** It's a {@link ParticleControllerComponent} which determines how the particles are rendered.
 * It's the base class of every particle renderer.
 * @author Inferno */
public abstract class ParticleControllerRenderer< D extends ParticleControllerRenderData, T extends ParticleBatch<D>> extends ParticleControllerComponent{
	protected T batch;
	protected D renderData;
	
	protected ParticleControllerRenderer(){}
	
	protected ParticleControllerRenderer(D renderData){
		this.renderData = renderData;
	}
	
	@Override
	public void update () {
		batch.draw(renderData);
	}
	
	@SuppressWarnings("unchecked")
	public boolean setBatch (ParticleBatch<?> batch){
		if(isCompatible(batch)){
			this.batch = (T)batch;
			return true;
		}
		return false;
	}
	
	public abstract boolean isCompatible (ParticleBatch<?> batch);

	@Override
	public void set (ParticleController particleController) {
		super.set(particleController);
		if(renderData != null)
			renderData.controller = controller;
	}
}
