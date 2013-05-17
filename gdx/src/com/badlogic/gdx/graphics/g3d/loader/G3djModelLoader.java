package com.badlogic.gdx.graphics.g3d.loader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.model.data.ModelAnimation;
import com.badlogic.gdx.graphics.g3d.model.data.ModelNodeAnimation;
import com.badlogic.gdx.graphics.g3d.model.data.ModelNodeKeyframe;
import com.badlogic.gdx.graphics.g3d.model.data.ModelData;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMaterial;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMesh;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMeshPart;
import com.badlogic.gdx.graphics.g3d.model.data.ModelNodePart;
import com.badlogic.gdx.graphics.g3d.model.data.ModelNode;
import com.badlogic.gdx.graphics.g3d.model.data.ModelTexture;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMaterial.MaterialType;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedMap;

/**
 * Loads the JSON format written by the 
 * <a href="https://github.com/libgdx/fbx-conv">fbx-conv</a> tool.
 * 
 * @author mzechner
 *
 */
public class G3djModelLoader extends ModelLoader<AssetLoaderParameters<Model>> {
	public static String VERSION = "1.0";
	
	public G3djModelLoader() {
		this(null);
	}
	
	public G3djModelLoader(FileHandleResolver resolver) {
		super(resolver);
	}
	
	@Override
	protected ModelData loadModelData (FileHandle fileHandle, AssetLoaderParameters<Model> parameters) {
		return parseModel(fileHandle);
	}
	
	public Model load (FileHandle handle) {
		ModelData modelData = parseModel(handle);
		return new Model(modelData);
	}

	public ModelData parseModel (FileHandle handle) {
		JsonReader reader = new JsonReader();
		JsonValue json = reader.parse(handle);
		
		/*String version = (String)json.getString("version");
		if(version == null || !version.equals(VERSION)) {
			throw new GdxRuntimeException("No or wrong JSON format version given, should be " + VERSION + ", is " + version);
		}*/
		
		G3dModelLoader loader = new G3dModelLoader();
		return loader.parseModel(json, handle.parent().path());
	}
}
