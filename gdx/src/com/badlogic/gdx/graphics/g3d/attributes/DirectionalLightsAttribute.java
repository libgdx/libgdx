
package com.badlogic.gdx.graphics.g3d.attributes;

import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.utils.Array;

/** An {@link Attribute} which can be used to send an {@link Array} of {@link DirectionalLight} instances to the {@link Shader}.
 * The lights are stored by reference, the {@link #copy()} or {@link #DirectionalLightsAttribute(DirectionalLightsAttribute)}
 * method will not create new lights.
 * @author Xoppa */
public class DirectionalLightsAttribute extends Attribute {
	public final static String Alias = "directionalLights";
	public final static long Type = register(Alias);

	public final static boolean is (final long mask) {
		return (mask & Type) == mask;
	}

	public final Array<DirectionalLight> lights;

	public DirectionalLightsAttribute () {
		super(Type);
		lights = new Array<DirectionalLight>(1);
	}

	public DirectionalLightsAttribute (final DirectionalLightsAttribute copyFrom) {
		this();
		lights.addAll(copyFrom.lights);
	}

	@Override
	public DirectionalLightsAttribute copy () {
		return new DirectionalLightsAttribute(this);
	}

	@Override
	public int hashCode () {
		int result = super.hashCode();
		for (DirectionalLight light : lights)
			result = 1229 * result + (light == null ? 0 : light.hashCode());
		return result;
	}
	
	@Override
	public int compareTo (Attribute o) {
		if (type != o.type) return type < o.type ? -1 : 1;
		return 0; // FIXME implement comparing
	}
}
