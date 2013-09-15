package de.swagner.paxbritannica.help;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;

import de.swagner.paxbritannica.DefaultScreen;
import de.swagner.paxbritannica.GameInstance;
import de.swagner.paxbritannica.Resources;
import de.swagner.paxbritannica.background.BackgroundFXRenderer;
import de.swagner.paxbritannica.mainmenu.MainMenu;

public class Help extends DefaultScreen implements InputProcessor {
	
	Sprite back;
	
	BoundingBox collisionBack = new BoundingBox();
	BoundingBox collisionMusic = new BoundingBox();

	Sprite fighter;
	Sprite bomber;
	Sprite frigate;
	Sprite upgrade;
	
	BackgroundFXRenderer backgroundFX = new BackgroundFXRenderer();
	Sprite blackFade;

	OrthographicCamera cam;
	
	BitmapFont font;
	
	SpriteBatch titleBatch;
	SpriteBatch fadeBatch;
	
	Ray collisionRay;

	boolean finished = false;
	
	float time = 0;
	float fade = 1.0f;

	private int width = 800;
	private int height = 480;

	public Help(Game game) {
		super(game);
		Gdx.input.setCatchBackKey( true );
		Gdx.input.setInputProcessor(this);
	}

	@Override
	public void show() {		
		GameInstance.getInstance().resetGame();
		
		blackFade = Resources.getInstance().blackFade;
		
		back = Resources.getInstance().back;
		back.setPosition(20, 010);
		back.setColor(1,1,1,0.5f);
		collisionBack.set(new Vector3(back.getVertices()[0], back.getVertices()[1], -10),new Vector3(back.getVertices()[10], back.getVertices()[11], 10));
		
		fighter = Resources.getInstance().fighterOutline;
		fighter.setRotation(0);
		bomber = Resources.getInstance().bomberOutline;
		bomber.setRotation(0);
		frigate = Resources.getInstance().frigateOutline;
		frigate.setRotation(0);
		upgrade = Resources.getInstance().upgradeOutline;
		upgrade.setRotation(0);
		
		titleBatch = new SpriteBatch();
		titleBatch.getProjectionMatrix().setToOrtho2D(0, 0, 800, 480);
		fadeBatch = new SpriteBatch();
		fadeBatch.getProjectionMatrix().setToOrtho2D(0, 0, 2, 2);
		
		font = new BitmapFont();
		font.getRegion().getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		time += delta;

		if (time < 1f)
			return;

		backgroundFX.render();

		titleBatch.begin();
		
		back.draw(titleBatch);
		
		font.draw(titleBatch, "Pax Britannica is a one-touch real-time strategy game by No Fun Games.", 90, 460);
		font.draw(titleBatch, "Two players battle it out underwater, struggling to be the last one standing!", 90, 440);
		font.draw(titleBatch, "Select your ship to start a battle against the computer. Select two ships to start a local multiplayer", 90, 400);
		font.draw(titleBatch, "battle. Touching your play area spins the needle on the radial menu in the middle of the player's", 90, 380);
		font.draw(titleBatch, "factory ship.The needle will only travel as far as the player's current resources allow. Resources ", 90, 360);
		font.draw(titleBatch, "(gold? seaweed? who knows!) accumulate over time.", 90, 340);
		font.draw(titleBatch, "Releasing creates a ship that corresponds to the quadrant that the needle is pointing at.", 90, 300);
		font.draw(titleBatch, "Fighter: Small, fast and cheap. Great at chasing down bombers.", 130, 260);
		fighter.setPosition(70, 215);
		fighter.draw(titleBatch);
		font.draw(titleBatch, "Bomber: Shoots slow projectiles that do massive damage to frigates or enemy factory ships!", 130, 220);
		bomber.setPosition(70, 175);
		bomber.draw(titleBatch);
		font.draw(titleBatch, "Frigate: A great hulk of a ship that fires volleys of heat-seeking torpedoes. Effective against fighters.", 130, 180);
		frigate.setPosition(70, 135);
		frigate.draw(titleBatch);
		font.draw(titleBatch, "Upgrade: Improve your factory ship to accumulate resources more quickly.", 130, 140);
		upgrade.setPosition(70, 95);
		upgrade.draw(titleBatch);
		font.draw(titleBatch, "Ships you spawn fight automatically using the latest in artificial aquatelligence technology.", 90, 100);
		font.draw(titleBatch, "The player who keeps their factory ship alive wins!", 90, 80);	 
	
		
		titleBatch.end();

		if (!finished && fade > 0) {
			fade = Math.max(fade - Gdx.graphics.getDeltaTime() / 2.f, 0);
			fadeBatch.begin();
			blackFade.setColor(blackFade.getColor().r, blackFade.getColor().g, blackFade.getColor().b, fade);
			blackFade.draw(fadeBatch);
			fadeBatch.end();
		}

		if (finished) {
			fade = Math.min(fade + Gdx.graphics.getDeltaTime() / 2.f, 1);
			fadeBatch.begin();
			blackFade.setColor(blackFade.getColor().r, blackFade.getColor().g, blackFade.getColor().b, fade);
			blackFade.draw(fadeBatch);
			fadeBatch.end();
			if (fade >= 1) {
				game.setScreen(new MainMenu(game));
			}
		}

	}
	
	@Override
	public void resize(int width, int height) {
		this.width = width;
		this.height = height;
		if (width == 480 && height == 320) {
			cam = new OrthographicCamera(700, 466);
			this.width = 700;
			this.height = 466;
		} else if (width == 320 && height == 240) {
			cam = new OrthographicCamera(700, 525);
			this.width = 700;
			this.height = 525;
		} else if (width == 400 && height == 240) {
			cam = new OrthographicCamera(800, 480);
			this.width = 800;
			this.height = 480;
		} else if (width == 432 && height == 240) {
			cam = new OrthographicCamera(700, 389);
			this.width = 700;
			this.height = 389;
		} else if (width == 960 && height == 640) {
			cam = new OrthographicCamera(800, 533);
			this.width = 800;
			this.height = 533;
		}  else if (width == 1366 && height == 768) {
			cam = new OrthographicCamera(1280, 720);
			this.width = 1280;
			this.height = 720;
		} else if (width == 1366 && height == 720) {
			cam = new OrthographicCamera(1280, 675);
			this.width = 1280;
			this.height = 675;
		} else if (width == 1536 && height == 1152) {
			cam = new OrthographicCamera(1366, 1024);
			this.width = 1366;
			this.height = 1024;
		} else if (width == 1920 && height == 1152) {
			cam = new OrthographicCamera(1366, 854);
			this.width = 1366;
			this.height = 854;
		} else if (width == 1920 && height == 1200) {
			cam = new OrthographicCamera(1366, 800);
			this.width = 1280;
			this.height = 800;
		} else if (width > 1280) {
			cam = new OrthographicCamera(1280, 768);
			this.width = 1280;
			this.height = 768;
		} else if (width < 800) {
			cam = new OrthographicCamera(800, 480);
			this.width = 800;
			this.height = 480;
		} else {
			cam = new OrthographicCamera(width, height);
		}
		cam.position.x = 400;
		cam.position.y = 240;
		cam.update();	
		backgroundFX.resize(width, height);
		titleBatch.getProjectionMatrix().set(cam.combined);
		
		back.setPosition(20 - ((this.width-800)/2), 10- ((this.height-480)/2));
		collisionBack.set(new Vector3(back.getVertices()[0], back.getVertices()[1], -10),new Vector3(back.getVertices()[10], back.getVertices()[11], 10));
	
	}

	@Override
	public void hide() {
	}

	@Override
	public boolean keyDown(int keycode) {
		if(keycode == Input.Keys.BACK) {
			finished = true;
		}
		
		if(keycode == Input.Keys.ESCAPE) {
			finished = true;
		}
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDown(int x, int y, int pointer, int button) {
		collisionRay = cam.getPickRay(x, y);
		
		if (Intersector.intersectRayBoundsFast(collisionRay, collisionBack)) {
			finished = true;
		}
		return false;
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		// TODO Auto-generated method stub
		return false;
	}
}