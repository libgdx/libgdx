package com.badlogic.gdx.graphics.g3d.particles.influencers;

import java.util.Arrays;

import com.badlogic.gdx.graphics.g3d.particles.ParallelArray.FloatChannel;
import com.badlogic.gdx.graphics.g3d.particles.ParticleChannels;
import com.badlogic.gdx.graphics.g3d.particles.ParticleController;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

/** It's an {@link Influencer} which controls the particles dynamics (movement, rotations).
 *  @author Inferno */
public class DynamicsInfluencer extends Influencer {
	public Array<DynamicsModifier> velocities;
	private FloatChannel 	accellerationChannel, 
												positionChannel, previousPositionChannel, 
												rotationChannel, angularVelocityChannel;
	boolean hasAcceleration, has2dAngularVelocity, has3dAngularVelocity;

	public DynamicsInfluencer(){
		this.velocities = new Array<DynamicsModifier>(true, 3, DynamicsModifier.class);
	}

	public DynamicsInfluencer(DynamicsModifier...velocities){
		this.velocities = new Array<DynamicsModifier>(true, velocities.length, DynamicsModifier.class);
		for(DynamicsModifier value : velocities){
			this.velocities.add((DynamicsModifier)value.copy());
		}
	}
	
	public DynamicsInfluencer (DynamicsInfluencer velocityInfluencer) {
		this((DynamicsModifier[])velocityInfluencer.velocities.toArray(DynamicsModifier.class));
	}
	
	@Override
	public void allocateChannels() {
		for(int k=0; k < velocities.size; ++k){
			velocities.items[k].allocateChannels();
		}
		
		//Hack, shouldn't be done but after all the modifiers allocated their channels
		//it's possible to check if we need to allocate previous position channel
		accellerationChannel = controller.particles.getChannel(ParticleChannels.Acceleration);
		hasAcceleration = accellerationChannel != null;
		if(hasAcceleration){
			positionChannel = controller.particles.addChannel(ParticleChannels.Position);
			previousPositionChannel = controller.particles.addChannel(ParticleChannels.PreviousPosition);
		}
		
		//Angular velocity check
		angularVelocityChannel = controller.particles.getChannel(ParticleChannels.AngularVelocity2D);
		has2dAngularVelocity = angularVelocityChannel != null;
		if(has2dAngularVelocity){
			rotationChannel = controller.particles.addChannel(ParticleChannels.Rotation2D);
			has3dAngularVelocity = false;
		}
		else{
			angularVelocityChannel = controller.particles.getChannel(ParticleChannels.AngularVelocity3D);
			has3dAngularVelocity = angularVelocityChannel != null;
			if(has3dAngularVelocity)
				rotationChannel = controller.particles.addChannel(ParticleChannels.Rotation3D);
		}
	}
	
	@Override
	public void set(ParticleController particleController) {
		super.set(particleController);
		for(int k=0; k < velocities.size; ++k){
			velocities.items[k].set(particleController);
		}
	}
	
	@Override
	public void init () {
		for(int k=0; k < velocities.size; ++k){
			velocities.items[k].init();
		}
	}

	public void activateParticles (int startIndex, int count) {
		if(hasAcceleration){
			//Previous position is the current position
			//Attention, this requires that some other influencer setting the position channel must execute before this influencer.
			for(int i=startIndex*positionChannel.strideSize, c = i +count*positionChannel.strideSize; i< c;  i+= positionChannel.strideSize){
				previousPositionChannel.data[i+ParticleChannels.XOffset] = positionChannel.data[i+ParticleChannels.XOffset];
				previousPositionChannel.data[i+ParticleChannels.YOffset] = positionChannel.data[i+ParticleChannels.YOffset];
				previousPositionChannel.data[i+ParticleChannels.ZOffset] = positionChannel.data[i+ParticleChannels.ZOffset];
				/*
				//Euler intialization
				previousPositionChannel.data[i+ParticleChannels.XOffset] = 
				previousPositionChannel.data[i+ParticleChannels.YOffset] = 
				previousPositionChannel.data[i+ParticleChannels.ZOffset] = 0;
				*/
			}	
		}
		
		if(has2dAngularVelocity){
			//Rotation back to 0
			for(int i=startIndex*rotationChannel.strideSize, c = i +count*rotationChannel.strideSize; i< c;  i+= rotationChannel.strideSize){
				rotationChannel.data[i+ParticleChannels.CosineOffset] = 1;
				rotationChannel.data[i+ParticleChannels.SineOffset] = 0;
			}	
		}
		else if(has3dAngularVelocity){
			//Rotation back to 0
			for(int i=startIndex*rotationChannel.strideSize, c = i +count*rotationChannel.strideSize; i< c;  i+= rotationChannel.strideSize){
				rotationChannel.data[i+ParticleChannels.XOffset] = 0;
				rotationChannel.data[i+ParticleChannels.YOffset] = 0;
				rotationChannel.data[i+ParticleChannels.ZOffset] = 0;
				rotationChannel.data[i+ParticleChannels.WOffset] = 1;
			}	
		}
		
		for(int k=0; k < velocities.size; ++k){
			velocities.items[k].activateParticles(startIndex, count);
		}
	}
	
	public void update(){
		//Clean previouse frame velocities
		if(hasAcceleration)
			Arrays.fill(accellerationChannel.data, 0, controller.particles.size*accellerationChannel.strideSize, 0);
		if(has2dAngularVelocity || has3dAngularVelocity) 
			Arrays.fill(angularVelocityChannel.data, 0, controller.particles.size*angularVelocityChannel.strideSize, 0);

		//Sum all the forces/accelerations
		for(int k=0; k < velocities.size; ++k){
			velocities.items[k].update();
		}
		
		//Apply the forces
		if(hasAcceleration){
			/*
			 //Euler Integration
			for(int 	i=0, offset = 0; i < controller.particles.size; ++i, offset +=positionChannel.strideSize){
				previousPositionChannel.data[offset + ParticleChannels.XOffset] += accellerationChannel.data[offset + ParticleChannels.XOffset]*controller.deltaTime;
				previousPositionChannel.data[offset + ParticleChannels.YOffset] += accellerationChannel.data[offset + ParticleChannels.YOffset]*controller.deltaTime;
				previousPositionChannel.data[offset + ParticleChannels.ZOffset] += accellerationChannel.data[offset + ParticleChannels.ZOffset]*controller.deltaTime;
				
				positionChannel.data[offset + ParticleChannels.XOffset] += previousPositionChannel.data[offset + ParticleChannels.XOffset]*controller.deltaTime;
				positionChannel.data[offset + ParticleChannels.YOffset] += previousPositionChannel.data[offset + ParticleChannels.YOffset]*controller.deltaTime;
				positionChannel.data[offset + ParticleChannels.ZOffset] += previousPositionChannel.data[offset + ParticleChannels.ZOffset]*controller.deltaTime;
			}
			*/
			//Verlet integration
			for(int 	i=0, offset = 0; i < controller.particles.size; ++i, offset +=positionChannel.strideSize){
				float 	x = positionChannel.data[offset + ParticleChannels.XOffset],
							y = positionChannel.data[offset + ParticleChannels.YOffset],
							z = positionChannel.data[offset + ParticleChannels.ZOffset];
				positionChannel.data[offset + ParticleChannels.XOffset] = 2*x - previousPositionChannel.data[offset + ParticleChannels.XOffset] + 
									accellerationChannel.data[offset + ParticleChannels.XOffset]*controller.deltaTimeSqr;
				positionChannel.data[offset + ParticleChannels.YOffset] = 2*y- previousPositionChannel.data[offset + ParticleChannels.YOffset] + 
									accellerationChannel.data[offset + ParticleChannels.YOffset]*controller.deltaTimeSqr;
				positionChannel.data[offset + ParticleChannels.ZOffset] = 2*z - previousPositionChannel.data[offset + ParticleChannels.ZOffset] + 
									accellerationChannel.data[offset + ParticleChannels.ZOffset]*controller.deltaTimeSqr;
				previousPositionChannel.data[offset + ParticleChannels.XOffset] = x;
				previousPositionChannel.data[offset + ParticleChannels.YOffset] = y;
				previousPositionChannel.data[offset + ParticleChannels.ZOffset] = z;
			}
		}

		if(has2dAngularVelocity){
			for(int 	i=0, offset = 0; i < controller.particles.size; ++i, offset +=rotationChannel.strideSize){
				float rotation = angularVelocityChannel.data[i]*controller.deltaTime;
				if(rotation != 0){
					float cosBeta = MathUtils.cosDeg(rotation), sinBeta = MathUtils.sinDeg(rotation);
					float currentCosine = rotationChannel.data[offset + ParticleChannels.CosineOffset];
					float currentSine = rotationChannel.data[offset + ParticleChannels.SineOffset];
					float 	newCosine = currentCosine*cosBeta - currentSine*sinBeta,
						newSine = currentSine*cosBeta + currentCosine*sinBeta;
					rotationChannel.data[offset + ParticleChannels.CosineOffset] = newCosine;
					rotationChannel.data[offset + ParticleChannels.SineOffset] = newSine;
				}
			}
		}		
		else if(has3dAngularVelocity){
			for(int 	i=0, offset = 0, angularOffset = 0; i < controller.particles.size; ++i, 
					offset +=rotationChannel.strideSize, angularOffset += angularVelocityChannel.strideSize){
				
				float	wx = angularVelocityChannel.data[angularOffset + ParticleChannels.XOffset],
							wy = angularVelocityChannel.data[angularOffset + ParticleChannels.YOffset],
							wz = angularVelocityChannel.data[angularOffset + ParticleChannels.ZOffset],
							qx = rotationChannel.data[offset + ParticleChannels.XOffset],
							qy = rotationChannel.data[offset + ParticleChannels.YOffset],
							qz = rotationChannel.data[offset + ParticleChannels.ZOffset],
							qw = rotationChannel.data[offset + ParticleChannels.WOffset];
				TMP_Q.set(wx, wy, wz, 0).mul(qx, qy, qz, qw).mul(0.5f*controller.deltaTime).add(qx, qy, qz, qw).nor();
				rotationChannel.data[offset + ParticleChannels.XOffset] = TMP_Q.x;
				rotationChannel.data[offset + ParticleChannels.YOffset] = TMP_Q.y;
				rotationChannel.data[offset + ParticleChannels.ZOffset] = TMP_Q.z;
				rotationChannel.data[offset + ParticleChannels.WOffset] = TMP_Q.w;
			}	
		}
	}

	@Override
	public DynamicsInfluencer copy () {
		return new DynamicsInfluencer(this);
	}
	
	@Override
	public void write (Json json) {
		json.writeValue("velocities", velocities, Array.class, DynamicsModifier.class);
	}
	
	@Override
	public void read (Json json, JsonValue jsonData) {
		velocities.addAll(json.readValue("velocities", Array.class, DynamicsModifier.class, jsonData));
	}
}
