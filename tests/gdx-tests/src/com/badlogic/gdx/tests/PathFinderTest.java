
package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.pathfinding.AStarPathFinder;
import com.badlogic.gdx.utils.pathfinding.AStarPathFinder.AStarHeuristicCalculator;
import com.badlogic.gdx.utils.pathfinding.NavContext;
import com.badlogic.gdx.utils.pathfinding.NavGraph;
import com.badlogic.gdx.utils.pathfinding.NavPath;
import com.badlogic.gdx.utils.pathfinding.PathFinder;
import com.badlogic.gdx.utils.pathfinding.tiled.ManhattanDistance;
import com.badlogic.gdx.utils.pathfinding.tiled.NavNodeTileBased;
import com.badlogic.gdx.utils.pathfinding.tiled.NavPathTileBased;

public class PathFinderTest extends GdxTest {
	final static float width = 20;
	ShapeRenderer sr;
	OrthographicCamera cam = new OrthographicCamera();
	Vector3 tmpUnprojection = new Vector3();

	TiledTestMap m = new TiledTestMap();
	NavPathTileBased path = new NavPathTileBased();
	PathFinder pathfinder = new AStarPathFinder(m, 100, new ManhattanDistance());

	@Override
	public void create () {
		sr = new ShapeRenderer();
		cam.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.input.setInputProcessor(this);
	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		sr.setProjectionMatrix(cam.combined);
		sr.begin(ShapeType.Filled);
		for (int x = 0; x < m.tiles.length; x++)
			for (int y = 0; y < m.tiles[0].length; y++) {
				sr.setColor(m.blocked[x][y] ? Color.BLACK : Color.WHITE);
				sr.rect(x * width, y * width, width, width);
			}
		for (int i = 0, n = path.x.size; i < n; i++) {
			sr.setColor(Color.RED);
			sr.rect(path.x.get(i) * width, path.y.get(i) * width, width, width);
		}
		sr.end();
	}

	@Override
	public boolean mouseMoved (int screenX, int screenY) {
		cam.unproject(tmpUnprojection.set(screenX, screenY, 0));
		int tileX = (int)(tmpUnprojection.x / width);
		int tileY = (int)(tmpUnprojection.y / width);
		path.clear();
		pathfinder.findPath(null, m.tiles[0][0], m.tiles[tileX][tileY], path);
		return true;
	}

	class TiledTestMap implements NavGraph<NavNodeTileBased> {
		final int size = 50;
		NavNodeTileBased[][] tiles; // hold them in array for easier access in render method
		boolean[][] blocked;

		public TiledTestMap () {
			tiles = new NavNodeTileBased[size][size];
			blocked = new boolean[size][size];
			for (int x = 0; x < tiles.length; x++)
				for (int y = 0; y < tiles[0].length; y++) {
					tiles[x][y] = new NavNodeTileBased(x, y);
					blocked[x][y] = (x == 5 || x == 15) && y != 1 || x == 10 && y < 10;
				}

			// setup each node's parents
			for (int x = 0; x < tiles.length; x++)
				for (int y = 0; y < tiles[0].length; y++) {
					if (x > 0) tiles[x][y].neighbors.add(tiles[x - 1][y]);
					if (y > 0) tiles[x][y].neighbors.add(tiles[x][y - 1]);
					if (x < tiles.length - 1) tiles[x][y].neighbors.add(tiles[x + 1][y]);
					if (y < tiles[0].length - 1) tiles[x][y].neighbors.add(tiles[x][y + 1]);
				}
		}

		/** Uses a simple boolean array to check if a given tile is blocked. */
		@Override
		public boolean blocked (NavContext<NavNodeTileBased> context, NavNodeTileBased targetNode) {
			return blocked[targetNode.x][targetNode.y];
		}

		/** Calculates the cost to go from source to target node. Straight movement costs 1, diagonal 1.44. */
		@Override
		public float getCost (NavContext<NavNodeTileBased> context, NavNodeTileBased targetNode) {
			return targetNode.x != context.getSourceNode().x && targetNode.y != context.getSourceNode().y ? 1.44f : 1;
		}
	}
}
