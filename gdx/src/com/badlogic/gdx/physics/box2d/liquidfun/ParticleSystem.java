
package com.badlogic.gdx.physics.box2d.liquidfun;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.Transform;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.LongMap;
import com.badlogic.gdx.utils.Pool;

/** Manages all particles; read http://google.github.io/liquidfun/ for more information
 * @author FinnStr */
public class ParticleSystem {
	// @off
	/*JNI
#include <Box2D/Box2D.h>
	*/
	
	protected final Pool<ParticleGroup> freeParticleGroups = new Pool<ParticleGroup>(100, 200) {
		@Override
		protected ParticleGroup newObject() {
			return new ParticleGroup(0);
		}
	};
	
	private final long worldAddr;
	
	/** all known particleGroups **/
	protected final LongMap<ParticleGroup> particleGroups = new LongMap<ParticleGroup>(100);
	
	public ParticleSystem(World pWorld) {	
		worldAddr = pWorld.getAddress();
	}
	
	/** @return Returns the index of the particle. */
	public int createParticle(ParticleDef pDef) {
		int flags;
		if(pDef.flags.size == 0) flags = 0;
		else {
			flags = pDef.flags.get(0).getValue();
			for(int i = 1; i < pDef.flags.size; i++) {
				flags = ((int)(flags | pDef.flags.get(i).getValue()));
			}
		}
		
		return jniCreateParticle(worldAddr, flags, pDef.position.x, pDef.position.y, pDef.velocitiy.x, pDef.velocitiy.y,
			(int) (pDef.color.r * 255f), (int) (pDef.color.g * 255f), (int) (pDef.color.b * 255f), (int) (pDef.color.a * 255f));
	}
	
	private native int jniCreateParticle(long addr, int pFlags, float pPositionX, float pPositionY, float pVelocityX, float pVelocityY, 
			int pColorR, int pColorG, int pColorB, int pColorA); /*
		b2ParticleDef particleDef;
		particleDef.flags = pFlags;
		particleDef.position.Set( pPositionX, pPositionY );
		particleDef.velocity.Set( pVelocityX, pVelocityY );
		particleDef.color.Set(pColorR, pColorG, pColorB, pColorA);
		
		b2World* world = (b2World*)addr;
		int32 index = world->CreateParticle( particleDef );
		return (jint)index;
	*/

	/** Removes a particle
	 * @param pIndex The index of the particle given by createParticle() */
	public void destroyParticle(int pIndex) {
		jniDestroyParticle(worldAddr, pIndex);
	}
	
	private native void jniDestroyParticle(long addr, int pIndex); /*
		b2World* world = (b2World*)addr;
		world->DestroyParticle( pIndex );
	*/
	
	/** Removes all particles in the bounds of the shape
	 * @param pShape
	 * @param pTransform transformation of the shape
	 * @return the number of particles destroyed*/
	public int destroyParticleInShape(Shape pShape, Transform pTransform) {
		return jniDestroyParticleInShape(worldAddr, pShape.getAddress(), pTransform.getPosition().x, pTransform.getPosition().y, pTransform.getRotation());
	}
	
	private native int jniDestroyParticleInShape(long addr, long pShapeAddr, float pTransformPosX, float pTransformPosY, float pAngle); /*
		b2Shape* shape = (b2Shape*)pShapeAddr;
		b2Transform transform;
		transform.Set( b2Vec2( pTransformPosX, pTransformPosY ), pAngle );
		
		b2World* world = (b2World*)addr;
		return (jint)world->DestroyParticlesInShape( *shape, transform );
	*/

	public ParticleGroup createParticleGroup(ParticleGroupDef pGroupDef) {
		int flags;
		if(pGroupDef.flags.size == 0) flags = 0;
		else {
			flags = pGroupDef.flags.get(0).getValue();
			for(int i = 1; i < pGroupDef.flags.size; i++) {
				flags = ((int)(flags | pGroupDef.flags.get(i).getValue()));
			}
		}
		
		int groupFlags;
		if(pGroupDef.groupFlags.size == 0) groupFlags = 0;
		else {
			groupFlags = pGroupDef.groupFlags.get(0).getValue();
			for(int i = 1; i < pGroupDef.groupFlags.size; i++) {
				flags = ((int)(flags | pGroupDef.groupFlags.get(i).getValue()));
			}
		}
		
		long addrParticleGroup = jniCreateParticleGroup(worldAddr, flags, groupFlags, pGroupDef.position.x, pGroupDef.position.y, pGroupDef.angle,
				pGroupDef.linearVelocity.x, pGroupDef.linearVelocity.y, pGroupDef.angularVelocity, 
				(int) (pGroupDef.color.r * 255f), (int) (pGroupDef.color.g * 255f), (int) (pGroupDef.color.b * 255f), (int) (pGroupDef.color.a * 255f),
				pGroupDef.strength, pGroupDef.shape.getAddress(), pGroupDef.destroyAutomatically);
		
		ParticleGroup group = freeParticleGroups.obtain();
		group.addr = addrParticleGroup;
		this.particleGroups.put(addrParticleGroup, group);
		return group;
	}
	
	private native long jniCreateParticleGroup(long addr, int pFlags, int pGroupFlags, float pPositionX, float pPositionY, float pAngle, float pLinVelocityX, float pLinVelocityY, 
			float pAngularVelocity, int pColorR, int pColorG, int pColorB, int pColorA, float pStrength, long pShapeAddr, boolean pDestroyAutomatically); /*
		b2ParticleGroupDef groupDef;
		groupDef.flags = pFlags;
		groupDef.groupFlags = pGroupFlags;
		groupDef.position.Set( pPositionX, pPositionY );
		groupDef.angle = pAngle;
		groupDef.linearVelocity.Set( pLinVelocityX, pLinVelocityY );
		groupDef.angularVelocity = pAngularVelocity;
		groupDef.color.Set( pColorR, pColorG, pColorB, pColorA );
		groupDef.strength = pStrength;
		groupDef.shape = (b2Shape*)pShapeAddr;
		groupDef.destroyAutomatically = pDestroyAutomatically;
		
		b2World* world = (b2World*)addr;
		return (jlong)world->CreateParticleGroup( groupDef );
	*/

	public void destroyParticlesInGroup(ParticleGroup pGroup) {
		jniDestroyParticlesInGroup(worldAddr, pGroup.addr);
	}
	
	private native void jniDestroyParticlesInGroup(long addr, long groupAddr); /*
		b2ParticleGroup* group = (b2ParticleGroup*)groupAddr;
	
		b2World* world = (b2World*)addr;
		world->DestroyParticlesInGroup( group );
	*/
	
	/** Join two particle groups. This function is locked during callbacks.
	 * @param pGroupA first group. Expands to encompass the second group.
	 * @param pGroupB second group. It is destroyed. */
	public void joinParticleGroups(ParticleGroup pGroupA, ParticleGroup pGroupB) {
		jniJoinParticleGroups(worldAddr, pGroupA.addr, pGroupB.addr);
	}
	
	private native void jniJoinParticleGroups(long addr, long addrA, long addrB); /*
		b2ParticleGroup* groupA = (b2ParticleGroup*)addrA;
		b2ParticleGroup* groupB = (b2ParticleGroup*)addrB;
	
		b2World* world = (b2World*)addr;
		world->JoinParticleGroups( groupA, groupB );
	*/
	
	public void destroyParticleGroup(ParticleGroup pGroup) {
		pGroup.setUsetData(null);
		this.particleGroups.remove(pGroup.addr);
		jniDestroyParticleGroup(worldAddr, pGroup.addr);
		freeParticleGroups.free(pGroup);
	}
	
	private native void jniDestroyParticleGroup(long addr, long groupAddr); /*
		b2ParticleGroup* group = (b2ParticleGroup*)groupAddr;
	
		b2World* world = (b2World*)addr;
		world->DestroyParticlesInGroup( group );
	*/

	private final static Array<Vector2> mPositions = new Array<Vector2>();
	
	/** Reloads the positionbuffer from native code and returns it */
	public Array<Vector2> getParticlePositionBuffer() {
		updateParticlePositionBuffer();
		return mPositions;
	}
	
	private native float[] jniGetParticlePositionBufferX(long addr); /*
		b2World* world = (b2World*)addr;
		int32 count = world->GetParticleCount();
		
		jfloatArray array;
		array = env->NewFloatArray((jsize) count);
		
		jfloat fill[count];
		for(int i = 0; i < count; i++) {
			fill[i] = world->GetParticlePositionBuffer()[i].x;
		}
		
		env->SetFloatArrayRegion(array, 0, (jsize) count, fill);
 		return array;
	*/
	private native float[] jniGetParticlePositionBufferY(long addr); /*
		b2World* world = (b2World*)addr;
		int32 count = world->GetParticleCount();
		
		jfloatArray array;
		array = env->NewFloatArray((jsize) count);
		
		jfloat fill[count];
		for(int i = 0; i < count; i++) {
			fill[i] = world->GetParticlePositionBuffer()[i].y;
		}
		
		env->SetFloatArrayRegion(array, 0, (jsize) count, fill);
		return array;
	*/
	
	private final static Array<Vector2> mVelocities = new Array<Vector2>();
	
	/** Reloads the velocitybuffer from native code and returns it */
	public Array<Vector2> getParticleVelocityBuffer() {
		updateParticleVelocitiyBuffer();
		return mVelocities;
	}
	
	private native float[] jniGetParticleVelocityBufferX(long addr); /*
		b2World* world = (b2World*)addr;
		int32 count = world->GetParticleCount();
		
		jfloatArray array;
		array = env->NewFloatArray((jsize) count);
		
		jfloat fill[count];
		for(int i = 0; i < count; i++) {
			fill[i] = world->GetParticleVelocityBuffer()[i].x;
		}
		
		env->SetFloatArrayRegion(array, 0, (jsize) count, fill);
 		return array;
	*/
	private native float[] jniGetParticleVelocityBufferY(long addr); /*
		b2World* world = (b2World*)addr;
		int32 count = world->GetParticleCount();
		
		jfloatArray array;
		array = env->NewFloatArray((jsize) count);
		
		jfloat fill[count];
		for(int i = 0; i < count; i++) {
			fill[i] = world->GetParticleVelocityBuffer()[i].y;
		}
		
		env->SetFloatArrayRegion(array, 0, (jsize) count, fill);
		return array;
	*/
	
	private final static Array<Color> mColors = new Array<Color>();
	
	/** Reloads the colorbuffer from native code and returns it */
	public Array<Color> getParticleColorBuffer() {
		updateParticleColorBuffer();
		return mColors;
	}
	
	private native int[] jniGetParticleColorBufferR(long addr); /*
		b2World* world = (b2World*)addr;
		int32 count = world->GetParticleCount();
		
		jintArray array;
		array = env->NewIntArray((jsize) count);
		
		jint fill[count];
		for(int i = 0; i < count; i++) {
			fill[i] = world->GetParticleColorBuffer()[i].r;
		}
		
		env->SetIntArrayRegion(array, 0, (jsize) count, fill);
		return array;
	*/
	
	private native int[] jniGetParticleColorBufferG(long addr); /*
		b2World* world = (b2World*)addr;
		int32 count = world->GetParticleCount();
		
		jintArray array;
		array = env->NewIntArray((jsize) count);
		
		jint fill[count];
		for(int i = 0; i < count; i++) {
			fill[i] = world->GetParticleColorBuffer()[i].g;
		}
		
		env->SetIntArrayRegion(array, 0, (jsize) count, fill);
		return array;
	*/
	
	private native int[] jniGetParticleColorBufferB(long addr); /*
		b2World* world = (b2World*)addr;
		int32 count = world->GetParticleCount();
		
		jintArray array;
		array = env->NewIntArray((jsize) count);
		
		jint fill[count];
		for(int i = 0; i < count; i++) {
			fill[i] = world->GetParticleColorBuffer()[i].b;
		}
		
		env->SetIntArrayRegion(array, 0, (jsize) count, fill);
		return array;
	*/
	
	private native int[] jniGetParticleColorBufferA(long addr); /*
		b2World* world = (b2World*)addr;
		int32 count = world->GetParticleCount();
		
		jintArray array;
		array = env->NewIntArray((jsize) count);
		
		jint fill[count];
		for(int i = 0; i < count; i++) {
			fill[i] = world->GetParticleColorBuffer()[i].a;
		}
		
		env->SetIntArrayRegion(array, 0, (jsize) count, fill);
		return array;
	*/
	
	/** Reloads the positionbuffer from native code */
	public void updateParticlePositionBuffer() {
		mPositions.ensureCapacity(getParticleCount());
		mPositions.clear();
		
		float[] x = jniGetParticlePositionBufferX(worldAddr);
		float[] y = jniGetParticlePositionBufferY(worldAddr);
		
		for(int i = 0; i < getParticleCount(); i++) {
			mPositions.add(new Vector2(x[i], y[i]));
		}
	}
	
	/** Reloads the velocitybuffer from native code */
	public void updateParticleVelocitiyBuffer() {
		mVelocities.ensureCapacity(getParticleCount());
		mVelocities.clear();
		
		float[] x = jniGetParticleVelocityBufferX(worldAddr);
		float[] y = jniGetParticleVelocityBufferY(worldAddr);
		
		for(int i = 0; i < getParticleCount(); i++) {
			mVelocities.add(new Vector2(x[i], y[i]));
		}
	}
	
	/** Reloads the colorbuffer from native code */
	public void updateParticleColorBuffer() {
		mColors.ensureCapacity(getParticleCount());
		mColors.clear();
		
		int[] r = jniGetParticleColorBufferR(worldAddr);
		int[] g = jniGetParticleColorBufferG(worldAddr);
		int[] b = jniGetParticleColorBufferB(worldAddr);
		int[] a = jniGetParticleColorBufferA(worldAddr);
		
		for(int i = 0; i < getParticleCount(); i++) {
			mColors.add(new Color(r[i] / 255f, g[i] / 255f, b[i] / 255f, a[i] / 255f));
		}
	}
	
	/** Reloads all buffers from native code */
	public void updateAllParticleBuffers() {
		updateParticlePositionBuffer();
		updateParticleVelocitiyBuffer();
		updateParticleColorBuffer();
	}
	
	/** returns the positionbuffer without reloading it */
	public Array<Vector2> getParticlePositionBufferWithoutUpdate() {
		return mPositions;
	}
	
	/** returns the velocitybuffer without reloading it */
	public Array<Vector2> getParticleVelocityBufferWithoutUpdate() {
		return mVelocities;
	}
	
	/** returns the colorbuffer without reloading it */
	public Array<Color> getParticleColorBufferWithoutUpdate() {
		return mColors;
	}
	
	public void setParticleRadius(float pRadius) {
		jniSetParticleRadius(worldAddr, pRadius);
	}
	
	private native void jniSetParticleRadius(long addr, float pRadius); /*
		b2World* world = (b2World*)addr;
		world->SetParticleRadius( pRadius );
	*/
	
	public float getParticleRadius() {
		return jniGetParticleRadius(worldAddr);
	}
	
	private native float jniGetParticleRadius(long addr); /*
		b2World* world = (b2World*)addr;
		return (jfloat)world->GetParticleRadius();
	*/
	
	/** The total count of particles currently in the simulation */
	public int getParticleCount() {
		return jniGetParticleCount(worldAddr);
	}
	
	private native int jniGetParticleCount(long addr); /*
		b2World* world = (b2World*)addr;
		return (jint)world->GetParticleCount();
	*/
	
	public void setParticleDensity(float pDensity) {
		jniSetParticleDensity(worldAddr, pDensity);
	}
	
	private native void jniSetParticleDensity(long addr, float pDensity); /*
		b2World* world = (b2World*)addr;
		world->SetParticleDensity(pDensity);
	*/
	
	public float getParticleDensity() {
		return jniGetParticleDensity(worldAddr);
	}
	
	private native float jniGetParticleDensity(long addr); /*
		b2World* world = (b2World*)addr;
		return (jfloat)world->GetParticleDensity();
	*/
	
	public void setParticleGravityScale(float pGravityScale) {
		jniSetParticleGravityScale(worldAddr, pGravityScale);
	}
	
	private native float jniSetParticleGravityScale(long addr, float pGravityScale); /*
		b2World* world = (b2World*)addr;
		world->SetParticleGravityScale(pGravityScale);
	*/
	
	public float getParticleGravityScale() {
		return jniGetParticleGravityScale(worldAddr);
	}
	
	private native float jniGetParticleGravityScale(long addr); /*
		b2World* world = (b2World*)addr;
		return (jfloat)world->GetParticleGravityScale();
	*/
	
	public void setParticleMaxCount(int pCount) {
		jniSetParticleMaxCount(worldAddr, pCount);
	}
	
	private native void jniSetParticleMaxCount(long addr, float pCount); /*
		b2World* world = (b2World*)addr;
		world->SetParticleMaxCount(pCount);
	*/
	
	public float getParticleMaxCount() {
		return jniGetParticleMaxCount(worldAddr);
	}
	
	private native float jniGetParticleMaxCount(long addr); /*
		b2World* world = (b2World*)addr;
		return (jint)world->GetParticleMaxCount();
	*/
	
	public void setParticleDamping(float pDamping) {
		jniSetParticleDamping(worldAddr, pDamping);
	}
	
	private native void jniSetParticleDamping(long addr, float pDamping); /*
		b2World* world = (b2World*)addr;
		world->SetParticleDamping(pDamping);
	*/
	
	public float getParticleDamping() {
		return jniGetParticleDamping(worldAddr);
	}
	
	private native float jniGetParticleDamping(long addr); /*
		b2World* world = (b2World*)addr;
		return (jfloat)world->GetParticleDamping();
	*/
	
	public String getVersionString() {
		return jniGetVersionString(worldAddr);
	}
	
	private native String jniGetVersionString(long addr); /*
		b2World* world = (b2World*)addr;
		const char* version = world->GetVersionString();
		return env->NewStringUTF(version);
	*/
}
