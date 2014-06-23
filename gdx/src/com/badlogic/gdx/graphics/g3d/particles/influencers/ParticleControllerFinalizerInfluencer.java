package com.badlogic.gdx.graphics.g3d.particles.influencers;

import com.badlogic.gdx.graphics.g3d.particles.ParallelArray.FloatChannel;
import com.badlogic.gdx.graphics.g3d.particles.ParallelArray.ObjectChannel;
import com.badlogic.gdx.graphics.g3d.particles.ParticleChannels;
import com.badlogic.gdx.graphics.g3d.particles.ParticleController;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** It's an {@link Influencer} which updates the simulation of particles containing a {@link ParticleController}.
 * Must be the last influencer to be updated, so it has to be placed at the end 
 * of the influencers list when creating a {@link ParticleController}.
 * @author Inferno */
public class ParticleControllerFinalizerInfluencer extends Influencer {
	FloatChannel positionChannel, scaleChannel, rotationChannel;
	ObjectChannel<ParticleController> controllerChannel;
	boolean hasScale, hasRotation;
	
	public ParticleControllerFinalizerInfluencer()	{}
	
	@Override
	public void init () {
		controllerChannel = controller.particles.getChannel(ParticleChannels.ParticleController);
		if(controllerChannel == null)
			throw new GdxRuntimeException("ParticleController channel not found, specify an influencer which will allocate it please.");
		scaleChannel = controller.particles.getChannel(ParticleChannels.Scale);
		rotationChannel = controller.particles.getChannel(ParticleChannels.Rotation3D);
		hasScale = scaleChannel != null;
		hasRotation = rotationChannel != null;
	}

	@Override
	public void allocateChannels () {
		positionChannel = controller.particles.addChannel(ParticleChannels.Position);
	}

	@Override
	public void update () {
		for(int i=0, positionOffset = 0, c = controller.particles.size;
			i< c; 
			++i, positionOffset += positionChannel.strideSize){
			ParticleController particleController = controllerChannel.data[i];
			float scale = hasScale ? scaleChannel.data[i] : 1;
			float qx=0, qy=0, qz=0, qw=1;
			if(hasRotation){
				int rotationOffset = i* rotationChannel.strideSize;
				qx = rotationChannel.data[rotationOffset + ParticleChannels.XOffset];
				qy = rotationChannel.data[rotationOffset + ParticleChannels.YOffset]; 
				qz = rotationChannel.data[rotationOffset + ParticleChannels.ZOffset];
				qw = rotationChannel.data[rotationOffset + ParticleChannels.WOffset];
			}
			particleController.setTransform(	positionChannel.data[positionOffset + ParticleChannels.XOffset],
				positionChannel.data[positionOffset + ParticleChannels.YOffset],
				positionChannel.data[positionOffset + ParticleChannels.ZOffset],
				qx,qy,qz,qw, scale);
			particleController.update();
		}
	}
	@Override
	public ParticleControllerFinalizerInfluencer copy () {
		return new ParticleControllerFinalizerInfluencer();
	}
}
