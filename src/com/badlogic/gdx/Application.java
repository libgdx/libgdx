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
package com.badlogic.gdx;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.media.opengl.GL;

import com.badlogic.gdx.backends.jogl.JoglApplication;

/**
 * <p>
 * An Application wraps all features related to game development. This includes
 * the graphics system, the audio system, file i/o as well as input from devices
 * such as the keyboard, the mouse or a touch screen. Mouse and touch screen are
 * handled transparently.
 * </p> 
 * 
 * <p>
 * For now there's two implementations of the Application interface. One is used
 * on desktop PCs and is called {@link JoglApplication}. The other is used on
 * Android devices and is called AndroidApplication. The JoglApplication wraps
 * OpenGL for desktop PCs via Jogl. The AndroidApplication wraps a GLSurfaceView
 * and is derived from the Android class Activity.
 * </p> 
 * 
 * <p>
 * A basic application starts by instantiating either a JoglApplication or an
 * AndroidApplication. After the instantiation a {@link RenderListener} has to
 * be registered with the Application. A RenderListener is called on three
 * events: when it is created {@link RenderListener.setup()} is called, during
 * rendering {@link RenderListener.render()} is called repeatedly and when the
 * RenderListener is unregistered or the application is destroyed {@link RenderListener.dispose()}
 * is called.
 * </p>
 * 
 * <p>
 * The Application interface offers a couple of methods in order to keep things plattform independant.
 * These methods are grouped into several areas: graphics, sound, input and file i/o.
 * </p>
 * 
 * <p>
 * The graphics method are modeled after OpenGL. There's methods for matrix stack manipulation,
 * setting render states like blending, culling and so on, methods for creating and enabling
 * light sources and more. Browse through the method summary of this class to get more information.
 * Note that not all of OpenGL's functionality is exposed. For example there's no way to set
 * materials for lighting, by default color material is used. Apart from these methods that 
 * essentially wrap OpenGL calls there's also methods to create graphics related resources. The following
 * paragraphs describe the 4 graphics related resources briefly. 
 * </p>
 * 
 * <p>
 * A {@link Mesh} encapsulates all geometry business you have to deal with in your game. Meshes
 * can only be created by an Application by calling one of the {@link Application.newMesh()} methods. 
 * Note that you are yourself responsible to fee all resources associated with a mesh by 
 * calling a mesh's {@link Mesh.dispose()} Method. 
 * </p>
 * 
 * <p>
 * A {@link Texture} encapsulates a single OpenGL texture. As with Meshes you can only create a Texture
 * via an Application by calling {@link Application.newTexture()}. You also have to take care of 
 * releasing all resources of a texture by calling its {@link Texture.dispose()} method when you're done. 
 * </p>
 * 
 * <p>
 * A {@link Font} encapsulates a font to be used to render text in OpenGL. Internally it holds a texture
 * with the glyphs used by all texts that are created by this Font. You can only create a Font by calling
 * one of the {@link Application.newFont()} methods. When you are done using a Font you have to release
 * its resources via a call to {@link Font.dispose()}.
 * </p>
 * 
 * <p>
 * A {@link Pixmap} is a plattform agnostic way of working with bitmaps. Pixmaps can only be created
 * by an Application via a call to {@link Application.newPixmap()}. You can draw to pixmaps and even
 * draw pixmaps to textures for some nice effects. 
 * </p>
 * 
 * <p>
 * To summarize: the Application acts as a factory for graphic resources. Once you have obtained such a 
 * resource its up to you to handle releasing all system resources it encapsulates by calling the resource's
 * <code>dispose()</code> method. See the respective interface documentation for more information.
 * </p>
 * 
 * <p>
 * Apart from being a resource factory an Application also wraps OpenGL functionality. This includes
 * matrix stack manipulation, state changes and so on. Refer to the method documentations of this class 
 * for more information. Not all of OpenGLs functionality is wrapped of course. Feel free to extend this
 * but make sure that your additions work on the PC as well as on Android!
 * </p>
 * 
 * <p>
 * Obtaining user input can be done in two ways, either via polling or by registering an {@link InputListener}
 * which follows the event based paradigm.
 * </p>
 * 
 * <p>
 * Polling is done via the methods {@link Application.isKeyPressed()} for checking the state of keys, {@link Application.getX()}
 * and {@link Application.getY()} for checking the mouse or touch position, {@link Application.isPressed()} for checking
 * wheter any mouse button was pressed or the screen is touched and {@link Application.getAccelerometerX(), Application.getAccelerometerY()
 * and Application.getAccelerometerZ()} to get the state of the 3-axis accelerometer if one is available. Note that it is
 * not possible at the moment to check for a concrete mouse button (left, right, middle) as this is not supported on Android
 * devices. Also, checking for keys is currently not fully implemented as there's no enum yet that encapsulates all the possible
 * keys for both plattforms. This will be added in a later version FIXME
 * </p>
 *  
 * <p>
 * When registering an {@link InputListener} the application will relay input events to this InputListener in a synchronous 
 * way. This means that the methods of the registered InputListener will be called in the rendering thread of the application
 * which also calls the RenderListener. Internally events are queued and processed upon each rendering of a frame. This
 * allows you to forget about thread synchronization issues between the UI and your RenderListener which operates in a seperate
 * rendering thread. 
 * </p>
 * 
 * <p>
 * For getting text input form the user a special method exists called {@link Application.getTextInput()} which creates a
 * plattform specific text input dialog box for the user to use. A callback you specify will be called synchronously to
 * you rendering thread once input has finished.
 * </p>
 * 
 * <p>
 * Audio is not supported 100% at the moment. FIXME You can play small sound effects you have loaded via the {@link Application.newSound()}
 * method easily or do your own sound mixing with a call to {@link Application.getAudioDevice()} which lets you feed PCM data
 * to the audio device directly. Currently this only supports output of 44.1khz mono PCM data. This will change in the near future.
 * Another method called {@link Application.newMusic()} will be introduced that allows streaming and playback of normal audio files.
 * </p> 
 * 
 * <p>
 * File I/O methods provided by the Application interface allow you to get access to assets and the SD card on Android. To map this to
 * the desktop the assets directory is assumed to be the directory the application was started from. To simulate reading and writting
 * to and form an SD card on the desktop the current users home directory is used. See the respective methods of this interface for
 * more information. 
 * </p>
 * @author mzechner
 *
 */
public interface Application 
{
	/**
	 * Callback interface for {@link Application.getTextInput()}
	 * 
	 * @author mzechner
	 *
	 */
	public interface TextInputListener
	{
		public void input( String text );
	}
	
	/**
	 * Called when application is exiting.
	 * 
	 * @author mzechner
	 *
	 */
	public interface CloseListener
	{
		public void close( );
	}
	
	/**
	 * Texture filter enum featuring the 3 most used filters.
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
	 * 
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
	 * 
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
	
	/**
	 * Culling modes
	 * 
	 * @author mzechner
	 *
	 */
	public enum CullMode
	{
		Clockwise,
		CounterClockwise
	}
	
	/**
	 * Different styles used by a {@link Font}
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
	
	/**
	 * Blend functions
	 * 
	 * @author mzechner
	 *
	 */
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
	
	/**
	 * Depth test functions.
	 * 
	 * @author mzechner
	 *
	 */
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

	/**
	 * Keys. 
	 * 
	 * @author mzechner
	 *
	 */
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
	 * Adds a {@link RenderListener} to this application. The listener's
	 * setup method is called once before the render method is invoked. 
	 * The render method is called as long as the listener is registered
	 * with the application. Upon application shutdown the dispose method
	 * is called.
	 *
	 * @param listener The listener to add.
	 */
	public void addRenderListener( RenderListener listener );
	
	/**
	 *	Removes the {@link RenderListener} from this application. The listener's
	 *  dispose method is called.
	 * 
	 * @param listener The listener to remove.
	 */
	public void removeRenderListener( RenderListener listener );
	
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
	 * Enables/disables multitouch (experimental)
	 * @param enable
	 */
	public void enableMultiTouch( boolean enable );
	
	/**
	 * Lists the files and directories in the given directory. The directory
	 * is relative to the resource directory which is platform dependent. On
	 * Android the directory given is relative to the assets directory, on 
	 * the PC it is relative to the root directory of the application.
	 * 
	 * @param directory The directory to list.
	 * @return An array of files and directories with names relative to the resource directory
	 */
	public String[] listResourceFiles( String directory ) throws IOException;
	
	/**
	 * Opens an input stream to the given file relative to the resource directory. The
	 * resource directory is the asset directory on Android. On the PC this is the
	 * root directory of the application.
	 * 
	 * @param file The file
	 * @return The input stream
	 * @throws IOException
	 */
	public InputStream getResourceInputStream( String file ) throws IOException;
	
	/**
	 * Opens an output stream to the given file relative to a platform dependent
	 * directory. On Android this directory is relative to "/sdcard", on the PC
	 * this directory is relative to the user's directory, e.g. "/home/mzechner/"
	 * or "C:\Users\mzechner\".
	 * 
	 * @param file The file
	 * @return The OutputStream
	 * @throws IOException
	 */
	public OutputStream getOutputStream( String file ) throws IOException;
	
	/**
	 * Opens an output stream to the given file relative to a platform dependent
	 * directory. On Android this directory is relative to "/sdcard", on the PC
	 * this directory is relative to the user's directory, e.g. "/home/mzechner/"
	 * or "C:\Users\mzechner\".
	 * 
	 * @param file The file
	 * @return The OutputStream
	 * @throws IOException
	 */
	public InputStream getInputStream( String file ) throws IOException;
	
	/**
	 * Create a new direcotry relative to a platform dependent directory.
	 * On Android this directory is relative to "/sdcard", on the PC
	 * this directory is relative to the user's directory, e.g. "/home/mzechner/"
	 * or "C:\Users\mzechner\".
	 * 
	 * @param directory The directory
	 */
	public void mkdir( String directory );
	
	/**
	 * Loads the texture from the given file which points to an image. The file is relative
	 * to the assets directory on Android and relative to the applications directory on the PC.
	 * The parameters specify the textures minification and
	 * magnification filters as well as the texture wrap mode in u and v. 
	 * 
	 * @param string The file name
	 * @param minFilter The minification filter
	 * @param magFilter The magnification filter
	 * @param uWrap The wrap in u
	 * @param vWrap The wrap in v
	 * @return
	 */
	public Texture newTexture(String file, TextureFilter minFilter,	TextureFilter magFilter, TextureWrap uWrap, TextureWrap vWrap);
	
	/**
	 * Creates a new texture from the given InputStream which points to an image. The
	 * InputSteam is not closed. The parameters specify the textures minification and
	 * magnification filters as well as the texture wrap mode in u and v. 
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
	 * Creates a new blank texture with the given dimensions in pixels. 
	 * The parameters specify the textures minification and
	 * magnification filters as well as the texture wrap mode in u and v.  
	 * 
	 * @param width The width of the texture
	 * @param height The height of the texture
	 * @param minFiler The minification filter
	 * @param maxFilter The magnification filter
	 * @param uWrap The texture wrap in u
	 * @param vWrap The texture wrap in v
	 * @return The texture
	 */
	public Texture newTexture( int width, int height, TextureFilter minFilter, TextureFilter maxFilter, TextureWrap uWrap, TextureWrap vWrap );	
	
	/**
	 * Creates a texture from the specified {@link Pixmap}. Tries
	 * to create a texture with the format of the pixmap. 
	 * The parameters specify the textures minification and
	 * magnification filters as well as the texture wrap mode in u and v.
	 * Only power of two textures are supported. Behaviour for none power of
	 * two textures is not specified. 
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
	 * Creates a new empty {@link Mesh} with the given characteristics. 
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
	 * @param width The width in pixels.
	 * @param height The height in pixels.
	 * @param format The format. This is only a hint.
	 * @return The pixmap.
	 */
	public Pixmap newPixmap( int width, int height, Pixmap.Format format );
	
	/**
	 * Creates a new {@link Pixmap} from the given resource. The file is given
	 * relative to the assets root directory on Android and relative to
	 * the application directory on the PC.
	 * 
	 * @param file The file. 
	 * @return The Pixmap.
	 */
	public Pixmap newPixmap( String file, Pixmap.Format format );
	
	/**
	 * Creates a new Pixmap from the given input stream. The input 
	 * steam is not closed.
	 * 
	 * @param in The input steam
	 * @param format The format. This is only a hint.
	 * @return
	 */
	public Pixmap newPixmap( InputStream in, Pixmap.Format format );
	
	/**
	 * Creates a new Pixmap from a native image. On Android this is
	 * an instance of Bitmap, on the PC this is an instance of BufferedImage.
	 * 
	 * @param nativeImage The native image.
	 * @return The pixmap.
	 */
	public Pixmap newPixmap( Object nativeImage );
	
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
	 * applications directory on the PC and relative to the assets
	 * directory on Android.
	 * 
	 * @param file The file to load the font from
	 * @param size The size of the font in pixels
	 * @param style The style of the font
	 * @return The font object
	 */
	public Font newFontFromFile( String file, int size, FontStyle style );
	
	/**
	 * Sets the matrix mode. This equivalent to the OpenGL function glSetMatrixMode.
	 * 
	 * @param mode The matrix mode
	 */
	public void setMatrixMode( MatrixMode mode );
	
	/**
	 * Multiplies the current matrix on the current matrix stack with a translation matrix constructed from the arguments.
	 * This equivalent to the OpenGL function glTranslatef.
	 * 
	 * @param x Translation in x
	 * @param y Translation in y
	 * @param z Translation in z
	 */
	public void translate( float x, float y, float z );
	
	/**
	 * Multiplies the current matrix on the current matrix stack with the scale matrix constructed from the arguments
	 * This equivalent to the OpenGL function glScalef.
	 * 
	 * @param x Scale in x
	 * @param y Scale in y
	 * @param z Scale in z
	 */
	public void scale( float x, float y, float z );
	
	/**
	 * Multiplies the current matrix on the current matrix stack with the rotation matrix constructed from the arguments
	 * This equivalent to the OpenGL function glRotatef.
	 * 
	 * @param angle the angle in degrees
	 * @param x The rotation axis x component
	 * @param y The rotation axis y component
	 * @param z The rotation axis z component
	 */
	public void rotate( float angle, float x, float y, float z );
	
	/**
	 * Loads an identity matrix. This equivalent to the OpenGL function glLoadIdentity.
	 */
	public void loadIdentity( );
	
	/**
	 * Loads a 4x4 matrix in column major order to the current matrix on the current matrix stack.
	 * This equivalent to the OpenGL function glLoadMatrix.
	 * 
	 * @param matrix The matrix
	 */
	public void loadMatrix( float[] matrix );
	
	/**
	 * Multiplies the current Matrix on the current matrix stack with the given matrix. The
	 * given matrix must be given in column major order. This equivalent to the OpenGL function glMultMatrix.
	 * 
	 * @param matrix The matrix
	 */
	public void multMatrix( float[] matrix );
	
	/**
	 * Pushes the current matrix. This equivalent to the OpenGL function glPushMatrix.
	 */
	public void pushMatrix();

	/** 
	 * Pops the current matrix. This equivalent to the OpenGL function glPopMatrix.
	 */
	public void popMatrix();
	
	/**
	 * @return The width of the viewport in pixels
	 */
	public int getViewportWidth( );
	
	/**
	 * @return The height of the viewport in pixels
	 */
	public int getViewportHeight( );
	
	/**
	 * Clears the buffers for which the flag is set to true. Uses the
	 * current clear color set by {@link Application.clearColor()} and
	 * the OpenGL default values for the depth buffer and stencil buffer.
	 * 
	 * FIXME should also be able to specify depth and stencil clear value
	 * 
	 * @param color clear the color (frame) buffer
	 * @param depth clear the depth buffer
	 * @param stencil clear the stencil buffer (not supported yet)
	 */
	public void clear( boolean color, boolean depth, boolean stencil );
	
	/**
	 * Sets the clear color.
	 * 
	 * @param r Red component
	 * @param g Green component
	 * @param b Blue component
	 * @param a Alpha component
	 */
	public void clearColor( float r, float g, float b, float a );
	
	/**
	 * Sets the current color.
	 * 
	 * @param r Red component
	 * @param g Green component
	 * @param b Blue component
	 * @param a Alpha component
	 */
	public void color( float r, float g, float b, float a );
	
	/**
	 * Sets the current normal
	 * 
	 * @param x X component
	 * @param y Y component
	 * @param z Z component
	 */
	public void normal( float x, float y, float z );	
	
	/**
	 * Enables the given {@link RenderState}. This is equivalent to
	 * glEnable. A couple of convenience settings are performed for
	 * a vew RenderStates:
	 * 
	 * <ul>
	 * 	<li>{@link RenderState.Lighting} will also enable GL_COLOR_MATERIAL</li>
	 *  <li>{@link RenderState.DepthTest} will also call glDepthMask( true )</li>
	 *  <li>{@link RenderState.AlphaTest} will also call gl.glAlphaFunc( GL.GL_GREATER, 0.9f )</li>  
	 * </ul> 
	 * 
	 * @param state The render state to enable
	 */
	public void enable( RenderState state );
	
	/**
	 * Disables the given {@link RenderState}. 
	 * 
	 * <ul>
	 * 	<li>{@link RenderState.DepthTest} will also call glDepthMask( false )</li>
	 * </ul>
	 * 
	 * @param state The render state
	 */
	public void disable( RenderState state );
	
	/**
	 * Sets the point size
	 * 
	 * @param width in pixels
	 */
	public void setPointSize( int width );
	
	/**
	 * Sets the blend function. This is equal to glBlendFunc.
	 * 
	 * @param srcArg The source argument
	 * @param dstArg The target argument
	 */
	public void blendFunc( BlendFunc srcArg, BlendFunc dstArg );

	/**
	 * Sets the depth function. This is equal to glDepthFunc.
	 * @param func The depth function
	 */
	public void depthFunc( DepthFunc func );
	
	/**
	 * Sets the culling winding order. This is equal to glCullFace.
	 * 
	 * @param order The culling winding order.
	 */
	public void setCullMode(CullMode order);

	/**
	 * Sets the ambient light color. This is equal to calling
	 * gl.glLightModelfv( GL.GL_LIGHT_MODEL_AMBIENT, color, 0 ).
	 * 
	 * @param r The red component
	 * @param g The green component
	 * @param b The blue component
	 * @param a The alpha component
	 */
	public void setAmbientLight( float r, float g, float b, float a );
	
	/**
	 * Sets the light to the given direction and color. It has to be
	 * enabled via enableLight. The direction has to be normalized. Sets
	 * the ambient and diffuse color of the directional light to the 
	 * given color components. As oposed to the standard notion of OpenGL
	 * the light direction is really given as a direction and not as a
	 * negative direction as usual!
	 * 
	 * @param light The number of the light source
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
	 * Enables the given light. This is equal to glEnable( light ).
	 * 
	 * @param light the light number
	 */
	public void disableLight( int light );
	
	/**
	 * Disables the light. This is equal to glDisable( light ).
	 * @param light The light number
	 */
	public void enableLight( int light );
	
	/**
	 * makes sure that any rendering commands are executed. This
	 * is equal to calling glFinish.
	 */
	public void flush();
	
	/** 
	 * @return wheter an accelerometer is available
	 */
	public boolean isAccelerometerAvailable( );	
	
	/** 
	 * @return The value of the accelerometer on its x-axis. ranges between [-10,10].
	 */
	public float getAccelerometerX( );
	
	/** 
	 * @return The value of the accelerometer on its y-axis. ranges between [-10,10].
	 */
	public float getAccelerometerY( );
	
	/** 
	 * @return The value of the accelerometer on its y-axis. ranges between [-10,10].
	 */
	public float getAccelerometerZ( );
	
	/**
	 * @return the mouse x position in screen coordinates. The screen origin is the top left corner.
	 */
	public int getX( );
	
	/**
	 * @return the mouse x position in screen coordinates. The screen origin is the top left corner.
	 */
	public int getY( );
	
	/**
	 * @return Wheter any button of the mouse is pressed
	 */
	public boolean isPressed( );
	
	/**
	 * Multitouch extension. Experimental.
	 * 
	 * @param pointer the pointer index.
	 * @return The x-coordinate of the pointer
	 */
	public int getX( int pointer );
	
	/**
	 * Multitouch extension. Experimental.
	 * 
	 * @param pointer the pointer index.
	 * @return The y-coordinate of the pointer
	 */
	public int getY( int pointer );
	
	/**
	 * Multitouch extension. Experimental.
	 * 
	 * @param pointer the pointer index.
	 * @return Wheter the pointer is down or not.
	 */
	public boolean isPressed( int pointer );
	
	/**
	 * Wheter the key is pressed.
	 * 
	 * @param key The key.
	 * @return True or false.
	 */
	public boolean isKeyPressed( Keys key );
	
	/**
	 * System dependent method to input a string of text. A dialog
	 * box will be created with the given title and the given text
	 * as a message for the user. Once the dialog has been closed
	 * the provided {@link TextInputListener} will be called in the
	 * rendering thread.
	 * 
	 * @param listener The TextInputListener.
	 * @param title The title of the text input dialog.
	 * @param text The message presented to the user.
	 */
	public void getTextInput( TextInputListener listener, String title, String text );
	
	/**
	 * @return the delta time in seconds to the previous frame.
	 */
	public float getDeltaTime();	
	
	/**
	 * logs the given message with the given tag to some output (e.g. System.out, Log.d)
	 * @param tag The tag
	 * @param message the message
	 */
	public void log( String tag, String message );
	
	/**
	 * Creates a new {@link Sound} form a file. The file is given
	 * relative to the assets directory on Android and relative to the
	 * applications directory on the PC.
	 * @param file The file.
	 * @return The Sound instance.
	 */
	public Sound newSound( String file );
	
	/**
	 * @return the AudioDevice in 44100Hz, mono mode. 
	 */
	public AudioDevice getAudioDevice( );
	
	/**
	 * @return wheter we are on Android or not.
	 */
	public boolean isAndroid( );
}

