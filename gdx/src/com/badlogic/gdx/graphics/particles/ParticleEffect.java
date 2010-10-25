
package com.badlogic.gdx.graphics.particles;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.GdxRuntimeException;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.SpriteBatch;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;

// BOZO - Cache particle images? Or add hook to customize loading of particle images?

public class ParticleEffect {
	private ArrayList<ParticleEmitter> emitters = new ArrayList();

	public void start () {
		for (ParticleEmitter particles : emitters)
			particles.start();
	}

	public void draw (SpriteBatch spriteBatch, float delta) {
		for (int i = 0, n = emitters.size(); i < n; i++)
			emitters.get(i).draw(spriteBatch, delta);
	}

	public void allowCompletion () {
		for (int i = 0, n = emitters.size(); i < n; i++) {
			ParticleEmitter emitter = emitters.get(i);
			emitter.setContinuous(false);
			emitter.durationTimer = emitter.duration;
		}
	}

	public boolean isComplete () {
		for (int i = 0, n = emitters.size(); i < n; i++) {
			ParticleEmitter emitter = emitters.get(i);
			if (emitter.isContinuous()) return false;
			if (!emitter.isComplete()) return false;
		}
		return true;
	}

	public void setDuration (int duration) {
		for (int i = 0, n = emitters.size(); i < n; i++) {
			ParticleEmitter emitter = emitters.get(i);
			emitter.setContinuous(false);
			emitter.duration = duration;
			emitter.durationTimer = 0;
		}
	}

	public void setPosition (int x, int y) {
		for (int i = 0, n = emitters.size(); i < n; i++)
			emitters.get(i).setPosition(x, y);
	}

	public ArrayList<ParticleEmitter> getEmitters () {
		return emitters;
	}

	public void save (File file) {
		Writer output = null;
		try {
			output = new FileWriter(file);
			int index = 0;
			for (int i = 0, n = emitters.size(); i < n; i++) {
				ParticleEmitter emitter = emitters.get(i);
				if (index++ > 0) output.write("\n\n");
				emitter.save(output);
				output.write("- Image Path -\n");
				output.write(emitter.getImagePath() + "\n");
			}
		} catch (IOException ex) {
			throw new GdxRuntimeException("Error saving effect: " + file, ex);
		} finally {
			try {
				if (output != null) output.close();
			} catch (IOException ex) {
			}
		}
	}

	public void load (FileHandle effectFile, String imagesDir, FileType fileType) {
		loadEmitters(effectFile);
		loadEmitterImages(imagesDir, fileType);
	}

	void loadEmitters (FileHandle file) {
		InputStream input = file.getInputStream();
		if (input == null) throw new GdxRuntimeException("Effect file not found: " + file);
		emitters.clear();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(input), 512);
			while (true) {
				ParticleEmitter emitter = new ParticleEmitter(reader);
				reader.readLine();
				emitter.setImagePath(ParticleEmitter.readString(reader, "Image Path"));
				emitters.add(emitter);
				if (reader.readLine() == null) break;
				if (reader.readLine() == null) break;
			}
		} catch (IOException ex) {
			throw new GdxRuntimeException("Error loading effect: " + file, ex);
		} finally {
			try {
				if (reader != null) reader.close();
			} catch (IOException ex) {
			}
		}
	}

	private void loadEmitterImages (String imagesDir, FileType fileType) {
		imagesDir = imagesDir.replace('\\', '/');
		if (!imagesDir.endsWith("/")) imagesDir += '/';
		for (int i = 0, n = emitters.size(); i < n; i++) {
			ParticleEmitter emitter = emitters.get(i);
			String imagePath = emitter.getImagePath();
			if (imagePath == null) continue;
			imagePath = imagesDir + new File(imagePath).getName();
			emitter.setTexture(Gdx.graphics.newTexture(Gdx.files.getFileHandle(imagePath, fileType), TextureFilter.Linear,
				TextureFilter.Linear, TextureWrap.ClampToEdge, TextureWrap.ClampToEdge));
		}
	}
}
