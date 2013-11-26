/**
 * Maintain a copy of the data
 */
%module btBvhTriangleMeshShape

%{
#include <BulletCollision/CollisionShapes/btBvhTriangleMeshShape.h>
%}

%ignore btBvhTriangleMeshShape::btBvhTriangleMeshShape(btStridingMeshInterface* meshInterface, bool useQuantizedAabbCompression, bool buildBvh = true);
%ignore btBvhTriangleMeshShape::btBvhTriangleMeshShape(btStridingMeshInterface* meshInterface, bool useQuantizedAabbCompression,const btVector3& bvhAabbMin,const btVector3& bvhAabbMax, bool buildBvh = true);
%javamethodmodifiers btBvhTriangleMeshShape::btBvhTriangleMeshShape "private";

%extend btBvhTriangleMeshShape {
	btBvhTriangleMeshShape(bool dummy, btStridingMeshInterface* meshInterface, bool useQuantizedAabbCompression, bool buildBvh = true) {
		return new btBvhTriangleMeshShape(meshInterface, useQuantizedAabbCompression, buildBvh);
	}
	btBvhTriangleMeshShape(bool dummy, btStridingMeshInterface* meshInterface, bool useQuantizedAabbCompression,const btVector3& bvhAabbMin,const btVector3& bvhAabbMax, bool buildBvh = true) {
		return new btBvhTriangleMeshShape(meshInterface, useQuantizedAabbCompression, bvhAabbMin, bvhAabbMax, buildBvh);
	}
};

%typemap(javaimports) btBvhTriangleMeshShape %{
import com.badlogic.gdx.physics.bullet.BulletBase;
import com.badlogic.gdx.physics.bullet.linearmath.*;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.utils.Array;
%}

%typemap(javacode) btBvhTriangleMeshShape %{
	protected final static Array<btBvhTriangleMeshShape> instances = new Array<btBvhTriangleMeshShape>();
	
	protected static <T extends MeshPart> btBvhTriangleMeshShape getInstance(final Array<T> meshParts) {
		for (final btBvhTriangleMeshShape instance : instances) {
			if (instance.meshInterface instanceof btTriangleIndexVertexArray &&
					btTriangleIndexVertexArray.compare((btTriangleIndexVertexArray)(instance.meshInterface), meshParts))
				return instance;
		}
		return null;
	}
	
	/** Obtain an instance of btBvhTriangleMeshShape, made up of the specified {@link MeshPart} instances.
	 * Where possible previously obtained objects are reused. You must call {@link #release()},
	 * when you no longer need the shape. */
	public static <T extends MeshPart> btBvhTriangleMeshShape obtain(final Array<T> meshParts) {
		btBvhTriangleMeshShape result = getInstance(meshParts);
		if (result == null) {
			result = new btBvhTriangleMeshShape(btTriangleIndexVertexArray.obtain(meshParts), true);
			instances.add(result);
		}
		result.obtain();
		return result;
	}
	
	private btStridingMeshInterface meshInterface = null;
	
	/** @return The {@link btStridingMeshInterface} this shape encapsulates. */ 
	public btStridingMeshInterface getMeshInterface() {
		return meshInterface;
	}

	public <T extends MeshPart> btBvhTriangleMeshShape(final Array<T> meshParts) {
		this(meshParts, true);
	}
	
	public <T extends MeshPart> btBvhTriangleMeshShape(final Array<T> meshParts, boolean useQuantizedAabbCompression) {
		this(1, btTriangleIndexVertexArray.obtain(meshParts), useQuantizedAabbCompression);
	}
	
	public <T extends MeshPart> btBvhTriangleMeshShape(final Array<T> meshParts, boolean useQuantizedAabbCompression, boolean buildBvh) {
		this(1, btTriangleIndexVertexArray.obtain(meshParts), useQuantizedAabbCompression, buildBvh);
	}
	
	public <T extends MeshPart> btBvhTriangleMeshShape(final Array<T> meshParts, boolean useQuantizedAabbCompression, Vector3 bvhAabbMin, Vector3 bvhAabbMax) {
		this(1, btTriangleIndexVertexArray.obtain(meshParts), useQuantizedAabbCompression, bvhAabbMin, bvhAabbMax);
	}
	
	public <T extends MeshPart> btBvhTriangleMeshShape(final Array<T> meshParts, boolean useQuantizedAabbCompression, Vector3 bvhAabbMin, Vector3 bvhAabbMax, boolean buildBvh) {
		this(1, btTriangleIndexVertexArray.obtain(meshParts), useQuantizedAabbCompression, bvhAabbMin, bvhAabbMax, buildBvh);
	}
	
	public btBvhTriangleMeshShape(btStridingMeshInterface meshInterface, boolean useQuantizedAabbCompression) {
		this(0, meshInterface, useQuantizedAabbCompression);
	}

	public btBvhTriangleMeshShape(btStridingMeshInterface meshInterface, boolean useQuantizedAabbCompression, boolean buildBvh) {
		this(0, meshInterface, useQuantizedAabbCompression, buildBvh);
	}
	
	public btBvhTriangleMeshShape(btStridingMeshInterface meshInterface, boolean useQuantizedAabbCompression, Vector3 bvhAabbMin, Vector3 bvhAabbMax, boolean buildBvh) {
		this(0, meshInterface, useQuantizedAabbCompression, bvhAabbMin, bvhAabbMax, buildBvh);
	}
	
	public btBvhTriangleMeshShape(btStridingMeshInterface meshInterface, boolean useQuantizedAabbCompression, Vector3 bvhAabbMin, Vector3 bvhAabbMax) {
		this(0, meshInterface, useQuantizedAabbCompression, bvhAabbMin, bvhAabbMax);
	}
	
	private btBvhTriangleMeshShape(int obtained, btStridingMeshInterface meshInterface, boolean useQuantizedAabbCompression) {
		this(true, meshInterface, useQuantizedAabbCompression);
		this.meshInterface = meshInterface;
		if (obtained == 0)
			meshInterface.obtain();
	}

	private btBvhTriangleMeshShape(int obtained, btStridingMeshInterface meshInterface, boolean useQuantizedAabbCompression, boolean buildBvh) {
		this(true, meshInterface, useQuantizedAabbCompression, buildBvh);
		this.meshInterface = meshInterface;
		if (obtained == 0)
			meshInterface.obtain();
	}
	
	private btBvhTriangleMeshShape(int obtained, btStridingMeshInterface meshInterface, boolean useQuantizedAabbCompression, Vector3 bvhAabbMin, Vector3 bvhAabbMax, boolean buildBvh) {
		this(true, meshInterface, useQuantizedAabbCompression, bvhAabbMin, bvhAabbMax, buildBvh);
		this.meshInterface = meshInterface;
		if (obtained == 0)
			meshInterface.obtain();
	}
	
	private btBvhTriangleMeshShape(int obtained, btStridingMeshInterface meshInterface, boolean useQuantizedAabbCompression, Vector3 bvhAabbMin, Vector3 bvhAabbMax) {
		this(true, meshInterface, useQuantizedAabbCompression, bvhAabbMin, bvhAabbMax);
		this.meshInterface = meshInterface;
		if (obtained == 0)
			meshInterface.obtain();
	}
	
	
	public void dispose() {
		if (meshInterface != null)
			meshInterface.release();
		meshInterface = null;
		super.dispose();
	}
%}

%include "BulletCollision/CollisionShapes/btBvhTriangleMeshShape.h"