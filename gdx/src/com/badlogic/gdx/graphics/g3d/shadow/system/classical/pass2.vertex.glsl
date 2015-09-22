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

uniform mat4 u_projViewTrans;
uniform mat4 u_worldTrans;
uniform mat4 u_shadowMapProjViewTrans;
attribute vec3 a_position;
varying vec4 v_shadowMapUv;

void main()
{
	vec4 pos = u_worldTrans * vec4(a_position, 1.0);
	v_shadowMapUv = u_shadowMapProjViewTrans * pos;
	gl_Position = u_projViewTrans * pos;
}