package com.badlogic.gdx.graphics.g3d.newparticles.values;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;

import com.badlogic.gdx.graphics.g3d.newparticles.BillboardParticle;
import com.badlogic.gdx.graphics.g3d.newparticles.ModelInstanceParticle;
import com.badlogic.gdx.graphics.g3d.newparticles.ParticleController;
import com.badlogic.gdx.graphics.g3d.newparticles.ParticleControllerParticle;
import com.badlogic.gdx.graphics.g3d.newparticles.PointParticle;
import com.badlogic.gdx.graphics.g3d.newparticles.values.VelocityDatas.CommonVelocityData;
import com.badlogic.gdx.graphics.g3d.newparticles.values.VelocityDatas.StrengthVelocityData;
import com.badlogic.gdx.graphics.g3d.newparticles.values.VelocityDatas.VelocityData;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

public final class VelocityValues {
	
	//Billboards
	
	public static class BillboardRotationVelocityValue extends StrengthVelocityValue<BillboardParticle, StrengthVelocityData>{
		@Override
		public void addVelocity (ParticleController<BillboardParticle> controller, BillboardParticle particle, StrengthVelocityData data) {
			BillboardParticle.ROTATION_ACCUMULATOR += (data.strengthStart + data.strengthDiff * strengthValue.getScale(particle.lifePercent)) * controller.deltaTime;
		}
	}
	
	public static class BillboardCentripetalVelocityValue extends StrengthVelocityValue<BillboardParticle, StrengthVelocityData>{
		public void addVelocity (ParticleController<BillboardParticle> controller, BillboardParticle particle, StrengthVelocityData velocityData){
			float strength = velocityData.strengthStart + velocityData.strengthDiff * strengthValue.getScale(particle.lifePercent);
			TMP_V3.set(particle.x, particle.y, particle.z);
			if(!isGlobal){
				float[] val = controller.transform.val;
				TMP_V3.sub(val[Matrix4.M03], val[Matrix4.M13], val[Matrix4.M23]);
			}
			particle.velocity.add(TMP_V3.nor().scl(strength* controller.deltaTime));
		}
	}
	
	public static class BillboardTangetialVelocityValue extends AngularVelocityValue<BillboardParticle>{
		public void addVelocity (ParticleController<BillboardParticle> controller, BillboardParticle particle, CommonVelocityData velocityData){
			float strength = velocityData.strengthStart + velocityData.strengthDiff * strengthValue.getScale(particle.lifePercent),
					phi = velocityData.phistart + velocityData.phiDiff * phiValue.getScale(particle.lifePercent),  
					theta = velocityData.thetaStart + velocityData.thetaDiff * thetaValue.getScale(particle.lifePercent);

			TMP_V3.set(Vector3.X).rotate(Vector3.Y, theta);
			TMP_V2.set(TMP_V3).crs(Vector3.Y);
			TMP_V3.rotate(TMP_V2, phi);
			TMP_V3.crs(TMP_V2.set(particle.x, particle.y, particle.z));
			particle.velocity.add(TMP_V3.nor().scl(strength* controller.deltaTime));
		}
	}
	
	public static class BillboardPolarVelocityValue extends AngularVelocityValue<BillboardParticle>{
		public void addVelocity (ParticleController<BillboardParticle> controller, BillboardParticle particle, CommonVelocityData velocityData){
			float strength = velocityData.strengthStart + velocityData.strengthDiff * strengthValue.getScale(particle.lifePercent),
					phi = velocityData.phistart + velocityData.phiDiff * phiValue.getScale(particle.lifePercent),  
					theta = velocityData.thetaStart + velocityData.thetaDiff * thetaValue.getScale(particle.lifePercent);

			TMP_V3.set(Vector3.X).rotate(Vector3.Y, theta);
			TMP_V2.set(TMP_V3).crs(Vector3.Y);
			TMP_V3.rotate(TMP_V2, phi);
			particle.velocity.add(TMP_V3.nor().scl(strength* controller.deltaTime));
		}
	}
	
	public static class BillboardWeightVelocityValue extends StrengthVelocityValue<BillboardParticle, StrengthVelocityData>{
		public void addVelocity (ParticleController<BillboardParticle> controller, BillboardParticle particle, StrengthVelocityData velocityData){
			float strength = velocityData.strengthStart + velocityData.strengthDiff * strengthValue.getScale(particle.lifePercent);
			TMP_V3.set(controller.velocity);
			particle.velocity.add(TMP_V3.scl(strength* controller.deltaTime));
		}
	}
	
	public static class BillboardBrownianVelocityValue extends StrengthVelocityValue<BillboardParticle, StrengthVelocityData>{
		public void addVelocity (ParticleController<BillboardParticle> controller, BillboardParticle particle, StrengthVelocityData velocityData){
			float strength = velocityData.strengthStart + velocityData.strengthDiff * strengthValue.getScale(particle.lifePercent);
			TMP_V3.set(MathUtils.random(-1, 1f), MathUtils.random(-1, 1f), MathUtils.random(-1, 1f)).nor();
			particle.velocity.add(TMP_V3.scl(strength* controller.deltaTime));
		}
	}

	//Model Instances 

	public static class ModelInstanceRotationVelocityValue extends AngularVelocityValue<ModelInstanceParticle>{
		@Override
		public void addVelocity (ParticleController<ModelInstanceParticle> controller, ModelInstanceParticle particle, CommonVelocityData data) {
			float strength = data.strengthStart + data.strengthDiff * strengthValue.getScale(particle.lifePercent),
					phi = data.phistart + data.phiDiff * phiValue.getScale(particle.lifePercent),  
					theta = data.thetaStart + data.thetaDiff * thetaValue.getScale(particle.lifePercent);
			TMP_V3.set(Vector3.X).rotate(Vector3.Y, theta);
			TMP_V2.set(TMP_V3).crs(Vector3.Y);
			TMP_V3.rotate(TMP_V2, phi);
			if(!isGlobal)
				TMP_V3.mul(particle.rotation);
			ModelInstanceParticle.ROTATION_ACCUMULATOR.mulLeft(TMP_Q.set(TMP_V3.nor(), strength*controller.deltaTime));
		}
	}
	
	public static class ModelInstanceCentripetalVelocityValue extends StrengthVelocityValue<ModelInstanceParticle, StrengthVelocityData>{
		@Override
		public void addVelocity (ParticleController<ModelInstanceParticle> controller, ModelInstanceParticle particle, StrengthVelocityData data) {
			float strength = data.strengthStart + data.strengthDiff * strengthValue.getScale(particle.lifePercent);
			float[] val = particle.instance.transform.val;
			TMP_V3.set(val[Matrix4.M03], val[Matrix4.M13], val[Matrix4.M23]);
			if(!isGlobal){
				float[] emitterTransform = controller.transform.val;
				TMP_V3.sub(emitterTransform[Matrix4.M03], emitterTransform[Matrix4.M13], emitterTransform[Matrix4.M23]);
			}
			particle.velocity.add(TMP_V3.nor().scl(strength* controller.deltaTime));
		}
	}
	
	public static class ModelInstanceTangetialVelocityValue extends AngularVelocityValue<ModelInstanceParticle>{
		public void addVelocity (ParticleController<ModelInstanceParticle> controller, ModelInstanceParticle particle, CommonVelocityData velocityData){
			float strength = velocityData.strengthStart + velocityData.strengthDiff * strengthValue.getScale(particle.lifePercent),
					phi = velocityData.phistart + velocityData.phiDiff * phiValue.getScale(particle.lifePercent),  
					theta = velocityData.thetaStart + velocityData.thetaDiff * thetaValue.getScale(particle.lifePercent);
			float[] val = particle.instance.transform.val;
			TMP_V3.set(Vector3.X).rotate(Vector3.Y, theta);
			TMP_V2.set(TMP_V3).crs(Vector3.Y);
			TMP_V3.rotate(TMP_V2, phi);
			TMP_V3.crs(TMP_V2.set(val[Matrix4.M03], val[Matrix4.M13], val[Matrix4.M23]));
			if(!isGlobal)
				TMP_V3.mul(particle.rotation);//.nor();
			particle.velocity.add(TMP_V3.nor().scl(strength* controller.deltaTime));
		}
	}
	
	public static class ModelInstancePolarVelocityValue extends AngularVelocityValue<ModelInstanceParticle>{
		public void addVelocity (ParticleController<ModelInstanceParticle> controller, ModelInstanceParticle particle, CommonVelocityData velocityData){
			float strength = velocityData.strengthStart + velocityData.strengthDiff * strengthValue.getScale(particle.lifePercent),
					phi = velocityData.phistart + velocityData.phiDiff * phiValue.getScale(particle.lifePercent),  
					theta = velocityData.thetaStart + velocityData.thetaDiff * thetaValue.getScale(particle.lifePercent);

			TMP_V3.set(Vector3.X).rotate(Vector3.Y, theta);
			TMP_V2.set(TMP_V3).crs(Vector3.Y);
			TMP_V3.rotate(TMP_V2, phi);
			if(!isGlobal)
				TMP_V3.mul(particle.rotation);//.nor();
			particle.velocity.add(TMP_V3.nor().scl(strength* controller.deltaTime));
		}
	}
	
	public static class ModelInstanceWeightVelocityValue extends StrengthVelocityValue<ModelInstanceParticle, StrengthVelocityData>{
		public void addVelocity (ParticleController<ModelInstanceParticle> controller, ModelInstanceParticle particle, StrengthVelocityData velocityData){
			float strength = velocityData.strengthStart + velocityData.strengthDiff * strengthValue.getScale(particle.lifePercent);
			TMP_V3.set(controller.velocity);
			particle.velocity.add(TMP_V3.scl(strength* controller.deltaTime));
		}
	}
	
	public static class ModelInstanceBrownianVelocityValue extends StrengthVelocityValue<ModelInstanceParticle, StrengthVelocityData>{
		public void addVelocity (ParticleController<ModelInstanceParticle> controller, ModelInstanceParticle particle, StrengthVelocityData velocityData){
			float strength = velocityData.strengthStart + velocityData.strengthDiff * strengthValue.getScale(particle.lifePercent);
			TMP_V3.set(MathUtils.random(-1, 1f), MathUtils.random(-1, 1f), MathUtils.random(-1, 1f)).nor();
			particle.velocity.add(TMP_V3.scl(strength* controller.deltaTime));
		}
	}
	
	
	
	//Particle Controller 

	public static class ParticleControllerRotationVelocityValue extends AngularVelocityValue<ParticleControllerParticle>{
		@Override
		public void addVelocity (ParticleController<ParticleControllerParticle> controller, ParticleControllerParticle particle, CommonVelocityData data) {
			float strength = data.strengthStart + data.strengthDiff * strengthValue.getScale(particle.lifePercent),
					phi = data.phistart + data.phiDiff * phiValue.getScale(particle.lifePercent),  
					theta = data.thetaStart + data.thetaDiff * thetaValue.getScale(particle.lifePercent);
			TMP_V3.set(Vector3.X).rotate(Vector3.Y, theta);
			TMP_V2.set(TMP_V3).crs(Vector3.Y);
			TMP_V3.rotate(TMP_V2, phi);
			if(!isGlobal)
				TMP_V3.mul(particle.rotation);
			ParticleControllerParticle.ROTATION_ACCUMULATOR.mulLeft(TMP_Q.set(TMP_V3.nor(), strength*controller.deltaTime));
		}
	}
	
	public static class ParticleControllerCentripetalVelocityValue extends StrengthVelocityValue<ParticleControllerParticle, StrengthVelocityData>{
		@Override
		public void addVelocity (ParticleController<ParticleControllerParticle> controller, ParticleControllerParticle particle, StrengthVelocityData data) {
			float strength = data.strengthStart + data.strengthDiff * strengthValue.getScale(particle.lifePercent);
			TMP_V3.set(particle.x, particle.y, particle.z);
			if(!isGlobal){
				float[] emitterTransform = controller.transform.val;
				TMP_V3.sub(emitterTransform[Matrix4.M03], emitterTransform[Matrix4.M13], emitterTransform[Matrix4.M23]);
			}
			particle.velocity.add(TMP_V3.nor().scl(strength* controller.deltaTime));
		}
	}
	
	public static class ParticleControllerTangetialVelocityValue extends AngularVelocityValue<ParticleControllerParticle>{
		public void addVelocity (ParticleController<ParticleControllerParticle> controller, ParticleControllerParticle particle, CommonVelocityData velocityData){
			float strength = velocityData.strengthStart + velocityData.strengthDiff * strengthValue.getScale(particle.lifePercent),
					phi = velocityData.phistart + velocityData.phiDiff * phiValue.getScale(particle.lifePercent),  
					theta = velocityData.thetaStart + velocityData.thetaDiff * thetaValue.getScale(particle.lifePercent);
			TMP_V3.set(Vector3.X).rotate(Vector3.Y, theta);
			TMP_V2.set(TMP_V3).crs(Vector3.Y);
			TMP_V3.rotate(TMP_V2, phi);
			TMP_V3.crs(TMP_V2.set(particle.x, particle.y, particle.z));
			if(!isGlobal)
				TMP_V3.mul(particle.rotation);//.nor();
			particle.velocity.add(TMP_V3.nor().scl(strength* controller.deltaTime));
		}
	}
	
	public static class ParticleControllerPolarVelocityValue extends AngularVelocityValue<ParticleControllerParticle>{
		public void addVelocity (ParticleController<ParticleControllerParticle> controller, ParticleControllerParticle particle, CommonVelocityData velocityData){
			float strength = velocityData.strengthStart + velocityData.strengthDiff * strengthValue.getScale(particle.lifePercent),
					phi = velocityData.phistart + velocityData.phiDiff * phiValue.getScale(particle.lifePercent),  
					theta = velocityData.thetaStart + velocityData.thetaDiff * thetaValue.getScale(particle.lifePercent);

			TMP_V3.set(Vector3.X).rotate(Vector3.Y, theta);
			TMP_V2.set(TMP_V3).crs(Vector3.Y);
			TMP_V3.rotate(TMP_V2, phi);
			if(!isGlobal)
				TMP_V3.mul(particle.rotation);//.nor();
			particle.velocity.add(TMP_V3.nor().scl(strength* controller.deltaTime));
		}
	}
	
	public static class ParticleControllerWeightVelocityValue extends StrengthVelocityValue<ParticleControllerParticle, StrengthVelocityData>{
		public void addVelocity (ParticleController<ParticleControllerParticle> controller, ParticleControllerParticle particle, StrengthVelocityData velocityData){
			float strength = velocityData.strengthStart + velocityData.strengthDiff * strengthValue.getScale(particle.lifePercent);
			TMP_V3.set(controller.velocity);
			particle.velocity.add(TMP_V3.scl(strength* controller.deltaTime));
		}
	}
	
	public static class ParticleControllerBrownianVelocityValue extends StrengthVelocityValue<ParticleControllerParticle, StrengthVelocityData>{
		public void addVelocity (ParticleController<ParticleControllerParticle> controller, ParticleControllerParticle particle, StrengthVelocityData velocityData){
			float strength = velocityData.strengthStart + velocityData.strengthDiff * strengthValue.getScale(particle.lifePercent);
			TMP_V3.set(MathUtils.random(-1, 1f), MathUtils.random(-1, 1f), MathUtils.random(-1, 1f)).nor();
			particle.velocity.add(TMP_V3.scl(strength* controller.deltaTime));
		}
	}
	
	
	
	//Points
	public static class PointRotationVelocityValue extends StrengthVelocityValue<PointParticle, StrengthVelocityData>{
		@Override
		public void addVelocity (ParticleController<PointParticle> controller, PointParticle particle, StrengthVelocityData data) {
			PointParticle.ROTATION_ACCUMULATOR += (data.strengthStart + data.strengthDiff * strengthValue.getScale(particle.lifePercent)) * controller.deltaTime;
		}
	}
	
	public static class PointCentripetalVelocityValue extends StrengthVelocityValue<PointParticle, StrengthVelocityData>{
		public void addVelocity (ParticleController<PointParticle> controller, PointParticle particle, StrengthVelocityData velocityData){
			float strength = velocityData.strengthStart + velocityData.strengthDiff * strengthValue.getScale(particle.lifePercent);
			TMP_V3.set(particle.x, particle.y, particle.z);
			if(!isGlobal){
				float[] val = controller.transform.val;
				TMP_V3.sub(val[Matrix4.M03], val[Matrix4.M13], val[Matrix4.M23]);
			}
			particle.velocity.add(TMP_V3.nor().scl(strength* controller.deltaTime));
		}
	}
	
	public static class PointTangetialVelocityValue extends AngularVelocityValue<PointParticle>{
		public void addVelocity (ParticleController<PointParticle> controller, PointParticle particle, CommonVelocityData velocityData){
			float strength = velocityData.strengthStart + velocityData.strengthDiff * strengthValue.getScale(particle.lifePercent),
					phi = velocityData.phistart + velocityData.phiDiff * phiValue.getScale(particle.lifePercent),  
					theta = velocityData.thetaStart + velocityData.thetaDiff * thetaValue.getScale(particle.lifePercent);

			TMP_V3.set(Vector3.X).rotate(Vector3.Y, theta);
			TMP_V2.set(TMP_V3).crs(Vector3.Y);
			TMP_V3.rotate(TMP_V2, phi);
			TMP_V3.crs(TMP_V2.set(particle.x, particle.y, particle.z));
			particle.velocity.add(TMP_V3.nor().scl(strength* controller.deltaTime));
		}
	}
	
	public static class PointPolarVelocityValue extends AngularVelocityValue<PointParticle>{
		public void addVelocity (ParticleController<PointParticle> controller, PointParticle particle, CommonVelocityData velocityData){
			float strength = velocityData.strengthStart + velocityData.strengthDiff * strengthValue.getScale(particle.lifePercent),
					phi = velocityData.phistart + velocityData.phiDiff * phiValue.getScale(particle.lifePercent),  
					theta = velocityData.thetaStart + velocityData.thetaDiff * thetaValue.getScale(particle.lifePercent);

			TMP_V3.set(Vector3.X).rotate(Vector3.Y, theta);
			TMP_V2.set(TMP_V3).crs(Vector3.Y);
			TMP_V3.rotate(TMP_V2, phi);
			particle.velocity.add(TMP_V3.nor().scl(strength* controller.deltaTime));
		}
	}
	
	public static class PointWeightVelocityValue extends StrengthVelocityValue<PointParticle, StrengthVelocityData>{
		public void addVelocity (ParticleController<PointParticle> controller, PointParticle particle, StrengthVelocityData velocityData){
			float strength = velocityData.strengthStart + velocityData.strengthDiff * strengthValue.getScale(particle.lifePercent);
			TMP_V3.set(controller.velocity);
			particle.velocity.add(TMP_V3.scl(strength* controller.deltaTime));
		}
	}
	
	public static class PointBrownianVelocityValue extends StrengthVelocityValue<PointParticle, StrengthVelocityData>{
		public void addVelocity (ParticleController<PointParticle> controller, PointParticle particle, StrengthVelocityData velocityData){
			float strength = velocityData.strengthStart + velocityData.strengthDiff * strengthValue.getScale(particle.lifePercent);
			TMP_V3.set(MathUtils.random(-1, 1f), MathUtils.random(-1, 1f), MathUtils.random(-1, 1f)).nor();
			particle.velocity.add(TMP_V3.scl(strength* controller.deltaTime));
		}
	}
	
	
}
