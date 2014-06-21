package com.badlogic.gdx.graphics.g3d.particles;

import java.util.Arrays;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.particles.ParallelArray.ChannelDescriptor;
import com.badlogic.gdx.graphics.g3d.particles.ParallelArray.ChannelInitializer;
import com.badlogic.gdx.graphics.g3d.particles.ParallelArray.FloatChannel;

/** This contains all the definitions of particle related channels and channel initializers.
 * It is also used by the {@link ParticleController} to handle temporary channels allocated by influencers.
 * @author inferno */
public class ParticleChannels {
	private static int currentGlobalId;
	public static int newGlobalId(){
		return currentGlobalId++;
	}
	
	//Initializers
	public static class TextureRegionInitializer implements ChannelInitializer<FloatChannel>{
		private static TextureRegionInitializer instance;
		public static TextureRegionInitializer get(){
				if(instance == null)
					instance = new TextureRegionInitializer();
				return instance;
		}
		
		@Override
		public void init (FloatChannel channel) {
			for(int i=0, c = channel.data.length; i < c; i+= channel.strideSize){
				channel.data[i + ParticleChannels.UOffset] = 0;
				channel.data[i + ParticleChannels.VOffset] = 0;
				channel.data[i + ParticleChannels.U2Offset] = 1;
				channel.data[i + ParticleChannels.V2Offset] = 1;
				channel.data[i + ParticleChannels.HalfWidthOffset] = 0.5f;
				channel.data[i + ParticleChannels.HalfHeightOffset] = 0.5f;
			}
		}
	}

	public static class ColorInitializer implements ChannelInitializer<FloatChannel>{
		private static ColorInitializer instance;
		public static ColorInitializer get(){
			if(instance == null)
				instance = new ColorInitializer();
			return instance;
		}
		@Override
		public void init (FloatChannel channel) {
			Arrays.fill(channel.data, 0, channel.data.length, 1);
		}
	}

	public static class ScaleInitializer implements ChannelInitializer<FloatChannel>{
		private static ScaleInitializer instance;
		public static ScaleInitializer get(){
			if(instance == null)
				instance = new ScaleInitializer();
			return instance;
		}
		@Override
		public void init (FloatChannel channel) {
			Arrays.fill(channel.data, 0, channel.data.length, 1);
		}
	}
	
	public static class Rotation2dInitializer implements ChannelInitializer<FloatChannel>{
		private static Rotation2dInitializer instance;
		public static Rotation2dInitializer get(){
			if(instance == null)
				instance = new Rotation2dInitializer();
			return instance;
		}
		@Override
		public void init (FloatChannel channel) {
			for(int i=0, c = channel.data.length; i < c; i+= channel.strideSize){
				channel.data[i + ParticleChannels.CosineOffset] = 1;
				channel.data[i + ParticleChannels.SineOffset] = 0;
			}
		}
	}
	
	public static class Rotation3dInitializer implements ChannelInitializer<FloatChannel>{
		private static Rotation3dInitializer instance;
		public static Rotation3dInitializer get(){
			if(instance == null)
				instance = new Rotation3dInitializer();
			return instance;
		}
		@Override
		public void init (FloatChannel channel) {
			for(int i=0, c = channel.data.length; i < c; i+= channel.strideSize){
				channel.data[i + ParticleChannels.XOffset] = 
				channel.data[i + ParticleChannels.YOffset] = 
				channel.data[i + ParticleChannels.ZOffset] = 0;
				channel.data[i + ParticleChannels.WOffset] = 1;
			}
		}
	}

	//Channels
	/** Channels of common use like position, life, color, etc...*/
	public static final ChannelDescriptor Life = new ChannelDescriptor(newGlobalId(), float.class, 3);
	public static final ChannelDescriptor Position = new ChannelDescriptor(newGlobalId(), float.class, 3); //gl units
	public static final ChannelDescriptor PreviousPosition = new ChannelDescriptor(newGlobalId(), float.class, 3);
	public static final ChannelDescriptor Color = new ChannelDescriptor(newGlobalId(), float.class, 4);
	public static final ChannelDescriptor TextureRegion = new ChannelDescriptor(newGlobalId(), float.class, 6);
	public static final ChannelDescriptor Rotation2D = new ChannelDescriptor(newGlobalId(), float.class, 2);
	public static final ChannelDescriptor Rotation3D = new ChannelDescriptor(newGlobalId(), float.class, 4);
	public static final ChannelDescriptor Scale = new ChannelDescriptor(newGlobalId(), float.class, 1);
	public static final ChannelDescriptor ModelInstance = new ChannelDescriptor(newGlobalId(), ModelInstance.class, 1);
	public static final ChannelDescriptor ParticleController = new ChannelDescriptor(newGlobalId(), ParticleController.class, 1);
	public static final ChannelDescriptor Acceleration = new ChannelDescriptor(newGlobalId(), float.class, 3); //gl units/s2
	public static final ChannelDescriptor AngularVelocity2D = new ChannelDescriptor(newGlobalId(), float.class, 1);
	public static final ChannelDescriptor AngularVelocity3D = new ChannelDescriptor(newGlobalId(), float.class, 3);
	public static final ChannelDescriptor Interpolation = new ChannelDescriptor(-1, float.class, 2);
	public static final ChannelDescriptor Interpolation4 = new ChannelDescriptor(-1, float.class, 4);
	public static final ChannelDescriptor Interpolation6 = new ChannelDescriptor(-1, float.class, 6);
	
	//Offsets
	/** Offsets to acess a particular value inside a stride of a given channel */
	public static final int CurrentLifeOffset = 0, TotalLifeOffset = 1, LifePercentOffset =2;
	public static final int RedOffset = 0, GreenOffset =1, BlueOffset = 2, AlphaOffset =3;
	public static final int InterpolationStartOffset = 0, InterpolationDiffOffset =1;
	public static final int VelocityStrengthStartOffset = 0, VelocityStrengthDiffOffset =1,
												VelocityThetaStartOffset = 0, VelocityThetaDiffOffset =1,
												VelocityPhiStartOffset = 2, VelocityPhiDiffOffset =3;
	public static final int XOffset = 0, YOffset = 1, ZOffset = 2, WOffset = 3;
	public static final int UOffset = 0, VOffset= 1, U2Offset = 2, V2Offset = 3,
												HalfWidthOffset = 4, HalfHeightOffset= 5;
	public static final int CosineOffset = 0, SineOffset= 1;

	private int currentId; 

	public ParticleChannels(){
		resetIds();
	}
	
	public int newId(){
		return currentId++;
	}
	
	protected void resetIds(){
		currentId = currentGlobalId;
	}
	
}
