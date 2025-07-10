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

package com.badlogic.gdx.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.actions.*;
import com.badlogic.gdx.utils.DefaultPool.PoolSupplier;

/** Stores a map of {@link Pool}s by type for convenient static access.
 * @author Nathan Sweet */
public class Pools {
	static private final ObjectMap<Class<?>, Pool<?>> typePools = new ObjectMap<>();
	static private final ObjectSet<Class<?>> initializedTypes = new ObjectSet<>();
	static public boolean WARN_ON_REFLECTION_POOL_CREATION = true;
	static public boolean THROW_ON_REFLECTION_POOL_CREATION = false;

	/** Returns a new or existing pool for the specified type, stored in a Class to {@link Pool} map. Note the max size is ignored
	 * if this is not the first time this pool has been requested. */
	static public <T> Pool<T> get (Class<T> type, int max) {
		Pool pool = typePools.get(type);
		if (pool == null) {
			// Force class initialization to run static blocks. All poolable libGDX classes have static initializers
			// that call Pools.set(ClassName::new). This approach avoids manual initialization order management and
			// prevents pulling in unused classes (unlike a central Pools static block), allowing ProGuard/R8/GraalVM
			// to strip unused classes while ensuring used ones register their pools before ReflectionPool creation.
			if (!initializedTypes.contains(type)) {
				initializedTypes.add(type);
				try {
					Class.forName(type.getName(), true, type.getClassLoader());
				} catch (ClassNotFoundException e) {
					// This should never happen in normal circumstances since we already have the Class object.
					// However, it can occur with aggressive code optimization/obfuscation tools that may:
					// - Remove classes deemed unused (ProGuard/R8 shrinking)
					// - Merge/inline classes (R8 optimization)
					// - Transform classes in incompatible ways (GraalVM native-image)
					// If you encounter this, ensure the class is kept by your obfuscation rules.
					if (Gdx.app != null) {
						Gdx.app.error("Pools", "Failed to initialize class " + type.getName() + ". "
							+ "This may occur with code obfuscation, shrinking, class merging (ProGuard/R8), or native compilation (GraalVM). "
							+ "Add keep rules for this class and its constructors.");
					}
				}
				pool = typePools.get(type);
			}
			if (pool == null) {
				if (THROW_ON_REFLECTION_POOL_CREATION) throw new RuntimeException("No pool registered for type " + type.getName()
					+ ". "
					+ "A ReflectionPool will be created which is slower and will fail if the class is not explicitly included with ProGuard/R8/GraalVM. "
					+ "To fix: Add Pools.set(" + type.getSimpleName()
					+ "::new) - this will automatically keep the class in ProGuard/R8/GraalVM.");
				if (WARN_ON_REFLECTION_POOL_CREATION && Gdx.app != null) Gdx.app.error("Pools", "No pool registered for type "
					+ type.getName() + ". "
					+ "A ReflectionPool will be created which is slower and will fail if the class is not explicitly included with ProGuard/R8/GraalVM. "
					+ "To fix: Add Pools.set(" + type.getSimpleName()
					+ "::new) - this will automatically keep the class in ProGuard/R8/GraalVM.");
				pool = new ReflectionPool(type, 4, max);
				typePools.put(type, pool);
			}
		}
		return pool;
	}

	/** Returns a new or existing pool for the specified type, stored in a Class to {@link Pool} map. The max size of the pool used
	 * is 100. */
	static public <T> Pool<T> get (Class<T> type) {
		return get(type, 100);
	}

	/** Sets an existing pool for the specified type, stored in a Class to {@link Pool} map. */
	static public <T> void set (Class<T> type, Pool<T> pool) {
		typePools.put(type, pool);
	}

	/** Sets an existing pool for the specified type, stored in a Class to {@link Pool} map. Usage can use java 8 method
	 * references: {@code Pools.set(MyClass::new, max)} */
	static public <T> void set (PoolSupplier<T> poolTypeSupplier, int max) {
		set((Class<T>)poolTypeSupplier.get().getClass(), new DefaultPool<>(poolTypeSupplier, 4, max));
	}

	/** Sets an existing pool for the specified type, stored in a Class to {@link Pool} map. Usage can use java 8 method
	 * references: {@code Pools.set(MyClass::new)} */
	static public <T> void set (PoolSupplier<T> poolTypeSupplier) {
		set(poolTypeSupplier, 100);
	}

	/** Obtains an object from the {@link #get(Class) pool}. */
	static public <T> T obtain (Class<T> type) {
		return get(type).obtain();
	}

	/** Frees an object from the {@link #get(Class) pool}. */
	static public void free (Object object) {
		if (object == null) throw new IllegalArgumentException("object cannot be null.");
		Pool pool = typePools.get(object.getClass());
		if (pool == null) return; // Ignore freeing an object that was never retained.
		pool.free(object);
	}

	/** Frees the specified objects from the {@link #get(Class) pool}. Null objects within the array are silently ignored. Objects
	 * don't need to be from the same pool. */
	static public void freeAll (Array objects) {
		freeAll(objects, false);
	}

	/** Frees the specified objects from the {@link #get(Class) pool}. Null objects within the array are silently ignored.
	 * @param samePool If true, objects don't need to be from the same pool but the pool must be looked up for each object. */
	static public void freeAll (Array objects, boolean samePool) {
		if (objects == null) throw new IllegalArgumentException("objects cannot be null.");
		Pool pool = null;
		for (int i = 0, n = objects.size; i < n; i++) {
			Object object = objects.get(i);
			if (object == null) continue;
			if (pool == null) {
				pool = typePools.get(object.getClass());
				if (pool == null) continue; // Ignore freeing an object that was never retained.
			}
			pool.free(object);
			if (!samePool) pool = null;
		}
	}

	private Pools () {
	}
}
