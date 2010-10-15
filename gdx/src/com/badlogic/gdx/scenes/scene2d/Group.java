package com.badlogic.gdx.scenes.scene2d;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.graphics.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/**
 * A group is an Actor that contains other Actors (also other Groups which are Actors).
 * 
 * @author mzechner
 *
 */
public class Group extends Actor
{
	private final Matrix3 transform;
	private final Matrix3 tmp;
	private final List<Actor> children; // TODO O(n) delete, baaad.
	private final List<Actor> immutableChildren; 
	private final List<Group> groups; // TODO O(n) delete, baad.
	private final List<Group> immutableGroups;
	private final Map<String, Actor> namesToActors;
	
	public Group( String name )
	{
		super( name );
		this.transform = new Matrix3( );
		this.tmp = new Matrix3( );
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
			transform.mul( tmp.setToScaling( scaleX, scaleY ) );
		if( rotation != 0 )
			transform.mul( tmp.setToRotation( rotation ) );
		if( refX != 0 || refY != 0 )
			transform.mul( tmp.setToTranslation( -refX, -refY ) );
		if( x != 0 || y != 0 )
			transform.mul( tmp.setToTranslation( x, y ) );
	}
	
	public void add( Actor actor )
	{
		children.add( actor );
		if( actor instanceof Group )
			groups.add( (Group)actor );
		namesToActors.put( actor.name, actor );
	}
	
	@Override
	void render(Stage stage, SpriteBatch batch) 
	{
		updateTransform( );
	}

	@Override
	boolean hit(int x, int y) 
	{
		return false;
	}

	@Override
	void touchDown(int x, int y, int pointer) 
	{
		
	}

	@Override
	void touchUp(int x, int y, int pointer) 
	{
		
	}

	@Override
	void touchDragged(int x, int y, int pointer) 
	{
		
	}

	@Override
	void keyDown(int keycode) 
	{
		
	}

	@Override
	void keyUp(int keycode) 
	{
		
	}

	@Override
	void keytyped(int keycode) 
	{
		
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
