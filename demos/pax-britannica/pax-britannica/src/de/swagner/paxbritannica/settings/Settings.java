package de.swagner.paxbritannica.settings;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Preferences;
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

public class Settings extends DefaultScreen implements InputProcessor {
	
	Sprite back;
	
	BoundingBox collisionBack = new BoundingBox();
	BoundingBox collisionDiffEasy = new BoundingBox();
	BoundingBox collisionDiffMedium = new BoundingBox();
	BoundingBox collisionDiffHard = new BoundingBox();
	
	BoundingBox collisionFacHealthLow = new BoundingBox();
	BoundingBox collisionFacHealthMedium = new BoundingBox();
	BoundingBox collisionFacHealthHigh = new BoundingBox();	
	
	BoundingBox collisionAntiAliasOff = new BoundingBox();
	BoundingBox collisionAntiAliasOn = new BoundingBox();

	Sprite checkboxOn;
	Sprite checkboxOff;
	
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
	
	public Settings(Game game) {
		super(game);
		Gdx.input.setCatchBackKey( true );
		Gdx.input.setInputProcessor(this);
	}

	@Override
	public void show() {		
		GameInstance.getInstance().resetGame();
		
		backgroundFX = new BackgroundFXRenderer();
		
		blackFade = Resources.getInstance().blackFade;
		
		back = Resources.getInstance().back;
		back.setPosition(20, 010);
		back.setColor(1,1,1,0.5f);
		collisionBack.set(new Vector3(back.getVertices()[0], back.getVertices()[1], -10),new Vector3(back.getVertices()[10], back.getVertices()[11], 10));
		
		collisionDiffEasy.set(new Vector3(90, 330,-10),new Vector3(190, 360, 10));
		collisionDiffMedium.set(new Vector3(240, 330,-10),new Vector3(340, 360, 10));
		collisionDiffHard.set(new Vector3(400, 330,-10),new Vector3(500, 360, 10));
		
		collisionFacHealthLow.set(new Vector3(90, 230,-10),new Vector3(190, 260, 10));
		collisionFacHealthMedium.set(new Vector3(240, 230,-10),new Vector3(340, 260, 10));
		collisionFacHealthHigh.set(new Vector3(400, 230,-10),new Vector3(500, 260, 10));
		
		collisionAntiAliasOff.set(new Vector3(90, 130,-10),new Vector3(190, 160, 10));
		collisionAntiAliasOn.set(new Vector3(240, 130,-10),new Vector3(340, 160, 10));
		
		checkboxOn = Resources.getInstance().checkboxOn;
		checkboxOff = Resources.getInstance().checkboxOff;
		
		titleBatch = new SpriteBatch();
		titleBatch.getProjectionMatrix().setToOrtho2D(0, 0, 800, 480);
		fadeBatch = new SpriteBatch();
		fadeBatch.getProjectionMatrix().setToOrtho2D(0, 0, 2, 2);
		
		font = new BitmapFont();
		font.getRegion().getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
	}
 
	@Override
	public void render(float delta) {
		time += delta;

		if (time < 1f)
			return;

		backgroundFX.render();
		
		titleBatch.begin();
		
		back.draw(titleBatch);
		
		font.draw(titleBatch, "Difficulty", 90, 400);
		font.draw(titleBatch, "Easy", 130, 360);
		if(GameInstance.getInstance().difficultyConfig==0) {
			checkboxOn.setPosition(90, 330);
			checkboxOn.draw(titleBatch);
		} else {
			checkboxOff.setPosition(90, 330);
			checkboxOff.draw(titleBatch);
		}
		font.draw(titleBatch, "Medium", 280, 360);
		if(GameInstance.getInstance().difficultyConfig==1) {
			checkboxOn.setPosition(240, 330);
			checkboxOn.draw(titleBatch);
		} else {
			checkboxOff.setPosition(240, 330);
			checkboxOff.draw(titleBatch);
		}
		font.draw(titleBatch, "Hard", 440, 360);
		if(GameInstance.getInstance().difficultyConfig==2) {
			checkboxOn.setPosition(400, 330);
			checkboxOn.draw(titleBatch);
		} else {
			checkboxOff.setPosition(400, 330);
			checkboxOff.draw(titleBatch);
		}
		
		
		font.draw(titleBatch, "Factory Health", 90, 300);
		font.draw(titleBatch, "Low", 130, 260);
		if(GameInstance.getInstance().factoryHealthConfig==0) {
			checkboxOn.setPosition(90, 230);
			checkboxOn.draw(titleBatch);
		} else {
			checkboxOff.setPosition(90, 230);
			checkboxOff.draw(titleBatch);
		}
		font.draw(titleBatch, "Medium", 280, 260);
		if(GameInstance.getInstance().factoryHealthConfig==1) {
			checkboxOn.setPosition(240, 230);
			checkboxOn.draw(titleBatch);
		} else {
			checkboxOff.setPosition(240, 230);
			checkboxOff.draw(titleBatch);
		}
		font.draw(titleBatch, "High", 440, 260);
		if(GameInstance.getInstance().factoryHealthConfig==2) {
			checkboxOn.setPosition(400, 230);
			checkboxOn.draw(titleBatch);
		} else {
			checkboxOff.setPosition(400, 230);
			checkboxOff.draw(titleBatch);
		}
		
		
		font.draw(titleBatch, "AntiAliasing (only for fast devices)", 90, 200);
		font.draw(titleBatch, "Off", 130, 160);
		if(GameInstance.getInstance().antiAliasConfig==0) {
			checkboxOn.setPosition(90, 130);
			checkboxOn.draw(titleBatch);
		} else {
			checkboxOff.setPosition(90, 130);
			checkboxOff.draw(titleBatch);
		}
		font.draw(titleBatch, "On", 280, 160);
		if(GameInstance.getInstance().antiAliasConfig==1) {
			checkboxOn.setPosition(240, 130);
			checkboxOn.draw(titleBatch);
		} else {
			checkboxOff.setPosition(240, 130);
			checkboxOff.draw(titleBatch);
		}
		
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
		
		if (Intersector.intersectRayBoundsFast(collisionRay, collisionDiffEasy)) {
			Preferences prefs = Gdx.app.getPreferences("paxbritannica");
			prefs.putInteger("difficulty",0);
			GameInstance.getInstance().difficultyConfig  = prefs.getInteger("difficulty",0);
		}
		if (Intersector.intersectRayBoundsFast(collisionRay, collisionDiffMedium)) {
			Preferences prefs = Gdx.app.getPreferences("paxbritannica");
			prefs.putInteger("difficulty",1);
			GameInstance.getInstance().difficultyConfig  = prefs.getInteger("difficulty",0);
		}
		if (Intersector.intersectRayBoundsFast(collisionRay, collisionDiffHard)) {
			Preferences prefs = Gdx.app.getPreferences("paxbritannica");
			prefs.putInteger("difficulty",2);
			GameInstance.getInstance().difficultyConfig  = prefs.getInteger("difficulty",0);
		}
		
		if (Intersector.intersectRayBoundsFast(collisionRay, collisionFacHealthLow)) {
			Preferences prefs = Gdx.app.getPreferences("paxbritannica");
			prefs.putInteger("factoryHealth",0);
			GameInstance.getInstance().factoryHealthConfig  = prefs.getInteger("factoryHealth",0);
		}
		if (Intersector.intersectRayBoundsFast(collisionRay, collisionFacHealthMedium)) {
			Preferences prefs = Gdx.app.getPreferences("paxbritannica");
			prefs.putInteger("factoryHealth",1);
			GameInstance.getInstance().factoryHealthConfig  = prefs.getInteger("factoryHealth",0);
		}
		if (Intersector.intersectRayBoundsFast(collisionRay, collisionFacHealthHigh)) {
			Preferences prefs = Gdx.app.getPreferences("paxbritannica");
			prefs.putInteger("factoryHealth",2);
			GameInstance.getInstance().factoryHealthConfig  = prefs.getInteger("factoryHealth",0);
		}	
		
		if (Intersector.intersectRayBoundsFast(collisionRay, collisionAntiAliasOff)) {
			Preferences prefs = Gdx.app.getPreferences("paxbritannica");
			prefs.putInteger("antiAliasConfig",0);
			GameInstance.getInstance().antiAliasConfig  = prefs.getInteger("antiAliasConfig",1);
			Resources.getInstance().reInit();
			show();
			resize(this.width,this.height);
		}
		if (Intersector.intersectRayBoundsFast(collisionRay, collisionAntiAliasOn)) {
			Preferences prefs = Gdx.app.getPreferences("paxbritannica");
			prefs.putInteger("antiAliasConfig",1);
			GameInstance.getInstance().antiAliasConfig  = prefs.getInteger("antiAliasConfig",1);
			Resources.getInstance().reInit();
			show();
			resize(this.width,this.height);
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