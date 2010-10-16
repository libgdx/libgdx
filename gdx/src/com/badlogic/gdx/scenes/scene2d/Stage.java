package com.badlogic.gdx.scenes.scene2d;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
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
	public static Texture debugTexture = null;
	public static boolean enableDebugging = false;
	
	private final int width;
	private final int height;
	private final int centerX;
	private final int centerY;
	private final boolean stretch;
	
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
	public Stage( int width, int height, boolean stretch ) 
	{
		this.width = width;
		this.height = height;
		this.stretch = stretch;
		this.root = new Group( "root" );
		this.batch = new SpriteBatch( );
		this.projection = new Matrix4( );
		this.identity = new Matrix4( );
		
		// TODO implement stretch, adjust width or height
		
		centerX = width / 2;
		centerY = height / 2;
		
		projection.setToOrtho2D( 0, 0, this.width, this.height );
	}
	
	/**
	 * @return the width of the stage in dips
	 */
	public int width()
	{
		return width;
	}
	
	/**
	 * @return the height of the stage in dips
	 */
	public int height()
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
	public int right( )
	{
		return width - 1;
	}
	
	/**
	 * @return the y-coordinate of the top edge of the stage in dips
	 */
	public int top( )
	{
		return height - 1;
	}
	
	/**
	 * @return the y-coordinate of the bottom edge of the stage in dips
	 */
	public int bottom( )
	{
		return 0;
	}
	
	/**
	 * @return the center x-coordinate of the stage in dips
	 */
	public int centerX( )
	{
		return centerX;
	}
	
	/**
	 * @return the center y-coordinate of the stage in dips
	 */
	public int centerY( )
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
	public boolean touchDown(int x, int y, int pointer) 
	{
		float stageY = (Gdx.graphics.getHeight() - 1) - y;
		float stageX = (float)x / Gdx.graphics.getWidth() * width; 
		stageY = stageY / Gdx.graphics.getHeight() * height;
		
		Group.toChildCoordinateSystem( root, stageX, stageY, point );
		System.out.println( "root " + point.x + ", " + point.y );
		return root.touchDown(point.x, point.y, pointer);
	}

	public boolean touchUp(int x, int y, int pointer) 
	{
		float stageY = (Gdx.graphics.getHeight() - 1) - y;
		float stageX = (float)x / Gdx.graphics.getWidth() * width; 
		stageY = stageY / Gdx.graphics.getHeight() * height;
		
		return root.touchUp( stageX, stageY, pointer );
	}

	public boolean touchDragged(int x, int y, int pointer) 
	{
		float stageY = (Gdx.graphics.getHeight() - 1) - y;
		float stageX = (float)x / Gdx.graphics.getWidth() * width; 
		stageY = stageY / Gdx.graphics.getHeight() * height;
		
		return root.touchDragged( stageX, stageY, pointer );	
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
}
