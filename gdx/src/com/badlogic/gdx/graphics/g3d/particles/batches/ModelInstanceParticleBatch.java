package com.badlogic.gdx.graphics.g3d.particles.batches;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.particles.ResourceData;
import com.badlogic.gdx.graphics.g3d.particles.renderers.ModelInstanceControllerRenderData;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

/*** This class is used to render particles having a model instance channel.
 * @author Inferno */
public class ModelInstanceParticleBatch implements ParticleBatch<ModelInstanceControllerRenderData> {
	Array<ModelInstanceControllerRenderData> controllersRenderData;
	int bufferedParticlesCount;
	public ModelInstanceParticleBatch () {
		controllersRenderData = new Array<ModelInstanceControllerRenderData>(false, 5);
	}

	@Override
	public void getRenderables (Array<Renderable> renderables, Pool<Renderable> pool) {
		for(ModelInstanceControllerRenderData data : controllersRenderData){
			for(int i=0, count = data.controller.particles.size; i < count; ++i){
				data.modelInstanceChannel.data[i].getRenderables(renderables, pool);
			}
		}
	}
	
	public int getBufferedCount(){
		return bufferedParticlesCount;
	}

	@Override
	public void begin () {
		controllersRenderData.clear();
		bufferedParticlesCount = 0;
	}
	
	@Override
	public void end () {}

	@Override
	public void draw (ModelInstanceControllerRenderData data) {
		controllersRenderData.add(data);
		bufferedParticlesCount += data.controller.particles.size;
	}

	@Override
	public void save (AssetManager manager, ResourceData assetDependencyData) {}

	@Override
	public void load (AssetManager manager, ResourceData assetDependencyData) {}
}
