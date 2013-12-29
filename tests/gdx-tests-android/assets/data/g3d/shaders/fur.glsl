[VS]
#include "g_attributes.glsl:VS"
#include "u_uniforms.glsl"
#include "skinning.glsl"
#include "common.glsl:VS"
#include "lights.glsl:lights"

uniform float u_pass;
varying float v_pass;
float furLength = u_pass * 0.25;

varying vec3 v_lightDir;
varying vec3 v_lightCol;
varying vec3 v_viewDir;
 
void main() {
	g_position = applySkinning(g_position);
	g_normal = normalize(applySkinning(g_normal));
	g_tangent = normalize(applySkinning(g_tangent));
	
	vec3 offset = furLength * g_normal; // furLength * ((abs(mod(u_time, 2.0) - 1.0) * 0.25) * g_tangent + g_normal);
	g_position = vec4(g_position.xyz + offset, g_position.w);
	g_position = u_worldTrans * g_position;
	gl_Position = u_projViewTrans * g_position;
	
	mat3 worldToTangent;
	worldToTangent[0] = normalize(u_normalMatrix * g_tangent);
	worldToTangent[1] = normalize(u_normalMatrix * cross(g_tangent, g_normal));
	worldToTangent[2] = normalize(u_normalMatrix * g_normal);
	
	v_lightDir = normalize(-u_dirLights[0].direction) * worldToTangent;
	v_lightCol = u_dirLights[0].color;
	v_viewDir = normalize(u_cameraPosition.xyz - g_position.xyz) * worldToTangent;
	v_pass = u_pass;
	
	pushColor();
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
#include "common.glsl:colorDiffuseFS"
#include "common.glsl:colorSpecularFS"

varying vec3 v_lightDir;
varying vec3 v_lightCol;
varying vec3 v_viewDir;

#define saturate(x) clamp( x, 0.0, 1.0 )

varying float v_pass;

void main() {
	pullColor();
	pullTexCoord0();
	
	vec4 diffuse = applyColorDiffuse(g_color);
	vec3 specular = fetchColorSpecular();

	vec4 N = vec4(0.0, 0.0, 1.0, 1.0);
	vec3 L = normalize(v_lightDir);
	vec3 V = normalize(v_viewDir);
	vec3 H = normalize(L + V);
	float NL = dot(N.xyz, L);
	float NH = max(0.0, dot(N.xyz, H));
	
	float specOpacity = (1.0 - diffuse.w);
	float spec = min(1.0, pow(NH, 10.0) * specOpacity);
	float selfShadow = saturate(4.0 * NL);
	
	gl_FragColor = vec4(v_lightCol * diffuse.rgb * NL, saturate(diffuse.w - v_pass));
	gl_FragColor.rgb += selfShadow * specular * (1.0 - v_pass) * spec;
}
