package com.badlogic.gdx.graphics.g3d.particles;

import java.lang.reflect.Array;
import java.util.Comparator;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.particles.renderers.IParticleBatch;
import com.badlogic.gdx.utils.Sort;

public abstract class ParticleBatch<T> implements IParticleBatch<T>{
	protected T[] bufferedParticles;
	protected com.badlogic.gdx.utils.Array<ParticleController<T>> controllers;
	protected ParticleSorter<T> sorter;
	protected Class<T> type;
	protected int bufferedParticlesCount;
	protected Camera camera;
	
	protected ParticleBatch(Class<T> type, ParticleSorter<T> sorter){
		this.type = type;
		this.sorter = sorter;
		controllers = new com.badlogic.gdx.utils.Array<ParticleController<T>>(false, 10, ParticleController.class);
	}
	
	public void begin(){
		controllers.clear();
		bufferedParticlesCount = 0;
	}
	
	public <K extends ParticleController<T>> void draw (K controller) {
		if(controller.emitter.activeCount > 0){
			controllers.add(controller);
			bufferedParticlesCount += controller.emitter.activeCount;
		}
	}
	
	public void end(){
		if(bufferedParticlesCount > 0){
			ensureCapacity(bufferedParticlesCount);
			
			//Copy all and then sort
			int i=0, count = 0;
			for(ParticleController<T> controller : controllers){
				count = controller.emitter.activeCount;
				System.arraycopy( controller.particles, 0, bufferedParticles, i, count);
				i+= count;
			}
			sorter.sort(bufferedParticles, bufferedParticlesCount);
			flush();
		}
	}

	/**Ensure the batch can contain the passed in amount of particles*/
	public void ensureCapacity(int capacity){
		if(bufferedParticles != null && bufferedParticles.length >= capacity) return;
		allocParticlesData(capacity);
	}
	
	protected void allocParticlesData (int capacity){
		T[] particles = (T[])Array.newInstance(type, capacity);
		bufferedParticles = particles;
	}
	
	public void setCamera(Camera camera){
		this.camera = camera;
		sorter.setCamera(camera);
	}

	public ParticleSorter<T> getSorter(){
		return sorter;
	}
	
	public void setSorter(ParticleSorter<T> sorter){
		this.sorter = sorter;
		sorter.setCamera(camera);
	}
	
	protected abstract void flush();

}
