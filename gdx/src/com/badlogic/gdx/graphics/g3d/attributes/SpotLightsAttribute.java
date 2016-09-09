
package com.badlogic.gdx.graphics.g3d.attributes;

import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.environment.SpotLight;
import com.badlogic.gdx.utils.Array;

/** An {@link Attribute} which can be used to send an {@link Array} of {@link SpotLight} instances to the {@link Shader}. The
 * lights are stored by reference, the {@link #copy()} or {@link #SpotLightsAttribute(SpotLightsAttribute)} method
 * will not create new lights.
 * @author Xoppa */
public class SpotLightsAttribute extends Attribute {
	public final static String Alias = "spotLights";
	public final static long Type = register(Alias);

	public final static boolean is (final long mask) {
		return (mask & Type) == mask;
	}

	public final Array<SpotLight> lights;

	public SpotLightsAttribute () {
		super(Type);
		lights = new Array<SpotLight>(1);
	}

	public SpotLightsAttribute (final SpotLightsAttribute copyFrom) {
		this();
		lights.addAll(copyFrom.lights);
	}

	@Override
	public SpotLightsAttribute copy () {
		return new SpotLightsAttribute(this);
	}

	@Override
	public int hashCode () {
		int result = super.hashCode();
		for (SpotLight light : lights)
			result = 1237 * result + (light == null ? 0 : light.hashCode());
		return result;
	}
	
	@Override
	public int compareTo (Attribute o) {
		if (type != o.type) return type < o.type ? -1 : 1;
		return 0; // FIXME implement comparing
	}
}
