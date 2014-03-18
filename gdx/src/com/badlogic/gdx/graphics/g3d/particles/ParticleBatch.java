package com.badlogic.gdx.graphics.g3d.particles;

import java.lang.reflect.Array;
import java.util.Comparator;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.particles.renderers.IParticleBatch;
import com.badlogic.gdx.utils.Sort;

public abstract class ParticleBatch<T> implements IParticleBatch<T>{
	/** particles more distant from the camera will be rendered first */
	public static final Comparator<Particle> COMPARATOR_FAR_DISTANCE  = new Comparator<Particle>() {
		@Override
		public int compare (Particle o1, Particle o2) {
			return o1.cameraDistance < o2.cameraDistance ? -1 : o1.cameraDistance == o2.cameraDistance ? 0 : 1;
		}
	};
	
	/** particles nearer to the camera will be rendered first */
	public static final Comparator<Particle> COMPARATOR_NEAR_DISTANCE  = new Comparator<Particle>() {
		@Override
		public int compare (Particle o1, Particle o2) {
			return o1.cameraDistance < o2.cameraDistance ? 1 : o1.cameraDistance == o2.cameraDistance ? 0 : -1;
		}
	};
	
	/** older particles will be rendered first */
	public static final Comparator<Particle> COMPARATOR_OLDER  = new Comparator<Particle>() {
		@Override
		public int compare (Particle o1, Particle o2) {
			return o1.lifePercent < o2.lifePercent ? -1 : o1.lifePercent == o2.lifePercent ? 0 : 1;
		}
	};
	
	/** younger particles will be rendered first */
	public static final Comparator<Particle> COMPARATOR_YOUNGER = new Comparator<Particle>() {
		@Override
		public int compare (Particle o1, Particle o2) {
			return o1.lifePercent < o2.lifePercent ? 1 : o1.lifePercent == o2.lifePercent ? 0 : -1;
		}
	};

	protected T[] bufferedParticles;
	protected com.badlogic.gdx.utils.Array<ParticleController<T>> controllers; 
	protected Class<T> type;
	protected int bufferedParticlesCount, leftBufferSpace;
	protected Sort sorter = Sort.instance();
	protected Comparator<T> comparator;
	protected Camera camera;
	
	protected ParticleBatch(Class<T> type, Comparator comparator){
		this.type = type;
		this.comparator = comparator;
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
			updateSortWeight(bufferedParticles, bufferedParticlesCount);
			
			//Sort each array, copy, and sort again
			/*
			int i=0, count = 0;
			for(ParticleController<T> controller : controllers){
				count = controller.emitter.activeCount;
				updateSortWeight(controller.particles, count);
				sorter.sort(controller.particles, comparator, 0, count);
				System.arraycopy( controller.particles, 0, bufferedParticles, i, count);
				i+= count;
			}
			*/
			
			sorter.sort(bufferedParticles, comparator, 0, bufferedParticlesCount);
			flush();
		}
	}

	public void ensureCapacity(int capacity){
		if(bufferedParticles != null && bufferedParticles.length >= capacity) return;
		allocParticlesData(capacity);
	}
	
	protected void allocParticlesData (int capacity){
		T[] particles = (T[])Array.newInstance(type, capacity);
		bufferedParticles = particles;
	}
	
	protected abstract void updateSortWeight (T[] particles, int count);
	protected abstract void flush();
	

	public void setCamera(Camera camera){
		this.camera = camera;
	}
	
	public void setComparator(Comparator comparator){
		this.comparator = comparator;
	}

	
}
