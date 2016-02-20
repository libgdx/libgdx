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

package com.badlogic.gdx.graphics.g3d.decals;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.NumberUtils;

/** <p/>
 * Represents a sprite in 3d space. Typical 3d transformations such as translation, rotation and scaling are supported. The
 * position includes a z component other than setting the depth no manual layering has to be performed, correct overlay is
 * guaranteed by using the depth buffer.
 * <p/>
 * Decals are handled by the {@link DecalBatch}. */
public class Decal {
	// 3(x,y,z) + 1(color) + 2(u,v)
	/** Size of a decal vertex in floats */
	private static final int VERTEX_SIZE = 3 + 1 + 2;
	/** Size of the decal in floats. It takes a float[SIZE] to hold the decal. */
	public static final int SIZE = 4 * VERTEX_SIZE;

	/** Temporary vector for various calculations. */
	private static Vector3 tmp = new Vector3();
	private static Vector3 tmp2 = new Vector3();

	/** Set a multipurpose value which can be queried and used for things like group identification. */
	public int value;

	protected float[] vertices = new float[SIZE];
	protected Vector3 position = new Vector3();
	protected Quaternion rotation = new Quaternion();
	protected Vector2 scale = new Vector2(1, 1);
	protected Color color = new Color();

	/** The transformation offset can be used to change the pivot point for rotation and scaling. By default the pivot is the middle
	 * of the decal. */
	public Vector2 transformationOffset = null;
	protected Vector2 dimensions = new Vector2();

	protected DecalMaterial material;
	protected boolean updated = false;

	public Decal () {
		this.material = new DecalMaterial();
	}

	public Decal (DecalMaterial material) {
		this.material = material;
	}

	/** Sets the color of all four vertices to the specified color
	 * 
	 * @param r Red component
	 * @param g Green component
	 * @param b Blue component
	 * @param a Alpha component */
	public void setColor (float r, float g, float b, float a) {
		color.set(r, g, b, a);
		int intBits = ((int)(255 * a) << 24) | ((int)(255 * b) << 16) | ((int)(255 * g) << 8) | ((int)(255 * r));
		float color = NumberUtils.intToFloatColor(intBits);
		vertices[C1] = color;
		vertices[C2] = color;
		vertices[C3] = color;
		vertices[C4] = color;
	}

	/** Sets the color used to tint this decal. Default is {@link Color#WHITE}. */
	public void setColor (Color tint) {
		color.set(tint);
		float color = tint.toFloatBits();
		vertices[C1] = color;
		vertices[C2] = color;
		vertices[C3] = color;
		vertices[C4] = color;
	}

	/** @see #setColor(Color) */
	public void setColor (float color) {
		this.color.set(NumberUtils.floatToIntColor(color));
		vertices[C1] = color;
		vertices[C2] = color;
		vertices[C3] = color;
		vertices[C4] = color;
	}

	/** Sets the rotation on the local X axis to the specified angle
	 * 
	 * @param angle Angle in degrees to set rotation to */
	public void setRotationX (float angle) {
		rotation.set(Vector3.X, angle);
		updated = false;
	}

	/** Sets the rotation on the local Y axis to the specified angle
	 * 
	 * @param angle Angle in degrees to set rotation to */
	public void setRotationY (float angle) {
		rotation.set(Vector3.Y, angle);
		updated = false;
	}

	/** Sets the rotation on the local Z axis to the specified angle
	 * 
	 * @param angle Angle in degrees to set rotation to */
	public void setRotationZ (float angle) {
		rotation.set(Vector3.Z, angle);
		updated = false;
	}

	/** Rotates along local X axis by the specified angle
	 * 
	 * @param angle Angle in degrees to rotate by */
	public void rotateX (float angle) {
		rotator.set(Vector3.X, angle);
		rotation.mul(rotator);
		updated = false;
	}

	/** Rotates along local Y axis by the specified angle
	 * 
	 * @param angle Angle in degrees to rotate by */
	public void rotateY (float angle) {
		rotator.set(Vector3.Y, angle);
		rotation.mul(rotator);
		updated = false;
	}

	/** Rotates along local Z axis by the specified angle
	 * 
	 * @param angle Angle in degrees to rotate by */
	public void rotateZ (float angle) {
		rotator.set(Vector3.Z, angle);
		rotation.mul(rotator);
		updated = false;
	}

	/** Sets the rotation of this decal to the given angles on all axes.
	 * @param yaw Angle in degrees to rotate around the Y axis
	 * @param pitch Angle in degrees to rotate around the X axis
	 * @param roll Angle in degrees to rotate around the Z axis */
	public void setRotation (float yaw, float pitch, float roll) {
		rotation.setEulerAngles(yaw, pitch, roll);
		updated = false;
	}

	/** Sets the rotation of this decal based on the (normalized) direction and up vector.
	 * @param dir the direction vector
	 * @param up the up vector */
	public void setRotation (Vector3 dir, Vector3 up) {
		tmp.set(up).crs(dir).nor();
		tmp2.set(dir).crs(tmp).nor();
		rotation.setFromAxes(tmp.x, tmp2.x, dir.x, tmp.y, tmp2.y, dir.y, tmp.z, tmp2.z, dir.z);
		updated = false;
	}

	/** Sets the rotation of this decal based on the provided Quaternion
	 * @param q desired Rotation */
	public void setRotation (Quaternion q) {
		rotation.set(q);
		updated = false;
	}

	/** Returns the rotation. The returned quaternion should under no circumstances be modified.
	 * 
	 * @return Quaternion representing the rotation */
	public Quaternion getRotation () {
		return rotation;
	}

	/** Moves by the specified amount of units along the x axis
	 * 
	 * @param units Units to move the decal */
	public void translateX (float units) {
		this.position.x += units;
		updated = false;
	}

	/** Sets the position on the x axis
	 * 
	 * @param x Position to locate the decal at */
	public void setX (float x) {
		this.position.x = x;
		updated = false;
	}

	/** @return position on the x axis */
	public float getX () {
		return this.position.x;
	}

	/** Moves by the specified amount of units along the y axis
	 * 
	 * @param units Units to move the decal */
	public void translateY (float units) {
		this.position.y += units;
		updated = false;
	}

	/** Sets the position on the y axis
	 * 
	 * @param y Position to locate the decal at */
	public void setY (float y) {
		this.position.y = y;
		updated = false;
	}

	/** @return position on the y axis */
	public float getY () {
		return this.position.y;
	}

	/** Moves by the specified amount of units along the z axis
	 * 
	 * @param units Units to move the decal */
	public void translateZ (float units) {
		this.position.z += units;
		updated = false;
	}

	/** Sets the position on the z axis
	 * 
	 * @param z Position to locate the decal at */
	public void setZ (float z) {
		this.position.z = z;
		updated = false;
	}

	/** @return position on the z axis */
	public float getZ () {
		return this.position.z;
	}

	/** Translates by the specified amount of units
	 * 
	 * @param x Units to move along the x axis
	 * @param y Units to move along the y axis
	 * @param z Units to move along the z axis */
	public void translate (float x, float y, float z) {
		this.position.add(x, y, z);
		updated = false;
	}

	/** @see Decal#translate(float, float, float) */
	public void translate (Vector3 trans) {
		this.position.add(trans);
		updated = false;
	}

	/** Sets the position to the given world coordinates
	 * 
	 * @param x X position
	 * @param y Y Position
	 * @param z Z Position */
	public void setPosition (float x, float y, float z) {
		this.position.set(x, y, z);
		updated = false;
	}

	/** @see Decal#setPosition(float, float, float) */
	public void setPosition (Vector3 pos) {
		this.position.set(pos);
		updated = false;
	}

	/** Returns the color of this decal. The returned color should under no circumstances be modified.
	 * 
	 * @return The color of this decal. */
	public Color getColor () {
		return color;
	}

	/** Returns the position of this decal. The returned vector should under no circumstances be modified.
	 * 
	 * @return vector representing the position */
	public Vector3 getPosition () {
		return position;
	}

	/** Sets scale along the x axis
	 * 
	 * @param scale New scale along x axis */
	public void setScaleX (float scale) {
		this.scale.x = scale;
		updated = false;
	}

	/** @return Scale on the x axis */
	public float getScaleX () {
		return this.scale.x;
	}

	/** Sets scale along the y axis
	 * 
	 * @param scale New scale along y axis */
	public void setScaleY (float scale) {
		this.scale.y = scale;
		updated = false;
	}

	/** @return Scale on the y axis */
	public float getScaleY () {
		return this.scale.y;
	}

	/** Sets scale along both the x and y axis
	 * 
	 * @param scaleX Scale on the x axis
	 * @param scaleY Scale on the y axis */
	public void setScale (float scaleX, float scaleY) {
		this.scale.set(scaleX, scaleY);
		updated = false;
	}

	/** Sets scale along both the x and y axis
	 * 
	 * @param scale New scale */
	public void setScale (float scale) {
		this.scale.set(scale, scale);
		updated = false;
	}

	/** Sets the width in world units
	 * 
	 * @param width Width in world units */
	public void setWidth (float width) {
		this.dimensions.x = width;
		updated = false;
	}

	/** @return width in world units */
	public float getWidth () {
		return this.dimensions.x;
	}

	/** Sets the height in world units
	 * 
	 * @param height Height in world units */
	public void setHeight (float height) {
		this.dimensions.y = height;
		updated = false;
	}

	/** @return height in world units */
	public float getHeight () {
		return dimensions.y;
	}

	/** Sets the width and height in world units
	 * 
	 * @param width Width in world units
	 * @param height Height in world units */
	public void setDimensions (float width, float height) {
		dimensions.set(width, height);
		updated = false;
	}

	/** Returns the vertices backing this sprite.<br/>
	 * The returned value should under no circumstances be modified.
	 * 
	 * @return vertex array backing the decal */
	public float[] getVertices () {
		return vertices;
	}

	/** Recalculates vertices array if it grew out of sync with the properties (position, ..) */
	protected void update () {
		if (!updated) {
			resetVertices();
			transformVertices();
		}
	}

	/** Transforms the position component of the vertices using properties such as position, scale, etc. */
	protected void transformVertices () {
		/** It would be possible to also load the x,y,z into a Vector3 and apply all the transformations using already existing
		 * methods. Especially the quaternion rotation already exists in the Quaternion class, it then would look like this:
		 * ----------------------------------------------------------------------------------------------------
		 * v3.set(vertices[xIndex] * scale.x, vertices[yIndex] * scale.y, vertices[zIndex]); rotation.transform(v3);
		 * v3.add(position); vertices[xIndex] = v3.x; vertices[yIndex] = v3.y; vertices[zIndex] = v3.z;
		 * ---------------------------------------------------------------------------------------------------- However, a half ass
		 * benchmark with dozens of thousands decals showed that doing it "by hand", as done here, is about 10% faster. So while
		 * duplicate code should be avoided for maintenance reasons etc. the performance gain is worth it. The math doesn't change. */
		float x, y, z, w;
		float tx, ty;
		if (transformationOffset != null) {
			tx = -transformationOffset.x;
			ty = -transformationOffset.y;
		} else {
			tx = ty = 0;
		}
		/** Transform the first vertex */
		// first apply the scale to the vector
		x = (vertices[X1] + tx) * scale.x;
		y = (vertices[Y1] + ty) * scale.y;
		z = vertices[Z1];
		// then transform the vector using the rotation quaternion
		vertices[X1] = rotation.w * x + rotation.y * z - rotation.z * y;
		vertices[Y1] = rotation.w * y + rotation.z * x - rotation.x * z;
		vertices[Z1] = rotation.w * z + rotation.x * y - rotation.y * x;
		w = -rotation.x * x - rotation.y * y - rotation.z * z;
		rotation.conjugate();
		x = vertices[X1];
		y = vertices[Y1];
		z = vertices[Z1];
		vertices[X1] = w * rotation.x + x * rotation.w + y * rotation.z - z * rotation.y;
		vertices[Y1] = w * rotation.y + y * rotation.w + z * rotation.x - x * rotation.z;
		vertices[Z1] = w * rotation.z + z * rotation.w + x * rotation.y - y * rotation.x;
		rotation.conjugate(); // <- don't forget to conjugate the rotation back to normal
		// finally translate the vector according to position
		vertices[X1] += position.x - tx;
		vertices[Y1] += position.y - ty;
		vertices[Z1] += position.z;
		/** Transform the second vertex */
		// first apply the scale to the vector
		x = (vertices[X2] + tx) * scale.x;
		y = (vertices[Y2] + ty) * scale.y;
		z = vertices[Z2];
		// then transform the vector using the rotation quaternion
		vertices[X2] = rotation.w * x + rotation.y * z - rotation.z * y;
		vertices[Y2] = rotation.w * y + rotation.z * x - rotation.x * z;
		vertices[Z2] = rotation.w * z + rotation.x * y - rotation.y * x;
		w = -rotation.x * x - rotation.y * y - rotation.z * z;
		rotation.conjugate();
		x = vertices[X2];
		y = vertices[Y2];
		z = vertices[Z2];
		vertices[X2] = w * rotation.x + x * rotation.w + y * rotation.z - z * rotation.y;
		vertices[Y2] = w * rotation.y + y * rotation.w + z * rotation.x - x * rotation.z;
		vertices[Z2] = w * rotation.z + z * rotation.w + x * rotation.y - y * rotation.x;
		rotation.conjugate(); // <- don't forget to conjugate the rotation back to normal
		// finally translate the vector according to position
		vertices[X2] += position.x - tx;
		vertices[Y2] += position.y - ty;
		vertices[Z2] += position.z;
		/** Transform the third vertex */
		// first apply the scale to the vector
		x = (vertices[X3] + tx) * scale.x;
		y = (vertices[Y3] + ty) * scale.y;
		z = vertices[Z3];
		// then transform the vector using the rotation quaternion
		vertices[X3] = rotation.w * x + rotation.y * z - rotation.z * y;
		vertices[Y3] = rotation.w * y + rotation.z * x - rotation.x * z;
		vertices[Z3] = rotation.w * z + rotation.x * y - rotation.y * x;
		w = -rotation.x * x - rotation.y * y - rotation.z * z;
		rotation.conjugate();
		x = vertices[X3];
		y = vertices[Y3];
		z = vertices[Z3];
		vertices[X3] = w * rotation.x + x * rotation.w + y * rotation.z - z * rotation.y;
		vertices[Y3] = w * rotation.y + y * rotation.w + z * rotation.x - x * rotation.z;
		vertices[Z3] = w * rotation.z + z * rotation.w + x * rotation.y - y * rotation.x;
		rotation.conjugate(); // <- don't forget to conjugate the rotation back to normal
		// finally translate the vector according to position
		vertices[X3] += position.x - tx;
		vertices[Y3] += position.y - ty;
		vertices[Z3] += position.z;
		/** Transform the fourth vertex */
		// first apply the scale to the vector
		x = (vertices[X4] + tx) * scale.x;
		y = (vertices[Y4] + ty) * scale.y;
		z = vertices[Z4];
		// then transform the vector using the rotation quaternion
		vertices[X4] = rotation.w * x + rotation.y * z - rotation.z * y;
		vertices[Y4] = rotation.w * y + rotation.z * x - rotation.x * z;
		vertices[Z4] = rotation.w * z + rotation.x * y - rotation.y * x;
		w = -rotation.x * x - rotation.y * y - rotation.z * z;
		rotation.conjugate();
		x = vertices[X4];
		y = vertices[Y4];
		z = vertices[Z4];
		vertices[X4] = w * rotation.x + x * rotation.w + y * rotation.z - z * rotation.y;
		vertices[Y4] = w * rotation.y + y * rotation.w + z * rotation.x - x * rotation.z;
		vertices[Z4] = w * rotation.z + z * rotation.w + x * rotation.y - y * rotation.x;
		rotation.conjugate(); // <- don't forget to conjugate the rotation back to normal
		// finally translate the vector according to position
		vertices[X4] += position.x - tx;
		vertices[Y4] += position.y - ty;
		vertices[Z4] += position.z;
		updated = true;
	}

	/** Resets the position components of the vertices array based ont he dimensions (preparation for transformation) */
	protected void resetVertices () {
		float left = -dimensions.x / 2f;
		float right = left + dimensions.x;
		float top = dimensions.y / 2f;
		float bottom = top - dimensions.y;

		// left top
		vertices[X1] = left;
		vertices[Y1] = top;
		vertices[Z1] = 0;
		// right top
		vertices[X2] = right;
		vertices[Y2] = top;
		vertices[Z2] = 0;
		// left bot
		vertices[X3] = left;
		vertices[Y3] = bottom;
		vertices[Z3] = 0;
		// right bot
		vertices[X4] = right;
		vertices[Y4] = bottom;
		vertices[Z4] = 0;

		updated = false;
	}

	/** Re-applies the uv coordinates from the material's texture region to the uv components of the vertices array */
	protected void updateUVs () {
		TextureRegion tr = material.textureRegion;
		// left top
		vertices[U1] = tr.getU();
		vertices[V1] = tr.getV();
		// right top
		vertices[U2] = tr.getU2();
		vertices[V2] = tr.getV();
		// left bot
		vertices[U3] = tr.getU();
		vertices[V3] = tr.getV2();
		// right bot
		vertices[U4] = tr.getU2();
		vertices[V4] = tr.getV2();
	}

	/** Sets the texture region
	 * 
	 * @param textureRegion Texture region to apply */
	public void setTextureRegion (TextureRegion textureRegion) {
		this.material.textureRegion = textureRegion;
		updateUVs();
	}

	/** @return the texture region this Decal uses. Do not modify it! */
	public TextureRegion getTextureRegion () {
		return this.material.textureRegion;
	}

	/** Sets the blending parameters for this decal
	 * 
	 * @param srcBlendFactor Source blend factor used by glBlendFunc
	 * @param dstBlendFactor Destination blend factor used by glBlendFunc */
	public void setBlending (int srcBlendFactor, int dstBlendFactor) {
		material.srcBlendFactor = srcBlendFactor;
		material.dstBlendFactor = dstBlendFactor;
	}

	public DecalMaterial getMaterial () {
		return material;
	}

	/**Set material
	 * 
	 * @param material custom material
	 */
	public void setMaterial (DecalMaterial material) {
		this.material = material;
	}

	final static Vector3 dir = new Vector3();

	/** Sets the rotation of the Decal to face the given point. Useful for billboarding.
	 * @param position
	 * @param up */
	public void lookAt (Vector3 position, Vector3 up) {
		dir.set(position).sub(this.position).nor();
		setRotation(dir, up);
	}

	// meaning of the floats in the vertices array
	public static final int X1 = 0;
	public static final int Y1 = 1;
	public static final int Z1 = 2;
	public static final int C1 = 3;
	public static final int U1 = 4;
	public static final int V1 = 5;
	public static final int X2 = 6;
	public static final int Y2 = 7;
	public static final int Z2 = 8;
	public static final int C2 = 9;
	public static final int U2 = 10;
	public static final int V2 = 11;
	public static final int X3 = 12;
	public static final int Y3 = 13;
	public static final int Z3 = 14;
	public static final int C3 = 15;
	public static final int U3 = 16;
	public static final int V3 = 17;
	public static final int X4 = 18;
	public static final int Y4 = 19;
	public static final int Z4 = 20;
	public static final int C4 = 21;
	public static final int U4 = 22;
	public static final int V4 = 23;

	protected static Quaternion rotator = new Quaternion(0, 0, 0, 0);

	/** Creates a decal assuming the dimensions of the texture region
	 * 
	 * @param textureRegion Texture region to use
	 * @return Created decal */
	public static Decal newDecal (TextureRegion textureRegion) {
		return newDecal(textureRegion.getRegionWidth(), textureRegion.getRegionHeight(), textureRegion, DecalMaterial.NO_BLEND,
			DecalMaterial.NO_BLEND);
	}

	/** Creates a decal assuming the dimensions of the texture region and adding transparency
	 * 
	 * @param textureRegion Texture region to use
	 * @param hasTransparency Whether or not this sprite will be treated as having transparency (transparent png, etc.)
	 * @return Created decal */
	public static Decal newDecal (TextureRegion textureRegion, boolean hasTransparency) {
		return newDecal(textureRegion.getRegionWidth(), textureRegion.getRegionHeight(), textureRegion,
			hasTransparency ? GL20.GL_SRC_ALPHA : DecalMaterial.NO_BLEND, hasTransparency ? GL20.GL_ONE_MINUS_SRC_ALPHA
				: DecalMaterial.NO_BLEND);
	}

	/** Creates a decal using the region for texturing
	 * 
	 * @param width Width of the decal in world units
	 * @param height Height of the decal in world units
	 * @param textureRegion TextureRegion to use
	 * @return Created decal */
	// TODO : it would be convenient if {@link com.badlogic.gdx.graphics.Texture} had a getFormat() method to assume transparency
// from RGBA,..
	public static Decal newDecal (float width, float height, TextureRegion textureRegion) {
		return newDecal(width, height, textureRegion, DecalMaterial.NO_BLEND, DecalMaterial.NO_BLEND);
	}

	/** Creates a decal using the region for texturing
	 * 
	 * @param width Width of the decal in world units
	 * @param height Height of the decal in world units
	 * @param textureRegion TextureRegion to use
	 * @param hasTransparency Whether or not this sprite will be treated as having transparency (transparent png, etc.)
	 * @return Created decal */
	public static Decal newDecal (float width, float height, TextureRegion textureRegion, boolean hasTransparency) {
		return newDecal(width, height, textureRegion, hasTransparency ? GL20.GL_SRC_ALPHA : DecalMaterial.NO_BLEND,
			hasTransparency ? GL20.GL_ONE_MINUS_SRC_ALPHA : DecalMaterial.NO_BLEND);
	}

	/** Creates a decal using the region for texturing and the specified blending parameters for blending
	 * 
	 * @param width Width of the decal in world units
	 * @param height Height of the decal in world units
	 * @param textureRegion TextureRegion to use
	 * @param srcBlendFactor Source blend used by glBlendFunc
	 * @param dstBlendFactor Destination blend used by glBlendFunc
	 * @return Created decal */
	public static Decal newDecal (float width, float height, TextureRegion textureRegion, int srcBlendFactor, int dstBlendFactor) {
		Decal decal = new Decal();
		decal.setTextureRegion(textureRegion);
		decal.setBlending(srcBlendFactor, dstBlendFactor);
		decal.dimensions.x = width;
		decal.dimensions.y = height;
		decal.setColor(1, 1, 1, 1);
		return decal;
	}

	/** Creates a decal using the region for texturing and the specified blending parameters for blending
	 * 
	 * @param width Width of the decal in world units
	 * @param height Height of the decal in world units
	 * @param textureRegion TextureRegion to use
	 * @param srcBlendFactor Source blend used by glBlendFunc
	 * @param dstBlendFactor Destination blend used by glBlendFunc
	 * @param material Custom decal material
	 * @return Created decal */
	public static Decal newDecal (float width, float height, TextureRegion textureRegion, int srcBlendFactor, int dstBlendFactor,
		DecalMaterial material) {
		Decal decal = new Decal(material);
		decal.setTextureRegion(textureRegion);
		decal.setBlending(srcBlendFactor, dstBlendFactor);
		decal.dimensions.x = width;
		decal.dimensions.y = height;
		decal.setColor(1, 1, 1, 1);
		return decal;
	}

}
