package com.badlogic.gdx.tests;

import java.util.Random;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

public class WaterRipples implements RenderListener
{
	static final short WIDTH = 50;
	static final short HEIGHT = 50;
	static final float DAMPING = 0.95f;
	static final float DISPLACEMENT = 5;
	static final float TICK = 0.033f;
	
	float accum;
	boolean initialized = false;
	PerspectiveCamera camera;
	Mesh mesh;
	Texture texture;
	Plane plane = new Plane( new Vector3(), new Vector3( 1, 0, 0 ), new Vector3( 0, 1, 0 ) );
	Vector3 point = new Vector3( );
	float[][] last;
	float[][] curr;
	float[] vertices;
	
	@Override
	public void surfaceCreated() 
	{
		
		if( !initialized )
		{
			camera = new PerspectiveCamera( );
			camera.getPosition().set( ( WIDTH ) / 2.0f, ( HEIGHT ) / 2.0f, WIDTH / 2.0f );
			camera.setViewport( Gdx.graphics.getWidth(), Gdx.graphics.getWidth() );
			camera.setFov( 90 );
			camera.setNear( 0.1f );
			camera.setFar( 1000 );
			last = new float[WIDTH+1][HEIGHT+1];
			curr = new float[WIDTH+1][HEIGHT+1];
			vertices = new float[(WIDTH+1)*(HEIGHT+1)*5];
			mesh = new Mesh( false, false, (WIDTH + 1) * (HEIGHT + 1), WIDTH * HEIGHT * 6, 
							 new VertexAttribute( VertexAttributes.Usage.Position, 3, "a_Position" ),
							 new VertexAttribute( VertexAttributes.Usage.TextureCoordinates, 2, "a_texCoords" ) );
			texture = Gdx.graphics.newTexture( Gdx.files.getFileHandle( "data/stones.jpg", FileType.Internal ), 
											   TextureFilter.Linear, TextureFilter.Linear,
											   TextureWrap.ClampToEdge, TextureWrap.ClampToEdge );
							
			createIndices( );
			updateVertices( );
			initialized = true;
		}
	}
	
	private void createIndices( )
	{
		short[] indices = new short[WIDTH*HEIGHT*6]; 
		int idx = 0;
		short vidx = 0;
		for( int y = 0; y < HEIGHT; y++ )
		{
			vidx = (short)(y * (WIDTH + 1));
			
			for( int x = 0; x < WIDTH; x++ )
			{
				indices[idx++] = vidx;
				indices[idx++] = (short)(vidx + 1);
				indices[idx++] = (short)(vidx + WIDTH + 1);
				
				indices[idx++] = (short)(vidx + 1);
				indices[idx++] = (short)(vidx + WIDTH + 2);
				indices[idx++] = (short)(vidx + WIDTH + 1);
				
				vidx++;
			}
		}
		
		mesh.setIndices( indices );
	}
	
	private void updateVertices( )
	{
		int idx = 0;
		for( int y = 0; y <= HEIGHT; y++ )
		{
			for( int x = 0; x <= WIDTH; x++ )
			{
				float xOffset = 0;
				float yOffset = 0;
				
				if( x > 0 && x < WIDTH && y > 0 && y < HEIGHT )
				{
					xOffset = (curr[x-1][y] - curr[x+1][y]) / WIDTH;
					yOffset = (curr[x][y-1] - curr[x][y+1]) / HEIGHT;
				}

				
				vertices[idx++] = x;
				vertices[idx++] = y;
				vertices[idx++] = 0;
				vertices[idx++] = x / (float)WIDTH + xOffset;
				vertices[idx++] = y / (float)HEIGHT + yOffset;
			}
		}
		mesh.setVertices( vertices );
	}
	
	private void updateWater( )
	{
		for( int y = 1; y < HEIGHT; y++ )
		{
			for( int x = 1; x < WIDTH; x++ )
			{
				curr[x][y] = (last[x-1][y]+
							  last[x+1][y]+
							  last[x][y+1]+
							  last[x][y-1]) / 4 - curr[x][y];
				curr[x][y] *= DAMPING;
			}
		}
	}
	
	@Override
	public void surfaceChanged(int width, int height) 
	{
		
	}

	long lastTick = System.nanoTime();
	Random rand = new Random();
	
	@Override
	public void render() 
	{
		GL10 gl = Gdx.graphics.getGL10();
		gl.glViewport( 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight() );
		gl.glClear( GL10.GL_COLOR_BUFFER_BIT );
		
		camera.update();
		gl.glMatrixMode( GL10.GL_PROJECTION );
		gl.glLoadMatrixf( camera.getCombinedMatrix().val, 0 );
		gl.glMatrixMode( GL10.GL_MODELVIEW );
		
		accum += Gdx.graphics.getDeltaTime();
		while( accum > TICK )
		{
			updateWater();
			float[][] tmp = curr;
			curr = last;
			last = tmp;
			accum -= TICK;
		}
		
		updateVertices();
		
		gl.glEnable( GL10.GL_TEXTURE_2D );
		texture.bind();
		mesh.render( GL10.GL_TRIANGLES );
		
		for( int i = 0; i < 4; i++ )
		{
			if( Gdx.input.isTouched(i) )
			{
				Ray ray = camera.getPickRay( Gdx.input.getX(i), (int)(Gdx.input.getY(i) / (float)Gdx.graphics.getHeight() * Gdx.graphics.getWidth()));
				Intersector.intersectRayPlane( ray, plane, point );
				int x = (int)point.x;
				int y = (int)point.y;
				if( x < 2 )
					x = 2;
				if( x > WIDTH - 1 )
					x = WIDTH - 2;
				if( y < 2 )
					y = 2;
				if( y > HEIGHT - 1 )
					y = HEIGHT - 2;
				curr[x][y] = DISPLACEMENT;
				curr[x-1][y] = DISPLACEMENT;
				curr[x+1][y] = DISPLACEMENT;
				curr[x][y-1] = DISPLACEMENT;
				curr[x][y+1] = DISPLACEMENT;
			}
		}
		
		if( System.nanoTime() - lastTick > 1000000000 )
		{
			Gdx.app.log( "Water", "fps: " + Gdx.graphics.getFramesPerSecond() );
			lastTick = System.nanoTime();
		}
	}

	@Override
	public void dispose() 
	{
		
	}

}
