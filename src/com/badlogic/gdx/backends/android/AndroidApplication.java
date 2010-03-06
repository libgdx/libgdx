package com.badlogic.gdx.backends.android;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashSet;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.widget.EditText;

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
import com.badlogic.gdx.math.WindowedMean;

public class AndroidApplication extends Activity implements GLSurfaceView.Renderer, OnTouchListener, SensorEventListener, OnKeyListener, Application
{
	public GLSurfaceView glView;
	private long lastFrameTime = System.nanoTime();
	private int viewportWidth;
	private int viewportHeight;

	public boolean debug = true;
	public boolean debugFine = true;

	private int[] touchX = new int[10];
	private int[] touchY = new int[10];		
	private boolean[] touched = new boolean[10];	

	private HashSet<Integer> keys = new HashSet<Integer>( );
	
	public boolean accelerometerAvailable = false;	
	private SensorManager manager;
	private final float[] accelerometerValues = new float[3];

	private float tiltOffsetX = 0.0f;
	private float tiltOffsetY = 6.0f;	

	private int frames;
	private long framesStartTime = System.nanoTime();
	private float fps;
	private float deltaTime = 0;	
	private WindowedMean mean = new WindowedMean( 5 );
	private android.os.Vibrator vibrator;

	private String text = null;
	private TextInputListener textListener = null;
	private Handler handle;

	private GL10 gl;	

	private final ArrayList<GraphicListener> setupListeners = new ArrayList<GraphicListener>( );
	private final ArrayList<GraphicListener> listeners = new ArrayList<GraphicListener>( );
	private final ArrayList<InputListener> inputListeners = new ArrayList<InputListener>( );
	private final ArrayList<CloseListener> closeListeners = new ArrayList<CloseListener>();

	private enum EventType
	{
		MouseDown,
		MouseUp,
		MouseMoved,
		MouseDragged,
		KeyDown,
		KeyUp,
		KeyTyped
	}

	private class Event
	{
		public int x, y;
		public int button;
		public int pointer;
		public int keycode;
		public char keychar;
		public EventType type;

		public void set( EventType type, int x, int y, int button, int pointer, int keycode, char keychar )
		{
			this.type = type;
			this.x = x;
			this.y = y;
			this.pointer = pointer;
			this.keycode = keycode;
			this.keychar = keychar;
		}
	}	

	private final ArrayList<Event> eventQueue = new ArrayList<Event>( );
	private final ArrayList<Event> freeEvents = new ArrayList<Event>( );
	private int freeEventIndex = 0;

	private SoundPool soundPool;

	private boolean multitouchEnabled = false;
	
	@Override	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.d( "AFX", "application created" );
		
		// set landscape mode, no titlebar, fullscreen
		setRequestedOrientation(0);
		requestWindowFeature(Window.FEATURE_NO_TITLE);        
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN );

		// create gl view        
		glView = new GLSurfaceView(this)
		{
			public boolean onKeyDown( int keychar, KeyEvent event )
			{
				keys.add( keychar );
				return false;
			}
			
			@Override
			public boolean onKeyUp( int keychar, KeyEvent event )
			{
				keys.remove( keychar );
				return false;
			}
		};
		glView.setEGLConfigChooser(true);
		glView.setRenderer(this);		
		glView.setKeepScreenOn(true);		
		setContentView(glView);

		// set touch & key listener
		glView.setOnTouchListener( this );
		glView.setFocusable(true);
		glView.setFocusableInTouchMode(true);
		glView.requestFocus();		
		glView.requestFocusFromTouch();

		// check accelerometer & compass
		setupSensors(this);

		// setup sound manager
		setupSoundManager(this);

		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		for( int i = 0; i < 1000; i++ )
			freeEvents.add( new Event( ) );

		handle = new Handler();
		
	}	

	public void enableMultiTouch( boolean enable )
	{
		this.multitouchEnabled = enable;
	}
	
	private void setupSoundManager(AndroidApplication openGLESApplication) {		
		soundPool = new SoundPool( 5, AudioManager.STREAM_MUSIC, 100); 		
	}

	/**
	 * @return the viewport width
	 */
	public int getViewportWidth( )
	{
		return viewportWidth;
	}

	/**
	 * 
	 * @return the viewport height
	 */
	public int getViewportHeight( )
	{
		return viewportHeight;
	}


	/**
	 * Uses the accelerometer values to calculate
	 * an x coordinate based on the tilt around the
	 * z axis of the phone from [-1, 1]
	 * 
	 * @return the tilt x coordinate 
	 */
	public float getTiltX( )
	{
		float x = (accelerometerValues[1] - tiltOffsetX) / 2f;
		if( x > 1 )
			x = 1;
		if( x < -1 )
			x = -1;
		return x;
	}	

	/**
	 * Uses the accelerometer values to calculate
	 * an y coordinate based on the tilt around the
	 * x axis of the phone from [-1, 1]
	 * 
	 * @return the tilt x coordinate 
	 */
	public float getTiltY( )
	{
		float y = (tiltOffsetY - accelerometerValues[0]) / 2f;
		if( y > 1 )
			y = 1;
		if( y < - 1 )
			y = -1;
		return y;
	}


	/**
	 * zeros the accelerometer at the current location.
	 * This is used as the reference for determining
	 * the tilt x/y coordinates
	 */
	public void calibrateAccelerometer( )
	{
		tiltOffsetX = accelerometerValues[1];
		tiltOffsetY = accelerometerValues[0];
	}	

	/**
	 * @return wheter the accelerometer is available
	 */
	public boolean isAccelerometerAvailable( )
	{
		return accelerometerAvailable;
	}

	public void vibrate( int ms )
	{
		vibrator.vibrate( ms );
	}

	/**
	 * @return the delta time in seconds to the last frame
	 */
	public float getDeltaTime( )
	{
		return mean.getMean() == 0?deltaTime:mean.getMean();
	}

	@Override
	public void onDrawFrame(GL10 gl) 
	{				
		//		synchronized( setupListeners )
		//		{
		if( setupListeners.size() > 0 )
		{
			for( int i = 0; i < setupListeners.size(); i++ )
			{
				GraphicListener listener = setupListeners.get(i);
				listener.setup(this);
				listeners.add(listener);
			}				
			setupListeners.clear();
		}
		//		}

		processEvents( );
		if( textListener != null )
		{
			textListener.input( text );
			textListener = null;
		}
		this.gl = gl;
		deltaTime = ( System.nanoTime() - lastFrameTime ) / 1000000000.0f;
		lastFrameTime = System.nanoTime();
		mean.addValue( deltaTime );
		for( int i = 0; i < listeners.size(); i++ )		
			listeners.get(i).render( this );		
		//		gl.glFinish();
		float frameTime = (System.nanoTime()-lastFrameTime) / 1000000000.0f;
		if( System.nanoTime() - framesStartTime > 1000000000l )
		{
			fps = frames / ((System.nanoTime()-framesStartTime) / 1000000000.0f );
			frames = 0;
			framesStartTime = System.nanoTime();
			if( debug )
				Log.d( "AFX", "fps: " + fps + ", delta: " + deltaTime + ", mean delta: " + mean.getMean() + ", stddev. delta: " + mean.standardDeviation() + ", #listeners: " + listeners.size() + ", #meshes: " + AndroidMesh.meshes + ", #textures: " + AndroidTexture.textures );
		}

		frames++;
	}

	private void processEvents( )
	{
		synchronized( eventQueue )
		{
			for( int i = 0; i < eventQueue.size(); i++ )
			{
				Event event = eventQueue.get(i);
				if( event.type == EventType.MouseDown )
					for( int j = 0; j < inputListeners.size(); j++ )					
						if( inputListeners.get(j).mouseDown(event.x, event.y, event.button, event.pointer ) )
							break;
				if( event.type == EventType.MouseUp )
					for( int j = 0; j < inputListeners.size(); j++ )					
						if( inputListeners.get(j).mouseUp(event.x, event.y, event.button, event.pointer ) )
							break;
				if( event.type == EventType.MouseMoved )
					for( int j = 0; j < inputListeners.size(); j++ )					
						if( inputListeners.get(j).mouseMoved(event.x, event.y, event.button, event.pointer ) )
							break;
				if( event.type == EventType.MouseDragged )
					for( int j = 0; j < inputListeners.size(); j++ )					
						if( inputListeners.get(j).mouseDragged(event.x, event.y, event.button, event.pointer ) )
							break;
			}
			eventQueue.clear();
			freeEventIndex = 0;
		}
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) 
	{			
		this.gl = gl;

		String renderer = gl.glGetString( GL10.GL_RENDERER );
		Log.d( "AFX", "OpenGL Renderer: " + renderer );
		if( renderer.toLowerCase().contains("pixelflinger" ) )
			AndroidMesh.globalVBO = false;

		Log.d( "AFX", "Model: " + android.os.Build.MODEL );
		if( android.os.Build.MODEL.equals( "MB200" ) || android.os.Build.MODEL.equals( "MB220" ) || android.os.Build.MODEL.contains( "Behold" ) )
			AndroidMesh.globalVBO = false;
		
		
		for( int i = 0; i < listeners.size(); i++ )		
			listeners.get(i).render( this );	
		viewportWidth = width;
		viewportHeight = height;
		gl.glViewport( 0, 0, width, height );
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) 
	{	
		this.gl = gl;
		gl.glDisable( GL10.GL_DITHER );	
//		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST );
		//		lastFrameTime = System.nanoTime();		
//		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

	}		

	boolean requestedFocus = true;
	
	@Override
	public boolean onTouch(View v, MotionEvent event) 
	{		
		if( requestedFocus )
		{
			glView.requestFocus();		
			glView.requestFocusFromTouch();
			requestedFocus = false;
		}
		
		if(! multitouchEnabled )
		{
			singleTouch( event );
			sleep(16);
		}
//		else
//			multiTouch( event );
		
//		event.recycle();
		
		return true;
	}	
	
	private void sleep( int ms )
	{
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}	
	}
	
	private void singleTouch( MotionEvent event )
	{
		touchX[0] = (int)event.getX();
		touchY[0] = (int)event.getY();
		if( event.getAction() == MotionEvent.ACTION_DOWN )
		{
			synchronized( eventQueue )
			{
				Event ev = freeEvents.get(freeEventIndex++);
				ev.set( EventType.MouseDown, touchX[0], touchY[0], InputListener.MOUSE_BUTTON1, 0, 0, '\0' );
				eventQueue.add( ev );
			}

			touched[0] = true;
		}
		if( event.getAction() == MotionEvent.ACTION_MOVE )
		{
			synchronized( eventQueue )
			{				
				Event ev = freeEvents.get(freeEventIndex++);
				ev.set( EventType.MouseDragged, touchX[0], touchY[0], InputListener.MOUSE_BUTTON1, 0, 0, '\0' );
				eventQueue.add( ev );
			}
			touched[0] = true;			
		}
		if( event.getAction() == MotionEvent.ACTION_UP )
		{
			Log.d("AFX Input", "touch up" );
			synchronized( eventQueue )
			{
				Event ev = freeEvents.get(freeEventIndex++);
				ev.set( EventType.MouseUp, touchX[0], touchY[0], InputListener.MOUSE_BUTTON1, 0, 0, '\0' );
				eventQueue.add( ev );
			}
			touched[0] = false;			
		}
		
		if( event.getAction() == MotionEvent.ACTION_CANCEL )
		{	
			Log.d("AFX Input", "touch cancled" );
			synchronized( eventQueue )
			{
				Event ev = freeEvents.get(freeEventIndex++);
				ev.set( EventType.MouseUp, touchX[0], touchY[0], InputListener.MOUSE_BUTTON1, 0, 0, '\0' );
				eventQueue.add( ev );
			}
			touched[0] = false;			
		}
	}
	
//	private void multiTouch( MotionEvent event )
//	{
//		int action = event.getAction();
//	    int ptrId = event.getPointerId(0);
//	    if(event.getPointerCount() > 1)
//	        ptrId = (action & MotionEvent.ACTION_POINTER_ID_MASK) >> MotionEvent.ACTION_POINTER_ID_SHIFT;
//	    action = action & MotionEvent.ACTION_MASK;
//	    if(action < 7 && action > 4)
//	        action = action - 5;
//	    	
//		
//		if( action == MotionEvent.ACTION_DOWN )
//		{
//			for( int i = 0; i < event.getPointerCount(); i++ )
//			{
//				float x = event.getX(i);
//			    float y = event.getY(i);
//			    
//				touchX[event.getPointerId(i)] = (int)x;
//				touchY[event.getPointerId(i)] = (int)y;
//			}
//			
//			synchronized( eventQueue )
//			{
//				Event ev = freeEvents.get(freeEventIndex++);
//				ev.set( EventType.MouseDown, touchX[ptrId], touchY[ptrId], InputListener.MOUSE_BUTTON1, ptrId, 0, '\0' );
//				eventQueue.add( ev );
//			}
//
//			touched[ptrId] = true;
//		}
//		if( action == MotionEvent.ACTION_MOVE )
//		{
//			synchronized( eventQueue )
//			{				
//				for( int i = 0; i < event.getPointerCount(); i++ )
//				{
//					float x = event.getX(i);
//				    float y = event.getY(i);
//				    
//					touchX[event.getPointerId(i)] = (int)x;
//					touchY[event.getPointerId(i)] = (int)y;
//					Event ev = freeEvents.get(freeEventIndex++);
//					ev.set( EventType.MouseDragged, touchX[i], touchY[i], InputListener.MOUSE_BUTTON1, event.getPointerId(i), 0, '\0' );
//					eventQueue.add( ev );
//				}							
//			}			
//		}
//		if( action == MotionEvent.ACTION_UP )
//		{
//			synchronized( eventQueue )
//			{
//				Event ev = freeEvents.get(freeEventIndex++);
//				ev.set( EventType.MouseUp, touchX[ptrId], touchY[ptrId], InputListener.MOUSE_BUTTON1, ptrId, 0, '\0' );
//				eventQueue.add( ev );
//			}
//			touched[ptrId] = false;
//			
//			if( event.getPointerCount() == 1 )
//				for( int i = 0; i < 10; i++ )
//					touched[i] = false;
//		}
//		if( action == MotionEvent.ACTION_CANCEL )
//		{
//			touched[ptrId] = false;
//			if( event.getPointerCount() == 1 )
//			for( int i = 0; i < 10; i++ )
//				touched[i] = false;
//		}
//	}

			

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if( event.getAction() == KeyEvent.ACTION_DOWN )
			keys.add( event.getKeyCode() );
		if( event.getAction() == KeyEvent.ACTION_DOWN )
			keys.remove( event.getKeyCode() );			
		
		return false;
	}	

	public void setupSensors( Context context )
	{				
		manager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);

		//
		// install accelerometer listener
		//
		if( manager.getSensorList(Sensor.TYPE_ACCELEROMETER).size() == 0 )
			accelerometerAvailable = false;
		else
		{
			Sensor accelerometer = manager.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
			if( !manager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME ) )
				accelerometerAvailable = false;
			else
				accelerometerAvailable = true;
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) 
	{	

	}

	@Override
	public void onSensorChanged(SensorEvent event) 
	{			
		if( event.sensor.getType() == Sensor.TYPE_ACCELEROMETER )				
		{
			System.arraycopy( event.values, 0, accelerometerValues, 0, accelerometerValues.length );			
		}		
	}		

	@Override
	protected void onPause() {
		super.onPause();
		glView.onPause();
		for( CloseListener listener: closeListeners )
			listener.close();		
		//        Debug.stopMethodTracing();
		Log.d("AFX", "application paused" );
		System.exit(0);	
	}

	@Override
	protected void onResume() {
		super.onResume();
		glView.onResume();
		Log.d("AFX", "application resumed" );
		//        Debug.startMethodTracing("androllz");
	}

	@Override
	public void addGraphicListener(GraphicListener listener) 
	{
		synchronized( setupListeners )
		{
			setupListeners.add( listener );
		}
	}

	@Override
	public void addInputListener(InputListener listener) 
	{		
		inputListeners.add( listener );
	}

	@Override
	public void clear(boolean color, boolean depth, boolean stencil) 
	{	
		int flags = (color?GL10.GL_COLOR_BUFFER_BIT:0) |
		(depth?GL10.GL_DEPTH_BUFFER_BIT:0) |
		(stencil?GL10.GL_STENCIL_BUFFER_BIT:0);
		if( depth )
			gl.glDepthMask( true );
		gl.glClear( flags );		
	}

	@Override
	public void clearColor(float r, float g, float b, float a) {
		gl.glClearColor( r, g, b, a );
	}

	@Override
	public void color(float r, float g, float b, float a) {
		gl.glColor4f( r, g, b, a );
	}

	@Override
	public InputStream getResourceInputStream(String file) throws IOException {
		return getAssets().open( file );
	}

	@Override
	public String[] listResourceFiles(String directory) throws IOException {
		return getAssets().list( directory );
	}

	@Override
	public void loadIdentity() {
		gl.glLoadIdentity();
	}

	@Override
	public void loadMatrix(float[] matrix) {
		gl.glLoadMatrixf( matrix, 0 );		
	}

	@Override
	public void multMatrix(float[] matrix) {	
		gl.glMultMatrixf( matrix, 0 );
	}

	@Override
	public Mesh newMesh(int maxVertices, boolean hasColors, boolean hasNormals,
			boolean hasUV, boolean hasIndices, int maxIndices, boolean isStatic) 
	{
		return new AndroidMesh(gl, maxVertices, hasColors, hasNormals, hasUV, hasIndices, maxIndices, isStatic );
	}

	@Override
	public Texture newTexture(InputStream in, TextureFilter minFilter,
			TextureFilter maxFilter, TextureWrap uWrap, TextureWrap vWrap) 
	{			
		Bitmap image = BitmapFactory.decodeStream( in );
		return new AndroidTexture(gl, image, minFilter, maxFilter, uWrap, vWrap);
	}

	@Override
	public Texture newTexture(int width, int height, TextureFilter minFilter,
			TextureFilter maxFilter, TextureWrap uWrap, TextureWrap vWrap ) 
	{	
		Bitmap.Config config = Bitmap.Config.ARGB_8888;		
		Bitmap image = Bitmap.createBitmap(width, height, config);
		return new AndroidTexture(gl, image, minFilter, maxFilter, uWrap, vWrap);
	}

	@Override
	public void normal(float x, float y, float z) 
	{	
		gl.glNormal3f(x, y, z );
	}

	@Override
	public void removeGraphicListener(GraphicListener listener) 
	{	
		listener.dispose(this);
		listeners.remove( listener );
	}

	@Override
	public void removeInputListener(InputListener listener) 
	{	
		inputListeners.remove(listener);
	}

	@Override
	public void rotate(float angle, float x, float y, float z) {
		gl.glRotatef( angle, x, y, z );
	}

	@Override
	public void scale(float x, float y, float z) {
		gl.glScalef( x, y, z );
	}

	@Override
	public void setMatrixMode(MatrixMode mode) 
	{
		if( mode == MatrixMode.ModelView )
			gl.glMatrixMode( GL10.GL_MODELVIEW );
		if( mode == MatrixMode.Projection )
			gl.glMatrixMode( GL10.GL_PROJECTION );
		if( mode == MatrixMode.Texture )
			gl.glMatrixMode( GL10.GL_TEXTURE );
	}

	@Override
	public void translate(float x, float y, float z) {
		gl.glTranslatef( x, y, z );		
	}

	@Override
	public int getX() {
		return touchX[0];
	}

	@Override
	public int getY() {		
		return touchY[0];
	}

	@Override
	public boolean isPressed() 
	{	
		return touched[0];
	}
		

	@Override
	public void disable(RenderState state) {
		if( state == RenderState.Blending )
			gl.glDisable( GL10.GL_BLEND );
		if( state == RenderState.DepthTest )
		{
			gl.glDisable( GL10.GL_DEPTH_TEST );		
			gl.glDepthMask( false );
		}
		if( state == RenderState.Lighting )
			gl.glDisable( GL10.GL_LIGHTING );
		if( state == RenderState.Texturing )
			gl.glDisable( GL10.GL_TEXTURE_2D );
		if( state == RenderState.Culling )
			gl.glDisable( GL10.GL_CULL_FACE );
		if( state == RenderState.AlphaTest )
			gl.glDisable( GL10.GL_ALPHA_TEST );
	}

	@Override
	public void enable(RenderState state) {
		if( state == RenderState.Blending )
		{
			gl.glEnable( GL10.GL_BLEND );
			gl.glBlendFunc( GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA );
		}
		if( state == RenderState.DepthTest )
		{
			gl.glEnable( GL10.GL_DEPTH_TEST );
			gl.glDepthMask( true );
		}
		if( state == RenderState.Lighting )
		{
			gl.glEnable( GL10.GL_LIGHTING );
			gl.glEnable( GL10.GL_COLOR_MATERIAL );
		}
		if( state == RenderState.AlphaTest )
		{
			gl.glEnable( GL10.GL_ALPHA_TEST );
			gl.glAlphaFunc( GL10.GL_GREATER, 0.9f );
		}
		if( state == RenderState.Texturing )
			gl.glEnable( GL10.GL_TEXTURE_2D );
		if( state == RenderState.Culling )
			gl.glEnable( GL10.GL_CULL_FACE );
	}

	@Override
	public void log(String tag, String message) {
		Log.d(tag, message );
	}

	float[] position = new float[4];
	float[] color = new float[4];
	@Override
	public void setAmbientLight(float r, float g, float b, float a) {
		color[0] = r;
		color[1] = g;
		color[2] = b;
		color[3] = a;
		gl.glLightModelfv( GL10.GL_LIGHT_MODEL_AMBIENT, color, 0 );		
	}

	@Override
	public void disableLight(int light) 
	{	
		gl.glDisable( GL10.GL_LIGHT0 + light );
	}

	@Override
	public void enableLight(int light) {
		gl.glEnable( GL10.GL_LIGHT0 + light );			
	}

	@Override
	public void setDirectionalLight(int light, float x, float y, float z,
			float r, float g, float b, float a) {
		color[0] = 0;
		color[1] = 0;
		color[2] = 0;
		color[3] = 0;
		position[0] = -x;
		position[1] = -y;
		position[2] = -z;
		position[3] = 0;
		gl.glLightfv( GL10.GL_LIGHT0 + light, GL10.GL_AMBIENT, color, 0 );
		color[0] = r;
		color[1] = g;
		color[2] = b;
		color[3] = a;
		gl.glLightfv( GL10.GL_LIGHT0 + light, GL10.GL_DIFFUSE, color, 0 );
		gl.glLightfv( GL10.GL_LIGHT0 + light, GL10.GL_POSITION, position, 0 );

	}

	@Override
	public void flush() {
		gl.glFinish();
	}

	GLU glu = new GLU();
	public boolean error( )
	{
		return gl.glGetError() != GL10.GL_NO_ERROR;
	}

	@Override
	public void popMatrix() {
		gl.glPopMatrix();
	}

	@Override
	public void pushMatrix() {
		gl.glPushMatrix();		
	}

	@Override
	public Font newFont(String fontName, int size, FontStyle style) {
		return new AndroidFont( this, fontName, size, style );
	}

	@Override
	public Font newFontFromFile(String file, int size, FontStyle style) {
		return new AndroidFont( this, getAssets(), file, size, style );
	}

	@Override
	public void blendFunc(BlendFunc arg1, BlendFunc arg2) {
		gl.glBlendFunc( getBlendFuncValue( arg1 ), getBlendFuncValue( arg2 ) );
	}

	private int getBlendFuncValue( BlendFunc func )
	{
		if( func == BlendFunc.DestAlpha )
			return GL10.GL_DST_ALPHA;
		if( func == BlendFunc.DestColor )
			return GL10.GL_DST_COLOR;
		if( func == BlendFunc.One )
			return GL10.GL_ONE;
		if( func == BlendFunc.OneMinusDestAlpha )
			return GL10.GL_ONE_MINUS_DST_ALPHA;
		if( func == BlendFunc.OneMinusDestColor )
			return GL10.GL_ONE_MINUS_DST_COLOR;
		if( func == BlendFunc.OneMinusSourceAlpha )
			return GL10.GL_ONE_MINUS_SRC_ALPHA;
		if( func == BlendFunc.OneMinusSourceColor )
			return GL10.GL_ONE_MINUS_SRC_COLOR;
		if( func == BlendFunc.SourceAlpha )
			return GL10.GL_SRC_ALPHA;
		if( func == BlendFunc.SourceColor )
			return GL10.GL_SRC_COLOR;
		if( func == BlendFunc.Zero )
			return GL10.GL_ZERO;
		return GL10.GL_ONE;

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
	public void getTextInput(final TextInputListener listener, final String title, final String text ) {
		handle.post( new Runnable() 
		{
			public void run( )
			{


				AlertDialog.Builder alert = new AlertDialog.Builder(AndroidApplication.this);  

				alert.setTitle(title);  				 

				// Set an EditText view to get user input   
				final EditText input = new EditText(AndroidApplication.this);
				input.setText( text );
				input.setSingleLine();
				alert.setView(input);  

				alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {  
					public void onClick(DialogInterface dialog, int whichButton) {  
						AndroidApplication.this.text = input.getText().toString();  
						textListener = listener; 
					}  
				});  		   	
				alert.show();
			}
		} );
	}

	@Override
	public Sound newSound(String file) {
		try {
			AssetFileDescriptor descriptor = getAssets().openFd( file );
			return new AndroidSound( soundPool, (AudioManager)getSystemService(Context.AUDIO_SERVICE), soundPool.load( descriptor, 1) );
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException( "couldn't open sound '" + file + "'" );
		}
	}

	@Override
	public OutputStream getOutputStream(String file) throws IOException 
	{
		if( !new File( "/sdcard/" + file ).exists() )
			new File( "/sdcard/" + file ).createNewFile();
		return new FileOutputStream( "/sdcard/" + file );
	}

	@Override
	public InputStream getInputStream(String file) throws IOException 
	{
		if( !new File( "/sdcard/" + file ).exists() )
			new File( "/sdcard/" + file ).createNewFile();
		return new FileInputStream( "/sdcard/" + file );
	}

	@Override
	public void mkdir(String directory) {	
		new File( "/sdcard/" + directory ).mkdirs();
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
		if( func == DepthFunc.Always )
			gl.glDepthFunc( GL10.GL_ALWAYS );
		if( func == DepthFunc.Equal )
			gl.glDepthFunc( GL10.GL_EQUAL );
		if( func == DepthFunc.Greater )
			gl.glDepthFunc( GL10.GL_GREATER );
		if( func == DepthFunc.Less )
			gl.glDepthFunc( GL10.GL_LESS );
		if( func == DepthFunc.LessEqual )
			gl.glDepthFunc( GL10.GL_LEQUAL );
		if( func == DepthFunc.Never )
			gl.glDepthFunc( GL10.GL_NEVER );
		if( func == DepthFunc.GreaterEqual )
			gl.glDepthFunc( GL10.GL_GEQUAL );
		if( func == DepthFunc.NotEqual )
			gl.glDepthFunc( GL10.GL_NOTEQUAL );
	}

	@Override
	public boolean isKeyPressed(Keys key) {
		if( key == Keys.Any )
			return keys.size() > 0;
		else
			return false;
	}

	@Override
	public float getAccelerometerX() {
		return accelerometerValues[0];
	}

	@Override
	public float getAccelerometerY() {
		return accelerometerValues[1];
	}

	@Override
	public float getAccelerometerZ() {
		return accelerometerValues[2];
	}

	@Override
	public int getX(int pointer) {
		// TODO Auto-generated method stub
		return touchX[pointer];
	}

	@Override
	public int getY(int pointer) {
		// TODO Auto-generated method stub
		return touchY[pointer];
	}

	@Override
	public boolean isPressed(int pointer) 
	{	
		return touched[pointer];
	}

	@Override
	public boolean isAndroid() 
	{	
		return true;
	}

	@Override
	public Pixmap newPixmap(int width, int height, Format format) 
	{	
		return new AndroidPixmap(width, height, format);
	}

	@Override
	public Texture newTexture(Pixmap pixmap, TextureFilter minFilter, TextureFilter maxFilter, TextureWrap uWrap, TextureWrap vwrap) 
	{	
		return new AndroidTexture(gl, (Bitmap)pixmap.getNativePixmap(), minFilter, maxFilter, uWrap, vwrap);
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
	public Pixmap newPixmap(InputStream in, Pixmap.Format format ) 
	{
		Options options = new Options( );
		options.inPreferredConfig = AndroidPixmap.getInternalFormat( format );
		Bitmap bitmap = BitmapFactory.decodeStream( in, null, options );		
		return new AndroidPixmap( bitmap );
	}

	@Override
	public void setPointSize(int width) {
		gl.glPointSize( width );		
	}

	@Override
	public void setCullMode(CullMode order) {
		if( order == CullMode.Clockwise )
			gl.glCullFace( GL10.GL_CW );
		else
			gl.glCullFace( GL10.GL_CCW );		
	}

	@Override
	public Pixmap newPixmap(Object nativeImage) {
		return new AndroidPixmap( (Bitmap)nativeImage );
	}

	@Override
	public AudioDevice getAudioDevice()
	{	
		return new AndroidAudioDevice();
	}

	@Override
	public void interpolate(FloatBuffer src, FloatBuffer dst, FloatBuffer out, float alpha, int count) 
	{
		for( int i = 0; i < count; i++ )
		{
			float s = src.get( i );
			float d = dst.get( i );
			out.put( i, s + (d - s) * alpha );
		}	
	}
}
