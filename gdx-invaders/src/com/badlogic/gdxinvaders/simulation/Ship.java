package com.badlogic.gdxinvaders.simulation;

import com.badlogic.gdx.math.Vector3;

public class Ship
{
	public static final float SHIP_RADIUS = 1;
	public static final float SHIP_VELOCITY = 20;
	public final Vector3 position = new Vector3( 0, 0, 0 );
	public int lives = 3;
	public boolean isExploding = false;
	public float explodeTime = 0;		
	
	public void update( float delta )
	{
		if( isExploding )
		{
			explodeTime += delta;
			if( explodeTime > Explosion.EXPLOSION_LIVE_TIME )
			{
				isExploding = false;
				explodeTime = 0;
			}
		}
	}
}
