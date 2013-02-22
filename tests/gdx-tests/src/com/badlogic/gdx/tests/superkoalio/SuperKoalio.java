package com.badlogic.gdx.tests.superkoalio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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
	
	private Vector2 tmp = new Vector2();
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
		Rectangle intersect = rectPool.obtain();
		tmp.set(koala.position).add(koala.velocity);
		
		koalaRect.set(tmp.x, tmp.y, Koala.WIDTH, Koala.HEIGHT);
		getSurroundingTilesForPosition(tmp.set(koala.position).add(Koala.WIDTH / 2, Koala.HEIGHT / 2), (TiledMapTileLayer) map.getLayers().getLayer(1));
		
		TileData item = null;
		
		item = surroundingTiles[UP];
		if (item.tile != null) {
			if (intersect(koalaRect, item.rectangle, intersect)) {				
				koalaRect.y -= intersect.height;
				koala.velocity.y = 0;
			}
		}
		item = surroundingTiles[DOWN];
		if (item.tile != null) {
			if (intersect(koalaRect, item.rectangle, intersect)) {
				koalaRect.y += intersect.height;
				koala.velocity.y = 0;
				koala.grounded = true;
			}	
		}
		item = surroundingTiles[LEFT];
		if (item.tile != null) {
			if (intersect(koalaRect, item.rectangle, intersect)) {
				koalaRect.x += intersect.width;
			}
		}
		item = surroundingTiles[RIGHT];
		if (item.tile != null) {
			if (intersect(koalaRect, item.rectangle, intersect)) {
				koalaRect.x -= intersect.width;
			}			
		}
		item = surroundingTiles[UP_LEFT];
		if (item.tile != null) {
			if (intersect(koalaRect, item.rectangle, intersect)) {
				if (intersect.width > intersect.height) {
					koalaRect.y -= intersect.height;
					koala.velocity.y = 0;
				} else {
					koalaRect.x += intersect.width;
				}
			}			
		}
		item = surroundingTiles[UP_RIGHT];
		if (item.tile != null) {
			if (intersect(koalaRect, item.rectangle, intersect)) {
				if (intersect.width > intersect.height) {
					koalaRect.y -= intersect.height;
					koala.velocity.y = 0;
				} else {
					koalaRect.x -= intersect.width;
				}
			}			
		}
		item = surroundingTiles[DOWN_LEFT];
		if (item.tile != null) {
			if (intersect(koalaRect, item.rectangle, intersect)) {
				if (intersect.width > intersect.height) {
					koalaRect.y += intersect.height;
				} else {
					koalaRect.x += intersect.width;
				}
			}			
		}
		item = surroundingTiles[DOWN_RIGHT];
		if (item.tile != null) {
			if (intersect(koalaRect, item.rectangle, intersect)) {
				if (intersect.width > intersect.height) {
					koalaRect.y += intersect.height;
				} else {
					koalaRect.x -= intersect.width;
				}
			}
		}
		koala.position.set(koalaRect.x, koalaRect.y);
		
		// unscale the velocity by the inverse delta time and set 
		// the latest position
		koala.velocity.mul(1/deltaTime);
		
		// free the koala rectangle
		rectPool.free(koalaRect);
		rectPool.free(intersect);
		
		// Apply damping to the velocity on the x-axis so we don't
		// walk infinitely once a key was pressed
		koala.velocity.x *= Koala.DAMPING;
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
	
	public static final int UP = 1;
	public static final int DOWN = 6;
	public static final int LEFT = 3;
	public static final int RIGHT = 4;
	public static final int UP_LEFT = 0;
	public static final int UP_RIGHT = 2;
	public static final int DOWN_LEFT = 5;
	public static final int DOWN_RIGHT = 7;
	
	TileData[] surroundingTiles = new TileData[] {
		new TileData(), new TileData(), new TileData(),
		new TileData(), /*  KOALIO  */  new TileData(),
		new TileData(), new TileData(), new TileData(),
	};
	
	public static class TileData {
		TiledMapTile tile;
		int tileX;
		int tileY;
		
		Rectangle rectangle;
		
		public TileData() {
			tile = null;
			tileX = -1;
			tileY = -1;
			rectangle = new Rectangle();
		}
		
		public void reset() {
			tile = null;
			tileX = -1;
			tileY = -1;
			rectangle.set(0, 0, 0, 0);
		}
		
	}
	private void getSurroundingTilesForPosition(Vector2 position, TiledMapTileLayer layer) {
		int tileX = (int) position.x;
		int tileY = (int) position.y;
		
		for (int i = 0, j = surroundingTiles.length; i < j; i++) {
			surroundingTiles[i].reset();
		}
		
		boolean goUp = tileY > 0;
		boolean goDown = tileY < layer.getHeight() - 1;
		boolean goLeft = tileX > 0;
		boolean goRight = tileX < layer.getWidth() - 1;

		TileData item = null;
		
		if (goUp) {
			item = surroundingTiles[UP];
			
			item.tileX = tileX;
			item.tileY = tileY + 1;
			
			item.tile = layer.getCell(item.tileX, item.tileY).getTile();
			item.rectangle.set(item.tileX, item.tileY, 1, 1);
			
			if (goLeft) {
				item = surroundingTiles[UP_LEFT];
				item.tileX = tileX - 1;
				item.tileY = tileY + 1;
				item.tile = layer.getCell(item.tileX, item.tileY).getTile();
				item.rectangle.set(item.tileX, item.tileY, 1, 1);
			}
			if (goRight) {
				item = surroundingTiles[UP_RIGHT];
				item.tileX = tileX + 1;
				item.tileY = tileY + 1;
				item.tile = layer.getCell(item.tileX, item.tileY).getTile();
				item.rectangle.set(item.tileX, item.tileY, 1, 1);
			}			
		}
		if (goDown) {
			item = surroundingTiles[DOWN];
			item.tileX = tileX;
			item.tileY = tileY - 1;
			item.tile = layer.getCell(item.tileX, item.tileY).getTile();
			item.rectangle.set(item.tileX, item.tileY, 1, 1);
			if (goLeft) {
				item = surroundingTiles[DOWN_LEFT];
				item.tileX = tileX - 1;
				item.tileY = tileY - 1;
				item.tile = layer.getCell(item.tileX, item.tileY).getTile();
				item.rectangle.set(item.tileX, item.tileY, 1, 1);
			}
			if (goRight) {
				item = surroundingTiles[DOWN_RIGHT];
				item.tileX = tileX + 1;
				item.tileY = tileY - 1;
				item.tile = layer.getCell(item.tileX, item.tileY).getTile();
				item.rectangle.set(item.tileX, item.tileY, 1, 1);
			}
		}
		if (goLeft) {
			item = surroundingTiles[LEFT];
			item.tileX = tileX - 1;
			item.tileY = tileY;
			item.tile = layer.getCell(item.tileX, item.tileY).getTile();
			item.rectangle.set(item.tileX, item.tileY, 1, 1);
		}
		if (goRight) {
			item = surroundingTiles[RIGHT];
			item.tileX = tileX + 1;
			item.tileY = tileY;
			item.tile = layer.getCell(item.tileX, item.tileY).getTile();
			item.rectangle.set(item.tileX, item.tileY, 1, 1);
		}
	}

	public static boolean intersect (Rectangle r1, Rectangle r2, Rectangle out) {
		final float r1L = r1.x;
		final float r1T = r1.y;

		final float r1R = r1.x + r1.width;
		final float r1B = r1.y + r1.height;

		final float r2L = r2.x;
		final float r2T = r2.y;

		final float r2R = r2.x + r2.width;
		final float r2B = r2.y + r2.height;

		if (r1L < r2R && r2L < r1R && r1T < r2B && r2T < r1B) {
			if (out != null) {
				final float outL = (r1L > r2L) ? r1L : r2L;
				final float outT = (r1T > r2T) ? r1T : r2T;
				final float outR = (r1R < r2R) ? r1R : r2R;
				final float outB = (r1B < r2B) ? r1B : r2B;

				out.set(outL, outT, outR - outL, outB - outT);
			}
			return true;
		}
		return false;
	}
}
