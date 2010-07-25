package com.badlogic.gdx.tests.box2d;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public class ContinuousTest extends Box2DTest
{
	int m_stepCount = 0;
	Body m_body;
	float m_angularVelocity;	
	
	@Override
	protected void createWorld(World world) 
	{
		{
			BodyDef bd = new BodyDef( );
			bd.position.set( 0, 0 );
			Body body = world.createBody( bd );
			
			PolygonShape shape = new PolygonShape( );
			shape.setAsEdge( new Vector2( -10, 0 ), new Vector2( 10, 0 ) );
			body.createFixture( shape, 0 );
			
			shape.setAsBox( 0.2f, 1.0f, new Vector2( 0.5f, 1.0f ), 0 );
			body.createFixture( shape, 0);
			shape.dispose();
		}
		
		{
			BodyDef bd = new BodyDef( );
			bd.type = BodyType.DynamicBody;
			bd.position.set( 0, 20 );
			
			PolygonShape shape = new PolygonShape( );
			shape.setAsBox( 2, 0.1f );
			
			m_body = world.createBody( bd );
			m_body.createFixture( shape, 1 );
			
			m_angularVelocity = 33.468121f;
			m_body.setLinearVelocity( new Vector2( 0, -100 ) );
			m_body.setAngularVelocity( m_angularVelocity );
			shape.dispose();
		}		
	}
	
	private void launch( )
	{
		m_body.setTransform( new Vector2( 0, 20 ), 0 );
		m_angularVelocity = (float)Math.random() * 100 - 50;
		m_body.setLinearVelocity( new Vector2( 0, -100 ) );
		m_body.setAngularVelocity( m_angularVelocity );
	}

	public void render( Application app )
	{
		super.render( app );
		
		
		m_stepCount++;
		if( m_stepCount % 60 == 0 )
			launch( );
	}
}
