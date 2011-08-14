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

import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;

/** Empty ninepatch for controls that require a ninepatch. */
public class EmptyNinePatch extends NinePatch {
	static private TextureRegion[] emptyPatches;
	static private EmptyNinePatch instance;
	static private TextureRegion region;

	static public EmptyNinePatch getInstance () {
		if (instance == null) {
			// This is kind of gross...
			Texture texture = new Texture(2, 2, Format.RGBA8888);
			region = new TextureRegion(texture) {
				public int getRegionWidth () {
					return 0;
				}

				public int getRegionHeight () {
					return 0;
				}
			};
			emptyPatches = new TextureRegion[] { //
			region, region, region, //
				region, region, region, //
				region, region, region //
			};
			instance = new EmptyNinePatch();
		}
		return instance;
	}

	static public TextureRegion getRegion () {
		getInstance();
		return region;
	}

	private EmptyNinePatch () {
		super(emptyPatches);
	}

	public void draw (SpriteBatch batch, float x, float y, float width, float height) {
	}
}