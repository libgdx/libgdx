package com.badlogic.gdx.physics.tokamak;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.LongMap;

/**
 * See <a href="http://www.tokamakphysics.com/documentation/reference/neSimulator.htm">http://www.tokamakphysics.com/documentation/reference/neSimulator.htm</a>.
 * 
 * The original CreateSimulator and DestroySimulator methods are mapped to the constructor and the #dispose method.
 * @author mzechner
 *
 */
public class Simulator implements Disposable {
	private long addr;
	private LongMap<Object> objects = new LongMap<Object>();
	private CollisionTable collisionTable = new CollisionTable(0);
	private float[] tmpFloat = new float[3];
	private Vector3 gravity = new Vector3();
	
	/*JNI
	#include <tokamak.h>
	 */
	
	/**
	 * Creates a new simulator. Needs to be disposed via {@link #dispose()}
	 * @param sizeInfo specifies the amount of various objects to allocate within the simulator.
	 * @param gravity specifies the gravity, in m/s*s
	 */
	public Simulator(SimulatorSizeInfo sizeInfo, Vector3 gravity) {
		addr = createSimulator(sizeInfo.animatedBodiesCount,
							   sizeInfo.constraintBufferSize,
							   sizeInfo.constraintsCount,
							   sizeInfo.constraintSetsCount,
							   sizeInfo.controllersCount,
							   sizeInfo.geometriesCount,
							   sizeInfo.overlappedPairsCount,
							   sizeInfo.rigidBodiesCount,
							   sizeInfo.rigidParticlesCount,
							   sizeInfo.sensorsCount,
							   sizeInfo.terrainNodesGrowByCount,
							   sizeInfo.terrainNodesStartCount, 
							   gravity.x, gravity.y, gravity.z);
	}

	private static native long createSimulator(int animatedBodiesCount,
			int constraintBufferSize, int constraintsCount,
			int constraintSetsCount, int controllersCount, int geometriesCount,
			int overlappedPairsCount, int rigidBodiesCount,
			int rigidParticleCount, int sensorsCount,
			int terrainNodesGrowByCount, int terrainNodesStartCount, float x,
			float y, float z); /*
		neSimulatorSizeInfo sizeInfo;
		sizeInfo.animatedBodiesCount = animatedBodiesCount;
		sizeInfo.constraintBufferSize = constraintBufferSize;
		sizeInfo.constraintsCount = constraintsCount;
		sizeInfo.constraintSetsCount = constraintSetsCount;
		sizeInfo.controllersCount = controllersCount;
		sizeInfo.geometriesCount = geometriesCount;
		sizeInfo.overlappedPairsCount = overlappedPairsCount;
		sizeInfo.rigidBodiesCount = rigidBodiesCount;
		sizeInfo.rigidParticleCount = rigidParticleCount;
		sizeInfo.sensorsCount = sensorsCount;
		sizeInfo.terrainNodesGrowByCount = terrainNodesGrowByCount;
		sizeInfo.terrainNodesStartCount = terrainNodesStartCount;
		
		neV3 gravity; 
		gravity.Set(x, y, z);
			
		return (jlong)neSimulator::CreateSimulator(sizeInfo, 0, &gravity);
	*/

	@Override
	public void dispose() {
		disposeJni(addr);
	}
	
	private static native void disposeJni(long addr); /*
		neSimulator::DestroySimulator((neSimulator*)addr);
	*/
	
	public RigidBody createRigidBody() {
		RigidBody body = new RigidBody(createRigidBodyJni(this.addr));
		objects.put(body.addr, body);
		return body;
	}
	
	private static native long createRigidBodyJni(long addr); /*
		return (jlong)((neSimulator*)addr)->CreateRigidBody();
	*/

	public RigidBody createRigidParticle() {
		RigidBody body = new RigidBody(createRigidParticleJni(this.addr));
		objects.put(body.addr, body);
		return body;
	}
	
	private static native long createRigidParticleJni(long addr); /*
		return (jlong)((neSimulator*)addr)->CreateRigidParticle();
	*/

	public AnimatedBody createAnimatedBody() {
		AnimatedBody body = new AnimatedBody(createAnimatedBodyJni(this.addr));
		objects.put(body.addr, body);
		return body;
	}
	
	private static native long createAnimatedBodyJni(long addr); /*
		return (jlong)((neSimulator*)addr)->CreateAnimatedBody();
	*/

	public void freeRigidBody(RigidBody body) {
		freeRigidBodyJni(addr, body.addr);
	}
	
	private static native void freeRigidBodyJni(long simAddr, long addr); /*
		((neSimulator*)simAddr)->FreeRigidBody((neRigidBody*)addr);
	*/
	
	public void freeAnimatedBody(AnimatedBody body) {
		freeAnimatedBodyJni(addr, body.addr);
	}
	
	private static native void freeAnimatedBodyJni(long simAddr, long addr); /*
		((neSimulator*)simAddr)->FreeAnimatedBody((neAnimatedBody*)addr);
	*/
	
	public CollisionTable getCollisionTable() {
		collisionTable.addr = getCollisionTableJNI(addr);
		return collisionTable;
	}
	
	private static native long getCollisionTableJNI(long addr); /*
		return (jlong)((neSimulator*)addr)->GetCollisionTable();
	*/
	
	public boolean setMaterial(Material material) {
		return setMaterialJni(addr, material.index, material.friction, material.restitution);
	}
	
	public boolean setMaterial(int index, float friction, float restitution) {
		return setMaterialJni(addr, index, friction, restitution);
	}
	
	private static native boolean setMaterialJni(long addr, int index, float friction, float restitution); /*
		((neSimulator*)addr)->SetMaterial(index, friction, restitution);
	*/
	
	public boolean getMaterial(Material material) {
		boolean result = getMaterialJni(addr, material.index, tmpFloat);
		material.friction = tmpFloat[0];
		material.restitution = tmpFloat[1];
		return result;
	}
	
	private static native boolean getMaterialJni(long addr, int index, float[] materialTmp); /*
		return ((neSimulator*)addr)->GetMaterial(index, materialTmp[0], materialTmp[1]);
	*/

	public void advance(float sec, int nSteps) {
		advanceJni(addr, sec, nSteps);
	}
	
	private static native void advanceJni(long addr, float sec, int nSteps); /*
		((neSimulator*)addr)->Advance(sec, nSteps);
	*/
	
	public void advance(float sec, float minTimeStep, float maxTimeStep) {
		advanceJni(addr, sec, minTimeStep, maxTimeStep);
	}
	
	private static native void advanceJni(long addr, float sec, float minTimeStep, float maxTimeStep); /*
		((neSimulator*)addr)->Advance(sec, minTimeStep, maxTimeStep);
	*/
	
	public void setTerrainMesh(TriangleMesh tris) {
		// FIXME
	}
	
	public void freeTerrainMesh() {
		// FIXME
	}
	
	public Joint createJoint(RigidBody bodyA) {
		Joint joint = new Joint(createJointJni(addr, bodyA.addr));
		objects.put(joint.addr, joint);
		return joint;
	}
	
	private static native long createJointJni(long addr, long addrBody); /*
		return (jlong)((neSimulator*)addr)->CreateJoint((neRigidBody*)addrBody);
	*/

	public Joint createJoint(RigidBody bodyA, RigidBody bodyB) {
		Joint joint = new Joint(createJointJni(addr, bodyA.addr, bodyB.addr));
		objects.put(joint.addr, joint);
		return joint;
	}
	
	private static native long createJointJni(long addr, long addrBodyA, long addrBodyB); /*
		return (jlong)((neSimulator*)addr)->CreateJoint((neRigidBody*)addrBodyA, (neRigidBody*)addrBodyB);
	*/
	
	public Joint createJoint(RigidBody bodyA, AnimatedBody bodyB) {
		Joint joint = new Joint(createJointAnimatedBodyJni(addr, bodyA.addr, bodyB.addr));
		objects.put(joint.addr, joint);
		return joint;
	}
	
	private static native long createJointAnimatedBodyJni(long addr, long addrBodyA, long addrBodyB); /*
		return (jlong)((neSimulator*)addr)->CreateJoint((neRigidBody*)addrBodyA, (neAnimatedBody*)addrBodyB);
	*/

	public void freeJoint(Joint joint) {
		freeJointJni(addr, joint.addr);
	}
	
	private static native void freeJointJni(long addr, long addrJoint); /*
		((neSimulator*)addr)->FreeJoint((neJoint*)addrJoint);
	*/
	
	public Vector3 getGravity() {
		getGravityJni(addr, tmpFloat);
		gravity.x = tmpFloat[0];
		gravity.y = tmpFloat[1];
		gravity.z = tmpFloat[2];
		return gravity;
	}
	
	private static native void getGravityJni(long addr, float[] gravity); /*
		neV3 vec;
		vec = ((neSimulator*)addr)->Gravity();
		gravity[0] = vec.X();
		gravity[1] = vec.Y();
		gravity[2] = vec.Z();
	*/

	public void setGravity(Vector3 gravity) {
		setGravityJni(addr, gravity.x, gravity.y, gravity.z);
	}
	
	public void setGravity(float x, float y, float z) {
		setGravityJni(addr, x, y, z);
	}

	private static native void setGravityJni(long addr, float x, float y, float z); /*
		neV3 vec;
		vec.Set(x, y, z);
		((neSimulator*)addr)->Gravity(vec);
	*/
	
	
	public void setBreakageCallback(BreakageCallback callback) {
		// FIXME
	}
	
	public BreakageCallback getBreakageCallback() {
		// FIXME
		return null;
	}
	
	public void setCollisionCallback(CollisionCallback callback) {
		// FIXME
	}
	
	public CollisionCallback getCollisionCallback() {
		// FIXME
		return null;
	}
	
	public void setTerrainTriangleQueryCallback(TerrainTriangleQueryCallback callback) {
		// FIXME
	}
	
	public TerrainTriangleQueryCallback getTerrainTriangleQueryCallback() {
		// FIXME
		return null;
	}
	
	public SimulatorSizeInfo getCurrentSizeInfo() {
		return null;
	}
	
	public SimulatorSizeInfo getStartSizeInfo() {
		return null;
	}
	
	public int getMemoryAllocated() {
		return getMemoryAllocatedJni(addr);
	}

	private static native int getMemoryAllocatedJni(long addr); /*
		s32 memory;
		((neSimulator*)addr)->GetMemoryAllocated(memory);
		return memory;
	*/
}