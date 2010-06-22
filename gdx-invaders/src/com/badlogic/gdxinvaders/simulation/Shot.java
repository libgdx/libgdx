package com.badlogic.gdxinvaders.simulation;

import com.badlogic.gdx.math.Vector3;

public class Shot 
{
	public static float SHOT_VELOCITY = 10;
	public final Vector3 position = new Vector3();
	public boolean isInvaderShot;
	public boolean hasLeftField = false;

	public Shot( Vector3 position, boolean isInvaderShot )
	{
		this.position.set( position );
		this.isInvaderShot = isInvaderShot;
	}
	
	public void update(float delta) 
	{	
		if( isInvaderShot )
			position.z += SHOT_VELOCITY * delta;
		else
			position.z -= SHOT_VELOCITY * delta;
		
		if( position.z > Simulation.PLAYFIELD_MAX_Z )
			hasLeftField = true;
		if( position.z < Simulation.PLAYFIELD_MIN_Z )
			hasLeftField = true;
	}
}
