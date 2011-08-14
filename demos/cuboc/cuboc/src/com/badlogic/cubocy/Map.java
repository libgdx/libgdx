
package com.badlogic.cubocy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.Array;

public class Map {
	static int EMPTY = 0;
	static int TILE = 0xffffff;
	static int START = 0xff0000;
	static int END = 0xff00ff;
	static int DISPENSER = 0xff0100;
	static int SPIKES = 0x00ff00;
	static int ROCKET = 0x0000ff;
	static int MOVING_SPIKES = 0xffff00;
	static int LASER = 0x00ffff;

	int[][] tiles;
	public Bob bob;
	Cube cube;
	Array<Dispenser> dispensers = new Array<Dispenser>();
	Dispenser activeDispenser = null;
	Array<Rocket> rockets = new Array<Rocket>();
	Array<MovingSpikes> movingSpikes = new Array<MovingSpikes>();
	Array<Laser> lasers = new Array<Laser>();
	public EndDoor endDoor;

	public Map () {
		Pixmap pixmap = new Pixmap(Gdx.files.internal("data/levels.png"));
		tiles = new int[pixmap.getWidth()][pixmap.getHeight()];
		for (int y = 0; y < pixmap.getHeight(); y++) {
			for (int x = 0; x < pixmap.getWidth(); x++) {
				int pix = pixmap.getPixel(x, y) >>> 8;
				if (pix == START) {
					Dispenser dispenser = new Dispenser(x, pixmap.getHeight() - 1 - y);
					dispensers.add(dispenser);
					activeDispenser = dispenser;
					bob = new Bob(this, activeDispenser.bounds.x, activeDispenser.bounds.y);
					bob.state = Bob.SPAWN;
					cube = new Cube(this, activeDispenser.bounds.x, activeDispenser.bounds.y);
					cube.state = Cube.DEAD;
				} else if (pix == DISPENSER) {
					Dispenser dispenser = new Dispenser(x, pixmap.getHeight() - 1 - y);
					dispensers.add(dispenser);
				} else if (pix == ROCKET) {
					Rocket rocket = new Rocket(this, x, pixmap.getHeight() - 1 - y);
					rockets.add(rocket);
				} else if (pix == MOVING_SPIKES) {
					movingSpikes.add(new MovingSpikes(this, x, pixmap.getHeight() - 1 - y));
				} else if (pix == LASER) {
					lasers.add(new Laser(this, x, pixmap.getHeight() - 1 - y));
				} else if (pix == END) {
					endDoor = new EndDoor(x, pixmap.getHeight() - 1 - y);
				} else {
					tiles[x][y] = pix;
				}
			}
		}

		for (int i = 0; i < movingSpikes.size; i++) {
			movingSpikes.get(i).init();
		}
		for (int i = 0; i < lasers.size; i++) {
			lasers.get(i).init();
		}
	}

	public void update (float deltaTime) {
		bob.update(deltaTime);
		if (bob.state == Bob.DEAD) bob = new Bob(this, activeDispenser.bounds.x, activeDispenser.bounds.y);
		cube.update(deltaTime);
		if (cube.state == Cube.DEAD) cube = new Cube(this, bob.bounds.x, bob.bounds.y);
		for (int i = 0; i < dispensers.size; i++) {
			if (bob.bounds.overlaps(dispensers.get(i).bounds)) {
				activeDispenser = dispensers.get(i);
			}
		}
		for (int i = 0; i < rockets.size; i++) {
			Rocket rocket = rockets.get(i);
			rocket.update(deltaTime);
		}
		for (int i = 0; i < movingSpikes.size; i++) {
			MovingSpikes spikes = movingSpikes.get(i);
			spikes.update(deltaTime);
		}
		for (int i = 0; i < lasers.size; i++) {
			lasers.get(i).update();
		}
	}

	public boolean isDeadly (int tileId) {
		return tileId == SPIKES;
	}
}
