package com.badlogic.gdx.tests.superkoalio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

/**
 * Super Mario Brothers like platformer, using a tile map build
 * via <a href="http://www.mapeditor.org/>Tiled</a> and a tileset
 * and sprites by <a href="http://www.vickiwenderlich.com/">Vicky Wenderlich</a>
 * @author mzechner
 *
 */
public class SuperKoalio extends GdxTest {
	
	private static class Koala {
		private static float WIDTH;
		private static float HEIGHT;
		private static float MAX_VELOCITY = 10f;
		private static float WALK_VELOCITY = 2f;
		private static float JUMP_VELOCITY = 40f;
		private static float DAMPING = 0.87f;
		
		enum State {
			Standing,
			Walking,
			Jumping
		}
		
		final Vector2 position = new Vector2();
		final Vector2 velocity = new Vector2();
		State state = State.Walking;
		float stateTime = 0;
		boolean facesRight = true;
		boolean grounded = false;
	}
	
	private TiledMap map;
	private OrthogonalTiledMapRenderer renderer;
	private OrthographicCamera camera;
	private Texture koalaTexture;
	private Animation stand;
	private Animation walk;
	private Animation jump;
	private Koala koala;
	private Pool<Rectangle> rectPool = new Pool<Rectangle>() {
		@Override
		protected Rectangle newObject () {
			return new Rectangle();
		}
	};
	private Array<Rectangle> tiles = new Array<Rectangle>();
	
	private static final float GRAVITY = -2.5f;
	
	@Override
	public void create () {
		// load the koala frames, split them, and assign them to Animations
		koalaTexture = new Texture("data/maps/tiled/super-koalio/koalio.png"); 
		TextureRegion[] regions = TextureRegion.split(koalaTexture, 18, 26)[0];
		stand = new Animation(0, regions[0]);
		jump = new Animation(0, regions[1]);
		walk = new Animation(0.15f, regions[2], regions[3], regions[4]);
		walk.setPlayMode(Animation.LOOP_PINGPONG);
		
		// figure out the width and height of the koala for collision
		// detection and rendering by converting a koala frames pixel
		// size into world units (1 unit == 16 pixels)
		Koala.WIDTH = 1 / 16f * regions[0].getRegionWidth();
		Koala.HEIGHT = 1 / 16f * regions[0].getRegionHeight();
		
		// load the map, set the unit scale to 1/16 (1 unit == 16 pixels)
		map = new TmxMapLoader().load("data/maps/tiled/super-koalio/level1.tmx");
		renderer = new OrthogonalTiledMapRenderer(map, 1 / 16f);
		
		// create an orthographic camera, shows us 30x20 units of the world
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 30, 20);
		camera.update();
		
		// create the Koala we want to move around the world
		koala = new Koala();
		koala.position.set(10, 2);
	}

	@Override
	public void render () {
		// clear the screen
		Gdx.gl.glClearColor(0.7f, 0.7f, 1.0f, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		
		// get the delta time
		float deltaTime = Gdx.graphics.getDeltaTime();
		
		// update the koala (process input, collision detection, position update)
		updateKoala(deltaTime);
		
		// let the camera follow the koala, x-axis only
		camera.position.x = koala.position.x;
		camera.update();
		
		// set the tile map rendere view based on what the
		// camera sees and render the map
		renderer.setView(camera);
		renderer.render();
		
		// render the koala
		renderKoala(deltaTime);
	}
	
	private void updateKoala(float deltaTime) {
		koala.stateTime += deltaTime;
		
		// check input and apply to velocity & state
		if(Gdx.input.isKeyPressed(Keys.SPACE) & koala.grounded) {
			koala.velocity.y += Koala.JUMP_VELOCITY;
			koala.state = Koala.State.Jumping;
			koala.grounded = false;
		}
		
		if(Gdx.input.isKeyPressed(Keys.LEFT) || Gdx.input.isKeyPressed(Keys.A)) {
			koala.velocity.x -= Koala.WALK_VELOCITY;
			if(koala.state != Koala.State.Jumping) koala.state = Koala.State.Walking;
			koala.facesRight = false;
		}
		
		if(Gdx.input.isKeyPressed(Keys.RIGHT) || Gdx.input.isKeyPressed(Keys.D)) {
			koala.velocity.x += Koala.WALK_VELOCITY;
			if(koala.state != Koala.State.Jumping) koala.state = Koala.State.Walking;
			koala.facesRight = true;
		}
		
		// apply gravity if we are falling
		koala.velocity.add(0, GRAVITY);
		
		// multiply by delta time so we know how far we go
		// in this frame
		koala.velocity.mul(deltaTime);
		
		// perform collision detection & response based on the current
		// position and velocity. Collision detection will clamp the
		// velocity if necessary and move the koala out of tiles
		Rectangle koalaRect = rectPool.obtain();
		System.out.println("frame");
		koalaRect.set(koala.position.x, koala.position.y, Koala.WIDTH, Koala.HEIGHT);
		
		koalaRect.y += koala.velocity.y;
		for (Rectangle rect: getCollidingTiles(koala.position, 0, koala.velocity.y)) {
			if (koalaRect.overlaps(rect)) {
				System.out.println("coll y");
				if (koala.velocity.y < 0) {
					koalaRect.y = rect.y + rect.height + 0.001f;
					koala.grounded = true;
				} else if(koala.velocity.y > 0 ){
					koalaRect.y = rect.y - rect.height - 0.001f;
				}
				koala.velocity.y = 0;
			}
		}
		koala.position.set(koalaRect.x, koalaRect.y);

		koalaRect.x += koala.velocity.x;
		for (Rectangle rect: getCollidingTiles(koala.position, koala.velocity.x, 0)) {
			if (koalaRect.overlaps(rect)) {
				System.out.println("coll x");
				if (koala.velocity.x < 0) {
					koalaRect.x = rect.x + rect.width + 0.001f;
				} else if(koala.velocity.x > 0){
					koalaRect.x = rect.x - rect.width - 0.001f;
				}
				koala.velocity.x = 0;
				koala.state = Koala.State.Standing;
			}
		}
		koala.position.set(koalaRect.x, koalaRect.y);

		// unscale the velocity by the inverse delta time and set 
		// the latest position
		koala.velocity.mul(1/deltaTime);
		
		// free the koala rectangle
		rectPool.free(koalaRect);
		
		// Apply damping to the velocity on the x-axis so we don't
		// walk infinitely once a key was pressed
		koala.velocity.x *= Koala.DAMPING;
	}
	
	private Array<Rectangle> getCollidingTiles(Vector2 position, float velocityX, float velocityY) {
		// figure out the tiles within which the koala is moving in the current timestep
		int startX, startY, endX, endY;
		if(velocityX < 0) startX = (int)(position.x + velocityX);
		else startX = (int)position.x;
		if(velocityY < 0) startY = (int)(position.y + velocityY);
		else startY = (int)position.y;
		endX = (int)(startX + Koala.WIDTH + Math.abs(velocityX));
		endY = (int)(startY + Koala.HEIGHT + Math.abs(velocityY));
		
		// get all the tiles that overlap this area
		TiledMapTileLayer layer = (TiledMapTileLayer)map.getLayers().getLayer(1);
		rectPool.freeAll(tiles);
		tiles.clear();
		for(int y = startY; y <= endY; y++) {
			for(int x = startX; x <= endX; x++) {
				Cell cell = layer.getCell(x, y);
				if(cell.getTile() != null) {
					Rectangle rect = rectPool.obtain();
					rect.set(x, y, 1, 1);
					tiles.add(rect);
				}
			}
		}
		return tiles;
	}
	
	private void renderKoala(float deltaTime) {
		// based on the koala state, get the animation frame
		TextureRegion frame = null;
		switch(koala.state) {
			case Standing: frame = stand.getKeyFrame(koala.stateTime); break;
			case Walking: frame = walk.getKeyFrame(koala.stateTime); break;
			case Jumping: frame = jump.getKeyFrame(koala.stateTime); break; 
		}
		
		// draw the koala, depending on the current velocity
		// on the x-axis, draw the koala facing either right
		// or left
		SpriteBatch batch = renderer.getSpriteBatch();
		batch.begin();
		if(koala.facesRight) {
			batch.draw(frame, koala.position.x, koala.position.y, Koala.WIDTH, Koala.HEIGHT);
		} else {
			batch.draw(frame, koala.position.x + Koala.WIDTH, koala.position.y, -Koala.WIDTH, Koala.HEIGHT);
		}
		batch.end();
	}

	@Override
	public void dispose () {
	}
}
