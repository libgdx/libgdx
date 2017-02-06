#ifdef GL_ES
precision mediump float;
precision mediump sampler2DArray;
#endif

uniform sampler2DArray u_textureArray;

in vec3 v_texCoords;

out vec4 color;

void main() {
	vec4 currentLayer = texture(u_textureArray, v_texCoords);
	vec4 nextLayer = texture(u_textureArray, v_texCoords + vec3(0.0, 0.0, 1.0));

    float interp = fract(v_texCoords.z - 0.5);
    color.rgb = mix(currentLayer.rgb, nextLayer.rgb, interp);
}
