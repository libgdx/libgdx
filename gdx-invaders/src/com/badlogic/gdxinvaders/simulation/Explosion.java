package com.badlogic.gdxinvaders.simulation;

import com.badlogic.gdx.math.Vector3;

public class Explosion
{
	public static final float EXPLOSION_LIVE_TIME = 1;
	public float aliveTime = 0;
	public final Vector3 position = new Vector3( );
	
	public Explosion( Vector3 position )
	{
		this.position.set( position );
	}
	
	public void update( float delta )
	{
		aliveTime += delta;
	}	
}
