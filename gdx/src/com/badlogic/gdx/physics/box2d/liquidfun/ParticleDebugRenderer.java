
package com.badlogic.gdx.physics.box2d.liquidfun;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

/** Renders all particles from a given {@link ParticleSystem}
 * @author FinnStr */
public class ParticleDebugRenderer {

	protected ShapeRenderer renderer;

	public ParticleDebugRenderer () {
		renderer = new ShapeRenderer();
	}

	public void render (ParticleSystem pSystem, Matrix4 projMatrix) {
		renderer.setProjectionMatrix(projMatrix);

		renderer.begin(ShapeType.Filled);
		Array<Vector2> positions = pSystem.getParticlePositionBuffer();
		Array<Color> colors = pSystem.getParticleColorBuffer();

		for (int i = 0; i < pSystem.getParticleCount(); i++) {
			renderer.setColor(colors.get(i));
			renderer.circle(positions.get(i).x, positions.get(i).y, pSystem.getParticleRadius(), 8);
		}

		renderer.end();
	}
}
