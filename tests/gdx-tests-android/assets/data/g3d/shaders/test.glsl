[VS]
#include "g_attributes.glsl:VS"
#include "u_uniforms.glsl"
#include "skinning.glsl"
#include "common.glsl:VS"
#include "common.glsl:tangentVectorsVS"
#include "lights.glsl:lights"


varying vec3 v_lightDir;
varying vec3 v_lightCol;
varying vec3 v_viewDir;
varying vec3 v_ambientLight;
#ifdef environmentCubemapFlag
varying vec3 v_reflect;
#endif
 
void main() {
	calculateTangentVectors();
	
	g_position = applySkinning(g_position);
	g_normal = normalize(u_normalMatrix * applySkinning(g_normal));
	g_binormal = normalize(u_normalMatrix * applySkinning(g_binormal));
	g_tangent = normalize(u_normalMatrix * applySkinning(g_tangent));
	
	g_position = u_worldTrans * g_position;
	gl_Position = u_projViewTrans * g_position;
	
	mat3 worldToTangent;
	worldToTangent[0] = g_tangent;
	worldToTangent[1] = g_binormal;
	worldToTangent[2] = g_normal;

	v_ambientLight = getAmbient(g_normal);
	
	v_lightDir = normalize(-u_dirLights[0].direction) * worldToTangent;
	v_lightCol = u_dirLights[0].color;
	vec3 viewDir = normalize(u_cameraPosition.xyz - g_position.xyz);
	v_viewDir = viewDir * worldToTangent;
#ifdef environmentCubemapFlag
	v_reflect = reflect(-viewDir, g_normal);
#endif
	
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
varying vec3 v_ambientLight;
#ifdef environmentCubemapFlag
varying vec3 v_reflect;
#endif

#ifdef environmentCubemapFlag
uniform samplerCube u_environmentCubemap;
#endif

#ifdef reflectionColorFlag
uniform vec4 u_reflectionColor;
#endif

#define saturate(x) clamp( x, 0.0, 1.0 )

void main() {
	pullColor();
	pullTexCoord0();
	
	vec4 diffuse = applyColorDiffuse(g_color);
	vec3 specular = fetchColorSpecular();
	
#ifdef normalTextureFlag
	vec4 N = vec4(normalize(texture2D(u_normalTexture, g_texCoord0).xyz * 2.0 - 1.0), 1.0);
#ifdef environmentCubemapFlag
	vec3 reflectDir = normalize(v_reflect + (vec3(0.0, 0.0, 1.0) - N.xyz));
#endif
#else
	vec4 N = vec4(0.0, 0.0, 1.0, 1.0);
#ifdef environmentCubemapFlag
	vec3 reflectDir = normalize(v_reflect);
#endif
#endif

	vec3 L = normalize(v_lightDir);
	vec3 V = normalize(v_viewDir);
	vec3 H = normalize(L + V);
	float NL = dot(N.xyz, L);
	float NH = max(0.0, dot(N.xyz, H));
	
	float specOpacity = 1.0; //(1.0 - diffuse.w);
	float spec = min(1.0, pow(NH, 10.0) * specOpacity);
	float selfShadow = saturate(4.0 * NL);

#ifdef environmentCubemapFlag
	vec3 environment = textureCube(u_environmentCubemap, reflectDir).rgb;
	specular *= environment;
#ifdef reflectionColorFlag
	diffuse.rgb = saturate(vec3(1.0) - u_reflectionColor.rgb) * diffuse.rgb + environment * u_reflectionColor.rgb;
#endif
#endif

	gl_FragColor = vec4(saturate((v_lightCol * diffuse.rgb) * NL), diffuse.w);
	gl_FragColor.rgb += v_ambientLight * diffuse.rgb;
	gl_FragColor.rgb += (selfShadow * spec) * specular;
}
