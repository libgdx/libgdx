package com.badlogic.gdx.models.md2;



public class MD2Instance 
{
	public MD2Mesh mesh;	
	public final float frameDuration;	
	public float accumulator = 0;
	public int frameIdx = 0;
	public int startIdx = 0;
	public int endIdx = 0;
	
	public MD2Instance( MD2Mesh mesh, float frameDuration )
	{
		this.mesh = mesh;
		this.frameDuration = frameDuration;
		this.startIdx = 0;
		this.endIdx = mesh.frames.length-1;
	}
	
	public void setFrameRange( int startIdx, int endIdx )
	{
		this.startIdx = startIdx;
		this.frameIdx = startIdx;
		this.endIdx = endIdx;
	}
	
	public void render( float deltaTime )
	{
		accumulator+=deltaTime;	
		
		while( accumulator > frameDuration )
		{
			accumulator -= frameDuration;
			frameIdx++;
			if( frameIdx > endIdx )
				frameIdx = startIdx;
		}		
		
		int endIdx = frameIdx +1;
		if( endIdx > this.endIdx )
			endIdx = startIdx;
		
		float alpha = accumulator / frameDuration;		
		mesh.render( frameIdx, endIdx, alpha );
	}
}
