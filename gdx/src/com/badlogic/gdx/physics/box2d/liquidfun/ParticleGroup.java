
package com.badlogic.gdx.physics.box2d.liquidfun;

import com.badlogic.gdx.math.Vector2;

/** Returned by createParticleGroup() in {@link ParticleSystem}
 * @author FinnStr */
public class ParticleGroup {
	// @off
	/*JNI
#include <Box2D/Box2D.h>
	*/
	
	protected long addr;
	
	private Object userData;
	
	protected ParticleGroup(long pAddr) {
		addr = pAddr;
	}
	
	public void setUsetData(Object pObject) {
		userData = pObject;
	}
	
	public Object getUserData() {
		return userData;
	}
	
	public int getBufferIndex() {
		return jniGetBufferIndex(addr);
	}
	
	private native int jniGetBufferIndex(long addr); /*
		b2ParticleGroup* group = (b2ParticleGroup*)addr;
		return (jint)group->GetBufferIndex();
	*/
	
	public int getGroupFlags() {
		return jniGetGroupFlags(addr);
	}
	
	private native int jniGetGroupFlags(long addr); /*
		b2ParticleGroup* group = (b2ParticleGroup*)addr;
		return (jint)group->GetGroupFlags();
	*/

	public void setGroupFlags(int pFlags) {
		jniSetGroupFlags(addr, pFlags);
	}
	
	private native void jniSetGroupFlags(long addr, int pFlags);	/*
		b2ParticleGroup* group = (b2ParticleGroup*)addr;
		group->SetGroupFlags(pFlags);
	 */
	
	public float getMass() {
		return jniGetMass(addr);
	}
	
	private native float jniGetMass(long addr); /*
		b2ParticleGroup* group = (b2ParticleGroup*)addr;
		return (jfloat)group->GetMass();
	*/
	
	public float getInertia() {
		return jniGetInertia(addr);
	}
	
	private native float jniGetInertia(long addr); /*
		b2ParticleGroup* group = (b2ParticleGroup*)addr;
		return (jfloat)group->GetInertia();
	*/
	
	public Vector2 getCenter() {
		return new Vector2(jniGetCenterX(addr), jniGetCenterY(addr));
	}
	
	private native float jniGetCenterX(long addr); /*
		b2ParticleGroup* group = (b2ParticleGroup*)addr;
		return (jfloat)group->GetCenter().x;
	*/
	private native float jniGetCenterY(long addr); /*
		b2ParticleGroup* group = (b2ParticleGroup*)addr;
		return (jfloat)group->GetCenter().y;
	*/
	
	public Vector2 getLinearVelocity() {
		return new Vector2(jniGetLinVelocityX(addr), jniGetLinVelocityY(addr));
	}
	
	private native float jniGetLinVelocityX(long addr); /*
		b2ParticleGroup* group = (b2ParticleGroup*)addr;
		return (jfloat)group->GetLinearVelocity().x;
	*/
	private native float jniGetLinVelocityY(long addr); /*
		b2ParticleGroup* group = (b2ParticleGroup*)addr;
		return (jfloat)group->GetLinearVelocity().x;
	*/
	
	public float getAngularVelocity() {
		return jniGetAngularVelocity(addr);
	}
	
	private native float jniGetAngularVelocity(long addr); /*
		b2ParticleGroup* group = (b2ParticleGroup*)addr;
		return (jfloat)group->GetAngularVelocity();
	*/
	
	public Vector2 getPosition() {
		return new Vector2(jniGetPositionX(addr), jniGetPositionY(addr));
	}
	
	public native float jniGetPositionX(long addr); /*
		b2ParticleGroup* group = (b2ParticleGroup*)addr;
		return (jfloat)group->GetPosition().x;
	*/
	public native float jniGetPositionY(long addr); /*
		b2ParticleGroup* group = (b2ParticleGroup*)addr;
		return (jfloat)group->GetPosition().y;
	*/
	
	public float getAngle() {
		return jniGetAngle(addr);
	}
	
	private native float jniGetAngle(long addr); /*
		b2ParticleGroup* group = (b2ParticleGroup*)addr;
		return (jfloat)group->GetAngle();
	 */ 
}
