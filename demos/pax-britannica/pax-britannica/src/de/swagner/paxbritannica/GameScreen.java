package de.swagner.paxbritannica;

import java.io.IOException;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;

import de.swagner.paxbritannica.background.BackgroundFXRenderer;
import de.swagner.paxbritannica.factory.EasyEnemyProduction;
import de.swagner.paxbritannica.factory.FactoryProduction;
import de.swagner.paxbritannica.factory.HardEnemyProduction;
import de.swagner.paxbritannica.factory.MediumEnemyProduction;
import de.swagner.paxbritannica.factory.PlayerProduction;
import de.swagner.paxbritannica.help.ScreenshotSaver;
import de.swagner.paxbritannica.mainmenu.MainMenu;

public class GameScreen extends DefaultScreen implements InputProcessor {

	double startTime = 0;
	BackgroundFXRenderer backgroundFX = new BackgroundFXRenderer();

	private float fade = 1.0f;
	Sprite blackFade;
	Sprite stouchAreaP1;
	Sprite stouchAreaP2;
	Sprite stouchAreaP3;
	Sprite stouchAreaP4;
	Sprite p1;
	Sprite p2;
	Sprite p3;
	Sprite p4;
	
	SpriteBatch fadeBatch;
	SpriteBatch gameBatch;

	FactoryProduction playerProduction;
	FactoryProduction enemyProduction;

//	ShapeRenderer shapeRenderer = new ShapeRenderer();
	OrthographicCamera cam;
	
	private boolean gameOver = false;
	private float gameOverTimer =5;
	
	private BoundingBox touchAreaP1;
	private BoundingBox touchAreaP2;
	private BoundingBox touchAreaP3;
	private BoundingBox touchAreaP4;
	int pointerP1;
	int pointerP2;
	int pointerP3;
	int pointerP4;
	float touchFadeP1 = 1.0f;
	float touchFadeP2 = 1.0f;
	float touchFadeP3 = 1.0f;
	float touchFadeP4 = 1.0f;
	boolean touchedP1 = false;
	boolean touchedP2 = false;
	boolean touchedP3 = false;
	boolean touchedP4 = false;
	
	int numPlayers = 0;
	
	Ray collisionRay;

	private Array<Vector2> POSITIONS = new Array<Vector2>();

	private Vector2 CENTER = new Vector2(300, 180);
	
	private int width = 800;
	private int height = 480;

	public GameScreen(Game game, Array<Integer> playerList, Array<Integer> cpuList) {
		super(game);
		Gdx.input.setCatchBackKey( true );
		Gdx.input.setInputProcessor(this);

		cam = new OrthographicCamera(width, height);
		
		cam.position.x = 400;
		cam.position.y = 240;
		cam.update();
		
		numPlayers = playerList.size;
		
		if(numPlayers==1) {
			touchAreaP1 = new BoundingBox(new Vector3(-((this.width-800)/2), -((this.height-480)/2), 0),new Vector3(-((this.width-800)/2)+(this.width), -((this.height-480)/2)+this.height, 0));
		} else if(numPlayers == 2) {
			touchAreaP1 = new BoundingBox(new Vector3(-((this.width-800)/2), -((this.height-480)/2), 0),new Vector3(-((this.width-800)/2)+(this.width/2), -((this.height-480)/2)+this.height, 0));
			touchAreaP2 = new BoundingBox(new Vector3(-((this.width-800)/2)+(this.width/2), -((this.height-480)/2), 0),new Vector3(-((this.width-800)/2)+this.width, -((this.height-480)/2)+this.height, 0));
		} else if(numPlayers == 3) {
			touchAreaP1 = new BoundingBox(new Vector3(-((this.width-800)/2), -((this.height-480)/2), 0),new Vector3(-((this.width-800)/2)+(this.width/2), -((this.height-480)/2)+(this.height/2), 0));
			touchAreaP2 = new BoundingBox(new Vector3(-((this.width-800)/2), -((this.height-480)/2)+(this.height/2), 0),new Vector3(-((this.width-800)/2)+(this.width/2), -((this.height-480)/2)+this.height, 0));
			touchAreaP3 = new BoundingBox(new Vector3(-((this.width-800)/2)+(this.width/2), -((this.height-480)/2), 0),new Vector3(-((this.width-800)/2)+this.width, -((this.height-480)/2)+this.height, 0));
		} else if(numPlayers == 4) {
			touchAreaP1 = new BoundingBox(new Vector3(-((this.width-800)/2), -((this.height-480)/2), 0),new Vector3(-((this.width-800)/2)+(this.width/2), -((this.height-480)/2)+(this.height/2), 0));
			touchAreaP2 = new BoundingBox(new Vector3(-((this.width-800)/2), -((this.height-480)/2)+(this.height/2), 0),new Vector3(-((this.width-800)/2)+(this.width/2), -((this.height-480)/2)+this.height, 0));
			touchAreaP3 = new BoundingBox(new Vector3(-((this.width-800)/2)+(this.width/2), -((this.height-480)/2), 0),new Vector3(-((this.width-800)/2)+this.width, -((this.height-480)/2)+(this.height/2), 0));
			touchAreaP4 = new BoundingBox(new Vector3(-((this.width-800)/2)+(this.width/2), -((this.height-480)/2)+(this.height/2), 0),new Vector3(-((this.width-800)/2)+this.width, -((this.height-480)/2)+this.height, 0));
		}

//		camera = new OrthographicCamera(800, 480);
//		camera.translate(400, 240, 0);

		if(playerList.size + cpuList.size != 3) {
			POSITIONS.add(new Vector2(150, 180));
			POSITIONS.add(new Vector2(450, 180));
			POSITIONS.add(new Vector2(300, 335));
			POSITIONS.add(new Vector2(300, 25));
		} else {
			POSITIONS.add(new Vector2(170, 92));
			POSITIONS.add(new Vector2(432, 100));
			POSITIONS.add(new Vector2(300, 335));
		}
		
		
		// Fade
		blackFade = Resources.getInstance().blackFade;
		fadeBatch = new SpriteBatch();
		fadeBatch.getProjectionMatrix().setToOrtho2D(0, 0, 2, 2);

		stouchAreaP1 = Resources.getInstance().touchArea1;
		stouchAreaP2 = Resources.getInstance().touchArea2;
		stouchAreaP3 = Resources.getInstance().touchArea3;
		stouchAreaP4 = Resources.getInstance().touchArea4;
		
		if(playerList.size>0 && playerList.get(0)==1) {
			p1 = Resources.getInstance().factoryP1Small;
		} else if(playerList.size>0 && playerList.get(0)==2) {
			p1 = Resources.getInstance().factoryP2Small;
		} else if(playerList.size>0 && playerList.get(0)==3) {
			p1 = Resources.getInstance().factoryP3Small;
		} else if(playerList.size>0 && playerList.get(0)==4) {
			p1 = Resources.getInstance().factoryP4Small;		
		}
		
		if(playerList.size>1 && playerList.get(1)==1) {
			p2 = Resources.getInstance().factoryP1Small;
		} else if(playerList.size>1 && playerList.get(1)==2) {
			p2 = Resources.getInstance().factoryP2Small;
		} else if(playerList.size>1 && playerList.get(1)==3) {
			p2 = Resources.getInstance().factoryP3Small;
		} else if(playerList.size>1 && playerList.get(1)==4) {
			p2 = Resources.getInstance().factoryP4Small;		
		}
		
		if(playerList.size>2 && playerList.get(2)==1) {
			p3 = Resources.getInstance().factoryP1Small;
		} else if(playerList.size>2 && playerList.get(2)==2) {
			p3 = Resources.getInstance().factoryP2Small;
		} else if(playerList.size>2 && playerList.get(2)==3) {
			p3 = Resources.getInstance().factoryP3Small;
		} else if(playerList.size>2 && playerList.get(2)==4) {
			p3 = Resources.getInstance().factoryP4Small;		
		}
		
		if(playerList.size>3 && playerList.get(3)==1) {
			p4 = Resources.getInstance().factoryP1Small;
		} else if(playerList.size>3 && playerList.get(3)==2) {
			p4 = Resources.getInstance().factoryP2Small;
		} else if(playerList.size>3 && playerList.get(3)==3) {
			p4 = Resources.getInstance().factoryP3Small;
		} else if(playerList.size>3 && playerList.get(3)==4) {
			p4 = Resources.getInstance().factoryP4Small;		
		}
		
		if(playerList.size>0) p1.setScale(.2f);
		if(playerList.size>1) p2.setScale(.2f);
		if(playerList.size>2) p3.setScale(.2f);
		if(playerList.size>3) p4.setScale(.2f);
		
		if(playerList.size>0) p1.rotate(-90);
		if(playerList.size>1) p2.rotate(90);
		if(playerList.size>2) p3.rotate(-90);
		if(playerList.size>3) p4.rotate(90);

		stouchAreaP1.setRotation(-90);
		stouchAreaP2.setRotation(90);
		stouchAreaP1.setRotation(-90);
		stouchAreaP2.setRotation(90);
		
		gameBatch = new SpriteBatch();
		gameBatch.getProjectionMatrix().set(cam.combined);


		// init player positions
//		Array<Vector2> positons = generatePositions(numPlayers + 1);
		
		int currentPos = 0;
		
		for(int i=0;i<playerList.size;++i) {
			Vector2 temp1 = new Vector2(POSITIONS.get(currentPos).x, POSITIONS.get(currentPos).y);
			Vector2 temp2 = new Vector2(POSITIONS.get(currentPos).x, POSITIONS.get(currentPos).y);
			Vector2 facing = new Vector2(-temp1.sub(CENTER).y, temp2.sub(CENTER).x).nor();
			playerProduction = new PlayerProduction(playerList.get(i), POSITIONS.get(currentPos), facing);
			GameInstance.getInstance().factorys.add(playerProduction);
			++currentPos;
		}
		
		for(int i=0;i<cpuList.size;++i) {
			Vector2 temp1 = new Vector2(POSITIONS.get(currentPos).x, POSITIONS.get(currentPos).y);
			Vector2 temp2 = new Vector2(POSITIONS.get(currentPos).x, POSITIONS.get(currentPos).y);
			Vector2 facing = new Vector2(-temp1.sub(CENTER).y, temp2.sub(CENTER).x).nor();
			if(GameInstance.getInstance().difficultyConfig == 0) {
				enemyProduction = new EasyEnemyProduction(cpuList.get(i), POSITIONS.get(currentPos), facing);
			} else if(GameInstance.getInstance().difficultyConfig == 1) {
				enemyProduction = new MediumEnemyProduction(cpuList.get(i), POSITIONS.get(currentPos), facing);
			} else {
				enemyProduction = new HardEnemyProduction(cpuList.get(i), POSITIONS.get(currentPos), facing);
			}
			GameInstance.getInstance().factorys.add(enemyProduction);
			++currentPos;
		}

//		// add cpu if only one player plays
//		if (idP2 == -1) {
//			temp1 = new Vector2(POSITIONS.get(1).x, POSITIONS.get(1).y);
//			temp2 = new Vector2(POSITIONS.get(1).x, POSITIONS.get(1).y);
//			facing = new Vector2(-temp1.sub(CENTER).y, temp2.sub(CENTER).x).nor();
//			if(GameInstance.getInstance().difficultyConfig == 0) {
//				enemyProduction = new EasyEnemyProduction((idP1+1)%4, POSITIONS.get(1), facing);
//			} else if(GameInstance.getInstance().difficultyConfig == 1) {
//				enemyProduction = new MediumEnemyProduction((idP1+1)%4, POSITIONS.get(1), facing);
//			} else {
//				enemyProduction = new HardEnemyProduction((idP1+1)%4, POSITIONS.get(1), facing);
//			}
//			GameInstance.getInstance().factorys.add(enemyProduction);
//			touchedP2 = true;
//			touchFadeP2 = 0;
//			
//			temp1 = new Vector2(POSITIONS.get(2).x, POSITIONS.get(2).y);
//			temp2 = new Vector2(POSITIONS.get(2).x, POSITIONS.get(2).y);
//			facing = new Vector2(-temp1.sub(CENTER).y, temp2.sub(CENTER).x).nor();
//			if(GameInstance.getInstance().difficultyConfig == 0) {
//				enemyProduction = new EasyEnemyProduction((idP1+2)%4, POSITIONS.get(2), facing);
//			} else if(GameInstance.getInstance().difficultyConfig == 1) {
//				enemyProduction = new MediumEnemyProduction((idP1+2)%4, POSITIONS.get(2), facing);
//			} else {
//				enemyProduction = new HardEnemyProduction((idP1+2)%4, POSITIONS.get(2), facing);
//			}
//			GameInstance.getInstance().factorys.add(enemyProduction);
//			touchedP2 = true;
//			touchFadeP2 = 0;
//			
//			temp1 = new Vector2(POSITIONS.get(3).x, POSITIONS.get(3).y);
//			temp2 = new Vector2(POSITIONS.get(3).x, POSITIONS.get(3).y);
//			facing = new Vector2(-temp1.sub(CENTER).y, temp2.sub(CENTER).x).nor();
//			if(GameInstance.getInstance().difficultyConfig == 0) {
//				enemyProduction = new EasyEnemyProduction((idP1+3)%4, POSITIONS.get(3), facing);
//			} else if(GameInstance.getInstance().difficultyConfig == 1) {
//				enemyProduction = new MediumEnemyProduction((idP1+3)%4, POSITIONS.get(3), facing);
//			} else {
//				enemyProduction = new HardEnemyProduction((idP1+3)%4, POSITIONS.get(3), facing);
//			}
//			GameInstance.getInstance().factorys.add(enemyProduction);
//			touchedP2 = true;
//			touchFadeP2 = 0;
//		} else {
//			temp1 = new Vector2(POSITIONS.get(1).x, POSITIONS.get(1).y);
//			temp2 = new Vector2(POSITIONS.get(1).x, POSITIONS.get(1).y);
//			facing = new Vector2(-temp1.sub(CENTER).y, temp2.sub(CENTER).x).nor();
//			playerProduction = new PlayerProduction(idP2, POSITIONS.get(1), facing);
//			GameInstance.getInstance().factorys.add(playerProduction);
//		}
		
		Gdx.gl.glDisable(GL20.GL_CULL_FACE);
		Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
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
		gameBatch.getProjectionMatrix().set(cam.combined);		
		
		if(numPlayers==1) {
			p1.setRotation(-90);
			
			stouchAreaP1.setRotation(-90);
			
			touchAreaP1 = new BoundingBox(new Vector3(-((this.width-800)/2), -((this.height-480)/2), 0),new Vector3(-((this.width-800)/2)+(this.width), -((this.height-480)/2)+this.height, 0));
			
			stouchAreaP1.setPosition(touchAreaP1.min.x, touchAreaP1.getCenter().y-40);
			p1.setPosition(touchAreaP1.min.x+10, touchAreaP1.getCenter().y-105);
		} else if(numPlayers == 2) {
			p1.setRotation(-90);
			p2.setRotation(90);

			stouchAreaP1.setRotation(-90);
			stouchAreaP2.setRotation(90);
			
			touchAreaP1 = new BoundingBox(new Vector3(-((this.width-800)/2), -((this.height-480)/2), 0),new Vector3(-((this.width-800)/2)+(this.width/2), -((this.height-480)/2)+this.height, 0));
			touchAreaP2 = new BoundingBox(new Vector3(-((this.width-800)/2)+(this.width/2), -((this.height-480)/2), 0),new Vector3(-((this.width-800)/2)+this.width, -((this.height-480)/2)+this.height, 0));
			
			stouchAreaP1.setPosition(touchAreaP1.min.x, touchAreaP1.getCenter().y-40);
			p1.setPosition(touchAreaP1.min.tmp().x+10, touchAreaP1.getCenter().y-105);
			stouchAreaP2.setPosition(touchAreaP2.max.x - 170, touchAreaP2.getCenter().y-40);
			p2.setPosition(touchAreaP2.max.x-190, touchAreaP2.getCenter().y-15);
		} else if(numPlayers == 3) {
			p1.setRotation(-90);
			p2.setRotation(-90);
			p3.setRotation(90);

			stouchAreaP1.setRotation(-90);
			stouchAreaP2.setRotation(-90);
			stouchAreaP3.setRotation(90);
			
			touchAreaP1 = new BoundingBox(new Vector3(-((this.width-800)/2), -((this.height-480)/2), 0),new Vector3(-((this.width-800)/2)+(this.width/2), -((this.height-480)/2)+(this.height/2), 0));
			touchAreaP2 = new BoundingBox(new Vector3(-((this.width-800)/2), -((this.height-480)/2)+(this.height/2), 0),new Vector3(-((this.width-800)/2)+(this.width/2), -((this.height-480)/2)+this.height, 0));
			touchAreaP3 = new BoundingBox(new Vector3(-((this.width-800)/2)+(this.width/2), -((this.height-480)/2), 0),new Vector3(-((this.width-800)/2)+this.width, -((this.height-480)/2)+this.height, 0));
			
			stouchAreaP1.setPosition(touchAreaP1.min.x, touchAreaP1.getCenter().y-40);
			p1.setPosition(touchAreaP1.min.tmp().x+10, touchAreaP1.getCenter().y-105);
			stouchAreaP2.setPosition(touchAreaP2.min.x, touchAreaP2.getCenter().y-40);
			p2.setPosition(touchAreaP2.min.x+10, touchAreaP2.getCenter().y-105);
			stouchAreaP3.setPosition(touchAreaP3.max.x - 170, touchAreaP3.getCenter().y-40);
			p3.setPosition(touchAreaP3.max.x-190, touchAreaP3.getCenter().y-15);
		} else if(numPlayers == 4) {
			p1.setRotation(-90);
			p2.setRotation(-90);
			p3.setRotation(90);
			p4.setRotation(90);

			stouchAreaP1.setRotation(-90);
			stouchAreaP2.setRotation(-90);
			stouchAreaP3.setRotation(90);
			stouchAreaP4.setRotation(90);
			
			touchAreaP1 = new BoundingBox(new Vector3(-((this.width-800)/2), -((this.height-480)/2), 0),new Vector3(-((this.width-800)/2)+(this.width/2), -((this.height-480)/2)+(this.height/2), 0));
			touchAreaP2 = new BoundingBox(new Vector3(-((this.width-800)/2), -((this.height-480)/2)+(this.height/2), 0),new Vector3(-((this.width-800)/2)+(this.width/2), -((this.height-480)/2)+this.height, 0));
			touchAreaP3 = new BoundingBox(new Vector3(-((this.width-800)/2)+(this.width/2), -((this.height-480)/2), 0),new Vector3(-((this.width-800)/2)+this.width, -((this.height-480)/2)+(this.height/2), 0));
			touchAreaP4 = new BoundingBox(new Vector3(-((this.width-800)/2)+(this.width/2), -((this.height-480)/2)+(this.height/2), 0),new Vector3(-((this.width-800)/2)+this.width, -((this.height-480)/2)+this.height, 0));
			
			stouchAreaP1.setPosition(touchAreaP1.min.x, touchAreaP1.getCenter().y-40);
			p1.setPosition(touchAreaP1.min.tmp().x+10, touchAreaP1.getCenter().y-105);
			stouchAreaP2.setPosition(touchAreaP2.min.x, touchAreaP2.getCenter().y-40);
			p2.setPosition(touchAreaP2.min.x+10, touchAreaP2.getCenter().y-105);
			stouchAreaP3.setPosition(touchAreaP3.max.x - 170, touchAreaP3.getCenter().y-40);
			p3.setPosition(touchAreaP3.max.x-190, touchAreaP3.getCenter().y-15);
			stouchAreaP4.setPosition(touchAreaP4.max.x - 170, touchAreaP4.getCenter().y-40);
			p4.setPosition(touchAreaP4.max.x-190, touchAreaP4.getCenter().y-15);
		}
	}

	public Array<Vector2> generatePositions(int n) {
		Array<Vector2> positions = new Array<Vector2>();
		for (int i = 1; i <= n; ++i) {
			positions.add(new Vector2(MathUtils.cos(i / n), MathUtils.sin(i / n)).scl(200));
		}
		return positions;
	}

	@Override
	public void show() {
	}

	@Override
	public void render(float delta) {
		delta = Math.min(0.06f, delta);
		
		backgroundFX.render();		

		Collision.collisionCheck();

		gameBatch.begin();
		// Bubbles
		GameInstance.getInstance().bubbleParticles.draw(gameBatch);
		GameInstance.getInstance().bigBubbleParticles.draw(gameBatch);

		// Factorys
		for (Ship ship : GameInstance.getInstance().factorys) {
			if (ship.alive) {
				ship.draw(gameBatch);
			} else {
				GameInstance.getInstance().factorys.removeValue(ship, true);
				if(GameInstance.getInstance().factorys.size < 2) gameOver = true;
			}
		}
		// Frigate
		for (Ship ship : GameInstance.getInstance().frigates) {
			if (ship.alive) {
				ship.draw(gameBatch);
			} else {
				GameInstance.getInstance().frigates.removeValue(ship, true);
			}
		}
		// Bomber
		for (Ship ship : GameInstance.getInstance().bombers) {
			if (ship.alive) {
				ship.draw(gameBatch);
			} else {
				GameInstance.getInstance().bombers.removeValue(ship, true);
			}
		}
		// Fighter
		for (Ship ship : GameInstance.getInstance().fighters) {
			if (ship.alive) {
				ship.draw(gameBatch);
			} else {
				GameInstance.getInstance().fighters.removeValue(ship, true);
			}
		}
		
		// Laser
		for (Ship ship : GameInstance.getInstance().bullets) {
			if (ship.alive) {
				ship.draw(gameBatch);
			} else {
				GameInstance.getInstance().bullets.removeValue((Bullet) ship, true);
			}
		}

		// Explosions
		GameInstance.getInstance().sparkParticles.draw(gameBatch);
		GameInstance.getInstance().explosionParticles.draw(gameBatch);

//		font.draw(gameBatch, "fps: " + Gdx.graphics.getFramesPerSecond(), 10, 30);
		gameBatch.end();
				
		//show touch area notification
		if(numPlayers > 0 && touchedP1) {
			touchFadeP1 = Math.max(touchFadeP1 - delta / 2.f, 0);
		}
		if(numPlayers > 0 && (!touchedP1 || touchFadeP1>0)) {
			gameBatch.begin();
			stouchAreaP1.setColor(stouchAreaP1.getColor().r, stouchAreaP1.getColor().g, stouchAreaP1.getColor().b, touchFadeP1);
			stouchAreaP1.draw(gameBatch);
			p1.setColor(p1.getColor().r, p1.getColor().g, p1.getColor().b, touchFadeP1);
			p1.draw(gameBatch);
			gameBatch.end();
		}
		if(numPlayers > 1 && touchedP2) {
			touchFadeP2 = Math.max(touchFadeP2 - delta / 2.f, 0);
		}
		if(numPlayers > 1 && (!touchedP2 || touchFadeP2>0)) {
			gameBatch.begin();
			stouchAreaP2.setColor(stouchAreaP2.getColor().r, stouchAreaP2.getColor().g, stouchAreaP2.getColor().b, touchFadeP2);
			stouchAreaP2.draw(gameBatch);
			p2.setColor(p2.getColor().r, p2.getColor().g, p2.getColor().b, touchFadeP2);
			p2.draw(gameBatch);
			gameBatch.end();
		}
		if(numPlayers > 2 && touchedP3) {
			touchFadeP3 = Math.max(touchFadeP3 - delta / 2.f, 0);
		}
		if(numPlayers > 2 && (!touchedP3 || touchFadeP3>0)) {
			gameBatch.begin();
			stouchAreaP3.setColor(stouchAreaP3.getColor().r, stouchAreaP3.getColor().g, stouchAreaP3.getColor().b, touchFadeP3);
			stouchAreaP3.draw(gameBatch);
			p3.setColor(p3.getColor().r, p3.getColor().g, p3.getColor().b, touchFadeP3);
			p3.draw(gameBatch);
			gameBatch.end();
		}
		if(numPlayers > 3 && touchedP4) {
			touchFadeP4 = Math.max(touchFadeP4 - delta / 2.f, 0);
		}
		if(numPlayers > 3 && (!touchedP4 || touchFadeP4>0)) {
			gameBatch.begin();
			stouchAreaP4.setColor(stouchAreaP4.getColor().r, stouchAreaP4.getColor().g, stouchAreaP4.getColor().b, touchFadeP4);
			stouchAreaP4.draw(gameBatch);
			p4.setColor(p4.getColor().r, p4.getColor().g, p4.getColor().b, touchFadeP4);
			p4.draw(gameBatch);
			gameBatch.end();
		}
		
		if (!gameOver && fade > 0 && fade < 100) {
			fade = Math.max(fade - delta / 2.f, 0);
			fadeBatch.begin();
			blackFade.setColor(blackFade.getColor().r, blackFade.getColor().g, blackFade.getColor().b, fade);
			blackFade.draw(fadeBatch);
			fadeBatch.end();
		}
		
		if(gameOver) {
			gameOverTimer -= delta;
		}
		if (gameOver && gameOverTimer <= 0) {
			fade = Math.min(fade + delta / 2.f, 1);
			fadeBatch.begin();
			blackFade.setColor(blackFade.getColor().r, blackFade.getColor().g, blackFade.getColor().b, fade);
			blackFade.draw(fadeBatch);
			fadeBatch.end();
			if(fade>=1) game.setScreen(new MainMenu(game));
		}
		
//		shapeRenderer.setProjectionMatrix(cam.combined);
//		 
//		 shapeRenderer.begin(ShapeType.Line);
//		 shapeRenderer.setColor(1, 1, 0, 1);
//		 shapeRenderer.line(touchAreaP1.min.x, touchAreaP1.min.y, touchAreaP1.max.x, touchAreaP1.max.y);
//		 shapeRenderer.line(touchAreaP2.min.x, touchAreaP2.min.y, touchAreaP2.max.x, touchAreaP2.max.y);
//		 shapeRenderer.line(touchAreaP3.min.x, touchAreaP3.min.y, touchAreaP3.max.x, touchAreaP3.max.y);
//		 shapeRenderer.line(touchAreaP4.min.x, touchAreaP4.min.y, touchAreaP4.max.x, touchAreaP4.max.y);
//		 shapeRenderer.end();
		 
	}

	@Override
	public void hide() {
		
	}

	@Override
	public boolean keyDown(int keycode) {
		if(keycode == Input.Keys.BACK) {
			gameOver = true;
			gameOverTimer=0;
		}
		
		if(keycode == Input.Keys.ESCAPE) {
			gameOver = true;
			gameOverTimer=0;
		}		
		
		if(numPlayers >0 && keycode == Input.Keys.A && GameInstance.getInstance().factorys.size>0) {
			((FactoryProduction) GameInstance.getInstance().factorys.get(0)).button_held = true;
			touchedP1 = true;
		} 
		if(numPlayers >1 && keycode == Input.Keys.F && GameInstance.getInstance().factorys.size>1) {
			((FactoryProduction) GameInstance.getInstance().factorys.get(1)).button_held = true;
			touchedP2 = true;
		} 
		if(numPlayers >2 && keycode == Input.Keys.H && GameInstance.getInstance().factorys.size>2) {
			((FactoryProduction) GameInstance.getInstance().factorys.get(2)).button_held = true;
			touchedP3 = true;
		} 
		if(numPlayers >3 && keycode == Input.Keys.L && GameInstance.getInstance().factorys.size>3) {
			((FactoryProduction) GameInstance.getInstance().factorys.get(3)).button_held = true;
			touchedP4 = true;
		} 
		
		if(GameInstance.getInstance().debugMode) {				
			if(keycode == Input.Keys.F8) {
				try {
					ScreenshotSaver.saveScreenshot("screenshot");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}		
		}
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		if(numPlayers >0 && keycode == Input.Keys.A && GameInstance.getInstance().factorys.size>0) {
			((FactoryProduction) GameInstance.getInstance().factorys.get(0)).button_held = false;
		} 
		if(numPlayers >1 && keycode == Input.Keys.F &&  GameInstance.getInstance().factorys.size>1) {
			((FactoryProduction) GameInstance.getInstance().factorys.get(1)).button_held = false;
		} 
		if(numPlayers >2 && keycode == Input.Keys.H &&  GameInstance.getInstance().factorys.size>1) {
			((FactoryProduction) GameInstance.getInstance().factorys.get(2)).button_held = false;
		} 
		if(numPlayers >3 && keycode == Input.Keys.L &&  GameInstance.getInstance().factorys.size>1) {
			((FactoryProduction) GameInstance.getInstance().factorys.get(3)).button_held = false;
		} 
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
		
		if(numPlayers >0 && Intersector.intersectRayBoundsFast(collisionRay, touchAreaP1) && GameInstance.getInstance().factorys.size>0) {
			((FactoryProduction) GameInstance.getInstance().factorys.get(0)).button_held = true;
			pointerP1 = pointer;
			touchedP1 = true;
		} 
		if(numPlayers >1 && Intersector.intersectRayBoundsFast(collisionRay, touchAreaP2) && GameInstance.getInstance().factorys.size>1) {
			((FactoryProduction) GameInstance.getInstance().factorys.get(1)).button_held = true;
			pointerP2 = pointer;
			touchedP2 = true;
		} 
		if(numPlayers >2 && Intersector.intersectRayBoundsFast(collisionRay, touchAreaP3) && GameInstance.getInstance().factorys.size>2) {
			((FactoryProduction) GameInstance.getInstance().factorys.get(2)).button_held = true;
			pointerP3 = pointer;
			touchedP3 = true;
		} 
		if(numPlayers >3 && Intersector.intersectRayBoundsFast(collisionRay, touchAreaP4) && GameInstance.getInstance().factorys.size>3) {
			((FactoryProduction) GameInstance.getInstance().factorys.get(3)).button_held = true;
			pointerP4 = pointer;
			touchedP4 = true;
		} 
		return false;
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button) {
		collisionRay = cam.getPickRay(x, y);
		
		if(numPlayers >0 && pointer == pointerP1 && GameInstance.getInstance().factorys.size>0) {
			((FactoryProduction) GameInstance.getInstance().factorys.get(0)).button_held = false;
			pointerP1 = -1;
		} 
		if(numPlayers >1 && pointer == pointerP2 &&  GameInstance.getInstance().factorys.size>1) {
			((FactoryProduction) GameInstance.getInstance().factorys.get(1)).button_held = false;
			pointerP2 = -1;
		} 
		if(numPlayers >2 && pointer == pointerP3 &&  GameInstance.getInstance().factorys.size>1) {
			((FactoryProduction) GameInstance.getInstance().factorys.get(2)).button_held = false;
			pointerP3 = -1;
		} 
		if(numPlayers >3 && pointer == pointerP4 &&  GameInstance.getInstance().factorys.size>1) {
			((FactoryProduction) GameInstance.getInstance().factorys.get(3)).button_held = false;
			pointerP4 = -1;
		} 
		return false;
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer) {
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

}
