package de.swagner.paxbritannica.mainmenu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

import de.swagner.paxbritannica.Resources;

public class FactorySelector extends Sprite {

	public boolean picked = false;
	public boolean playerSelect = false;
	public boolean cpuSelect = false;

	public BoundingBox collision = new BoundingBox();
	public BoundingBox collisionPlayerSelect = new BoundingBox();
	public BoundingBox collisionCPUSelect = new BoundingBox();
	public Vector3 collisionMinVector = new Vector3();
	public Vector3 collisionMaxVector = new Vector3();
	
	private float fade = 0.2f;
	private float fadeButton = 0.0f;

	private float pulse_time = 0;	
	
	private Sprite button;
	private Sprite aCpuButton;
	private Sprite aPlayerButton;
	private Sprite cpuButton;
	private Sprite playerButton;
	
	float delta;
	
	private Vector2 position = new Vector2();
	
//	Mesh darkSection;
//	Mesh lightSection;
	
	public FactorySelector(Vector2 position, int id) {
		super();
		this.position = position;
		this.setPosition(position.x, position.y);

		switch (id) {
		case 1:
			this.set(Resources.getInstance().factoryP1);
			break;
		case 2:
			this.set(Resources.getInstance().factoryP2);
			break;
		case 3:
			this.set(Resources.getInstance().factoryP3);
			break;
		default:
			this.set(Resources.getInstance().factoryP4);
			break;
		}
		setRotation(90);
		this.setPosition(position.x, position.y);
		this.setColor(0, 0, 0, 1);

		button = new Sprite(Resources.getInstance().aButton);
		button.setPosition(position.x+70f,position.y + 35.f);
		
		aCpuButton = new Sprite(Resources.getInstance().aCpuButton);
		aCpuButton.setPosition(position.x+70f,position.y + 35.f);
		
		aPlayerButton = new Sprite(Resources.getInstance().aPlayerButton);
		aPlayerButton.setPosition(position.x+70f,position.y + 35.f);
		
		cpuButton = new Sprite(Resources.getInstance().cpuButton);
		cpuButton.setPosition(position.x+30f,position.y -0.f);
		
		playerButton = new Sprite(Resources.getInstance().playerButton);
		playerButton.setPosition(position.x+30f,position.y + 70.f);
		
		float pulse = (1 + MathUtils.cos((pulse_time/180.f)*2.f*MathUtils.PI))/2.f;
		float color = fade * pulse + 1 * (1-pulse);
		this.setColor(color, color, color, 1);
		button.setColor(color, color, color, 1);
		cpuButton.setColor(color, color, color, 1);	
	}
	
	public void reset() {
		picked = false;
		cpuSelect = false;
		playerSelect = false;
		
		fade = 0.2f;
		fadeButton = 0.0f;

		pulse_time = 0;		
		float pulse = (1 + MathUtils.cos((pulse_time/180.f)*2.f*MathUtils.PI))/2.f;
		float color = fade * pulse + 1 * (1-pulse);
		this.setColor(color, color, color, 1);
		button.setColor(color, color, color, 1);
		cpuButton.setColor(color, color, color, 1);	
	}

	@Override
	public void draw(SpriteBatch spriteBatch) {
		
		
		delta = Math.min(0.06f, Gdx.graphics.getDeltaTime());
		
		super.draw(spriteBatch);
		
		collisionMinVector.set(this.getVertices()[0], this.getVertices()[1], -10);
		collisionMaxVector.set(this.getVertices()[10], this.getVertices()[11], 10);
		collision.set(collisionMinVector,collisionMaxVector);
		
		collisionMinVector.set(this.getVertices()[0], this.getVertices()[1], -10);
		collisionMaxVector.set(this.getVertices()[10], this.getVertices()[11], 10);
		collisionMinVector.y += ((this.getVertices()[11]-this.getVertices()[1])/2);
		collisionPlayerSelect.set(collisionMinVector,collisionMaxVector);
		
		collisionMinVector.set(this.getVertices()[0], this.getVertices()[1], -10);
		collisionMaxVector.set(this.getVertices()[10], this.getVertices()[11], 10);
		collisionMaxVector.y -= ((this.getVertices()[11]-this.getVertices()[1])/2);
		collisionCPUSelect.set(collisionMinVector,collisionMaxVector);
		
		
		pulse_time += Gdx.graphics.getDeltaTime();

		float pulse = (1 + MathUtils.cos((pulse_time/5.f)*2.f*MathUtils.PI))/2.f;
		float color = fade * pulse + 1 * (1-pulse);
				
		if(picked && !(playerSelect || cpuSelect)) {
			button.draw(spriteBatch);
			button.setColor(0.2f, 0.2f, 0.2f, 1);
		} else {
			if(playerSelect) {
				aPlayerButton.draw(spriteBatch);
				aPlayerButton.setColor(color, color, color, 1);
			} else if(cpuSelect) {
				aCpuButton.draw(spriteBatch);
				aCpuButton.setColor(color, color, color, 1);
			} else {
				button.draw(spriteBatch);
				button.setColor(color, color, color, 1);
			}
		}
		
		if(picked && !(playerSelect || cpuSelect)) {
			fade = 0.2f;
		    this.setColor(fade, fade, fade, 1);
			
		    fadeButton = Math.min(fadeButton +delta, 1);
			cpuButton.setColor(fadeButton, fadeButton, fadeButton, 1);
		    cpuButton.draw(spriteBatch);
		    
			playerButton.setColor(fadeButton, fadeButton, fadeButton, 1);
		    playerButton.draw(spriteBatch);

		} else if(playerSelect || cpuSelect) {
		    fade = Math.min(fade +delta, 1);
		    this.setColor(fade, fade, fade, 1);
		    
			fadeButton = Math.max(fadeButton -delta, 0);
			if(cpuSelect) {
				cpuButton.setColor(0, 0, 0, fadeButton);
			    cpuButton.draw(spriteBatch);
			} else {
				cpuButton.setColor(fadeButton, fadeButton, fadeButton, fadeButton);
			    cpuButton.draw(spriteBatch);
			}
		    
			if(playerSelect) {
				playerButton.setColor(0, 0, 0, fadeButton);
			    playerButton.draw(spriteBatch);
			} else {
				playerButton.setColor(fadeButton, fadeButton, fadeButton, fadeButton);
			    playerButton.draw(spriteBatch);
			}
		}

		
	}
}
