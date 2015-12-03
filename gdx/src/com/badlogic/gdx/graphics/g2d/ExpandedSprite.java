/*******************************************************************************
 * Copyright 2015 See AUTHORS file.
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
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.NumberUtils;
import com.badlogic.gdx.utils.ObjectIntMap;

/** A {@linkplain Sprite} that supports extra vertex attributes beyond the standard ones (position, color, and texture
 * coordinates). The sprite can be drawn with a SpriteBatch that was instantiated with the same array of VertexAttributes defined
 * in this sprite's assigned {@linkplain Template}. See {@link SpriteBatch#SpriteBatch(int, ShaderProgram, VertexAttribute...)}.
 * <p>
 * Attempting to draw an ExpandedSprite with a SpriteBatch whose extra attributes don't match will cause unexpected rendering.
 * 
 * @author Darren Keese */
public class ExpandedSprite extends Sprite {

	private int[] extraAttributeIndexMapping; // the data index of the first component of the first vertex for each attribute
	private ObjectIntMap<String> extraAliasIndexMapping; // same as above
	private int vertexSize;
	private int spriteSize;
	private float[] expandedVertices;

	/** Creates an uninitialized sprite. The sprite will need a texture region and bounds set, and a {@linkplain Template} applied,
	 * before it can be drawn. */
	public ExpandedSprite () {
		super();
	}

	/** Creates a sprite with width, height, and texture region equal to the size of the texture.
	 * @param template The {@linkplain Template} defines the extra attributes that this sprite will support. To avoid redundancies,
	 *           the same template instance should be used for every sprite that uses the same extra VertexAttributes. The
	 *           VertexAttribute array should never be modified. */
	public ExpandedSprite (Template template, Texture texture) {
		this(template, texture, 0, 0, texture.getWidth(), texture.getHeight());
	}

	/** Creates a sprite with width, height, and texture region equal to the specified size. The texture region's upper left corner
	 * will be 0,0.
	 * @param template The {@linkplain Template} defines the extra attributes that this sprite will support. To avoid redundancies,
	 *           the same template instance should be used for every sprite that uses the same extra VertexAttributes. The
	 *           VertexAttribute array should never be modified.
	 * @param srcWidth The width of the texture region. May be negative to flip the sprite when drawn.
	 * @param srcHeight The height of the texture region. May be negative to flip the sprite when drawn. */
	public ExpandedSprite (Template template, Texture texture, int srcWidth, int srcHeight) {
		this(template, texture, 0, 0, srcWidth, srcHeight);
	}

	/** Creates a sprite with width, height, and texture region equal to the specified size.
	 * @param template The {@linkplain Template} defines the extra attributes that this sprite will support. To avoid redundancies,
	 *           the same template instance should be used for every sprite that uses the same extra VertexAttributes. The
	 *           VertexAttribute array should never be modified.
	 * @param srcWidth The width of the texture region. May be negative to flip the sprite when drawn.
	 * @param srcHeight The height of the texture region. May be negative to flip the sprite when drawn. */
	public ExpandedSprite (Template template, Texture texture, int srcX, int srcY, int srcWidth, int srcHeight) {
		super(texture, srcX, srcY, srcWidth, srcHeight);
		applyTemplate(template);
	}

	/** Sets the sprite to a specific TextureRegion, the new sprite's region is a copy of the parameter region - altering one does
	 * not affect the other.
	 * @param template The {@linkplain Template} defines the extra attributes that this sprite will support. To avoid redundancies,
	 *           the same template instance should be used for every sprite that uses the same extra VertexAttributes. The
	 *           VertexAttribute array should never be modified. */
	public ExpandedSprite (Template template, TextureRegion region) {
		super(region);
		applyTemplate(template);
	}

	/** Creates a sprite with width, height, and texture region equal to the specified size, relative to specified sprite's texture
	 * region.
	 * @param template The {@linkplain Template} defines the extra attributes that this sprite will support. To avoid redundancies,
	 *           the same template instance should be used for every sprite that uses the same extra VertexAttributes. The
	 *           VertexAttribute array should never be modified.
	 * @param srcWidth The width of the texture region. May be negative to flip the sprite when drawn.
	 * @param srcHeight The height of the texture region. May be negative to flip the sprite when drawn. */
	public ExpandedSprite (Template template, TextureRegion region, int srcX, int srcY, int srcWidth, int srcHeight) {
		super(region, srcX, srcY, srcWidth, srcHeight);
		applyTemplate(template);
	}

	/** Creates a sprite that is a copy in every way of the specified sprite. */
	public ExpandedSprite (ExpandedSprite sprite) {
		set(sprite);
	}

	/** Make this sprite a copy in every way of the specified sprite */
	public void set (ExpandedSprite sprite) {
		super.set(sprite);
		vertexSize = sprite.vertexSize;
		spriteSize = sprite.spriteSize;
		if (expandedVertices == null || expandedVertices.length != vertexSize) expandedVertices = new float[spriteSize];
		System.arraycopy(sprite.expandedVertices, 0, expandedVertices, 0, spriteSize);
		extraAttributeIndexMapping = sprite.extraAttributeIndexMapping;
		extraAliasIndexMapping = sprite.extraAliasIndexMapping;
	}

	/** Apply a template that defines the types of extra attributes this sprite supports. Note that this method may generate
	 * garbage if the sprite already has had a template applied.
	 * @param template The {@linkplain Template} defines the extra attributes that this sprite will support. To avoid redundancies,
	 *           the same template instance should be used to for every sprite that uses the same extra VertexAttributes. The
	 *           VertexAttribute array should never be modified. */
	public void applyTemplate (Template template) {
		vertexSize = template.vertexSize;
		spriteSize = vertexSize * 4;
		if (expandedVertices == null || expandedVertices.length != vertexSize)
			expandedVertices = new float[spriteSize];
		else
			for (int i = 0; i < expandedVertices.length; i++)
				expandedVertices[i] = 0; // clear old data to avoid surprises
		extraAttributeIndexMapping = template.extraAttributeIndexMapping;
		extraAliasIndexMapping = template.extraAliasIndexMapping;
	}

	/** Returns the packed vertices, colors, texture coordinates, and additional vertex attributes for this sprite. The returned
	 * vertex positions, colors, and texture coordinates are overwritten on each call to this method. */
	@Override
	public float[] getVertices () {
		// The standard vertices of the superclass must be collated into the larger, complete data array. This is a
		// sub-optimal strategy, but preserves the performance of the superclass.
		float[] basicVertices = super.getVertices();
		float[] expandedVertices = this.expandedVertices;
		int totalVertexSize = this.vertexSize;
		for (int i = 0; i < 4; i++) {
			System.arraycopy(basicVertices, i * Sprite.VERTEX_SIZE, expandedVertices, i * totalVertexSize, Sprite.VERTEX_SIZE);
		}
		return expandedVertices;
	}

	/** Set the value of a vertex attribute on all four vertices.
	 * @param value The value to apply.
	 * @param extraAttributeIndex The index of the extra VertexAttribute, as was defined in the {@linkplain Template} applied to
	 *           this sprite.
	 * @param component The index of the component of the vertex attribute to be set. It is in the range of [0,
	 *           {@linkplain VertexAttribute#numComponents}), unless it is a
	 *           {@linkplain com.badlogic.gdx.graphics.VertexAttributes.Usage#ColorPacked ColorPacked}, in which case the component
	 *           should always be 0. */
	public void setExtraAttributeValue (float value, int extraAttributeIndex, int component) {
		int firstIndex = extraAttributeIndexMapping[extraAttributeIndex];
		expandedVertices[firstIndex + component] = value;
		firstIndex += vertexSize;
		expandedVertices[firstIndex + component] = value;
		firstIndex += vertexSize;
		expandedVertices[firstIndex + component] = value;
		firstIndex += vertexSize;
		expandedVertices[firstIndex + component] = value;
	}

	/** Set the value of a vertex attribute on all four vertices.
	 * @param value The value to apply.
	 * @param attributeAlias The alias of the attribute, as used in a {@link ShaderProgram}. Must match one of the vertex
	 *           attributes of the {@linkplain Template} applied to this sprite.
	 * @param component The index of the component of the vertex attribute to be set. It is in the range of [0,
	 *           {@linkplain VertexAttribute#numComponents}), unless it is a
	 *           {@linkplain com.badlogic.gdx.graphics.VertexAttributes.Usage#ColorPacked ColorPacked}, in which case the component
	 *           should always be 0. */
	public void setExtraAttributeValue (float value, String attributeAlias, int component) {
		int firstIndex = extraAliasIndexMapping.get(attributeAlias, 0);
		expandedVertices[firstIndex + component] = value;
		firstIndex += vertexSize;
		expandedVertices[firstIndex + component] = value;
		firstIndex += vertexSize;
		expandedVertices[firstIndex + component] = value;
		firstIndex += vertexSize;
		expandedVertices[firstIndex + component] = value;
	}

	/** Set the value of a vertex attribute on all four vertices.
	 * @param value The value to apply to the first or only component of the vertex attribute.
	 * @param extraAttributeIndex The index of the extra VertexAttribute, as was defined in the {@linkplain Template} applied to
	 *           this sprite. */
	public void setExtraAttributeSoleValue (float value, int extraAttributeIndex) {
		int firstIndex = extraAttributeIndexMapping[extraAttributeIndex];
		expandedVertices[firstIndex] = value;
		firstIndex += vertexSize;
		expandedVertices[firstIndex] = value;
		firstIndex += vertexSize;
		expandedVertices[firstIndex] = value;
		firstIndex += vertexSize;
		expandedVertices[firstIndex] = value;
	}

	/** Set the value of a vertex attribute on all four vertices.
	 * @param value The value to apply to the first or only component of the vertex attribute.
	 * @param attributeAlias The alias of the attribute, as used in a {@link ShaderProgram}. Must match one of the vertex
	 *           attributes of the {@linkplain Template} applied to this sprite. */
	public void setExtraAttributeSoleValue (float value, String attributeAlias) {
		int firstIndex = extraAliasIndexMapping.get(attributeAlias, 0);
		expandedVertices[firstIndex] = value;
		firstIndex += vertexSize;
		expandedVertices[firstIndex] = value;
		firstIndex += vertexSize;
		expandedVertices[firstIndex] = value;
		firstIndex += vertexSize;
		expandedVertices[firstIndex] = value;
	}

	/** Set the value of a vertex attribute on a specific vertex.
	 * @param value The value to apply.
	 * @param extraAttributeIndex The index of the extra VertexAttribute, as was defined in the {@linkplain Template} applied to
	 *           this sprite, or the vertex data will be silently corrupted.
	 * @param component The index of the component of the vertex attribute to be set. It is in the range of [0,
	 *           {@linkplain VertexAttribute#numComponents}), unless it is a
	 *           {@linkplain com.badlogic.gdx.graphics.VertexAttributes.Usage#ColorPacked ColorPacked}, in which case the component
	 *           should always be 0.
	 * @param vertex The vertex number. One of 0 to 3 inclusive, starting in the top left, going counter-clockwise. */
	public void setExtraAttributeValue (float value, int extraAttributeIndex, int component, int vertex) {
		expandedVertices[extraAttributeIndexMapping[extraAttributeIndex] + vertex * vertexSize + component] = value;
	}

	/** Set the value of a vertex attribute on a specific vertex.
	 * @param value The value to apply.
	 * @param attributeAlias The alias of the attribute, as used in a {@link ShaderProgram}. Must match one of the vertex
	 *           attributes of the {@linkplain Template} applied to this sprite, or the vertex data will be silently corrupted.
	 * @param component The index of the component of the vertex attribute to be set. It is in the range of [0,
	 *           {@linkplain VertexAttribute#numComponents}), unless it is a
	 *           {@linkplain com.badlogic.gdx.graphics.VertexAttributes.Usage#ColorPacked ColorPacked}, in which case the component
	 *           should always be 0.
	 * @param vertex The vertex number. One of 0 to 3 inclusive, starting in the top left, going counter-clockwise. */
	public void setExtraAttributeValue (float value, String attributeAlias, int component, int vertex) {
		expandedVertices[extraAliasIndexMapping.get(attributeAlias, 0) + vertex * vertexSize + component] = value;
	}

	/** Set the value of a vertex attribute on a specific vertex.
	 * @param value The value to apply to the first or only component of the vertex attribute.
	 * @param extraAttributeIndex The index of the extra VertexAttribute, as was defined in the {@linkplain Template} applied to
	 *           this sprite, or the vertex data will be silently corrupted.
	 * @param vertex The vertex number. One of 0 to 3 inclusive, starting in the top left, going counter-clockwise. */
	public void setExtraAttributeSoleValue (float value, int extraAttributeIndex, int vertex) {
		expandedVertices[extraAttributeIndexMapping[extraAttributeIndex] + vertex * vertexSize] = value;
	}

	/** Set the value of a vertex attribute on a specific vertex.
	 * @param value The value to apply to the first or only component of the vertex attribute.
	 * @param attributeAlias The alias of the attribute, as used in a {@link ShaderProgram}. Must match one of the vertex
	 *           attributes of the {@linkplain Template} applied to this sprite, or the vertex data will be silently corrupted.
	 * @param vertex The vertex number. One of 0 to 3 inclusive, starting in the top left, going counter-clockwise. */
	public void setExtraAttributeSoleValue (float value, String attributeAlias, int vertex) {
		expandedVertices[extraAliasIndexMapping.get(attributeAlias, 0) + vertex * vertexSize] = value;
	}

	@Override
	public void draw (Batch batch) {
		batch.draw(texture, getVertices(), 0, spriteSize);
	}

	/** Describes a set of extra attributes for use by an {@linkplain ExpandedSprite}. */
	public static final class Template {
		final VertexAttribute[] extraAttributes;
		final int[] extraAttributeIndexMapping;
		final ObjectIntMap<String> extraAliasIndexMapping;
		final int vertexSize;

		public Template (VertexAttribute... extraAttributes) {
			this.extraAttributes = extraAttributes;
			vertexSize = Sprite.VERTEX_SIZE + VertexAttribute.calculateSize(extraAttributes);
			extraAttributeIndexMapping = new int[extraAttributes.length];
			extraAliasIndexMapping = new ObjectIntMap<String>(extraAttributes.length);
			int idx = Sprite.VERTEX_SIZE;
			for (int i = 0; i < extraAttributeIndexMapping.length; i++) {
				VertexAttribute attribute = extraAttributes[i];
				extraAttributeIndexMapping[i] = idx;
				extraAliasIndexMapping.put(attribute.alias, idx);
				idx += attribute.usage == Usage.ColorPacked ? 1 : attribute.numComponents;
			}
		}

		/** @return The array of extra vertex attributes this Template represents. The array should not be modified. */
		public VertexAttribute[] getExtraAttributes () {
			return extraAttributes;
		}
	}
}
