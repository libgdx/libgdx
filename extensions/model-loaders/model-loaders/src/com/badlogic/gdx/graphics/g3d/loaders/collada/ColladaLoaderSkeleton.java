/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.badlogic.gdx.graphics.g3d.loaders.collada;

import java.io.InputStream;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.ModelLoaderHints;
import com.badlogic.gdx.graphics.g3d.loaders.SkeletonModelLoader;
import com.badlogic.gdx.graphics.g3d.model.skeleton.Skeleton;
import com.badlogic.gdx.graphics.g3d.model.skeleton.SkeletonAnimation;
import com.badlogic.gdx.graphics.g3d.model.skeleton.SkeletonJoint;
import com.badlogic.gdx.graphics.g3d.model.skeleton.SkeletonModel;
import com.badlogic.gdx.graphics.g3d.model.skeleton.SkeletonSubMesh;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

public class ColladaLoaderSkeleton  implements SkeletonModelLoader {
	@Override
	public SkeletonModel load(FileHandle handle, ModelLoaderHints hints) {
		return loadSkeletonModel(handle);
	}
	
	public static SkeletonModel loadSkeletonModel (FileHandle handle) {
		return loadSkeletonModel(handle.read());
	}
	
	public static SkeletonModel loadSkeletonModel (InputStream in) {
		XmlReader xml = new XmlReader();
		Element root = null;
		try {
			root = xml.parse(in);
		} catch (Exception e) {
			throw new GdxRuntimeException("Couldn't load Collada model", e);
		}

		// get geometries
		Array<Geometry> geos = ColladaLoader.readGeometries(root);
		
		Array<Skin> skins = readSkins(root);
		
		Joint joint = readJoint(root);
		
		Animation anim = readAnim(root);

		// convert geometries to meshes
		SkeletonSubMesh[] meshes = createSkeletonSubMeshes(geos, skins);
		
		Skeleton skeleton = createSkeleton(skins, joint.joint,anim);

		// create SkeletonModel
		SkeletonModel model = new SkeletonModel(skeleton,meshes);
		return model;
	}
	
	private static Array<Skin> readSkins (Element root) {
		// check whether the library_controllers element is there
		Element colladaGeoLibrary = root.getChildByName("library_controllers");
		if (colladaGeoLibrary == null) throw new GdxRuntimeException("not <library_controllers> element in file");

		// check for controller
		Element colladaController = colladaGeoLibrary.getChildByName("controller");
		if (colladaController == null) throw new GdxRuntimeException("no <controller> elements in file");

		// check for controller
		Array<Element> colladaSkin = colladaController.getChildrenByName("skin");
		if (colladaController == null) throw new GdxRuntimeException("no <controller> elements in file");
		
		Array<Skin> skins = new Array<Skin>();
		
		for (int i = 0; i < colladaSkin.size; i++) {
			skins.add(new Skin(colladaSkin.get(i)));
		}
		
		return skins;
	}
	
	private static Joint readJoint (Element root) {
		// check whether the library_controllers element is there
		Element colladaLibrary = root.getChildByName("library_visual_scenes");
		if (colladaLibrary == null) throw new GdxRuntimeException("not <library_visual_scenes> element in file");

		// check for controller
		Element colladaScene = colladaLibrary.getChildByName("visual_scene");
		if (colladaScene == null) throw new GdxRuntimeException("no <controller> elements in file");

		return new Joint(colladaScene);
	}
	
	private static Animation readAnim (Element root) {
		// check whether the library_controllers element is there
		Element colladaAnimationLib = root.getChildByName("library_animations");
		if (colladaAnimationLib == null) throw new GdxRuntimeException("not <library_animations> element in file");

		return new Animation(colladaAnimationLib);
	}
	
	private static Skeleton createSkeleton(Array<Skin> skins, SkeletonJoint joint, Animation anim)
	{
		Skeleton skeleton = new Skeleton();
		
		for(int i=0;i<joint.children.size;i++){
			skeleton.hierarchy.add(joint.children.get(i));
		}
		
		skeleton.buildFromHierarchy();
		
		float[] times = anim.inputMap.values().next();
		float totalDuration = times[times.length-1];

		
		skeleton.animations.put("My Animation", new SkeletonAnimation("My Animation", totalDuration, anim.keyFrames));
		
		return skeleton;
	}
	
	private static SkeletonSubMesh[] createSkeletonSubMeshes (Array<Geometry> geos, Array<Skin> skins) {
		SkeletonSubMesh[] meshes = new SkeletonSubMesh[geos.size];
		for (int i = 0; i < geos.size; i++) {
			meshes[i] = geos.get(i).getSkeletonSubMesh(skins.get(i));
		}
		return meshes;
	}
}