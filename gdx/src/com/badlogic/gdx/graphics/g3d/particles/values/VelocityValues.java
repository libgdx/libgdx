package com.badlogic.gdx.graphics.g3d.particles.values;

import com.badlogic.gdx.graphics.g3d.particles.BillboardParticle;
import com.badlogic.gdx.graphics.g3d.particles.ModelInstanceParticle;
import com.badlogic.gdx.graphics.g3d.particles.Particle;
import com.badlogic.gdx.graphics.g3d.particles.ParticleController;
import com.badlogic.gdx.graphics.g3d.particles.ParticleControllerParticle;
import com.badlogic.gdx.graphics.g3d.particles.PointSpriteParticle;
import com.badlogic.gdx.graphics.g3d.particles.influencers.VelocityInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityDatas.AngularVelocityData;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityDatas.StrengthVelocityData;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityDatas.VelocityData;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

/** Defines the {@link VelocityValue} values acting on each type of {@link Particle} particles.*/
/** @author Inferno */
public final class VelocityValues {
	
	//Billboards
	
	public static class BillboardRotationVelocityValue extends StrengthVelocityValue<BillboardParticle, StrengthVelocityData>{
		public BillboardRotationVelocityValue () {
			super();
		}
		public BillboardRotationVelocityValue (BillboardRotationVelocityValue billboardRotationVelocityValue) {
			super(billboardRotationVelocityValue);
		}
		@Override
		public BillboardRotationVelocityValue copy () {
			return new BillboardRotationVelocityValue(this);
		}
		@Override
		public void addVelocity (ParticleController<BillboardParticle> controller, BillboardParticle particle, StrengthVelocityData data) {
			BillboardParticle.ROTATION_ACCUMULATOR += (data.strengthStart + data.strengthDiff * strengthValue.getScale(particle.lifePercent));
		}
	}
	
	public static class BillboardCentripetalVelocityValue extends StrengthVelocityValue<BillboardParticle, StrengthVelocityData>{
		public BillboardCentripetalVelocityValue () {
			super();
		}
		public BillboardCentripetalVelocityValue (BillboardCentripetalVelocityValue billboardRotationVelocityValue) {
			super(billboardRotationVelocityValue);
		}
		@Override
		public BillboardCentripetalVelocityValue copy () {
			return new BillboardCentripetalVelocityValue(this);
		}
		public void addVelocity (ParticleController<BillboardParticle> controller, BillboardParticle particle, StrengthVelocityData velocityData){
			float strength = velocityData.strengthStart + velocityData.strengthDiff * strengthValue.getScale(particle.lifePercent);
			TMP_V3.set(particle.x, particle.y, particle.z);
			if(!isGlobal){
				float[] val = controller.transform.val;
				TMP_V3.sub(val[Matrix4.M03], val[Matrix4.M13], val[Matrix4.M23]);
			}
			particle.velocity.add(TMP_V3.nor().scl(strength));
		}
	}
	
	public static class BillboardTangetialVelocityValue extends AngularVelocityValue<BillboardParticle>{
		public BillboardTangetialVelocityValue () {
			super();
		}
		public BillboardTangetialVelocityValue (BillboardTangetialVelocityValue billboardRotationVelocityValue) {
			super(billboardRotationVelocityValue);
		}
		@Override
		public BillboardTangetialVelocityValue copy () {
			return new BillboardTangetialVelocityValue(this);
		}
		public void addVelocity (ParticleController<BillboardParticle> controller, BillboardParticle particle, AngularVelocityData velocityData){
			float strength = velocityData.strengthStart + velocityData.strengthDiff * strengthValue.getScale(particle.lifePercent),
					phi = velocityData.phistart + velocityData.phiDiff * phiValue.getScale(particle.lifePercent),  
					theta = velocityData.thetaStart + velocityData.thetaDiff * thetaValue.getScale(particle.lifePercent);

			float cosTheta = MathUtils.cosDeg(theta), sinTheta = MathUtils.sinDeg(theta),
					cosPhi = MathUtils.cosDeg(phi), sinPhi = MathUtils.sinDeg(phi);
			TMP_V3.set(	cosPhi *sinTheta, cosTheta, sinPhi *sinTheta);
			TMP_V3.crs(TMP_V2.set(particle.x, particle.y, particle.z));
			particle.velocity.add(TMP_V3.nor().scl(strength));
		}
	}
	
	public static class BillboardPolarVelocityValue extends AngularVelocityValue<BillboardParticle>{
		public BillboardPolarVelocityValue () {
			super();
		}
		public BillboardPolarVelocityValue (BillboardPolarVelocityValue billboardRotationVelocityValue) {
			super(billboardRotationVelocityValue);
		}
		@Override
		public BillboardPolarVelocityValue copy () {
			return new BillboardPolarVelocityValue(this);
		}
		public void addVelocity (ParticleController<BillboardParticle> controller, BillboardParticle particle, AngularVelocityData velocityData){
			float strength = velocityData.strengthStart + velocityData.strengthDiff * strengthValue.getScale(particle.lifePercent),
					phi = velocityData.phistart + velocityData.phiDiff * phiValue.getScale(particle.lifePercent),  
					theta = velocityData.thetaStart + velocityData.thetaDiff * thetaValue.getScale(particle.lifePercent);
			float cosTheta = MathUtils.cosDeg(theta), sinTheta = MathUtils.sinDeg(theta),
					cosPhi = MathUtils.cosDeg(phi), sinPhi = MathUtils.sinDeg(phi);
			TMP_V3.set(	cosPhi *sinTheta, cosTheta, sinPhi *sinTheta);	
			particle.velocity.add(TMP_V3.nor().scl(strength));
		}
	}
	
	public static class BillboardWeightVelocityValue extends StrengthVelocityValue<BillboardParticle, StrengthVelocityData>{
		public BillboardWeightVelocityValue () {
			super();
		}
		public BillboardWeightVelocityValue (BillboardWeightVelocityValue billboardRotationVelocityValue) {
			super(billboardRotationVelocityValue);
		}
		@Override
		public BillboardWeightVelocityValue copy () {
			return new BillboardWeightVelocityValue(this);
		}
		public void addVelocity (ParticleController<BillboardParticle> controller, BillboardParticle particle, StrengthVelocityData velocityData){
			float strength = velocityData.strengthStart + velocityData.strengthDiff * strengthValue.getScale(particle.lifePercent);
			TMP_V3.set(controller.velocity);
			particle.velocity.add(TMP_V3.scl(strength));
		}
	}
	
	public static class BillboardBrownianVelocityValue extends StrengthVelocityValue<BillboardParticle, StrengthVelocityData>{
		public BillboardBrownianVelocityValue () {
			super();
		}
		public BillboardBrownianVelocityValue (BillboardBrownianVelocityValue billboardRotationVelocityValue) {
			super(billboardRotationVelocityValue);
		}
		@Override
		public BillboardBrownianVelocityValue copy () {
			return new BillboardBrownianVelocityValue(this);
		}
		public void addVelocity (ParticleController<BillboardParticle> controller, BillboardParticle particle, StrengthVelocityData velocityData){
			float strength = velocityData.strengthStart + velocityData.strengthDiff * strengthValue.getScale(particle.lifePercent);
			TMP_V3.set(MathUtils.random(-1, 1f), MathUtils.random(-1, 1f), MathUtils.random(-1, 1f)).nor();
			particle.velocity.add(TMP_V3.scl(strength));
		}
	}

	//Model Instances 

	public static class ModelInstanceRotationVelocityValue extends AngularVelocityValue<ModelInstanceParticle>{
		public ModelInstanceRotationVelocityValue () {
			super();
		}
		public ModelInstanceRotationVelocityValue (ModelInstanceRotationVelocityValue billboardRotationVelocityValue) {
			super(billboardRotationVelocityValue);
		}
		@Override
		public ModelInstanceRotationVelocityValue copy () {
			return new ModelInstanceRotationVelocityValue(this);
		}
		@Override
		public void addVelocity (ParticleController<ModelInstanceParticle> controller, ModelInstanceParticle particle, AngularVelocityData data) {
			float strength = data.strengthStart + data.strengthDiff * strengthValue.getScale(particle.lifePercent),
					phi = data.phistart + data.phiDiff * phiValue.getScale(particle.lifePercent),  
					theta = data.thetaStart + data.thetaDiff * thetaValue.getScale(particle.lifePercent);
			float cosTheta = MathUtils.cosDeg(theta), sinTheta = MathUtils.sinDeg(theta),
				cosPhi = MathUtils.cosDeg(phi), sinPhi = MathUtils.sinDeg(phi);
			TMP_V3.set(	cosPhi *sinTheta, cosTheta, sinPhi *sinTheta);	
			if(!isGlobal)
				TMP_V3.mul(particle.rotation);
			Particle.ROTATION_3D_ACCUMULATOR.mulLeft(TMP_Q.set(TMP_V3.nor(), strength));
		}
	}
	
	public static class ModelInstanceCentripetalVelocityValue extends StrengthVelocityValue<ModelInstanceParticle, StrengthVelocityData>{
		public ModelInstanceCentripetalVelocityValue () {
			super();
		}
		public ModelInstanceCentripetalVelocityValue (ModelInstanceCentripetalVelocityValue billboardRotationVelocityValue) {
			super(billboardRotationVelocityValue);
		}
		@Override
		public ModelInstanceCentripetalVelocityValue copy () {
			return new ModelInstanceCentripetalVelocityValue(this);
		}
		@Override
		public void addVelocity (ParticleController<ModelInstanceParticle> controller, ModelInstanceParticle particle, StrengthVelocityData data) {
			float strength = data.strengthStart + data.strengthDiff * strengthValue.getScale(particle.lifePercent);
			float[] val = particle.instance.transform.val;
			TMP_V3.set(val[Matrix4.M03], val[Matrix4.M13], val[Matrix4.M23]);
			if(!isGlobal){
				float[] emitterTransform = controller.transform.val;
				TMP_V3.sub(emitterTransform[Matrix4.M03], emitterTransform[Matrix4.M13], emitterTransform[Matrix4.M23]);
			}
			particle.velocity.add(TMP_V3.nor().scl(strength));
		}
	}
	
	public static class ModelInstanceTangetialVelocityValue extends AngularVelocityValue<ModelInstanceParticle>{
		public ModelInstanceTangetialVelocityValue () {
			super();
		}
		public ModelInstanceTangetialVelocityValue (ModelInstanceTangetialVelocityValue billboardRotationVelocityValue) {
			super(billboardRotationVelocityValue);
		}
		@Override
		public ModelInstanceTangetialVelocityValue copy () {
			return new ModelInstanceTangetialVelocityValue(this);
		}
		public void addVelocity (ParticleController<ModelInstanceParticle> controller, ModelInstanceParticle particle, AngularVelocityData velocityData){
			float strength = velocityData.strengthStart + velocityData.strengthDiff * strengthValue.getScale(particle.lifePercent),
					phi = velocityData.phistart + velocityData.phiDiff * phiValue.getScale(particle.lifePercent),  
					theta = velocityData.thetaStart + velocityData.thetaDiff * thetaValue.getScale(particle.lifePercent);
			float cosTheta = MathUtils.cosDeg(theta), sinTheta = MathUtils.sinDeg(theta),
				cosPhi = MathUtils.cosDeg(phi), sinPhi = MathUtils.sinDeg(phi);
			float[] val = particle.instance.transform.val;
			TMP_V3.set(	cosPhi *sinTheta, cosTheta, sinPhi *sinTheta);	
			TMP_V3.crs(TMP_V2.set(val[Matrix4.M03], val[Matrix4.M13], val[Matrix4.M23]));
			if(!isGlobal)
				TMP_V3.mul(particle.rotation);//.nor();
			particle.velocity.add(TMP_V3.nor().scl(strength));
		}
	}
	
	public static class ModelInstancePolarVelocityValue extends AngularVelocityValue<ModelInstanceParticle>{
		public ModelInstancePolarVelocityValue () {
			super();
		}
		public ModelInstancePolarVelocityValue (ModelInstancePolarVelocityValue billboardRotationVelocityValue) {
			super(billboardRotationVelocityValue);
		}
		@Override
		public ModelInstancePolarVelocityValue copy () {
			return new ModelInstancePolarVelocityValue(this);
		}
		public void addVelocity (ParticleController<ModelInstanceParticle> controller, ModelInstanceParticle particle, AngularVelocityData velocityData){
			float strength = velocityData.strengthStart + velocityData.strengthDiff * strengthValue.getScale(particle.lifePercent),
					phi = velocityData.phistart + velocityData.phiDiff * phiValue.getScale(particle.lifePercent),  
					theta = velocityData.thetaStart + velocityData.thetaDiff * thetaValue.getScale(particle.lifePercent);
			float cosTheta = MathUtils.cosDeg(theta), sinTheta = MathUtils.sinDeg(theta),
				cosPhi = MathUtils.cosDeg(phi), sinPhi = MathUtils.sinDeg(phi);
			TMP_V3.set(	cosPhi *sinTheta, cosTheta, sinPhi *sinTheta);	
			if(!isGlobal)
				TMP_V3.mul(particle.rotation);//.nor();
			particle.velocity.add(TMP_V3.nor().scl(strength));
		}
	}
	
	public static class ModelInstanceWeightVelocityValue extends StrengthVelocityValue<ModelInstanceParticle, StrengthVelocityData>{
		public ModelInstanceWeightVelocityValue () {
			super();
		}
		public ModelInstanceWeightVelocityValue (ModelInstanceWeightVelocityValue billboardRotationVelocityValue) {
			super(billboardRotationVelocityValue);
		}
		@Override
		public ModelInstanceWeightVelocityValue copy () {
			return new ModelInstanceWeightVelocityValue(this);
		}
		public void addVelocity (ParticleController<ModelInstanceParticle> controller, ModelInstanceParticle particle, StrengthVelocityData velocityData){
			float strength = velocityData.strengthStart + velocityData.strengthDiff * strengthValue.getScale(particle.lifePercent);
			TMP_V3.set(controller.velocity);
			particle.velocity.add(TMP_V3.scl(strength));
		}
	}
	
	public static class ModelInstanceBrownianVelocityValue extends StrengthVelocityValue<ModelInstanceParticle, StrengthVelocityData>{
		public ModelInstanceBrownianVelocityValue () {
			super();
		}
		public ModelInstanceBrownianVelocityValue (ModelInstanceBrownianVelocityValue billboardRotationVelocityValue) {
			super(billboardRotationVelocityValue);
		}
		@Override
		public ModelInstanceBrownianVelocityValue copy () {
			return new ModelInstanceBrownianVelocityValue(this);
		}
		public void addVelocity (ParticleController<ModelInstanceParticle> controller, ModelInstanceParticle particle, StrengthVelocityData velocityData){
			float strength = velocityData.strengthStart + velocityData.strengthDiff * strengthValue.getScale(particle.lifePercent);
			TMP_V3.set(MathUtils.random(-1, 1f), MathUtils.random(-1, 1f), MathUtils.random(-1, 1f)).nor();
			particle.velocity.add(TMP_V3.scl(strength));
		}
	}
	
	public static class ModelInstanceFaceVelocityValue extends VelocityValue<ModelInstanceParticle, VelocityData>{
		public ModelInstanceFaceVelocityValue () {
			super();
		}
		public ModelInstanceFaceVelocityValue (ModelInstanceFaceVelocityValue billboardRotationVelocityValue) {
			super(billboardRotationVelocityValue);
		}
		@Override
		public ModelInstanceFaceVelocityValue copy () {
			return new ModelInstanceFaceVelocityValue(this);
		}
		@Override
		public void addVelocity (ParticleController<ModelInstanceParticle> controller, ModelInstanceParticle particle, VelocityData data) {
			Vector3 	axisZ = TMP_V1.set(particle.velocity).nor(),
						axisY = TMP_V2.set(TMP_V1).crs(Vector3.Y).nor().crs(TMP_V1).nor(),
						axisX = TMP_V3.set(axisY).crs(axisZ).nor();
			particle.rotation.setFromAxes(false, 	axisX.x,  axisY.x, axisZ.x,
																axisX.y,  axisY.y, axisZ.y,
																axisX.z,  axisY.z, axisZ.z);
		}
		@Override
		public VelocityData allocData () {
			return null;
		}
		@Override
		public void initData (VelocityData velocityData) {}
	}
	
	
	
	//Particle Controller 

	public static class ParticleControllerRotationVelocityValue extends AngularVelocityValue<ParticleControllerParticle>{
		public ParticleControllerRotationVelocityValue () {
			super();
		}
		public ParticleControllerRotationVelocityValue (ParticleControllerRotationVelocityValue billboardRotationVelocityValue) {
			super(billboardRotationVelocityValue);
		}
		@Override
		public ParticleControllerRotationVelocityValue copy () {
			return new ParticleControllerRotationVelocityValue(this);
		}
		@Override
		public void addVelocity (ParticleController<ParticleControllerParticle> controller, ParticleControllerParticle particle, AngularVelocityData data) {
			float strength = data.strengthStart + data.strengthDiff * strengthValue.getScale(particle.lifePercent),
					phi = data.phistart + data.phiDiff * phiValue.getScale(particle.lifePercent),  
					theta = data.thetaStart + data.thetaDiff * thetaValue.getScale(particle.lifePercent);
			float cosTheta = MathUtils.cosDeg(theta), sinTheta = MathUtils.sinDeg(theta),
				cosPhi = MathUtils.cosDeg(phi), sinPhi = MathUtils.sinDeg(phi);
			TMP_V3.set(	cosPhi *sinTheta, cosTheta, sinPhi *sinTheta);	
			if(!isGlobal)
				TMP_V3.mul(particle.rotation);
			Particle.ROTATION_3D_ACCUMULATOR.mulLeft(TMP_Q.set(TMP_V3.nor(), strength));
		}
	}
	
	public static class ParticleControllerCentripetalVelocityValue extends StrengthVelocityValue<ParticleControllerParticle, StrengthVelocityData>{
		public ParticleControllerCentripetalVelocityValue () {
			super();
		}
		public ParticleControllerCentripetalVelocityValue (ParticleControllerCentripetalVelocityValue billboardRotationVelocityValue) {
			super(billboardRotationVelocityValue);
		}
		@Override
		public ParticleControllerCentripetalVelocityValue copy () {
			return new ParticleControllerCentripetalVelocityValue(this);
		}
		@Override
		public void addVelocity (ParticleController<ParticleControllerParticle> controller, ParticleControllerParticle particle, StrengthVelocityData data) {
			float strength = data.strengthStart + data.strengthDiff * strengthValue.getScale(particle.lifePercent);
			//TMP_V3.set(particle.x, particle.y, particle.z);
			particle.controller.transform.getTranslation(TMP_V3);
			if(!isGlobal){
				float[] emitterTransform = controller.transform.val;
				TMP_V3.sub(emitterTransform[Matrix4.M03], emitterTransform[Matrix4.M13], emitterTransform[Matrix4.M23]);
			}
			particle.velocity.add(TMP_V3.nor().scl(strength));
		}
	}
	
	public static class ParticleControllerTangetialVelocityValue extends AngularVelocityValue<ParticleControllerParticle>{
		public ParticleControllerTangetialVelocityValue () {
			super();
		}
		public ParticleControllerTangetialVelocityValue (ParticleControllerTangetialVelocityValue billboardRotationVelocityValue) {
			super(billboardRotationVelocityValue);
		}
		@Override
		public ParticleControllerTangetialVelocityValue copy () {
			return new ParticleControllerTangetialVelocityValue(this);
		}
		public void addVelocity (ParticleController<ParticleControllerParticle> controller, ParticleControllerParticle particle, AngularVelocityData velocityData){
			float strength = velocityData.strengthStart + velocityData.strengthDiff * strengthValue.getScale(particle.lifePercent),
					phi = velocityData.phistart + velocityData.phiDiff * phiValue.getScale(particle.lifePercent),  
					theta = velocityData.thetaStart + velocityData.thetaDiff * thetaValue.getScale(particle.lifePercent);
			float cosTheta = MathUtils.cosDeg(theta), sinTheta = MathUtils.sinDeg(theta),
				cosPhi = MathUtils.cosDeg(phi), sinPhi = MathUtils.sinDeg(phi);
			TMP_V3.set(	cosPhi *sinTheta, cosTheta, sinPhi *sinTheta);	
			//TMP_V3.crs(TMP_V2.set(particle.x, particle.y, particle.z));
			TMP_V3.crs(particle.controller.transform.getTranslation(TMP_V2));
			if(!isGlobal)
				TMP_V3.mul(particle.rotation);//.nor();
			particle.velocity.add(TMP_V3.nor().scl(strength));
		}
	}
	
	public static class ParticleControllerPolarVelocityValue extends AngularVelocityValue<ParticleControllerParticle>{
		public ParticleControllerPolarVelocityValue () {
			super();
		}
		public ParticleControllerPolarVelocityValue (ParticleControllerPolarVelocityValue billboardRotationVelocityValue) {
			super(billboardRotationVelocityValue);
		}
		@Override
		public ParticleControllerPolarVelocityValue copy () {
			return new ParticleControllerPolarVelocityValue(this);
		}
		public void addVelocity (ParticleController<ParticleControllerParticle> controller, ParticleControllerParticle particle, AngularVelocityData velocityData){
			float strength = velocityData.strengthStart + velocityData.strengthDiff * strengthValue.getScale(particle.lifePercent),
					phi = velocityData.phistart + velocityData.phiDiff * phiValue.getScale(particle.lifePercent),  
					theta = velocityData.thetaStart + velocityData.thetaDiff * thetaValue.getScale(particle.lifePercent);

			float cosTheta = MathUtils.cosDeg(theta), sinTheta = MathUtils.sinDeg(theta),
				cosPhi = MathUtils.cosDeg(phi), sinPhi = MathUtils.sinDeg(phi);
			TMP_V3.set(	cosPhi *sinTheta, cosTheta, sinPhi *sinTheta);	
			if(!isGlobal)
				TMP_V3.mul(particle.rotation);//.nor();
			particle.velocity.add(TMP_V3.nor().scl(strength));
		}
	}
	
	public static class ParticleControllerWeightVelocityValue extends StrengthVelocityValue<ParticleControllerParticle, StrengthVelocityData>{
		public ParticleControllerWeightVelocityValue () {
			super();
		}
		public ParticleControllerWeightVelocityValue (ParticleControllerWeightVelocityValue billboardRotationVelocityValue) {
			super(billboardRotationVelocityValue);
		}
		@Override
		public ParticleControllerWeightVelocityValue copy () {
			return new ParticleControllerWeightVelocityValue(this);
		}
		public void addVelocity (ParticleController<ParticleControllerParticle> controller, ParticleControllerParticle particle, StrengthVelocityData velocityData){
			float strength = velocityData.strengthStart + velocityData.strengthDiff * strengthValue.getScale(particle.lifePercent);
			TMP_V3.set(controller.velocity);
			particle.velocity.add(TMP_V3.scl(strength));
		}
	}
	
	public static class ParticleControllerBrownianVelocityValue extends StrengthVelocityValue<ParticleControllerParticle, StrengthVelocityData>{
		public ParticleControllerBrownianVelocityValue () {
			super();
		}
		public ParticleControllerBrownianVelocityValue (ParticleControllerBrownianVelocityValue billboardRotationVelocityValue) {
			super(billboardRotationVelocityValue);
		}
		@Override
		public ParticleControllerBrownianVelocityValue copy () {
			return new ParticleControllerBrownianVelocityValue(this);
		}
		public void addVelocity (ParticleController<ParticleControllerParticle> controller, ParticleControllerParticle particle, StrengthVelocityData velocityData){
			float strength = velocityData.strengthStart + velocityData.strengthDiff * strengthValue.getScale(particle.lifePercent);
			TMP_V3.set(MathUtils.random(-1, 1f), MathUtils.random(-1, 1f), MathUtils.random(-1, 1f)).nor();
			particle.velocity.add(TMP_V3.scl(strength));
		}
	}
	
	public static class ParticleControllerFaceVelocityValue extends VelocityValue<ParticleControllerParticle, VelocityData>{
		public ParticleControllerFaceVelocityValue () {
			super();
		}
		public ParticleControllerFaceVelocityValue (ParticleControllerFaceVelocityValue billboardRotationVelocityValue) {
			super(billboardRotationVelocityValue);
		}
		@Override
		public ParticleControllerFaceVelocityValue copy () {
			return new ParticleControllerFaceVelocityValue(this);
		}
		@Override
		public void addVelocity (ParticleController<ParticleControllerParticle> controller, ParticleControllerParticle particle, VelocityData data) {
			Vector3 	axisZ = TMP_V1.set(particle.velocity).nor(),
						axisY = TMP_V2.set(TMP_V1).crs(Vector3.Y).nor().crs(TMP_V1).nor(),
						axisX = TMP_V3.set(axisY).crs(axisZ).nor();
			particle.rotation.setFromAxes(false, 	axisX.x,  axisY.x, axisZ.x,
																axisX.y,  axisY.y, axisZ.y,
																axisX.z,  axisY.z, axisZ.z);
		}
		@Override
		public VelocityData allocData () {
			return null;
		}
		@Override
		public void initData (VelocityData velocityData) {}
	}
	
	
	
	//Points
	public static class PointRotationVelocityValue extends StrengthVelocityValue<PointSpriteParticle, StrengthVelocityData>{
		public PointRotationVelocityValue () {
			super();
		}
		public PointRotationVelocityValue (PointRotationVelocityValue billboardRotationVelocityValue) {
			super(billboardRotationVelocityValue);
		}
		@Override
		public PointRotationVelocityValue copy () {
			return new PointRotationVelocityValue(this);
		}
		@Override
		public void addVelocity (ParticleController<PointSpriteParticle> controller, PointSpriteParticle particle, StrengthVelocityData data) {
			PointSpriteParticle.ROTATION_ACCUMULATOR += (data.strengthStart + data.strengthDiff * strengthValue.getScale(particle.lifePercent));
		}
	}
	
	public static class PointCentripetalVelocityValue extends StrengthVelocityValue<PointSpriteParticle, StrengthVelocityData>{
		public PointCentripetalVelocityValue () {
			super();
		}
		public PointCentripetalVelocityValue (PointCentripetalVelocityValue billboardRotationVelocityValue) {
			super(billboardRotationVelocityValue);
		}
		@Override
		public PointCentripetalVelocityValue copy () {
			return new PointCentripetalVelocityValue(this);
		}
		public void addVelocity (ParticleController<PointSpriteParticle> controller, PointSpriteParticle particle, StrengthVelocityData velocityData){
			float strength = velocityData.strengthStart + velocityData.strengthDiff * strengthValue.getScale(particle.lifePercent);
			TMP_V3.set(particle.x, particle.y, particle.z);
			if(!isGlobal){
				float[] val = controller.transform.val;
				TMP_V3.sub(val[Matrix4.M03], val[Matrix4.M13], val[Matrix4.M23]);
			}
			particle.velocity.add(TMP_V3.nor().scl(strength));
		}
	}
	
	public static class PointTangetialVelocityValue extends AngularVelocityValue<PointSpriteParticle>{
		public PointTangetialVelocityValue () {
			super();
		}
		public PointTangetialVelocityValue (PointTangetialVelocityValue billboardRotationVelocityValue) {
			super(billboardRotationVelocityValue);
		}
		@Override
		public PointTangetialVelocityValue copy () {
			return new PointTangetialVelocityValue(this);
		}
		public void addVelocity (ParticleController<PointSpriteParticle> controller, PointSpriteParticle particle, AngularVelocityData velocityData){
			float strength = velocityData.strengthStart + velocityData.strengthDiff * strengthValue.getScale(particle.lifePercent),
					phi = velocityData.phistart + velocityData.phiDiff * phiValue.getScale(particle.lifePercent),  
					theta = velocityData.thetaStart + velocityData.thetaDiff * thetaValue.getScale(particle.lifePercent);

			float cosTheta = MathUtils.cosDeg(theta), sinTheta = MathUtils.sinDeg(theta),
				cosPhi = MathUtils.cosDeg(phi), sinPhi = MathUtils.sinDeg(phi);
			TMP_V3.set(	cosPhi *sinTheta, cosTheta, sinPhi *sinTheta);	
			TMP_V3.crs(TMP_V2.set(particle.x, particle.y, particle.z));
			particle.velocity.add(TMP_V3.nor().scl(strength));
		}
	}
	
	public static class PointPolarVelocityValue extends AngularVelocityValue<PointSpriteParticle>{
		public PointPolarVelocityValue () {
			super();
		}
		public PointPolarVelocityValue (PointPolarVelocityValue billboardRotationVelocityValue) {
			super(billboardRotationVelocityValue);
		}
		@Override
		public PointPolarVelocityValue copy () {
			return new PointPolarVelocityValue(this);
		}
		public void addVelocity (ParticleController<PointSpriteParticle> controller, PointSpriteParticle particle, AngularVelocityData velocityData){
			float strength = velocityData.strengthStart + velocityData.strengthDiff * strengthValue.getScale(particle.lifePercent),
					phi = velocityData.phistart + velocityData.phiDiff * phiValue.getScale(particle.lifePercent),  
					theta = velocityData.thetaStart + velocityData.thetaDiff * thetaValue.getScale(particle.lifePercent);

			float cosTheta = MathUtils.cosDeg(theta), sinTheta = MathUtils.sinDeg(theta),
				cosPhi = MathUtils.cosDeg(phi), sinPhi = MathUtils.sinDeg(phi);
			TMP_V3.set(	cosPhi *sinTheta, cosTheta, sinPhi *sinTheta);	
			particle.velocity.add(TMP_V3.nor().scl(strength));
		}
	}
	
	public static class PointWeightVelocityValue extends StrengthVelocityValue<PointSpriteParticle, StrengthVelocityData>{
		public PointWeightVelocityValue () {
			super();
		}
		public PointWeightVelocityValue (PointWeightVelocityValue billboardRotationVelocityValue) {
			super(billboardRotationVelocityValue);
		}
		@Override
		public PointWeightVelocityValue copy () {
			return new PointWeightVelocityValue(this);
		}
		public void addVelocity (ParticleController<PointSpriteParticle> controller, PointSpriteParticle particle, StrengthVelocityData velocityData){
			float strength = velocityData.strengthStart + velocityData.strengthDiff * strengthValue.getScale(particle.lifePercent);
			TMP_V3.set(controller.velocity);
			particle.velocity.add(TMP_V3.scl(strength));
		}
	}
	
	public static class PointBrownianVelocityValue extends StrengthVelocityValue<PointSpriteParticle, StrengthVelocityData>{
		public PointBrownianVelocityValue () {
			super();
		}
		public PointBrownianVelocityValue (PointBrownianVelocityValue billboardRotationVelocityValue) {
			super(billboardRotationVelocityValue);
		}
		@Override
		public PointBrownianVelocityValue copy () {
			return new PointBrownianVelocityValue(this);
		}
		public void addVelocity (ParticleController<PointSpriteParticle> controller, PointSpriteParticle particle, StrengthVelocityData velocityData){
			float strength = velocityData.strengthStart + velocityData.strengthDiff * strengthValue.getScale(particle.lifePercent);
			TMP_V3.set(MathUtils.random(-1, 1f), MathUtils.random(-1, 1f), MathUtils.random(-1, 1f)).nor();
			particle.velocity.add(TMP_V3.scl(strength));
		}
	}
	
	
}
