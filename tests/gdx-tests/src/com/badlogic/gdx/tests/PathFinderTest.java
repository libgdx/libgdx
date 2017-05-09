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

package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.pathfinding.AStarPathFinder;
import com.badlogic.gdx.ai.pathfinding.NavContext;
import com.badlogic.gdx.ai.pathfinding.NavGraph;
import com.badlogic.gdx.ai.pathfinding.PathFinder;
import com.badlogic.gdx.ai.pathfinding.tiled.ManhattanDistance;
import com.badlogic.gdx.ai.pathfinding.tiled.NavNodeTileBased;
import com.badlogic.gdx.ai.pathfinding.tiled.NavPathTileBased;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.tests.utils.GdxTest;

/** This test shows how the A* implementation of a PathFinder is used on a tiled map with no diagonal movement. It shows
 * pathfinding for a WALKING and a FLYING mover.
 * @author hneuer */
public class PathFinderTest extends GdxTest {
	static final String[] MOVERS = {"WALKING", "FLYING"};
	final static float width = 20;
	ShapeRenderer sr;
	OrthographicCamera cam = new OrthographicCamera();
	Vector3 tmpUnprojection = new Vector3();

	int lastScreenX;
	int lastScreenY;

	Skin skin;
	Stage stage;
	Label infoLabel;

	TiledTestMap m = new TiledTestMap();
	NavPathTileBased path = new NavPathTileBased();
	PathFinder pathfinder = new AStarPathFinder(m, 100, new ManhattanDistance());

	int currentMover = 0;
	String mover;

	@Override
	public void create () {
		skin = new Skin(Gdx.files.internal("data/uiskin.json"));
		stage = new Stage();

		mover = MOVERS[currentMover];
		infoLabel = new Label(mover + " mover (press 'm' key to change)", skin);
		infoLabel.setColor(Color.CYAN);

		stage.addActor(infoLabel);

		sr = new ShapeRenderer();
		cam.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.input.setInputProcessor(this);
	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		sr.setProjectionMatrix(cam.combined);
		sr.begin(ShapeType.Filled);
		for (int x = 0; x < m.tiles.length; x++)
			for (int y = 0; y < m.tiles[0].length; y++) {
				sr.setColor(m.blocked[x][y] ? Color.BLACK : Color.WHITE);
				sr.rect(x * width, y * width, width, width);
			}

		Color c = "WALKING".equals(mover) ? Color.RED : Color.BLUE;
		for (int i = 0, n = path.x.size; i < n; i++) {
			sr.setColor(c);
			sr.rect(path.x.get(i) * width, path.y.get(i) * width, width, width);
		}
		sr.end();

		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();
	}

	@Override
	public boolean keyTyped (char character) {
		if (character == 'm') {
			mover = MOVERS[++currentMover % 2];
			infoLabel.setText(mover + " mover (press 'm' key to change)");
			updatePath();
		}
		return true;
	}

	@Override
	public void resize (int width, int height) {
		stage.getViewport().update(width, height, true);
	}

	@Override
	public boolean mouseMoved (int screenX, int screenY) {
		lastScreenX = screenX;
		lastScreenY = screenY;
		updatePath();
		return true;
	}

	private void updatePath () {
		cam.unproject(tmpUnprojection.set(lastScreenX, lastScreenY, 0));
		int tileX = (int)(tmpUnprojection.x / width);
		int tileY = (int)(tmpUnprojection.y / width);
		path.clear();
		pathfinder.findPath(mover, m.tiles[0][0], m.tiles[tileX][tileY], path);
	}

	/** An implementation of a navigation graph simply backed by a two dimensional array representing a tiled map. */
	class TiledTestMap implements NavGraph<NavNodeTileBased> {
		final int size = 50;
		NavNodeTileBased[][] tiles;
		boolean[][] blocked;

		public TiledTestMap () {
			tiles = new NavNodeTileBased[size][size];
			blocked = new boolean[size][size];
			for (int x = 0; x < tiles.length; x++)
				for (int y = 0; y < tiles[0].length; y++) {
					tiles[x][y] = new NavNodeTileBased(x, y);

					// block some tiles to simulate walls on the map
					blocked[x][y] = (x == 5 || x == 15) && y != 1 || x == 10 && y < 10;
				}

			// Each node has up to 4 neighbors, therefore no diagonal movement is possible
			for (int x = 0; x < tiles.length; x++)
				for (int y = 0; y < tiles[0].length; y++) {
					if (x > 0) tiles[x][y].neighbors.add(tiles[x - 1][y]);
					if (y > 0) tiles[x][y].neighbors.add(tiles[x][y - 1]);
					if (x < tiles.length - 1) tiles[x][y].neighbors.add(tiles[x + 1][y]);
					if (y < tiles[0].length - 1) tiles[x][y].neighbors.add(tiles[x][y + 1]);
				}
		}

		/** Uses a the boolean array to check if a given tile is blocked. */
		@Override
		public boolean blocked (NavContext<NavNodeTileBased> context, NavNodeTileBased targetNode) {
			if ("WALKING".equals(context.getMover())) return blocked[targetNode.x][targetNode.y];

			// for FLYING nothing will be blocked
			return false;
		}

		/** Calculates the cost to go from source to target node. Straight movement costs 1, diagonal 1.44. */
		@Override
		public float getCost (NavContext<NavNodeTileBased> context, NavNodeTileBased targetNode) {
			return targetNode.x != context.getSourceNode().x && targetNode.y != context.getSourceNode().y ? 1.44f : 1;
		}
	}
}
