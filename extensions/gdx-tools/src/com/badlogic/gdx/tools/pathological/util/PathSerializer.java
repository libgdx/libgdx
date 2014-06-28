
package com.badlogic.gdx.tools.pathological.util;

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

		System.out.println(json);
	}

	@Override
	public Path<Vector2> read (Json json, JsonValue jsonData, Class type) {
		Array<Vector2> temp = new Array<>(4);
		return null;
	}

}
