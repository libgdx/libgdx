package com.badlogic.gdx.graphics.g3d.particles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.particles.emitters.Emitter;
import com.badlogic.gdx.graphics.g3d.particles.influencers.Influencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.RegionInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.renderers.Renderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

public abstract class ParticleController<T> implements RenderableProvider{
	/** Name of the controller */
	public String name;
	
	/** Controls the emission of the particles */
	public Emitter<T> emitter;
	
	/** Update the properties of the particles */
	public Array<Influencer<T>> influencers;
	
	/** Controls the graphical representation of the particles */
	public Renderer<T> renderer;

	/** Particles components */
	public T[] particles;
	
	/** Current time */
	public float deltaTime;
	
	/** Current transform of the controller
	 *	 DO NOT CHANGE MANUALLY */
	public Matrix4 transform, tmpTransform;
	
	/** Transform flags */
	public boolean isDirty, isRecomputeScale;
	
	protected BoundingBox boundingBox;
	
	/** Current velocity of the emitter.
	 * it's calculated as the difference between the current transform and the new transform.
	 * DO NOT CHANGE MANUALLY */
	public Vector3 velocity;
	
	public ParticleController(String name, Emitter<T> emitter, Renderer<T> renderer, Influencer<T>...influencers){
		this.name = name;
		this.emitter = emitter;
		this.renderer = renderer;
		this.influencers = new Array<Influencer<T>>(influencers);
		transform = new Matrix4();
		tmpTransform = new Matrix4();
		velocity = new Vector3();
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
		boolean alloc = true;
		if(particles != null) {
			killParticles(0, emitter.activeCount);
			alloc = particles.length < emitter.maxParticleCount;
		}
		if(alloc) 
			particles = allocParticles(emitter.maxParticleCount);
		bind();
		emitter.init();
		for(Influencer influencer : influencers)
			influencer.init();
		renderer.init();
	}
	
	protected abstract T[] allocParticles (int maxParticleCount);

	protected void bind(){
		emitter.bind(this);
		for(Influencer influencer : influencers)
			influencer.bind(this);
		renderer.bind(this);
	}
	
	/** Start the simulation. */
	public void start () {
		emitter.start();
		for(Influencer influencer : influencers)
			influencer.start();
		//renderer.start();
	}
	
	public void initParticles (int startIndex, int count) {
		emitter.initParticles(startIndex, count);
		for(Influencer influencer : influencers)
			influencer.initParticles(startIndex, count);
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
				renderer.particlesRefScaleX = (float)Math.sqrt(	newTransform[Matrix4.M00]*newTransform[Matrix4.M00] + 
																				newTransform[Matrix4.M01]*newTransform[Matrix4.M01] + 
																				newTransform[Matrix4.M02]*newTransform[Matrix4.M02]);
				renderer.particlesRefScaleY = (float)Math.sqrt(	newTransform[Matrix4.M10]*newTransform[Matrix4.M10] + 
																				newTransform[Matrix4.M11]*newTransform[Matrix4.M11] + 
																				newTransform[Matrix4.M12]*newTransform[Matrix4.M12]);
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

	@Override
	public void getRenderables (Array<Renderable> renderables, Pool<Renderable> pool) {
		if(emitter.activeCount > 0){
			//This is called here and not in the update method because the camera or everything else on which 
			//the rendering could relay on are supposed to be ready and will not be modified again in this frame.
			renderer.update();
			renderer.getRenderables(renderables, pool);
		}
	}

	public abstract ParticleController copy ();

	public void dispose(){
		emitter.dispose();
		for(Influencer influencer : influencers)
			influencer.dispose();
		renderer.dispose();
	}

	public BoundingBox getBoundingBox (){
		if(boundingBox == null) boundingBox = new BoundingBox();
		calculateBoundingBox();
		return boundingBox;
	}
	
	protected abstract void calculateBoundingBox ();

	public <K extends Influencer<T>> K findInfluencer (Class<K> influencerClass) {
		for(int i = 0; i< influencers.size; ++i){
			Influencer influencer = influencers.items[i];
			if(influencerClass.isAssignableFrom(influencer.getClass()))
				return (K)influencer;
		}
		return null;
	}

}
