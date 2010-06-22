package com.badlogic.gdxinvaders.simulation;

import com.badlogic.gdx.math.Vector3;

public class Block
{	
	public final static float BLOCK_RADIUS = 0.5f;
	
	public Vector3 position = new Vector3( );
	
	public Block(Vector3 position) 
	{
		this.position.set( position );
	}
}
