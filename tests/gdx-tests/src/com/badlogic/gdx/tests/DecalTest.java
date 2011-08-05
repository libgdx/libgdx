package com.badlogic.gdx.tests;

import java.util.LinkedList;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.graphics.g3d.decals.GroupStrategy;
import com.badlogic.gdx.graphics.g3d.decals.SimpleOrthoGroupStrategy;
import com.badlogic.gdx.math.WindowedMean;

public class DecalTest extends Game {
	public static final int TARGET_FPS = 40;
	public static final int INITIAL_RENDERED = 100;
	private boolean willItBlend_that_is_the_question = true;
	private GroupStrategy strategy = new SimpleOrthoGroupStrategy();
	//private GroupStrategy strategy = new DefaultGroupStrategy();
	Texture egg;
	Texture wheel;
	LinkedList<Decal> toRender = new LinkedList<Decal>();
	DecalBatch batch;
	float timePassed = 0;
	int frames = 0;
	Camera cam;
	WindowedMean fps = new WindowedMean(5);
	int idx = 0;
	float w;
	float h;

	@Override
	public void create() {
		Gdx.gl.glEnable(GL10.GL_DEPTH_TEST);
		Gdx.gl10.glDepthFunc(GL10.GL_LESS);

		egg = new Texture(Gdx.files.internal("data/egg.png"));
		egg.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		egg.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
		wheel = new Texture(Gdx.files.internal("data/wheel.png"));
		wheel.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		wheel.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);

		w = Gdx.graphics.getWidth() / 0.8f;
		h = Gdx.graphics.getHeight() / 0.8f;
		for(int i = 0; i < INITIAL_RENDERED; i++) {
			toRender.add(makeDecal());
		}
		batch = new DecalBatch(strategy);

		Gdx.gl.glClearColor(1,1,0,1);
	}

	@Override
	public void render() {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);


		float elapsed = Gdx.graphics.getDeltaTime();
		float scale = timePassed > 0.5 ? 1 - timePassed / 2 : 0.5f + timePassed / 2;

		for(Decal decal : toRender) {
			decal.rotateZ(elapsed * 45);
			decal.setScale(scale);
			batch.add(decal);
		}
		batch.flush();

		timePassed += elapsed;
		frames++;
		if(timePassed > 1.0f) {
			System.out.println("DecalPerformanceTest2 fps: " + frames + " at spritecount: " + toRender.size());
			fps.addValue(frames);
			if(fps.hasEnoughData()) {
				float factor = fps.getMean() / (float) TARGET_FPS;
				int target = (int) (toRender.size() * factor);
				if(fps.getMean() > TARGET_FPS) {
					int start = toRender.size();
					for(int i = start; toRender.size() < target; i++) {
						toRender.add(makeDecal());
					}
					fps.clear();
				}
				else {
					while(toRender.size() > target) {
						toRender.removeLast();
					}
					fps.clear();
				}
			}
			timePassed = 0;
			frames = 0;
		}
	}

	@Override
	public void resize(int width, int height) {
		w = Gdx.graphics.getWidth() / 0.8f;
		h = Gdx.graphics.getHeight() / 0.8f;
		cam = new OrthographicCamera(width, height);
		cam.near = 0.1f;
		cam.far = 10f;
		cam.position.set(0, 0, 0.1f);
		cam.direction.set(0, 0, -1f);
		cam.update();
		cam.apply(Gdx.gl10);
	}

	private Decal makeDecal() {
		Decal sprite = null;
		switch(idx % 2) {
			case 0:
				sprite = Decal.newDecal(new TextureRegion(egg), willItBlend_that_is_the_question);
				break;
			case 1:
				sprite = Decal.newDecal(new TextureRegion(wheel));
				break;
		}
		sprite.setPosition(-w / 2 + (float) Math.random() * w, h / 2 - (float) Math.random() * h, (float) -Math.random() * 10);
		idx++;
		return sprite;
	}
}
