package com.badlogic.gdx;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.FloatBuffer;

/**
 * A GraphicPanel wraps the system dependent GLCanvas and rendering
 * loop. It offers methods to poll the current keyboard and mouse/touchscreen
 * state. Additionally it features a listener concept for setup,
 * rendering and disposal of {@link GraphicListener}s. It also provides means
 * to access resources in a resource directory. The location of the directory
 * is platform dependent. The {@link Application} is responsible for creating
 * {@link Texture}s and {@Mesh}es.
 * 
 * @author mzechner
 *
 */
public interface Application 
{
	/**
	 * Called when getTextInput has finished
	 * @author mzechner
	 *
	 */
	public interface TextInputListener
	{
		public void input( String text );
	}
	
	/**
	 * Called when application is done
	 * @author mzechner
	 *
	 */
	public interface CloseListener
	{
		public void close( );
	}
	
	/**
	 * Texture filter enum featuring the 3 most used filters
	 * @author mzechner
	 *
	 */
	public enum TextureFilter
	{
		Nearest,
		Linear,
		MipMap
	}
	
	/**
	 * Texture wrap enum
	 * @author mzechner
	 *
	 */
	public enum TextureWrap
	{
		ClampToEdge,
		Wrap
	}
	
	/**
	 * Matrix mode enum
	 * 
	 * @author mzechner
	 *
	 */
	public enum MatrixMode
	{
		ModelView,
		Projection,
		Texture
	}
	
	/**
	 * Renderstates that can be enabled/disabled
	 * @author mzechner
	 *
	 */
	public enum RenderState
	{
		DepthTest,
		Lighting,
		Blending,
		AlphaTest,
		Texturing, 
		Culling
	}
	
	public enum CullMode
	{
		Clockwise,
		CounterClockwise
	}
	
	/**
	 * A FontStyle defines the style of a font
	 * @author mzechner
	 *
	 */
	public enum FontStyle
	{
		Plain,
		Bold,
		Italic,
		BoldItalic
	}
	
	public enum BlendFunc
	{
		Zero,
		One,
		SourceColor,
		DestColor,
		OneMinusDestColor,
		OneMinusSourceColor,
		SourceAlpha,
		OneMinusSourceAlpha,
		DestAlpha,
		OneMinusDestAlpha,
	}
	
	public enum DepthFunc
	{
		Never,
		Less,
		Equal,
		LessEqual,
		Greater,
		GreaterEqual,
		NotEqual,
		Always		
	}
	
	public enum Keys
	{
		Left,
		Right,
		Up,
		Down,
		Shift,
		Control,
		Space,
		Any
	}
	
	/**
	 * Adds a {@link GraphicListener} to this application. The listener's
	 * setup method is called once before the render method is invoked. 
	 * The render method is called as long as the listener is registered
	 * with the application. Upon application shutdown the dispose method
	 * is called.
	 *
	 * @param listener The listener to add.
	 */
	public void addGraphicListener( GraphicListener listener );
	
	/**
	 *	Removes the {@link GraphicListener} from this application. The listener's
	 *  dispose method is called.
	 * 
	 * @param listener The listener to remove.
	 */
	public void removeGraphicListener( GraphicListener listener );
	
	/**
	 * Adds an {@link InputListener} to this application. The order input listeners
	 * are added is the same as the order in which they are accessed. If an input
	 * listener signals that it processed the event the event is not passed to
	 * the other listeners in the chain.
	 * 
	 * @param listener The listener
	 */
	public void addInputListener( InputListener listener );
	
	/**
	 * Removes the {@link InputListener} from this application.
	 * @param listener The listener
	 */
	public void removeInputListener( InputListener listener );
	
	/**
	 * Adds a close listener. 
	 * @param listener The listener
	 */
	public void addCloseListener( CloseListener listener );
	
	/**
	 * Removes a close listener
	 * @param listener The listener
	 */
	public void removeCloseListener( CloseListener listener );	
	
	/**
	 * Enables/disables multitouch
	 * @param enable
	 */
	public void enableMultiTouch( boolean enable );
	
	/**
	 * Lists the files and directories in the given directory. The directory
	 * is relative to the resource directory which is platform dependent.
	 * 
	 * @param directory The directory to list.
	 * @return An array of files and directories with names relative to the resource directory
	 */
	public String[] listResourceFiles( String directory ) throws IOException;
	
	/**
	 * Opens an input stream to the given file relative to the resource directory
	 * @param file The file
	 * @return The input stream
	 * @throws IOException
	 */
	public InputStream getResourceInputStream( String file ) throws IOException;
	
	/**
	 * Opens an output stream to the given file relative to a platform dependent
	 * directory (e.g. android /sdcard/, linux /home/user/ )
	 * @param file The file
	 * @return The OutputStream
	 * @throws IOException
	 */
	public OutputStream getOutputStream( String file ) throws IOException;
	
	/**
	 * Opens an output stream to the given file relative to a platform dependent
	 * directory (e.g. android /sdcard/, linux /home/user/ )
	 * @param file The file
	 * @return The OutputStream
	 * @throws IOException
	 */
	public InputStream getInputStream( String file ) throws IOException;
	
	/**
	 * Create a new direcotry relative to a platform dependent directory
	 * @param directory The directory
	 */
	public void mkdir( String directory );
	
	/**
	 * Creates a new texture from the given inputstream which points to an image. The
	 * inputstream is not closed.
	 * 
	 * @param in The inputstream
	 * @param minFiler The minification filter
	 * @param maxFilter The magnification filter
	 * @param uWrap The texture wrap in u
	 * @param vWrap The texture wrap in v
	 * @return The texture
	 */
	public Texture newTexture( InputStream in, TextureFilter minFiler, TextureFilter maxFilter, TextureWrap uWrap, TextureWrap vWrap );
	
	/**
	 * Creates a new texture with the given dimensions. 
	 * 
	 * @param width The width of the texture
	 * @param height The height of the texture
	 * @param minFiler The minification filter
	 * @param maxFilter The magnification filter
	 * @param uWrap The texture wrap in u
	 * @param vWrap The texture wrap in v
	 * @param alpha wheter this is an alpha texture
	 * @return The texture
	 */
	public Texture newTexture( int width, int height, TextureFilter minFilter, TextureFilter maxFilter, TextureWrap uWrap, TextureWrap vWrap );	
	
	/**
	 * Creates a texture from the specified pixmap. Tries
	 * to create a texture with the format of the pixmap.
	 * 
	 * @param pixmap The pixmap.
	 * @param minFilter The minification filter.
	 * @param maxFilter The magnification filter.
	 * @param uWrap The wrap in u.
	 * @param vwrap The wrap in v.
	 * @return The texture.
	 */
	public Texture newTexture( Pixmap pixmap, TextureFilter minFilter, TextureFilter maxFilter, TextureWrap uWrap, TextureWrap vwrap );
	
	/**
	 * Creates a new empty mesh. 
	 * 
	 * @param numVertices The maximum number of vertices
	 * @param hasColors Wheter the mesh has colors
	 * @param hasNormals Wheter the mesh has normals
	 * @param hasUV Wheter the mesh has texture coordinates
	 * @param hasIndices Wheter the mesh has indices
	 * @param numIndices The maximum number of indices
	 * @param isStatic Wheter the mesh is static or not.
	 * @return The mesh
	 */
	public Mesh newMesh( int maxVertices, boolean hasColors, boolean hasNormals, boolean hasUV, boolean hasIndices, int maxIndices, boolean isStatic );
	
	/**
	 * Creates a new Pixmap for drawing.
	 * 
	 * @param width
	 * @param height
	 * @param format
	 * @return
	 */
	public Pixmap newPixmap( int width, int height, Pixmap.Format format );
	
	/**
	 * Creates a new Pixmap from the given resource
	 * @param file
	 * @return
	 */
	public Pixmap newPixmap( String file, Pixmap.Format format );
	
	/**
	 * Creates a new Pixmap from the given input stream
	 * @param in
	 * @return
	 */
	public Pixmap newPixmap( InputStream in, Pixmap.Format format );
	
	/**
	 * Creates a new Pixmap from a native image.
	 * @param nativeImage The native image.
	 * @return The pixmap.
	 */
	public Pixmap newPixmap( Object nativeImage );
	
	/**
	 * Sets the matrix mode.
	 * @param mode The matrix mode
	 */
	public void setMatrixMode( MatrixMode mode );
	
	/**
	 * Multiplies the current matrix with a translation matrix constructed from the arguments
	 * @param x Translation in x
	 * @param y Translation in y
	 * @param z Translation in z
	 */
	public void translate( float x, float y, float z );
	
	/**
	 * Multiplies the current matrix with the scale matrix constructed from the arguments
	 * @param x Scale in x
	 * @param y Scale in y
	 * @param z Scale in z
	 */
	public void scale( float x, float y, float z );
	
	/**
	 * Multiplies the current Matrix with the rotation matrix constructed from the arguments
	 * @param angle the angle in degrees
	 * @param x The rotation axis x component
	 * @param y The rotation axis y component
	 * @param z The rotation axis z component
	 */
	public void rotate( float angle, float x, float y, float z );
	
	/**
	 * Loads an identity matrix
	 */
	public void loadIdentity( );
	
	/**
	 * Loads a 4x4 matrix
	 * @param matrix The matrix
	 */
	public void loadMatrix( float[] matrix );
	
	/**
	 * Multiplies the current Matrix with the given matrix.
	 * @param matrix The matrix
	 */
	public void multMatrix( float[] matrix );
	
	/**
	 * Clears the buffers for which the flag is set to true
	 */
	public void clear( boolean color, boolean depth, boolean stencil );
	
	/**
	 * Sets the clear color.
	 * @param r Red component
	 * @param g Green component
	 * @param b Blue component
	 * @param a Alpha component
	 */
	public void clearColor( float r, float g, float b, float a );
	
	/**
	 * Sets the current clear color.
	 * @param r Red component
	 * @param g Green component
	 * @param b Blue component
	 * @param a Alpha component
	 */
	public void color( float r, float g, float b, float a );
	
	/**
	 * Sets the current normal
	 * @param x X component
	 * @param y Y component
	 * @param z Z component
	 */
	public void normal( float x, float y, float z );	
	
	/**
	 * @return The width of the viewport in pixels
	 */
	public int getViewportWidth( );
	
	/**
	 * @return The height of the viewport in pixels
	 */
	public int getViewportHeight( );
	
	/**
	 * wheter an accelerometer is available
	 * @return
	 */
	public boolean isAccelerometerAvailable( );
	
	/**
	 * Sets the origin of the tilt to the devices current orientation
	 */
	public void calibrateAccelerometer( );
	
	public float getAccelerometerX( );
	public float getAccelerometerY( );
	public float getAccelerometerZ( );
	
	/**
	 * Returns the tilt of the phone in x in range [-1,1]
	 * @return The tilt.
	 */
	public float getTiltX( );
	
	/**
	 * Returns the tilt of the phone in y in range [-1,1];
	 * @return
	 */
	public float getTiltY( );
	
	/**
	 * @return the mouse x position in screen coordinates
	 */
	public int getX( );
	
	/**
	 * @return the mouse x position in screen coordinates
	 */
	public int getY( );
	
	/**
	 * Wheter any button of the mouse is pressed
	 * @return
	 */
	public boolean isPressed( );
	
	public int getX( int pointer );
	public int getY( int pointer );
	public boolean isPressed( int pointer );
	
	/**
	 * Wheter the key is pressed.
	 * @param key The key.
	 * @return True or false.
	 */
	public boolean isKeyPressed( Keys key );
	
	/**
	 * @return the delta time in seconds to the previous frame
	 */
	public float getDeltaTime();
	
	/**
	 * Enables the given {@link RenderState}
	 * @param state The render state
	 */
	public void enable( RenderState state );
	
	/**
	 * Disables the given {@link RenderState}
	 * @param state The render state
	 */
	public void disable( RenderState state );

	/**
	 * Sets the ambient light color
	 * @param r The red component
	 * @param g The green component
	 * @param b The blue component
	 * @param a The alpha component
	 */
	public void setAmbientLight( float r, float g, float b, float a );
	
	/**
	 * Sets the light to the given direction and color. It has to be
	 * enabled via enableLight. The direction has to be normalized.
	 * 
	 * @param x The direction x component
	 * @param y The direction y component
	 * @param z The direction z component
	 * @param r The red component
	 * @param g The green component
	 * @param b The blue component
	 * @param a The alpha component
	 */
	public void setDirectionalLight( int light, float x, float y, float z, float r, float g, float b, float a );
	
	/**
	 * Enables the light
	 * @param light the light number
	 */
	public void disableLight( int light );
	
	/**
	 * Disables the light
	 * @param light The light number
	 */
	public void enableLight( int light );
	
	/**
	 * logs the given message with the given tag to some output (e.g. System.out)
	 * @param tag The tag
	 * @param message the message
	 */
	public void log( String tag, String message );

	/**
	 * makes sure that any rendering commands are executed
	 */
	public void flush();
	
	/**
	 *
	 * @return Wheter an error occured in the pipeline
	 */
	public boolean error( );

	/**
	 * pushes the current matrix
	 */
	public void pushMatrix();

	/** 
	 * pops the current matrix
	 */
	public void popMatrix();

	/**
	 * Returns a font object for the attributes specified 
	 * @param fontName The name of the font
	 * @param size The size of the font in pixels
	 * @param style The style of the font
	 * @return The font
	 */
	public Font newFont( String fontName, int size, FontStyle style );
	
	/**
	 * Returns a font object for the attributes specified loaded
	 * from the given file. The loading is done internally with the
	 * getInputStream method so the file path is relative to the
	 * applications resource directory.
	 * 
	 * @param file The file to load the font from
	 * @param size The size of the font in pixels
	 * @param style The style of the font
	 * @return The font object
	 */
	public Font newFontFromFile( String file, int size, FontStyle style );
	
	/**
	 * Sets the blend function
	 * @param arg1
	 * @param arg2
	 */
	public void blendFunc( BlendFunc arg1, BlendFunc arg2 );

	/**
	 * Sets the depth func
	 * @param func
	 */
	public void depthFunc( DepthFunc func );
	
	/**
	 * Loads the texture from the resource
	 * @param string
	 * @param minFilter
	 * @param magFilter
	 * @param vWrap
	 * @param vWrap
	 * @return
	 */
	public Texture newTexture(String file, TextureFilter minFilter,	TextureFilter magFilter, TextureWrap uWrap, TextureWrap vWrap);

	/**
	 * System dependent method to input a string of text. The 
	 * listener will be called in the render thread. 
	 * 
	 */
	public void getTextInput( TextInputListener listener, String title, String text );
	
	/**
	 * creates a new sound from a file.
	 * @param file The file
	 * @return The sound
	 */
	public Sound newSound( String file );
	
	/**
	 * Sets the point size
	 * @param width in pixels
	 */
	public void setPointSize( int width );
	
	public boolean isAndroid( );

	/**
	 * Sets the culling winding order.
	 * @param counterclockwise
	 */
	public void setCullMode(CullMode order);
	
	/**
	 * Interpolates the given float buffers
	 * @param src
	 * @param dst
	 * @param out
	 * @param alpha
	 * @param count
	 */
	public void interpolate( FloatBuffer src, FloatBuffer dst, FloatBuffer out, float alpha, int count );
	
	/**
	 * 
	 * @return the audio device in 44100Hz, mono mode. 
	 */
	public AudioDevice getAudioDevice( );
}

