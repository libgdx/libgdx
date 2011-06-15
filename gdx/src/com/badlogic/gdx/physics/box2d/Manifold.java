package com.badlogic.gdx.physics.box2d;

import com.badlogic.gdx.math.Vector2;

public class Manifold {
	final World world;
	long addr;
	final ManifoldPoint[] points = new ManifoldPoint[] { new ManifoldPoint(), new ManifoldPoint() };
	final Vector2 localNormal = new Vector2();
	final Vector2 localPoint = new Vector2();	
	
	final int[] tmpInt = new int[2];
	final float[] tmpFloat = new float[4];
	
	protected Manifold(World world, long addr) {
		this.world = world;
		this.addr = addr;
	}
	
	public ManifoldType getType() {
		int type = jniGetType(addr);
		if(type == 0) return ManifoldType.Circle;
		if(type == 1) return ManifoldType.FaceA;
		if(type == 2) return ManifoldType.FaceB;
		return ManifoldType.Circle;
	}
	
	private native int jniGetType(long addr);
	
	public int getPointCount() {
		return jniGetPointCount(addr);
	}
	
	private native int jniGetPointCount(long addr);
	
	public Vector2 getLocalNormal() {
		jniGetLocalNormal(addr, tmpFloat);
		localNormal.set(tmpFloat[0], tmpFloat[1]);
		return localNormal;
	}
	
	private native void jniGetLocalNormal(long addr, float[] values);
	
	public Vector2 getLocalPoint() {
		jniGetLocalPoint(addr, tmpFloat);
		localPoint.set(tmpFloat[0], tmpFloat[1]);
		return localPoint;
	}
	
	private native void jniGetLocalPoint(long addr, float[] values);
	
	public ManifoldPoint[] getPoints() {
		int count = jniGetPointCount(addr);
		
		for(int i = 0; i < count; i++) {
			int contactID = jniGetPoint(addr, tmpFloat, i);
			ManifoldPoint point = points[i];
			point.contactID = contactID;
			point.localPoint.set(tmpFloat[0], tmpFloat[1]);
			point.normalImpulse = tmpFloat[2];
			point.tangentImpulse = tmpFloat[3];
		}
		
		return points;
	}
	
	private native int jniGetPoint(long addr, float[] values, int i);	
	
	public class ManifoldPoint {
		public final Vector2 localPoint = new Vector2();
		public float normalImpulse;
		public float tangentImpulse;
		public int contactID = 0;
		
		public String toString() {
			return "id: " + contactID + ", " + localPoint + ", "  + normalImpulse + ", " + tangentImpulse;
		}
	}
	
	public enum ManifoldType {
		Circle,
		FaceA,
		FaceB
	}	
}
