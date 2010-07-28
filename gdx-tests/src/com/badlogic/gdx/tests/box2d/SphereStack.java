package com.badlogic.gdx.tests.box2d;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class SphereStack extends Box2DTest
{
	int e_count = 10;
	
	
	@Override
	protected void createWorld(World world) 
	{
		{
			BodyDef bd = new BodyDef();
			Body ground = world.createBody( bd );
			
			PolygonShape shape = new PolygonShape();
			shape.setAsEdge( new Vector2( -40, 0 ), new Vector2( 40, 0 ) );
			ground.createFixture( shape, 0 );
			shape.dispose();
		}
		
		{
			CircleShape shape = new CircleShape();
			shape.setRadius( 1.0f );
			
			for( int i = 0; i < e_count; i++ )
			{
				BodyDef bd = new BodyDef( );
				bd.type = BodyType.DynamicBody;
				bd.position.set( 0, 4.0f + 3.0f * i );
				Body body = world.createBody( bd );
				body.createFixture( shape, 1.0f );
			}
			
			shape.dispose();
		}
		
	}

}
