%module btBroadphasePairArray

%include "gdxDisableBuffers.i"
%include "gdxEnableCriticalArrays.i"

%typemap(javacode) btAlignedObjectArray<btBroadphasePair> %{
	/**
	 * @param out The array to fill with collision objects
	 * @param exclude The (if any) collision object to exclude from the result
	 * @param tempArray A temporary array used by the method, not more object than the length of this array are added 
	 * @return The array specified by out */
	public com.badlogic.gdx.utils.Array<btCollisionObject> getCollisionObjects(final com.badlogic.gdx.utils.Array<btCollisionObject> out, final btCollisionObject exclude, final int[] tempArray) {
		final int c = getCollisionObjects(tempArray, tempArray.length, (int)btCollisionObject.getCPtr(exclude));
		for (int i = 0; i < c; i++)
			out.add(btCollisionObject.getInstance(tempArray[i], false));
		return out;
	}
%}

%rename(btBroadphasePairArray) btAlignedObjectArray<btBroadphasePair>;
class btAlignedObjectArray<btBroadphasePair> {
public:
	SIMD_FORCE_INLINE	int size() const;
	SIMD_FORCE_INLINE const btBroadphasePair& at(int n) const;
};

%extend btAlignedObjectArray<btBroadphasePair> {
	int getCollisionObjects(int result[], int max, int exclude) {
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
						result[count++] = obj0 == exclude ? obj1 : obj0;
						if (count >= max)
							return count;
					}
				}
			}
		}
		return count;
	}
};

%include "gdxEnableBuffers.i"
