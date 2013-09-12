
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
import com.badlogic.gdx.utils.pathfinding.AStarPathFinder;
import com.badlogic.gdx.utils.pathfinding.NavPath;
import com.badlogic.gdx.utils.pathfinding.PathFinder;
import com.badlogic.gdx.utils.pathfinding.PathFindingContext;
import com.badlogic.gdx.utils.pathfinding.TileBasedMap;

public class PathFinderTest extends GdxTest {
	final static float width = 20;

	ShapeRenderer sr;
	SpriteBatch batch;
	BitmapFont font;
	OrthographicCamera cam = new OrthographicCamera();
	TestMap m = new TestMap();
	NavPath path = new NavPath();
	PathFinder[] pathfinder = new AStarPathFinder[] {new AStarPathFinder(m, 1000, true, AStarPathFinder.CLOSEST),
		new AStarPathFinder(m, 1000, false, AStarPathFinder.CLOSEST),
		new AStarPathFinder(m, 1000, true, AStarPathFinder.MANHATTAN),
		new AStarPathFinder(m, 1000, false, AStarPathFinder.MANHATTAN)};
	String[] pathfinderInfo = new String[] {"Heuristic: CLOSEST, AllowDiag: true\n(Click to change)",
		"Heuristic: CLOSEST, AllowDiag: false\n(Click to change)", "Heuristic: MANHATTAN, AllowDiag: true\n(Click to change)",
		"Heuristic: MANHATTAN, AllowDiag: false\n(Click to change)"};
	int activePathFinder;

	@Override
	public void create () {
		sr = new ShapeRenderer();
		font = new BitmapFont();
		batch = new SpriteBatch();
		cam.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.input.setInputProcessor(this);
	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		sr.setProjectionMatrix(cam.combined);
		sr.begin(ShapeType.Filled);
		for (int x = 0; x < m.blocked.length; x++)
			for (int y = 0; y < m.blocked[0].length; y++) {
				sr.setColor(m.blocked[x][y] ? Color.BLACK : Color.WHITE);
				sr.rect(x * width, y * width, width, width);
			}
		for (int i = 0, n = path.getLength(); i < n; i++) {
			sr.setColor(Color.RED);
			sr.rect(path.getX(i) * width, path.getY(i) * width, width, width);
		}
		sr.end();

		batch.begin();
		font.setScale(1.5f);
		font.setColor(Color.CYAN);
		font.drawMultiLine(batch, pathfinderInfo[activePathFinder], 10, 50);
		batch.end();
	}

	@Override
	public boolean mouseMoved (int screenX, int screenY) {
		findPath(screenX, screenY);
		return true;
	}

	@Override
	public boolean touchDragged (int screenX, int screenY, int pointer) {
		findPath(screenX, screenY);
		return true;
	}

	@Override
	public boolean touchUp (int screenX, int screenY, int pointer, int button) {
		activePathFinder = (activePathFinder + 1) % pathfinder.length;
		findPath(screenX, screenY);
		return true;
	}

	Vector3 tmpVec = new Vector3();

	private void findPath (int screenX, int screenY) {
		cam.unproject(tmpVec.set(screenX, screenY, 0));
		int tileX = (int)(tmpVec.x / width);
		int tileY = (int)(tmpVec.y / width);
		pathfinder[activePathFinder].findPath(null, 0, 0, tileX, tileY, path);
	}

	class TestMap implements TileBasedMap {
		boolean[][] blocked;

		public TestMap () {
			blocked = new boolean[50][50];
			for (int x = 0; x < blocked.length; x++)
				for (int y = 0; y < blocked[0].length; y++)
					// place some walls
					if ((x == 5 || x == 15) && y != 1 || x == 10 && y < 10) blocked[x][y] = true;
		}

		@Override
		public int getWidthInTiles () {
			return blocked[0].length;
		}

		@Override
		public int getHeightInTiles () {
			return blocked.length;
		}

		@Override
		public boolean blocked (PathFindingContext context, int tx, int ty) {
			return blocked[tx][ty];
		}

		@Override
		public float getCost (PathFindingContext context, int tx, int ty) {
			if (tx != context.getSourceX() && ty != context.getSourceY()) {
				return 1.44f;
			}
			return 1;
		}

	}
}
