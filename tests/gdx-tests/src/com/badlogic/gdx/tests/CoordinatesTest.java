package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.HdpiMode;
import com.badlogic.gdx.graphics.glutils.HdpiUtils;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class CoordinatesTest extends GdxTest {
	Skin skin;
	Stage stage;
	ShapeRenderer shapeRenderer;
	Viewport stageViewport;
	Viewport gameViewport;
	Camera camera;
	private Image img;
	
	final Vector2 localActorScreen = new Vector2();
	private Label inScreenLabel;
	private Label vpScreenLabel;
	private Label stScreenLabel;
	private Label acScreenLabel;
	private Label cmScreenLabel;

	private final Vector2 vec2 = new Vector2();
	private final Vector3 vec3 = new Vector3();

	@Override
	public void create () {
		
		skin = new Skin(Gdx.files.internal("data/uiskin.json"));
		TextureRegionDrawable logo = new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("data/badlogic.jpg"))));

		stageViewport = new FitViewport(500, 500);
		stageViewport = new Viewport() {
		    public void update(int screenWidth, int screenHeight, boolean centerCamera) {
		        setScreenBounds(20, 20, screenWidth - 20, screenHeight - 20);
		        setWorldSize(500, 500);
		        apply(centerCamera);
		    }
		};
		stageViewport.setCamera(new OrthographicCamera());
		gameViewport = new FitViewport(100, 100);
		
		camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		
		stage = new Stage(stageViewport);
		Gdx.input.setInputProcessor(stage);

		shapeRenderer = new ShapeRenderer();
		
		Table root = new Table();
		root.setFillParent(true);
		stage.addActor(root);
		
		Table metrics = new Table(skin);
		root.add(metrics).expand().top().left();
		metrics.defaults().pad(3).expandX().left();
		
		img = new Image(logo){
			@Override
			public void draw (Batch batch, float parentAlpha) {
				stage.toScreenCoordinates(localActorScreen.set(getX(), getY()), batch.getTransformMatrix());
				super.draw(batch, parentAlpha);
			}
		};
		img.setSize(64, 64);
		stage.addActor(img);
		
		inScreenLabel = metrics.add("").getActor();
		metrics.row();
		vpScreenLabel = metrics.add("").getActor();
		metrics.row();
		cmScreenLabel = metrics.add("").getActor();
		metrics.row();
		stScreenLabel = metrics.add("").getActor();
		metrics.row();
		acScreenLabel = metrics.add("").getActor();
		metrics.row();
	}
	
	@Override
	public void render () {
		
		final float screenHeight = Gdx.graphics.getHeight();
		
		// display screen coordinates for actor, stage, viewport, and camera
		int pointerX = Gdx.input.getX();
		int pointerY = Gdx.input.getY();
		inScreenLabel.setText("input: " + pointerX + " " + pointerY);
		
		gameViewport.unproject(vec2.set(pointerX, pointerY));
		float vpWorldX = vec2.x;
		float vpWorldY = vec2.y;
		
		gameViewport.project(vec2);
		float vpScreenX = vec2.x;
		float vpScreenY = screenHeight - vec2.y;
		vpScreenLabel.setText("viewport re-project: " + vpScreenX + " " + vpScreenY);
		
		camera.unproject(vec3.set(pointerX, pointerY, 0));
		float cmWorldX = vec3.x;
		float cmWorldY = vec3.y;
		float cmWorldZ = vec3.z;
		
		camera.project(vec3.set(cmWorldX, cmWorldY, cmWorldZ));
		float cmScreenX = vec3.x;
		float cmScreenY = screenHeight - vec3.y;
		cmScreenLabel.setText("camera re-project: " + cmScreenX + " " + cmScreenY);
		
		stage.screenToStageCoordinates(vec2.set(pointerX, pointerY));
		float stWorldX = vec2.x;
		float stWorldY = vec2.y;
		
		stage.stageToScreenCoordinates(vec2);
		float stScreenX = vec2.x;
		float stScreenY = vec2.y;
		stScreenLabel.setText("stage re-project: " + stScreenX + " " + stScreenY);
		
		
		acScreenLabel.setText("actor real: " + localActorScreen.x + " " + localActorScreen.y);
		
		// clear screen with dark color
		Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		// clear viewport virtual screen with lighter color
		Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
		Gdx.gl.glScissor(gameViewport.getScreenX(), gameViewport.getScreenY(), gameViewport.getScreenWidth(), gameViewport.getScreenHeight());
		Gdx.gl.glClearColor(0.5f, 0.5f, 0.5f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
		
		// test stage world coordinates
		img.setPosition(stWorldX, stWorldY);
		stage.getViewport().apply();
		stage.act();
		stage.draw();
		
		
		// test viewport world coordinates
		gameViewport.apply();
		shapeRenderer.setProjectionMatrix(gameViewport.getCamera().combined);
		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.setColor(Color.RED);
		shapeRenderer.rect(vpWorldX, vpWorldY, 10, 10);
		shapeRenderer.end();
		
		// test camera world coordinates
		HdpiUtils.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		shapeRenderer.setProjectionMatrix(camera.combined);
		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.setColor(Color.GREEN);
		shapeRenderer.rect(cmWorldX, cmWorldY, 10, 10);
		shapeRenderer.end();
	}

	@Override
	public void resize (int width, int height) {
		stageViewport.update(width, height, true);
		gameViewport.update(width, height);
	}

	@Override
	public void dispose () {
		stage.dispose();
		skin.dispose();
		shapeRenderer.dispose();
	}
}