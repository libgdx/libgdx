package com.badlogic.gdx.graphics.g3d.particles.batches;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.particles.ResourceData;
import com.badlogic.gdx.graphics.g3d.particles.renderers.ParticleControllerRenderData;

/** Common interface to all the batches that render particles. 
 * @author Inferno */
public interface ParticleBatch<T extends ParticleControllerRenderData> extends RenderableProvider, ResourceData.Configurable {
	
	/** Must be called once before any drawing operation*/
	public void begin();
	public void draw (T controller);
	/** Must be called after all the drawing operations */
	public void end();

	public void save (AssetManager manager, ResourceData assetDependencyData);
	public void load (AssetManager manager, ResourceData assetDependencyData);
}
