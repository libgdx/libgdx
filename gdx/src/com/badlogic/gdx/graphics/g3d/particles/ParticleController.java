package com.badlogic.gdx.graphics.g3d.particles;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect.ParticleEffectData;
import com.badlogic.gdx.graphics.g3d.particles.emitters.Emitter;
import com.badlogic.gdx.graphics.g3d.particles.influencers.Influencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.RandomColorInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.VelocityInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.renderers.IParticleBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

public abstract class ParticleController<T> implements Json.Serializable{
	/** Name of the controller */
	public String name;
	
	/** Controls the emission of the particles */
	public Emitter<T> emitter;
	
	/** Update the properties of the particles */
	public Array<Influencer<T>> influencers;
	
	/** Controls the graphical representation of the particles */
	//public Renderer<T> renderer;
	public  IParticleBatch<T> batch;
	
	/** Particles components */
	public T[] particles;
	
	/** Current time */
	public float deltaTime;
	
	/** Current transform of the controller
	 *	 DO NOT CHANGE MANUALLY */
	public Matrix4 transform, tmpTransform;
	
	/** Transform flags */
	public boolean isDirty, isRecomputeScale;
	public Vector3 scale;
	
	protected BoundingBox boundingBox;
	
	/** Current velocity of the emitter.
	 * it's calculated as the difference between the current transform and the new transform.
	 * DO NOT CHANGE MANUALLY */
	public Vector3 velocity;
	
	public ParticleController(){
		transform = new Matrix4();
		tmpTransform = new Matrix4();
		velocity = new Vector3();
		scale = new Vector3(1,1,1);
	}
	
	public ParticleController(String name, Emitter<T> emitter, IParticleBatch<T> batch, Influencer<T>...influencers){
		this();
		this.name = name;
		this.emitter = emitter;
		this.batch = batch;
		this.influencers = new Array<Influencer<T>>(influencers);
	}
	

	/** Sets the current transformation to the given one.
	 * @param transform the new transform matrix */
	public void setTransform (Matrix4 transform) {
		tmpTransform.set(transform);
		isRecomputeScale = true;
		isDirty = true;
	}

	/** Postmultiplies the current transformation with a rotation matrix represented by the given quaternion.*/
	public void rotate(Quaternion rotation){
		tmpTransform.rotate(rotation);
		isDirty = true;
	}
	
	/** Postmultiplies the current transformation with a rotation matrix by the given angle around the given axis.
	 * @param axis the rotation axis
	 * @param angle the rotation angle in degrees*/
	public void rotate(Vector3 axis, float angle){
		tmpTransform.rotate(axis, angle);
		isDirty = true;
	}
	
	/** Postmultiplies the current transformation with a translation matrix represented by the given translation.*/
	public void translate(Vector3 translation){
		tmpTransform.translate(translation);
		isDirty = true;
	}
	
	/** Postmultiplies the current transformation with a scale matrix represented by the given scale on x,y,z.*/
	public void scale(float scaleX, float scaleY, float scaleZ){
		tmpTransform.scale(scaleX, scaleY, scaleZ);
		isRecomputeScale = true;
		isDirty = true;
	}
	
	/** Postmultiplies the current transformation with a scale matrix represented by the given scale vector.*/
	public void scale(Vector3 scale){
		scale(scale.x, scale.y, scale.z);
	}
	
	/** Postmultiplies the current transformation with the given matrix.*/
	public void mul(Matrix4 transform){
		this.tmpTransform.mul(transform);
		isDirty = true;
	}
	
	/** Set the given matrix to the current transformation matrix.*/
	public void getTransform(Matrix4 transform){
		transform.set(this.tmpTransform);
	}
	
	
	/** Initialize the controller: all the sub systems will be initialized. */
	public void init(){
		bind();
		boolean alloc = true;
		if(particles != null) {
			end();
			alloc = particles.length < emitter.maxParticleCount;
		}
		if(alloc) 
			particles = allocParticles(emitter.maxParticleCount);
		initParticles();
		emitter.init();
		for(Influencer influencer : influencers)
			influencer.init();
		//renderer.init();
	}
	
	protected void initParticles (){};

	protected abstract T[] allocParticles (int maxParticleCount);

	protected void bind(){
		emitter.bind(this);
		for(Influencer influencer : influencers)
			influencer.bind(this);
		//renderer.bind(this);
	}
	
	/** Start the simulation. */
	public void start () {
		emitter.start();
		for(Influencer influencer : influencers)
			influencer.start();
		//renderer.start();
	}
	
	/** End the simulation. */
	protected void end () {
		emitter.end();
		for(Influencer influencer : influencers)
			influencer.end();
	}
	
	public void activateParticles (int startIndex, int count) {
		emitter.activateParticles(startIndex, count);
		for(Influencer influencer : influencers)
			influencer.activateParticles(startIndex, count);
	}
	
	public void killParticles (int startIndex, int count){
		emitter.killParticles(startIndex, count);
		for(Influencer influencer : influencers)
			influencer.killParticles(startIndex, count);
	}
	
	/** Updates the particles data */
	public void update(float dt){
		deltaTime = dt;
		
		//Update transform first
		if(isDirty){
			/*
			if(isAttached){
				TMP_M3_1.set(transform).transpose().mul(TMP_M3_2.set(tmpTransform));
				transform.getTranslation(TMP_V2);
				tmpTransform.getTranslation(TMP_V3);
				for (int i = 0; i < activeCount; ++i){
					Particle particle = particles[i];
					TMP_V1.set(particle.x, particle.y, particle.z).sub(TMP_V2);
					mul(TMP_M3_1, TMP_V1);
					particle.x = TMP_V1.x + TMP_V3.x; particle.y = TMP_V1.y + TMP_V3.y; particle.z = TMP_V1.z + TMP_V3.z;
				}
			}
			*/
			float[] 	currentTransform = transform.val,
						newTransform = tmpTransform.val;
			velocity.set(	newTransform[Matrix4.M03] -currentTransform[Matrix4.M03], 
								newTransform[Matrix4.M13] -currentTransform[Matrix4.M13], 
								newTransform[Matrix4.M23] -currentTransform[Matrix4.M23]).scl(1f/dt);
			if(isRecomputeScale){
				//Compute x and y scale
				tmpTransform.getScale(scale);
				isRecomputeScale = false;
			}
			
			//Swap
			Matrix4 temp = this.transform;
			this.transform = tmpTransform;
			tmpTransform = temp;
			
			//Reset
			tmpTransform.idt();
			isDirty = false;
			emitter.update();
			for(Influencer influencer : influencers)
				influencer.update();

			velocity.set(0, 0, 0);
		}
		else {
			emitter.update();
			for(Influencer influencer : influencers)
				influencer.update();
		}
	}

	public void draw () {
		if(emitter.activeCount > 0){
			batch.draw(this);
		}
	}
	
	public abstract ParticleController copy ();

	public void dispose(){
		emitter.dispose();
		for(Influencer influencer : influencers)
			influencer.dispose();
		//renderer.dispose();
	}

	public BoundingBox getBoundingBox (){
		if(boundingBox == null) boundingBox = new BoundingBox();
		calculateBoundingBox();
		return boundingBox;
	}
	
	protected abstract void calculateBoundingBox ();

	private <K extends Influencer<T>> int findIndex(Class<K> type){
		for(int i = 0; i< influencers.size; ++i){
			Influencer influencer = influencers.get(i);
			if(type.isAssignableFrom(influencer.getClass())){
				return i;
			}
		}
		return -1;
	}
	
	public <K extends Influencer<T>> K findInfluencer (Class<K> influencerClass) {
		int index = findIndex(influencerClass);
		return index >-1 ? (K)influencers.get(index) : null;
	}
	
	public  <K extends Influencer<T>> void removeInfluencer (Class<K> type) {
		int index = findIndex(type);
		if(index > -1 )
			influencers.removeIndex(index);
	}
	
	public <K extends Influencer<T>> boolean replaceInfluencer (Class<K> type, K newInfluencer) {
		int index = findIndex(type);
		if(index > -1){
			influencers.insert(index, newInfluencer);
			influencers.removeIndex(index+1);
			return true;
		}
		return false;
	}

	@Override
	public void write (Json json) {
      json.writeValue("name", name);
      json.writeValue("emitter", emitter, Emitter.class);
      json.writeValue("influencers", influencers, Array.class, Influencer.class);
   }

	@Override
	public void read (Json json, JsonValue jsonMap) {
		name = json.readValue("name", String.class, jsonMap);
		emitter = json.readValue("emitter", Emitter.class, jsonMap);
		influencers = json.readValue("influencers", Array.class, Influencer.class, jsonMap);
	}

	public void saveAssets (AssetManager manager, ParticleEffectData data) {
		emitter.saveAssets(manager, data);
		for(Influencer influencer : influencers)
			influencer.saveAssets(manager, data);
	}

	public void loadAssets (AssetManager manager, ParticleEffectData data) {
		emitter.loadAssets(manager, data);
		for(Influencer influencer : influencers)
			influencer.loadAssets(manager, data);
	}

}
