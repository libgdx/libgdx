/**
 *  This file is part of Libgdx by Mario Zechner (badlogicgames@gmail.com)
 *
 *  Libgdx is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Libgdx is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.badlogic.gdx.graphics.models;



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
