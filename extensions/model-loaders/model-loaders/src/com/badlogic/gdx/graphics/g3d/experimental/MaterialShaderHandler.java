
package com.badlogic.gdx.graphics.g3d.experimental;

import com.badlogic.gdx.graphics.g3d.lights.LightManager;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;

public class MaterialShaderHandler {

	private LightManager lightManager;

	public MaterialShaderHandler (LightManager lightManager) {
		this.lightManager = lightManager;
	}

	private final Array<Material> materialsWithShader = new Array<Material>(false, 64, Material.class);

	public ShaderProgram getShader (Material material) {

		for (int i = 0; i < materialsWithShader.size; i++) {
			if (material.shaderEquals(materialsWithShader.items[i])) {
				material.shader = materialsWithShader.items[i].shader;
				return material.shader;
			}
		}

		material.shader = ShaderFactory.createShader(material, lightManager);
		materialsWithShader.add(material);
		return material.shader;
	}

	public void dispose () {
		for (int i = 0; i < materialsWithShader.size; i++) {
			if (materialsWithShader.items[i].shader != null) {
				materialsWithShader.items[i].shader.dispose();
				materialsWithShader.items[i].shader = null;
			}
		}
	}
}
