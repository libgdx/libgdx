package com.badlogic.gdx.graphics.g3d.particles;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.particles.batches.ParticleBatch;
import com.badlogic.gdx.graphics.g3d.particles.emitters.Emitter;
import com.badlogic.gdx.graphics.g3d.particles.influencers.Influencer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

/** Base class of all the particle controllers.
 * Encapsulate the generic structure of a controller and methods to update the particles simulation.*/
/** @author Inferno */
public abstract class ParticleController<T> implements Json.Serializable, ResourceData.Configurable{
	/** Name of the controller */
	public String name;
	
	/** Controls the emission of the particles */
	public Emitter<T> emitter;
	
	/** Update the properties of the particles */
	public Array<Influencer<T>> influencers;
	
	/** Controls the graphical representation of the particles */
	public  ParticleBatch<T> batch;
	
	/** Particles components */
	public T[] particles;
	
	/** Current time */
	public float deltaTime;
	
	/** Current transform of the controller
	 *	 DO NOT CHANGE MANUALLY */
	public Matrix4 transform;
	
	/** Transform flags */
	public Vector3 scale;
	
	protected BoundingBox boundingBox;
	
	/** Current velocity of the emitter.
	 * it's calculated as the difference between the current transform and the new transform.
	 * DO NOT CHANGE MANUALLY */
	public Vector3 velocity;
	
	public ParticleController(){
		transform = new Matrix4();
		velocity = new Vector3();
		scale = new Vector3(1,1,1);
	}
	
	public ParticleController(String name, Emitter<T> emitter, ParticleBatch<T> batch, Influencer<T>...influencers){
		this();
		this.name = name;
		this.emitter = emitter;
		this.batch = batch;
		this.influencers = new Array<Influencer<T>>(influencers);
	}
	

	/** Sets the current transformation to the given one.
	 * @param transform the new transform matrix */
	public void setTransform (Matrix4 transform) {
		this.transform.set(transform);
		transform.getScale(scale);
	}

	/** Postmultiplies the current transformation with a rotation matrix represented by the given quaternion.*/
	public void rotate(Quaternion rotation){
		this.transform.rotate(rotation);
	}
	
	/** Postmultiplies the current transformation with a rotation matrix by the given angle around the given axis.
	 * @param axis the rotation axis
	 * @param angle the rotation angle in degrees*/
	public void rotate(Vector3 axis, float angle){
		this.transform.rotate(axis, angle);
	}
	
	/** Postmultiplies the current transformation with a translation matrix represented by the given translation.*/
	public void translate(Vector3 translation){
		this.transform.translate(translation);
	}
	
	public void setTranslation (Vector3 translation) {
		this.transform.setTranslation(translation);
	}
	
	/** Postmultiplies the current transformation with a scale matrix represented by the given scale on x,y,z.*/
	public void scale(float scaleX, float scaleY, float scaleZ){
		this.transform.scale(scaleX, scaleY, scaleZ);
		this.transform.getScale(scale);
	}
	
	/** Postmultiplies the current transformation with a scale matrix represented by the given scale vector.*/
	public void scale(Vector3 scale){
		scale(scale.x, scale.y, scale.z);
	}
	
	/** Postmultiplies the current transformation with the given matrix.*/
	public void mul(Matrix4 transform){
		this.transform.mul(transform);
		this.transform.getScale(scale);
	}
	
	/** Set the given matrix to the current transformation matrix.*/
	public void getTransform(Matrix4 transform){
		transform.set(this.transform);
	}
	
	
	/** Initialize the controller.
	 *  All the sub systems will be initialized and binded to the controller. 
	 *  Must be called before any other method. */
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
	}
	
	protected void initParticles (){};

	protected abstract T[] allocParticles (int maxParticleCount);

	/** Bind the sub systems to the controller
	 *  Called once during the init phase.*/
	protected void bind(){
		emitter.bind(this);
		for(Influencer influencer : influencers)
			influencer.bind(this);
	}
	
	/** Start the simulation. */
	public void start () {
		emitter.start();
		for(Influencer influencer : influencers)
			influencer.start();
	}
	
	/** Reset the simulation. */
	public void reset(){
		end();
		start();
	}
	
	/** End the simulation. */
	public void end () {
		emitter.end();
		for(Influencer influencer : influencers)
			influencer.end();
	}
	
	/** Generally called by the Emitter. 
	 * This method will notify all the sub systems that a given amount 
	 * of particles have been activated. */
	public void activateParticles (int startIndex, int count) {
		emitter.activateParticles(startIndex, count);
		for(Influencer influencer : influencers)
			influencer.activateParticles(startIndex, count);
	}
	
	/** Generally called by the Emitter. 
	 * This method will notify all the sub systems that a given amount 
	 * of particles have been killed. */
	public void killParticles (int startIndex, int count){
		emitter.killParticles(startIndex, count);
		for(Influencer influencer : influencers)
			influencer.killParticles(startIndex, count);
	}
	
	/** Updates the particles data */
	public void update(float dt){
		deltaTime = dt;
		
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
		transform.translate(velocity.x*dt, velocity.y*dt, velocity.z*dt);
		emitter.update();
		for(Influencer influencer : influencers)
			influencer.update();
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

	@Override
	public void save (AssetManager manager, ResourceData data) {
		emitter.save(manager, data);
		for(Influencer influencer : influencers)
			influencer.save(manager, data);
	}

	@Override
	public void load (AssetManager manager, ResourceData data) {
		emitter.load(manager, data);
		for(Influencer influencer : influencers)
			influencer.load(manager, data);
	}

	/**@return if this controller can be rendered by the given batch */
	public abstract boolean isCompatible(ParticleBatch batch);
	
	/**Sets the batch used to render the particle.
	 * It will implicitly check if the batch is compatible with this controller.*/
	public boolean setBatch (ParticleBatch batch) {
		if(isCompatible(batch)){
			this.batch = batch;
			return true;
		}
		return false;
	}

}
