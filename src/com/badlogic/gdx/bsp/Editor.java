package com.badlogic.gdx.bsp;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.InputListener;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.Input.TextInputListener;
import com.badlogic.gdx.backends.desktop.JoglApplication;
import com.badlogic.gdx.graphics.FloatMesh;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.ImmediateModeRenderer;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.ModelLoader;
import com.badlogic.gdx.graphics.ModelWriter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector;

/**
 * A very simple editor for creating 2D concave levels. This is only
 * used to generate test cases. Press c to clear the current level.
 * Press s to save the level. Click with the mouse and draw a concave
 * polygon in clockwise order.
 * 
 * @author badlogicgames@gmail.com
 *
 */
public class Editor implements RenderListener, InputListener 
{
	OrthographicCamera cam;
	List<Polygon> polygons = new ArrayList<Polygon>( );
	Polygon currentPolygon;
	ImmediateModeRenderer g;
	boolean drawing = false;
	Application app;
		
	public static void main( String[] argv )
	{ 
		JoglApplication app = new JoglApplication( "Editor", 480, 320, false );
		app.getGraphics().setRenderListener( new Editor() );
	}

	@Override
	public void dispose(Application app) 
	{	
		
	}

	@Override
	public void render(Application app) 
	{	
		GL10 gl = app.getGraphics().getGL10();
		gl.glClear( GL10.GL_COLOR_BUFFER_BIT );
		gl.glColor4f( 1, 1, 1, 1 );
		renderPolygons( gl );		
	}

	private void renderPolygons( GL10 gl )
	{
		gl.glColor4f( 0, 1, 0, 1 );
		g.begin( GL10.GL_LINES );
		for( Polygon polygon: polygons )
			renderPolygon( polygon, true );
		g.end( );
				
		gl.glColor4f( 1, 1, 1, 1 );
		g.begin( GL10.GL_LINES );
		if( currentPolygon != null )
		{
			renderPolygon( currentPolygon, false );
			float touchX = 10 * (app.getInput().getX() / 480.0f - 0.5f);
			float touchY = 10 * (0.5f - app.getInput().getY() / 320.0f);
			g.vertex( currentPolygon.points.get(currentPolygon.points.size()-1).x, currentPolygon.points.get(currentPolygon.points.size()-1).y, 0);
			g.vertex( touchX, touchY, 0 );
		}
		g.end();
				
	}
	
	Vector a = new Vector();
	Vector b = new Vector();
	Vector n = new Vector();
	private void renderPolygon( Polygon p, boolean close )
	{
		for( int i = 0; i < (close?p.points.size():p.points.size()-1); i++ )
		{
			Vector p1 = p.points.get(i);
			Vector p2 = p.points.get(i<p.points.size()-1?i+1:0);
			g.vertex( p1.x, p1.y, p1.z );
			g.vertex( p2.x, p2.y, p2.z );
			
			b.set(p2).sub(p1);
			n.set( p2.x, p2.y, 1 ).sub(p2);
			n.crs(b).nor().mul(0.1f);
			
			Plane plane = new Plane( p1, p2, new Vector( p2.x, p2.y, 1 ) );
			
			g.vertex( p1.x + (p2.x - p1.x) / 2, p1.y + (p2.y - p1.y) / 2, 0 );
			g.vertex( p1.x + (p2.x - p1.x) / 2 + n.x, p1.y + (p2.y - p1.y) / 2 + n.y, 0 );
		}	
	}
	
	@Override
	public void surfaceChanged(Application app, int width, int height) 
	{	
		
	}

	@Override
	public void surfaceCreated(Application app) 
	{			
		this.app = app;
		app.getInput().addInputListener( this );
		
		cam = new OrthographicCamera( );
		cam.setViewport( 10, 10 );
		cam.update();
		
		GL10 gl = app.getGraphics().getGL10();
		gl.glMatrixMode( GL10.GL_PROJECTION );
		gl.glLoadMatrixf( cam.getCombinedMatrix().val, 0 );
		gl.glMatrixMode( GL10.GL_MODELVIEW );
		gl.glLoadIdentity();			
		
		g = new ImmediateModeRenderer( gl );			
	}

	@Override
	public boolean keyDown(int keycode) 
	{	
		return false;
	}

	@Override
	public boolean keyTyped(char character) 
	{	
		if( character == 'c' )
		{
			polygons.clear();
			currentPolygon = null;
			drawing = false;
		}
		
		if( character == 's' )
		{
			currentPolygon = null;
			drawing = false;
			
			app.getInput().getTextInput( new TextInputListener() 
			{			
				@Override
				public void input(String text) 
				{				
					saveMesh( text );
				}
			}, "Save", "" );
		}
		return false;
	}
	
	private void saveMesh( String filename )
	{
		int numVertices = 0;
		for( Polygon polygon: polygons )
			numVertices += (polygon.points.size()) * 6;
		
		FloatMesh mesh = new FloatMesh(numVertices, 3, false, false, false, 0, 0, false, 0 );
		FloatBuffer vertices = FloatBuffer.allocate( numVertices * 3 );
		for( Polygon polygon: polygons )
		{
			for( int i = 0; i < polygon.points.size(); i++ )
			{
				Vector p1 = polygon.points.get(i);
				Vector p2 = polygon.points.get(i < polygon.points.size()-1?i+1:0);
				Vector p3 = new Vector( p2 );
				Vector p4 = new Vector( p1 );				
				p3.z = p4.z = 1;				
				
				vertices.put( p1.x ); vertices.put( p1.z ); vertices.put( p1.y );
				vertices.put( p2.x ); vertices.put( p2.z ); vertices.put( p2.y );
				vertices.put( p3.x ); vertices.put( p3.z ); vertices.put( p3.y );
				
				vertices.put( p3.x ); vertices.put( p3.z ); vertices.put( p3.y );
				vertices.put( p4.x ); vertices.put( p4.z ); vertices.put( p4.y );
				vertices.put( p1.x ); vertices.put( p1.z ); vertices.put( p1.y );
			}
		}
		mesh.setVertices( vertices.array() );
		try
		{
			FileOutputStream out = new FileOutputStream( filename );
			ModelWriter.writeObj(out, mesh);
			out.flush();
			out.close();
			
			mesh = (FloatMesh)ModelLoader.loadObj( new FileInputStream( filename ), true );
		}
		catch( Exception ex )
		{
			ex.printStackTrace();
		}
	}

	@Override
	public boolean keyUp(int keycode) 
	{	
		return false;
	}

	@Override
	public boolean touchDown(int x, int y, int pointer) 
	{	
		return false;
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer) 
	{	
		return false;
	}

	@Override
	public boolean touchUp(int x, int y, int pointer) 
	{	
		float touchX = 10 * (x / 480.0f - 0.5f);
		float touchY = 10 * (0.5f - y / 320.0f);
		
		if( drawing == false )
		{			
			currentPolygon = new Polygon();
			currentPolygon.add( touchX, touchY, 0 );
			drawing = true;
		}
		else
		{				
			if( currentPolygon.points.get(0).dst( touchX, touchY, 0 ) < 0.1f )
			{				
				polygons.add( currentPolygon );
				currentPolygon = null;
				drawing = false;
			}				
			else
			{
				currentPolygon.add( touchX, touchY, 0 );
			}
		}
		
		return false;
	}
}

