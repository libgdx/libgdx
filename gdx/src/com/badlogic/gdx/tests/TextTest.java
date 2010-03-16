package com.badlogic.gdx.tests;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.backends.desktop.JoglApplication;
import com.badlogic.gdx.graphics.FloatMesh;
import com.badlogic.gdx.graphics.Font;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.MeshRenderer;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Text;
import com.badlogic.gdx.graphics.Font.FontStyle;
import com.badlogic.gdx.graphics.Text.HorizontalAlign;

public class TextTest implements RenderListener
{
	OrthographicCamera cam;	
	MeshRenderer bounds;
	Font font;
	Text text;
	
	public static void main( String[] argv )
	{
		JoglApplication app = new JoglApplication( "Text Test", 480, 320, false );
		app.getGraphics().setRenderListener( new TextTest() );
	}	

	@Override
	public void surfaceCreated(Application app) 
	{
		if( text == null )
		{
			cam = new OrthographicCamera();		
			font = app.getGraphics().newFont( "Arial", 12, FontStyle.Plain, true );
			text = font.newText( );
			text.setText( "This is a test\nIt is a multline text!\nyes really!11!111one" );
			text.setHorizontalAlign( HorizontalAlign.Center );			
			text.rebuild();
			
			FloatMesh m = new FloatMesh( 4, 2, false, false, false, 0, 0, false, 0 );
			m.setVertices( new float[]{ 0, 0, text.getWidth(), 0, text.getWidth(), text.getHeight(), 0, text.getHeight() } );
			bounds = new MeshRenderer( app.getGraphics().getGL10(), m, true, true );
		}
	}
	
	@Override
	public void render(Application app) 
	{
		GL10 gl = app.getGraphics().getGL10();		
		gl.glViewport( 0, 0, app.getGraphics().getWidth(), app.getGraphics().getHeight() );
		gl.glClearColor( 0.7f, 0.7f, 0.7f, 1 );
		gl.glClear( GL10.GL_COLOR_BUFFER_BIT );
		cam.setMatrices( app.getGraphics() );
		
		gl.glColor4f( 1, 1, 1, 1 );
		bounds.render( GL10.GL_TRIANGLE_FAN, 0, 4);
		
		gl.glEnable( GL10.GL_TEXTURE_2D );
		gl.glEnable( GL10.GL_BLEND );
		gl.glColor4f( 1, 0, 0, 1 );
		gl.glBlendFunc( GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA );
		text.render();		
		gl.glDisable( GL10.GL_TEXTURE_2D );
		gl.glDisable( GL10.GL_BLEND );		
	}

	@Override
	public void dispose(Application app) 
	{	
		
	}
	
	@Override
	public void surfaceChanged(Application app, int width, int height) 
	{	
		
	}
}
