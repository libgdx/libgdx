
package com.badlogic.gdx.tools.etc1;

import java.io.File;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.tools.texturepacker.TexturePacker.Page;
import com.badlogic.gdx.utils.Array;

public class AtlasETC1CompressorTest implements ApplicationListener {

	private String atlas_path;
	private FileHandle atlas_file;

	public AtlasETC1CompressorTest (String atlas_path) {
		this.atlas_path = atlas_path;
		this.atlas_file = new FileHandle(atlas_path);
	}

	public static void main (String[] args) throws Exception {

		String application_home_path_string = System.getProperty("user.dir");

		FileHandle home_folder = new FileHandle(new File(application_home_path_string));
		FileHandle sprites_folder = home_folder.child("sprites");
		FileHandle atlas_folder = home_folder.child("atlas");

		String output_atlas_filename = "atlas_test.atlas";
		String png_input_dir = sprites_folder.path();
		String atlas_output_dir = atlas_folder.path();

		prepareTestAtlas(png_input_dir, atlas_output_dir, output_atlas_filename);

		String atlas_file_path_string = atlas_folder.child(output_atlas_filename).path();

		boolean COMPRESS = true;

		if (COMPRESS) {
			AtlasETC1CompressorSettings settings = AtlasETC1Compressor.newCompressionSettings();
			settings.setAtlasFilePathString(atlas_file_path_string);
			AtlasETC1CompressionResult compression_result = AtlasETC1Compressor.compress(settings);
			log();
			compression_result.print();
		}

		log("Showing compressed sprites");
		new LwjglApplication(new AtlasETC1CompressorTest(atlas_file_path_string), "", 1024, 768);

	}

	private static void prepareTestAtlas (String png_input_dir, String atlas_output_dir, String output_atlas_filename) {
		log("png_input_dir", png_input_dir);
		log("atlas_output_dir", atlas_output_dir);
		log("output_atlas_filename", output_atlas_filename);

		TexturePacker.Settings atlas_settings = new TexturePacker.Settings();
		atlas_settings.debug = true;
		atlas_settings.maxWidth = 512;
		atlas_settings.maxHeight = 256;
		atlas_settings.format = Format.RGBA8888;

		TexturePacker.process(atlas_settings, png_input_dir, atlas_output_dir, output_atlas_filename);
	}

	private static void log (String tag, Object message) {
		AtlasETC1Compressor.log(tag, message);
	}

	private static void log (Object message) {
		AtlasETC1Compressor.log(message);
	}

	private static void log () {
		AtlasETC1Compressor.log();
	}

	/// -------------------------------------------------------------------------------------------------

	SpriteBatch batch;
	Array<Page> pages;
	private TextureAtlas atlas;
	private Array<Sprite> sprites;

	public void create () {
		batch = new SpriteBatch();

		atlas = new TextureAtlas(this.atlas_file);
		sprites = atlas.createSprites();
		float x = 0;
		for (int i = 0; i < sprites.size; i++) {
			Sprite sprite = sprites.get(i);
			sprite.setX(x);
			x = x + sprite.getWidth() * 0.7f;
		}

	}

	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		int x = 20, y = 20;
		batch.begin();
		for (Sprite sprite : sprites) {
			sprite.draw(batch);
		}
		batch.end();
	}

	public void resize (int width, int height) {
		batch.setProjectionMatrix(new Matrix4().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
	}

	@Override
	public void pause () {
	}

	@Override
	public void resume () {
	}

	@Override
	public void dispose () {
	}

}
