/*******************************************************************************
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.badlogic.gdx.scenes.scene2d;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.graphics.SpriteBatch;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;

/**
 * A group is an Actor that contains other Actors (also other Groups which are Actors).
 * 
 * @author mzechner
 *
 */
public class Group extends Actor
{
	private final Matrix4 tmp4 = new Matrix4( );
	private final Matrix4 oldBatchTransform = new Matrix4( );	
	private final Matrix3 transform;
	private final Matrix3 scenetransform = new Matrix3( );
	
	private final List<Actor> children; // TODO O(n) delete, baaad.
	private final List<Actor> immutableChildren; 
	private final List<Group> groups; // TODO O(n) delete, baad.
	private final List<Group> immutableGroups;
	private final Map<String, Actor> namesToActors;
	
	public Group( String name )
	{
		super( name );
		this.transform = new Matrix3( );
		this.children = new ArrayList<Actor>( );
		this.immutableChildren = Collections.unmodifiableList( this.children );
		this.groups = new ArrayList<Group>( );
		this.immutableGroups = Collections.unmodifiableList( this.groups );
		this.namesToActors = new HashMap<String, Actor>( );
	}
	
	private void updateTransform( )
	{
		transform.idt();
		if( refX != 0 || refY != 0 )
			transform.setToTranslation( refX, refY );
		if( scaleX != 1 || scaleY != 1 )
			transform.mul( scenetransform.setToScaling( scaleX, scaleY ) );
		if( rotation != 0 )
			transform.mul( scenetransform.setToRotation( rotation ) );
		if( refX != 0 || refY != 0 )
			transform.mul( scenetransform.setToTranslation( -refX, -refY ) );
		if( x != 0 || y != 0 )
		{
			transform.getValues()[6] += x;
			transform.getValues()[7] += y;
		}
		
		if( parent != null )
		{
			scenetransform.set(parent.scenetransform);
			scenetransform.mul( transform );
		}
		else
		{
			scenetransform.set(transform);
		}
	}
	
	@Override
	protected void render( SpriteBatch batch ) 
	{
		updateTransform( );
		tmp4.set( scenetransform );		
		
		batch.end();
		Matrix4 projection = batch.getProjectionMatrix();
		oldBatchTransform.set(batch.getTransformMatrix());
		batch.begin( projection, tmp4 );

		int len = children.size();
		for( int i = 0; i < len; i++ )
			children.get(i).render( batch );
		
		batch.end();
		batch.begin( projection, oldBatchTransform );
	}

	final Vector2 point = new Vector2( );

	static final Vector2 xAxis = new Vector2();
	static final Vector2 yAxis = new Vector2();
	static final Vector2 p = new Vector2();
	static final Vector2 ref = new Vector2();
	public static void toChildCoordinates( Actor child, float x, float y, Vector2 out )
	{
		if( child.rotation == 0 )
		{
			if( child.scaleX == 1 && child.scaleY == 1 )
			{
				out.x = x - child.x;
				out.y = y - child.y;
			}
			else
			{
				if( child.refX == 0 && child.refY == 0 )
				{
					out.x = (x - child.x) / child.scaleX;
					out.y = (y - child.y) / child.scaleY;
				}
				else
				{
					out.x = x / child.scaleX - (child.x - child.refX);
					out.x = x / child.scaleX - (child.x - child.refX);
				}
			}
		}
		else
		{
			final float cos = (float)Math.cos( (float)Math.toRadians(child.rotation) );
			final float sin = (float)Math.sin( (float)Math.toRadians(child.rotation) );
			
			if( child.scaleX == 1 && child.scaleY == 1 )
			{
				if( child.refX == 0 && child.refY == 0 )
				{	
					float tox = x - child.x;
					float toy = y - child.y;
					
					out.x = tox * cos + toy * sin;
					out.y = tox * -sin + toy * cos;
				}
				else
				{
					float refX = -sin * child.refX + cos * child.refY;
					float refY =  cos * child.refX + sin * child.refY;
					
					float px = child.x + child.refX - refX;
					float py = child.y + child.refY - refY;
					
					float tox = x - px;
					float toy = y - py;
					
					out.x = tox * cos + toy * sin;
					out.y = tox * -sin + toy * cos;
				}
			}
			else
			{
				if( child.refX == 0 && child.refY == 0 )
				{	
					float tox = x - child.x;
					float toy = y - child.y;
					
					out.x = tox * cos + toy * sin;
					out.y = tox * -sin + toy * cos;
					
					out.x /= child.scaleX;
					out.y /= child.scaleY;
				}
				else
				{
					float srefX = child.refX * child.scaleY;
					float srefY = child.refY * child.scaleX;
					
					float refX = -sin * srefX + cos * srefY;
					float refY =  cos * srefX + sin * srefY;
					
					float px = child.x + child.refX - refX;
					float py = child.y + child.refY - refY;
					
					float tox = x - px;
					float toy = y - py;
					
					out.x = tox * cos + toy * sin;
					out.y = tox * -sin + toy * cos;
					
					out.x /= child.scaleX;
					out.y /= child.scaleY;
				}
			}
		}
	}
	
	public static void slowToChildCoordinateSystem( Actor child, float x, float y, Vector2 out )
	{
		final float cos = (float)Math.cos( (float)Math.toRadians(child.rotation) );
		final float sin = (float)Math.sin( (float)Math.toRadians(child.rotation) );
		
		float refX = -sin * child.refX + cos * child.refY;
		float refY =  cos * child.refX + sin * child.refY;
		
		refX *= child.scaleX;
		refY *= child.scaleY;
		
		float px = child.x + child.refX - refX;
		float py = child.y + child.refY - refY;
		
		float tox = x - px;
		float toy = y - py;
		
		out.x = tox * cos + toy * sin;
		out.y = tox * -sin + toy * cos;
		
		out.x /= child.scaleX;
		out.y /= child.scaleY;
	}
	
	@Override
	protected boolean touchDown(float x, float y, int pointer) 
	{	
		int len = children.size();
		for( int i = 0; i < len; i++ )
		{
			Actor child = children.get(i);
			if( !child.touchable )
				continue;
			
			toChildCoordinates( child, x, y, point );
			
			if( child.touchDown( point.x, point.y, pointer ) )
				return true;
		}
		
		return false;
	}
	
	@Override
	protected boolean touchUp(float x, float y, int pointer) 
	{
		int len = children.size();
		for( int i = 0; i < len; i++ )
		{
			Actor child = children.get(i);
			if( !child.touchable )
				continue;
			
			toChildCoordinates( child, x, y, point );
			
			if( child.touchUp( point.x, point.y, pointer ) )
				return true;
		}
		return false;
	}

	@Override
	protected boolean touchDragged(float x, float y, int pointer) 
	{
		int len = children.size();
		for( int i = 0; i < len; i++ )
		{
			Actor child = children.get(i);
			if( !child.touchable )
				continue;
			
			toChildCoordinates( child, x, y, point );
			
			if( child.touchDragged( point.x, point.y, pointer ) )
				return true;
		}
		return false;
	}
	
	public Actor hit( float x, float y )
	{
		int len = children.size();
		for( int i = 0; i < len; i++ )
		{
			Actor child = children.get(i);
			
			toChildCoordinates( child, x, y, point );
			
			Actor hit = child.hit( point.x, point.y );
			if( hit != null )
			{
				return hit;
			}
		}
		return null;	
	}
	
	public void addActor( Actor actor )
	{
		children.add( actor );
		if( actor instanceof Group )
			groups.add( (Group)actor );
		namesToActors.put( actor.name, actor );
		actor.parent = this;
	}
	
	public void removeActor( Actor actor )
	{
		children.remove( actor );
		if( actor instanceof Group )
			groups.remove( (Group)actor );
		namesToActors.remove( actor.name );
	}
	
	public Actor findActor( String name )
	{
		Actor actor = namesToActors.get( name );
		if( actor == null )
		{
			int len = groups.size();
			for( int i = 0; i < len; i++ )
			{
				actor = groups.get(i).findActor( name );
				if( actor != null )
					return actor;
			}
		}
		
		return actor;
	}
	
	public List<Actor> getActors( )
	{
		return immutableChildren;
	}
	
	public List<Group> getGroups( )
	{
		return immutableGroups;
	}
}
