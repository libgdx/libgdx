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

package com.badlogic.gdx.assets.loaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.SynchronousAssetLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.BSpline;
import com.badlogic.gdx.math.Bezier;
import com.badlogic.gdx.math.CatmullRomSpline;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Path;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.tools.pathological.util.PathSerializer;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;

/** Use this to load {@link Path}s created with the included path editor. {@link Bezier}s, {@link BSpline}s, and
 * {@link CatmullRomSpline}s are supported. Only 2D paths (i.e. with {@code Vector2}s) are supported.
 * @author Jesse Talavera-Greenberg */
public class PathLoader extends SynchronousAssetLoader<Path<Vector2>, PathLoader.PathParameters> {
	private Json json;

	public PathLoader (FileHandleResolver resolver) {
		super(resolver);
		this.json = new Json();
		PathSerializer ps = new PathSerializer();

		this.json.setSerializer(Path.class, ps);
		this.json.setSerializer(Bezier.class, ps);
		this.json.setSerializer(BSpline.class, ps);
		this.json.setSerializer(CatmullRomSpline.class, ps);
	}

	public static class PathParameters extends AssetLoaderParameters<Path<Vector2>> {
		public Matrix3 transform = new Matrix3();
	}

	@Override
	public Path<Vector2> load (AssetManager assetManager, String fileName, FileHandle file, PathParameters parameter) {
		Path<Vector2> path = this.json.fromJson(Path.class, file);
		if (parameter != null) {
			if (path instanceof Bezier) {
				Bezier<Vector2> bezier = (Bezier<Vector2>)path;
				Array<Vector2> points = bezier.points;
				for (Vector2 v : points) {
					v.mul(parameter.transform);
				}

				bezier.set(points, 0, points.size);
			} else if (path instanceof BSpline) {
				BSpline<Vector2> bspline = (BSpline<Vector2>)path;
				Vector2[] points = bspline.controlPoints;

				for (Vector2 v : points) {
					v.mul(parameter.transform);
				}

				bspline.set(points, bspline.degree, bspline.continuous);
			} else if (path instanceof CatmullRomSpline) {
				CatmullRomSpline<Vector2> crs = (CatmullRomSpline<Vector2>)path;
				Vector2[] points = crs.controlPoints;

				for (Vector2 v : points) {
					v.mul(parameter.transform);
				}

				crs.set(points, crs.continuous);
			} else {
				Gdx.app.error("PathLoader", "Unknown path type " + path.getClass() + ", not transforming");
			}
		}
		return path;
	}

	@Override
	public Array<AssetDescriptor> getDependencies (String fileName, FileHandle file, PathParameters parameter) {
		// TODO Auto-generated method stub
		return null;
	}
}
