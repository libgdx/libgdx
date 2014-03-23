package com.badlogic.gdx.graphics.g3d.particles.renderers;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.particles.ParticleController;
import com.badlogic.gdx.graphics.g3d.particles.ResourceData;

/** Common interface to all the batches that render particles. */
public interface IParticleBatch<T> extends RenderableProvider, ResourceData.Configurable {
	
	/** Must be called once before any drawing operation*/
	public void begin();
	public <K extends ParticleController<T>> void draw (K controller);
	/** Must be called after all the drawing operations */
	public void end();

	public void save (AssetManager manager, ResourceData assetDependencyData);
	public void load (AssetManager manager, ResourceData assetDependencyData);
}
