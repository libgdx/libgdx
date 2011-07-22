
package com.badlogic.gdx.graphics.g2d;

import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;

/**
 * Empty ninepatch for controls that require a ninepatch.
 */
public class EmptyNinePatch extends NinePatch {
	static private final TextureRegion[] emptyPatches;
	static {
		// This is kind of gross...
		Texture texture = new Texture(2, 2, Format.RGBA8888);
		TextureRegion region = new TextureRegion(texture) {
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
	}

	static public final EmptyNinePatch instance = new EmptyNinePatch();

	public EmptyNinePatch () {
		super(emptyPatches);
	}

	public void draw (SpriteBatch batch, float x, float y, float width, float height) {
	}
}
