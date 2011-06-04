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
package com.badlogic.gdx.scenes.scene2d.actors;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class FastImage extends Actor {
	public final TextureRegion region;

	private float sX;
	private float sY;
	private float sOriginX;
	private float sOriginY;
	private float sRotation;
	private float sScaleX;
	private float sScaleY;
	private float sWidth;
	private float sHeight;
	private Sprite sprite = new Sprite();	
	
	public FastImage (String name) {
		super(name);
		this.region = new TextureRegion();
	}

	public FastImage (String name, Texture texture) {
		super(name);
		this.originX = texture.getWidth() / 2.0f;
		this.originY = texture.getHeight() / 2.0f;
		this.width = texture.getWidth();
		this.height = texture.getHeight();
		this.region = new TextureRegion(texture);		
	}

	public FastImage (String name, TextureRegion region) {
		super(name);
		width = Math.abs(region.getRegionWidth());
		height = Math.abs(region.getRegionHeight());
		originX = width / 2.0f;
		originY = height / 2.0f;
		this.region = new TextureRegion(region);
	}

	@Override protected void draw (SpriteBatch batch, float parentAlpha) {
		updateSprite();
		
		if (region.getTexture() != null) {			
			sprite.draw(batch, parentAlpha);
		}
	}
	
	private void updateSprite() {		
		if(sX != x || sY != y) {
			sprite.setPosition(x, y);
			sX = x;
			sY = y;
		}
		
		if(sOriginX != originX || sOriginY != originY) {
			sprite.setOrigin(originX, originY);
			sOriginX = originX;
			sOriginY = originY;
		}
		
		if(sRotation != rotation) {
			sprite.setRotation(rotation);
			sRotation = rotation;
		}
		
		if(sScaleX != scaleX || sScaleY != scaleY) {
			sprite.setScale(scaleX, scaleY);
			sScaleX = scaleX;
			sScaleY = scaleY;
		}
		
		if(sWidth != width || sHeight != height) {
			sprite.setSize(width, height);
			sWidth = width;
			sHeight = height;
		}
				
		sprite.setRegion(region);		
	}

	@Override protected boolean touchDown (float x, float y, int pointer) {
		return x > 0 && y > 0 && x < width && y < height;
	}

	@Override protected boolean touchUp (float x, float y, int pointer) {
		return false;
	}

	@Override protected boolean touchDragged (float x, float y, int pointer) {
		return false;
	}

	public Actor hit (float x, float y) {
		if (x > 0 && x < width) if (y > 0 && y < height) return this;

		return null;
	}
}
