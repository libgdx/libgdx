
package com.badlogic.gdx.utils;

/** Stores a map of {@link ReflectionPool}s by type for convenient static access.
 * @author Nathan Sweet */
public class Pools {
	static private final ObjectMap<Class, ReflectionPool> typePools = new ObjectMap();

	/** Returns a new or existing pool for the specified type, stored in a a Class to {@link ReflectionPool} map. The max size of
	 * the pool used is 100. */
	static public <T> Pool<T> get (Class<T> type) {
		ReflectionPool pool = typePools.get(type);
		if (pool == null) {
			pool = new ReflectionPool(type, 4, 100);
			typePools.put(type, pool);
		}
		return pool;
	}

	/** Obtains an object from the {@link #get(Class) pool}. */
	static public <T> T obtain (Class<T> type) {
		return (T)get(type).obtain();
	}

	/** Frees an object from the {@link #get(Class) pool}. */
	static public void free (Object object) {
		if (object == null) return;
		ReflectionPool pool = typePools.get(object.getClass());
		if (pool != null) pool.free(object);
	}

	private Pools () {
	}
}
