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

/**
 Pack a float into a vec4
 It allows to save the float in the texture with a 32 bits precision
*/
vec4 pack(HIGH float depth) {
	const vec4 bitSh = vec4(256 * 256 * 256, 256 * 256, 256, 1.0);
	const vec4 bitMsk = vec4(0, 1.0 / 256.0, 1.0 / 256.0, 1.0 / 256.0);
	vec4 comp = fract(depth * bitSh);
	comp -= comp.xxyz * bitMsk;
	return comp;
}

void main()
{
	gl_FragColor = pack(gl_FragCoord.z);
}
