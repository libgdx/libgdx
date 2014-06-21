package com.badlogic.gdx.graphics.g3d.particles.renderers;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.particles.ParticleChannels;
import com.badlogic.gdx.graphics.g3d.particles.ParticleControllerComponent;
import com.badlogic.gdx.graphics.g3d.particles.batches.BillboardParticleBatch;
import com.badlogic.gdx.graphics.g3d.particles.batches.ModelInstanceParticleBatch;
import com.badlogic.gdx.graphics.g3d.particles.batches.ParticleBatch;

/** A {@link ParticleControllerRenderer} which will render particles 
 * as {@link ModelInstance} to a {@link ModelInstanceParticleBatch}.
 * @author Inferno */
public class ModelInstanceRenderer extends ParticleControllerRenderer<ModelInstanceControllerRenderData, ModelInstanceParticleBatch> {
	private boolean hasColor, hasScale, hasRotation;
	public ModelInstanceRenderer(){
		super(new ModelInstanceControllerRenderData());
	}
	
	public ModelInstanceRenderer(ModelInstanceParticleBatch batch){
		this();
		setBatch(batch);
	}
	
	@Override
	public void allocateChannels () {
		renderData.positionChannel = controller.particles.addChannel(ParticleChannels.Position);
	}
	
	@Override
	public void init () {
		renderData.modelInstanceChannel = controller.particles.getChannel(ParticleChannels.ModelInstance);
		renderData.colorChannel = controller.particles.getChannel(ParticleChannels.Color);
		renderData.scaleChannel = controller.particles.getChannel(ParticleChannels.Scale);
		renderData.rotationChannel = controller.particles.getChannel(ParticleChannels.Rotation3D);
		hasColor = renderData.colorChannel != null;
		hasScale = renderData.scaleChannel != null;
		hasRotation = renderData.rotationChannel != null;
	}

	@Override
	public void update () {
		for(int i=0, positionOffset = 0, c = controller.particles.size;
			i< c; 
			++i, positionOffset += renderData.positionChannel.strideSize){
			ModelInstance instance = renderData.modelInstanceChannel.data[i];
			float scale = hasScale ? renderData.scaleChannel.data[i] : 1;
			float qx=0, qy=0, qz=0, qw=1;
			if(hasRotation){
				int rotationOffset = i* renderData.rotationChannel.strideSize;
				qx = renderData.rotationChannel.data[rotationOffset + ParticleChannels.XOffset];
				qy = renderData.rotationChannel.data[rotationOffset + ParticleChannels.YOffset]; 
				qz = renderData.rotationChannel.data[rotationOffset + ParticleChannels.ZOffset];
				qw = renderData.rotationChannel.data[rotationOffset + ParticleChannels.WOffset];
			}
			
			instance.transform.set(	renderData.positionChannel.data[positionOffset + ParticleChannels.XOffset],
				renderData.positionChannel.data[positionOffset + ParticleChannels.YOffset],
				renderData.positionChannel.data[positionOffset + ParticleChannels.ZOffset],
				qx, qy, qz, qw,
				scale, scale, scale);
			if(hasColor){
				int colorOffset = i*renderData.colorChannel.strideSize;
				ColorAttribute colorAttribute = (ColorAttribute)instance.materials.get(0).get(ColorAttribute.Diffuse);
				BlendingAttribute blendingAttribute = (BlendingAttribute)instance.materials.get(0).get(BlendingAttribute.Type);
				colorAttribute.color.r = renderData.colorChannel.data[colorOffset +ParticleChannels.RedOffset];
				colorAttribute.color.g = renderData.colorChannel.data[colorOffset +ParticleChannels.GreenOffset];
				colorAttribute.color.b = renderData.colorChannel.data[colorOffset +ParticleChannels.BlueOffset];
				if(blendingAttribute != null)
					blendingAttribute.opacity  = renderData.colorChannel.data[colorOffset +ParticleChannels.AlphaOffset];
			}
		}
		super.update();
	}
	
	@Override
	public ParticleControllerComponent copy () {
		return new ModelInstanceRenderer(batch);
	}

	@Override
	public boolean isCompatible (ParticleBatch<?> batch) {
		return batch instanceof ModelInstanceParticleBatch;
	}

}
