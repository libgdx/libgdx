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

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;

/**
 * <p>A Stage is a container for StageObjects and handles
 * distributing touch events, animating StageObjects and
 * asking them to render themselves.</p> 
 * 
 * <p>A Stage object fills the whole screen. It has a width and
 * height given in device independent pixels. It will create
 * a projection matrix that maps this viewport to the given 
 * real screen resolution. If the stretched attribute is set
 * to true then the viewport is enforced no matter the difference
 * in aspect ratio between the stage object and the screen dimensions.
 * In case stretch is disabled then the viewport is extended in the
 * bigger screen dimensions.</p>
 * @author mzechner
 *
 */
public class Stage
{		
	private float width;
	private float height;
	private float centerX;
	private float centerY;
	private boolean stretch;
	
	private final Group root;
	
	private final SpriteBatch batch;
	private final Matrix4 projection;
	private final Matrix4 identity;
	
	/**
	 * <p>Constructs a new Stage object with the given
	 * dimensions. If the device resolution does not
	 * equal the Stage objects dimensions the stage
	 * object will setup a projection matrix to guarantee
	 * a fixed coordinate system. If stretch is disabled
	 * then the bigger dimension of the Stage will be increased
	 * to accomodate the actual device resolution.</p>
	 * 
	 * @param width the width of the viewport
	 * @param height the height of the viewport
	 * @param stretch whether to stretch the viewport to the real device resolution
	 */
	public Stage( float width, float height, boolean stretch ) 
	{
		this.width = width;
		this.height = height;
		this.stretch = stretch;
		this.root = new Group( "root" );
		this.batch = new SpriteBatch( );
		this.projection = new Matrix4( );
		this.identity = new Matrix4( );
		setViewport( width, height, stretch );
	}
	
	/**
	 * Sets the viewport dimensions in device independent pixels. If stretch
	 * is false and the viewport aspect ratio is not equal to the device
	 * ratio then the bigger dimension of the viewport will be extended (device
	 * independent pixels stay quardatic instead of getting stretched).
	 * 
	 * @param width thew width of the viewport in device independent pixels
	 * @param height the height of the viewport in device independent pixels
	 * @param strech whether to stretch the viewport or not
	 */
	public void setViewport( float width, float height, boolean strech )
	{
		// TODO implement stretch, adjust width or height
		if( !stretch )
		{
			if( width > height )
			{
				float toDeviceSpace = Gdx.graphics.getHeight() / height;
				float toViewportSpace = height / Gdx.graphics.getHeight();
				
				float deviceWidth = width * toDeviceSpace;
				this.width = width + (Gdx.graphics.getWidth() - deviceWidth ) * toViewportSpace;
				this.height = height;
			}
			else
			{
				float toDeviceSpace = Gdx.graphics.getWidth() / width;
				float toViewportSpace = width / Gdx.graphics.getWidth();
				
				float deviceHeight = height * toDeviceSpace;
				this.height = height + (Gdx.graphics.getHeight() - deviceHeight ) * toViewportSpace;
				this.width = width;
			}
		}
		else
		{
			this.width = width;
			this.height = height;
		}
		
		
		centerX = width / 2;
		centerY = height / 2;
		
		projection.setToOrtho2D( 0, 0, this.width, this.height );
	}
	
	/**
	 * @return the width of the stage in dips
	 */
	public float width()
	{
		return width;
	}
	
	/**
	 * @return the height of the stage in dips
	 */
	public float height()
	{
		return height;
	}
	
	/**
	 * @return the x-coordinate of the left edge of the stage in dips
	 */
	public int left( )
	{
		return 0;
	}
	
	/**
	 * @return the x-coordinate of the right edge of the stage in dips
	 */
	public float right( )
	{
		return width - 1;
	}
	
	/**
	 * @return the y-coordinate of the top edge of the stage in dips
	 */
	public float top( )
	{
		return height - 1;
	}
	
	/**
	 * @return the y-coordinate of the bottom edge of the stage in dips
	 */
	public float bottom( )
	{
		return 0;
	}
	
	/**
	 * @return the center x-coordinate of the stage in dips
	 */
	public float centerX( )
	{
		return centerX;
	}
	
	/**
	 * @return the center y-coordinate of the stage in dips
	 */
	public float centerY( )
	{
		return centerY;
	}
	
	/**
	 * @return whether the stage is stretched
	 */
	public boolean isStretched( )
	{
		return stretch;
	}
	
	public Actor findActor( String name )
	{
		return root.findActor( name );
	}
	
	public List<Actor> getActors( )
	{
		return root.getActors();
	}
	
	public List<Group> getGroups( )
	{
		return root.getGroups();
	}
	
	final Vector2 point = new Vector2( );
	final Vector2 coords = new Vector2( );
	public boolean touchDown(int x, int y, int pointer) 
	{
		toStageCoordinates(x, y, coords );		
		Group.toChildCoordinates( root, coords.x, coords.y, point );
		return root.touchDown(point.x, point.y, pointer);
	}

	public boolean touchUp(int x, int y, int pointer) 
	{
		toStageCoordinates(x, y, coords );		
		Group.toChildCoordinates( root, coords.x, coords.y, point );
		return root.touchUp( point.x, point.y, pointer );
	}

	public boolean touchDragged(int x, int y, int pointer) 
	{
		toStageCoordinates(x, y, coords );		
		Group.toChildCoordinates( root, coords.x, coords.y, point );
		return root.touchDragged( point.x, point.y, pointer );	
	}
	
	public void act( float delta )
	{
		root.act( delta );
	}
	
	public void render( )
	{
		batch.begin( projection, identity );
		root.render( batch );
		batch.end( );
	}
	
	public void dispose( )
	{
		batch.dispose();
	}

	public void addActor(Actor actor) 
	{
		root.addActor( actor );
	}
	
	public String graphToString( )
	{
		StringBuilder buffer = new StringBuilder( );
		graphToString( buffer, root, 0 );
		return buffer.toString();
	}
	
	private void graphToString( StringBuilder buffer, Actor actor, int level )
	{
		for( int i = 0; i < level; i++ )
			buffer.append( ' ' );
		
		buffer.append( actor );
		buffer.append( "\n" );
		
		if( actor instanceof Group )
		{
			Group group = (Group)actor;
			for( int i = 0; i < group.getActors().size(); i++ )
				graphToString( buffer, group.getActors().get(i), level + 1 );
		}
	}

	public Group getRoot() 
	{
		return root;
	}

	public SpriteBatch getSpriteBatch( )
	{
		return batch;
	}
	
	public Actor getLastTouchedChild( )
	{
		return root.lastTouchedChild;
	}
	
	public Actor hit(float x, float y) 
	{
		Group.toChildCoordinates( root, x, y, point );
		return root.hit( point.x, point.y );
	}
	
	public void toStageCoordinates( int x, int y, Vector2 out )
	{
		out.y = (Gdx.graphics.getHeight() - 1) - y;
		out.x = (float)x / Gdx.graphics.getWidth() * width; 
		out.y = out.y / Gdx.graphics.getHeight() * height;		
	}
}
