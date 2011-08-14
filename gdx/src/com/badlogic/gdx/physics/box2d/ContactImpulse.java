
package com.badlogic.gdx.physics.box2d;

public class ContactImpulse {
	final World world;
	long addr;
	float[] tmp = new float[2];
	final float[] normalImpulses = new float[2];
	final float[] tangentImpulses = new float[2];

	protected ContactImpulse (World world, long addr) {
		this.world = world;
		this.addr = addr;
	}

	public float[] getNormalImpulses () {
		jniGetNormalImpulses(addr, normalImpulses);
		return normalImpulses;
	}

	private native void jniGetNormalImpulses (long addr, float[] values);

	public float[] getTangentImpulses () {
		jniGetTangentImpulses(addr, tangentImpulses);
		return tangentImpulses;
	}

	private native void jniGetTangentImpulses (long addr, float[] values);
}
