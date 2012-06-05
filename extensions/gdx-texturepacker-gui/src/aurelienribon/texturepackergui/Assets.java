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
