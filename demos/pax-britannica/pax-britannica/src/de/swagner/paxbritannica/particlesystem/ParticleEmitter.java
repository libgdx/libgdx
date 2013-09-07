package de.swagner.paxbritannica.particlesystem;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

public class ParticleEmitter extends Sprite {
	
	public int maxParticle = 500;
	
	protected Vector2 random = new Vector2();
	
	public float life = 1;
	float damping = 1;
	float delta_scale;
	Array<Particle> particles = new Array<Particle>(false,maxParticle);
	private Pool<Particle> freeParticles = new Pool<Particle>(maxParticle,maxParticle) {
		@Override
		protected Particle newObject() {
			return new Particle();
		}
	};

	float delta;

	@Override
	public void draw(SpriteBatch spriteBatch) {
		delta = Math.min(0.06f, Gdx.graphics.getDeltaTime());
		
		this.setOrigin(0,0);
		for (int i = particles.size - 1; i >= 0; i--) {
			Particle particle = particles.get(i);
			if (particle.life > 0) {
				updateParticle(particle);
				float dx = this.getWidth() / 2 * particle.scale;
				float dy = this.getHeight() / 2 * particle.scale;
				this.setColor(1, 1, 1, Math.max(particle.life / this.life,0));	
				this.setScale(particle.scale);
				this.setPosition(particle.position.x -dx, particle.position.y -dy);
				if(!(particle.position.y -dy>=-10 && particle.position.y -dy<=10) && !(particle.position.x -dx>=-10 && particle.position.x -dx<=10)) {
					super.draw(spriteBatch);
				} else {
					particle.life = 0;
				}
			} else {
				particles.removeIndex(i);
				freeParticles.free(particle);
			}
		}
		
	}

	private void updateParticle(Particle particle) {
		delta = Math.min(0.06f, Gdx.graphics.getDeltaTime());
		
		if (particle.life > 0) {
			particle.life -= delta;
			particle.position.add(particle.velocity.x * delta*10,particle.velocity.y * delta*10);
			particle.velocity.scl((float) Math.pow(damping, delta));
			particle.scale += this.delta_scale * delta/5f;
		}
	}
	
	public void addParticle(Vector2 position, Vector2 velocity, float life, float scale) {
	     if(particles.size>maxParticle) return;
	     if(Gdx.graphics.getFramesPerSecond()<25 && !(this instanceof ExplosionParticleEmitter)) return;
		 Particle particle = freeParticles.obtain();
	     particle.setup(position,velocity,life,scale);
	     particles.add(particle);
	}
	
	public void dispose() {
		particles.clear();
		freeParticles.clear();
	}

}
