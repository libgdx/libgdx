package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureAdapter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.tests.utils.GdxTest;

/**
 * A simple example of how to use a y-down coordinate system.
 * @author mzechner
 *
 */
public class YDownTest extends GdxTest {
	SpriteBatch batch;
	BitmapFont font;
	TextureRegion region;
	TextureAtlas atlas;
	Stage stage;
	MyActor image;
	OrthographicCamera camera;
	
	@Override
	public void create () {
		// a bitmap font to draw some text, note that we
		// pass true to the constructor, which flips glyphs on y
		font = new BitmapFont(true);
		
		// a texture region, note the flipping on y again
		region = new TextureRegion(new Texture("data/badlogic.jpg"));
		region.flip(false, true);
		
		// a texture atlas, note the boolean
		atlas = new TextureAtlas(Gdx.files.internal("data/pack"), true);
		
		// a sprite batch with which we want to render
		batch = new SpriteBatch();
		
		// a camera, note the setToOrtho call, which will set the y-axis
		// to point downwards
		camera = new OrthographicCamera();
		camera.setToOrtho(true);
		
		// a stage which uses our y-down camera and a simple actor (see MyActor below), 
		// which uses the flipped region. The key here is to
		// set our y-down camera on the stage, the rest is just for demo purposes.
		stage = new Stage(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
		stage.setCamera(camera);
		image = new MyActor(region);
		image.x = 100;
		image.y = 100;
		stage.addActor(image);
		
		// finally we write up the stage as the input process and call it a day.
		Gdx.input.setInputProcessor(stage);
	}

	@Override
	public void resize (int width, int height) {
		// handling resizing is simple, just set the camera to ortho again
		camera.setToOrtho(true, width, height);
	}

	@Override
	public void render () {
		// clear the screen, update the camera and make the sprite batch
		// use its matrices.
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		camera.update();
		batch.setProjectionMatrix(camera.combined);
		
		// render all the things, we render in a y-down
		// cartesian coordinate system
		batch.begin();
		// drawing a region, x and y will be the top left corner of the region, would be bottom left
		// with y-up.
		batch.draw(region, 20, 100);
		// drawing text, x and y will be the top left corner for text, same as with y-up 
		font.draw(batch, "This is a test", 270, 100);
		// drawing regions from an atlas, x and y will be the top left corner.
		// you shouldn't call findRegion every frame, cache the result. 
		batch.draw(atlas.findRegion("badlogicsmall"), 360, 100);
		// finally we draw our current touch/mouse coordinates
		font.draw(batch, Gdx.input.getX() + ", " + Gdx.input.getY(), 0, 0);
		batch.end();
		
		// tell the stage to act and draw itself
		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();
	}
	
	/**
	 * A very simple actor implementation that does not obey rotation/scale/origin
	 * set on the actor. Allows dragging of the actor.
	 * @author mzechner
	 *
	 */
	public class MyActor extends Actor {
		TextureRegion region;
		float lastX;
		float lastY;
		
		public MyActor(TextureRegion region) {
			this.region = region;
			this.width = region.getRegionWidth();
			// note that the height of the region will be negative since 
			// we flipped it, hence the minus sign. This is the only
			// portion of the code of MyActor that is dependend on
			// whether y points up or down!
			this.height = -region.getRegionHeight();
		}
		
		@Override
		public void draw (SpriteBatch batch, float parentAlpha) {
			batch.draw(region, x, y);
		}
		
		@Override
		public boolean touchDown (float x, float y, int pointer) {
			// we only care for the first finger to make things easier
			if(pointer != 0) return false;
			
			// record the coordinates the finger went down on. they
			// are given relative to the actor's upper left corner (0, 0)
			lastX = x;
			lastY = y;
			return true;
		}

		@Override
		public void touchDragged (float x, float y, int pointer) {
			//we only care for the first finger to make things easier
			if(pointer != 0) return;
			
			// adjust the actor's position by (current mouse position - last mouse position)
			// in the actor's coordinate system.
			this.x += (x - lastX);
			this.y += (y - lastY);
			
			// save the current mouse position as the basis for the next drag event.
			// we adjust by the same delta so next time drag is called, lastX/lastY
			// are in the actor's local coordinate system automatically.
			lastX = x - (x - lastX);
			lastY = y - (y - lastY);
		}

		@Override
		public Actor hit (float x, float y) {
			// coordinates are passed in relative to the actor's upper left
			// corner (0, 0)
			Actor result = x > 0 && x < width && y > 0 && y < height ? this : null;
			return result;
		}
	}
}
