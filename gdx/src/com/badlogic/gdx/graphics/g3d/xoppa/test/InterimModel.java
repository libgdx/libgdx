package com.badlogic.gdx.graphics.g3d.xoppa.test;

import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.materials.MaterialAttribute;
import com.badlogic.gdx.graphics.g3d.model.Model;
import com.badlogic.gdx.graphics.g3d.model.SubMesh;
import com.badlogic.gdx.graphics.g3d.xoppa.RenderInstance;
import com.badlogic.gdx.graphics.g3d.xoppa.Renderable;
import com.badlogic.gdx.graphics.g3d.xoppa.materials.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.xoppa.materials.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.xoppa.materials.NewMaterial;
import com.badlogic.gdx.graphics.g3d.xoppa.materials.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.xoppa.utils.TextureDescription;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class InterimModel implements NewModel {
	public final Array<Renderable> parts;
	
	public final static NewMaterial convertMaterial(final Material mtl) {
		NewMaterial result = new NewMaterial();
		for (MaterialAttribute attr : mtl) {
			if (attr instanceof com.badlogic.gdx.graphics.g3d.materials.BlendingAttribute) {
				com.badlogic.gdx.graphics.g3d.materials.BlendingAttribute a = (com.badlogic.gdx.graphics.g3d.materials.BlendingAttribute)attr;
				result.add(new BlendingAttribute(a.blendSrcFunc, a.blendDstFunc));
			} else if (attr instanceof com.badlogic.gdx.graphics.g3d.materials.ColorAttribute) {
				com.badlogic.gdx.graphics.g3d.materials.ColorAttribute a = (com.badlogic.gdx.graphics.g3d.materials.ColorAttribute)attr;
				if (a.name.compareTo(com.badlogic.gdx.graphics.g3d.materials.ColorAttribute.specular)==0)
					result.add(ColorAttribute.createSpecular(a.color));
				else
					result.add(ColorAttribute.createDiffuse(a.color));
			} else if (attr instanceof com.badlogic.gdx.graphics.g3d.materials.TextureAttribute) {
				com.badlogic.gdx.graphics.g3d.materials.TextureAttribute a = (com.badlogic.gdx.graphics.g3d.materials.TextureAttribute)attr;
				final TextureDescription tex = new TextureDescription(a.texture, a.minFilter, a.magFilter, a.uWrap, a.vWrap);
				if (a.name.compareTo(com.badlogic.gdx.graphics.g3d.materials.TextureAttribute.specularTexture)==0)
					result.add(new TextureAttribute(TextureAttribute.Specular, tex));
				else // if (a.name.compareTo(com.badlogic.gdx.graphics.g3d.materials.TextureAttribute.diffuseTexture)==0)
					result.add(new TextureAttribute(TextureAttribute.Diffuse, tex));
				// else unknown texture attribute
			} // else throw new GdxRuntimeException("Unkown material attribute");
		}
		return result;
	}
	
	public InterimModel(final Model model) {
		SubMesh[] meshes = model.getSubMeshes();
		parts = new Array<Renderable>(meshes.length);
		for (int i = 0; i < meshes.length; i++) {
			final Renderable inst = new Renderable();
			inst.mesh = meshes[i].mesh;
			inst.meshPartOffset = 0;
			inst.meshPartSize = meshes[i].mesh.getMaxIndices() > 0 ? meshes[i].mesh.getNumIndices() : meshes[i].mesh.getNumVertices();
			inst.primitiveType = meshes[i].primitiveType;
			inst.material = convertMaterial(meshes[i].material);
			parts.add(inst);
		}
	}
	
	public InterimModel(final Renderable... parts) {
		this.parts = new Array<Renderable>(parts);
	}
	
	public InterimModel(final Array<Renderable> parts) {
		this.parts = new Array<Renderable>(parts);
	}

	@Override
	public Iterable<Renderable> getParts (float distance) {
		return parts;
	}
}
