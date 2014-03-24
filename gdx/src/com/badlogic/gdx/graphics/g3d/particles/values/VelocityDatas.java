package com.badlogic.gdx.graphics.g3d.particles.values;

/** Contains generic {@link VelocityData} used by {@link VelocityValue}. */
/** @author Inferno */
public final class VelocityDatas {
	
	/** Common interface to all the velocities data relative to a particle */
	public static interface VelocityData{}

	/** Generally used by {@link VelocityValue} dealing with magnitude only.*/
	public static class StrengthVelocityData implements VelocityData{
		float strengthStart, strengthDiff;
	}
	
	/** Generally used by {@link VelocityValue} dealing with direction and magnitude.*/
	public static class AngularVelocityData extends StrengthVelocityData{
		public float 	thetaStart, thetaDiff,
							phistart, phiDiff;
	}
	
}
