/* ######################################
 * Copyright 2014 (c) Pixel Scientists
 * All rights reserved.
 * Unauthorized copying of this file, via
 * any medium is strictly prohibited.
 * Proprietary and confidential.
 * ###################################### */

package com.badlogic.gdx.tests.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;

/** @author Daniel Holderbaum */
public class DebugActor extends Actor {

	private Texture texture;

	public DebugActor (Color color) {
		Pixmap pixmap = new Pixmap(16, 16, Format.RGBA8888);
		pixmap.setColor(color);
		pixmap.fill();
		texture = new Texture(pixmap);
	}

	@Override
	public void draw (Batch batch, float parentAlpha) {
		batch.draw(texture, getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), getScaleX(), getScaleY(),
			getRotation(), 0, 0, 16, 16, false, false);
	}

}
