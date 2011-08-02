package com.badlogic.gdx.box2deditor;

import com.badlogic.gdx.box2deditor.FixtureAtlas;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.World;
import java.util.Random;

public class App implements ApplicationListener {

	// -------------------------------------------------------------------------
	// Launcher
	// -------------------------------------------------------------------------

	public static void main(final String[] args) {
		new LwjglApplication(new App(), "", 500, 750, false);
	}

	// -------------------------------------------------------------------------
	// Static fields
	// -------------------------------------------------------------------------
	
	private static final Vector2 WORLD_SIZE = new Vector2(10, 15);
	private static final Vector2 VIAL_SIZE = new Vector2(8, 8);
	private static final Vector2 BALL_SIZE = new Vector2(0.3f, 0.3f);
	private static final Vector2 VIAL_POS = new Vector2(-4, -7);

	private static final int MAX_BALL_COUNT = 150;

	// -------------------------------------------------------------------------
	// Public API
	// -------------------------------------------------------------------------
	
	private SpriteBatch spriteBatch;
	private BitmapFont font;
	private OrthographicCamera camera;

	private World world;

	private Body vialModel;
	private Texture vialTexture;
	private Sprite vialSprite;
	
	private Body[] ballModels;
	private Texture ballTexture;
	private Sprite[] ballSprites;

	@Override
	public void create() {
		Gdx.graphics.setVSync(true);
		Gdx.input.setInputProcessor(inputProcessor);

		spriteBatch = new SpriteBatch();
		font = new BitmapFont();
		font.setColor(Color.BLACK);
		camera = new OrthographicCamera(WORLD_SIZE.x, WORLD_SIZE.y);
		camera.position.set(0, 0, 0);
		camera.update();

		world = new World(new Vector2(0, -5), true);

		createVialModel();
		createBallModels();
		createSprites();
	}

	private void createVialModel() {
		// Create a FixtureAtlas which will automatically load the fixture
		// list for every body defined with the editor.
		FixtureAtlas atlas = new FixtureAtlas(Gdx.files.internal("data/bodies.bin"));

		// Creation of the vial model:
		// 1. Create a BodyDef, as usual.
		BodyDef vialBodyDef = new BodyDef();
		vialBodyDef.position.set(VIAL_POS);
		vialBodyDef.type = BodyType.StaticBody;

		// 2. Create a Body from the BodyDef, as usual.
		vialModel = world.createBody(vialBodyDef);

		// 3. Create its fixtures automatically by using the FixtureAtlas. Note
		//    that the fixture name must exactly match the one used in the
		//    editor. It has no real relationship with the asset used itself.
		atlas.createFixtures(vialModel, "gfx\\test01.png", VIAL_SIZE.x, VIAL_SIZE.y);
	}

	private void createBallModels() {
		BodyDef ballBodyDef = new BodyDef();
		ballBodyDef.type = BodyType.DynamicBody;

		CircleShape ballShape = new CircleShape();
		ballShape.setRadius(BALL_SIZE.x/2);

		Random rand = new Random();

		ballModels = new Body[MAX_BALL_COUNT];
		for (int i=0; i<MAX_BALL_COUNT; i++) {
			float tx = rand.nextFloat() * 1.0f - 0.5f;
			float ty = rand.nextFloat() * WORLD_SIZE.y*4 + WORLD_SIZE.y/2 + BALL_SIZE.y;
			float angle = rand.nextFloat() * MathUtils.PI * 2;
			ballBodyDef.position.set(tx, ty);
			ballBodyDef.angle = angle;
			ballModels[i] = world.createBody(ballBodyDef);
			ballModels[i].createFixture(ballShape, 1);
		}
	}

	private void createSprites() {
		// Define the Sprite, as usual.
		vialTexture = new Texture(Gdx.files.internal("data/gfx/vial.png"));
		vialTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		vialSprite = new Sprite(vialTexture);
		vialSprite.setSize(VIAL_SIZE.x, VIAL_SIZE.y);

		// "vialModel.getPosition()" returns the reference point, which is the
		// bottom left corner of the asset used for each body defined with the
		// FixtureAtlas.
		Vector2 vialPos = vialModel.getPosition();
		vialSprite.setPosition(vialPos.x, vialPos.y);

		ballTexture = new Texture(Gdx.files.internal("data/gfx/ball.png"));
		ballTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);

		ballSprites = new Sprite[MAX_BALL_COUNT];
		for (int i=0; i<MAX_BALL_COUNT; i++) {
			ballSprites[i] = new Sprite(ballTexture);
			ballSprites[i].setSize(BALL_SIZE.x, BALL_SIZE.y);
			ballSprites[i].setOrigin(BALL_SIZE.x/2, BALL_SIZE.y/2);
		}
	}

	@Override
	public void dispose() {
		vialTexture.dispose();
		ballTexture.dispose();
		spriteBatch.dispose();
		font.dispose();
		world.dispose();
	}

	@Override
	public void render() {
		// Update
		world.step(1 / 60f, 10, 10);
		for (int i=0; i<MAX_BALL_COUNT; i++) {
			Vector2 pos = ballModels[i].getPosition().sub(
				ballSprites[i].getWidth()/2,
				ballSprites[i].getHeight()/2);
			float angleDeg = ballModels[i].getAngle() * MathUtils.radiansToDegrees;

			ballSprites[i].setPosition(pos.x, pos.y);
			ballSprites[i].setRotation(angleDeg);
		}

		// Render
		GL10 gl = Gdx.gl10;
		gl.glClearColor(1, 1, 1, 1);
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		spriteBatch.setProjectionMatrix(camera.combined);
		spriteBatch.begin();
		vialSprite.draw(spriteBatch);
		for (int i=0; i<MAX_BALL_COUNT; i++)
			ballSprites[i].draw(spriteBatch);
		spriteBatch.end();

		spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0,
			Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		spriteBatch.begin();
		font.draw(spriteBatch, "Touch the screen to restart", 5, 25);
		spriteBatch.end();
	}

	@Override
	public void resize(int i, int i1) {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {

	}
	
	// -------------------------------------------------------------------------
	// Internals
	// -------------------------------------------------------------------------

	private void restart() {
		Random rand = new Random();
		for (int i=0; i<MAX_BALL_COUNT; i++) {
			float tx = rand.nextFloat() * 1.0f - 0.5f;
			float ty = rand.nextFloat() * WORLD_SIZE.y*4 + WORLD_SIZE.y/2 + BALL_SIZE.y;
			float angle = rand.nextFloat() * MathUtils.PI * 2;

			ballModels[i].setLinearVelocity(0, 0);
			ballModels[i].setAngularVelocity(0);
			ballModels[i].setTransform(tx, ty, angle);
			ballModels[i].setAwake(true);
		}
	}

	// -------------------------------------------------------------------------
	// Inputs
	// -------------------------------------------------------------------------

	private final InputProcessor inputProcessor = new InputAdapter() {
		@Override
		public boolean touchDown(int x, int y, int pointer, int button) {
			restart();
			return false;
		}
	};
}
