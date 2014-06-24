/*******************************************************************************
* Copyright 2011 See AUTHORS file.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
******************************************************************************/

package com.badlogic.gdx.scenes.scene2d.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.utils.Disposable;

/**
 * Drawable that will draw in a single plain color.
 * 
 * @author Noel De Martin
 */
public class ColorDrawable extends BaseDrawable implements Disposable {

	public Color color;
	private ShapeRenderer renderer;

	/**
	 * Create a ColorDrawable, with a default white color.
	 */
	public ColorDrawable () {
		this(Color.WHITE);
	}

	/**
	 * Create a ColorDrawable with the given color.
	 * 
	 * @param color
	 */
	public ColorDrawable (Color color) {
		if (color == null) {
			throw new NullPointerException("null color passed in ColorDrawable constructor");
		}
		this.color = color;
		this.renderer = new ShapeRenderer();
	}

	@Override
	public void draw (Batch batch, float x, float y, float width, float height) {
		batch.end();

		renderer.setProjectionMatrix(batch.getProjectionMatrix());
		renderer.setTransformMatrix(batch.getTransformMatrix());
		renderer.begin(ShapeType.Filled);
		renderer.setColor(color);
		renderer.rect(x, y, width, height);
		renderer.end();

		batch.begin();
		super.draw(batch, x, y, width, height);
	}

	@Override
	public void dispose () {
		renderer.dispose();
	}

}
