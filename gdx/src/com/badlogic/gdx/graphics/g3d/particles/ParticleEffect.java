/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.badlogic.gdx.graphics.g3d.particles;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.particles.renderers.ParticleBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

/** It's a set of particles controllers.
 * It can be updated, rendered, transformed which means the changes will be applied
 * on all the particles controllers.*/
/** @author Inferno */
public class ParticleEffect implements Disposable, ResourceData.Configurable{
	private Array<ParticleController> controllers;
	private BoundingBox bounds;

	public ParticleEffect () {
		controllers = new Array<ParticleController>(3);
	}

	public ParticleEffect (ParticleEffect effect) {
		controllers = new Array<ParticleController>(true, effect.controllers.size);
		for (int i = 0, n = effect.controllers.size; i < n; i++)
			controllers.add(effect.controllers.get(i).copy());
	}

	public ParticleEffect (ParticleController...emitters) {
		this.controllers = new Array<ParticleController>(emitters);
	}

	public void init(){
		for (int i = 0, n = controllers.size; i < n; i++)
			controllers.get(i).init();
	}

	public void start () {
		for (int i = 0, n = controllers.size; i < n; i++)
			controllers.get(i).start();
	}
	public void end () {
		for (int i = 0, n = controllers.size; i < n; i++)
			controllers.get(i).end();
	}
	public void reset() {
		for (int i = 0, n = controllers.size; i < n; i++)
			controllers.get(i).reset();
	}

	public void update (float delta) {
		for (int i = 0, n = controllers.size; i < n; i++)
			controllers.get(i).update(delta);
	}

	public void draw () {
		for (int i = 0, n = controllers.size; i < n; i++)
			controllers.get(i).draw();
	}

	/** Sets the given transform matrix on each controller.*/
	public void setTransform (Matrix4 transform) {
		for (int i = 0, n = controllers.size; i < n; i++)
			controllers.get(i).setTransform(transform);
	}

	/** Applies the rotation to the current transformation matrix of each controller.*/
	public void rotate(Quaternion rotation){
		for (int i = 0, n = controllers.size; i < n; i++)
			controllers.get(i).rotate(rotation);
	}

	/** Applies the rotation by the given angle around the given axis to the current transformation matrix of each controller.
	 * @param axis the rotation axis
	 * @param angle the rotation angle in degrees*/
	public void rotate(Vector3 axis, float angle){
		for (int i = 0, n = controllers.size; i < n; i++)
			controllers.get(i).rotate(axis, angle);
	}

	/** Applies the translation to the current transformation matrix of each controller.*/
	public void translate(Vector3 translation){
		for (int i = 0, n = controllers.size; i < n; i++)
			controllers.get(i).translate(translation);
	}

	/** Applies the scale to the current transformation matrix of each controller.*/
	public void scale(float scaleX, float scaleY, float scaleZ){
		for (int i = 0, n = controllers.size; i < n; i++)
			controllers.get(i).scale(scaleX, scaleY, scaleZ);
	}

	/** Applies the scale to the current transformation matrix of each controller.*/
	public void scale(Vector3 scale){
		for (int i = 0, n = controllers.size; i < n; i++)
			controllers.get(i).scale(scale.x, scale.y, scale.z);
	}

	public Array<ParticleController> getControllers () {
		return controllers;
	}

	/** Returns the controller with the specified name, or null. */
	public ParticleController findController (String name) {
		for (int i = 0, n = controllers.size; i < n; i++) {
			ParticleController emitter = controllers.get(i);
			if (emitter.name.equals(name)) return emitter;
		}
		return null;
	}

	public void dispose () {
		for (int i = 0, n = controllers.size; i < n; i++) {
			controllers.get(i).dispose();
		}
	}

	public BoundingBox getBoundingBox () {
		if (bounds == null) bounds = new BoundingBox();

		BoundingBox bounds = this.bounds;
		bounds.inf();
		for (ParticleController emitter : controllers)
			bounds.ext(emitter.getBoundingBox());
		return bounds;
	}

	public void save (AssetManager assetManager, ResourceData data){
		for(ParticleController controller : controllers){
			controller.save(assetManager, data);
		}
	}
	
	public void load (AssetManager assetManager, ResourceData data){
		int i=0;
		for(ParticleController controller : controllers){
			controller.load(assetManager, data);
		}
	}

	/** Assign one batch, among those passed in, to each controller.
	 * The batch must be compatible with the controller to be assigned.*/
	public void setBatch(ParticleBatch... batches){
		for(ParticleController controller : controllers){
			for(ParticleBatch batch : batches)
				if(controller.setBatch(batch))
					break;
		}
	}

	public ParticleEffect copy () {
		return new ParticleEffect(this);
	}
}
