package com.badlogic.gdx.graphics.g3d.particles;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.particles.ParallelArray.FloatChannel;
import com.badlogic.gdx.graphics.g3d.particles.emitters.Emitter;
import com.badlogic.gdx.graphics.g3d.particles.influencers.Influencer;
import com.badlogic.gdx.graphics.g3d.particles.renderers.ParticleControllerRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.reflect.ClassReflection;

/** Base class of all the particle controllers.
 * Encapsulate the generic structure of a controller and methods to update the particles simulation.
 * @author Inferno */
public class ParticleController implements Json.Serializable, ResourceData.Configurable{
	
	/** the default time step used to update the simulation */
	protected static final float DEFAULT_TIME_STEP = 1f/60; 
	
	/** Name of the controller */
	public String name;
	
	/** Controls the emission of the particles */
	public Emitter emitter;
	
	/** Update the properties of the particles */
	public Array<Influencer> influencers;
	
	/** Controls the graphical representation of the particles */
	public  ParticleControllerRenderer<?, ?> renderer;
	
	/** Particles components */
	public ParallelArray particles;
	public ParticleChannels particleChannels;
	
	/** Current transform of the controller
	 *	 DO NOT CHANGE MANUALLY */
	public Matrix4 transform;
	
	/** Transform flags */
	public Vector3 scale;
	
	/** Not used by the simulation, it should represent the bounding box containing all the particles*/
	protected BoundingBox boundingBox;
	
	/** Time step, DO NOT CHANGE MANUALLY */
	public float deltaTime, deltaTimeSqr;
	
	public ParticleController(){
		transform = new Matrix4();
		scale = new Vector3(1,1,1);
		influencers = new Array<Influencer>(true, 3, Influencer.class);
		setTimeStep(DEFAULT_TIME_STEP);
	}

	public ParticleController(String name, Emitter emitter, ParticleControllerRenderer<?, ?> renderer, Influencer...influencers){
		this();
		this.name = name;
		this.emitter = emitter;
		this.renderer = renderer;
		this.particleChannels = new ParticleChannels();
		this.influencers = new Array<Influencer>(influencers);
	}

	/**Sets the delta used to step the simulation */
	private void setTimeStep (float timeStep) {
		deltaTime = timeStep;
		deltaTimeSqr = deltaTime*deltaTime;
	}
	
	/** Sets the current transformation to the given one.
	 * @param transform the new transform matrix */
	public void setTransform (Matrix4 transform) {
		this.transform.set(transform);
		transform.getScale(scale);
	}

	/** Sets the current transformation. */
	public void setTransform(float x, float y, float z, float qx, float qy, float qz, float qw, float scale ){
		transform.set(x, y, z, qx, qy, qz, qw, scale, scale, scale);
		this.scale.set(scale, scale, scale);
	}

	/** Post-multiplies the current transformation with a rotation matrix represented by the given quaternion.*/
	public void rotate(Quaternion rotation){
		this.transform.rotate(rotation);
	}
	
	/** Post-multiplies the current transformation with a rotation matrix by the given angle around the given axis.
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
		if(particles != null) {
			end();
			particleChannels.resetIds();
		}
		allocateChannels(emitter.maxParticleCount);

		emitter.init();
		for(Influencer influencer : influencers)
			influencer.init();
		renderer.init();
	}

	protected void allocateChannels (int maxParticleCount){
		particles = new ParallelArray(maxParticleCount);
		//Alloc additional channels
		emitter.allocateChannels();
		for(Influencer influencer : influencers)
			influencer.allocateChannels();
		renderer.allocateChannels();
	}

	/** Bind the sub systems to the controller
	 *  Called once during the init phase.*/
	protected void bind(){
		emitter.set(this);
		for(Influencer influencer : influencers)
			influencer.set(this);
		renderer.set(this);
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
		for(Influencer influencer : influencers)
			influencer.end();
		emitter.end();
	}
	
	/** Generally called by the Emitter. 
	 * This method will notify all the sub systems that a given amount 
	 * of particles has been activated. */
	public void activateParticles (int startIndex, int count) {
		emitter.activateParticles(startIndex, count);
		for(Influencer influencer : influencers)
			influencer.activateParticles(startIndex, count);
	}
	
	/** Generally called by the Emitter. 
	 * This method will notify all the sub systems that a given amount 
	 * of particles has been killed. */
	public void killParticles (int startIndex, int count){
		emitter.killParticles(startIndex, count);
		for(Influencer influencer : influencers)
			influencer.killParticles(startIndex, count);
	}
	
	/** Updates the particles data */
	public void update(){
		emitter.update();
		for(Influencer influencer : influencers)
			influencer.update();
	}

	/**Updates the renderer used by this controller, usually this means the particles will be draw inside a batch. */
	public void draw () {
		if(particles.size > 0){
			renderer.update();
		}
	}
	
	/** @return a copy of this controller*/
	public ParticleController copy () {
		Emitter emitter = (Emitter)this.emitter.copy();
		Influencer[] influencers = new Influencer[this.influencers.size];
		int i=0;
		for(Influencer influencer : this.influencers){
			influencers[i++] = (Influencer)influencer.copy();
		}
		return new ParticleController(new String(this.name), emitter, (ParticleControllerRenderer<?, ?>)renderer.copy(), influencers);
	}

	public void dispose(){
		emitter.dispose();
		for(Influencer influencer : influencers)
			influencer.dispose();
	}

	/** @return a copy of this controller, should be used after the particle effect has been loaded. */
	public BoundingBox getBoundingBox (){
		if(boundingBox == null) boundingBox = new BoundingBox();
		calculateBoundingBox();
		return boundingBox;
	}
	
	/** Updates the bounding box using the position channel. */
	protected void calculateBoundingBox () {
		boundingBox.clr();
		FloatChannel positionChannel = particles.getChannel(ParticleChannels.Position);
		for(int pos = 0, c = positionChannel.strideSize*particles.size ; pos < c; pos += positionChannel.strideSize){
			boundingBox.ext(	positionChannel.data[pos + ParticleChannels.XOffset], 
												positionChannel.data[pos + ParticleChannels.YOffset], 
												positionChannel.data[pos + ParticleChannels.ZOffset]);
		}
	}

	/** @return the index of the Influencer of the given type. */
	private <K extends Influencer> int findIndex(Class<K> type){
		for(int i = 0; i< influencers.size; ++i){
			Influencer influencer = influencers.get(i);
			if(ClassReflection.isAssignableFrom(type, influencer.getClass())){
				return i;
			}
		}
		return -1;
	}
	
	/** @return the influencer having the given type. */
	public <K extends Influencer> K findInfluencer (Class<K> influencerClass) {
		int index = findIndex(influencerClass);
		return index >-1 ? (K)influencers.get(index) : null;
	}
	
	/** Removes the Influencer of the given type. */
	public  <K extends Influencer> void removeInfluencer (Class<K> type) {
		int index = findIndex(type);
		if(index > -1 )
			influencers.removeIndex(index);
	}
	
	/** Replaces the Influencer of the given type with the one passed as parameter. */
	public <K extends Influencer> boolean replaceInfluencer (Class<K> type, K newInfluencer) {
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
      json.writeValue("renderer", renderer, ParticleControllerRenderer.class);
   }

	@Override
	public void read (Json json, JsonValue jsonMap) {
		name = json.readValue("name", String.class, jsonMap);
		emitter = json.readValue("emitter", Emitter.class, jsonMap);
		influencers.addAll(json.readValue("influencers", Array.class, Influencer.class, jsonMap));
		renderer = json.readValue("renderer", ParticleControllerRenderer.class, jsonMap);
	}

	@Override
	public void save (AssetManager manager, ResourceData data) {
		emitter.save(manager, data);
		for(Influencer influencer : influencers)
			influencer.save(manager, data);
		renderer.save(manager, data);
	}

	@Override
	public void load (AssetManager manager, ResourceData data) {
		emitter.load(manager, data);
		for(Influencer influencer : influencers)
			influencer.load(manager, data);
		renderer.load(manager, data);
	}
}
