package de.swagner.paxbritannica.factory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import de.swagner.paxbritannica.Constants;
import de.swagner.paxbritannica.GameInstance;
import de.swagner.paxbritannica.Resources;
import de.swagner.paxbritannica.bomber.Bomber;
import de.swagner.paxbritannica.fighter.Fighter;
import de.swagner.paxbritannica.frigate.Frigate;

public class Production {

	public float BUILDING_SPEED = 100;
	
	public int MAXSHIPS = 100;

	private float SEGMENTS = 32;
	private float RADIUS = 32;
	private float DRAW_OFFSET = -4;
	private float SPAWN_OFFSET = 47;

	public float fighterCost = 50;
	public float bomberCost = 170;
	public float frigateCost = 360;
	public float upgradeCost = 1080;

	private float potential_cost = 0;
	private float needle_angle = 0;
	private float needle_velocity = 0;

	private Vector2 facing90 = new Vector2();

	public boolean halt_production = false;

	private Sprite needle;

	private Sprite upgrade_outline;
	private Sprite frigate_outline;
	private Sprite bomber_outline;
	private Sprite fighter_outline;

	private Sprite production1;
	private Sprite production2;
	private Sprite production3;
	
	private Sprite production_tile1;
	private Sprite production_tile2;
	private Sprite production_tile3;
	private Sprite production_tile4;

	private Sprite health_none;
	private Sprite health_some;
	private Sprite health_full;
	
	public int currentBuildingUnit = -1;
	
	public float fade = 0;
	
//	public ImmediateModeRenderer renderer = new ImmediateModeRenderer();
//	OrthographicCamera camera;

	private FactoryProduction factory;

	public Production(FactoryProduction factory) {
		this.factory = factory;
		
//		camera = new OrthographicCamera(800, 480);
//		camera.translate(400, 240, 0);
		
		needle = Resources.getInstance().needle;
		needle.setOrigin(needle.getHeight() / 2, needle.getWidth() / 2);

		production1 = Resources.getInstance().production1;
		production1.setOrigin(production1.getHeight() / 2, production1.getWidth() / 2);
		production2 = Resources.getInstance().production2;
		production2.setOrigin(production2.getHeight() / 2, production2.getWidth() / 2);
		production3 = Resources.getInstance().production3;
		production3.setOrigin(production3.getHeight() / 2, production3.getWidth() / 2);
		
		production_tile1 = Resources.getInstance().production_tile1;
		production_tile1.setOrigin(production_tile1.getHeight() / 2, production_tile1.getWidth() / 2);
		production_tile2 = Resources.getInstance().production_tile2;
		production_tile2.setOrigin(production_tile2.getHeight() / 2, production_tile2.getWidth() / 2);
		production_tile3 = Resources.getInstance().production_tile3;
		production_tile3.setOrigin(production_tile3.getHeight() / 2, production_tile3.getWidth() / 2);
		production_tile4 = Resources.getInstance().production_tile4;
		production_tile4.setOrigin(production_tile4.getHeight() / 2, production_tile4.getWidth() / 2);

		upgrade_outline = Resources.getInstance().upgradeOutline;
		upgrade_outline.setOrigin(upgrade_outline.getHeight() / 2, upgrade_outline.getWidth() / 2);
		frigate_outline = Resources.getInstance().frigateOutline;
		frigate_outline.setOrigin(frigate_outline.getHeight() / 2, frigate_outline.getWidth() / 2);
		bomber_outline = Resources.getInstance().bomberOutline;
		bomber_outline.setOrigin(bomber_outline.getHeight() / 2, bomber_outline.getWidth() / 2);
		fighter_outline = Resources.getInstance().fighterOutline;
		fighter_outline.setOrigin(fighter_outline.getHeight() / 2, fighter_outline.getWidth() / 2);

		health_none = Resources.getInstance().healthNone;
		health_none.setOrigin(health_none.getHeight() / 2, health_none.getWidth() / 2);
		health_some = Resources.getInstance().healthSome;
		health_some.setOrigin(health_some.getHeight() / 2, health_some.getWidth() / 2);
		health_full = Resources.getInstance().healthFull;
		health_full.setOrigin(health_full.getHeight() / 2, health_full.getWidth() / 2);
	}

	/**
	 * Span new Ship
	 * 
	 * 1 = Fighter
	 * 2 = Bomber
	 * 3 = Frigate
	 * 4 = Upgrade
	 * @param unitType
	 */
	public void spawn(int unitType) {
//		factory.ownShips = 0;
//		for (Ship fighter : GameInstance.getInstance().fighters) {
//			if(fighter.id == factory.id) factory.ownShips++;
//		}		
//		for (Ship bomber : GameInstance.getInstance().bombers) {
//			if(bomber.id != factory.id) factory.ownShips++;
//		}		
//		for (Ship frigate : GameInstance.getInstance().frigates) {
//			if(frigate.id != factory.id) factory.ownShips++;
//		}
//		if(factory.ownShips>MAXSHIPS) {
//			return;
//		}
		
		
		Vector2 spawn_pos = new Vector2(factory.collisionCenter.x + (SPAWN_OFFSET * factory.facing.x), 
				factory.collisionCenter.y + (SPAWN_OFFSET * factory.facing.y));

		if (unitType == 1) {
			factory.resourceAmount -= fighterCost;
			GameInstance.getInstance().fighters.add(new Fighter(factory.id, spawn_pos, new Vector2(factory.facing.x, factory.facing.y)));
		} else if (unitType == 2) {
			spawn_pos.sub(10, 10);
			factory.resourceAmount -= bomberCost;
			GameInstance.getInstance().bombers.add(new Bomber(factory.id, spawn_pos, new Vector2(factory.facing.x, factory.facing.y)));
		} else if (unitType == 3) {
			spawn_pos.sub(25, 25);
			factory.resourceAmount -= frigateCost;
			GameInstance.getInstance().frigates.add(new Frigate(factory.id, spawn_pos, new Vector2(factory.facing.x, factory.facing.y)));
		} else if (unitType == 4) {
			factory.resourceAmount -= upgradeCost;
			factory.upgradesUsed += 1;
			factory.harvestRate += (factory.harvestRateUpgrade/factory.upgradesUsed);
		}
	}

	private void update() {
		if (factory.button_held) {
			if (potential_cost == 0 && factory.resourceAmount >= potential_cost) {
				potential_cost = fighterCost;
			}
			if (factory.resourceAmount > potential_cost + (BUILDING_SPEED * Gdx.graphics.getDeltaTime()) - 1) {
				potential_cost += BUILDING_SPEED * Gdx.graphics.getDeltaTime();
			}
		} else {
			if (potential_cost > upgradeCost) {
				spawn(4);
			} else if (potential_cost > frigateCost) {
				spawn(3);
			} else if (potential_cost > bomberCost) {
				spawn(2);
			} else if (potential_cost > fighterCost) {
				spawn(1);
			}
			potential_cost = 0;
		}
	}

	public float scale_angle(float frames) {
		float angle = 0;
		if (frames < fighterCost) {
			angle = 0.25f;
		} else if (frames < bomberCost) {
			angle = (frames - fighterCost) / (bomberCost - fighterCost) * 0.25f + 0.25f;
		} else if (frames < frigateCost) {
			angle = (frames - bomberCost) / (frigateCost - bomberCost) * 0.25f + 0.5f;
		} else {
			angle = (frames - frigateCost) / (upgradeCost - frigateCost) * 0.25f + 0.75f;
		}
		return Math.min(angle - 0.25f, 1);
	}

	public float get_resources_spent(float frames) {
		float spent = 0;
		if (frames < fighterCost) {
			spent = 0;
		} else if (frames < bomberCost) {
			spent = fighterCost;
		} else if (frames < frigateCost) {
			spent = bomberCost;
		} else if (frames < upgradeCost) {
			spent = frigateCost;
		} else {
			spent = upgradeCost;
		}
		return spent;
	}

	public void draw(SpriteBatch spriteBatch) {
		update();

		facing90.set(factory.facing);
		facing90.rotate(90).nor();
//		factory.facing.nor();

//		//Renders behind sprites... :(
//		float SEGMENTS = 32;
//		float RADIUS = 32;
//		renderer.begin(GL10.GL_TRIANGLE_FAN);
//		Gdx.graphics.getGL11().glPushMatrix();
//		camera.position.set(350, +240, 0);
//		camera.update();
//		camera.apply(Gdx.graphics.getGL10());
//		Gdx.graphics.getGL11().glTranslatef(factory.collisionCenter.x,
//				factory.collisionCenter.y, 0);
//		Gdx.graphics.getGL11().glRotatef(factory.facing.angle() - 90, 0, 0, 1);
//		float angle = factory.production
//				.scale_angle(factory.resourceAmount) * MathUtils.PI * 2f;
//		float filled_angle = MathUtils.PI * 2f - angle;
//		renderer.color(0, 0, 0, 1.6f);
//		renderer.vertex(0, 0, -10);
//		for (int point = 0; point < SEGMENTS; point++) {
//			Vector2 vert = new Vector2(MathUtils.cos(MathUtils.PI / 2.f - angle - point / SEGMENTS * filled_angle) * RADIUS, MathUtils.sin(MathUtils.PI / 2.f
//					- angle - point / SEGMENTS * filled_angle)
//					* RADIUS);
//			renderer.vertex(vert.x, vert.y, -10);
//		}
//		renderer.vertex(0, 0, 10);
//		renderer.end();
//
//		Gdx.graphics.getGL11().glPopMatrix();
//		camera.position.set(400, 240, 0);
//		camera.update();
//		camera.apply(Gdx.graphics.getGL10());
		
		float angle = Math.min(1, Math.max(0, norm(scale_angle(factory.resourceAmount),0,0.25f)));
		fade = Math.min(MathUtils.PI, fade+(Gdx.graphics.getDeltaTime()));
		if(fade == MathUtils.PI) fade =0;
		
		
		production_tile1.setOrigin(0, 0);
		
//		if(factory.ownShips>MAXSHIPS-1) {
//			production_tile1.setColor(1.0f, 0.1f,0.1f, MathUtils.sin(fade));
//		} else {
			production_tile1.setColor(angle, angle,angle, angle);
//		}
		production_tile1.setRotation(factory.facing.angle());
		production_tile1.setPosition(factory.collisionCenter.x - (35 * factory.facing.x) - (32* facing90.x), 
				factory.collisionCenter.y	- (35 * factory.facing.y) - (32 * facing90.y));
		production_tile1.draw(spriteBatch);
		
		angle = Math.min(1, Math.max(0, norm(scale_angle(factory.resourceAmount),0.25f,0.5f)));
		production_tile2.setOrigin(0, 0);
//		if(factory.ownShips>MAXSHIPS-1) {
//			production_tile2.setColor(1, 0.1f,0.1f, MathUtils.sin(fade));
//		} else {
			production_tile2.setColor(angle, angle,angle, angle);
//		}
		production_tile2.setRotation(factory.facing.angle());
		production_tile2.setPosition(factory.collisionCenter.x - (35 * factory.facing.x) - (32 * facing90.x), 
				factory.collisionCenter.y	- (35 * factory.facing.y) - (32 * facing90.y));
		production_tile2.draw(spriteBatch);
		
		angle = Math.min(1, Math.max(0, norm(scale_angle(factory.resourceAmount),0.5f,0.75f)));
		production_tile3.setOrigin(0, 0);
//		if(factory.ownShips>MAXSHIPS-1) {
//			production_tile3.setColor(1, 0.1f,0.1f, MathUtils.sin(fade));
//		} else {
			production_tile3.setColor(angle, angle,angle, angle);
//		}
		production_tile3.setRotation(factory.facing.angle());
		production_tile3.setPosition(factory.collisionCenter.x - (35* factory.facing.x)- (32 * facing90.x), 
				factory.collisionCenter.y	- (35 * factory.facing.y) - (32 * facing90.y));
		production_tile3.draw(spriteBatch);
		
		angle = Math.min(1, Math.max(0, norm(scale_angle(factory.resourceAmount),0.75f,1f)));
		production_tile4.setOrigin(0, 0);
//		if(factory.ownShips>MAXSHIPS-1) {
//			production_tile4.setColor(1, 0.1f,0.1f, MathUtils.sin(fade));
//		} else {
			production_tile4.setColor(angle, angle,angle, angle);
//		}
		production_tile4.setRotation(factory.facing.angle());
		production_tile4.setPosition(factory.collisionCenter.x - (35 * factory.facing.x)- (32 * facing90.x), 
				factory.collisionCenter.y	- (35* factory.facing.y) - (32 * facing90.y));
		production_tile4.draw(spriteBatch);
		
		// Draw the needle
		angle = scale_angle(potential_cost);
		if (angle == 0) {
			if (needle_angle > 0 || needle_velocity < 0) {
				needle_velocity = Math.min(needle_velocity + 0.2f * Gdx.graphics.getDeltaTime(), 0.025f);
				needle_angle = Math.max(needle_angle - needle_velocity, 0);
				if (needle_angle == 0) {
					needle_velocity *= Math.pow(-0.475f, Gdx.graphics.getDeltaTime());
				}
			}
		} else {
			needle_velocity = 0;
			needle_angle = angle;
		}
		needle.setOrigin(0, 0);
		needle.setPosition(factory.collisionCenter.x - (2 * factory.facing.x) - (-2 * facing90.x), 
				factory.collisionCenter.y	- (2 * factory.facing.y) - (-2 * facing90.y));
		needle.setRotation(factory.facing.angle() + ((-needle_angle) * 360) - 90);
		needle.draw(spriteBatch);

		production2.setOrigin(0, 0);
		production2.setRotation(factory.facing.angle());
		production2.setPosition(factory.collisionCenter.x - (35 * factory.facing.x) - (32 * facing90.x), 
				factory.collisionCenter.y	- (35 * factory.facing.y) - (32 * facing90.y));
		production2.draw(spriteBatch);

		production3.setOrigin(0, 0);
		production3.setRotation(factory.facing.angle());
		production3.setPosition(factory.collisionCenter.x - (35 * factory.facing.x) - (32 * facing90.x), 
				factory.collisionCenter.y	- (35 * factory.facing.y) - (32 * facing90.y));
		production3.draw(spriteBatch);
		
		
		// Draw the preview outline
		if (factory.button_held) {
			if (potential_cost > upgradeCost) {
				upgrade_outline.setOrigin(0, 0);
				upgrade_outline.setRotation(factory.facing.angle());
				upgrade_outline.setPosition(factory.collisionCenter.x - (35 * factory.facing.x) - (32 * facing90.x), 
						factory.collisionCenter.y - (35 * factory.facing.y) - (32 * facing90.y));
				upgrade_outline.draw(spriteBatch);
				currentBuildingUnit = 3;
			} else if (potential_cost > frigateCost) {
				frigate_outline.setOrigin(0, 0);
				frigate_outline.setRotation(factory.facing.angle());
				frigate_outline.setPosition(factory.collisionCenter.x - (35 * factory.facing.x) - (32* facing90.x), 
						factory.collisionCenter.y - (35 * factory.facing.y) - (32 * facing90.y));
				frigate_outline.draw(spriteBatch);
				currentBuildingUnit = 2;
			} else if (potential_cost > bomberCost) {
				bomber_outline.setOrigin(0, 0);
				bomber_outline.setRotation(factory.facing.angle());
				bomber_outline.setPosition(factory.collisionCenter.x - (35 * factory.facing.x) - (32 * facing90.x), 
						factory.collisionCenter.y - (35 * factory.facing.y) - (32 * facing90.y));
				bomber_outline.draw(spriteBatch);
				currentBuildingUnit = 1;
			} else if (potential_cost > fighterCost) {
				fighter_outline.setOrigin(0, 0);
				fighter_outline.setRotation(factory.facing.angle());
				fighter_outline.setPosition(factory.collisionCenter.x - (35 * factory.facing.x) - (32 * facing90.x), 
						factory.collisionCenter.y - (35 * factory.facing.y) - (32 * facing90.y));
				fighter_outline.draw(spriteBatch);
				currentBuildingUnit = 0;
			} else {
				currentBuildingUnit = -1;
			}
		} else {
			currentBuildingUnit = -1;
			
			float health = factory.healthPercentage();
			if (health < Constants.lowHealthThreshold) {
				float factor = health / Constants.lowHealthThreshold;
				health_none.setOrigin(0, 0);
				health_none.setRotation(factory.facing.angle());
				health_none.setColor(1, factor * 0.3f, factor * 0.3f, 1);
				health_none.setPosition(factory.collisionCenter.x - (35 * factory.facing.x) - (32 * facing90.x), 
						factory.collisionCenter.y - (35 * factory.facing.y) - (32 * facing90.y));
				health_none.draw(spriteBatch);
			} else if (health < Constants.highHealthThreshold) {
				float factor = (health - Constants.lowHealthThreshold) / (Constants.highHealthThreshold - Constants.lowHealthThreshold);
				health_some.setOrigin(0, 0);
				health_some.setRotation(factory.facing.angle());
				health_some.setColor(1, factor * 0.7f + 0.3f, factor * 0.2f + 0.3f, 1);
				health_some.setPosition(factory.collisionCenter.x - (35 * factory.facing.x) - (32 * facing90.x), 
						factory.collisionCenter.y - (35 * factory.facing.y) - (32*facing90.y));
				health_some.draw(spriteBatch);
			} else {
				float factor = (health - Constants.highHealthThreshold) / (1 - Constants.highHealthThreshold);
				health_full.setOrigin(0, 0);
				health_full.setRotation(factory.facing.angle());
				health_full.setColor((1 - factor) * 0.3f + 0.7f, 1, factor * 0.4f + 0.6f, 1);
				health_full.setPosition(factory.collisionCenter.x - (35 * factory.facing.x) - (32 * facing90.x), 
						factory.collisionCenter.y - (35 * factory.facing.y) - (32 * facing90.y));
				health_full.draw(spriteBatch);
			}
		}

	}
	
	  /**
	   * Normalize a value to exist between 0 and 1 (inclusive).
	   * Mathematically the opposite of lerp(), figures out what proportion
	   * a particular value is relative to start and stop coordinates.
	   */
	  public float norm(float value, float start, float stop) {
	    return (value - start) / (stop - start);
	  }
}
