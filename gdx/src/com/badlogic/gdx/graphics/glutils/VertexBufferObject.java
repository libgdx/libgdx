package com.badlogic.gdx.graphics.glutils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * <p>
 * A {@link VertexData} implementation based on OpenGL vertex buffer objects.
 * </p>
 * 
 * <p>
 * If the OpenGL ES context was lost you can call {@link #invalidate()} to recreate
 * a new OpenGL vertex buffer object. 
 * </p>
 * 
 * <p>
 * In case OpenGL ES 2.0 is used in the application the data is bound via
 * glVertexAttribPointer() according to the attribute aliases specified via
 * {@link VertexAttributes} in the constructor.
 * </p>
 * 
 * <p>
 * Uses indirect Buffers on Android 1.5/1.6 to fix GC invocation due to leaking
 * PlatformAddress instances.
 * </p>
 * 
 * @author mzechner
 * 
 */
public class VertexBufferObject implements VertexData {
	final static IntBuffer tmpHandle = BufferUtils.newIntBuffer(1);
	
	final VertexAttributes attributes;	
	final FloatBuffer buffer;	
	final ByteBuffer byteBuffer;
	int bufferHandle;
	final boolean isDirect;
	final boolean isStatic;
	final int usage;
	boolean isDirty=false;
	
	/**
	 * Constructs a new interleaved VertexBufferObject. 
	 * @param isStatic whether the vertex data is static.
	 * @param numVertices the maximum number of vertices
	 * @param attributes the {@link VertexAttribute}s.
	 */
	public VertexBufferObject (boolean isStatic, int numVertices, VertexAttribute ... attributes) {
		this.isStatic = isStatic;
		this.attributes = new VertexAttributes(attributes);
		if( Gdx.app.getType() == ApplicationType.Android && Gdx.app.getVersion() < 5 ) {
			byteBuffer = ByteBuffer.allocate(this.attributes.vertexSize * numVertices);
			byteBuffer.order(ByteOrder.nativeOrder());
			isDirect = true;
		}
		else {
			byteBuffer = ByteBuffer.allocateDirect(this.attributes.vertexSize * numVertices);
			byteBuffer.order(ByteOrder.nativeOrder());
			isDirect = false;
		}		
		buffer = byteBuffer.asFloatBuffer();
		bufferHandle = createBufferObject();
		usage = isStatic?GL11.GL_STATIC_DRAW:GL11.GL_DYNAMIC_DRAW;
	}
	
	private int createBufferObject () {					
		if(Gdx.gl20!=null)
			Gdx.gl20.glGenBuffers(1, tmpHandle);					
		else 
			Gdx.gl11.glGenBuffers(1, tmpHandle);		
		return tmpHandle.get(0);
	}	

	@Override
	public VertexAttributes getAttributes() {
		return attributes;
	}

	@Override
	public int getNumVertices() {
		return byteBuffer.limit() / attributes.vertexSize;
	}

	public int getNumMaxVertices() {
		return byteBuffer.capacity() / attributes.vertexSize;
	}
	
	@Override
	public FloatBuffer getBuffer() {
		isDirty = true;
		return buffer;
	}

	@Override
	public void setVertices(float[] vertices, int offset, int count) {
		isDirty = true;
		if( isDirect ) {
			BufferUtils.copy(vertices, byteBuffer, count, offset);
			buffer.position(0);
			buffer.limit(count);
		}
		else {
			buffer.clear();
			buffer.put( vertices, offset, count );
			buffer.flip();
			byteBuffer.position(0);
			byteBuffer.limit(buffer.limit() << 2);
		}
	}

	@Override
	public void bind() {
		GL11 gl = Gdx.gl11;
		
		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, bufferHandle);
		if( isDirty ) {
			gl.glBufferData(GL11.GL_ARRAY_BUFFER, byteBuffer.limit(), null, usage);
			gl.glBufferData(GL11.GL_ARRAY_BUFFER, byteBuffer.limit(), byteBuffer, usage);
			isDirty = false;
		}
		
		int textureUnit = 0;
		int numAttributes = attributes.size();

		for (int i = 0; i < numAttributes; i++) {
			VertexAttribute attribute = attributes.get(i);

			switch (attribute.usage) {
			case Usage.Position:				
				gl.glEnableClientState(GL11.GL_VERTEX_ARRAY);
				gl.glVertexPointer(attribute.numComponents, GL10.GL_FLOAT,
						attributes.vertexSize, attribute.offset);
				break;

			case Usage.Color:
			case Usage.ColorPacked:
				int colorType = GL10.GL_FLOAT;
				if (attribute.usage == Usage.ColorPacked)
					colorType = GL11.GL_UNSIGNED_BYTE;
				
				gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
				gl.glColorPointer(attribute.numComponents, colorType,
						attributes.vertexSize, attribute.offset);
				break;

			case Usage.Normal:				
				gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
				gl.glNormalPointer(GL10.GL_FLOAT, attributes.vertexSize,
						attribute.offset);
				break;

			case Usage.TextureCoordinates:
				gl.glClientActiveTexture(GL10.GL_TEXTURE0 + textureUnit);
				gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
				gl.glTexCoordPointer(attribute.numComponents, GL10.GL_FLOAT,
						attributes.vertexSize, attribute.offset);
				textureUnit++;
				break;

			default:
				throw new GdxRuntimeException("unkown vertex attribute type: "
						+ attribute.usage);
			}
		}
	}
	
	public void bind(ShaderProgram shader) {
		GL20 gl = Gdx.gl20;
		
		gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, bufferHandle);
		if( isDirty ) {
			gl.glBufferData(GL20.GL_ARRAY_BUFFER, byteBuffer.limit(), null, usage);
			gl.glBufferData(GL20.GL_ARRAY_BUFFER, byteBuffer.limit(), byteBuffer, usage);
			isDirty = false;
		}
		
		int numAttributes = attributes.size();		
		for( int i = 0; i < numAttributes; i++ ) {
			VertexAttribute attribute = attributes.get(i);
			shader.enableVertexAttribute(attribute.alias);
			int colorType = GL20.GL_FLOAT;
			boolean normalize = false;
			if(attribute.usage == Usage.ColorPacked) {
				colorType = GL20.GL_UNSIGNED_BYTE;
				normalize = true;
			}
			shader.setVertexAttribute(attribute.alias, attribute.numComponents, colorType, normalize, attributes.vertexSize, attribute.offset);
		}
	}	
	
	@Override
	public void unbind() {
		GL11 gl = Gdx.gl11;
		int textureUnit = 0;
		int numAttributes = attributes.size();

		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
		
		for (int i = 0; i < numAttributes; i++) {

			VertexAttribute attribute = attributes.get(i);
			switch (attribute.usage) {
			case Usage.Position:
				break; // no-op, we also need a position bound in gles
			case Usage.Color:
			case Usage.ColorPacked:
				gl.glDisableClientState(GL11.GL_COLOR_ARRAY);
				break;
			case Usage.Normal:
				gl.glDisableClientState(GL11.GL_NORMAL_ARRAY);
				break;
			case Usage.TextureCoordinates:
				gl.glClientActiveTexture(GL11.GL_TEXTURE0 + textureUnit);
				gl.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
				textureUnit++;
				break;
			default:
				throw new GdxRuntimeException("unkown vertex attribute type: "
						+ attribute.usage);
			}
		}
	}
	
	private void unbind(ShaderProgram shader) {
		GL20 gl = Gdx.gl20;
		int numAttributes = attributes.size();
		for( int i = 0; i < numAttributes; i++ ) {
			VertexAttribute attribute = attributes.get(i);
			shader.disableVertexAttribute(attribute.alias);
		}
		gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, 0);
	}
	
	public void invalidate() {
		bufferHandle = createBufferObject();
		isDirty = true;
	}
	
	@Override
	public void dispose() {
		if( Gdx.gl20!=null) {
			tmpHandle.clear();
			tmpHandle.put(bufferHandle);
			GL20 gl = Gdx.gl20;
			gl.glBindBuffer( GL20.GL_ARRAY_BUFFER, 0);			
			gl.glDeleteBuffers(1, tmpHandle);
			bufferHandle = 0;
		}
		else {
			tmpHandle.clear();
			tmpHandle.put(bufferHandle);
			GL11 gl = Gdx.gl11;
			gl.glBindBuffer( GL11.GL_ARRAY_BUFFER, 0);			
			gl.glDeleteBuffers(1, tmpHandle);
			bufferHandle = 0;			
		}
			
	}
}
