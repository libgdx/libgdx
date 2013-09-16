package com.badlogic.gdx.tests.g3d.shaders;

import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;

public class MultiPassShader extends DefaultShader {
	public static int passes = 10;
	
	protected final int u_pass = register(new Uniform("u_pass"));
	
	public MultiPassShader (String vertexShader, String fragmentShader, Renderable renderable, boolean lighting, 
		boolean environmentCubemap, boolean shadowMap, boolean fog, int numDirectional, int numPoint, int numSpot, int numBones) {
		super(vertexShader, fragmentShader, renderable, lighting, environmentCubemap, shadowMap, fog, numDirectional, numPoint, numSpot, numBones);
	}

	@Override
	public void render (Renderable renderable) {
		set(u_pass, 0f);
		super.render(renderable);
		context.setDepthTest(GL10.GL_LESS);
		if (has(u_pass)) {
			context.setBlending(true, GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
			for (int i = 1; i < passes; ++i) {
				set(u_pass, (float)i/(float)passes);
				renderable.mesh.render(program, renderable.primitiveType, renderable.meshPartOffset, renderable.meshPartSize, false);
			}
		}
	}
}
