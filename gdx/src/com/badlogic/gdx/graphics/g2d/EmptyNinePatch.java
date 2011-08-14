
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
