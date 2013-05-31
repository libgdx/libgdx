package com.badlogic.gdx.graphics.g3d.shaders.subshaders;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.materials.Material.Attribute;
import com.badlogic.gdx.graphics.g3d.materials.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class DiffuseTextureShader extends BaseSubShader {
	private boolean enabled;
	
	@Override
	public void init (Renderable renderable) {
		if(renderable.material.has(TextureAttribute.Diffuse)) {
			vertexVars.addAll(new String[] {
				"attribute vec2 a_texCoord0;",
				"varying vec2 v_texCoords0;"
			});
			vertexCode.addAll(new String[] {
				"v_texCoords0 = a_texCoord0;"
			});
			fragmentVars.addAll(new String[] {
				"uniform sampler2D u_diffuseTexture;",
				"varying MED vec2 v_texCoords0;"
			});
			fragmentCode.addAll(new String[] {
				"color = texture2D(u_diffuseTexture, v_texCoords0);"
			});
			enabled = true;
		}
	}

	@Override
	public void apply (ShaderProgram program, RenderContext context, Camera camera, Renderable renderable) {
		if(enabled) {
			TextureAttribute attribute = (TextureAttribute)renderable.material.get(TextureAttribute.Diffuse);
			int unit = context.textureBinder.bind(attribute.textureDescription);
			program.setUniformi("u_diffuseTexture", unit);
		}
	}
}
