[VS]
#include "g_attributes.glsl:VS"
#include "u_uniforms.glsl"
#include "skinning.glsl"
#include "common.glsl:VS"

varying vec3 v_cubeMapUV;
 
void main() {
	g_position = u_worldTrans * applySkinning(g_position);
	v_cubeMapUV = normalize(g_position.xyz);
	gl_Position = u_projViewTrans * g_position;
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

varying vec3 v_cubeMapUV;

#ifdef environmentCubemapFlag
uniform samplerCube u_environmentCubemap;
#endif

void main() {

#ifdef environmentCubemapFlag
	gl_FragColor = vec4(textureCube(u_environmentCubemap, v_cubeMapUV).rgb, 1.0);
#else
	gl_FragColor = vec4(v_cubeMapUV, 1.0);
#endif
}
