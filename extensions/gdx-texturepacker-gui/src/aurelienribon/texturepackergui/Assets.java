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
package aurelienribon.texturepackergui;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;

/**
 * @author Aurelien Ribon | http://www.aurelienribon.com/
 */
public class Assets extends AssetManager {
	private static AssetManager manager;

	public static void loadAll() {
		manager = new AssetManager();

		String[] textures = new String[] {
			"res/data/transparent-light.png",
			"res/data/transparent-dark.png",
			"res/data/white.png"
		};

		for (String tex : textures) manager.load(tex, Texture.class);
		while (!manager.update()) {}
	}

	public static Texture getTransparentLightTex() {return manager.get("res/data/transparent-light.png", Texture.class);}
	public static Texture getTransparentDarkTex() {return manager.get("res/data/transparent-dark.png", Texture.class);}
	public static Texture getWhiteTex() {return manager.get("res/data/white.png", Texture.class);}
}