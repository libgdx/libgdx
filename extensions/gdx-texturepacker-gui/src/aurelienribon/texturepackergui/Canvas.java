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

import aurelienribon.texturepackergui.Label.Anchor;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Canvas extends ApplicationAdapter {
	private final List<Sprite> sprites = new ArrayList<Sprite>();
	private SpriteBatch batch;
	private OrthographicCamera camera;
	private BitmapFont font;
	private InputMultiplexer inputMultiplexer;
	private Callback callback;

	private Sprite infoLabel;
	private Label lblNextPage;
	private Label lblPreviousPage;
	private Texture bgTex;
	private TextureAtlas atlas;
	private int index = 0;

	private boolean previousPageRequested = false;
	private boolean nextPageRequested = false;
	private boolean packReloadRequested = false;
	private FileHandle packFile = null;

	public static interface Callback {
		public void atlasError();
	}

	@Override
	public void create() {
		Assets.loadAll();
		Texture.setEnforcePotImages(false);

		batch = new SpriteBatch();
		font = new BitmapFont();
		camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.update();

		infoLabel = new Sprite(Assets.getWhiteTex());
		infoLabel.setPosition(0, 0);
		infoLabel.setSize(140, 80);
		infoLabel.setColor(new Color(0x2A/255f, 0x3B/255f, 0x56/255f, 180/255f));

		int lblH = 25;
		Color lblC = new Color(0x2A/255f, 0x6B/255f, 0x56/255f, 180/255f);
		lblNextPage = new Label(10+lblH, 120, lblH, "Next page", font, lblC, Anchor.TOP_RIGHT);
		lblPreviousPage = new Label(15+lblH*2, 120, lblH, "Previous page", font, lblC, Anchor.TOP_RIGHT);

		lblNextPage.setCallback(new Label.TouchCallback() {
			@Override public void touchDown(Label source) {
				nextPageRequested = true;
			}
		});

		lblPreviousPage.setCallback(new Label.TouchCallback() {
			@Override public void touchDown(Label source) {
				previousPageRequested = true;
			}
		});

		lblNextPage.show();
		lblPreviousPage.show();

		bgTex = Assets.getTransparentLightTex();
		bgTex.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

		inputMultiplexer = new InputMultiplexer();
		inputMultiplexer.addProcessor(new PanZoomInputProcessor(this));
		inputMultiplexer.addProcessor(buttonsInputProcessor);
		Gdx.input.setInputProcessor(inputMultiplexer);
	}

	@Override
	public void render() {
		if (previousPageRequested) {
			previousPageRequested = false;
			index = index-1 < 0 ? sprites.size()-1 : index-1;
		}

		if (nextPageRequested) {
			nextPageRequested = false;
			index = index+1 >= sprites.size() ? 0 : index+1;
		}

		if (packReloadRequested) {
			packReloadRequested = false;
			index = 0;
			camera.position.set(0, 0, 0);
			camera.update();

			sprites.clear();
			if (atlas != null) atlas.dispose();

			if (packFile != null && packFile.exists()) {
				try {
					atlas = new TextureAtlas(packFile);
					List<Texture> textures = new ArrayList<Texture>();

					for (AtlasRegion region : atlas.getRegions()) {
						if (!textures.contains(region.getTexture()))
							textures.add(region.getTexture());
					}

					for (Texture tex : textures) {
						Sprite sp = new Sprite(tex);
						sp.setOrigin(sp.getWidth()/2, sp.getHeight()/2);
						sp.setPosition(-sp.getOriginX(), -sp.getOriginY());
						sprites.add(sp);
					}
				} catch (GdxRuntimeException ex) {
					atlas = null;
					sprites.clear();
					callback.atlasError();
				}
			}
		}

		// Render

		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();
		float tw = bgTex.getWidth();
		float th = bgTex.getHeight();

		batch.getProjectionMatrix().setToOrtho2D(0, 0, w, h);
		batch.begin();
		batch.disableBlending();
		batch.draw(bgTex, 0f, 0f, w, h, 0f, 0f, w/tw, h/th);
		batch.enableBlending();
		batch.end();

		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		if (!sprites.isEmpty()) sprites.get(index).draw(batch);
		batch.end();

		batch.getProjectionMatrix().setToOrtho2D(0, 0, w, h);
		batch.begin();
		font.setColor(Color.WHITE);
		lblNextPage.draw(batch);
		lblPreviousPage.draw(batch);
		infoLabel.draw(batch);
		if (sprites.isEmpty()) font.draw(batch, "No page to show", 10, 65);
		else font.draw(batch, "Page " + (index + 1) + " / " + sprites.size(), 10, 65);
		font.draw(batch, String.format(Locale.US, "Zoom: %.0f %%", 100f / camera.zoom), 10, 45);
		font.draw(batch, "Fps: " + Gdx.graphics.getFramesPerSecond(), 10, 25);
		batch.end();
	}

	@Override
	public void resize(int width, int height) {
		Gdx.gl.glViewport(0, 0, width, height);
		camera.viewportWidth = width;
		camera.viewportHeight = height;
		camera.update();
	}

	public Vector2 screenToWorld(int x, int y) {
		Vector3 v3 = new Vector3(x, y, 0);
		camera.unproject(v3);
		return new Vector2(v3.x, v3.y);
	}

	public OrthographicCamera getCamera() {
		return camera;
	}

	public void requestPackReload(String packPath) {
		packReloadRequested = true;
		if (packPath != null) packFile = Gdx.files.absolute(packPath);
		else packFile = null;
	}

	public void setCallback(Callback callback) {
		this.callback = callback;
	}

	private final InputProcessor buttonsInputProcessor = new InputAdapter() {
		@Override
		public boolean touchDown(int x, int y, int pointer, int button) {
			if (button == Input.Buttons.LEFT) {
				if (lblNextPage.touchDown(x, y)) return true;
				if (lblPreviousPage.touchDown(x, y)) return true;
			}
			return false;
		}

		@Override
		public boolean touchMoved(int x, int y) {
			lblNextPage.touchMoved(x, y);
			lblPreviousPage.touchMoved(x, y);
			return false;
		}
	};
}