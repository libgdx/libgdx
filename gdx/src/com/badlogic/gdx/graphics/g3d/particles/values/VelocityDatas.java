package com.badlogic.gdx.graphics.g3d.particles.values;

public final class VelocityDatas {
	
	/** Common interface to all the velocities data relative to a particle */
	public static interface VelocityData{}
	
	public static class StrengthVelocityData implements VelocityData{
		float strengthStart, strengthDiff;
	}
	
	public static class AngularVelocityData extends StrengthVelocityData{
		public float 	thetaStart, thetaDiff,
							phistart, phiDiff;
	}
	
}
