/**
 *  This file is part of Libgdx by Mario Zechner (badlogicgames@gmail.com)
 *
 *  Libgdx is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Libgdx is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.badlogic.gdx.backends.jogl;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLEventListener;
import javax.swing.JPanel;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.graphics.RenderListener;
import com.badlogic.gdx.math.WindowedMean;
import com.sun.opengl.impl.NativeLibLoader;
import com.sun.opengl.util.Animator;

/**
 * An OpenGL based graphics panel. Shares its context with all
 * other GraphicPanels in the application. Has a continous render
 * thread constantly invoking the paint method. Note that you have
 * to dispose the panel before it is being removed from another
 * gui component such as a JFrame.
 * 
 * @author badlogicgames@gmail.com
 *
 */
final class JoglPanel extends JPanel implements GLEventListener, MouseMotionListener, MouseListener, KeyListener
{	
	private static final long serialVersionUID = -3638194405409146221L;

	/** global flag used to check wheter the libraries are loaded already **/
	private static boolean loaded = false;
	
	/** the context to be used by all GraphicPanels **/
	private static GLContext context = null;	
	
	/** the canvas object **/
	private final GLCanvas canvas;
	
	/** animator for continous updates **/
	private final Animator animator;
	
	/** frame counter **/
	private int frames = 0;
	
	/** frame count start time **/
	private long start_time = 0;
	
	/** average frames per second **/
	private float framesPerSecond = 0;
	
	/** vsynch flag **/
	private boolean vSynch = true;
	
	/** last mouse position **/
	private final Point mousePosition = new Point( );
	
	/** last mouse button states **/
	private final HashSet<Integer> buttons = new HashSet<Integer>( );
	
	/** last key states **/
	private final HashSet<Integer> keys = new HashSet<Integer>( );
	
	/** listeners **/
	private final ArrayList<RenderListener> listeners = new ArrayList<RenderListener>( );
	
	/** listeners **/
	private final ArrayList<RenderListener> setupListeners = new ArrayList<RenderListener>( );	
	
	/** start time of last frame **/
	private long frameStart = System.nanoTime();
	
	/** delta time between current and last frame **/
	private float deltaTime = 0;
	private WindowedMean mean = new WindowedMean(10);
	
	/** the application **/
	private Application application = null;

	private JoglInputMultiplexer multiplexer;
	
	/**
	 * loads the necessary libraries depending on the operating system
	 */
	private static void loadLibraries( ) 
	{
		if( loaded ) 
			return;
		
		NativeLibLoader.disableLoading();		
		com.sun.gluegen.runtime.NativeLibLoader.disableLoading();
        // By wkien: On some systems (read: mine) jogl_awt would not find its dependency jawt if not loaded before
        System.loadLibrary("jawt");
        loadLibrary( "gluegen-rt" );
		loadLibrary( "jogl_awt" );	
		loadLibrary( "jogl" );		
		
		loaded = true;
	}
	
	/**  
	 * helper method to load a specific library in an operation system dependant manner
	 * @param resource the name of the resource
	 */
	private static void loadLibrary( String resource )
	{
		String package_path = "/javax/media/";
		String library = "";
		
		String os = System.getProperty( "os.name" );
		String arch = System.getProperty( "os.arch" );
		
		if( os.contains( "Windows" ) )
		{
			if( !arch.equals( "amd64" ) )
				library = resource + "-win32.dll";
			else
			{				
				library = resource + "-win64.dll";
			}
		}
		
		if( os.contains( "Linux" ) )
		{
			if( !arch.equals( "amd64" ) )
				library = "lib" + resource + "-linux32.so";
			else					
				library = "lib" + resource + "-linux64.so";			
		}
		
		if( os.contains( "Mac" ) )
		{
			library = "lib" + resource + ".jnilib";
		}
		
		String so = System.getProperty( "java.io.tmpdir" ) + "/" + System.nanoTime() + library;
		InputStream in = JoglPanel.class.getResourceAsStream( package_path + library );
		if( in == null )
			throw new RuntimeException( "couldn't find " + library + " in jar file." );					
		
		try 
		{
			BufferedOutputStream out = new BufferedOutputStream( new FileOutputStream( so ) );
			byte[] bytes = new byte[1024*4];
			while( true )
			{
				int read_bytes = in.read(bytes);
				if( read_bytes == -1 )
					break;
				
				out.write( bytes, 0, read_bytes );
			}
			out.close();
			in.close();
			System.load( so );
		} 
		catch (FileNotFoundException e) 
		{		
			throw new RuntimeException( "couldn't write " + library + " to temporary file " + so );
		} 
		catch (IOException e) 
		{
			throw new RuntimeException( "couldn't write " + library + " to temporary file " + so );
		}				
	}
	
	/**
	 * Constructor loading the libraries if needed and creating/reusing a
	 * {@link GLContext}. A standard back/depth/stencil buffer combination is
	 * created (32-bit, 16-bit, 8-bit). Additionally an animator thread is
	 * created that is responsible for redrawing the panel as fast as possible in
	 * case the continous flag is set to true.
	 * Keyboard and Mouse events are processed by the panel so that a client
	 * can poll for specific states.
	 */
	JoglPanel( Application application )
	{
		super();
		loadLibraries();		      
		this.application = application;				
		
		GLCapabilities caps = new GLCapabilities();
		caps.setRedBits(8);
    	caps.setGreenBits(8);
    	caps.setBlueBits(8);
    	caps.setAlphaBits(8);
    	caps.setDepthBits(16);
    	caps.setStencilBits(8);    	
    	caps.setNumSamples(4);
    	caps.setSampleBuffers(true);
    	caps.setDoubleBuffered(true);  
    	
    	if( context == null )
    	{
    		canvas = new GLCanvas( caps );
    		context = canvas.getContext();
    	}
    	else
    		canvas = new GLCanvas( caps, null, context, null );
    	
        canvas.addGLEventListener(this);   
        this.setLayout( new BorderLayout() );
        add(canvas,BorderLayout.CENTER);                
               
	    animator = new Animator( canvas );
	    animator.start();        
        
        start_time = System.nanoTime();   
        
        canvas.addMouseListener( this );
        canvas.addMouseMotionListener( this );
        canvas.addKeyListener( this );
	}
	
	/**
	 * stops the animator thread and releases any resources
	 */
	public void dispose( )
	{
		canvas.getContext().makeCurrent();
		for( RenderListener listener: listeners )
			listener.dispose( application );
		remove(canvas);
		if( animator != null )
			animator.stop();    	
	}
	
	
	/**
	 * @return the delta time between the current and last frame
	 */
	public float getDeltaTime( )
	{
		return mean.hasEnoughData()?mean.getMean():deltaTime;
//		return deltaTime;
	}
	
	/**
	 * @return the underlying GLCanvas
	 */
	public GLCanvas getCanvas( )
	{
		return canvas;
	}
	
	/**
	 * sets wheter vsynch is enabled or disabled. true by default.
	 * 
	 * @param v_synch
	 */
	public void setVerticalSynch( boolean v_synch )
	{
		this.vSynch = v_synch;		
		if( v_synch )
			GLContext.getCurrent().getGL().setSwapInterval(1);
		else
			GLContext.getCurrent().getGL().setSwapInterval(0);
	}
	
	/**
	 * @return wheter vsynch is on or not
	 */
	public boolean getVerticalSynch( )
	{
		return vSynch;
	}
		
	/**
	 * @return frames rendered per second
	 */
	public float getFramesPerSecond( )
	{
		return framesPerSecond;	
	}
	
	public void mouseDragged(MouseEvent arg0) 
	{
		mousePosition.x = arg0.getX();
		mousePosition.y = arg0.getY();
		
	}

	public void mouseMoved(MouseEvent arg0) 
	{	
		mousePosition.x = arg0.getX();
		mousePosition.y = arg0.getY();	
	}

	public int getMouseX( )
	{
		return mousePosition.x;
	}
	
	public int getMouseY( )
	{
		return mousePosition.y;
	}
	
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void mousePressed(MouseEvent arg0) 
	{
		buttons.add( arg0.getButton() );		
	}

	public void mouseReleased(MouseEvent arg0) 
	{				
		buttons.remove( arg0.getButton() );
	}

	public void keyPressed(KeyEvent arg0) 
	{
		keys.add( arg0.getKeyCode() );		
	}

	public void keyReleased(KeyEvent arg0) 
	{
//		getCanvas().getContext().makeCurrent();
		keys.remove( arg0.getKeyCode() );		
	}

	public void keyTyped(KeyEvent arg0) {
		
	}

	/**
	 * returns wheter the given mousebutton is down. use
	 * the constants in {@link MouseEvent}.
	 * 
	 * @param button the button in question
	 * @return wheter the button is down or not
	 */
	public boolean isButtonDown( int button )
	{
		return buttons.contains( button );
	}
	
	/**
	 * returns wheter the given key is down. use the
	 * constants in {@link KeyEvent}.
	 * 
	 * @param key the key in question
	 * @return wheter the key is down or not
	 */
	public boolean isKeyDown( int key )
	{
		return keys.contains( key );
	}
	
	/** 
	 * adds a new graphic listener 
	 * @param listener the listener
	 */
	public void addGraphicListener( RenderListener listener )
	{
		setupListeners.add( listener );
	}
	
	/**
	 * removes a graphic listener 
	 * @param listener the listener
	 */
	public void removeGraphicListener( RenderListener listener )
	{
		setupListeners.remove(listener);
		listeners.remove( listener );
		listener.dispose( application );
	}

	@Override
	public void display(GLAutoDrawable arg0) 
	{
		if( multiplexer != null )
			multiplexer.processEvents();
		deltaTime = (System.nanoTime() - frameStart ) / 1000000000.0f;
		frameStart = System.nanoTime();
		mean.addValue( deltaTime );
		
		for( RenderListener listener: setupListeners )
			listener.setup( application );
		listeners.addAll(setupListeners);
		setupListeners.clear();
		
		for( RenderListener listener: listeners )
			listener.render( application );	
		meassureFPS();				
	}

	@Override
	public void displayChanged(GLAutoDrawable arg0, boolean arg1, boolean arg2) 
	{
		deltaTime = (System.nanoTime() - frameStart ) / 1000000000.0f;
		frameStart = System.nanoTime();
		
		for( RenderListener listener: listeners )
			listener.render( application );	
		meassureFPS();
	}

	@Override
	public void init(GLAutoDrawable arg0) 
	{			
		for( RenderListener listener: listeners )
			listener.setup( application );		
	}

	@Override
	public void reshape(GLAutoDrawable arg0, int arg1, int arg2, int arg3, int arg4) 
	{
		deltaTime = (System.nanoTime() - frameStart ) / 1000000000.0f;
		frameStart = System.nanoTime();
		
		for( RenderListener listener: listeners )
			listener.render( application );	
		meassureFPS();
	}
	
	private void meassureFPS( )
	{
		frames++;
		if( System.nanoTime() - start_time > 1000000000 )
		{
			framesPerSecond = frames;
			frames = 0;
			start_time = System.nanoTime();		
			System.out.println( "AFX fps: " + framesPerSecond + ", #meshes: " + JoglMesh.meshes + ", #textures: " + JoglTexture.textures );
		}
	}
	
	/**
	 * @return the gl context this panel works on
	 */
	public GL getGL( )
	{
		return canvas.getGL();
	}

	public Point getLastMousePosition( )
	{
		return mousePosition;
	}

	public void setInputMultiplexer(JoglInputMultiplexer inputMultiplexer) {
		multiplexer = inputMultiplexer;
	}

	public boolean isAnyKeyDown() {
		return keys.size() > 0;
	}
}
