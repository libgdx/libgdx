package com.badlogic.gdx.scenes.scene2d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.graphics.SpriteBatch;
import com.badlogic.gdx.math.Matrix;
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
	private final Matrix3 transform = new Matrix3( );
	private final Matrix3 tmp = new Matrix3( );
	private final List<Actor> actors = new ArrayList<Actor>( );
	private final Map<String, Actor> namesToActors = new HashMap<String, Actor>( );
	
	public Group( String name )
	{
		super( name );
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
	
	public void insert( Actor actor )
	{
		actors.add( actor );
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
	
	public static void main( String[] argv )
	{
		Matrix3 mat = new Matrix3( );
		Matrix3 tmp = new Matrix3( );
		Vector2 v = new Vector2( 1, 0 );
		
		mat.mul( tmp.setToScaling( 2, 1 ) );
		mat.mul( tmp.setToRotation( 90 ) );
		mat.mul( tmp.setToTranslation(1, 0 ) );
		
		v.mul( mat );
		System.out.println( v );
		
		Matrix mat4 = new Matrix( );
		Matrix tmp4 = new Matrix( );
		Vector3 v3 = new Vector3( 1, 0, 0 );
		
		mat4.mul( tmp4.setToScaling( 2, 1, 0 ) );
		mat4.mul( tmp4.setToRotation( new Vector3( 0, 0, 1 ), 90 ) );
		mat4.mul( tmp4.setToTranslation( 1, 0, 0 ) );
		
		
		v3.mul( mat4 );
		System.out.println( v3 );
	}
}
