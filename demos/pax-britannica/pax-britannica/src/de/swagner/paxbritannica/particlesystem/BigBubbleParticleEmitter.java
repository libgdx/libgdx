package de.swagner.paxbritannica.particlesystem;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import de.swagner.paxbritannica.Resources;

public class BigBubbleParticleEmitter extends ParticleEmitter {

	public BigBubbleParticleEmitter() {
		super();
				
		life = 2.5f;
		damping =1f;
		
		set(Resources.getInstance().bigbubble);
	}

	public void addParticle(Vector2 position) {
		addParticle(position, random.set(MathUtils.random() * 1.2f - 0.05f, 0.02f + MathUtils.random() * 1.2f) ,life, 1);
	}	

}
