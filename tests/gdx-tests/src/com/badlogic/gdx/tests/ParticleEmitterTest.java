
package com.badlogic.gdx.tests;

import java.util.ArrayList;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputListener;
import com.badlogic.gdx.graphics.BitmapFont;
import com.badlogic.gdx.graphics.BitmapFont.HAlignment;
import com.badlogic.gdx.graphics.BitmapFontCache;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Sprite;
import com.badlogic.gdx.graphics.SpriteBatch;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.particles.ParticleEffect;
import com.badlogic.gdx.graphics.particles.ParticleEmitter;
import com.badlogic.gdx.tests.utils.GdxTest;

public class ParticleEmitterTest implements GdxTest {
	private SpriteBatch spriteBatch;
	private BitmapFont font;
	ParticleEffect effect;
	int emitterIndex;
	ArrayList<ParticleEmitter> emitters;
	int particleCount = 10;

	public void surfaceCreated () {
		if (spriteBatch != null) return;
		spriteBatch = new SpriteBatch();

		font = new BitmapFont(Gdx.files.getFileHandle("data/verdana39.fnt", FileType.Internal), Gdx.files.getFileHandle(
			"data/verdana39.png", FileType.Internal), false);

		effect = new ParticleEffect();
		effect.load(Gdx.files.getFileHandle("data/test.p", FileType.Internal), "data", FileType.Internal);
		effect.setPosition(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
		emitters = new ArrayList(effect.getEmitters());
		effect.getEmitters().clear();
		effect.getEmitters().add(emitters.get(0));

		Gdx.input.addInputListener(new InputListener() {
			public boolean touchUp (int x, int y, int pointer) {
				return false;
			}

			public boolean touchDragged (int x, int y, int pointer) {
				effect.setPosition(x, Gdx.graphics.getHeight() - y);
				return false;
			}

			public boolean touchDown (int x, int y, int pointer) {
				effect.setPosition(x, Gdx.graphics.getHeight() - y);
				return false;
			}

			public boolean keyUp (int keycode) {
				return false;
			}

			public boolean keyTyped (char character) {
				return false;
			}

			public boolean keyDown (int keycode) {
				ParticleEmitter emitter = emitters.get(emitterIndex);
				if (keycode == Input.Keys.KEYCODE_DPAD_UP)
					particleCount += 5;
				else if (keycode == Input.Keys.KEYCODE_DPAD_DOWN)
					particleCount -= 5;
				else if (keycode == Input.Keys.KEYCODE_SPACE)
					emitterIndex = (emitterIndex + 1) % emitters.size();
				else
					return false;
				particleCount = Math.max(0, particleCount);
				if (particleCount > emitter.getMaxParticleCount()) emitter.setMaxParticleCount(particleCount * 2);
				emitter.getEmission().setHigh(particleCount / emitter.getLife().getHighMax());
				effect.getEmitters().clear();
				effect.getEmitters().add(emitters.get(emitterIndex));
				return false;
			}
		});
	}

	public void surfaceChanged (int width, int height) {
	}

	public void render () {
		GL10 gl = Gdx.graphics.getGL10();
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		spriteBatch.begin();
		effect.draw(spriteBatch, Gdx.graphics.getDeltaTime());
		font.draw(spriteBatch, Gdx.graphics.getFramesPerSecond() + " fps", 5, 40, Color.WHITE);
		int activeCount = emitters.get(emitterIndex).getActiveCount();
		font.draw(spriteBatch, activeCount + "/" + particleCount + " particles", 5, Gdx.graphics.getHeight() - 5, Color.WHITE);
		spriteBatch.end();
	}

	public void dispose () {
	}

	public boolean needsGL20 () {
		return false;
	}
}
