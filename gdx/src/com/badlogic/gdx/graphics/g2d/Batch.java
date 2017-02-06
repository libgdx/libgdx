/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.graphics.g2d;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;

/** A Batch is used to draw 2D rectangles that reference a texture (region). The class will batch the drawing commands and optimize
 * them for processing by the GPU.
 * <p>
 * To draw something with a Batch one has to first call the {@link Batch#begin()} method which will setup appropriate render
 * states. When you are done with drawing you have to call {@link Batch#end()} which will actually draw the things you specified.
 * <p>
 * All drawing commands of the Batch operate in screen coordinates. The screen coordinate system has an x-axis pointing to the
 * right, an y-axis pointing upwards and the origin is in the lower left corner of the screen. You can also provide your own
 * transformation and projection matrices if you so wish.
 * <p>
 * A Batch is managed. In case the OpenGL context is lost all OpenGL resources a Batch uses internally get invalidated. A context
 * is lost when a user switches to another application or receives an incoming call on Android. A Batch will be automatically
 * reloaded after the OpenGL context is restored.
 * <p>
 * A Batch is a pretty heavy object so you should only ever have one in your program.
 * <p>
 * A Batch works with OpenGL ES 2.0. It will use its own custom shader to draw all provided
 * sprites. You can set your own custom shader via {@link #setShader(ShaderProgram)}.
 * <p>
 * A Batch has to be disposed if it is no longer used.
 * @author mzechner
 * @author Nathan Sweet */
public interface Batch extends Disposable {
	/** Sets up the Batch for drawing. This will disable depth buffer writing. It enables blending and texturing. If you have more
	 * texture units enabled than the first one you have to disable them before calling this. Uses a screen coordinate system by
	 * default where everything is given in pixels. You can specify your own projection and modelview matrices via
	 * {@link #setProjectionMatrix(Matrix4)} and {@link #setTransformMatrix(Matrix4)}. */
	public void begin ();

	/** Finishes off rendering. Enables depth writes, disables blending and texturing. Must always be called after a call to
	 * {@link #begin()} */
	public void end ();

	/** Sets the color used to tint images when they are added to the Batch. Default is {@link Color#WHITE}. */
	public void setColor (Color tint);

	/** @see #setColor(Color) */
	public void setColor (float r, float g, float b, float a);

	/** @see #setColor(Color)
	 * @see Color#toFloatBits() */
	public void setColor (float color);

	/** @return the rendering color of this Batch. Manipulating the returned instance has no effect. */
	public Color getColor ();

	/** @return the rendering color of this Batch in vertex format
	 * @see Color#toFloatBits() */
	public float getPackedColor ();

	/** Draws a rectangle with the bottom left corner at x,y having the given width and height in pixels. The rectangle is offset by
	 * originX, originY relative to the origin. Scale specifies the scaling factor by which the rectangle should be scaled around
	 * originX, originY. Rotation specifies the angle of counter clockwise rotation of the rectangle around originX, originY. The
	 * portion of the {@link Texture} given by srcX, srcY and srcWidth, srcHeight is used. These coordinates and sizes are given in
	 * texels. FlipX and flipY specify whether the texture portion should be flipped horizontally or vertically.
	 * @param x the x-coordinate in screen space
	 * @param y the y-coordinate in screen space
	 * @param originX the x-coordinate of the scaling and rotation origin relative to the screen space coordinates
	 * @param originY the y-coordinate of the scaling and rotation origin relative to the screen space coordinates
	 * @param width the width in pixels
	 * @param height the height in pixels
	 * @param scaleX the scale of the rectangle around originX/originY in x
	 * @param scaleY the scale of the rectangle around originX/originY in y
	 * @param rotation the angle of counter clockwise rotation of the rectangle around originX/originY
	 * @param srcX the x-coordinate in texel space
	 * @param srcY the y-coordinate in texel space
	 * @param srcWidth the source with in texels
	 * @param srcHeight the source height in texels
	 * @param flipX whether to flip the sprite horizontally
	 * @param flipY whether to flip the sprite vertically */
	public void draw (Texture texture, float x, float y, float originX, float originY, float width, float height, float scaleX,
		float scaleY, float rotation, int srcX, int srcY, int srcWidth, int srcHeight, boolean flipX, boolean flipY);

	/** Draws a rectangle with the bottom left corner at x,y having the given width and height in pixels. The portion of the
	 * {@link Texture} given by srcX, srcY and srcWidth, srcHeight is used. These coordinates and sizes are given in texels. FlipX
	 * and flipY specify whether the texture portion should be flipped horizontally or vertically.
	 * @param x the x-coordinate in screen space
	 * @param y the y-coordinate in screen space
	 * @param width the width in pixels
	 * @param height the height in pixels
	 * @param srcX the x-coordinate in texel space
	 * @param srcY the y-coordinate in texel space
	 * @param srcWidth the source with in texels
	 * @param srcHeight the source height in texels
	 * @param flipX whether to flip the sprite horizontally
	 * @param flipY whether to flip the sprite vertically */
	public void draw (Texture texture, float x, float y, float width, float height, int srcX, int srcY, int srcWidth,
		int srcHeight, boolean flipX, boolean flipY);

	/** Draws a rectangle with the bottom left corner at x,y having the given width and height in pixels. The portion of the
	 * {@link Texture} given by srcX, srcY and srcWidth, srcHeight are used. These coordinates and sizes are given in texels.
	 * @param x the x-coordinate in screen space
	 * @param y the y-coordinate in screen space
	 * @param srcX the x-coordinate in texel space
	 * @param srcY the y-coordinate in texel space
	 * @param srcWidth the source with in texels
	 * @param srcHeight the source height in texels */
	public void draw (Texture texture, float x, float y, int srcX, int srcY, int srcWidth, int srcHeight);

	/** Draws a rectangle with the bottom left corner at x,y having the given width and height in pixels. The portion of the
	 * {@link Texture} given by u, v and u2, v2 are used. These coordinates and sizes are given in texture size percentage. The
	 * rectangle will have the given tint {@link Color}.
	 * @param x the x-coordinate in screen space
	 * @param y the y-coordinate in screen space
	 * @param width the width in pixels
	 * @param height the height in pixels */
	public void draw (Texture texture, float x, float y, float width, float height, float u, float v, float u2, float v2);

	/** Draws a rectangle with the bottom left corner at x,y having the width and height of the texture.
	 * @param x the x-coordinate in screen space
	 * @param y the y-coordinate in screen space */
	public void draw (Texture texture, float x, float y);

	/** Draws a rectangle with the bottom left corner at x,y and stretching the region to cover the given width and height. */
	public void draw (Texture texture, float x, float y, float width, float height);

	/** Draws a rectangle using the given vertices. There must be 4 vertices, each made up of 5 elements in this order: x, y, color,
	 * u, v. The {@link #getColor()} from the Batch is not applied. */
	public void draw (Texture texture, float[] spriteVertices, int offset, int count);

	/** Draws a rectangle with the bottom left corner at x,y having the width and height of the region. */
	public void draw (TextureRegion region, float x, float y);

	/** Draws a rectangle with the bottom left corner at x,y and stretching the region to cover the given width and height. */
	public void draw (TextureRegion region, float x, float y, float width, float height);

	/** Draws a rectangle with the bottom left corner at x,y and stretching the region to cover the given width and height. The
	 * rectangle is offset by originX, originY relative to the origin. Scale specifies the scaling factor by which the rectangle
	 * should be scaled around originX, originY. Rotation specifies the angle of counter clockwise rotation of the rectangle around
	 * originX, originY. */
	public void draw (TextureRegion region, float x, float y, float originX, float originY, float width, float height,
		float scaleX, float scaleY, float rotation);

	/** Draws a rectangle with the texture coordinates rotated 90 degrees. The bottom left corner at x,y and stretching the region
	 * to cover the given width and height. The rectangle is offset by originX, originY relative to the origin. Scale specifies the
	 * scaling factor by which the rectangle should be scaled around originX, originY. Rotation specifies the angle of counter
	 * clockwise rotation of the rectangle around originX, originY.
	 * @param clockwise If true, the texture coordinates are rotated 90 degrees clockwise. If false, they are rotated 90 degrees
	 *           counter clockwise. */
	public void draw (TextureRegion region, float x, float y, float originX, float originY, float width, float height,
		float scaleX, float scaleY, float rotation, boolean clockwise);

	/** Draws a rectangle transformed by the given matrix. */
	public void draw (TextureRegion region, float width, float height, Affine2 transform);

	/** Causes any pending sprites to be rendered, without ending the Batch. */
	public void flush ();

	/** Disables blending for drawing sprites. Calling this within {@link #begin()}/{@link #end()} will flush the batch. */
	public void disableBlending ();

	/** Enables blending for drawing sprites. Calling this within {@link #begin()}/{@link #end()} will flush the batch. */
	public void enableBlending ();

	/** Sets the blending function to be used when rendering sprites.
	 * @param srcFunc the source function, e.g. GL20.GL_SRC_ALPHA. If set to -1, Batch won't change the blending function.
	 * @param dstFunc the destination function, e.g. GL20.GL_ONE_MINUS_SRC_ALPHA */
	public void setBlendFunction (int srcFunc, int dstFunc);

	public int getBlendSrcFunc ();

	public int getBlendDstFunc ();

	/** Returns the current projection matrix. Changing this within {@link #begin()}/{@link #end()} results in undefined behaviour. */
	public Matrix4 getProjectionMatrix ();

	/** Returns the current transform matrix. Changing this within {@link #begin()}/{@link #end()} results in undefined behaviour. */
	public Matrix4 getTransformMatrix ();

	/** Sets the projection matrix to be used by this Batch. If this is called inside a {@link #begin()}/{@link #end()} block, the
	 * current batch is flushed to the gpu. */
	public void setProjectionMatrix (Matrix4 projection);

	/** Sets the transform matrix to be used by this Batch. */
	public void setTransformMatrix (Matrix4 transform);

	/** Sets the shader to be used in a GLES 2.0 environment. Vertex position attribute is called "a_position", the texture
	 * coordinates attribute is called "a_texCoord0", the color attribute is called "a_color". See
	 * {@link ShaderProgram#POSITION_ATTRIBUTE}, {@link ShaderProgram#COLOR_ATTRIBUTE} and {@link ShaderProgram#TEXCOORD_ATTRIBUTE}
	 * which gets "0" appended to indicate the use of the first texture unit. The combined transform and projection matrx is
	 * uploaded via a mat4 uniform called "u_projTrans". The texture sampler is passed via a uniform called "u_texture".
	 * <p>
	 * Call this method with a null argument to use the default shader.
	 * <p>
	 * This method will flush the batch before setting the new shader, you can call it in between {@link #begin()} and
	 * {@link #end()}.
	 * @param shader the {@link ShaderProgram} or null to use the default shader. */
	public void setShader (ShaderProgram shader);

	/** @return the current {@link ShaderProgram} set by {@link #setShader(ShaderProgram)} or the defaultShader */
	public ShaderProgram getShader ();

	/** @return true if blending for sprites is enabled */
	public boolean isBlendingEnabled ();

	/** @return true if currently between begin and end. */
	public boolean isDrawing ();

	static public final int X1 = 0;
	static public final int Y1 = 1;
	static public final int C1 = 2;
	static public final int U1 = 3;
	static public final int V1 = 4;
	static public final int X2 = 5;
	static public final int Y2 = 6;
	static public final int C2 = 7;
	static public final int U2 = 8;
	static public final int V2 = 9;
	static public final int X3 = 10;
	static public final int Y3 = 11;
	static public final int C3 = 12;
	static public final int U3 = 13;
	static public final int V3 = 14;
	static public final int X4 = 15;
	static public final int Y4 = 16;
	static public final int C4 = 17;
	static public final int U4 = 18;
	static public final int V4 = 19;
}
