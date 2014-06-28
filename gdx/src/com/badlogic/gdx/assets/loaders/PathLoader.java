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
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Json.Serializer;
import com.badlogic.gdx.utils.reflect.ClassReflection;

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

	public static class PathSerializer<T extends Path> implements Serializer<Path> {
		@Override
		public void write (Json json, Path object, Class knownType) {
			json.writeObjectStart();
			{
				json.writeObjectStart("path");
				{
					json.writeValue("type", ClassReflection.getSimpleName(object.getClass()));

					Array<Vector2> points;
					Integer degree = null;
					Boolean continuous = null;
					if (object instanceof Bezier) {
						points = ((Bezier<Vector2>)object).points;
					} else if (object instanceof BSpline) {
						BSpline<Vector2> bspline = (BSpline<Vector2>)object;
						points = new Array<Vector2>(bspline.controlPoints);
						continuous = bspline.continuous;
						degree = bspline.degree;
					} else if (object instanceof CatmullRomSpline) {
						CatmullRomSpline<Vector2> catmullrom = (CatmullRomSpline<Vector2>)object;
						points = new Array<Vector2>(catmullrom.controlPoints);
						continuous = catmullrom.continuous;
					} else {
						throw new GdxRuntimeException("Unknown path type " + object.getClass());
					}

					if (degree != null) {
						json.writeValue("degree", degree);
					}

					if (continuous != null) {
						json.writeValue("continuous", continuous);
					}

					json.writeArrayStart("points");
					{
						for (Vector2 v : points) {
							json.writeObjectStart();
							{
								json.writeValue("x", v.x);
								json.writeValue("y", v.y);
							}
							json.writeObjectEnd();
						}
					}
					json.writeArrayEnd();
				}
				json.writeObjectEnd();
			}
			json.writeObjectEnd();
		}

		@Override
		public Path<Vector2> read (Json json, JsonValue jsonData, Class cls) {
			Path<Vector2> path = null;
			JsonValue pathjson = jsonData.get("path");
			if (pathjson != null) {
				String type = pathjson.getString("type", null);
				JsonValue points = pathjson.get("points");
				if (type != null && points.isArray()) {
					boolean continuous = pathjson.getBoolean("continuous", false);
					Array<Vector2> pointarray = new Array<Vector2>(points.size);
					for (JsonValue point = points.get(0); point != null; point = point.next()) {
						pointarray.add(new Vector2(point.getFloat("x", 0), point.getFloat("y", 0)));
					}

					if ("Bezier".equals(type)) {
						path = new Bezier<Vector2>(pointarray, 0, pointarray.size);
					} else if ("BSpline".equals(type)) {
						int degree = pathjson.getInt("degree", 3);
						path = new BSpline(pointarray.shrink(), degree, continuous);
					} else if ("CatmullRomSpline".equals(type)) {
						path = new CatmullRomSpline(pointarray.shrink(), continuous);
					} else {
						throw new GdxRuntimeException("Unknown Path type " + type);
					}
				}
			}

			return path;
		}
	}
}
