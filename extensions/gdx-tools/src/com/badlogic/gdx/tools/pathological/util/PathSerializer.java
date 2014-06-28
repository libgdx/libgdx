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

package com.badlogic.gdx.tools.pathological.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.BSpline;
import com.badlogic.gdx.math.Bezier;
import com.badlogic.gdx.math.CatmullRomSpline;
import com.badlogic.gdx.math.Path;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Json.Serializer;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.reflect.ClassReflection;

public class PathSerializer<T extends Path> implements Serializer<Path> {
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
					points = new Array<>(bspline.controlPoints);
					continuous = bspline.continuous;
					degree = bspline.degree;
				} else if (object instanceof CatmullRomSpline) {
					CatmullRomSpline<Vector2> catmullrom = (CatmullRomSpline<Vector2>)object;
					points = new Array<>(catmullrom.controlPoints);
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
				Array<Vector2> pointarray = new Array<>(points.size);
				for (JsonValue point = points.get(0); point != null; point = point.next()) {
					pointarray.add(new Vector2(point.getFloat("x", 0), point.getFloat("y", 0)));
				}

				switch (type) {
				case "Bezier": {
					path = new Bezier<>(pointarray, 0, pointarray.size);
					break;
				}
				case "BSpline": {
					int degree = pathjson.getInt("degree", 3);
					path = new BSpline(pointarray.shrink(), degree, continuous);
					break;
				}
				case "CatmullRomSpline": {
					path = new CatmullRomSpline(pointarray.shrink(), continuous);
				}
				default: {
					throw new GdxRuntimeException("Unknown Path type " + type);
				}
				}
			}
		}

		return path;
	}

}
