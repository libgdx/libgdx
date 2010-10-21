package com.badlogic.gdx.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.MathUtils;

/**
 * temporary sprite class. do not use yet...
 * @author mzechner
 *
 */
public final class Sprite2 
{	
	protected final float vertices[] = new float[20];
	private boolean dirty = false;
			
	public float x, y, width, height;
	public float scaleX, scaleY;
	public float rotation;
	public float originX, originY;
	public float srcX, srcY, srcWidth, srcHeight;
	public float r, g, b, a;
	public Texture texture;
	
	public Sprite2( Texture texture )
	{
		this( texture, 0, 0, texture.getWidth(), texture.getHeight() );
	}
	
	public Sprite2( Texture texture, int srcX, int srcY, int srcWidth, int srcHeight )
	{
		this.texture = texture;
		setTextureRegion( srcX, srcY, srcWidth, srcHeight );		
		setColor( 1, 1, 1, 1 );
		
		width = srcWidth;
		height = srcHeight;
		
		originX = width / 2;
		originY = height / 2;
		
		scaleX = 1;
		scaleY = 1;
		
		rotation = 0;		
		
		dirty = true;		
	}
	
	public void setBounds( float x, float y, float width, float height )
	{
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		
		dirty = true;		
	}
	
	public void setScale( float scale )
	{
		this.scaleX = this.scaleY = scale;
		dirty =true;
	}
	
	public void setScale( float scaleX, float scaleY )
	{
		this.scaleX = scaleX;
		this.scaleY = scaleY;
		dirty = true;
	}
	
	public void setRotation( float rotation )
	{
		this.rotation = rotation;
		dirty = true;
	}
	
	public void setOrigin( float originX, float originY )
	{
		this.originX = originX;
		this.originY = originY;
		dirty = true;
	}	
	
	public void setTextureRegion( int srcX, int srcY, int srcWidth, int srcHeight )
	{	
		this.srcX = srcX;
		this.srcY = srcY;
		this.srcWidth = srcWidth;
		this.srcHeight = srcHeight;
		
		final float invTexWidth = 1.0f / texture.getWidth();
		final float invTexHeight = 1.0f / texture.getHeight();
		final float u = srcX * invTexWidth;
		final float v = (srcY + srcHeight) * invTexHeight;
		final float u2 = (srcX + srcWidth) * invTexWidth;
		final float v2 = srcY * invTexHeight;

		vertices[U1] = u;
		vertices[V1] = v;

		vertices[U2] = u;
		vertices[V2] = v2;

		vertices[U3] = u2;
		vertices[V3] = v2;

		vertices[U4] = u2;
		vertices[V4] = v;

	}	
	
	public void setTextureWrap (boolean x, boolean y) {
		texture.bind();
		GL10 gl = Gdx.graphics.getGL10();
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, x ? GL10.GL_REPEAT : GL10.GL_CLAMP_TO_EDGE);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, y ? GL10.GL_REPEAT : GL10.GL_CLAMP_TO_EDGE);
	}
	
	public void flip (boolean x, boolean y) 
	{
		if (x) 
		{
			float u = vertices[U1];
			float u2 = vertices[U3];
			vertices[U1] = u2;
			vertices[U2] = u2;
			vertices[U3] = u;
			vertices[U4] = u;
		}
		if (y) 
		{
			float v = vertices[V1];
			float v2 = vertices[V3];
			vertices[V1] = v2;
			vertices[V2] = v;
			vertices[V3] = v;
			vertices[V4] = v2;
		}
	}
	
	public void scrollTexture (float xAmount, float yAmount) 
	{
		if (xAmount > 0) 
		{
			float u = (vertices[2] + xAmount) % 1;
			float u2 = u + width / texture.getWidth();
			vertices[U1] = u;
			vertices[U2] = u;
			vertices[U3] = u2;
			vertices[U4] = u2;
		}
		if (yAmount > 0) 
		{
			float v = (vertices[V1] + yAmount) % 1;
			float v2 = v + height / texture.getHeight();
			vertices[V1] = v;
			vertices[V2] = v2;
			vertices[V3] = v2;
			vertices[V4] = v;
		}
	}
	
	public void setColor( Color color )
	{
		this.r = color.r;
		this.g = color.g;
		this.b = color.b;
		this.a = color.a;
		float c = color.toFloatBits();
		vertices[C1] = c;
		vertices[C2] = c;
		vertices[C3] = c;
		vertices[C4] = c;
	}
	
	public void setColor( float r, float g, float b, float a )
	{
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
		float c = Color.toFloatBits(r, g, b, a);
		vertices[C1] = c;
		vertices[C2] = c;
		vertices[C3] = c;
		vertices[C4] = c;
	}	
	
	/**
	 * Shifts the screen coordinates where the sprite will be drawn. Preserves the origin.
	 */
	public void translate (float xAmount, float yAmount) 
	{
		x += xAmount;
		y += yAmount;
		dirty = true;
	}
	
	public void rotate (float degrees) 
	{
		rotation += degrees;
		dirty = true;
	}
	
	public void scale (float scale)
	{
		scaleX += scale;
		scaleY += scale;
	}
	
	private static final native void computeVerticesJNI( float[] vertices, int offset,
														 float x, float y, float width, float height,														 
														 float originX, float originY,
														 float scaleX, float scaleY,
														 float rotation );
	
	protected final void computeVertices( final float[] out, final int offset )
	{			
		if( dirty )
		{		
			
			computeVerticesJNI( vertices, offset, x, y, width, height, originX, originY, scaleX, scaleY, rotation );
//			final float x = this.x;
//			final float y = this.y;
//			final float originX = this.originX;
//			final float originY = this.originY;
//			final float width = this.width;
//			final float height = this.height;
//			final float scaleX = this.scaleX;
//			final float scaleY = this.scaleY;
//			final float rotation = this.rotation;
//			final float[] vertices = this.vertices;
//			
//			// bottom left and top right corner points relative to origin
//			final float worldOriginX = x + originX;
//			final float worldOriginY = y + originY;
//			float fx = -originX;
//			float fy = -originY;
//			float fx2 = width - originX;
//			float fy2 = height - originY;
//			
//			// scale
//			if( scaleX != 1 || scaleY != 1 )
//			{
//				fx *= scaleX;
//				fy *= scaleY;
//				fx2 *= scaleX;
//				fy2 *= scaleY;
//			}
//			
//			// construct corner points, start from top left and go counter clockwise
//			final float p1x = fx;
//			final float p1y = fy;
//			final float p2x = fx;
//			final float p2y = fy2;
//			final float p3x = fx2;
//			final float p3y = fy2;
//			final float p4x = fx2;
//			final float p4y = fy;
//			
//			float x1;
//			float y1;
//			float x2;
//			float y2;
//			float x3;
//			float y3;
//			float x4;
//			float y4;
//			
//			
//			// rotate
//			if( rotation != 0 )
//			{
//				final float cos = MathUtils.cosDeg( rotation );
//				final float sin = MathUtils.sinDeg( rotation );						
//				
//				x1 = cos * p1x - sin * p1y;
//				y1 = sin * p1x + cos * p1y;
//				
//				x2 = cos * p2x - sin * p2y;
//				y2 = sin * p2x + cos * p2y;
//				
//				x3 = cos * p3x - sin * p3y;
//				y3 = sin * p3x + cos * p3y;
//				
//				x4 = cos * p4x - sin * p4y;
//				y4 = sin * p4x + cos * p4y;			
//			}
//			else
//			{
//				x1 = p1x;
//				y1 = p1y;
//				
//				x2 = p2x;
//				y2 = p2y;
//				
//				x3 = p3x;
//				y3 = p3y;
//				
//				x4 = p4x;
//				y4 = p4y;
//			}			
//			
//			x1 += worldOriginX; y1 += worldOriginY;
//			x2 += worldOriginX; y2 += worldOriginY;
//			x3 += worldOriginX; y3 += worldOriginY;
//			x4 += worldOriginX; y4 += worldOriginY;								
//			
//			vertices[X1] = x1;
//			vertices[Y1] = y1;		
//			
//			vertices[X2] = x2;
//			vertices[Y2] = y2;		
//			
//			vertices[X3] = x3;
//			vertices[Y3] = y3;		
//			
//			vertices[X4] = x4;
//			vertices[Y4] = y4;		
			
			dirty = false;
		}
		
		System.arraycopy(vertices, 0, out, offset, 20);		
	}	
	
	static private final int X1 = 0;
	static private final int Y1 = 1;
	static private final int C1 = 2;
	static private final int U1 = 3;
	static private final int V1 = 4;
	static private final int X2 = 5;
	static private final int Y2 = 6;
	static private final int C2 = 7;
	static private final int U2 = 8;
	static private final int V2 = 9;
	static private final int X3 = 10;
	static private final int Y3 = 11;
	static private final int C3 = 12;
	static private final int U3 = 13;
	static private final int V3 = 14;
	static private final int X4 = 15;
	static private final int Y4 = 16;
	static private final int C4 = 17;
	static private final int U4 = 18;
	static private final int V4 = 19;
}
