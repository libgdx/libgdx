package com.badlogic.gdx.backends.desktop;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;

import android.util.Log;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.ShaderProgram;
import com.badlogic.gdx.math.Matrix;

/**
 * Implementation of {@link ShaderProgram} for Jogl.
 * 
 * @author mzechner
 *
 */
public class JoglShaderProgram implements ShaderProgram
{
	/** the gl instance **/
	private final GL20 gl;

	/** the log **/
	private String log = "";
	
	/** whether this program compiled succesfully **/
	private boolean isCompiled;
	
	/** uniform lookup **/
	private final HashMap<String, Integer> uniforms = new HashMap<String, Integer>();
	
	/** attribute lookup **/
	private final HashMap<String, Integer> attributes = new HashMap<String, Integer>( );

	/** program handle **/
	private int program;
	
	/** vertex shader handle **/
	private int vertexShaderHandle;
	
	/** fragment shader handle **/
	private int fragmentShaderHandle;
	
	/**
	 * Construcs a new JOglShaderProgram and immediatly compiles it. 
	 * 
	 * @param gl the GL20 instance
	 * @param vertexShader the vertex shader
	 * @param fragmentShader the fragment shader
	 */
	
	public JoglShaderProgram( GL20 gl, String vertexShader, String fragmentShader )
	{
		if( gl == null )
			throw new IllegalArgumentException( "gl must not be null" );
		if( vertexShader == null )
			throw new IllegalArgumentException( "vertex shader must not be null" );
		if( fragmentShader == null )
			throw new IllegalArgumentException( "fragment shader must not be null" );
		this.gl = gl;
		
		compileShaders( vertexShader, fragmentShader );	
	}
	
	/**
	 * Loads and compiles the shaders, creates a new program and links the shaders.
	 * @param vertexShader
	 * @param fragmentShader
	 */
	private void compileShaders( String vertexShader, String fragmentShader )
	{
		vertexShaderHandle = loadShader( GL20.GL_VERTEX_SHADER, vertexShader );
		fragmentShaderHandle = loadShader( GL20.GL_FRAGMENT_SHADER, fragmentShader );
		
		if( vertexShaderHandle == -1 || fragmentShaderHandle == -1 )
		{
			isCompiled = false;
			return;
		}
		
		program = linkProgram( );
		if( program == -1 )
		{
			isCompiled = false;
			return;
		}
		
		isCompiled = true;
	}
	
	private int loadShader( int type, String source )
	{
		ByteBuffer tmp = ByteBuffer.allocateDirect(4);
		tmp.order(ByteOrder.nativeOrder());
		IntBuffer intbuf = tmp.asIntBuffer();
		
		int shader = gl.glCreateShader( type );
		if( shader == 0 )
			return -1;
		
		gl.glShaderSource( shader, source );
		gl.glCompileShader( shader );
		gl.glGetShaderiv( shader, GL20.GL_COMPILE_STATUS, intbuf );
		
		int compiled = intbuf.get(0);
		if( compiled == 0 )
		{					
			gl.glGetShaderiv( shader, GL20.GL_INFO_LOG_LENGTH, intbuf );
			int infoLogLength = intbuf.get(0);
			if( infoLogLength > 1 )
			{
				String infoLog = gl.glGetShaderInfoLog( shader );
				log += infoLog;
			}
			return -1;
		}
		
		return shader;
	}
	
	private int linkProgram( )
	{
		int program = gl.glCreateProgram();
		if( program == 0 )		
			return -1;		
		
		gl.glAttachShader( program, vertexShaderHandle );
		gl.glAttachShader( program, fragmentShaderHandle );				
		gl.glLinkProgram( program );
		
		ByteBuffer tmp = ByteBuffer.allocateDirect(4);
		tmp.order(ByteOrder.nativeOrder());
		IntBuffer intbuf = tmp.asIntBuffer();
		
		gl.glGetProgramiv( program, GL20.GL_LINK_STATUS, intbuf );
		int linked = intbuf.get(0);
		if( linked == 0 )
		{
			gl.glGetProgramiv( program, GL20.GL_INFO_LOG_LENGTH, intbuf );
			int infoLogLength = intbuf.get(0);
			if( infoLogLength > 1 )			
				log += gl.glGetProgramInfoLog( program );								
			
			return -1;
		}
		
		return program;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getLog() 
	{	
		return log;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isCompiled() 
	{	
		return isCompiled;
	}

	private int fetchAttributeLocation( String name )
	{
		Integer location;
		if( (location = attributes.get( name )) == null )
		{
			location = gl.glGetAttribLocation( program, name );
			if( location == - 1 )
				throw new IllegalArgumentException( "no attribute with name '" + name + "' in shader" );
			attributes.put( name, location );			
		}
		return location;
	}
	
	private int fetchUniformLocation( String name )
	{
		Integer location;
		if( (location = uniforms.get( name )) == null )
		{
			location = gl.glGetAttribLocation( program, name );
			if( location == - 1 )
				throw new IllegalArgumentException( "no uniform with name '" + name + "' in shader" );
			uniforms.put( name, location );			
		}
		return location;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setUniform(String name, int value) 
	{	
		int location = fetchUniformLocation(name);		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setUniform(String name, int value1, int value2) 
	{	
		int location = fetchUniformLocation(name);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setUniform(String name, int value1, int value2, int value3) 
	{	
		int location = fetchUniformLocation(name);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setUniform(String name, int value1, int value2, int value3, int value4) 
	{	
		int location = fetchUniformLocation(name);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setUniform(String name, float value) 
	{	
		int location = fetchUniformLocation(name);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setUniform(String name, float value1, float value2) 
	{	
		int location = fetchUniformLocation(name);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setUniform(String name, float value1, float value2, float value3) 
	{	
		int location = fetchUniformLocation(name);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setUniform(String name, float value1, float value2, float value3, float value4) 
	{	
		int location = fetchUniformLocation(name);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setUniformMatrix(String name, Matrix matrix) 
	{	
		int location = fetchUniformLocation(name);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setVertexAttribute(String name, int size, int type, boolean normalize, int stride, FloatBuffer buffer) 
	{	
		int location = fetchUniformLocation(name);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setVertexAttribute(String name, int size, int type, boolean normalize, int offset) 
	{	
		int location = fetchUniformLocation(name);
	}

	@Override
	public void begin() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void end() {
		// TODO Auto-generated method stub
		
	}
}
