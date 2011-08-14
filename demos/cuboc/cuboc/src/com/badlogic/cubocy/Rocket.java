
package com.badlogic.cubocy;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Rocket {
	static final int FLYING = 0;
	static final int EXPLODING = 1;
	static final int DEAD = 2;
	static final float VELOCITY = 6;

	Map map;
	float stateTime = 0;
	int state = FLYING;
	Vector2 startPos = new Vector2();
	Vector2 pos = new Vector2();
	Vector2 vel = new Vector2();
	Rectangle bounds = new Rectangle();

	public Rocket (Map map, float x, float y) {
		this.map = map;
		this.startPos.set(x, y);
		this.pos.set(x, y);
		this.bounds.x = x + 0.2f;
		this.bounds.y = y + 0.2f;
		this.bounds.width = 0.6f;
		this.bounds.height = 0.6f;
		this.vel.set(-VELOCITY, 0);
	}

	public void update (float deltaTime) {
		if (state == FLYING) {
// if(pos.dst(map.bob.pos) < pos.dst(map.cube.pos)) vel.set(map.bob.pos);
// else vel.set(map.cube.pos);
			vel.set(map.bob.pos);
			vel.sub(pos).nor().mul(VELOCITY);
			pos.add(vel.x * deltaTime, vel.y * deltaTime);
			bounds.x = pos.x + 0.2f;
			bounds.y = pos.y + 0.2f;
			if (checkHit()) {
				state = EXPLODING;
				stateTime = 0;
			}
		}

		if (state == EXPLODING) {
			if (stateTime > 0.6f) {
				state = FLYING;
				stateTime = 0;
				pos.set(startPos);
				bounds.x = pos.x + 0.2f;
				bounds.y = pos.y + 0.2f;
			}
		}

		stateTime += deltaTime;
	}

	Rectangle[] r = {new Rectangle(), new Rectangle(), new Rectangle(), new Rectangle()};

	private boolean checkHit () {
		fetchCollidableRects();
		for (int i = 0; i < r.length; i++) {
			if (bounds.overlaps(r[i])) {
				return true;
			}
		}

		if (bounds.overlaps(map.bob.bounds)) {
			if (map.bob.state != Bob.DYING) {
				map.bob.state = Bob.DYING;
				map.bob.stateTime = 0;
			}
			return true;
		}

		if (bounds.overlaps(map.cube.bounds)) {
			return true;
		}

		return false;
	}

	private void fetchCollidableRects () {
		int p1x = (int)bounds.x;
		int p1y = (int)Math.floor(bounds.y);
		int p2x = (int)(bounds.x + bounds.width);
		int p2y = (int)Math.floor(bounds.y);
		int p3x = (int)(bounds.x + bounds.width);
		int p3y = (int)(bounds.y + bounds.height);
		int p4x = (int)bounds.x;
		int p4y = (int)(bounds.y + bounds.height);

		int[][] tiles = map.tiles;
		int tile1 = tiles[p1x][map.tiles[0].length - 1 - p1y];
		int tile2 = tiles[p2x][map.tiles[0].length - 1 - p2y];
		int tile3 = tiles[p3x][map.tiles[0].length - 1 - p3y];
		int tile4 = tiles[p4x][map.tiles[0].length - 1 - p4y];

		if (tile1 != Map.EMPTY)
			r[0].set(p1x, p1y, 1, 1);
		else
			r[0].set(-1, -1, 0, 0);
		if (tile2 != Map.EMPTY)
			r[1].set(p2x, p2y, 1, 1);
		else
			r[1].set(-1, -1, 0, 0);
		if (tile3 != Map.EMPTY)
			r[2].set(p3x, p3y, 1, 1);
		else
			r[2].set(-1, -1, 0, 0);
		if (tile4 != Map.EMPTY)
			r[3].set(p4x, p4y, 1, 1);
		else
			r[3].set(-1, -1, 0, 0);
	}
}
