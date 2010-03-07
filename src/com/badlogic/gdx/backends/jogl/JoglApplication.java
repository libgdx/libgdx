package com.badlogic.gdx.backends.jogl;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.media.opengl.GL;
import javax.media.opengl.GLContext;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.AudioDevice;
import com.badlogic.gdx.Font;
import com.badlogic.gdx.GraphicListener;
import com.badlogic.gdx.InputListener;
import com.badlogic.gdx.Mesh;
import com.badlogic.gdx.Pixmap;
import com.badlogic.gdx.Sound;
import com.badlogic.gdx.Texture;
import com.badlogic.gdx.Pixmap.Format;

public class JoglApplication implements Application, GraphicListener
{
	static 
	{
		System.loadLibrary( "libgdx" );
	}
	
	JFrame frame;
	/** the graphic panel **/
	JoglPanel graphicPanel;
	/** input multiplexer **/
	JoglInputMultiplexer inputMultiplexer;
	
	/** Runnable **/
	TextInputListener textListener;
	String text;	
	
	ArrayList<CloseListener> closeListeners = new ArrayList<CloseListener>();	
	
	public JoglApplication( String title, int width, int height )
	{
		frame = new JFrame( title );
		graphicPanel = new JoglPanel( this );        
        graphicPanel.setPreferredSize( new Dimension( width, height ) );
        frame.setSize( width + frame.getInsets().left + frame.getInsets().right, frame.getInsets().top + frame.getInsets().bottom + height );
        frame.add(graphicPanel, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible( true );                
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {                 	    
            	for( CloseListener listener: closeListeners )
            		listener.close();
            	graphicPanel.dispose();            	            	            	               
            }
        });    
        inputMultiplexer = new JoglInputMultiplexer(graphicPanel.getCanvas());
        graphicPanel.setInputMultiplexer( inputMultiplexer );
        graphicPanel.addGraphicListener( this );
	}
	
	@Override
	public void addGraphicListener(GraphicListener listener) 
	{
		graphicPanel.addGraphicListener( listener );
	}

	@Override
	public void clear(boolean color, boolean depth, boolean stencil) 
	{
		GL gl = GLContext.getCurrent().getGL();
		int flags = (color?GL.GL_COLOR_BUFFER_BIT:0) |
					(depth?GL.GL_DEPTH_BUFFER_BIT:0) |
					(stencil?GL.GL_STENCIL_BUFFER_BIT:0);
		if( depth )
			gl.glDepthMask( true );
		gl.glClear( flags );
	}

	@Override
	public void clearColor(float r, float g, float b, float a) 
	{
		GL gl = GLContext.getCurrent().getGL();
		gl.glClearColor( r, g, b, a );
	}

	@Override
	public void color(float r, float g, float b, float a) 
	{
		GL gl = GLContext.getCurrent().getGL();
		gl.glColor4f( r, g, b, a );
	}

	@Override
	public InputStream getResourceInputStream(String file) throws IOException 
	{		
		return new FileInputStream( file );
	}

	@Override
	public String[] listResourceFiles(String directory) 
	{
		return new File(directory).list();
	}

	@Override
	public void loadIdentity() 
	{
		GL gl = GLContext.getCurrent().getGL();
		gl.glLoadIdentity();
	}

	@Override
	public void loadMatrix(float[] matrix) 
	{
		GL gl = GLContext.getCurrent().getGL();
		gl.glLoadMatrixf( matrix, 0);		
	}

	@Override
	public void multMatrix(float[] matrix) 
	{
		GL gl = GLContext.getCurrent().getGL();
		gl.glMultMatrixf( matrix, 0);		
	}

	@Override
	public Mesh newMesh(int maxVertices, boolean hasColors, boolean hasNormals, boolean hasUV, boolean hasIndices, int maxIndices, boolean isStatic) 
	{	
		return new JoglMesh( GLContext.getCurrent().getGL(), maxVertices, hasColors, hasNormals, hasUV, hasIndices, maxIndices, isStatic );
	}

	@Override
	public Texture newTexture(InputStream in, TextureFilter minFilter, TextureFilter maxFilter, TextureWrap uWrap, TextureWrap vWrap) 
	{
		return new JoglTexture( in, minFilter, maxFilter, uWrap, vWrap);
	}
	

	@Override
	public Texture newTexture(int width, int height, TextureFilter minFilter, TextureFilter maxFilter, TextureWrap uWrap, TextureWrap vWrap ) 
	{	
		return new JoglTexture( width, height, minFilter, maxFilter, uWrap, vWrap);
	}

	@Override
	public void normal(float x, float y, float z) 
	{
		GL gl = GLContext.getCurrent().getGL();
		gl.glNormal3f(x, y, z);
	}

	@Override
	public void removeGraphicListener(GraphicListener listener) 
	{
		graphicPanel.removeGraphicListener( listener );
		
	}

	@Override
	public void rotate(float angle, float x, float y, float z) 
	{
		GL gl = GLContext.getCurrent().getGL();
		gl.glRotatef( angle, x, y, z );		
	}

	@Override
	public void scale(float x, float y, float z) 
	{
		GL gl = GLContext.getCurrent().getGL();
		gl.glScalef( x, y, z );
	}

	@Override
	public void setMatrixMode(MatrixMode mode) 
	{
		GL gl = GLContext.getCurrent().getGL();
		if( mode == MatrixMode.ModelView )
			gl.glMatrixMode( GL.GL_MODELVIEW );
		if( mode == MatrixMode.Projection )
			gl.glMatrixMode( GL.GL_PROJECTION );
		if( mode == MatrixMode.Texture )
			gl.glMatrixMode( GL.GL_TEXTURE );
	}

	@Override
	public void translate(float x, float y, float z) 
	{
		GL gl = GLContext.getCurrent().getGL();
		gl.glTranslatef( x, y, z );
	}

	@Override
	public void addInputListener(InputListener listener) 
	{
		inputMultiplexer.addListener(listener);
	}

	@Override
	public void removeInputListener(InputListener listener) 
	{	
		inputMultiplexer.removeListener(listener);
	}

	@Override
	public int getViewportHeight() 
	{		
		return graphicPanel.getHeight();
	}

	@Override
	public int getViewportWidth() 
	{	
		return graphicPanel.getWidth();
	}

	@Override
	public boolean isAccelerometerAvailable() {
		return false;
	}

	@Override
	public int getX() {		
		return graphicPanel.getMouseX();
	}

	@Override
	public int getY() {
		return graphicPanel.getMouseY( );
	}

	@Override
	public boolean isPressed() {
		return graphicPanel.isButtonDown(MouseEvent.BUTTON1) ||
				graphicPanel.isButtonDown(MouseEvent.BUTTON2) ||
				graphicPanel.isButtonDown(MouseEvent.BUTTON3);
	}

	@Override
	public float getDeltaTime() {
		// TODO Auto-generated method stub
		return graphicPanel.getDeltaTime();
	}

	@Override
	public void disable(RenderState state) {
		GL gl = GLContext.getCurrent().getGL();
		if( state == RenderState.Blending )
			gl.glDisable( GL.GL_BLEND );
		if( state == RenderState.DepthTest )
		{
			gl.glDisable( GL.GL_DEPTH_TEST );
			gl.glDepthMask( false );
		}
		if( state == RenderState.Lighting )
			gl.glDisable( GL.GL_LIGHTING );
		if( state == RenderState.Texturing )
			gl.glDisable( GL.GL_TEXTURE_2D );
		if( state == RenderState.Culling )
			gl.glDisable( GL.GL_CULL_FACE );
		if( state == RenderState.AlphaTest )
			gl.glDisable( GL.GL_ALPHA_TEST );
	}

	@Override
	public void enable(RenderState state) {
		GL gl = GLContext.getCurrent().getGL();
		if( state == RenderState.Blending )
		{
			gl.glEnable( GL.GL_BLEND );
		}
		if( state == RenderState.DepthTest )
		{
			gl.glEnable( GL.GL_DEPTH_TEST );
			gl.glDepthMask( true );
		}
		if( state == RenderState.Lighting )
		{
			gl.glEnable( GL.GL_LIGHTING );
			gl.glEnable( GL.GL_COLOR_MATERIAL );
		}
		if( state == RenderState.AlphaTest )
		{
			gl.glEnable( GL.GL_ALPHA_TEST );
			gl.glAlphaFunc( GL.GL_GREATER, 0.9f );
		}
		if( state == RenderState.Texturing )
			gl.glEnable( GL.GL_TEXTURE_2D );
		if( state == RenderState.Culling )
			gl.glEnable( GL.GL_CULL_FACE );
	}

	@Override
	public void log(String tag, String message) {
		System.out.println( tag + ": " + message );
	}

	float[] position = new float[4];
	float[] color = new float[4];
	@Override	
	public void setAmbientLight(float r, float g, float b, float a) {
		GL gl = GLContext.getCurrent().getGL();		
		color[0] = r;
		color[1] = g;
		color[2] = b;
		color[3] = a;
		gl.glLightModelfv( GL.GL_LIGHT_MODEL_AMBIENT, color, 0 );
	}

	@Override
	public void disableLight(int light) 
	{	
		GL gl = GLContext.getCurrent().getGL();	
		gl.glDisable( GL.GL_LIGHT0 + light );
	}

	@Override
	public void enableLight(int light) {
		GL gl = GLContext.getCurrent().getGL();	
		gl.glEnable( GL.GL_LIGHT0 + light );			
	}

	@Override
	public void setDirectionalLight(int light, float x, float y, float z,
			float r, float g, float b, float a) {
		GL gl = GLContext.getCurrent().getGL();	
		color[0] = 0;
		color[1] = 0;
		color[2] = 0;
		color[3] = 0;
		position[0] = -x;
		position[1] = -y;
		position[2] = -z;
		position[3] = 0;
		gl.glLightfv( GL.GL_LIGHT0 + light, GL.GL_AMBIENT, color, 0 );
		color[0] = r;
		color[1] = g;
		color[2] = b;
		color[3] = a;
		gl.glLightfv( GL.GL_LIGHT0 + light, GL.GL_DIFFUSE, color, 0 );
		gl.glLightfv( GL.GL_LIGHT0 + light, GL.GL_POSITION, position, 0 );
		
	}

	@Override
	public void flush() {
		GLContext.getCurrent().getGL().glFinish();
	}
	
	
	public boolean error( )
	{
		return false; // GLContext.getCurrent().getGL().glGetError() != GL.GL_NO_ERROR;
	}

	@Override
	public void popMatrix() {
		GLContext.getCurrent().getGL().glPopMatrix();
	}

	@Override
	public void pushMatrix() {
		GLContext.getCurrent().getGL().glPushMatrix();
	}

	@Override
	public Font newFont(String fontName, int size, FontStyle style) {
		return new JoglFont( this, fontName, size, style );
	}

	@Override
	public Font newFontFromFile(String file, int size, FontStyle style) {
		try {
			return new JoglFont( this, getResourceInputStream( file ), size, style );
		} catch (IOException e) {
			return new JoglFont( this, "Arial", size, style );
		}
	}

	@Override
	public void blendFunc(BlendFunc arg1, BlendFunc arg2) {
		GLContext.getCurrent().getGL().glBlendFunc( getBlendFuncValue( arg1 ), getBlendFuncValue( arg2 ) );
	}
	
	private int getBlendFuncValue( BlendFunc func )
	{
		if( func == BlendFunc.DestAlpha )
			return GL.GL_DST_ALPHA;
		if( func == BlendFunc.DestColor )
			return GL.GL_DST_COLOR;
		if( func == BlendFunc.One )
			return GL.GL_ONE;
		if( func == BlendFunc.OneMinusDestAlpha )
			return GL.GL_ONE_MINUS_DST_ALPHA;
		if( func == BlendFunc.OneMinusDestColor )
			return GL.GL_ONE_MINUS_DST_COLOR;
		if( func == BlendFunc.OneMinusSourceAlpha )
			return GL.GL_ONE_MINUS_SRC_ALPHA;
		if( func == BlendFunc.OneMinusSourceColor )
			return GL.GL_ONE_MINUS_SRC_COLOR;
		if( func == BlendFunc.SourceAlpha )
			return GL.GL_SRC_ALPHA;
		if( func == BlendFunc.SourceColor )
			return GL.GL_SRC_COLOR;
		if( func == BlendFunc.Zero )
			return GL.GL_ZERO;
		return GL.GL_ONE;
		
	}

	@Override
	public Texture newTexture(String file, TextureFilter minFilter,
			TextureFilter magFilter, TextureWrap uWrap, TextureWrap vWrap) {
		try
		{
			InputStream in = getResourceInputStream( file );
			Texture texture = newTexture(in, minFilter, magFilter, uWrap, vWrap);
			in.close();
			return texture;
		}
		catch( Exception ex )
		{
			throw new RuntimeException( "couldn't load texture '" + file + "'" );
		}		
	}

	@Override
	public void getTextInput( final TextInputListener listener, final String title, final String text )
	{
		SwingUtilities.invokeLater( new Runnable() {			
			@Override
			public void run() {							
				JoglApplication.this.text = JOptionPane.showInputDialog(frame, title, text );
				if( JoglApplication.this.text != null )
					textListener = listener;
			}
		});
	}

	@Override
	public void dispose(Application application) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void render(Application application) {
		if( textListener != null )
		{
			textListener.input( text );
			textListener = null;
		}
	}

	@Override
	public void setup(Application application) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Sound newSound(String file) {
		try
		{
			return new JoglSound( getResourceInputStream( file ));
		}
		catch( Exception ex )
		{
			ex.printStackTrace();
			throw new RuntimeException ( "couldn't load sound '" + file + "'" );
		}
	}

	@Override
	public OutputStream getOutputStream(String file) throws IOException {
		return new FileOutputStream( System.getProperty("user.home") + "/" + file );
	}

	@Override
	public InputStream getInputStream(String file) throws IOException {
		return new FileInputStream( System.getProperty("user.home") + "/" + file );
	}

	@Override
	public void mkdir(String directory) {
		new File( System.getProperty("user.home") + "/" + directory ).mkdirs();
	}

	@Override
	public void addCloseListener(CloseListener listener) {
		closeListeners.add(listener);
	}

	@Override
	public void removeCloseListener(CloseListener listener) {	
		closeListeners.remove(listener);
	}

	@Override
	public void depthFunc(DepthFunc func) {
		GL gl = GLContext.getCurrent().getGL();
		if( func == DepthFunc.Always )
			gl.glDepthFunc( GL.GL_ALWAYS );
		if( func == DepthFunc.Equal )
			gl.glDepthFunc( GL.GL_EQUAL );
		if( func == DepthFunc.Greater )
			gl.glDepthFunc( GL.GL_GREATER );
		if( func == DepthFunc.Less )
			gl.glDepthFunc( GL.GL_LESS );
		if( func == DepthFunc.LessEqual )
			gl.glDepthFunc( GL.GL_LEQUAL );
		if( func == DepthFunc.Never )
			gl.glDepthFunc( GL.GL_NEVER );
		if( func == DepthFunc.GreaterEqual )
			gl.glDepthFunc( GL.GL_GEQUAL );
		if( func == DepthFunc.NotEqual )
			gl.glDepthFunc( GL.GL_NOTEQUAL );
	}

	@Override
	public boolean isKeyPressed(Keys key) 
	{ 
		int k = KeyEvent.VK_0;
		if( key == Keys.Left)
			k = KeyEvent.VK_LEFT;
		if( key == Keys.Right )
			k = KeyEvent.VK_RIGHT;
		if( key == Keys.Up )
			k = KeyEvent.VK_UP;
		if( key == Keys.Down )
			k = KeyEvent.VK_DOWN;
		if( key == Keys.Control )
			k = KeyEvent.VK_CONTROL;
		if( key == Keys.Shift )
			k = KeyEvent.VK_SHIFT;
		if( key == Keys.Space )
			k = KeyEvent.VK_SPACE;
		if( key == Keys.Any )
			graphicPanel.isAnyKeyDown( );
		
		return graphicPanel.isKeyDown( k ); 
	}

	@Override
	public float getAccelerometerX() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getAccelerometerY() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getAccelerometerZ() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void enableMultiTouch(boolean enable) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getX(int pointer) {
		if( pointer == 0 )
			return graphicPanel.getMouseX();
		else
			return 0;
	}

	@Override
	public int getY(int pointer) {
		if( pointer == 0 )
			return graphicPanel.getMouseY();
		else
			return 0;
	}

	@Override
	public boolean isPressed(int pointer) {
		if( pointer == 0 )
			return graphicPanel.isButtonDown(MouseEvent.BUTTON1) ||
			graphicPanel.isButtonDown(MouseEvent.BUTTON2) ||
			graphicPanel.isButtonDown(MouseEvent.BUTTON3);
		else
			return false;
	}

	@Override
	public boolean isAndroid() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Pixmap newPixmap(int width, int height, Format format) 
	{		
		return new JoglPixmap(width, height, format);
	}

	@Override
	public Texture newTexture(Pixmap pixmap, TextureFilter minFilter, TextureFilter maxFilter, TextureWrap uWrap, TextureWrap vwrap) 
	{	
		return new JoglTexture( (BufferedImage)pixmap.getNativePixmap(), minFilter, maxFilter, uWrap, vwrap );		
	}

	@Override
	public Pixmap newPixmap(String file, Pixmap.Format format) 
	{
		try {
			InputStream in = getResourceInputStream( file );
			Pixmap pixmap = newPixmap( in, format );
			in.close();
			return pixmap;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}		
	}

	@Override
	public Pixmap newPixmap(InputStream in, Pixmap.Format format) 
	{
		try {
			BufferedImage image = ImageIO.read( in );
			return new JoglPixmap( image );			
			
		} catch (IOException e) {
			return null;
		}		
	}

	@Override
	public void setPointSize(int width) {
		GL gl = GLContext.getCurrent().getGL();
		gl.glPointSize( width );
	}

	@Override
	public void setCullMode(CullMode order) 
	{	
		GL gl = GLContext.getCurrent().getGL();
		if( order == CullMode.Clockwise )
			gl.glCullFace( GL.GL_CW );
		else
			gl.glCullFace( GL.GL_CCW );
	}


	@Override
	public Pixmap newPixmap(Object nativeImage) {
		return new JoglPixmap( (BufferedImage)nativeImage );		
	}

	@Override
	public AudioDevice getAudioDevice() 
	{	
		return new JoglAudioDevice();
	}
}

