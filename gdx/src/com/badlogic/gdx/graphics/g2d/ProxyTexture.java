
package com.badlogic.gdx.graphics.g2d;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;

/** A {@link Texture} derived class to use with {@link ProxyTextureLoader}.
 * @author https://github.com/avianey
 */
public class ProxyTexture extends Texture {

	public ProxyTexture(Pixmap pixmap) {
		super(pixmap);
	}

	public ProxyTexture(TextureData data) {
		super(data);
	}

}
