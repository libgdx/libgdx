package com.badlogic.gdx;

public interface AudioDevice 
{
	public void writeSamples( short[] samples );
	
	public void writeSamples( float[] samples );
}
