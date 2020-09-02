[VS]
#include "skinning.glsl"

attribute vec3 a_position;
uniform mat4 u_projViewWorldTrans;

varying float v_depth;

void main() {
	vec4 pos = u_projViewWorldTrans * applySkinning(vec4(a_position, 1.0));

	v_depth = pos.z * 0.5 + 0.5;

	gl_Position = pos;
}
[FS]
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

#include "common.glsl:FS"

varying float v_depth;

void main() {
	float depth = v_depth;
	const vec4 bias = vec4(1.0 / 255.0, 1.0 / 255.0, 1.0 / 255.0, 0.0);
	vec4 color = vec4(depth, fract(depth * 255.0), fract(depth * 65025.0), fract(depth * 16581375.0));
	gl_FragColor = color - (color.yzww * bias);
}