[VS]
#include "g_attributes.glsl:VS"
#include "u_uniforms.glsl"
#include "skinning.glsl"
#include "common.glsl:VS"

// varying vec3 v_viewDir;
varying vec3 v_reflect;

void main() {
	g_position = applySkinning(g_position);
	g_normal = normalize(u_normalMatrix * applySkinning(g_normal));
	
	g_position = u_worldTrans * g_position;
	gl_Position = u_projViewTrans * g_position;
	
//	mat3 worldToTangent;
//	worldToTangent[0] = normalize(u_normalMatrix * g_tangent);
//	worldToTangent[1] = normalize(u_normalMatrix * cross(g_tangent, g_normal));
//	worldToTangent[2] = normalize(u_normalMatrix * g_normal);

	vec3 viewDir = normalize(u_cameraPosition.xyz - g_position.xyz);
	
	v_reflect = reflect(-viewDir, g_normal);
	
	pushTexCoord0();
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

#include "g_attributes.glsl:FS"
#include "u_uniforms.glsl"
#include "common.glsl:FS"

varying vec3 v_reflect;

#ifdef environmentCubemapFlag
uniform samplerCube u_environmentCubemap;
#endif

void main() {
	pullTexCoord0();
	
#ifdef normalTextureFlag
	vec4 N = vec4(normalize(texture2D(u_normalTexture, g_texCoord0).xyz * 2.0 - 1.0), 1.0);
#else
	vec4 N = vec4(0.0, 0.0, 1.0, 1.0);
#endif
	
	vec3 reflectDir = normalize(v_reflect + (vec3(0.0, 0.0, 1.0) - N.xyz));

#ifdef environmentCubemapFlag
	gl_FragColor = vec4(textureCube(u_environmentCubemap, reflectDir).rgb, 1.0);
#else
	gl_FragColor = vec4(reflectDir, 1.0);
#endif
}
