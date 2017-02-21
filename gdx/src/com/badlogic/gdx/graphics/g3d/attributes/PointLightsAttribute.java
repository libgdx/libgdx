
package com.badlogic.gdx.graphics.g3d.attributes;

import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.utils.Array;

/** An {@link Attribute} which can be used to send an {@link Array} of {@link PointLight} instances to the {@link Shader}. The
 * lights are stored by reference, the {@link #copy()} or {@link #PointLightsAttribute(PointLightsAttribute)} method
 * will not create new lights.
 * @author Xoppa */
public class PointLightsAttribute extends Attribute {
	public final static String Alias = "pointLights";
	public final static long Type = register(Alias);

	public final static boolean is (final long mask) {
		return (mask & Type) == mask;
	}

	public final Array<PointLight> lights;

	public PointLightsAttribute () {
		super(Type);
		lights = new Array<PointLight>(1);
	}

	public PointLightsAttribute (final PointLightsAttribute copyFrom) {
		this();
		lights.addAll(copyFrom.lights);
	}

	@Override
	public PointLightsAttribute copy () {
		return new PointLightsAttribute(this);
	}

	@Override
	public int hashCode () {
		int result = super.hashCode();
		for (PointLight light : lights)
			result = 1231 * result + (light == null ? 0 : light.hashCode());
		return result;
	}
	
	@Override
	public int compareTo (Attribute o) {
		if (type != o.type) return type < o.type ? -1 : 1;
		return 0; // FIXME implement comparing
	}
}
