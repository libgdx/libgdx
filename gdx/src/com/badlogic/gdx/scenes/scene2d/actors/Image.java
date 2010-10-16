/*******************************************************************************
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com)
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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class Image extends Actor
{
	public final Color color;
	public final TextureRegion region;
	
	public Image( String name )
	{
		super( name );
		this.color = new Color( 1, 1, 1, 1 );
		this.region = new TextureRegion( null, 0, 0, 0, 0 );
	}
	
	public Image( String name, Texture texture )
	{
		super( name );
		this.color = new Color( 1, 1, 1, 1 );
		this.refX = texture.getWidth() / 2.0f;
		this.refY = texture.getHeight() / 2.0f;
		this.width = texture.getWidth();
		this.height = texture.getHeight();
		this.region = new TextureRegion( texture, 0, 0, texture.getWidth(), texture.getHeight() );
	}
	
	public Image( String name, TextureRegion region )
	{
		super( name );
		this.color = new Color( 1, 1, 1, 1 );
		this.refX = region.width / 2.0f;
		this.refY = region.height / 2.0f;
		this.width = region.width;
		this.height = region.height;
		this.region = new TextureRegion( region.texture, region.x, region.y, region.width, region.height );
	}
	
	@Override
	protected void render(SpriteBatch batch) 
	{
		if( region.texture != null )
			batch.draw( region.texture, x, y, refX, refY, width, height, scaleX, scaleY, rotation, region.x, region.y, region.width, region.height, color, false, false );
	}

	@Override
	protected boolean touchDown(float x, float y, int pointer) {
		System.out.println( name + " touch down:"  + x + ", " + y );
		return false;
	}

	@Override
	protected boolean touchUp(float x, float y, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean touchDragged(float x, float y, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}

	public Actor hit( float x, float y )
	{
		if( x > 0 && x < width )
			if( y > 0 && y < height )
				return this;
		
		return null;
	}
}
