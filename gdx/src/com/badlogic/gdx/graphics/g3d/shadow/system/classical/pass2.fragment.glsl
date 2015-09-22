#ifdef GL_ES
#define LOWP lowp
#define MED mediump
#define HIGH highp
precision mediump float;
#else
#define MED
#define LOWP
#define HIGH
#endif

uniform sampler2D u_shadowTexture;
uniform vec4 u_uvTransform;
uniform int u_lightQuantity;
varying vec4 v_shadowMapUv;

float unpack (vec4 colour) {
	const vec4 bitShifts = vec4(1.0 / (256.0 * 256.0 * 256.0),
								1.0 / (256.0 * 256.0),
								1.0 / 256.0,
								1);
	return dot(colour , bitShifts);
}

void main()
{
	const float bias = 0.005;
	vec3 depth = (v_shadowMapUv.xyz / v_shadowMapUv.w)*0.5+0.5;
	vec2 uv = u_uvTransform.xy + depth.xy * u_uvTransform.zw;
	float lenDepthMap = unpack(texture2D(u_shadowTexture, uv));
	float intensity = 1.0/u_lightQuantity;

	if (depth.x >= 0.0 &&
		depth.x <= 1.0 &&
		depth.y >= 0.0 &&
		depth.y <= 1.0
		) {
	    if( depth.z - lenDepthMap <= bias ) {
			intensity = 0.0;
		}
	}
	gl_FragColor = vec4(intensity);
}
