
[VS]
#include ":defaultVS"
#include ":fogVS"

[FS]
#include ":defaultFS"

[lights]
// Declare all lighting uniforms
#include ":ambient"
#include ":pointLights"
#include ":dirLights"

[default]
// Declare methods for the default lighting algorithm
#include "common.glsl:separateAmbientFlag"
#include ":ambient"

	varying vec3 v_lightAmbient;	
	varying vec3 v_lightDiffuse;
	varying vec3 v_lightSpecular;
	
 	vec3 g_lightAmbient = vec3(0.0);
	vec3 g_lightDiffuse = vec3(0.0);
	vec3 g_lightSpecular = vec3(0.0);

[defaultVS]
#include ":default"

#ifdef lightingFlag
	#define passLightAmbient()	(v_lightAmbient = g_lightAmbient)
	#define passLightDiffuse()	(v_lightDiffuse = g_lightDiffuse)
	#define passLightSpecular()	(v_lightSpecular = g_lightSpecular)
#else
	#define passLightAmbient() nop()
	#define passLightDiffuse() nop()
	#define passLightSpecular()	nop()
#endif
	#define passLights() { passLightAmbient(); passLightDiffuse(); passLightSpecular(); }
 
[defaultFS]
#include ":default"

#ifdef lightingFlag
	#define pullLightAmbient()	(g_lightAmbient = v_lightAmbient)
	#define pullLightDiffuse()	(g_lightDiffuse = v_lightDiffuse)
	#define pullLightSpecular()	(g_lightSpecular = v_lightSpecular)
#else
	#define pullLightAmbient() nop()
	#define pullLightDiffuse() nop()
	#define pullLightSpecular()	nop()
#endif
	#define pullLights() { pullLightAmbient(); pullLightDiffuse(); pullLightSpecular(); }
	
[fogVS]
#include "common.glsl:nop"
 	varying float v_fog;
#ifdef fogFlag
	float calculateFog(const in float distanceSq, const in float camW)	{
		return min(distanceSq * camW, 1.0);
	}
	float calculateFog(const in vec3 distance, const in float camW) {
		return calculateFog(dot(distance, distance), camW) 
	}
	float calculateFog(const in vec4 position, const in vec4 camPos) {
		return calculateFog(camPos.xyz - position.xyz, camPos.w)
	}
	#define passFog(val) (v_fog = val)
#else
	#define calculateFog(distanceSq, camW) (1.0)
	#define passFog(val) nop()
#endif
	#define pushFog(val) (v_fog = val)

[ambient]
#include ":ambientLight"
#include ":ambientCubemap"
 
#if defined(ambientLightFlag) && defined(ambientCubemapFlag)
	#define getAmbient(normal) (getAmbientLight() + getAmbientCubeLight(normal))
#elif defined(ambientLightFlag)
	#define getAmbient(normal) getAmbientLight()
#elif defined(ambientCubemapFlag)
	#define getAmbient(normal) getAmbientCubeLight(normal)
#else
	#define getAmbient(normal) (vec3(0.0))
#endif

[ambientLight]
 //////////////////////////////////////////////////////
 ////// AMBIENT LIGHT
 //////////////////////////////////////////////////////
#ifdef ambientLightFlag
	#ifndef ambientFlag
		#define ambientFlag
	#endif
 	uniform vec3 u_ambientLight;
	#define getAmbientLight() (u_ambientLight)
#else
	#define getAmbientLight() (vec3(0.0))
#endif


[ambientCubemap]
//////////////////////////////////////////////////////
////// AMBIENT CUBEMAP
//////////////////////////////////////////////////////
#ifdef ambientCubemapFlag
	#ifndef ambientFlag
		#define ambientFlag
	#endif
 	uniform vec3 u_ambientCubemap[6];
	vec3 getAmbientCubeLight(const in vec3 normal) {
		vec3 squaredNormal = normal * normal;
		vec3 isPositive  = step(0.0, normal);
		return squaredNormal.x * mix(u_ambientCubemap[0], u_ambientCubemap[1], isPositive.x) +
				squaredNormal.y * mix(u_ambientCubemap[2], u_ambientCubemap[3], isPositive.y) +
				squaredNormal.z * mix(u_ambientCubemap[4], u_ambientCubemap[5], isPositive.z);
	}
#else
	#define getAmbientCubeLight(normal) (vec3(0.0))
#endif


[dirLights]
//////////////////////////////////////////////////////
////// DIRECTIONAL LIGHTS
//////////////////////////////////////////////////////
#ifdef lightingFlag
	#if defined(numDirectionalLights) && (numDirectionalLights > 0)
		#define directionalLightsFlag
	#endif // numDirectionalLights
#endif //lightingFlag

#ifdef directionalLightsFlag
	struct DirectionalLight
	{
		vec3 color;
		vec3 direction;
	};
	uniform DirectionalLight u_dirLights[numDirectionalLights];
#endif
	
[pointLights]
//////////////////////////////////////////////////////
////// POINTS LIGHTS
//////////////////////////////////////////////////////
#ifdef lightingFlag
	#if defined(numPointLights) && (numPointLights > 0)
		#define pointLightsFlag
	#endif // numPointLights
#endif //lightingFlag
		
#ifdef pointLightsFlag
	struct PointLight
	{
		vec3 color;
		vec3 position;
		float intensity;
	};
	uniform PointLight u_pointLights[numPointLights];
#endif

