package com.badlogic.gdx.audio.analysis;

/**
 * Some signal metric functions like energy, power etc.
 * @author mzechner
 *
 */
public class Signals 
{
	public static float mean( float[] signal )
	{
		float mean = 0;
		for( int i = 0; i < signal.length; i++ )
			mean+=signal[i];
		mean /= signal.length;
		return mean;
	}
	
	public static float energy( float[] signal )
	{
		float totalEnergy = 0;
		for( int i = 0; i < signal.length; i++ )		
			totalEnergy += (signal[i] * signal[i]);
		return totalEnergy;
	}

	public static float power(float[] signal ) 
	{	
		return energy( signal ) / signal.length;
	}
	
	public static float norm( float[] signal )
	{
		return (float)Math.sqrt( energy(signal) );
	}
	
	public static float minimum( float[] signal )
	{
		float min = Float.POSITIVE_INFINITY;
		for( int i = 0; i < signal.length; i++ )
			min = Math.min( min, signal[i] );
		return min;
	}
	
	public static float maximum( float[] signal )
	{
		float max = Float.NEGATIVE_INFINITY;
		for( int i = 0; i < signal.length; i++ )
			max = Math.max( max, signal[i] );
		return max;
	}
	
	public static void scale( float[] signal, float scale )
	{
		for( int i = 0; i < signal.length; i++ )
			signal[i] *= scale;
	}
}
