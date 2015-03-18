
#include "lights.glsl:default"
#include "lights.glsl:ambient"
#include ":dirLights"
#include ":pointLights"
 
#ifdef lightingFlag
	void applyLights(const in vec4 position, const in vec3 viewVec, const in vec3 normal, const in float shininess) {
		#if defined(ambientFlag)
			#if defined(separateAmbientFlag)
		g_lightAmbient = getAmbient(normal);
			#else
		g_lightDiffuse = getAmbient(normal);
			#endif
		#endif
		applyDirectionalLights(g_lightDiffuse, g_lightSpecular, viewVec, normal, shininess);
		applyPointLights(g_lightDiffuse, g_lightSpecular, position, viewVec, normal, shininess);
	}
#else
	#define applyLights(position, viewVec, normal, shininess) nop()
#endif

[common]
#define gouraudSpecularComponent(normal, lightDir, viewDir, shininess) pow(dot(normal, normalize((lightDir) + (viewDir))), shininess)


[dirLights]
#include "common.glsl:specularFlag"
#include "common.glsl:nop"
#include "lights.glsl:dirLights"
#include ":common"

#ifdef directionalLightsFlag
	/** Apply the directional lights to the diffuse argument using a basic lighting algorithm */
	void applyDiffuseDirectionalLights(inout vec3 diffuse, const in vec3 normal) {
		for (int i = 0; i < numDirectionalLights; i++) {
			vec3 lightDir = -u_dirLights[i].direction;
			float NdotL = clamp(dot(normal, lightDir), 0.0, 1.0);
			diffuse += u_dirLights[i].color * NdotL;
		}
	}
	
	/** Apply the directional lights to both the diffuse and specular arguments using a basic lighting algorithm */
	void applyDiffuseAndSpecularDirectionalLights(inout vec3 diffuse, inout vec3 specular, const in vec3 viewVec, const in vec3 normal, const in float shininess) {
		for (int i = 0; i < numDirectionalLights; i++) {
			vec3 lightDir = -u_dirLights[i].direction;
			float NdotL = clamp(dot(normal, lightDir), 0.0, 1.0);
			diffuse += u_dirLights[i].color * NdotL;
			specular += u_dirLights[i].color * NdotL * gouraudSpecularComponent(normal, lightDir, viewVec, shininess);
			//float halfDotView = dot(normal, normalize(lightDir + viewVec));
			//specular += u_dirLights[i].color * clamp(NdotL * pow(halfDotView, shininess), 0.0, 1.0);
		}
	}
	#ifdef specularFlag
	#define applyDirectionalLights(diffuse, specular, viewVec, normal, shininess) applyDiffuseAndSpecularDirectionalLights(diffuse, specular, viewVec, normal, shininess)
	#else
	#define applyDirectionalLights(diffuse, specular, viewVec, normal, shininess) applyDiffuseDirectionalLights(diffuse, normal) 
	#endif
#else
	#define applyDiffuseDirectionalLights(diffuse, normal) nop()
	#define applyDiffuseAndSpecularDirectionalLights(diffuse, specular, viewVec, normal, shininess)	nop()
	#define applyDirectionalLights(diffuse, specular, viewVec, normal, shininess) nop() 
#endif





[pointLights]
#include "common.glsl:nop"
#include "common.glsl:specularFlag"
#include "lights.glsl:pointLights"
#include ":common"

#ifdef pointLightsFlag
	void applyDiffusePointLights(inout vec3 diffuse, const in vec3 pos, const in vec3 normal) {
		for (int i = 0; i < numPointLights; i++) {
			vec3 lightDir = u_pointLights[i].position - pos;
			float dist2 = dot(lightDir, lightDir);
			lightDir *= inversesqrt(dist2);
			float NdotL = clamp(dot(normal, lightDir), 0.0, 2.0);
			float falloff = clamp(u_pointLights[i].intensity / (1.0 + dist2), 0.0, 2.0); // FIXME mul intensity on cpu
			diffuse += u_pointLights[i].color * (NdotL * falloff);
		}
	}
	
	void applyDiffusePointLights(inout vec3 diffuse, const in vec4 pos, const in vec3 normal) {
		applyDiffusePointLights(diffuse, pos.xyz, normal);
	}
	
	void applyDiffuseAndSpecularPointLights(inout vec3 diffuse, inout vec3 specular, const in vec3 pos, const in vec3 viewVec, const in vec3 normal, const in float shininess) {
		for (int i = 0; i < numPointLights; i++) {
			vec3 lightDir = u_pointLights[i].position - pos;
			float dist2 = dot(lightDir, lightDir);
			lightDir *= inversesqrt(dist2);
			float NdotL = clamp(dot(normal, lightDir), 0.0, 2.0);
			float falloff = clamp(u_pointLights[i].intensity / (1.0 + dist2), 0.0, 2.0); // FIXME mul intensity on cpu
			diffuse += u_pointLights[i].color * (NdotL * falloff);
			float halfDotView = clamp(dot(normal, normalize(lightDir + viewVec)), 0.0, 2.0);
			specular += u_pointLights[i].color * clamp(NdotL * pow(halfDotView, shininess) * falloff, 0.0, 2.0);
		}
	}
	
	void applyDiffuseAndSpecularPointLights(inout vec3 diffuse, inout vec3 specular, const in vec4 pos, const in vec3 viewVec, const in vec3 normal, const in float shininess) {
		applyDiffuseAndSpecularPointLights(diffuse, specular, pos.xyz, viewVec, normal, shininess);
	}
	#ifdef specularFlag
	#define applyPointLights(diffuse, specular, pos, viewVec, normal, shininess) applyDiffuseAndSpecularPointLights(diffuse, specular, pos, viewVec, normal, shininess)
	#else
	#define applyPointLights(diffuse, specular, pos, viewVec, normal, shininess) applyDiffusePointLights(diffuse, pos, normal)
	#endif
#else
	#define applyDiffusePointLights(diffuse, pos, normal) nop()
	#define applyDiffuseAndSpecularPointLights(diffuse, specular, pos, viewVec, normal, shininess) nop()
	#define applyPointLights(diffuse, specular, pos, viewVec, normal, shininess) nop()
#endif
