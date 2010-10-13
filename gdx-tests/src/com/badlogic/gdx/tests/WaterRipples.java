package com.badlogic.gdx.tests;

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
	static final short WIDTH = 20;
	static final short HEIGHT = 20;
	static final float DAMPING = 0.8f;
	
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
		texture = Gdx.graphics.newTexture( Gdx.files.getFileHandle( "data/badlogic.jpg", FileType.Internal ), 
										   TextureFilter.Linear, TextureFilter.Linear,
										   TextureWrap.ClampToEdge, TextureWrap.ClampToEdge );
						
		createIndices( );
		updateVertices( );
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
				vertices[idx++] = x;
				vertices[idx++] = y;
				vertices[idx++] = curr[x][y];
				vertices[idx++] = x / (float)WIDTH;
				vertices[idx++] = y / (float)HEIGHT;
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
		
		updateWater();
		updateVertices();
		float[][] tmp = curr;
		curr = last;
		last = tmp;
		
		gl.glEnable( GL10.GL_TEXTURE_2D );
//		gl.glPolygonMode( GL10.GL_FRONT_AND_BACK, GL10.GL_LINE );
		texture.bind();
		mesh.render( GL10.GL_TRIANGLES );
		
		if( Gdx.input.isTouched() )
		{
			Ray ray = camera.getPickRay( Gdx.input.getX(), (int)(Gdx.input.getY() / (float)Gdx.graphics.getHeight() * Gdx.graphics.getWidth()));
			Intersector.intersectRayPlane( ray, plane, point );
			curr[(int)point.x][(int)point.y] = 2;
		}
	}

	@Override
	public void dispose() 
	{
		
	}

}
