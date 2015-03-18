/**
 * Shader to visualize normals
 * @author Xoppa
 */
[VS]
#include "g_attributes.glsl:VS"
#include "u_uniforms.glsl"
#include "skinning.glsl"
#include "common.glsl:VS"
 
void main() {
	g_position = u_worldTrans * applySkinning(g_position);
	gl_Position = u_projViewTrans * g_position;

#ifdef normalTextureFlag
	g_binormal = normalize(u_normalMatrix * applySkinning(g_binormal));
	g_tangent = normalize(u_normalMatrix * applySkinning(g_tangent));
	pushBinormal();
	pushTangent();
	pushTexCoord0();
#endif
	
	passNormalValue(normalize(u_normalMatrix * applySkinning(g_normal)));
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

void main() {
	pullNormal();
	
#ifdef normalTextureFlag
	pullBinormal();
	pullTangent();
	
	vec3 normal = normalize(texture2D(u_normalTexture, g_texCoord0).xyz * 2.0 - 1.0);
	g_normal = normalize((g_tangent * normal.x) + (g_binormal * normal.y) + (g_normal * normal.z));
#endif
	
	gl_FragColor = vec4( normalize( g_normal * vec3( 0.5 ) + vec3( 0.5 ) ), 1.0 );
}
