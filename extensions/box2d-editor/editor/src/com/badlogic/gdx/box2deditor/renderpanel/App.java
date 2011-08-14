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
package com.badlogic.gdx.box2deditor.renderpanel;

import com.badlogic.gdx.box2deditor.AppContext;
import com.badlogic.gdx.box2deditor.renderpanel.inputprocessors.BallThrowInputProcessor;
import com.badlogic.gdx.box2deditor.renderpanel.inputprocessors.PanZoomInputProcessor;
import com.badlogic.gdx.box2deditor.renderpanel.inputprocessors.ShapeCreationInputProcessor;
import com.badlogic.gdx.box2deditor.renderpanel.inputprocessors.ShapeEditionInputProcessor;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author Aurelien Ribon (aurelien.ribon@gmail.com)
 */
public class App implements ApplicationListener {
	private static App instance = new App();
	public static App instance() { if (instance == null) instance = new App(); return instance; }

	private static final float PX_PER_METER = 50;

	private AppDrawer drawer;
	private SpriteBatch sb;
	private BitmapFont font;
	private Texture backgroundTexture;

	private OrthographicCamera camera;
	private int zoom = 100;
	private final int[] zoomLevels = {16, 25, 33, 50, 66, 100, 150, 200, 300, 400, 600, 800, 1000, 1500, 2000, 2500, 3000, 4000, 5000};

	private Pixmap assetPixmap;
	private Texture assetTexture;
	private Sprite assetSprite;
	int[] potWidths = {1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 5096};

	private Random rand;
	private World world;
	private Texture ballTexture;
	private List<Body> ballModels;
	private List<Sprite> ballSprites;
	
	@Override
	public void create() {
		sb = new SpriteBatch();
		
		font = new BitmapFont();
		font.setColor(Color.BLACK);

		camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.update();

		backgroundTexture = new Texture(Gdx.files.classpath("aurelienribon/box2deditor/gfx/transparent.png"));
		backgroundTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

		rand = new Random();
		ballTexture = new Texture(Gdx.files.classpath("aurelienribon/box2deditor/gfx/ball.png"));
		ballModels = new ArrayList<Body>();
		ballSprites = new ArrayList<Sprite>();
		
		drawer = new AppDrawer(camera);

		InputMultiplexer im = new InputMultiplexer();
		im.addProcessor(new PanZoomInputProcessor());
		im.addProcessor(new ShapeCreationInputProcessor());
		im.addProcessor(new ShapeEditionInputProcessor());
		im.addProcessor(new BallThrowInputProcessor());
		Gdx.input.setInputProcessor(im);
		Gdx.graphics.setVSync(true);
	}

	@Override
	public void resume() {
	}

	@Override
	public void render() {
		if (assetSprite != null)
			assetSprite.setColor(1, 1, 1, AppContext.instance().isAssetDrawnWithOpacity50 ? 0.5f : 1f);

		if (world != null)
			world.step(Gdx.graphics.getDeltaTime(), 10, 10);

		GL10 gl = Gdx.gl10;
		gl.glClearColor(1, 1, 1, 1);
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();
		float tw = backgroundTexture.getWidth();
		float th = backgroundTexture.getHeight();

		sb.getProjectionMatrix().setToOrtho2D(0, 0, w, h);
		sb.begin();
		sb.disableBlending();
		sb.draw(backgroundTexture, 0f, 0f, w, h, 0f, 0f, w/tw, h/th);
		sb.enableBlending();
		font.draw(sb, "Zoom: " + zoom + "%", 5, 45);
		font.draw(sb, "Fps: " + Gdx.graphics.getFramesPerSecond(), 5, 25);
		sb.end();

		sb.setProjectionMatrix(camera.combined);
		sb.begin();
		if (assetSprite != null && AppContext.instance().isAssetDrawn)
			assetSprite.draw(sb);
		for (int i=0; i<ballSprites.size(); i++) {
			Sprite sp = ballSprites.get(i);
			Vector2 pos = ballModels.get(i).getWorldCenter().mul(PX_PER_METER).sub(sp.getWidth()/2, sp.getHeight()/2);
			float angleDeg = ballModels.get(i).getAngle() * MathUtils.radiansToDegrees;
			sp.setPosition(pos.x, pos.y);
			sp.setRotation(angleDeg);
			sp.draw(sb);
		}
		sb.end();

		camera.apply(gl);
		drawer.draw();
	}

	@Override
	public void resize(int width, int height) {
		GL10 gl = Gdx.gl10;
		gl.glViewport(0, 0, width, height);
		camera.viewportWidth = width;
		camera.viewportHeight = height;
		camera.update();
	}

	@Override
	public void pause() {
	}

	@Override
	public void dispose() {
		clearAsset();
		backgroundTexture.dispose();
		sb.dispose();
		font.dispose();
	}

	// -------------------------------------------------------------------------
	// Public API
	// -------------------------------------------------------------------------

	public Vector2 screenToWorld(int x, int y) {
		return new Vector2(x, Gdx.graphics.getHeight() - y)
			.sub(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2)
			.mul(camera.zoom)
			.add(camera.position.x, camera.position.y);
	}

	public void clearAsset() {
		if (assetPixmap != null) {
			assetPixmap.dispose();
			assetPixmap = null;
		}
		if (assetTexture != null) {
			assetTexture.dispose();
			assetTexture = null;
		}
		if (assetSprite != null) {
			assetSprite = null;
		}
		clearBody();
	}

	public Vector2 setAssetByFile(String fullpath) {
		clearAsset();

		Pixmap tempPm = new Pixmap(Gdx.files.absolute(fullpath));
		int origW = tempPm.getWidth();
		int origH = tempPm.getHeight();
		int w = getNearestPOT(origW);
		int h = getNearestPOT(origH);
		assetPixmap = new Pixmap(w, h, tempPm.getFormat());
		assetPixmap.drawPixmap(tempPm, 0, h - origH, 0, 0, origW, origH);
		tempPm.dispose();

		assetTexture = new Texture(assetPixmap);
		assetTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		assetSprite = new Sprite(assetTexture);
		assetSprite.setPosition(0, 0);

		camera.position.set(origW/2, origH/2, 0);
		camera.update();

		return new Vector2(origW, origH);
	}

	public void clearBody() {
		ballModels.clear();
		ballSprites.clear();
		if (world != null) {
			world.dispose();
			world = null;
		}
	}

	public boolean isWorldReady() {
		return world != null;
	}

	public void setBody(Vector2[][] polygons) {
		ballModels.clear();
		ballSprites.clear();
		if (world != null) {
			world.dispose();
			world = null;
		}

		if (polygons == null || polygons.length == 0)
			return;

		world = new World(new Vector2(0, 0), true);
		Body b = world.createBody(new BodyDef());

		for (Vector2[] polygon : polygons) {
			Vector2[] resizedPolygon = new Vector2[polygon.length];
			for (int i=0; i<polygon.length; i++)
				resizedPolygon[i] = new Vector2(polygon[i]).mul(1f / PX_PER_METER);

			PolygonShape shape = new PolygonShape();
			shape.set(resizedPolygon);

			FixtureDef fd = new FixtureDef();
			fd.density = 1f;
			fd.friction = 0.8f;
			fd.restitution = 0.2f;
			fd.shape = shape;

			b.createFixture(fd);
			shape.dispose();
		}
	}

	public void fireBall(Vector2 orig, Vector2 force) {
		float radius = rand.nextFloat() * 10 + 5;

		BodyDef bd = new BodyDef();
		bd.angularDamping = 0.5f;
		bd.linearDamping = 0.5f;
		bd.type = BodyType.DynamicBody;
		bd.position.set(orig).mul(1 / PX_PER_METER);
		bd.angle = rand.nextFloat() * MathUtils.PI;
		Body b = world.createBody(bd);
		b.applyLinearImpulse(force.mul(2 / PX_PER_METER), orig);
		ballModels.add(b);

		CircleShape shape = new CircleShape();
		shape.setRadius(radius / PX_PER_METER);
		b.createFixture(shape, 1f);

		Sprite sp = new Sprite(ballTexture);
		sp.setSize(radius*2, radius*2);
		sp.setOrigin(sp.getWidth()/2, sp.getHeight()/2);
		ballSprites.add(sp);
	}

	public OrthographicCamera getCamera() {
		return camera;
	}

	public int getZoom() {
		return zoom;
	}

	public void setZoom(int zoom) {
		this.zoom = zoom;
	}

	public int[] getZoomLevels() {
		return zoomLevels;
	}

	// -------------------------------------------------------------------------
	// Internals
	// -------------------------------------------------------------------------

	private int getNearestPOT(int d) {
		for (int i=0; i<potWidths.length; i++)
			if (d <= potWidths[i])
				return potWidths[i];
		return -1;
	}
}