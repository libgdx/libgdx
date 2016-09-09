%module btBroadphasePairArray

%include "../common/gdxDisableBuffers.i"
%include "../common/gdxEnableCriticalArrays.i"

%typemap(javacode) btAlignedObjectArray<btBroadphasePair> %{
	/**
	 * @param out The array to fill with collision objects
	 * @param other The collision object the pair must contain (which itself is excluded from the result)
	 * @param tempArray A temporary array used by the method, not more object than the length of this array are added 
	 * @return The array specified by out */
	public com.badlogic.gdx.utils.Array<btCollisionObject> getCollisionObjects(final com.badlogic.gdx.utils.Array<btCollisionObject> out, final btCollisionObject other, final int[] tempArray) {
		final int c = getCollisionObjects(tempArray, tempArray.length, (int)btCollisionObject.getCPtr(other));
		for (int i = 0; i < c; i++)
			out.add(btCollisionObject.getInstance(tempArray[i], false));
		return out;
	}
	
	/** Fills the given array with user value set using {@link btCollisionObject#setUserValue(int)} of the collision objects
	 * within this pair array colliding with the given collision object.
	 * @param out The array to fill with the user values
	 * @param other The collision object the pair must contain (which itself is excluded from the result)
	 * @return The amount of user values set in the out array. */
	public int getCollisionObjectsValue(final int[] out, final btCollisionObject other) {
		return getCollisionObjectsValue(out, out.length, (int)btCollisionObject.getCPtr(other));
	}
%}

%rename(btBroadphasePairArray) btAlignedObjectArray<btBroadphasePair>;
class btAlignedObjectArray<btBroadphasePair> {
public:
	SIMD_FORCE_INLINE	int size() const;
};

%extend btAlignedObjectArray<btBroadphasePair> {

	btBroadphasePair *at(int n) {
		return &($self->at(n));
	}

	int getCollisionObjects(int result[], int max, int other) {
		static btManifoldArray marr;
		const int n = $self->size();
		int count = 0;
		int obj0, obj1;
		for (int i = 0; i < n; i++) {
			const btBroadphasePair& collisionPair = (*$self)[i];
			if (collisionPair.m_algorithm) {
				marr.resize(0);
				collisionPair.m_algorithm->getAllContactManifolds(marr);
				const int s = marr.size();
				for (int j = 0; j < s; j++) {
					btPersistentManifold *manifold = marr[j];
					if (manifold->getNumContacts() > 0) {
						*(const btCollisionObject **)&obj0 = manifold->getBody0();
						*(const btCollisionObject **)&obj1 = manifold->getBody1();
						if (obj0 == other)
							result[count++] = obj1;
						else if (obj1 == other)
							result[count++] = obj0;
						else continue;
						if (count >= max)
							return count;
					}
				}
			}
		}
		return count;
	}
	
	int getCollisionObjectsValue(int result[], int max, int other) {
		static btManifoldArray marr;
		const int n = $self->size();
		int count = 0;
		int obj0, obj1;
		for (int i = 0; i < n; i++) {
			const btBroadphasePair& collisionPair = (*$self)[i];
			if (collisionPair.m_algorithm) {
				marr.resize(0);
				collisionPair.m_algorithm->getAllContactManifolds(marr);
				const int s = marr.size();
				for (int j = 0; j < s; j++) {
					btPersistentManifold *manifold = marr[j];
					if (manifold->getNumContacts() > 0) {
						*(const btCollisionObject **)&obj0 = manifold->getBody0();
						*(const btCollisionObject **)&obj1 = manifold->getBody1();
						if (obj0 == other)
							result[count++] = ((GdxCollisionObjectBridge*)manifold->getBody1()->getUserPointer())->userValue;
						else if (obj1 == other)
							result[count++] = ((GdxCollisionObjectBridge*)manifold->getBody0()->getUserPointer())->userValue;
						else continue;
						if (count >= max)
							return count;
					}
				}
			}
		}
		return count;
	}
};

%include "../common/gdxEnableBuffers.i"
