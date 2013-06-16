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
package com.badlogic.gdx.tests.bullet;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.lights.Lights;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;

/** @author xoppa 
 * No physics, simple base class for rendering a bunch of entities.  
 */
public class BaseWorld<T extends BaseEntity> implements Disposable {
	public static abstract class Constructor<T extends BaseEntity> implements Disposable {
		public Model model = null;
		public abstract T construct(final float x, final float y, final float z);
		public abstract T construct(final Matrix4 transform);
	}
	
	private final ObjectMap<String, Constructor<T>> constructors = new ObjectMap<String, Constructor<T>>();
	protected final Array<T> entities = new Array<T>();
	private final Array<Model> models = new Array<Model>();
	
	public void addConstructor(final String name, final Constructor<T> constructor) {
		constructors.put(name, constructor);
		if (constructor.model != null && !models.contains(constructor.model, true))
			models.add(constructor.model);
	}
	
	public Constructor<T> getConstructor(final String name) {
		return constructors.get(name);
	}
	
	public void add(final T entity) {
		entities.add(entity);
	}
	
	public T add(final String type, float x, float y, float z) {
		final T entity = constructors.get(type).construct(x, y, z);
		add(entity);
		return entity;
	}
	
	public T add(final String type, final Matrix4 transform) {
		final T entity = constructors.get(type).construct(transform);
		add(entity);
		return entity;
	}
	
	public void render(final ModelBatch batch, final Lights lights) {
		render(batch, lights, entities);
	}
	
	public void render(final ModelBatch batch, final Lights lights, final Iterable<T> entities) {
		for (final T e : entities) {
			e.modelInstance.calculateTransforms();
			batch.render(e.modelInstance, lights);
		}
	}
	
	public void render(final ModelBatch batch, final Lights lights, final T entity) {
		entity.modelInstance.calculateTransforms();
		batch.render(entity.modelInstance, lights);
	}
	
	public void update() {	}
	
	@Override
	public void dispose () {
		for (int i = 0; i < entities.size; i++)
			entities.get(i).dispose();
		entities.clear();
		
		for (Constructor<T> constructor : constructors.values())
			constructor.dispose();
		constructors.clear();
		
		//for (int i = 0; i < models.size; i++)
			//models.get(i).dispose();
		models.clear();
	}
}